import type { ReactNode } from 'react'

/**
 * Barra fixa no rodapé das telas de lançamento (venda, troca): total e botão principal sempre
 * à vista, ao alcance do polegar. No PC fica no fim do conteúdo.
 */
export function BarraAcao({ children }: { children: ReactNode }) {
  return (
    <>
      <div className="h-28 md:hidden" aria-hidden="true" />
      <div className="pb-seguro fixed inset-x-0 bottom-0 z-30 border-t bg-card/95 backdrop-blur-lg md:static md:z-auto md:mt-2 md:rounded-2xl md:border md:bg-card md:pb-0 md:shadow-xs">
        <div className="mx-auto flex max-w-3xl items-center gap-4 px-4 py-3">{children}</div>
      </div>
    </>
  )
}
