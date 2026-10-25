// Drive sync service — real implementation using Google Identity Services
// (GIS) for OAuth and Drive REST API v3 for file-based sync.
//
// The service manages the access token lifecycle and delegates Drive I/O to
// the sync engine. In the browser the OAuth flow uses the GIS token client
// (implicit flow). In the Capacitor WebView popups and third-party-cookie
// iframes don't work, so the native flow navigates the WebView to Google's
// OAuth page and back with the token in the URL fragment (captured at module
// load below). `accounts.google.com` must be listed in `server.allowNavigation`
// so the flow stays inside the WebView.
//
// The user must configure their Google Cloud client ID in Settings (or via
// VITE_GOOGLE_CLIENT_ID env var) before sign-in works.

import { Capacitor } from '@capacitor/core'
import type { Recipe } from '../../types/recipe'
import type { Cookbook } from '../../types/cookbook'
import { getGoogleClientId, GOOGLE_SCOPES } from '../../lib/googleConfig'
import { runSync, type SyncResult, type SyncCallbacks } from './syncEngine'

// Tokens are short-lived (~1h). Web keeps the token in memory for the
// session; native survives the OAuth redirect by parking it in
// sessionStorage, which the redirect reload then picks up.
const TOKEN_KEY = 'driveAccessToken'
const AUTH_ERROR_KEY = 'driveAuthError'
const RETURN_TO_KEY = 'driveAuthReturnTo'

let accessToken: string | null = null

// Capture the OAuth redirect (native): Google sends the token back as a URL
// fragment (#access_token=…). Runs on import, before any page renders, so the
// app resumes with the token in place and the fragment stripped from the URL.
if (typeof window !== 'undefined' && Capacitor.isNativePlatform()) {
  const hash = window.location.hash
  if (hash.includes('access_token=') || hash.includes('error=')) {
    const params = new URLSearchParams(hash.slice(1))
    const token = params.get('access_token')
    const err = params.get('error')
    if (token) sessionStorage.setItem(TOKEN_KEY, token)
    else if (err) sessionStorage.setItem(AUTH_ERROR_KEY, err)
    const returnTo = sessionStorage.getItem(RETURN_TO_KEY)
    sessionStorage.removeItem(RETURN_TO_KEY)
    // Strip the fragment and land back where the user signed in.
    window.history.replaceState(null, '', returnTo || window.location.pathname)
  }
}

/** The access token from the OAuth redirect, if the flow just completed. */
export function takeAuthError(): string | null {
  const err = sessionStorage.getItem(AUTH_ERROR_KEY)
  if (err) sessionStorage.removeItem(AUTH_ERROR_KEY)
  return err
}

export interface DriveSyncService {
  signIn(): Promise<void>
  signOut(): Promise<void>
  isAuthenticated(): Promise<boolean>
  sync(
    localRecipes: Recipe[],
    localCookbooks: Cookbook[],
    deviceId: string,
    callbacks: SyncCallbacks,
  ): Promise<SyncResult>
}

export class RealDriveSyncService implements DriveSyncService {
  async signIn(): Promise<void> {
    const clientId = getGoogleClientId()
    if (!clientId) throw new Error('Google client ID not configured. Set it in Settings first.')

    if (Capacitor.isNativePlatform()) {
      startNativeSignIn(clientId)
      return
    }
    // Use Google Identity Services token client (implicit flow)
    accessToken = await requestToken(clientId)
  }

  async signOut(): Promise<void> {
    const token = accessToken ?? sessionStorage.getItem(TOKEN_KEY)
    accessToken = null
    sessionStorage.removeItem(TOKEN_KEY)
    // Best-effort revoke — GIS has no logout endpoint for the token model.
    revokeToken(token ?? '')
  }

  async isAuthenticated(): Promise<boolean> {
    return accessToken != null || sessionStorage.getItem(TOKEN_KEY) != null
  }

  async sync(
    localRecipes: Recipe[],
    localCookbooks: Cookbook[],
    deviceId: string,
    callbacks: SyncCallbacks,
  ): Promise<SyncResult> {
    const token = accessToken ?? sessionStorage.getItem(TOKEN_KEY)
    if (!token) throw new Error('Not authenticated. Sign in to Drive first.')
    return runSync(token, localRecipes, localCookbooks, deviceId, callbacks)
  }
}

/** Start the WebView OAuth flow: navigate to Google's implicit-grant page.
 *  The token comes back as a URL fragment when Google redirects to the app
 *  origin — captured at module load. This promise never settles; the page
 *  navigates away instead. */
function startNativeSignIn(clientId: string): Promise<void> {
  const params = new URLSearchParams({
    client_id: clientId,
    response_type: 'token',
    redirect_uri: window.location.origin,
    scope: GOOGLE_SCOPES,
    prompt: 'select_account',
  })
  sessionStorage.setItem(RETURN_TO_KEY, window.location.pathname)
  window.location.assign(`https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`)
  return new Promise(() => {})
}

/** Request an access token using GIS token client (implicit flow, browser). */
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

/** Revoke an access token — GIS on web, the REST endpoint on native. */
function revokeToken(token: string): void {
  if (!token) return
  if (!Capacitor.isNativePlatform() && 'google' in window && window.google?.accounts?.oauth2) {
    window.google.accounts.oauth2.revoke(token, () => {})
    return
  }
  // Best-effort; failures just mean the token lives out its ~1h lifetime.
  void fetch('https://oauth2.googleapis.com/revoke', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ token }).toString(),
  }).catch(() => {})
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
