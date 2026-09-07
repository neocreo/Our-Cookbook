// Drive sync service — real implementation using Google Identity Services
// (GIS) for OAuth and Drive REST API v3 for file-based sync.
//
// The service manages the access token lifecycle and delegates Drive I/O to
// the sync engine. The OAuth flow uses the implicit token model (GIS token
// client), which works in browser and Capacitor WebView.
//
// The user must configure their Google Cloud client ID in Settings (or via
// VITE_GOOGLE_CLIENT_ID env var) before sign-in works.

import type { Recipe } from '../../types/recipe'
import { getGoogleClientId, GOOGLE_SCOPES } from '../../lib/googleConfig'
import { runSync, type SyncResult } from './syncEngine'

// Token stored in memory (session-scoped). GIS tokens are short-lived (~1h);
// the user re-authenticates when they expire.
let accessToken: string | null = null

export interface DriveSyncService {
  signIn(): Promise<void>
  signOut(): Promise<void>
  isAuthenticated(): Promise<boolean>
  sync(localRecipes: Recipe[], deviceId: string, saveRemoteRecipe: (r: Recipe) => Promise<void>): Promise<SyncResult>
}

export class RealDriveSyncService implements DriveSyncService {
  async signIn(): Promise<void> {
    const clientId = getGoogleClientId()
    if (!clientId) throw new Error('Google client ID not configured. Set it in Settings first.')

    // Use Google Identity Services token client (implicit flow)
    accessToken = await requestToken(clientId)
  }

  async signOut(): Promise<void> {
    accessToken = null
    // GIS doesn't have a logout endpoint for token model; just clear the token
    revokeToken(accessToken ?? '')
    accessToken = null
  }

  async isAuthenticated(): Promise<boolean> {
    return accessToken != null
  }

  async sync(
    localRecipes: Recipe[],
    deviceId: string,
    saveRemoteRecipe: (r: Recipe) => Promise<void>,
  ): Promise<SyncResult> {
    if (!accessToken) throw new Error('Not authenticated. Sign in to Drive first.')
    return runSync(accessToken, localRecipes, deviceId, saveRemoteRecipe)
  }
}

/** Request an access token using GIS token client (implicit flow). */
function requestToken(clientId: string): Promise<string> {
  return new Promise((resolve, reject) => {
    if (!('google' in window) || !window.google?.accounts?.oauth2) {
      reject(new Error('Google Identity Services not loaded. Check your network connection.'))
      return
    }

    const tokenClient = window.google.accounts.oauth2.initTokenClient({
      client_id: clientId,
      scope: GOOGLE_SCOPES,
      callback: (response) => {
        if (response.access_token) {
          resolve(response.access_token)
        } else {
          reject(new Error('Failed to get access token'))
        }
      },
      error_callback: (err) => {
        reject(new Error(err?.message ?? 'OAuth error'))
      },
    })

    tokenClient.requestAccessToken({ prompt: '' })
  })
}

/** Revoke a GIS access token. */
function revokeToken(token: string): void {
  if (!token) return
  if ('google' in window && window.google?.accounts?.oauth2) {
    window.google.accounts.oauth2.revoke(token, () => {})
  }
}

// TypeScript declarations for the Google Identity Services API
declare global {
  interface Window {
    google?: {
      accounts: {
        oauth2: {
          initTokenClient(config: {
            client_id: string
            scope: string
            callback: (response: { access_token?: string }) => void
            error_callback?: (err: { message?: string }) => void
          }): { requestAccessToken(opts?: { prompt?: string }): void }
          revoke(token: string, callback: () => void): void
        }
      }
    }
  }
}

let service: DriveSyncService | null = null

export function getDriveSyncService(): DriveSyncService {
  if (!service) service = new RealDriveSyncService()
  return service
}

// Load the GIS script dynamically (loaded once on first use)
let gisLoaded = false
export function ensureGisLoaded(): Promise<void> {
  if (gisLoaded) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.onload = () => { gisLoaded = true; resolve() }
    script.onerror = () => reject(new Error('Failed to load Google Identity Services script'))
    document.head.appendChild(script)
  })
}
