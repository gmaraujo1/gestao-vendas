import { ArrowLeftRight, ChevronRight, LogOut } from 'lucide-react'
import { Link } from 'react-router'
import { GRUPOS_MENU, visiveis } from '@/components/layout/navegacao'
import { SeletorTema } from '@/components/layout/SeletorTema'
import { useMeusNegocios } from '@/hooks/use-negocios'
import { useSessao } from '@/hooks/use-sessao'
import { useSessaoStore } from '@/lib/sessao'
import { iniciais } from '@/lib/texto'

/** Menu completo no celular: todas as telas, aparência e conta. */
export function Mais() {
  const { login, ehAdmin } = useSessao()
  const { data: negocios } = useMeusNegocios()

  return (
    <div className="space-y-7 motion-safe:animate-in motion-safe:fade-in-0">
      <div className="flex items-center gap-4">
        <span className="grid size-14 place-items-center rounded-2xl bg-primary text-lg font-semibold text-primary-foreground">
          {iniciais(login?.nome)}
        </span>
        <div className="min-w-0">
          <h1 className="truncate text-xl font-semibold">{login?.nome}</h1>
          <p className="text-sm text-muted-foreground">{ehAdmin ? 'Administrador' : 'Funcionário'}</p>
        </div>
      </div>

      {GRUPOS_MENU.map((grupo) => {
        const itens = visiveis(grupo.itens, ehAdmin).filter((i) => i.rota !== '/')
        if (itens.length === 0) return null
        return (
          <section key={grupo.titulo} aria-label={grupo.titulo}>
            <h2 className="px-1 pb-2 text-xs font-medium tracking-wide text-muted-foreground uppercase">
              {grupo.titulo}
            </h2>
            <ul className="divide-y overflow-hidden rounded-2xl border bg-card">
              {itens.map(({ rota, rotulo, icone: Icone }) => (
                <li key={rota}>
                  <Link
                    to={rota}
                    className="flex h-14 items-center gap-3 px-4 outline-none hover:bg-accent/60 focus-visible:bg-accent active:bg-accent"
                  >
                    <span className="grid size-9 place-items-center rounded-xl bg-accent text-accent-foreground">
                      <Icone className="size-4.5" aria-hidden="true" />
                    </span>
                    <span className="flex-1 font-medium">{rotulo}</span>
                    <ChevronRight className="size-4 text-muted-foreground" aria-hidden="true" />
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        )
      })}

      <section aria-label="Aparência">
        <h2 className="px-1 pb-2 text-xs font-medium tracking-wide text-muted-foreground uppercase">Aparência</h2>
        <SeletorTema className="bg-card ring-1 ring-border" />
      </section>

      <section aria-label="Conta" className="overflow-hidden rounded-2xl border bg-card">
        {(negocios?.length ?? 0) > 1 && (
          <button
            type="button"
            onClick={() => useSessaoStore.getState().sairDoNegocio()}
            className="flex h-14 w-full items-center gap-3 border-b px-4 font-medium outline-none hover:bg-accent/60 focus-visible:bg-accent"
          >
            <ArrowLeftRight className="size-5 text-muted-foreground" aria-hidden="true" /> Trocar de negócio
          </button>
        )}
        <button
          type="button"
          onClick={() => useSessaoStore.getState().sair()}
          className="flex h-14 w-full items-center gap-3 px-4 font-medium text-destructive outline-none hover:bg-destructive/10 focus-visible:bg-destructive/10"
        >
          <LogOut className="size-5" aria-hidden="true" /> Sair
        </button>
      </section>
    </div>
  )
}
