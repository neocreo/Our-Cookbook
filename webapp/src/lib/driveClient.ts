// Google Drive REST API v3 client. Uses fetch directly (no gapi library).
//
// Layout — everything lives under one visible folder in the user's My Drive
// so it is easy to see what is what:
//
//   OurOpenCookbook/
//     <Cookbook name>/          one folder per cookbook
//       _cookbook.json          cookbook metadata
//       <Recipe title>.json     recipe data, readable file names
//     Unsorted/                 recipes that are not in any cookbook
//     _tombstones.json          recipe ids deleted on any device
//     _index.json               id -> Drive file/folder ids (bookkeeping)
//
// The folder tree and readable names are a human-friendly projection of the
// app's data: the source of truth for grouping is the cookbook metadata
// (`recipeIds`), not the file's location. All files are created by this app,
// so the narrow `drive.file` scope still covers them.

import type { Recipe } from '../types/recipe'
import type { Cookbook } from '../types/cookbook'

const DRIVE_API = 'https://www.googleapis.com/drive/v3'
const UPLOAD_API = 'https://www.googleapis.com/upload/drive/v3'

export const ROOT_FOLDER_NAME = 'OurOpenCookbook'
const COOKBOOK_META_FILE = '_cookbook.json'
const TOMBSTONES_FILE = '_tombstones.json'
const INDEX_FILE = '_index.json'
export const UNSORTED_FOLDER_NAME = 'Unsorted'
const FOLDER_MIME = 'application/vnd.google-apps.folder'

export interface DriveFile {
  id: string
  name: string
  mimeType?: string
}

/** id -> Drive file/folder mapping, rebuilt from the tree when missing. */
export interface DriveIndex {
  rootFolderId: string
  unsortedFolderId: string | null
  /** recipeId -> { drive file id, parent folder id } */
  recipes: Record<string, { fileId: string; parent: string }>
  /** cookbookId -> folder id */
  cookbooks: Record<string, string>
}

export interface RemoteCookbook {
  cookbook: Cookbook
  folderId: string
}

export class DriveApiClient {
  private token: string
  private rootFolderId: string | null = null

  constructor(accessToken: string) {
    this.token = accessToken
  }

  private headers(contentType = 'application/json'): HeadersInit {
    return {
      Authorization: `Bearer ${this.token}`,
      'Content-Type': contentType,
    }
  }

