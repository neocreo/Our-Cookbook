// Settings — theme, sync mode, and export/import. Drive connect is a stub
// (full sync lands in Phase 2).

import { useRef, useState } from 'react'
import { Button } from '../components/Button'
import { useAppStore, effectiveTheme, type ThemePref, type SyncMode } from '../stores/appStore'
import { useRecipes } from '../hooks/useRecipes'
import { getRepositoryMode, saveRecipe } from '../features/recipe/repository'
import { getDeviceId } from '../lib/device'
import { recipesToMarkdown, recipesToJSON, parseRecipesJSON, downloadFile, readFileText } from '../lib/exportImport'

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
        <h3>Theme</h3>
        <div style={{ display: 'flex', gap: 8 }}>
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
            ? 'Google Drive is selected. Sign-in and sync are wired in Phase 2.'
            : 'Offline mode — recipes are stored on this device only.'}
        </p>
        <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
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
        </div>
      </section>

      <section className="recipe-section">
        <h3>Export</h3>
        <p className="text-muted">Export all {recipes.length} recipes.</p>
        <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
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
        <p className="text-muted">Import recipes from a JSON file exported by this app.</p>
        <input
          ref={fileInput}
          type="file"
          accept="application/json,.json"
          onChange={onImportFile}
          style={{ display: 'none' }}
        />
        <Button variant="secondary" onClick={() => fileInput.current?.click()}>
          Choose JSON file
        </Button>
        {importMsg && <p className="text-muted" style={{ marginTop: 8 }}>{importMsg}</p>}
      </section>

      <section className="recipe-section">
        <h3>About</h3>
        <p className="text-muted">Our Cookbook · Phase 1</p>
        <p className="text-muted">Storage: {getRepositoryMode() === 'rxdb' ? 'RxDB (IndexedDB)' : 'Session (memory fallback)'}</p>
      </section>
    </main>
  )
}
