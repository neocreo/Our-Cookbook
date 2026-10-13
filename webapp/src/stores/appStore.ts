// Global app state: onboarding status, sync mode, theme. Persisted to
// localStorage so the user's choices survive reloads.

import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type SyncMode = 'offline' | 'drive'
export type ThemePref = 'light' | 'dark' | 'system'

interface AppState {
  onboardingDone: boolean
  syncMode: SyncMode | null
  theme: ThemePref
  userName: string | null
  /** True once the user has completed a Drive sign-in — gates silent re-auth. */
  driveConsentGranted: boolean
  /** OCR recognition language on the scan page. */
  scanLanguage: 'eng' | 'swe'
  completeOnboarding: (mode: SyncMode) => void
  setSyncMode: (mode: SyncMode) => void
  setTheme: (theme: ThemePref) => void
  setUserName: (name: string) => void
  setDriveConsentGranted: (granted: boolean) => void
  setScanLanguage: (language: 'eng' | 'swe') => void
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      onboardingDone: false,
      syncMode: null,
      theme: 'system',
      userName: null,
      driveConsentGranted: false,
      scanLanguage: 'swe',
      completeOnboarding: (mode) => set({ onboardingDone: true, syncMode: mode }),
      setSyncMode: (mode) => set({ syncMode: mode }),
      setTheme: (theme) => set({ theme }),
      setUserName: (name) => set({ userName: name }),
      setDriveConsentGranted: (granted) => set({ driveConsentGranted: granted }),
      setScanLanguage: (language) => set({ scanLanguage: language }),
    }),
    { name: 'oc.app' },
  ),
)

export function effectiveTheme(pref: ThemePref): 'light' | 'dark' {
  if (pref !== 'system') return pref
  const prefersDark =
    typeof window !== 'undefined' &&
    window.matchMedia('(prefers-color-scheme: dark)').matches
  return prefersDark ? 'dark' : 'light'
}
