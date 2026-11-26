import { OPCOES_TEMA } from '@/components/layout/navegacao'
import { useTemaStore } from '@/lib/tema'
import { cn } from '@/lib/utils'

/** Claro, escuro ou automático (segue o celular). Três botões de alternância. */
export function SeletorTema({ className }: { className?: string }) {
  const tema = useTemaStore((s) => s.tema)
  const setTema = useTemaStore((s) => s.setTema)
  return (
    <div role="group" aria-label="Aparência" className={cn('grid grid-cols-3 gap-1 rounded-xl bg-muted p-1', className)}>
      {OPCOES_TEMA.map(({ valor, rotulo, icone: Icone }) => {
        const ativo = tema === valor
        return (
          <button
            key={valor}
            type="button"
            aria-pressed={ativo}
            onClick={() => setTema(valor)}
            className={cn(
              'flex h-11 items-center justify-center gap-1.5 rounded-lg text-sm font-medium text-muted-foreground transition-colors outline-none focus-visible:ring-3 focus-visible:ring-ring/50',
              ativo && 'bg-card text-foreground shadow-sm',
            )}
          >
            <Icone className="size-4" aria-hidden="true" />
            {rotulo}
          </button>
        )
      })}
    </div>
  )
}
