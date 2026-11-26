import { useSessao } from '@/hooks/use-sessao'

/** Id do negócio ativo. Só use dentro das rotas protegidas por ExigeNegocio. */
export function useNegocioId(): number {
  const { negocioId } = useSessao()
  if (negocioId == null) throw new Error('useNegocioId fora de ExigeNegocio')
  return negocioId
}
