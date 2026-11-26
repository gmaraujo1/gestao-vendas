import { useEffect, useRef } from 'react'
import { NavLink, Outlet, useLocation, useMatches, useNavigation } from 'react-router'
import { LogoNegocio } from '@/components/marca/LogoNegocio'
import { MenuUsuario } from '@/components/layout/MenuUsuario'
import { BARRA_INFERIOR, GRUPOS_MENU, NOVA_VENDA, visiveis } from '@/components/layout/navegacao'
import { useNegocioAtual } from '@/hooks/use-negocios'
import { useSessao } from '@/hooks/use-sessao'
import { cn } from '@/lib/utils'

/** Título de cada rota (handle no router.tsx): vai para a aba do navegador e o leitor de tela. */
export type HandleRota = {
  titulo?: string
  /** Telas de lançamento (venda, troca): a barra de ação delas fica no lugar da barra inferior. */
  semBarra?: boolean
}

/**
 * Casca do app depois de escolher o negócio. No celular: cabeçalho fino e barra inferior com
 * a "Nova venda" no meio. No computador (md+): menu lateral. As cores seguem o negócio ativo.
 */
export function AppLayout() {
  const { marca, negocio } = useNegocioAtual()
  const { ehAdmin } = useSessao()
  const principal = useTituloEFoco(marca?.nomeCurto ?? negocio?.nome)
  const semBarra = useMatches().some((m) => (m.handle as HandleRota | undefined)?.semBarra)
  const carregando = useNavigation().state === 'loading'

  return (
    <div className="min-h-svh md:grid md:grid-cols-[16rem_1fr]">
      <div
        aria-hidden="true"
        className={cn(
          'faixa-marca fixed inset-x-0 top-0 z-50 h-0.5 origin-left transition-[transform,opacity] duration-500',
          carregando ? 'scale-x-75 opacity-100' : 'scale-x-100 opacity-0',
        )}
      />
      <a
        href="#conteudo"
        className="sr-only z-50 rounded-lg bg-primary px-4 py-2 text-primary-foreground focus:not-sr-only focus:fixed focus:top-2 focus:left-2"
      >
        Pular para o conteúdo
      </a>
      <aside className="sticky top-0 hidden h-svh flex-col border-r bg-sidebar md:flex">
        <div className="faixa-marca h-1" />
        <div className="flex items-center gap-3 px-5 py-5">
          <LogoNegocio marca={marca} tamanho="sm" />
          <div className="min-w-0">
            <p className="truncate font-semibold">{marca?.nomeCurto ?? negocio?.nome}</p>
            <p className="truncate text-xs text-muted-foreground">{marca?.descricao}</p>
          </div>
        </div>
        <nav aria-label="Menu" className="flex-1 space-y-6 overflow-y-auto px-3 pb-6">
          <NavLink
            to={NOVA_VENDA.rota}
            className="flex h-11 items-center justify-center gap-2 rounded-xl bg-primary font-medium text-primary-foreground shadow-xs transition-colors hover:bg-primary/90"
          >
            <NOVA_VENDA.icone className="size-5" aria-hidden="true" /> {NOVA_VENDA.rotulo}
          </NavLink>
          {GRUPOS_MENU.map((grupo) => {
            const itens = visiveis(grupo.itens, ehAdmin)
            if (itens.length === 0) return null
            return (
              <div key={grupo.titulo}>
                <p className="px-3 pb-1.5 text-xs font-medium tracking-wide text-muted-foreground uppercase">
                  {grupo.titulo}
                </p>
                {itens.map(({ rota, rotulo, icone: Icone }) => (
                  <NavLink
                    key={rota}
                    to={rota}
                    end={rotaExata(rota)}
                    className={({ isActive }) =>
                      cn(
                        'flex h-10 items-center gap-3 rounded-lg px-3 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                        isActive && 'bg-accent font-medium text-accent-foreground',
                      )
                    }
                  >
                    <Icone className="size-4.5" aria-hidden="true" /> {rotulo}
                  </NavLink>
                ))}
              </div>
            )
          })}
        </nav>
      </aside>

      <div className="flex min-w-0 flex-col">
        <header className="pt-seguro sticky top-0 z-30 border-b bg-background/85 backdrop-blur-lg">
          <div className="faixa-marca h-1 md:hidden" />
          <div className="mx-auto flex h-16 max-w-5xl items-center gap-3 px-4 md:px-8">
            <div className="flex min-w-0 flex-1 items-center gap-3 md:hidden">
              <LogoNegocio marca={marca} tamanho="sm" />
              <span className="truncate font-semibold">{marca?.nomeCurto ?? negocio?.nome}</span>
            </div>
            <div className="hidden flex-1 md:block" />
            <MenuUsuario />
          </div>
        </header>

        <main
          id="conteudo"
          ref={principal}
          tabIndex={-1}
          className={cn('mx-auto w-full max-w-5xl flex-1 px-4 pt-5 outline-none md:px-8 md:pb-10', semBarra ? 'pb-6' : 'pb-32')}
        >
          <Outlet />
        </main>
      </div>

      {!semBarra && <BarraInferior />}
    </div>
  )
}

