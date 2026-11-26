import { cn } from '@/lib/utils'

/** Emblema do app (login e escolha do negócio): barras de vendas sobre a faixa da marca. */
export function EmblemaApp({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 64 64" aria-hidden="true" className={cn('size-14', className)}>
      <rect width="64" height="64" rx="18" className="fill-primary" />
      <path
        d="M18 42V30M28 42V22M38 42V34M48 42V26"
        className="stroke-primary-foreground"
        strokeWidth="6"
        strokeLinecap="round"
      />
      <path d="M14 50h36" stroke="url(#faixa)" strokeWidth="4" strokeLinecap="round" />
      <defs>
        <linearGradient id="faixa" x1="14" x2="50" y1="0" y2="0" gradientUnits="userSpaceOnUse">
          <stop offset="0" stopColor="oklch(0.66 0.2 25)" />
          <stop offset="1" stopColor="oklch(0.76 0.12 236)" />
        </linearGradient>
      </defs>
    </svg>
  )
}
