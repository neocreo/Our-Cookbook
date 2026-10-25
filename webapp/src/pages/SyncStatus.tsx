// SyncStatus — the sync page. The user sees: sign in with Google, sync now,
// and connection status. No developer-facing config — the client ID is baked
// into the build.
//
// Returning users who have signed in before are silently re-authenticated
// (GIS prompt: '') and pending changes sync automatically. On native the
// sign-in navigates the WebView to Google and back, so re-authentication is
// never automatic — the user taps the button instead.

import { useCallback, useEffect, useState } from 'react'
import { Capacitor } from '@capacitor/core'
import { Button } from '../components/Button'
import { useRecipes } from '../hooks/useRecipes'
import { useCookbooks } from '../hooks/useCookbooks'
import { getDeviceId } from '../lib/device'
import { getGoogleClientId } from '../lib/googleConfig'
import { getDriveSyncService, ensureGisLoaded, takeAuthError } from '../features/sync/driveService'
import { getPendingCount, getSyncMetadata } from '../features/sync/repository'
import { saveRecipeFromRemote, deleteRecipe } from '../features/recipe/repository'
import { saveCookbookFromRemote, fileUnfiledRecipes } from '../features/cookbook/repository'
import { useAppStore } from '../stores/appStore'
import { useT } from '../i18n'
import type { SyncResult } from '../features/sync/syncEngine'


// The OAuth redirect's error (if any) is read once and cached — lazy state
// initializers can run twice under React StrictMode.
let cachedAuthError: string | null | undefined

export function SyncStatus() {
  const t = useT()
  const { recipes } = useRecipes()
  const { cookbooks } = useCookbooks()
  const driveConsentGranted = useAppStore((s) => s.driveConsentGranted)
  const setDriveConsentGranted = useAppStore((s) => s.setDriveConsentGranted)
  const [authenticated, setAuthenticated] = useState(false)
  const [syncing, setSyncing] = useState(false)
  const [syncResult, setSyncResult] = useState<SyncResult | null>(null)
  const [error, setError] = useState<string | null>(() => {
    if (cachedAuthError === undefined) {
      const authError = takeAuthError()
      cachedAuthError = authError ? t('sync.authError', { error: authError }) : null
    }
    return cachedAuthError
  })
  const [pendingCount, setPendingCount] = useState(getPendingCount())
  const metadata = getSyncMetadata()
  const hasClientId = getGoogleClientId() != null

  const runSyncNow = useCallback(async () => {
    setSyncing(true)
    setError(null)
    try {
      const result = await getDriveSyncService().sync(
        recipes,
        cookbooks,
        getDeviceId(),
        {
          saveRemoteRecipe: saveRecipeFromRemote,
          saveRemoteCookbook: saveCookbookFromRemote,
          deleteRemoteRecipe: (id) => deleteRecipe(id),
          fileUnfiledRecipes: async (ids) => { await fileUnfiledRecipes(getDeviceId(), ids) },
        },
      )
      setSyncResult(result)
      setPendingCount(getPendingCount())
    } catch (err) {
      setError(err instanceof Error ? err.message : t('sync.syncFailed'))
    } finally {
      setSyncing(false)
    }
  }, [recipes, cookbooks, t])

  const onSignIn = useCallback(async () => {
    setError(null)
    try {
      // The native flow navigates away to Google — mark the consent before
      // the page unloads so the sync page picks the session back up.
      setDriveConsentGranted(true)
      if (!Capacitor.isNativePlatform()) await ensureGisLoaded()
      await getDriveSyncService().signIn()
      setAuthenticated(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : t('sync.signInFailed'))
    }
  }, [setDriveConsentGranted, t])

  // Returning users: silently re-authenticate, then drain any pending queue.
  // Never on native — signIn there is a full-page redirect, not silent.
  useEffect(() => {
    if (!hasClientId || !driveConsentGranted || Capacitor.isNativePlatform()) return
    let active = true
    ;(async () => {
      try {
        const service = getDriveSyncService()
        if (!(await service.isAuthenticated())) {
          await ensureGisLoaded()
          await service.signIn()
        }
        if (!active) return
        setAuthenticated(true)
        if (getPendingCount() > 0) await runSyncNow()
      } catch {
        // Silent sign-in is best-effort — the user signs in manually instead.
      }
    })()
    return () => { active = false }
  }, [hasClientId, driveConsentGranted, runSyncNow])

  async function onSignOut() {
    await getDriveSyncService().signOut()
    setAuthenticated(false)
    setDriveConsentGranted(false)
  }

  return (
    <main className="page">
      <h1 className="page-title">{t('sync.title')}</h1>

      <section className="recipe-section">
        <h3>{t('sync.googleDriveTitle')}</h3>
        {!hasClientId && (
          <p className="text-muted text-error">{t('sync.notConfigured')}</p>
        )}
        {hasClientId && (
          <>
            <p className="text-muted">
              {authenticated ? t('sync.connected') : t('sync.signInPrompt')}
            </p>
            <div className="form-actions">
              {authenticated ? (
                <Button variant="secondary" onClick={onSignOut}>
                  {t('sync.disconnect')}
                </Button>
              ) : (
                <Button variant="primary" onClick={onSignIn}>
                  {t('sync.signInGoogle')}
                </Button>
              )}
              {authenticated && (
                <Button variant="primary" onClick={runSyncNow} disabled={syncing}>
                  {syncing ? t('sync.syncing') : t('sync.syncNow')}
                </Button>
              )}
            </div>
          </>
        )}
      </section>

      {hasClientId && (
        <section className="recipe-section">
          <h3>{t('sync.statusTitle')}</h3>
          <p className="text-muted">
            {t('sync.pending', { count: pendingCount })}
          </p>
          {metadata?.lastSyncTimestamp && (
            <p className="text-muted">
              {t('sync.lastSync', { time: new Date(metadata.lastSyncTimestamp).toLocaleString() })}
            </p>
          )}
          {metadata?.syncInProgress && <p className="text-muted">{t('sync.inProgress')}</p>}
        </section>
      )}

      {syncResult && (
        <section className="recipe-section">
          <h3>{t('sync.lastResultTitle')}</h3>
          <p className="text-muted">
            {t('sync.pushed', { count: syncResult.pushed })} · {t('sync.pulled', { count: syncResult.pulled })}
          </p>
          {syncResult.conflicts.length > 0 && (
            <p className="text-muted">
              {t('sync.conflicts', { count: syncResult.conflicts.length })}
            </p>
          )}
          {syncResult.errors.length > 0 && (
            <div>
              <p className="text-muted text-error">{t('sync.errors')}</p>
              <ul className="ingredient-list">
                {syncResult.errors.map((e, i) => (
                  <li key={i} className="text-sm">{e}</li>
                ))}
              </ul>
            </div>
          )}
        </section>
      )}

      {error && (
        <section className="recipe-section">
          <p className="text-muted text-error">{error}</p>
        </section>
      )}
    </main>
  )
}
