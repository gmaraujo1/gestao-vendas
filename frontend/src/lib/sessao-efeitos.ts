import { useCarrinho } from '@/lib/carrinho'
import { queryClient } from '@/lib/query-client'
import { aplicarMarca } from '@/lib/marca'
import { CHAVE_SESSAO, useSessaoStore } from '@/lib/sessao'
import { CHAVE_TEMA, useTemaStore } from '@/lib/tema'

/**
 * Reações às mudanças de sessão, venham de onde vierem (botão, 401 ou outra aba):
 *  - outro usuário (ou saiu): apaga todo o cache das telas;
 *  - outro negócio: apaga o cache, menos a lista de negócios (a tela de escolha usa);
 *  - cor do negócio: pinta o <html>.
 * Chamado uma vez no main.tsx.
 */
export function iniciarEfeitosDaSessao() {
  aplicarMarca(useSessaoStore.getState().marca)
  conferirDonoDoCarrinho(useSessaoStore.getState().login?.usuarioId)

  useSessaoStore.subscribe((agora, antes) => {
    if (agora.marca !== antes.marca) aplicarMarca(agora.marca)
    conferirDonoDoCarrinho(agora.login?.usuarioId)
    if (agora.login?.usuarioId !== antes.login?.usuarioId) {
      void limparCache(() => true)

    } else if (agora.negocioId !== antes.negocioId) {
      void limparCache((chave) => chave[0] !== 'negocios')
    }
  })

  // o que outra aba gravou: recarrega o store (e as reações acima rodam)
  window.addEventListener('storage', (e) => {
    if (e.key === CHAVE_SESSAO || e.key === null) void useSessaoStore.persist.rehydrate()
    if (e.key === CHAVE_TEMA || e.key === null) void useTemaStore.persist.rehydrate()
  })
}

/**
 * O rascunho da venda é de quem o começou. Se outra pessoa entrar no aparelho, ele é
 * descartado; se a mesma pessoa entrar de novo (sessão expirada), continua lá.
 */
function conferirDonoDoCarrinho(usuarioId?: number) {
  if (usuarioId == null) return
  const { donoId } = useCarrinho.getState()
  if (donoId !== usuarioId) useCarrinho.setState({ donoId: usuarioId, porNegocio: {} })
}

async function limparCache(remover: (chave: readonly unknown[]) => boolean) {
  // cancela antes: uma resposta atrasada não volta a encher o cache com dados do anterior
  await queryClient.cancelQueries({ predicate: (q) => remover(q.queryKey) })
  queryClient.removeQueries({ predicate: (q) => remover(q.queryKey) })
}
