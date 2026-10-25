// i18n — minimal translation layer. The active language lives in appStore
// (persisted); `useT()` returns a `t(key, vars)` function bound to it, so
// components re-render when the language changes. English is the
// reference catalog and the fallback for missing keys.
// Pluralized keys pick the `_one` / `_other` suffix when `vars.count` is a
// number — enough for en/sv/fi/de/fr, which only distinguish one/many.

import { useCallback } from 'react'
import en from './en'
import sv from './sv'
import fi from './fi'
import de from './de'
import fr from './fr'
import { useAppStore } from '../stores/appStore'

export type Language = 'en' | 'sv' | 'fi' | 'de' | 'fr'

/** Selectable UI languages, labeled with their own endonym. */
export const LANGUAGES: { value: Language; label: string }[] = [
  { value: 'en', label: 'English' },
  { value: 'sv', label: 'Svenska' },
  { value: 'fi', label: 'Suomi' },
  { value: 'de', label: 'Deutsch' },
  { value: 'fr', label: 'Français' },
]

const catalogs: Record<Language, Record<string, string>> = { en, sv, fi, de, fr }

export type TFunc = (key: string, vars?: Record<string, string | number>) => string

export function translate(
  language: Language,
  key: string,
  vars?: Record<string, string | number>,
): string {
  const catalog = catalogs[language] ?? catalogs.en
  let text: string | undefined
  if (vars && typeof vars.count === 'number') {
    const pluralKey = `${key}_${vars.count === 1 ? 'one' : 'other'}`
    text = catalog[pluralKey] ?? catalogs.en[pluralKey]
  }
  if (text == null) text = catalog[key] ?? catalogs.en[key] ?? key
  if (vars) {
    for (const [name, value] of Object.entries(vars)) {
      text = text.replaceAll(`{${name}}`, String(value))
    }
  }
  return text
}

/** The translator for the current app language. Re-renders on change. */
export function useT(): TFunc {
  const language = useAppStore((s) => s.language)
  return useCallback((key, vars) => translate(language, key, vars), [language])
}

/** Recipe time as "1 hr 20 min" etc., localized. */
export function formatRecipeTime(minutes: number | null, t: TFunc): string | null {
  if (minutes == null) return null
  if (minutes < 60) return t('common.timeMin', { n: minutes })
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? t('common.timeHrMin', { h, m }) : t('common.timeHr', { n: h })
}
