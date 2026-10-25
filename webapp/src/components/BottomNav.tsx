// BottomNav — primary navigation. Fixed to the bottom; uses React Router
// NavLink so the active item gets aria-current="page".

import { NavLink } from 'react-router-dom'
import { Home, Search, Library, ScanLine, Settings } from 'lucide-react'
import { useT } from '../i18n'

export function BottomNav() {
  const t = useT()
  const items = [
    { to: '/', label: t('nav.home'), Icon: Home, end: true },
    { to: '/cookbooks', label: t('nav.cookbooks'), Icon: Library, end: false },
    { to: '/scan', label: t('nav.scan'), Icon: ScanLine, end: false },
    { to: '/search', label: t('nav.recipes'), Icon: Search, end: false },
    { to: '/settings', label: t('nav.settings'), Icon: Settings, end: false },
  ]

  return (
    <nav className="bottom-nav" aria-label="Primary">
      {items.map(({ to, label, Icon, end }) => (
        <NavLink key={to} to={to} end={end}>
          <Icon strokeWidth={2.75} />
          <span>{label}</span>
        </NavLink>
      ))}
    </nav>
  )
}
