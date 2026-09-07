// App shell — routing, onboarding guard, theme application, bottom nav.
// Onboarding replaces the old device-registration gate: until the user
// chooses Drive or offline, only the Onboarding screen is reachable.

import { useEffect } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { BottomNav } from './components/BottomNav'
import { Onboarding } from './pages/Onboarding'
import { Home } from './pages/Home'
import { RecipeDetail } from './pages/RecipeDetail'
import { RecipeEdit } from './pages/RecipeEdit'
import { Search } from './pages/Search'
import { Scan } from './pages/Scan'
import { Settings } from './pages/Settings'
import { Cookbooks } from './pages/Cookbooks'
import { CookbookDetail } from './pages/CookbookDetail'
import { SyncStatus } from './pages/SyncStatus'
import { useAppStore, effectiveTheme } from './stores/appStore'

function useTheme() {
  const theme = useAppStore((s) => s.theme)
  useEffect(() => {
    const root = document.documentElement
    const apply = () => {
      root.dataset.theme = effectiveTheme(theme)
    }
    apply()
    if (theme === 'system') {
      const mq = window.matchMedia('(prefers-color-scheme: dark)')
      mq.addEventListener('change', apply)
      return () => mq.removeEventListener('change', apply)
    }
  }, [theme])
}

export function App() {
  const onboardingDone = useAppStore((s) => s.onboardingDone)
  useTheme()

  if (!onboardingDone) return <Onboarding />

  return (
    <div className="app-shell">
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/recipes/new" element={<RecipeEdit />} />
        <Route path="/recipes/:id" element={<RecipeDetail />} />
        <Route path="/recipes/:id/edit" element={<RecipeEdit />} />
        <Route path="/cookbooks" element={<Cookbooks />} />
        <Route path="/cookbooks/:id" element={<CookbookDetail />} />
        <Route path="/search" element={<Search />} />
        <Route path="/sync" element={<SyncStatus />} />
        <Route path="/scan" element={<Scan />} />
        <Route path="/settings" element={<Settings />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <BottomNav />
    </div>
  )
}
