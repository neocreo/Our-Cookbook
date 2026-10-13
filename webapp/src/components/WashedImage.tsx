// WashedImage — wraps food photography in the Organic `.washed` filter so it
// sits back into the warm page. Sizing and corners come from the global.css
// classes (`.img-cover`, `.thumb-*`); pass a className, never a style.

interface WashedImageProps {
  src: string
  alt: string
  className?: string
}

export function WashedImage({ src, alt, className }: WashedImageProps) {
  const classes = ['washed', className].filter(Boolean).join(' ')
  return <img src={src} alt={alt} className={classes} loading="lazy" />
}
