import { queryClient } from '@/lib/query-client'
import { encerrarSessao, escolherNegocio } from '@/lib/sessao'

/**
 * Ações de sessão vindas da interface: além do localStorage, limpam o cache do React Query,
 * para não mostrar dados de outro usuário ou do outro negócio.
 * Mesmo assim, inclua o negocioId na queryKey das consultas de dados do negócio.
 */
export function sair() {
  encerrarSessao()
  queryClient.clear()
}

export function trocarNegocio(negocioId: number) {
  escolherNegocio(negocioId)
  queryClient.clear()
}
