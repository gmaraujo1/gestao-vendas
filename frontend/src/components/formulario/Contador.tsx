import { Minus, Plus } from 'lucide-react'
import { paraCampo, paraNumero } from '@/lib/numeros'
import { cn } from '@/lib/utils'

/** Quantidade com botões − e + (grandes para o polegar) e digitação livre no meio. */
export function Contador({
  valor,
  aoMudar,
  rotulo,
  invalido,
  className,
}: {
  valor: string
  aoMudar: (v: string) => void
  rotulo: string
  invalido?: boolean
  className?: string
}) {
  const n = paraNumero(valor) ?? 0
  const passo = (delta: number) => aoMudar(paraCampo(Math.max(0, Math.round((n + delta) * 1000) / 1000), 'quantidade'))
  const botao =
    'grid size-11 shrink-0 place-items-center rounded-lg text-foreground transition-[background-color,transform] outline-none hover:bg-accent focus-visible:ring-3 focus-visible:ring-ring/50 active:scale-95 disabled:opacity-40'
  return (
    <div
      className={cn(
        'flex h-12 items-center rounded-xl border bg-card p-0.5',
        invalido && 'border-destructive',
        className,
      )}
    >
      <button type="button" className={botao} aria-label={`Diminuir ${rotulo}`} onClick={() => passo(-1)} disabled={n <= 1}>
        <Minus className="size-4" aria-hidden="true" />
      </button>
      <input
        aria-label={rotulo}
        aria-invalid={invalido || undefined}
        inputMode="decimal"
        value={valor}
        onChange={(e) => aoMudar(e.target.value)}
        onFocus={(e) => e.target.select()}
        className="numeros h-full w-full min-w-0 bg-transparent text-center text-base font-semibold outline-none"
      />
      <button type="button" className={botao} aria-label={`Aumentar ${rotulo}`} onClick={() => passo(1)}>
        <Plus className="size-4" aria-hidden="true" />
      </button>
    </div>
  )
}
