import { useQuery } from '@tanstack/react-query'
import {
  ArrowLeftRight,
  ChevronRight,
  Package,
  PackageX,
  RefreshCw,
  ShoppingBasket,
  TrendingDown,
  TrendingUp,
  Users,
  Wallet,
  type LucideIcon,
} from 'lucide-react'
import { Link } from 'react-router'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useSessao } from '@/hooks/use-sessao'
import { api, dados, doNegocio } from '@/lib/api/client'
import { FUSO, formatarMoeda, saudacao } from '@/lib/formatadores'
import { cn } from '@/lib/utils'

function hojePorExtenso() {
  const texto = new Date().toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    timeZone: FUSO,
  })
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

export function Inicio() {
  const { login, ehAdmin, negocioId } = useSessao()
  const primeiroNome = login?.nome?.split(' ')[0]

  return (
    <div className="space-y-6 motion-safe:animate-in motion-safe:fade-in-0">
      <div>
        <p className="text-sm text-muted-foreground">{hojePorExtenso()}</p>
        <h1 className="mt-0.5 text-2xl font-semibold tracking-tight">
          {saudacao()}
          {primeiroNome ? `, ${primeiroNome}` : ''}
        </h1>
      </div>

      {ehAdmin && negocioId != null && <ResumoDoDia negocioId={negocioId} />}

      <section aria-labelledby="atalhos" className="space-y-3">
        <h2 id="atalhos" className="sr-only">
          Atalhos
        </h2>
        <Link
          to="/vendas/nova"
          className="group flex items-center gap-4 rounded-3xl bg-primary p-5 text-primary-foreground shadow-sm transition-transform outline-none focus-visible:ring-3 focus-visible:ring-ring/50 active:scale-[0.98]"
        >
          <span className="grid size-12 place-items-center rounded-2xl bg-primary-foreground/15">
            <ShoppingBasket className="size-6" aria-hidden="true" />
          </span>
          <span className="flex-1">
            <span className="block text-lg font-semibold">Nova venda</span>
            <span className="block text-sm opacity-80">Cliente, produtos e pronto</span>
          </span>
          <ChevronRight className="size-5 opacity-70 transition-transform group-hover:translate-x-0.5" aria-hidden="true" />
        </Link>

        <div className="grid grid-cols-3 gap-3">
          <Atalho to="/trocas" icone={ArrowLeftRight} rotulo="Troca" />
          <Atalho to="/perdas" icone={PackageX} rotulo="Perda" />
          <Atalho to="/despesas" icone={Wallet} rotulo="Despesa" />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Atalho to="/clientes" icone={Users} rotulo="Clientes" horizontal />
          <Atalho to="/estoque" icone={Package} rotulo="Estoque" horizontal />
        </div>
      </section>
    </div>
  )
}

function Atalho({
  to,
  icone: Icone,
  rotulo,
  horizontal,
}: {
  to: string
  icone: LucideIcon
  rotulo: string
  horizontal?: boolean
}) {
  return (
    <Link
      to={to}
      className={cn(
        'flex items-center gap-3 rounded-2xl border bg-card p-4 font-medium shadow-xs transition-all outline-none hover:border-primary/30 focus-visible:ring-3 focus-visible:ring-ring/50 active:scale-[0.97]',
        horizontal ? 'h-16' : 'h-24 flex-col justify-center gap-2 text-sm',
      )}
    >
      <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground">
        <Icone className="size-5" aria-hidden="true" />
      </span>
      {rotulo}
    </Link>
  )
}

/** Números de hoje e do mês (só ADMIN: a API de dashboard é dos donos). */
function ResumoDoDia({ negocioId }: { negocioId: number }) {
  const hoje = useQuery({
    queryKey: ['dashboard', negocioId, 'serie', 'DIA'],
    queryFn: () =>
      dados(api.GET('/api/dashboard/serie', { params: { header: doNegocio(negocioId), query: { periodo: 'DIA' } } })),
  })
  const mes = useQuery({
    queryKey: ['dashboard', negocioId, 'serie', 'MES'],
    queryFn: () =>
      dados(api.GET('/api/dashboard/serie', { params: { header: doNegocio(negocioId), query: { periodo: 'MES' } } })),
  })

  if (hoje.isError || mes.isError) {
    return (
      <section role="alert" className="flex items-center gap-3 rounded-3xl border bg-card p-4 text-sm">
        <span className="flex-1 text-muted-foreground">Não deu para carregar os números de hoje.</span>
        <Button
          variant="outline"
          onClick={() => {
            void hoje.refetch()
            void mes.refetch()
          }}
        >
          <RefreshCw aria-hidden="true" /> Tentar de novo
        </Button>
      </section>
    )
  }

  const totalHoje = hoje.data?.total
  const totalMes = mes.data?.total

  return (
    <section aria-labelledby="resumo-hoje" className="overflow-hidden rounded-3xl border bg-card shadow-xs">
      <div className="faixa-marca h-1" aria-hidden="true" />
      <div className="p-5">
        <div className="flex items-baseline justify-between">
          <h2 id="resumo-hoje" className="text-sm font-medium text-muted-foreground">
            Vendas de hoje
          </h2>
          {totalHoje ? (
            <span className="text-xs text-muted-foreground">
              {totalHoje.numeroVendas} {totalHoje.numeroVendas === 1 ? 'venda' : 'vendas'}
            </span>
          ) : null}
        </div>
        {totalHoje ? (
          <p className="numeros mt-1 text-4xl font-semibold tracking-tight">{formatarMoeda(totalHoje.vendas)}</p>
        ) : (
          <Skeleton className="mt-2 h-10 w-44" />
        )}

        <dl className="mt-5 grid grid-cols-2 gap-3">
          <Indicador rotulo="Lucro bruto hoje" valor={totalHoje?.lucroBruto} />
          <Indicador rotulo="Resultado do mês" valor={totalMes?.resultado} destacarSinal />
        </dl>
      </div>
    </section>
  )
}

function Indicador({ rotulo, valor, destacarSinal }: { rotulo: string; valor?: number; destacarSinal?: boolean }) {
  const sinal = Math.sign(valor ?? 0)
  const Seta = sinal < 0 ? TrendingDown : TrendingUp
  return (
    <div className="rounded-2xl bg-muted/70 p-3">
      <dt className="text-xs text-muted-foreground">{rotulo}</dt>
      <dd className="mt-1">
        {valor == null ? (
          <Skeleton className="h-6 w-24" />
        ) : (
          <span
            className={cn(
              'numeros flex items-center gap-1 text-lg font-semibold',
              destacarSinal && sinal < 0 && 'text-destructive',
              destacarSinal && sinal > 0 && 'text-sucesso',
            )}
          >
            {destacarSinal && sinal !== 0 && <Seta className="size-4" aria-hidden="true" />}
            {formatarMoeda(valor)}
          </span>
        )}
      </dd>
    </div>
  )
}
