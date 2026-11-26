import { useTable, type ColumnDef, type RowData } from '@tanstack/react-table'
import type { ReactNode } from 'react'
import { useTelaLarga } from '@/hooks/use-midia'
import { recursosTabela, type RecursosTabela } from '@/lib/tabela'
import { cn } from '@/lib/utils'

/**
 * Lista responsiva com TanStack Table: no PC, tabela com colunas; no celular, um cartão por
 * linha (o `cartao` decide o que aparece). As colunas são definidas uma vez por tela.
 */
export function TabelaDados<T extends RowData>({
  dados,
  colunas,
  cartao,
  chave,
  aoAbrir,
  rotulo,
}: {
  dados: T[]
  // a v9 tipa cada coluna pelo seu valor; aqui a lista aceita qualquer uma
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  colunas: ColumnDef<RecursosTabela, T, any>[]
  cartao: (item: T) => ReactNode
  chave: (item: T) => string | number
  aoAbrir?: (item: T) => void
  rotulo: string
}) {
  const larga = useTelaLarga()
  const tabela = useTable({ features: recursosTabela, columns: colunas, data: dados, getRowId: (r) => String(chave(r)) })

  if (!larga) {
    return (
      <ul aria-label={rotulo} className="space-y-2.5">
        {dados.map((item) => (
          <li key={chave(item)}>
            {aoAbrir ? (
              <button
                type="button"
                onClick={() => aoAbrir(item)}
                className="block w-full rounded-2xl border bg-card p-4 text-left shadow-xs transition-[border-color,transform] outline-none hover:border-primary/30 focus-visible:ring-3 focus-visible:ring-ring/50 active:scale-[0.98]"
              >
                {cartao(item)}
              </button>
            ) : (
              <div className="rounded-2xl border bg-card p-4 shadow-xs">{cartao(item)}</div>
            )}
          </li>
        ))}
      </ul>
    )
  }

  return (
    <div className="overflow-hidden rounded-2xl border bg-card shadow-xs">
      <table aria-label={rotulo} className="w-full text-sm">
        <thead className="border-b bg-muted/50 text-left text-xs font-medium text-muted-foreground">
          {tabela.getHeaderGroups().map((grupo) => (
            <tr key={grupo.id}>
              {grupo.headers.map((h) => (
                <th
                  key={h.id}
                  scope="col"
                  className={cn('h-11 px-4 font-medium', (h.column.columnDef.meta as MetaColuna)?.direita && 'text-right')}
                >
                  {h.isPlaceholder ? null : <tabela.FlexRender header={h} />}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody className="divide-y">
          {tabela.getRowModel().rows.map((linha) => (
            <tr
              key={linha.id}
              onClick={aoAbrir ? () => aoAbrir(linha.original) : undefined}
              onKeyDown={
                aoAbrir
                  ? (e) => {
                      if (e.key === 'Enter') aoAbrir(linha.original)
                    }
                  : undefined
              }
              tabIndex={aoAbrir ? 0 : undefined}
              className={cn(
                'transition-colors outline-none',
                aoAbrir && 'cursor-pointer hover:bg-accent/50 focus-visible:bg-accent',
              )}
            >
              {linha.getAllCells().map((celula) => (
                <td
                  key={celula.id}
                  className={cn('h-14 px-4', (celula.column.columnDef.meta as MetaColuna)?.direita && 'numeros text-right')}
                >
                  <tabela.FlexRender cell={celula} />
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

/** meta das colunas: { direita: true } alinha números à direita. */
type MetaColuna = { direita?: boolean } | undefined
