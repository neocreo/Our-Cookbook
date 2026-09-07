// BottomNav — primary navigation. Fixed to the bottom; uses React Router
// NavLink so the active item gets aria-current="page".

import { NavLink } from 'react-router-dom'
import { Home, Search, Library, ScanLine, Settings } from 'lucide-react'

const items = [
  { to: '/', label: 'Home', Icon: Home, end: true },
  { to: '/cookbooks', label: 'Cookbooks', Icon: Library, end: false },
  { to: '/scan', label: 'Scan', Icon: ScanLine, end: false },
  { to: '/search', label: 'Search', Icon: Search, end: false },
  { to: '/settings', label: 'Settings', Icon: Settings, end: false },
]

export function BottomNav() {
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
