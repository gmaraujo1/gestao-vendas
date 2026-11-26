import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'

/** Mensagem centralizada para telas vazias, sem permissão, em construção ou com erro. */
export function EstadoVazio({
  icone: Icone,
  titulo,
  descricao,
  acao,
}: {
  icone: LucideIcon
  titulo: string
  descricao?: string
  acao?: ReactNode
}) {
  return (
    <div className="flex flex-col items-center px-6 py-16 text-center motion-safe:animate-in motion-safe:fade-in-0 motion-safe:slide-in-from-bottom-2">
      <span className="mb-4 grid size-16 place-items-center rounded-2xl bg-accent text-accent-foreground">
        <Icone className="size-7" aria-hidden="true" />
      </span>
      <h1 className="text-lg font-semibold text-balance">{titulo}</h1>
      {descricao && <p className="mt-1.5 max-w-sm text-pretty text-muted-foreground">{descricao}</p>}
      {acao && <div className="mt-6">{acao}</div>}
    </div>
  )
}
