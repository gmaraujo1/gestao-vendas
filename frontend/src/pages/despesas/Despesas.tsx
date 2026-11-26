import { useQuery } from '@tanstack/react-query'
import { Car, Plus, Receipt, Wallet } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { Filtros } from '@/components/lista/BarraLista'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { FiltroPeriodo } from '@/components/lista/FiltroPeriodo'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Cartao, Pagina } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { useSessao } from '@/hooks/use-sessao'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { intervaloDoAtalho, rotuloDia, type AtalhoPeriodo } from '@/lib/datas'
import { formatarData, formatarMoeda } from '@/lib/formatadores'
import { TIPOS_DESPESA, opcoes, type TipoDespesa } from '@/lib/rotulos'
import { ICONES_DESPESA } from '@/lib/icones'
import { colunasDe } from '@/lib/tabela'
import { FormDespesa } from '@/pages/despesas/FormDespesa'

export type Despesa = components['schemas']['DespesaResponseDTO']

const h = colunasDe<Despesa>()
const COLUNAS = [
  h.accessor('data', { header: 'Data', cell: (c) => <span className="numeros">{formatarData(c.getValue())}</span> }),
  h.accessor('tipo', { header: 'Tipo', cell: (c) => <span className="font-medium">{c.getValue() ? TIPOS_DESPESA[c.getValue()!] : ''}</span> }),
  h.accessor('descricao', { header: 'Descrição', cell: (c) => <span className="text-muted-foreground">{c.getValue()}</span> }),
  h.accessor('veiculoDescricao', { header: 'Veículo', cell: (c) => c.getValue() ?? '—' }),
  h.accessor('valor', { header: 'Valor', meta: { direita: true }, cell: (c) => <span className="font-semibold">{formatarMoeda(c.getValue())}</span> }),
]

export function Despesas() {
  const negocioId = useNegocioId()
  const { ehAdmin } = useSessao()
  const [params, setParams] = useSearchParams()
  const [periodo, setPeriodo] = useState<AtalhoPeriodo>('mes')
  const [tipo, setTipo] = useState<TipoDespesa | 'todos'>('todos')
  const [editando, setEditando] = useState<Despesa | null>(null)
  const nova = params.get('nova') === '1'
  const intervalo = intervaloDoAtalho(periodo)
  const filtros = { ...intervalo, tipo: tipo === 'todos' ? undefined : tipo }

  const resumo = useQuery({
    queryKey: ['despesas', negocioId, 'resumo', intervalo],
    queryFn: () => dados(api.GET('/api/despesa/resumo', { params: { header: doNegocio(negocioId), query: intervalo } })),
  })
  const lista = usePaginado(['despesas', negocioId, filtros], (page) =>
    dados(api.GET('/api/despesa', { params: { header: doNegocio(negocioId), query: { ...filtros, page, size: TAMANHO_PAGINA } } })),
  )
  const maior = Math.max(...(resumo.data?.porTipo?.map((t) => t.total ?? 0) ?? [0]), 1)

  return (
    <Pagina
      titulo="Despesas"
      subtitulo="Gasolina, pedágio, pneus, mecânica…"
      acoes={
        <Button onClick={() => setParams({ nova: '1' }, { replace: true })}>
          <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Nova despesa</span>
        </Button>
      }
    >
      <FiltroPeriodo valor={periodo} aoMudar={setPeriodo} />

      <Cartao className="overflow-hidden">
        <div className="faixa-marca h-1" aria-hidden="true" />
        <div className="p-5">
          <p className="text-sm text-muted-foreground">Total no período</p>
          {resumo.data ? (
            <p className="numeros text-3xl font-semibold tracking-tight">{formatarMoeda(resumo.data.total)}</p>
          ) : (
            <Skeleton className="mt-1 h-9 w-36" />
          )}
          {(resumo.data?.porTipo?.length ?? 0) > 0 && (
            <ul className="mt-4 space-y-2.5" aria-label="Por tipo">
              {resumo.data?.porTipo?.map((t) => (
                <li key={t.tipo} className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span>{t.tipo ? TIPOS_DESPESA[t.tipo] : ''}</span>
                    <span className="numeros font-medium">{formatarMoeda(t.total)}</span>
                  </div>
                  <div className="h-2 overflow-hidden rounded-full bg-muted">
                    <div className="h-full rounded-full bg-primary transition-[width] duration-500" style={{ width: `${((t.total ?? 0) / maior) * 100}%` }} />
                  </div>
                </li>
              ))}
            </ul>
          )}
          {(resumo.data?.porVeiculo?.filter((v) => v.veiculoId != null).length ?? 0) > 0 && (
            <div className="mt-4 flex flex-wrap gap-2">
              {resumo.data?.porVeiculo
                ?.filter((v) => v.veiculoId != null)
                .map((v) => (
                  <span key={v.veiculoId} className="inline-flex items-center gap-1.5 rounded-full bg-accent px-3 py-1 text-sm text-accent-foreground">
                    <Car className="size-3.5" aria-hidden="true" /> {v.veiculoDescricao}: <span className="numeros font-medium">{formatarMoeda(v.total)}</span>
                  </span>
                ))}
            </div>
          )}
        </div>
      </Cartao>

      <Filtros<TipoDespesa | 'todos'>
        rotulo="Tipo"
        valor={tipo}
        aoMudar={setTipo}
        opcoes={[{ valor: 'todos', rotulo: 'Todos os tipos' }, ...opcoes(TIPOS_DESPESA)]}
      />

      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={lista.itens.length === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={Wallet}
        tituloVazio="Nenhuma despesa no período"
      >
        <TabelaDados
          rotulo="Despesas"
          dados={lista.itens}
          colunas={COLUNAS}
          chave={(d) => d.id ?? 0}
          aoAbrir={ehAdmin ? setEditando : undefined}
          cartao={(d) => {
            const Icone = d.tipo ? ICONES_DESPESA[d.tipo] : Receipt
            return (
              <div className="flex items-center gap-3">
                <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground">
                  <Icone className="size-5" aria-hidden="true" />
                </span>
                <div className="min-w-0 flex-1">
                  <p className="truncate font-medium">{d.tipo ? TIPOS_DESPESA[d.tipo] : ''}{d.descricao ? ` · ${d.descricao}` : ''}</p>
                  <p className="truncate text-sm text-muted-foreground">
                    {rotuloDia(d.data)}
                    {d.veiculoDescricao ? ` · ${d.veiculoDescricao}` : ''}
                  </p>
                </div>
                <span className="numeros font-semibold">{formatarMoeda(d.valor)}</span>
              </div>
            )
          }}
        />
        <CarregarMais
          temMais={lista.hasNextPage}
          carregando={lista.isFetchingNextPage}
          aoCarregar={() => void lista.fetchNextPage()}
          exibidos={lista.itens.length}
          total={lista.total}
        />
      </EstadosLista>

      <FormDespesa
        despesa={nova ? 'nova' : editando}
        aoFechar={() => {
          setEditando(null)
          if (nova) setParams({}, { replace: true })
        }}
      />
    </Pagina>
  )
}
