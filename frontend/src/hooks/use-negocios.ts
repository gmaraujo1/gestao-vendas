import { useQuery } from '@tanstack/react-query'
import { api, dados } from '@/lib/api/client'
import { marcaDe, marcaPorSlug } from '@/lib/marca'
import { useSessao } from '@/hooks/use-sessao'

/** Negócios que o usuário pode abrir (GET /api/negocio/meus). Muda raramente: fica em cache. */
export function useMeusNegocios() {
  const { login } = useSessao()
  return useQuery({
    queryKey: ['negocios', login?.usuarioId],
    queryFn: () => dados(api.GET('/api/negocio/meus')),
    enabled: !!login,
    staleTime: Infinity,
  })
}

/**
 * O negócio ativo e a marca dele. Enquanto a lista carrega, a marca vem da sessão salva,
 * para a tela não piscar com as cores neutras.
 */
export function useNegocioAtual() {
  const { negocioId, marca: slugSalvo } = useSessao()
  const consulta = useMeusNegocios()
  const negocio = consulta.data?.find((n) => n.id === negocioId) ?? null
  const marca = marcaDe(negocio?.codigo) ?? marcaPorSlug(slugSalvo)
  return { ...consulta, negocio, marca }
}
