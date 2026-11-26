import { Plus, Receipt } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { Etiqueta } from '@/components/Etiqueta'
import { Filtros } from '@/components/lista/BarraLista'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { FiltroPeriodo } from '@/components/lista/FiltroPeriodo'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Pagina } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { useTelaLarga } from '@/hooks/use-midia'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { horaDe, intervaloDoAtalho, rotuloDia, type AtalhoPeriodo } from '@/lib/datas'
import { formatarDataHora, formatarMoeda } from '@/lib/formatadores'
import { colunasDe } from '@/lib/tabela'
import { cn } from '@/lib/utils'

type VendaResumo = components['schemas']['VendaResumoDTO']
type Status = 'todas' | 'REGISTRADA' | 'CANCELADA'

const h = colunasDe<VendaResumo>()
const COLUNAS = [
  h.accessor('id', { header: 'Venda', cell: (c) => <span className="numeros text-muted-foreground">#{c.getValue()}</span> }),
  h.accessor('dataHora', { header: 'Data', cell: (c) => <span className="numeros">{formatarDataHora(c.getValue())}</span> }),
  h.accessor('clienteNome', { header: 'Cliente', cell: (c) => <span className="font-medium">{c.getValue()}</span> }),
  h.accessor('status', {
    header: 'Situação',
    cell: (c) => (c.getValue() === 'CANCELADA' ? <Etiqueta tom="perigo">Cancelada</Etiqueta> : <Etiqueta tom="sucesso">Registrada</Etiqueta>),
  }),
  h.accessor('total', { header: 'Total', meta: { direita: true }, cell: (c) => <span className="font-semibold">{formatarMoeda(c.getValue())}</span> }),
]

export function Vendas() {
  const negocioId = useNegocioId()
  const navigate = useNavigate()
  const larga = useTelaLarga()
  const [periodo, setPeriodo] = useState<AtalhoPeriodo>('7dias')
  const [status, setStatus] = useState<Status>('todas')
  const filtros = { ...intervaloDoAtalho(periodo), status: status === 'todas' ? undefined : status }
  const lista = usePaginado(['vendas', negocioId, filtros], (page) =>
    dados(api.GET('/api/venda', { params: { header: doNegocio(negocioId), query: { ...filtros, page, size: TAMANHO_PAGINA } } })),
  )

  // no celular, agrupa por dia ("Hoje", "Ontem", 24/11/2026...)
  const dias = new Map<string, VendaResumo[]>()
  for (const v of lista.itens) {
    const dia = v.dataHora?.slice(0, 10) ?? ''
    dias.set(dia, [...(dias.get(dia) ?? []), v])
  }

  return (
    <Pagina
      titulo="Vendas"
      subtitulo={lista.isSuccess ? `${lista.total} ${lista.total === 1 ? 'venda' : 'vendas'}` : undefined}
      acoes={
        <Button asChild>
          <Link to="/vendas/nova">
            <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Nova venda</span>
          </Link>
        </Button>
      }
    >
      <div className="space-y-2">
        <FiltroPeriodo valor={periodo} aoMudar={setPeriodo} />
        <Filtros<Status>
          rotulo="Situação"
          valor={status}
          aoMudar={setStatus}
          opcoes={[
            { valor: 'todas', rotulo: 'Todas' },
            { valor: 'REGISTRADA', rotulo: 'Registradas' },
            { valor: 'CANCELADA', rotulo: 'Canceladas' },
          ]}
        />
      </div>
      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={lista.itens.length === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={Receipt}
        tituloVazio="Nenhuma venda no período"
        acaoVazio={
          <Button asChild size="lg">
            <Link to="/vendas/nova">
              <Plus aria-hidden="true" /> Nova venda
            </Link>
          </Button>
        }
      >
        {larga ? (
          <TabelaDados rotulo="Vendas" dados={lista.itens} colunas={COLUNAS} chave={(v) => v.id ?? 0} aoAbrir={(v) => void navigate(`/vendas/${v.id}`)} cartao={() => null} />
        ) : (
          <div className="space-y-5">
            {[...dias.entries()].map(([dia, vendas]) => (
              <section key={dia} aria-label={rotuloDia(dia)} className="space-y-2">
                <h2 className="px-1 text-sm font-medium text-muted-foreground">{rotuloDia(dia)}</h2>
                <ul className="divide-y overflow-hidden rounded-2xl border bg-card shadow-xs">
                  {vendas.map((v) => (
                    <li key={v.id}>
                      <Link
                        to={`/vendas/${v.id}`}
                        className="flex items-center gap-3 px-4 py-3.5 transition-colors outline-none hover:bg-accent/50 focus-visible:bg-accent"
                      >
                        <div className="min-w-0 flex-1">
                          <p className="truncate font-medium">{v.clienteNome}</p>
                          <p className="text-sm text-muted-foreground">
                            {horaDe(v.dataHora)} · #{v.id}
                          </p>
                        </div>
                        {v.status === 'CANCELADA' && <Etiqueta tom="perigo">Cancelada</Etiqueta>}
                        <span className={cn('numeros font-semibold', v.status === 'CANCELADA' && 'text-muted-foreground line-through')}>
                          {formatarMoeda(v.total)}
                        </span>
                      </Link>
                    </li>
                  ))}
                </ul>
              </section>
            ))}
          </div>
        )}
        <CarregarMais
          temMais={lista.hasNextPage}
          carregando={lista.isFetchingNextPage}
          aoCarregar={() => void lista.fetchNextPage()}
          exibidos={lista.itens.length}
          total={lista.total}
        />
      </EstadosLista>
    </Pagina>
  )
}
