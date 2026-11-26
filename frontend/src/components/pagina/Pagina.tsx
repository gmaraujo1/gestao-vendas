import { ChevronLeft } from 'lucide-react'
import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { cn } from '@/lib/utils'

/**
 * Cabeçalho padrão das telas: voltar (quando é uma tela "filha"), título, subtítulo e ações
 * (no celular as ações ficam ao lado do título; no PC, alinhadas à direita).
 */
export function Pagina({
  titulo,
  subtitulo,
  voltar,
  acoes,
  children,
  className,
}: {
  titulo: ReactNode
  subtitulo?: ReactNode
  voltar?: string
  acoes?: ReactNode
  children: ReactNode
  className?: string
}) {
  return (
    <div className={cn('space-y-5 motion-safe:animate-in motion-safe:fade-in-0 motion-safe:slide-in-from-bottom-1', className)}>
      <header className="flex items-start gap-3">
        {voltar && (
          <Link
            to={voltar}
            aria-label="Voltar"
            className="-ml-2 grid size-10 shrink-0 place-items-center rounded-full text-muted-foreground transition-colors outline-none hover:bg-accent hover:text-accent-foreground focus-visible:ring-3 focus-visible:ring-ring/50"
          >
            <ChevronLeft className="size-6" aria-hidden="true" />
          </Link>
        )}
        <div className="min-w-0 flex-1 pt-1">
          <h1 className="text-2xl font-semibold tracking-tight text-balance">{titulo}</h1>
          {subtitulo && <p className="mt-0.5 text-sm text-pretty text-muted-foreground">{subtitulo}</p>}
        </div>
        {acoes && <div className="flex shrink-0 items-center gap-2">{acoes}</div>}
      </header>
      {children}
    </div>
  )
}

/** Bloco com título dentro de uma tela (seções de detalhe, formulários longos). */
export function Secao({ titulo, acao, children, className }: { titulo?: ReactNode; acao?: ReactNode; children: ReactNode; className?: string }) {
  return (
    <section className={cn('space-y-2.5', className)}>
      {(titulo || acao) && (
        <div className="flex items-center justify-between gap-2 px-1">
          {titulo && <h2 className="text-sm font-medium text-muted-foreground">{titulo}</h2>}
          {acao}
        </div>
      )}
      {children}
    </section>
  )
}

/** Superfície branca padrão (cartão). */
export function Cartao({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn('rounded-2xl border bg-card text-card-foreground shadow-xs', className)}>{children}</div>
}
