// WashedImage — wraps food photography in the Organic `.washed` filter so it
// sits back into the warm page. Rounded, over-sized corners.

interface WashedImageProps {
  src: string
  alt: string
  className?: string
}

export function WashedImage({ src, alt, className }: WashedImageProps) {
  const classes = ['washed', className].filter(Boolean).join(' ')
  return (
    <img
      src={src}
      alt={alt}
      className={classes}
      loading="lazy"
      style={{ borderRadius: 'var(--radius-lg)', width: '100%', objectFit: 'cover' }}
    />
  )
}
