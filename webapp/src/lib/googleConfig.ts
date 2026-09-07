// Google OAuth client ID storage. The user provides their own Google Cloud
// project client ID (Settings page), stored in localStorage. For development,
// it can also be set via Vite env var VITE_GOOGLE_CLIENT_ID.
//
// Required scopes for Drive sync: drive.file (read/write files created by
// this app only).

const CLIENT_ID_KEY = 'oc.google.clientId'

export function getGoogleClientId(): string | null {
  const fromEnv = import.meta.env.VITE_GOOGLE_CLIENT_ID
  if (fromEnv && typeof fromEnv === 'string' && fromEnv.trim()) return fromEnv.trim()
  return localStorage.getItem(CLIENT_ID_KEY)
}

export function setGoogleClientId(id: string): void {
  const trimmed = id.trim()
  if (trimmed) localStorage.setItem(CLIENT_ID_KEY, trimmed)
  else localStorage.removeItem(CLIENT_ID_KEY)
}

export const GOOGLE_SCOPES = 'https://www.googleapis.com/auth/drive.file'
