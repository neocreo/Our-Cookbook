// Tag — port of the Organic `.tag` classes.

interface TagProps {
  variant?: 'accent' | 'accent-2' | 'neutral' | 'outline'
  children: string
}

export function Tag({ variant = 'neutral', children }: TagProps) {
  const cls = ['tag', `tag-${variant}`].join(' ')
  return <span className={cls}>{children}</span>
}
