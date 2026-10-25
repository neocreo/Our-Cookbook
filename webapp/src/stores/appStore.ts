// Global app state: onboarding status, sync mode, theme. Persisted to
// localStorage so the user's choices survive reloads.

import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Language } from '../i18n'

export type SyncMode = 'offline' | 'drive'
export type ThemePref = 'light' | 'dark' | 'system'
export type OcrLanguagePref = 'auto' | 'eng' | 'swe' | 'fin' | 'deu' | 'fra'

interface AppState {
  onboardingDone: boolean
  syncMode: SyncMode | null
  theme: ThemePref
  userName: string | null
  /** True once the user has completed a Drive sign-in — gates silent re-auth. */
  driveConsentGranted: boolean
  /** UI language; English is the default. */
  language: Language
  /** OCR recognition language on the scan page. */
  scanLanguage: OcrLanguagePref
  completeOnboarding: (mode: SyncMode) => void
  setSyncMode: (mode: SyncMode) => void
  setTheme: (theme: ThemePref) => void
  setUserName: (name: string) => void
  setDriveConsentGranted: (granted: boolean) => void
  setLanguage: (language: Language) => void
  setScanLanguage: (language: OcrLanguagePref) => void
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      onboardingDone: false,
      syncMode: null,
      theme: 'system',
      userName: null,
      driveConsentGranted: false,
      language: 'en',
      scanLanguage: 'auto',
      completeOnboarding: (mode) => set({ onboardingDone: true, syncMode: mode }),
      setSyncMode: (mode) => set({ syncMode: mode }),
      setTheme: (theme) => set({ theme }),
      setUserName: (name) => set({ userName: name }),
      setDriveConsentGranted: (granted) => set({ driveConsentGranted: granted }),
      setLanguage: (language) => set({ language }),
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
