// Tag — port of the Organic `.tag` classes.

import type { ReactNode } from 'react'

interface TagProps {
  variant?: 'accent' | 'accent-2' | 'neutral' | 'outline'
  children: ReactNode
}

export function Tag({ variant = 'neutral', children }: TagProps) {
  const cls = ['tag', `tag-${variant}`].join(' ')
  return <span className={cls}>{children}</span>
}
