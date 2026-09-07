// SyncStatus — shows the current sync state: pending changes, last sync,
// conflicts, and a manual sync button. Also lets the user configure their
// Google Cloud client ID (required for Drive sign-in).

import { useState, useEffect } from 'react'
import { Button } from '../components/Button'
import { useRecipes } from '../hooks/useRecipes'
import { getDeviceId } from '../lib/device'
import { getGoogleClientId, setGoogleClientId } from '../lib/googleConfig'
import { getDriveSyncService, ensureGisLoaded } from '../features/sync/driveService'
import { getPendingCount, getPendingSyncQueue, getSyncMetadata } from '../features/sync/repository'
import { saveRecipe } from '../features/recipe/repository'
import type { SyncResult } from '../features/sync/syncEngine'

export function SyncStatus() {
  const { recipes } = useRecipes()
  const [clientId, setClientIdState] = useState(() => getGoogleClientId() ?? '')
  const [authenticated, setAuthenticated] = useState(false)
  const [syncing, setSyncing] = useState(false)
  const [syncResult, setSyncResult] = useState<SyncResult | null>(null)
  const [error, setError] = useState<string | null>(null)
  const pendingCount = getPendingCount()
  const metadata = getSyncMetadata()

  useEffect(() => {
    getDriveSyncService().isAuthenticated().then(setAuthenticated)
  }, [])

  async function onSaveClientId() {
    setGoogleClientId(clientId)
    setError(null)
  }

  async function onSignIn() {
    setError(null)
    try {
      await ensureGisLoaded()
      await getDriveSyncService().signIn()
      setAuthenticated(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sign-in failed')
    }
  }

  async function onSignOut() {
    await getDriveSyncService().signOut()
    setAuthenticated(false)
  }

  async function onSync() {
    setSyncing(true)
    setError(null)
    try {
      const result = await getDriveSyncService().sync(recipes, getDeviceId(), async (r) => { await saveRecipe(r, getDeviceId()) })
      setSyncResult(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sync failed')
    } finally {
      setSyncing(false)
    }
  }

  return (
    <main className="page">
      <h1 className="page-title">Sync</h1>

      <section className="recipe-section">
        <h3>Google Drive</h3>
        <p className="text-muted">
          {authenticated
            ? 'Connected to Google Drive.'
            : 'Not connected. Configure your client ID and sign in.'}
        </p>

        <div className="form-row">
          <label htmlFor="clientId">Google Client ID</label>
          <input
            id="clientId"
            className="input"
            value={clientId}
            onChange={(e) => setClientIdState(e.target.value)}
            placeholder="xxxx.apps.googleusercontent.com"
          />
        </div>
        <Button variant="secondary" onClick={onSaveClientId} disabled={!clientId.trim()}>
          Save client ID
        </Button>

        <div className="form-actions">
          {authenticated ? (
            <Button variant="secondary" onClick={onSignOut}>
              Sign out
            </Button>
          ) : (
            <Button variant="primary" onClick={onSignIn} disabled={!clientId.trim()}>
              Sign in to Drive
            </Button>
          )}
          <Button variant="primary" onClick={onSync} disabled={!authenticated || syncing}>
            {syncing ? 'Syncing…' : 'Sync now'}
          </Button>
        </div>
      </section>

      <section className="recipe-section">
        <h3>Status</h3>
        <p className="text-muted">
          Pending changes: {pendingCount}
        </p>
        {metadata?.lastSyncTimestamp && (
          <p className="text-muted">Last sync: {new Date(metadata.lastSyncTimestamp).toLocaleString()}</p>
        )}
        {metadata?.syncInProgress && <p className="text-muted">Sync in progress…</p>}
      </section>

      {syncResult && (
        <section className="recipe-section">
          <h3>Last sync result</h3>
          <p className="text-muted">Pushed: {syncResult.pushed} · Pulled: {syncResult.pulled}</p>
          {syncResult.conflicts.length > 0 && (
            <p className="text-muted">Conflicts: {syncResult.conflicts.length} (auto-resolved)</p>
          )}
          {syncResult.errors.length > 0 && (
            <div>
              <p className="text-muted" style={{ color: 'var(--color-accent-700)' }}>Errors:</p>
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

      {pendingCount > 0 && (
        <section className="recipe-section">
          <h3>Pending queue</h3>
          <ul className="ingredient-list">
            {getPendingSyncQueue().slice(0, 10).map((p) => (
              <li key={p.id} style={{ fontSize: 13 }}>
                {p.operation} {p.entityType}: {p.entityId.slice(0, 8)}…
                {p.lastError && <span style={{ color: 'var(--color-accent-700)' }}> ({p.lastError})</span>}
              </li>
            ))}
          </ul>
        </section>
      )}
    </main>
  )
}
