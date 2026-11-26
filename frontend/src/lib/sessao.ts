import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { components } from '@/lib/api/schema'
import { armazenamento } from '@/lib/armazenamento'
import type { Marca } from '@/lib/marca'

/**
 * Sessão no navegador (Zustand, salva em localStorage na chave gv.sessao): token, usuário,
 * negócio escolhido e a cor dele. O cliente da API lê daqui para montar os headers e o
 * script do index.html lê a cor antes da primeira pintura.
 *
 * Os efeitos colaterais (limpar o cache das telas, pintar o <html>) ficam em
 * lib/sessao-efeitos.ts, que observa este store: valem para qualquer origem da mudança
 * (botão, 401, outra aba).
 *
 * A validade do token é decidida pelo servidor (401), não pelo relógio do celular, que pode
 * estar errado.
 */
export type Login = components['schemas']['LoginResponseDTO']

export const CHAVE_SESSAO = 'gv.sessao'

type EstadoSessao = {
  login: Login | null
  negocioId: number | null
  marca: Marca['slug'] | null
  /** Para onde voltar depois de entrar de novo (só quando a sessão expirou sozinha). */
  voltarPara: string | null
  entrar: (login: Login) => void
  escolherNegocio: (negocioId: number, marca: Marca['slug'] | null) => void
  definirMarca: (marca: Marca['slug'] | null) => void
  sairDoNegocio: () => void
  sair: () => void
  expirar: (voltarPara: string) => void
  consumirVoltarPara: () => string | null
}

export const useSessaoStore = create<EstadoSessao>()(
  persist(
    (set, get) => ({
      login: null,
      negocioId: null,
      marca: null,
      voltarPara: null,
      entrar: (login) => set({ login, negocioId: null, marca: null }),
      escolherNegocio: (negocioId, marca) => set({ negocioId, marca }),
      definirMarca: (marca) => set({ marca }),
      sairDoNegocio: () => set({ negocioId: null, marca: null }),
      sair: () => set({ login: null, negocioId: null, marca: null, voltarPara: null }),
      expirar: (voltarPara) => set({ login: null, negocioId: null, marca: null, voltarPara }),
      consumirVoltarPara: () => {
        const destino = get().voltarPara
        if (destino) set({ voltarPara: null })
        return destino
      },
    }),
    {
      name: CHAVE_SESSAO,
      storage: armazenamento,
      partialize: ({ login, negocioId, marca, voltarPara }) => ({ login, negocioId, marca, voltarPara }),
    },
  ),
)

/** Leitura fora do React (cliente da API). */
export const tokenAtual = () => useSessaoStore.getState().login?.token ?? null
export const negocioAtualId = () => useSessaoStore.getState().negocioId
