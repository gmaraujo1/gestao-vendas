import type { ComponentProps, ReactNode } from 'react'
import { ChevronDown } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'

/** Campo numérico com o teclado decimal do celular (aceita vírgula). */
export function InputNumero({
  prefixo,
  sufixo,
  className,
  ...props
}: ComponentProps<'input'> & { prefixo?: ReactNode; sufixo?: ReactNode }) {
  return (
    <div className="relative">
      {prefixo && (
        <span className="pointer-events-none absolute inset-y-0 left-0 grid w-11 place-items-center text-muted-foreground">
          {prefixo}
        </span>
      )}
      <Input
        type="text"
        inputMode="decimal"
        autoComplete="off"
        className={cn('numeros', prefixo && 'pl-11', sufixo && 'pr-14', className)}
        {...props}
      />
      {sufixo && (
        <span className="pointer-events-none absolute inset-y-0 right-0 grid w-14 place-items-center text-sm text-muted-foreground">
          {sufixo}
        </span>
      )}
    </div>
  )
}

/** Select nativo com a aparência dos inputs: no celular abre o seletor do sistema. */
export function Selecao({ className, children, ...props }: ComponentProps<'select'>) {
  return (
    <div className="relative">
      <select
        className={cn(
          'h-12 w-full appearance-none rounded-lg border border-input bg-card pr-10 pl-3.5 text-base outline-none transition-[border-color,box-shadow] focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 aria-invalid:border-destructive dark:bg-input/30',
          className,
        )}
        {...props}
      >
        {children}
      </select>
      <ChevronDown className="pointer-events-none absolute top-1/2 right-3.5 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
    </div>
  )
}

/**
 * Escolha única em "pílulas" (tipo de despesa, motivo da perda...): mais rápido que um select
 * quando são poucas opções.
 */
export function Pilulas<T extends string>({
  opcoes,
  valor,
  aoMudar,
  rotulo,
  className,
}: {
  opcoes: { valor: T; rotulo: string }[]
  valor: T | null | undefined
  aoMudar: (v: T) => void
  rotulo: string
  className?: string
}) {
  return (
    <div role="group" aria-label={rotulo} className={cn('flex flex-wrap gap-2', className)}>
      {opcoes.map((o) => {
        const ativo = o.valor === valor
        return (
          <button
            key={o.valor}
            type="button"
            aria-pressed={ativo}
            onClick={() => aoMudar(o.valor)}
            className={cn(
              'h-11 rounded-full border px-4 text-sm font-medium transition-[background-color,border-color,color] outline-none focus-visible:ring-3 focus-visible:ring-ring/50',
              ativo
                ? 'border-primary bg-primary text-primary-foreground'
                : 'bg-card text-foreground hover:border-primary/40',
            )}
          >
            {o.rotulo}
          </button>
        )
      })}
    </div>
  )
}
