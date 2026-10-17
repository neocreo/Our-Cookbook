// Settings — theme, sync mode, export/import, and about.

import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../components/Button'
import { useAppStore, effectiveTheme, type ThemePref, type SyncMode } from '../stores/appStore'
import { useRecipes } from '../hooks/useRecipes'
import { useCookbooks } from '../hooks/useCookbooks'
import { getRepositoryMode, saveRecipe } from '../features/recipe/repository'
import { createCookbookNamed, addRecipeToCookbook, fileUnfiledRecipes } from '../features/cookbook/repository'
import { getDeviceId } from '../lib/device'
import { recipesToMarkdown, recipesToJSON, parseRecipesJSON, downloadFile, readFileText } from '../lib/exportImport'
import { getPendingCount } from '../features/sync/repository'

const CREATE_NEW_COOKBOOK = '__create__'

const themes: { value: ThemePref; label: string }[] = [
  { value: 'light', label: 'Light' },
  { value: 'dark', label: 'Dark' },
  { value: 'system', label: 'System' },
]

export function Settings() {
  const theme = useAppStore((s) => s.theme)
  const setTheme = useAppStore((s) => s.setTheme)
  const syncMode = useAppStore((s) => s.syncMode)
  const setSyncMode = useAppStore((s) => s.setSyncMode)
  const userName = useAppStore((s) => s.userName)
  const setUserName = useAppStore((s) => s.setUserName)
  const { recipes } = useRecipes()
  const { cookbooks } = useCookbooks()
  const fileInput = useRef<HTMLInputElement>(null)
  const [importMsg, setImportMsg] = useState<string | null>(null)
  const [importCookbookId, setImportCookbookId] = useState('')
  const [newCookbookName, setNewCookbookName] = useState('')

  function exportMarkdown() {
    downloadFile('recipes.md', recipesToMarkdown(recipes), 'text/markdown')
  }
  function exportJSON() {
    downloadFile('recipes.json', recipesToJSON(recipes), 'application/json')
  }

  async function onImportFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    try {
      const text = await readFileText(file)
      const imported = parseRecipesJSON(text)
      const deviceId = getDeviceId()

      // Resolve the import target: the chosen cookbook, a new one, or —
      // with nothing chosen — the default cookbook ("Mina recept").
      let targetId: string | null = importCookbookId || null
      let targetName = cookbooks.find((cb) => cb.id === targetId)?.name ?? ''
      if (importCookbookId === CREATE_NEW_COOKBOOK) {
        const name = newCookbookName.trim()
        if (!name) {
          setImportMsg('Type a name for the new cookbook first.')
          e.target.value = ''
          return
        }
        const cookbook = await createCookbookNamed(name, deviceId, null, userName)
        targetId = cookbook.id
        targetName = name
        setImportCookbookId(cookbook.id)
        setNewCookbookName('')
      }

      let count = 0
      for (const r of imported) {
        await saveRecipe(r, deviceId)
        count++
      }
      const recipeIds = imported.map((r) => r.id)
      if (targetId) {
        for (const id of recipeIds) {
          await addRecipeToCookbook(targetId, id, deviceId)
        }
      } else {
        await fileUnfiledRecipes(deviceId, recipeIds)
      }
      setImportMsg(`Imported ${count} recipe${count === 1 ? '' : 's'}${targetName ? ` into "${targetName}"` : ''}.`)
    } catch (err) {
      setImportMsg(`Import failed: ${err instanceof Error ? err.message : 'invalid file'}`)
    }
    e.target.value = ''
  }

  return (
    <main className="page">
      <h1 className="page-title">Settings</h1>

      <section className="recipe-section">
        <h3>You</h3>
        <p className="text-muted">Your name is shown as the owner of cookbooks you create.</p>
        <div className="field field-narrow">
          <label htmlFor="settings-name">Your name</label>
          <input
            id="settings-name"
            className="input"
            value={userName ?? ''}
            onChange={(e) => setUserName(e.target.value)}
            placeholder="Your name"
          />
        </div>
      </section>

      <section className="recipe-section">
        <h3>Theme</h3>
        <div className="row">
          {themes.map((t) => (
            <Button
              key={t.value}
              variant={effectiveTheme(theme) === effectiveTheme(t.value) ? 'primary' : 'secondary'}
              onClick={() => setTheme(t.value)}
            >
              {t.label}
            </Button>
          ))}
        </div>
      </section>

      <section className="recipe-section">
        <h3>Sync</h3>
        <p className="text-muted">
          {syncMode === 'drive'
            ? 'Google Drive sync is on. Sign in on the Sync status page — recipes and cookbooks then sync to the visible folder /Google Drive/OurOpenCookbook/, one subfolder per cookbook.'
            : 'Offline mode — recipes are stored on this device only.'}
        </p>
        <div className="row mt-2">
          <Button
            variant={syncMode === 'offline' ? 'primary' : 'secondary'}
            onClick={() => setSyncMode('offline' as SyncMode)}
          >
            Offline
          </Button>
          <Button
            variant={syncMode === 'drive' ? 'primary' : 'secondary'}
            onClick={() => setSyncMode('drive' as SyncMode)}
          >
            Google Drive
          </Button>
          <Link to="/sync" className="btn btn-secondary">
            Sync status
          </Link>
        </div>
        {syncMode === 'drive' && (
          <p className="text-muted mt-2">
            {getPendingCount() > 0
              ? `${getPendingCount()} change${getPendingCount() === 1 ? '' : 's'} queued for sync.`
              : 'No pending changes.'}
          </p>
        )}
      </section>

      <section className="recipe-section">
        <h3>Export</h3>
        <p className="text-muted">
          Exports all {recipes.length} of your recipes as a single file — recipes only. Cookbooks,
          their groupings, and app settings are not included. Import the file on another device to
          bring your recipes there.
        </p>
        <div className="row mt-2">
          <Button variant="secondary" onClick={exportMarkdown} disabled={recipes.length === 0}>
            Markdown (.md)
          </Button>
          <Button variant="secondary" onClick={exportJSON} disabled={recipes.length === 0}>
            JSON (.json)
          </Button>
        </div>
      </section>

      <section className="recipe-section">
        <h3>Import</h3>
        <p className="text-muted">
          Imports recipes from a JSON file exported from Our Open Cookbook — bare label rows such
          as "Biscuits" and "Gravy" become named ingredient lists. Importing a file with recipes
          you already have updates them in place, so re-importing is safe.
        </p>
        <div className="field field-narrow mt-2">
          <label htmlFor="import-cookbook">Import into</label>
          <select
            id="import-cookbook"
            className="input"
            value={importCookbookId}
            onChange={(e) => setImportCookbookId(e.target.value)}
          >
            <option value="">Mina recept (default)</option>
            {cookbooks.map((cb) => <option key={cb.id} value={cb.id}>{cb.name}</option>)}
            <option value={CREATE_NEW_COOKBOOK}>+ New cookbook…</option>
          </select>
          {importCookbookId === CREATE_NEW_COOKBOOK && (
            <input
              className="input"
              value={newCookbookName}
              onChange={(e) => setNewCookbookName(e.target.value)}
              placeholder="Cookbook name"
            />
          )}
        </div>
        <input
          ref={fileInput}
          type="file"
          accept="application/json,.json"
          onChange={onImportFile}
          hidden
        />
        <Button variant="secondary" onClick={() => fileInput.current?.click()}>
          Choose JSON file
        </Button>
        {importMsg && <p className="text-muted mt-2">{importMsg}</p>}
      </section>

      <section className="recipe-section">
        <h3>About</h3>
        <p className="text-muted">Our Open Cookbook · Version 0.1.0</p>
        <p className="text-muted">Storage: {getRepositoryMode() === 'rxdb' ? 'RxDB (IndexedDB)' : 'Session (memory fallback)'}</p>
        <p className="text-muted mt-2">
          <a href="https://github.com/ourcookbook">Homepage</a>
        </p>
        <p className="text-muted mt-1">
          <a href="https://github.com/ourcookbook/support">Support page</a>
        </p>
        <p className="text-muted mt-2 text-xs">
          All rights reserved. This app is provided as-is without warranty.
        </p>
      </section>
    </main>
  )
}
