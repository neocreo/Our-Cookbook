// Settings — theme, sync mode, export/import, and about.

import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../components/Button'
import { useAppStore, effectiveTheme, type ThemePref, type SyncMode } from '../stores/appStore'
import { useRecipes } from '../hooks/useRecipes'
import { getRepositoryMode, saveRecipe } from '../features/recipe/repository'
import { getDeviceId } from '../lib/device'
import { recipesToMarkdown, recipesToJSON, parseRecipesJSON, downloadFile, readFileText } from '../lib/exportImport'
import { getPendingCount } from '../features/sync/repository'

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
  const fileInput = useRef<HTMLInputElement>(null)
  const [importMsg, setImportMsg] = useState<string | null>(null)

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
      let count = 0
      for (const r of imported) {
        await saveRecipe(r, deviceId)
        count++
      }
      setImportMsg(`Imported ${count} recipe${count === 1 ? '' : 's'}.`)
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
          Imports recipes from a JSON file exported from Our Open Cookbook. The recipes are added
          alongside your existing ones — nothing is deleted or overwritten.
        </p>
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
