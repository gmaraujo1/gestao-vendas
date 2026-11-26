import { useQuery } from '@tanstack/react-query'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio } from '@/lib/api/client'

/** Categorias do negócio (lista curta, sem paginação). */
export function useCategorias(apenasAtivas = false) {
  const negocioId = useNegocioId()
  return useQuery({
    queryKey: ['categorias', negocioId, { apenasAtivas }],
    queryFn: () =>
      dados(api.GET('/api/categoria', { params: { header: doNegocio(negocioId), query: { apenasAtivas } } })),
    staleTime: 5 * 60_000,
  })
}

/** Veículos (globais: os dois negócios usam os mesmos). */
export function useVeiculos(apenasAtivos = false) {
  return useQuery({
    queryKey: ['veiculos', { apenasAtivos }],
    queryFn: () => dados(api.GET('/api/veiculo', { params: { query: { apenasAtivos } } })),
    staleTime: 5 * 60_000,
  })
}
