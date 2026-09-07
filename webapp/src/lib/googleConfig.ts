// Google OAuth client ID — build-time configuration. The developer sets this
// once in webapp/.env (VITE_GOOGLE_CLIENT_ID) after creating a Google Cloud
// project. The end user never sees or touches it; they just click "Sign in
// with Google" and approve.
//
// Required scope: drive.file (read/write files created by this app only).

export function getGoogleClientId(): string | null {
  const id = import.meta.env.VITE_GOOGLE_CLIENT_ID
  if (typeof id === 'string' && id.trim()) return id.trim()
  return null
}

export const GOOGLE_SCOPES = 'https://www.googleapis.com/auth/drive.file'
