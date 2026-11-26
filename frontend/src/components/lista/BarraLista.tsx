import { Search, X } from 'lucide-react'
import type { ReactNode } from 'react'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'

/** Campo de busca das listas (com botão de limpar). */
export function CampoBusca({
  valor,
  aoMudar,
  placeholder,
  rotulo,
  className,
}: {
  valor: string
  aoMudar: (v: string) => void
  placeholder: string
  rotulo: string
  className?: string
}) {
  return (
    <div className={cn('relative', className)}>
      <Search className="pointer-events-none absolute top-1/2 left-3.5 size-5 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
      <Input
        type="search"
        enterKeyHint="search"
        aria-label={rotulo}
        placeholder={placeholder}
        value={valor}
        onChange={(e) => aoMudar(e.target.value)}
        className="pr-11 pl-11 [&::-webkit-search-cancel-button]:hidden"
      />
      {valor && (
        <button
          type="button"
          onClick={() => aoMudar('')}
          aria-label="Limpar busca"
          className="absolute inset-y-0 right-0 grid w-11 place-items-center text-muted-foreground outline-none hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50"
        >
          <X className="size-4" aria-hidden="true" />
        </button>
      )}
    </div>
  )
}

/** Fileira de filtros em "chips" que rola de lado no celular. */
export function Filtros<T extends string>({
  opcoes,
  valor,
  aoMudar,
  rotulo,
}: {
  opcoes: { valor: T; rotulo: ReactNode }[]
  valor: T
  aoMudar: (v: T) => void
  rotulo: string
}) {
  return (
    <div role="group" aria-label={rotulo} className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1 [scrollbar-width:none]">
      {opcoes.map((o) => {
        const ativo = o.valor === valor
        return (
          <button
            key={o.valor}
            type="button"
            aria-pressed={ativo}
            onClick={() => aoMudar(o.valor)}
            className={cn(
              'h-10 shrink-0 rounded-full border px-4 text-sm font-medium whitespace-nowrap transition-[background-color,border-color,color] outline-none focus-visible:ring-3 focus-visible:ring-ring/50',
              ativo ? 'border-primary bg-primary text-primary-foreground' : 'bg-card text-muted-foreground hover:text-foreground',
            )}
          >
            {o.rotulo}
          </button>
        )
      })}
    </div>
  )
}
