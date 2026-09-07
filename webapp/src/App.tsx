// App shell — routing, onboarding guard, theme application, bottom nav.
// Onboarding replaces the old device-registration gate: until the user
// chooses Drive or offline, only the Onboarding screen is reachable.
// All pages except Home are lazy-loaded to reduce the initial bundle.

import { lazy, Suspense, useEffect } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { BottomNav } from './components/BottomNav'
import { Onboarding } from './pages/Onboarding'
import { Home } from './pages/Home'
import { useAppStore, effectiveTheme } from './stores/appStore'

const RecipeDetail = lazy(() => import('./pages/RecipeDetail').then((m) => ({ default: m.RecipeDetail })))
const RecipeEdit = lazy(() => import('./pages/RecipeEdit').then((m) => ({ default: m.RecipeEdit })))
const Search = lazy(() => import('./pages/Search').then((m) => ({ default: m.Search })))
const Scan = lazy(() => import('./pages/Scan').then((m) => ({ default: m.Scan })))
const Settings = lazy(() => import('./pages/Settings').then((m) => ({ default: m.Settings })))
const Cookbooks = lazy(() => import('./pages/Cookbooks').then((m) => ({ default: m.Cookbooks })))
const CookbookDetail = lazy(() => import('./pages/CookbookDetail').then((m) => ({ default: m.CookbookDetail })))
const SyncStatus = lazy(() => import('./pages/SyncStatus').then((m) => ({ default: m.SyncStatus })))

function PageLoader() {
  return <main className="page"><p className="text-muted">Loading…</p></main>
}

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
      <Suspense fallback={<PageLoader />}>
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
      </Suspense>
      <BottomNav />
    </div>
  )
}
