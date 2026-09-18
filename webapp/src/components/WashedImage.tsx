// WashedImage — wraps food photography in the Organic `.washed` filter so it
// sits back into the warm page. Rounded, over-sized corners.

import type { CSSProperties } from 'react'

interface WashedImageProps {
  src: string
  alt: string
  className?: string
  style?: CSSProperties
}

export function WashedImage({ src, alt, className, style }: WashedImageProps) {
  const classes = ['washed', className].filter(Boolean).join(' ')
  return (
    <img
      src={src}
      alt={alt}
      className={classes}
      loading="lazy"
      style={{ borderRadius: 'var(--radius-lg)', width: '100%', objectFit: 'cover', ...style }}
    />
  )
}
