// Settings — language, theme, sync mode, export/import, and about.

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
import { useT, LANGUAGES } from '../i18n'

const CREATE_NEW_COOKBOOK = '__create__'

export function Settings() {
  const t = useT()
  const language = useAppStore((s) => s.language)
  const setLanguage = useAppStore((s) => s.setLanguage)
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

  const themes: { value: ThemePref; labelKey: string }[] = [
    { value: 'light', labelKey: 'settings.themeLight' },
    { value: 'dark', labelKey: 'settings.themeDark' },
    { value: 'system', labelKey: 'settings.themeSystem' },
  ]

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
      // with nothing chosen — the default cookbook.
      let targetId: string | null = importCookbookId || null
      let targetName = cookbooks.find((cb) => cb.id === targetId)?.name ?? ''
      if (importCookbookId === CREATE_NEW_COOKBOOK) {
        const name = newCookbookName.trim()
        if (!name) {
          setImportMsg(t('settings.importNeedsName'))
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
      setImportMsg(
        (targetName
          ? t('settings.importedInto', { count: t('common.recipeCount', { count }), name: targetName })
          : t('settings.imported', { count: t('common.recipeCount', { count }) })),
      )
    } catch (err) {
      setImportMsg(t('settings.importFailed', { error: err instanceof Error ? err.message : t('common.unknown') }))
    }
    e.target.value = ''
  }

  return (
    <main className="page">
      <h1 className="page-title">{t('settings.title')}</h1>

      <section className="recipe-section">
        <h3>{t('settings.language')}</h3>
        <p className="text-muted">{t('settings.languageDesc')}</p>
        <div className="row">
          {LANGUAGES.map((l) => (
            <Button
              key={l.value}
              variant={language === l.value ? 'primary' : 'secondary'}
              onClick={() => setLanguage(l.value)}
            >
              {l.label}
            </Button>
          ))}
        </div>
      </section>

      <section className="recipe-section">
        <h3>{t('settings.you')}</h3>
        <p className="text-muted">{t('settings.youDesc')}</p>
        <div className="field field-narrow">
          <label htmlFor="settings-name">{t('settings.yourName')}</label>
          <input
            id="settings-name"
            className="input"
            value={userName ?? ''}
            onChange={(e) => setUserName(e.target.value)}
            placeholder={t('settings.yourName')}
          />
        </div>
      </section>

      <section className="recipe-section">
        <h3>{t('settings.theme')}</h3>
        <div className="row">
          {themes.map((th) => (
            <Button
              key={th.value}
              variant={effectiveTheme(theme) === effectiveTheme(th.value) ? 'primary' : 'secondary'}
              onClick={() => setTheme(th.value)}
            >
              {t(th.labelKey)}
            </Button>
          ))}
        </div>
      </section>

      <section className="recipe-section">
        <h3>{t('settings.syncTitle')}</h3>
        <p className="text-muted">
          {syncMode === 'drive' ? t('settings.syncOnDesc') : t('settings.syncOffDesc')}
        </p>
        <div className="row mt-2">
          <Button
            variant={syncMode === 'offline' ? 'primary' : 'secondary'}
            onClick={() => setSyncMode('offline' as SyncMode)}
          >
            {t('settings.offline')}
          </Button>
          <Button
            variant={syncMode === 'drive' ? 'primary' : 'secondary'}
            onClick={() => setSyncMode('drive' as SyncMode)}
          >
            {t('sync.googleDriveTitle')}
          </Button>
          <Link to="/sync" className="btn btn-secondary">
            {t('settings.syncStatusLink')}
          </Link>
        </div>
        {syncMode === 'drive' && (
          <p className="text-muted mt-2">
            {getPendingCount() > 0
              ? t('settings.pendingQueued', { count: t('common.changeCount', { count: getPendingCount() }) })
              : t('settings.noPending')}
          </p>
        )}
      </section>

      <section className="recipe-section">
        <h3>{t('settings.export')}</h3>
        <p className="text-muted">{t('settings.exportDesc', { count: recipes.length })}</p>
        <div className="row mt-2">
          <Button variant="secondary" onClick={exportMarkdown} disabled={recipes.length === 0}>
            {t('settings.exportMarkdown')}
          </Button>
          <Button variant="secondary" onClick={exportJSON} disabled={recipes.length === 0}>
            {t('settings.exportJson')}
          </Button>
        </div>
      </section>

      <section className="recipe-section">
        <h3>{t('settings.import')}</h3>
        <p className="text-muted">{t('settings.importDesc')}</p>
        <div className="field field-narrow mt-2">
          <label htmlFor="import-cookbook">{t('settings.importInto')}</label>
          <select
            id="import-cookbook"
            className="input"
            value={importCookbookId}
            onChange={(e) => setImportCookbookId(e.target.value)}
          >
            <option value="">{t('common.defaultCookbookOption')}</option>
            {cookbooks.map((cb) => <option key={cb.id} value={cb.id}>{cb.name}</option>)}
            <option value={CREATE_NEW_COOKBOOK}>{t('settings.newCookbookOption')}</option>
          </select>
          {importCookbookId === CREATE_NEW_COOKBOOK && (
            <input
              className="input"
              value={newCookbookName}
              onChange={(e) => setNewCookbookName(e.target.value)}
              placeholder={t('edit.cookbookNamePlaceholder')}
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
          {t('settings.chooseJson')}
        </Button>
        {importMsg && <p className="text-muted mt-2">{importMsg}</p>}
      </section>

      <section className="recipe-section">
        <h3>{t('settings.about')}</h3>
        <p className="text-muted">Our Open Cookbook · Version 0.1.0</p>
        <p className="text-muted">{getRepositoryMode() === 'rxdb' ? t('settings.storageRxdb') : t('settings.storageFallback')}</p>
        <p className="text-muted mt-2">
          <a href="https://opencookbook.online">{t('settings.homepage')}</a>
        </p>
        <p className="text-muted mt-1">
          <a href="mailto:ouropencookbook@neocreo.net">{t('settings.support')}</a>
        </p>
        <p className="text-muted mt-2 text-xs">
          {t('settings.disclaimer')}
        </p>
      </section>
    </main>
  )
}
