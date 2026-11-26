import { RefreshCw } from 'lucide-react'
import { useEffect } from 'react'
import { Navigate, Outlet } from 'react-router'
import { EstadoVazio } from '@/components/estados/EstadoVazio'
import { SemPermissao } from '@/components/estados/SemPermissao'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useNegocioAtual } from '@/hooks/use-negocios'
import { useSessao } from '@/hooks/use-sessao'
import { marcaDe } from '@/lib/marca'
import { useSessaoStore } from '@/lib/sessao'

/**
 * Sem login, vai para /login. Sair (botão, 401 ou outra aba) só limpa a sessão:
 * quem leva ao login são estes guardas.
 */
export function ExigeLogin() {
  const { login } = useSessao()
  if (!login) return <Navigate to="/login" replace />
  return <Outlet />
}

/**
 * Só abre as telas depois de confirmar que o negócio salvo ainda é do usuário: assim
 * nenhuma tela chama a API com um X-Negocio-Id sem acesso.
 */
export function ExigeNegocio() {
  const { negocioId } = useSessao()
  const { data: negocios, negocio, isPending, isError, refetch } = useNegocioAtual()
  const semAcesso = negocioId != null && negocios != null && negocio == null
  const slugCerto = marcaDe(negocio?.codigo)?.slug ?? null

  useEffect(() => {
    const sessao = useSessaoStore.getState()
    if (semAcesso) sessao.sairDoNegocio()
    else if (negocio && sessao.marca !== slugCerto) sessao.definirMarca(slugCerto)
  }, [semAcesso, negocio, slugCerto])

  if (negocioId == null || semAcesso) return <Navigate to="/negocio" replace />
  if (isPending) return <CarregandoApp />
  if (isError) {
    return (
      <main className="grid min-h-svh place-items-center">
        <EstadoVazio
          icone={RefreshCw}
          titulo="Sem conexão com o servidor"
          descricao="Confira a internet e tente de novo."
          acao={
            <Button size="lg" onClick={() => void refetch()}>
              Tentar de novo
            </Button>
          }
        />
      </main>
    )
  }
  return <Outlet />
}

/** Telas só do ADMIN: o FUNCIONARIO vê uma explicação em vez de um erro. */
export function ExigeAdmin() {
  const { ehAdmin } = useSessao()
  return ehAdmin ? <Outlet /> : <SemPermissao />
}

function CarregandoApp() {
  return (
    <div role="status" aria-busy="true" className="mx-auto max-w-3xl space-y-4 px-4 pt-6">
      <span className="sr-only">Carregando…</span>
      <Skeleton className="h-10 w-48" />
      <Skeleton className="h-44 rounded-3xl" />
      <Skeleton className="h-24 rounded-3xl" />
    </div>
  )
}
