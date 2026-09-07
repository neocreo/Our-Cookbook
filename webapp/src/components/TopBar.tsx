// TopBar — port of the Organic `.nav` header. Brand + trailing actions.

import type { ReactNode } from 'react'

interface TopBarProps {
  title: string
  actions?: ReactNode
}

export function TopBar({ title, actions }: TopBarProps) {
  return (
    <header className="nav">
      <span className="nav-brand">{title}</span>
      {actions && <div className="nav-actions">{actions}</div>}
    </header>
  )
}
