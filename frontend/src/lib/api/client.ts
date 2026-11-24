import createClient, { type Middleware } from 'openapi-fetch'
import type { paths } from '@/lib/api/schema'
import { ApiError, paraApiError } from '@/lib/api/erros'
import { lerLogin, lerNegocioId } from '@/lib/sessao'

/**
 * Cliente tipado da API, gerado a partir do OpenAPI do backend (npm run gen:api).
 *
 * Uso:
 *   const { data } = await api.GET('/api/produto', { params: { query: { descricao: 'leite' } } })
 *
 * O middleware põe o token (Authorization) e o negócio ativo (X-Negocio-Id) em toda
 * requisição. Em dev, VITE_API_URL fica vazio e o proxy do Vite repassa /api ao Spring Boot.
 */
export const api = createClient<paths>({ baseUrl: import.meta.env.VITE_API_URL ?? '' })

let aoExpirar: () => void = () => {}

/** Registrado no main.tsx: o que fazer quando a API responde 401 (sessão vencida). */
export function aoExpirarSessao(callback: () => void) {
  aoExpirar = callback
}

const autenticacao: Middleware = {
  onRequest({ request }) {
    const login = lerLogin()
    if (login?.token) request.headers.set('Authorization', `Bearer ${login.token}`)
    const negocioId = lerNegocioId()
    if (negocioId != null) request.headers.set('X-Negocio-Id', String(negocioId))
    return request
  },
  onResponse({ response, request }) {
    // 401 no login é só senha errada; em qualquer outra chamada é sessão vencida
    if (response.status === 401 && new URL(request.url).pathname !== '/api/auth/login') {
      aoExpirar()
    }
    return response
  },
}

api.use(autenticacao)

type Resposta<T> = Promise<{ data?: T; error?: unknown; response: Response }>

/**
 * Para usar com React Query: devolve `data` ou lança ApiError.
 *   useQuery({ queryKey: ['produtos', negocioId], queryFn: () => dados(api.GET('/api/produto')) })
 * Falha de rede lança TypeError (não ApiError).
 */
export async function dados<T>(promessa: Resposta<T>): Promise<T> {
  const { data, error, response } = await promessa
  if (!response.ok) throw paraApiError(response.status, error)
  if (data === undefined) throw new ApiError(response.status, 'Resposta vazia da API')
  return data
}

/** Para endpoints sem corpo na resposta (DELETE, 204): só confere o sucesso. */
export async function semConteudo(promessa: Resposta<unknown>): Promise<void> {
  const { error, response } = await promessa
  if (!response.ok) throw paraApiError(response.status, error)
}
