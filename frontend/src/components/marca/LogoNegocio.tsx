import { Wheat } from 'lucide-react'
import logoLaticinio from '@/assets/logo-laticinio.png'
import type { Marca } from '@/lib/marca'
import { cn } from '@/lib/utils'

const TAMANHOS = {
  sm: 'h-9 w-12 rounded-lg',
  md: 'h-14 w-20 rounded-xl',
  lg: 'h-20 w-28 rounded-2xl',
}

/**
 * Selo do negócio. O Laticínio usa a logo Bomtlê, sempre sobre branco (a arte tem fundo branco,
 * então o selo continua branco no tema escuro). O Pão Nino, sem logo ainda, usa um trigo.
 */
export function LogoNegocio({
  marca,
  tamanho = 'md',
  className,
}: {
  marca: Marca | null
  tamanho?: keyof typeof TAMANHOS
  className?: string
}) {
  const base = cn(
    'grid shrink-0 place-items-center overflow-hidden ring-1 ring-black/5 dark:ring-white/10',
    TAMANHOS[tamanho],
    className,
  )
  if (marca?.slug === 'laticinio') {
    return (
      <span className={cn(base, 'bg-white p-1')}>
        <img src={logoLaticinio} alt="Bomtlê" className="size-full object-contain" />
      </span>
    )
  }
  return (
    <span
      className={cn(base, 'bg-linear-to-br from-marca-1 to-marca-2 text-white')}
      aria-label={marca?.nomeCurto}
    >
      <Wheat className={tamanho === 'sm' ? 'size-5' : tamanho === 'md' ? 'size-7' : 'size-9'} />
    </span>
  )
}
