import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'

const ESTILOS = {
  neutro: 'bg-muted text-muted-foreground',
  sucesso: 'bg-sucesso/12 text-sucesso',
  perigo: 'bg-destructive/10 text-destructive',
  marca: 'bg-accent text-accent-foreground',
} as const

/** Etiqueta curta de situação (Inativo, Cancelada, Negativo...). */
export function Etiqueta({ tom = 'neutro', children }: { tom?: keyof typeof ESTILOS; children: ReactNode }) {
  return (
    <span className={cn('inline-flex h-6 items-center rounded-full px-2.5 text-xs font-medium whitespace-nowrap', ESTILOS[tom])}>
      {children}
    </span>
  )
}
