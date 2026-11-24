import type { components } from '@/lib/api/schema'

/**
 * Sessão do usuário no navegador: token JWT, dados do login e o negócio escolhido
 * (Laticínio ou Pão Nino). O cliente da API lê daqui para montar os headers.
 *
 * Funções puras sobre o localStorage. Para sair ou trocar de negócio pela interface use
 * `sair()`/`trocarNegocio()` de `@/lib/sessao-acoes`, que também limpam o cache das telas.
 */
export type Login = components['schemas']['LoginResponseDTO']

const CHAVE_LOGIN = 'gv.login'
const CHAVE_NEGOCIO = 'gv.negocioId'

export function lerLogin(): Login | null {
  const bruto = localStorage.getItem(CHAVE_LOGIN)
  if (!bruto) return null
  try {
    const login = JSON.parse(bruto) as Login
    const expiraEm = typeof login.expiraEm === 'string' ? new Date(login.expiraEm).getTime() : NaN
    if (typeof login.token !== 'string' || Number.isNaN(expiraEm) || expiraEm <= Date.now()) {
      encerrarSessao()
      return null
    }
    return login
  } catch {
    encerrarSessao()
    return null
  }
}

export function salvarLogin(login: Login) {
  localStorage.setItem(CHAVE_LOGIN, JSON.stringify(login))
}

export function lerNegocioId(): number | null {
  const valor = Number(localStorage.getItem(CHAVE_NEGOCIO))
  return Number.isInteger(valor) && valor > 0 ? valor : null
}

export function escolherNegocio(negocioId: number) {
  localStorage.setItem(CHAVE_NEGOCIO, String(negocioId))
}

export function encerrarSessao() {
  localStorage.removeItem(CHAVE_LOGIN)
  localStorage.removeItem(CHAVE_NEGOCIO)
}
