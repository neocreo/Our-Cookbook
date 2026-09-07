// Google Drive REST API v3 client. Uses fetch directly (no gapi library
// needed). Files are stored in the Drive appDataFolder — a hidden folder
// scoped to this app, accessible only with the drive.file scope.
//
// File format: each recipe is a JSON file named `{recipeId}.json` containing
// the full Recipe JSON. Tombstones (deletions) are tracked in
// `_tombstones.json`.

import type { Recipe } from '../types/recipe'

const DRIVE_API = 'https://www.googleapis.com/drive/v3'
const UPLOAD_API = 'https://www.googleapis.com/upload/drive/v3'

export interface DriveFile {
  id: string
  name: string
}

export class DriveApiClient {
  private token: string

  constructor(accessToken: string) {
    this.token = accessToken
  }

  private headers(): HeadersInit {
    return {
      Authorization: `Bearer ${this.token}`,
      'Content-Type': 'application/json',
    }
  }

  /** Find a file by name in appDataFolder. Returns null if not found. */
  async findFile(name: string): Promise<DriveFile | null> {
    const url = new URL(`${DRIVE_API}/files`)
    url.searchParams.set('q', `name='${name}'`)
    url.searchParams.set('spaces', 'appDataFolder')
    url.searchParams.set('fields', 'files(id,name)'
    )
    const res = await fetch(url.toString(), { headers: this.headers() })
    if (!res.ok) throw new Error(`Drive findFile failed: ${res.status}`)
    const data = await res.json()
    return data.files?.[0] ?? null
  }

  /** List all files in appDataFolder. */
  async listFiles(): Promise<DriveFile[]> {
    const url = new URL(`${DRIVE_API}/files`)
    url.searchParams.set('spaces', 'appDataFolder')
    url.searchParams.set('fields', 'files(id,name)')
    url.searchParams.set('pageSize', '200')
    const res = await fetch(url.toString(), { headers: this.headers() })
    if (!res.ok) throw new Error(`Drive listFiles failed: ${res.status}`)
    const data = await res.json()
    return data.files ?? []
  }

  /** Upload (create or update) a JSON file in appDataFolder. */
  async uploadJsonFile(name: string, content: string): Promise<DriveFile> {
    const existing = await this.findFile(name)
    const metadata = {
      name,
      parents: ['appDataFolder'],
      mimeType: 'application/json',
    }

    if (existing) {
      // Update existing file content
      const boundary = '-------ourcookbook' + crypto.randomUUID()
      const body =
        `--${boundary}\r\n` +
        'Content-Type: application/json; charset=UTF-8\r\n\r\n' +
        content +
        `\r\n--${boundary}--`
      const res = await fetch(
        `${UPLOAD_API}/files/${existing.id}?uploadType=multipart`,
        {
          method: 'PATCH',
          headers: {
            ...this.headers(),
            'Content-Type': `multipart/related; boundary=${boundary}`,
          },
          body,
        },
      )
      if (!res.ok) throw new Error(`Drive update failed: ${res.status}`)
      return existing
    }

    // Create new file via multipart upload
    const boundary = '-------ourcookbook' + crypto.randomUUID()
    const body =
      `--${boundary}\r\n` +
      'Content-Type: application/json; charset=UTF-8\r\n\r\n' +
      JSON.stringify(metadata) +
      `\r\n--${boundary}\r\n` +
      'Content-Type: application/json\r\n\r\n' +
      content +
      `\r\n--${boundary}--`
    const res = await fetch(`${UPLOAD_API}/files?uploadType=multipart`, {
      method: 'POST',
      headers: {
        ...this.headers(),
        'Content-Type': `multipart/related; boundary=${boundary}`,
      },
      body,
    })
    if (!res.ok) throw new Error(`Drive create failed: ${res.status}`)
    return res.json()
  }

  /** Download a file's content. */
  async downloadFile(fileId: string): Promise<string> {
    const url = new URL(`${DRIVE_API}/files/${fileId}`)
    url.searchParams.set('alt', 'media')
    const res = await fetch(url.toString(), { headers: this.headers() })
    if (!res.ok) throw new Error(`Drive download failed: ${res.status}`)
    return res.text()
  }

  /** Delete a file. */
  async deleteFile(fileId: string): Promise<void> {
    const res = await fetch(`${DRIVE_API}/files/${fileId}`, {
      method: 'DELETE',
      headers: this.headers(),
    })
    if (!res.ok && res.status !== 204) throw new Error(`Drive delete failed: ${res.status}`)
  }

  /** Pull all recipe JSON files from Drive. Returns parsed recipes. */
  async pullRecipes(): Promise<Recipe[]> {
    const files = await this.listFiles()
    const recipeFiles = files.filter((f) => f.name.endsWith('.json') && f.name !== '_tombstones.json')
    const recipes: Recipe[] = []
    for (const file of recipeFiles) {
      try {
        const content = await this.downloadFile(file.id)
        recipes.push(JSON.parse(content) as Recipe)
      } catch (err) {
        console.error(`[drive] Failed to parse ${file.name}`, err)
      }
    }
    return recipes
  }

  /** Push a recipe to Drive as `{recipeId}.json`. */
  async pushRecipe(recipe: Recipe): Promise<void> {
    const name = `${recipe.id}.json`
    await this.uploadJsonFile(name, JSON.stringify(recipe))
  }

  /** Delete a recipe file from Drive. */
  async deleteRecipeFile(recipeId: string): Promise<void> {
    const file = await this.findFile(`${recipeId}.json`)
    if (file) await this.deleteFile(file.id)
  }

  /** Get tombstones (deleted recipe IDs) from Drive. */
  async getTombstones(): Promise<string[]> {
    const file = await this.findFile('_tombstones.json')
    if (!file) return []
    const content = await this.downloadFile(file.id)
    return JSON.parse(content) as string[]
  }

  /** Add a recipe ID to the tombstones file on Drive. */
  async addTombstone(recipeId: string): Promise<void> {
    const existing = await this.getTombstones()
    if (existing.includes(recipeId)) return
    existing.push(recipeId)
    await this.uploadJsonFile('_tombstones.json', JSON.stringify(existing))
  }
}
