// SyncStatus — the sync page. The user sees: sign in with Google, sync now,
// and connection status. No developer-facing config — the client ID is baked
// into the build.
//
// Returning users who have signed in before are silently re-authenticated
// (GIS prompt: '') and pending changes sync automatically.

import { useCallback, useEffect, useState } from 'react'
import { Button } from '../components/Button'
import { useRecipes } from '../hooks/useRecipes'
import { useCookbooks } from '../hooks/useCookbooks'
import { getDeviceId } from '../lib/device'
import { getGoogleClientId } from '../lib/googleConfig'
import { getDriveSyncService, ensureGisLoaded } from '../features/sync/driveService'
import { getPendingCount, getSyncMetadata } from '../features/sync/repository'
import { saveRecipeFromRemote, deleteRecipe } from '../features/recipe/repository'
import { saveCookbookFromRemote, fileUnfiledRecipes } from '../features/cookbook/repository'
import { useAppStore } from '../stores/appStore'
import type { SyncResult } from '../features/sync/syncEngine'

export function SyncStatus() {
  const { recipes } = useRecipes()
  const { cookbooks } = useCookbooks()
  const driveConsentGranted = useAppStore((s) => s.driveConsentGranted)
  const setDriveConsentGranted = useAppStore((s) => s.setDriveConsentGranted)
  const [authenticated, setAuthenticated] = useState(false)
  const [syncing, setSyncing] = useState(false)
  const [syncResult, setSyncResult] = useState<SyncResult | null>(null)
  const [error, setError] = useState<string | null>(null)
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
      setError(err instanceof Error ? err.message : 'Synk misslyckades')
    } finally {
      setSyncing(false)
    }
  }, [recipes, cookbooks])

  const onSignIn = useCallback(async () => {
    setError(null)
    try {
      await ensureGisLoaded()
      await getDriveSyncService().signIn()
      setAuthenticated(true)
      setDriveConsentGranted(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Inloggning misslyckades')
    }
  }, [setDriveConsentGranted])

  // Returning users: silently re-authenticate, then drain any pending queue.
  useEffect(() => {
    if (!hasClientId || !driveConsentGranted) return
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
      <h1 className="page-title">Sync</h1>

      <section className="recipe-section">
        <h3>Google Drive</h3>
        {!hasClientId && (
          <p className="text-muted" style={{ color: 'var(--color-accent-700)' }}>
            Drive-sync har inte konfigurerats ännu. Det kräver en engångsinställning
            av appens utvecklare.
          </p>
        )}
        {hasClientId && (
          <>
            <p className="text-muted">
              {authenticated
                ? 'Ansluten till Google Drive. Recept och kokböcker synkas till mappen /Google Drive/OurOpenCookbook/ — en katalog per kokbok.'
                : 'Logga in med ditt Google-konto för att synka recepten till din Drive.'}
            </p>
            <div className="form-actions">
              {authenticated ? (
                <Button variant="secondary" onClick={onSignOut}>
                  Koppla bort
                </Button>
              ) : (
                <Button variant="primary" onClick={onSignIn}>
                  Logga in med Google
                </Button>
              )}
              {authenticated && (
                <Button variant="primary" onClick={runSyncNow} disabled={syncing}>
                  {syncing ? 'Synkar…' : 'Synka nu'}
                </Button>
              )}
            </div>
          </>
        )}
      </section>

      {hasClientId && (
        <section className="recipe-section">
          <h3>Status</h3>
          <p className="text-muted">
            Väntande ändringar: {pendingCount}
          </p>
          {metadata?.lastSyncTimestamp && (
            <p className="text-muted">
              Senaste synk: {new Date(metadata.lastSyncTimestamp).toLocaleString()}
            </p>
          )}
          {metadata?.syncInProgress && <p className="text-muted">Synk pågår…</p>}
        </section>
      )}

      {syncResult && (
        <section className="recipe-section">
          <h3>Senaste synk</h3>
          <p className="text-muted">
            Skickade: {syncResult.pushed} · Hämtade: {syncResult.pulled}
          </p>
          {syncResult.conflicts.length > 0 && (
            <p className="text-muted">
              Konflikter: {syncResult.conflicts.length} (löstes automatiskt)
            </p>
          )}
          {syncResult.errors.length > 0 && (
            <div>
              <p className="text-muted" style={{ color: 'var(--color-accent-700)' }}>Fel:</p>
              <ul className="ingredient-list">
                {syncResult.errors.map((e, i) => (
                  <li key={i} style={{ fontSize: 13 }}>{e}</li>
                ))}
              </ul>
            </div>
          )}
        </section>
      )}

      {error && (
        <section className="recipe-section">
          <p className="text-muted" style={{ color: 'var(--color-accent-700)' }}>{error}</p>
        </section>
      )}
    </main>
  )
}
