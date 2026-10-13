// Button — port of the Organic `.btn` classes.

import type { ButtonHTMLAttributes, ReactNode } from 'react'

type Variant = 'primary' | 'secondary' | 'ghost' | 'icon'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  block?: boolean
  children?: ReactNode
}

const variantClass: Record<Variant, string> = {
  primary: 'btn-primary',
  secondary: 'btn-secondary',
  ghost: 'btn-ghost',
  icon: 'btn-icon',
}

export function Button({ variant = 'primary', block, className, children, ...rest }: ButtonProps) {
  const classes = ['btn', variantClass[variant], block ? 'btn-block' : '', className]
    .filter(Boolean)
    .join(' ')
  return (
    <button className={classes} {...rest}>
      {children}
    </button>
  )
}