/** "Vendas" não acende em "Nova venda", que tem o próprio botão. */
function rotaExata(rota: string) {
  return rota === '/' || rota === '/vendas'
}

/**
 * Numa SPA, trocar de tela não avisa o leitor de tela nem move o foco: aqui o título da aba
 * muda e o foco vai para o conteúdo (menos na primeira tela, para não pular o cabeçalho).
 */
function useTituloEFoco(negocio?: string) {
  const { pathname } = useLocation()
  const matches = useMatches()
  const principal = useRef<HTMLElement>(null)
  const primeira = useRef(true)
  const titulo = [...matches].reverse().map((m) => (m.handle as HandleRota | undefined)?.titulo).find(Boolean)

  useEffect(() => {
    document.title = [titulo, negocio, 'Gestão Vendas'].filter(Boolean).join(' · ')
  }, [titulo, negocio])

  useEffect(() => {
    if (primeira.current) {
      primeira.current = false
      return
    }
    principal.current?.focus({ preventScroll: true })
    window.scrollTo({ top: 0 })
  }, [pathname])

  return principal
}

function BarraInferior() {
  const [inicio, vendas, estoque, mais] = BARRA_INFERIOR
  return (
    <nav
      aria-label="Menu principal"
      className="pb-seguro fixed inset-x-0 bottom-0 z-30 border-t bg-card/90 backdrop-blur-lg md:hidden"
    >
      <div className="mx-auto grid h-16 max-w-md grid-cols-5 items-center px-2">
        {inicio && <ItemBarra {...inicio} />}
        {vendas && <ItemBarra {...vendas} />}
        <div className="flex justify-center">
          <NavLink
            to={NOVA_VENDA.rota}
            aria-label={NOVA_VENDA.rotulo}
            className="-mt-7 grid size-15 place-items-center rounded-2xl bg-primary text-primary-foreground shadow-lg ring-4 shadow-primary/30 ring-background transition-transform active:scale-95"
          >
            <NOVA_VENDA.icone className="size-7" aria-hidden="true" />
          </NavLink>
        </div>
        {estoque && <ItemBarra {...estoque} />}
        {mais && <ItemBarra {...mais} />}
      </div>
    </nav>
  )
}

function ItemBarra({ rota, rotulo, icone: Icone }: (typeof BARRA_INFERIOR)[number]) {
  return (
    <NavLink
      to={rota}
      end={rotaExata(rota)}
      className={({ isActive }) =>
        cn(
          'flex h-14 flex-col items-center justify-center gap-1 rounded-xl text-[11px] font-medium text-muted-foreground transition-colors',
          isActive && 'text-primary',
        )
      }
    >
      {({ isActive }) => (
        <>
          <span
            className={cn(
              'grid h-7 w-12 place-items-center rounded-full transition-colors',
              isActive && 'bg-accent',
            )}
          >
            <Icone className="size-5" aria-hidden="true" strokeWidth={isActive ? 2.3 : 1.9} />
          </span>
          {rotulo}
        </>
      )}
    </NavLink>
  )
}
