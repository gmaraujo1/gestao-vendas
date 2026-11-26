import { ChevronRight, LogOut, RefreshCw, Store } from 'lucide-react'
import { useEffect } from 'react'
import { useNavigate } from 'react-router'
import { EstadoVazio } from '@/components/estados/EstadoVazio'
import { LogoNegocio } from '@/components/marca/LogoNegocio'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useMeusNegocios } from '@/hooks/use-negocios'
import { useSessao } from '@/hooks/use-sessao'
import { marcaDe } from '@/lib/marca'
import { saudacao } from '@/lib/formatadores'
import { useSessaoStore } from '@/lib/sessao'

/** Depois do login: em qual negócio trabalhar. Com um negócio só, entra direto. */
export function EscolherNegocio() {
  const { login } = useSessao()
  const { data: negocios, isPending, isError, refetch } = useMeusNegocios()
  const navigate = useNavigate()

  function abrir(id: number, codigo?: string) {
    const sessao = useSessaoStore.getState()
    sessao.escolherNegocio(id, marcaDe(codigo)?.slug ?? null)
    // depois de uma sessão expirada, volta para a tela em que estava
    void navigate(sessao.consumirVoltarPara() ?? '/', { replace: true })
  }

  // com um negócio só (o funcionário, por exemplo), não há o que escolher
  const unico = negocios?.length === 1 ? negocios[0] : undefined
  useEffect(() => {
    if (unico?.id == null) return
    const sessao = useSessaoStore.getState()
    sessao.escolherNegocio(unico.id, marcaDe(unico.codigo)?.slug ?? null)
    void navigate(sessao.consumirVoltarPara() ?? '/', { replace: true })
  }, [unico, navigate])

  const primeiroNome = login?.nome?.split(' ')[0]

  return (
    <main className="pt-seguro pb-seguro flex min-h-svh flex-col px-5">
      <div className="mx-auto flex w-full max-w-sm flex-1 flex-col justify-center py-10">
        <div className="mb-8 motion-safe:animate-in motion-safe:fade-in-0 motion-safe:slide-in-from-bottom-2">
          <p className="text-muted-foreground">
            {saudacao()}
            {primeiroNome ? `, ${primeiroNome}` : ''}
          </p>
          <h1 className="mt-1 text-2xl font-semibold tracking-tight text-balance">
            Em qual negócio vamos trabalhar?
          </h1>
        </div>

        {isPending && (
          <div className="space-y-3" role="status" aria-busy="true">
            <span className="sr-only">Carregando negócios…</span>
            <Skeleton className="h-24 rounded-3xl" />
            <Skeleton className="h-24 rounded-3xl" />
          </div>
        )}

        {isError && (
          <EstadoVazio
            icone={RefreshCw}
            titulo="Não deu para carregar os negócios"
            descricao="Confira a internet e tente de novo."
            acao={
              <Button size="lg" onClick={() => void refetch()}>
                Tentar de novo
              </Button>
            }
          />
        )}

        {negocios?.length === 0 && (
          <EstadoVazio
            icone={Store}
            titulo="Nenhum negócio liberado"
            descricao="Sua conta ainda não tem acesso ao Laticínio nem ao Pão Nino. Peça a liberação a um administrador."
          />
        )}

        {negocios && negocios.length > 1 && (
          <ul className="space-y-3">
            {negocios.map((n, i) => {
              const marca = marcaDe(n.codigo)
              return (
                <li
                  key={n.id}
                  // cada cartão já mostra as cores do seu negócio
                  data-negocio={marca?.slug}
                  style={{ animationDelay: `${i * 70}ms` }}
                  className="motion-safe:animate-in motion-safe:fill-mode-both motion-safe:fade-in-0 motion-safe:slide-in-from-bottom-3"
                >
                  <button
                    type="button"
                    onClick={() => n.id != null && abrir(n.id, n.codigo)}
                    className="group flex w-full items-center gap-4 overflow-hidden rounded-3xl border bg-card p-4 text-left text-card-foreground shadow-sm transition-[background-color,border-color,color,box-shadow,transform,opacity] outline-none hover:border-primary/40 focus-visible:ring-3 focus-visible:ring-ring/50 active:scale-[0.98]"
                  >
                    <LogoNegocio marca={marca} tamanho="md" />
                    <span className="min-w-0 flex-1">
                      <span className="block text-lg font-semibold">{marca?.nomeCurto ?? n.nome}</span>
                      <span className="block truncate text-sm text-muted-foreground">{marca?.descricao}</span>
                    </span>
                    <ChevronRight
                      className="size-5 text-muted-foreground transition-transform group-hover:translate-x-0.5"
                      aria-hidden="true"
                    />
                  </button>
                </li>
              )
            })}
          </ul>
        )}

        <Button
          variant="ghost"
          className="mx-auto mt-10 text-muted-foreground"
          onClick={() => useSessaoStore.getState().sair()}
        >
          <LogOut aria-hidden="true" /> Sair
        </Button>
      </div>
    </main>
  )
}
