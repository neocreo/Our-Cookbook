// Card — port of the Organic `.card` classes with elevation utilities.

import type { ReactNode } from 'react'

type Elevation = 'sm' | 'md' | 'lg'

interface CardProps {
  kicker?: string
  title?: string
  meta?: ReactNode
  children?: ReactNode
  elevation?: Elevation
  as?: 'div' | 'article' | 'section'
  onClick?: () => void
}

const elevClass: Record<Elevation, string> = {
  sm: 'elev-sm',
  md: 'elev-md',
  lg: 'elev-lg',
}

export function Card({
  kicker,
  title,
  meta,
  children,
  elevation = 'sm',
  as: Tag = 'div',
  onClick,
}: CardProps) {
  const className = ['card', elevClass[elevation]].join(' ')
  return (
    <Tag className={className} onClick={onClick} role={onClick ? 'button' : undefined} tabIndex={onClick ? 0 : undefined}>
      {kicker && <div className="card-kicker">{kicker}</div>}
      {title && <div className="card-title">{title}</div>}
      {children && <div className="card-body">{children}</div>}
      {meta && <div className="card-meta">{meta}</div>}
    </Tag>
  )
}
