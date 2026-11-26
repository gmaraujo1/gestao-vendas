import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { armazenamento } from '@/lib/armazenamento'
import type { Unidade } from '@/lib/rotulos'

/**
 * Rascunho da venda em andamento (Zustand, salvo no aparelho). Se o app fechar no meio da
 * venda, nada se perde. Também lembra o último preço cobrado de cada produto, que vira a
 * sugestão da próxima venda.
 */
export type ItemCarrinho = {
  produtoId: number
  descricao: string
  unidade?: Unidade
  quantidade: string
  preco: string
}

type Rascunho = {
  clienteId: number | null
  clienteNome: string | null
  itens: ItemCarrinho[]
  observacao: string
  /** Vazio = agora. */
  dataHora: string
}

const VAZIO: Rascunho = { clienteId: null, clienteNome: null, itens: [], observacao: '', dataHora: '' }

type EstadoCarrinho = {
  /** Usuário dono dos rascunhos: outro usuário no mesmo aparelho começa do zero. */
  donoId: number | null
  porNegocio: Record<number, Rascunho>
  ultimosPrecos: Record<string, string>
  rascunho: (negocioId: number) => Rascunho
  atualizar: (negocioId: number, mudanca: Partial<Rascunho>) => void
  adicionarItem: (negocioId: number, item: Omit<ItemCarrinho, 'quantidade' | 'preco'>) => void
  alterarItem: (negocioId: number, produtoId: number, mudanca: Partial<ItemCarrinho>) => void
  removerItem: (negocioId: number, produtoId: number) => void
  limpar: (negocioId: number) => void
  lembrarPrecos: (negocioId: number, itens: ItemCarrinho[]) => void
}

const chavePreco = (negocioId: number, produtoId: number) => `${negocioId}:${produtoId}`

export const useCarrinho = create<EstadoCarrinho>()(
  persist(
    (set, get) => {
      const mudar = (negocioId: number, f: (r: Rascunho) => Rascunho) =>
        set((s) => ({ porNegocio: { ...s.porNegocio, [negocioId]: f(s.porNegocio[negocioId] ?? VAZIO) } }))
      return {
        donoId: null,
        porNegocio: {},
        ultimosPrecos: {},
        rascunho: (negocioId) => get().porNegocio[negocioId] ?? VAZIO,
        atualizar: (negocioId, mudanca) => mudar(negocioId, (r) => ({ ...r, ...mudanca })),
        adicionarItem: (negocioId, item) =>
          mudar(negocioId, (r) => {
            const existente = r.itens.find((i) => i.produtoId === item.produtoId)
            if (existente) return r
            const preco = get().ultimosPrecos[chavePreco(negocioId, item.produtoId)] ?? ''
            return { ...r, itens: [...r.itens, { ...item, quantidade: '1', preco }] }
          }),
        alterarItem: (negocioId, produtoId, mudanca) =>
          mudar(negocioId, (r) => ({
            ...r,
            itens: r.itens.map((i) => (i.produtoId === produtoId ? { ...i, ...mudanca } : i)),
          })),
        removerItem: (negocioId, produtoId) =>
          mudar(negocioId, (r) => ({ ...r, itens: r.itens.filter((i) => i.produtoId !== produtoId) })),
        limpar: (negocioId) => mudar(negocioId, () => VAZIO),
        lembrarPrecos: (negocioId, itens) =>
          set((s) => ({
            ultimosPrecos: {
              ...s.ultimosPrecos,
              ...Object.fromEntries(itens.map((i) => [chavePreco(negocioId, i.produtoId), i.preco])),
            },
          })),
      }
    },
    {
      name: 'gv.carrinho',
      storage: armazenamento,
      version: 1,
      // formato antigo ou desconhecido: começa vazio em vez de quebrar a tela
      migrate: () => ({ donoId: null, porNegocio: {}, ultimosPrecos: {} }),
      partialize: ({ donoId, porNegocio, ultimosPrecos }) => ({ donoId, porNegocio, ultimosPrecos }),
    },
  ),
)
