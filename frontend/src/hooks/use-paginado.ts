import { keepPreviousData, useInfiniteQuery, type QueryKey } from '@tanstack/react-query'
import { useMemo } from 'react'

export type Pagina<T> = {
  conteudo?: T[]
  pagina?: number
  totalElementos?: number
  totalPaginas?: number
}

/**
 * Listagem paginada da API (PaginaDTO) com "Carregar mais": no celular, rolar e pedir mais
 * é melhor que paginação numerada.
 */
export function usePaginado<T>(
  queryKey: QueryKey,
  buscar: (pagina: number) => Promise<Pagina<T>>,
  opcoes: { enabled?: boolean } = {},
) {
  const consulta = useInfiniteQuery({
    queryKey,
    queryFn: ({ pageParam }) => buscar(pageParam),
    initialPageParam: 0,
    getNextPageParam: (ultima) => {
      const atual = ultima.pagina ?? 0
      return atual + 1 < (ultima.totalPaginas ?? 0) ? atual + 1 : undefined
    },
    enabled: opcoes.enabled,
    // ao buscar/filtrar, a lista anterior fica na tela até a nova chegar (sem piscar)
    placeholderData: keepPreviousData,
  })
  const itens = useMemo(() => consulta.data?.pages.flatMap((p) => p.conteudo ?? []) ?? [], [consulta.data])
  const total = consulta.data?.pages[0]?.totalElementos ?? 0
  return { ...consulta, itens, total }
}

export const TAMANHO_PAGINA = 30