  private async api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(`${DRIVE_API}${path}`, {
      ...init,
      headers: { ...this.headers(), ...(init?.headers as HeadersInit | undefined) },
    })
    if (!res.ok) throw new Error(`Drive ${path} failed: ${res.status}`)
    return res.json() as Promise<T>
  }

  // ── folders ──────────────────────────────────────────────────────────

  /** The visible root folder, created on first sync. */
  async ensureRootFolder(): Promise<string> {
    if (this.rootFolderId) return this.rootFolderId
    const rootId = (await this.api<{ id: string }>('/files/root?fields=id')).id
    const existing = await this.findChildByName(rootId, ROOT_FOLDER_NAME)
    this.rootFolderId = existing
      ? existing.id
      : await this.createFolder(ROOT_FOLDER_NAME, rootId)
    return this.rootFolderId
  }

  async listChildren(parentId: string): Promise<DriveFile[]> {
    const q = encodeURIComponent(`'${parentId}' in parents and trashed=false`)
    const data = await this.api<{ files?: DriveFile[] }>(
      `/files?q=${q}&fields=files(id,name,mimeType)&pageSize=200`,
    )
    return data.files ?? []
  }

  private async findChildByName(parentId: string, name: string): Promise<DriveFile | null> {
    const children = await this.listChildren(parentId)
    return children.find((c) => c.name === name) ?? null
  }

  private async createFolder(name: string, parentId: string): Promise<string> {
    const created = await this.api<DriveFile>('/files', {
      method: 'POST',
      body: JSON.stringify({ name, mimeType: FOLDER_MIME, parents: [parentId] }),
    })
    return created.id
  }

  private async renameEntity(fileId: string, name: string): Promise<void> {
    await this.api(`/files/${fileId}`, { method: 'PATCH', body: JSON.stringify({ name }) })
  }

  private async moveEntity(fileId: string, addParent: string, removeParent: string): Promise<void> {
    const params = new URLSearchParams({ addParents: addParent, removeParents: removeParent })
    await this.api(`/files/${fileId}?${params.toString()}`, { method: 'PATCH' })
  }

  private async deleteEntity(fileId: string): Promise<void> {
    const res = await fetch(`${DRIVE_API}/files/${fileId}`, {
      method: 'DELETE',
      headers: this.headers(),
    })
    if (!res.ok && res.status !== 204) throw new Error(`Drive delete failed: ${res.status}`)
  }

  // ── file content ──────────────────────────────────────────────────────

  private async readFile(fileId: string): Promise<string> {
    const res = await fetch(`${DRIVE_API}/files/${fileId}?alt=media`, {
      headers: this.headers(),
    })
    if (!res.ok) throw new Error(`Drive download failed: ${res.status}`)
    return res.text()
  }

  private async readJson<T>(fileId: string, fallback: T): Promise<T> {
    try {
      return JSON.parse(await this.readFile(fileId)) as T
    } catch {
      return fallback
    }
  }

  /** Create a JSON file (multipart) or replace an existing one's content. */
  private async writeJsonFile(
    content: string,
    name: string,
    parent: string,
    fileId: string | null,
  ): Promise<string> {
    if (fileId) {
      const res = await fetch(`${UPLOAD_API}/files/${fileId}?uploadType=media`, {
        method: 'PATCH',
        headers: this.headers(),
        body: content,
      })
      if (!res.ok) throw new Error(`Drive update failed: ${res.status}`)
      return fileId
    }
    const boundary = '-------ourcookbook' + crypto.randomUUID()
    const body =
      `--${boundary}\r\n` +
      'Content-Type: application/json; charset=UTF-8\r\n\r\n' +
      JSON.stringify({ name, parents: [parent], mimeType: 'application/json' }) +
      `\r\n--${boundary}\r\n` +
      'Content-Type: application/json\r\n\r\n' +
      content +
      `\r\n--${boundary}--`
    const res = await fetch(`${UPLOAD_API}/files?uploadType=multipart`, {
      method: 'POST',
      headers: { ...this.headers(`multipart/related; boundary=${boundary}`) },
      body,
    })
    if (!res.ok) throw new Error(`Drive create failed: ${res.status}`)
    return (await res.json()).id
  }

  // ── index ─────────────────────────────────────────────────────────────

  /** Read the index; rebuild it by walking the tree when missing or broken. */
  async getIndex(): Promise<DriveIndex> {
    const rootFolderId = await this.ensureRootFolder()
    const indexFile = await this.findChildByName(rootFolderId, INDEX_FILE)
    if (indexFile) {
      const stored = await this.readJson<DriveIndex | null>(indexFile.id, null)
      if (stored && stored.rootFolderId === rootFolderId) return stored
    }
    return this.rebuildIndex(rootFolderId)
  }

  private async rebuildIndex(rootFolderId: string): Promise<DriveIndex> {
    const index: DriveIndex = {
      rootFolderId,
      unsortedFolderId: null,
      recipes: {},
      cookbooks: {},
    }
    const children = await this.listChildren(rootFolderId)
    for (const child of children) {
      if (child.mimeType !== FOLDER_MIME) continue
      if (child.name === UNSORTED_FOLDER_NAME) {
        index.unsortedFolderId = child.id
        continue
      }
      const meta = await this.findChildByName(child.id, COOKBOOK_META_FILE)
      if (!meta) continue
      const cookbook = await this.readJson<Cookbook | null>(meta.id, null)
      if (cookbook?.id) index.cookbooks[cookbook.id] = child.id
    }
    // Map recipe files by walking every folder — including Unsorted.
    const folders = children.filter((c) => c.mimeType === FOLDER_MIME)
    for (const folder of folders) {
      const files = await this.listChildren(folder.id)
      for (const file of files) {
        if (!file.name.endsWith('.json') || file.name.startsWith('_')) continue
        const recipe = await this.readJson<Recipe | null>(file.id, null)
        if (recipe?.id) index.recipes[recipe.id] = { fileId: file.id, parent: folder.id }
      }
    }
    return index
  }

  async writeIndex(index: DriveIndex): Promise<void> {
    const existing = await this.findChildByName(index.rootFolderId, INDEX_FILE)
    await this.writeJsonFile(
      JSON.stringify(index),
      INDEX_FILE,
      index.rootFolderId,
      existing?.id ?? null,
    )
  }

  // ── cookbooks ─────────────────────────────────────────────────────────

  /** All cookbooks on Drive, read from each folder's `_cookbook.json`. */
  async pullCookbooks(index: DriveIndex): Promise<RemoteCookbook[]> {
    const children = await this.listChildren(index.rootFolderId)
    const result: RemoteCookbook[] = []
    for (const child of children) {
      if (child.mimeType !== FOLDER_MIME || child.name === UNSORTED_FOLDER_NAME) continue
      const meta = await this.findChildByName(child.id, COOKBOOK_META_FILE)
      if (!meta) continue
      const cookbook = await this.readJson<Cookbook | null>(meta.id, null)
      if (cookbook?.id) result.push({ cookbook, folderId: child.id })
    }
    return result
  }

  /**
   * Project the merged cookbook list onto the folder tree: create folders for
   * new cookbooks, rename renamed ones, keep metadata files fresh, and
   * delete folders for deleted cookbooks — their recipe files move to
   * Unsorted (recipes are never deleted with a cookbook).
   */
  async syncCookbookFolders(
    merged: Cookbook[],
    deletedIds: string[],
    index: DriveIndex,
  ): Promise<void> {
    // Make sure Unsorted exists before any recipe needs it.
    if (!index.unsortedFolderId) {
      index.unsortedFolderId = await this.createFolder(UNSORTED_FOLDER_NAME, index.rootFolderId)
    }
    const children = await this.listChildren(index.rootFolderId)
    const byId = new Map(children.map((c) => [c.id, c]))

    for (const id of deletedIds) {
      const folderId = index.cookbooks[id]
      if (!folderId) continue
      await this.moveRecipeFilesToUnsorted(folderId, index)
      await this.deleteEntity(folderId)
      delete index.cookbooks[id]
    }

    for (const cb of merged) {
      let folderId = index.cookbooks[cb.id]
      if (!folderId || !byId.has(folderId)) {
        folderId = await this.createFolder(cb.name, index.rootFolderId)
        index.cookbooks[cb.id] = folderId
      } else if (byId.get(folderId)!.name !== cb.name) {
        await this.renameEntity(folderId, cb.name)
      }
      // Keep the metadata file fresh — one small write per cookbook per sync.
      const meta = await this.findChildByName(folderId, COOKBOOK_META_FILE)
      await this.writeJsonFile(JSON.stringify(cb), COOKBOOK_META_FILE, folderId, meta?.id ?? null)
    }
  }

  private async moveRecipeFilesToUnsorted(folderId: string, index: DriveIndex): Promise<void> {
    if (!index.unsortedFolderId) return
    const files = await this.listChildren(folderId)
    for (const file of files) {
      if (file.name.startsWith('_')) continue
      await this.moveEntity(file.id, index.unsortedFolderId, folderId)
      for (const [recipeId, entry] of Object.entries(index.recipes)) {
        if (entry.fileId === file.id) index.recipes[recipeId].parent = index.unsortedFolderId
      }
    }
  }

  // ── recipes ───────────────────────────────────────────────────────────

  /** Pull every recipe file from all cookbook folders (and Unsorted). */
  async pullRecipes(index: DriveIndex): Promise<Recipe[]> {
    const children = await this.listChildren(index.rootFolderId)
    const folders = children.filter((c) => c.mimeType === FOLDER_MIME)
    const recipes: Recipe[] = []
    for (const folder of folders) {
      const files = await this.listChildren(folder.id)
      for (const file of files) {
        if (!file.name.endsWith('.json') || file.name.startsWith('_')) continue
        const recipe = await this.readJson<Recipe | null>(file.id, null)
        if (recipe) {
          recipes.push(recipe)
          index.recipes[recipe.id] = { fileId: file.id, parent: folder.id }
        }
      }
    }
    return recipes
  }

  /** Create or update a recipe file inside its cookbook folder. */
  async pushRecipe(recipe: Recipe, parentFolderId: string, fileName: string, index: DriveIndex): Promise<void> {
    const entry = index.recipes[recipe.id]
    if (entry) {
      const fileId = await this.writeJsonFile(JSON.stringify(recipe), fileName, entry.parent, entry.fileId)
      if (entry.parent !== parentFolderId) {
        await this.moveEntity(fileId, parentFolderId, entry.parent)
        entry.parent = parentFolderId
      }
      return
    }
    const fileId = await this.writeJsonFile(JSON.stringify(recipe), fileName, parentFolderId, null)
    index.recipes[recipe.id] = { fileId, parent: parentFolderId }
  }

  /** Delete a recipe file and drop it from the index. */
  async deleteRecipe(recipeId: string, index: DriveIndex): Promise<void> {
    const entry = index.recipes[recipeId]
    if (entry) {
      await this.deleteEntity(entry.fileId)
      delete index.recipes[recipeId]
    }
  }

  // ── tombstones ────────────────────────────────────────────────────────

  async getTombstones(index: DriveIndex): Promise<string[]> {
    const file = await this.findChildByName(index.rootFolderId, TOMBSTONES_FILE)
    if (!file) return []
    return await this.readJson<string[]>(file.id, [])
  }

  async addTombstone(recipeId: string, index: DriveIndex): Promise<void> {
    const existing = await this.getTombstones(index)
    if (existing.includes(recipeId)) return
    const file = await this.findChildByName(index.rootFolderId, TOMBSTONES_FILE)
    await this.writeJsonFile(
      JSON.stringify([...existing, recipeId]),
      TOMBSTONES_FILE,
      index.rootFolderId,
      file?.id ?? null,
    )
  }
}
