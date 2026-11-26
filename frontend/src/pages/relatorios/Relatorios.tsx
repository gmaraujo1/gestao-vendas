import { useQuery } from '@tanstack/react-query'
import { BarChart3, FileDown, Loader2, PackageX, TrendingDown, TrendingUp } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { toast } from 'sonner'
import { Filtros } from '@/components/lista/BarraLista'
import { EstadosLista } from '@/components/lista/EstadosLista'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Cartao, Pagina, Secao } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio } from '@/lib/api/client'
import { baixarArquivo } from '@/lib/api/arquivo'
import { mensagemDeErro } from '@/lib/api/erros'
import type { components } from '@/lib/api/schema'
import { formatarMoeda, formatarQuantidade, hojeIso } from '@/lib/formatadores'
import { MOTIVOS_PERDA, PERIODOS, type TipoPeriodo } from '@/lib/rotulos'
import { colunasDe } from '@/lib/tabela'
import { cn } from '@/lib/utils'
import { GraficoDespesas, GraficoSerie } from '@/pages/relatorios/Graficos'
import { SeletorPeriodo, type Filtro } from '@/pages/relatorios/SeletorPeriodo'

type ProdutoVendido = components['schemas']['ProdutoVendidoDTO']
type Criterio = 'VENDAS' | 'QUANTIDADE' | 'LUCRO'

const h = colunasDe<ProdutoVendido>()
const COLUNAS = [
  h.accessor('descricao', { header: 'Produto', cell: (c) => <span className="font-medium">{c.getValue()}</span> }),
  h.accessor('quantidade', { header: 'Qtd.', meta: { direita: true }, cell: (c) => `${formatarQuantidade(c.getValue())} ${c.row.original.unidadeMedida}` }),
  h.accessor('vendas', { header: 'Vendas', meta: { direita: true }, cell: (c) => formatarMoeda(c.getValue()) }),
  h.accessor('cmv', { header: 'Custo', meta: { direita: true }, cell: (c) => <span className="text-muted-foreground">{formatarMoeda(c.getValue())}</span> }),
  h.accessor('lucroBruto', {
    header: 'Lucro bruto',
    meta: { direita: true },
    cell: (c) => <span className={cn('font-semibold', (c.getValue() ?? 0) < 0 && 'text-destructive')}>{formatarMoeda(c.getValue())}</span>,
  }),
]

const dataValida = (v: string | null) => (v && /^\d{4}-\d{2}-\d{2}$/.test(v) && !Number.isNaN(Date.parse(v)) ? v : hojeIso())

/** O filtro fica na URL (dá para favoritar ou mandar o link); valores estranhos viram o padrão. */
function lerFiltro(p: URLSearchParams): Filtro {
  const periodo = p.get('periodo')
  if (periodo === 'LIVRE') {
    const de = dataValida(p.get('de'))
    const ate = dataValida(p.get('ate'))
    return { periodo, de: de <= ate ? de : ate, ate }
  }
  return { periodo: periodo && periodo in PERIODOS ? (periodo as TipoPeriodo) : 'MES', data: dataValida(p.get('data')) }
}

/** Lucro e prejuízo do período, gráficos e PDF. Só os donos (ADMIN). */
export function Relatorios() {
  const negocioId = useNegocioId()
  const [params, setParams] = useSearchParams()
  const filtro = lerFiltro(params)
  const [criterio, setCriterio] = useState<Criterio>('VENDAS')
  const [baixando, setBaixando] = useState(false)
  const query = filtro.periodo === 'LIVRE' ? { de: filtro.de, ate: filtro.ate } : { periodo: filtro.periodo, data: filtro.data }

  const relatorio = useQuery({
    queryKey: ['dashboard', negocioId, 'lucro', query],
    queryFn: () => dados(api.GET('/api/relatorio/lucro', { params: { header: doNegocio(negocioId), query } })),
    placeholderData: (anterior) => anterior,
  })
  const ranking = useQuery({
    queryKey: ['dashboard', negocioId, 'ranking', query, criterio],
    queryFn: () =>
      dados(api.GET('/api/dashboard/produtos-mais-vendidos', { params: { header: doNegocio(negocioId), query: { ...query, criterio, limite: 10 } } })),
    placeholderData: (anterior) => anterior,
  })

  function mudarFiltro(f: Filtro) {
    setParams(f.periodo === 'LIVRE' ? { periodo: 'LIVRE', de: f.de, ate: f.ate } : { periodo: f.periodo, data: f.data }, { replace: true })
  }

  async function baixarPdf() {
    setBaixando(true)
    try {
      await baixarArquivo('/api/relatorio/lucro/pdf', query)
    } catch (e) {
      toast.error(mensagemDeErro(e))
    } finally {
      setBaixando(false)
    }
  }

  const r = relatorio.data?.resultado
  const resultado = r?.resultado ?? 0
  const margem = r?.vendas ? ((r.lucroBruto ?? 0) / r.vendas) * 100 : null
  const atualizando = relatorio.isFetching && !relatorio.isPending

  return (
    <Pagina
      titulo="Relatórios"
      subtitulo="Lucro e prejuízo: vendas − custo dos produtos − despesas."
      acoes={
        <Button variant="outline" onClick={() => void baixarPdf()} disabled={baixando || !relatorio.data}>
          {baixando ? <Loader2 className="animate-spin" aria-hidden="true" /> : <FileDown aria-hidden="true" />}
          PDF
        </Button>
      }
    >
      <SeletorPeriodo
        filtro={filtro}
        aoMudar={mudarFiltro}
        descricao={relatorio.data?.descricaoPeriodo}
        de={relatorio.data?.de}
        ate={relatorio.data?.ate}
      />

      <EstadosLista
        carregando={relatorio.isPending}
        erro={relatorio.error}
        vazio={false}
        tentarDeNovo={() => void relatorio.refetch()}
        iconeVazio={BarChart3}
        tituloVazio=""
      >
        <div className={cn('space-y-6 transition-opacity', atualizando && 'opacity-60')} aria-busy={atualizando}>
          <Cartao className="overflow-hidden">
            <div className="faixa-marca h-1" aria-hidden="true" />
            <div className="grid gap-5 p-5 md:grid-cols-[1.2fr_1fr]">
              <div>
                <p className="text-sm text-muted-foreground">{resultado < 0 ? 'Prejuízo do período' : 'Lucro do período'}</p>
                <p className={cn('numeros flex items-center gap-2 text-4xl font-semibold tracking-tight', resultado < 0 ? 'text-destructive' : resultado > 0 && 'text-sucesso')}>
                  {resultado !== 0 && (resultado < 0 ? <TrendingDown className="size-7" aria-hidden="true" /> : <TrendingUp className="size-7" aria-hidden="true" />)}
                  {formatarMoeda(resultado)}
                </p>
                <p className="mt-1 text-sm text-muted-foreground">
                  Considerando as perdas: <span className="numeros font-medium text-foreground">{formatarMoeda(r?.resultadoComPerdas)}</span>
                </p>
              </div>
              <dl className="grid grid-cols-2 gap-2.5">
                <Indicador rotulo={`Vendas (${r?.numeroVendas ?? 0})`} valor={r?.vendas} />
                <Indicador rotulo="Custo dos produtos" valor={r?.cmv} />
                <Indicador rotulo={margem != null ? `Lucro bruto · ${Math.round(margem)}%` : 'Lucro bruto'} valor={r?.lucroBruto} />
                <Indicador rotulo="Despesas" valor={r?.despesas} />
              </dl>
            </div>
          </Cartao>

          <Secao titulo={`Vendas e resultado${relatorio.data?.agrupamento ? ` por ${PERIODOS[relatorio.data.agrupamento].toLowerCase()}` : ''}`}>
            <Cartao className="p-4">
              {(r?.numeroVendas ?? 0) === 0 && (r?.despesas ?? 0) === 0 ? (
                <p className="py-12 text-center text-sm text-muted-foreground">Nenhuma venda nem despesa no período.</p>
              ) : (
                <GraficoSerie pontos={relatorio.data?.detalhamento ?? []} agrupamento={relatorio.data?.agrupamento} />
              )}
            </Cartao>
          </Secao>

          <div className="grid gap-6 lg:grid-cols-2">
            <Secao titulo="Despesas por tipo">
              <Cartao className="p-4">
                {(relatorio.data?.despesasPorTipo?.length ?? 0) === 0 ? (
                  <p className="py-8 text-center text-sm text-muted-foreground">Nenhuma despesa no período.</p>
                ) : (
                  <GraficoDespesas porTipo={relatorio.data?.despesasPorTipo ?? []} />
                )}
              </Cartao>
            </Secao>
            <Secao titulo={`Perdas · ${formatarMoeda(r?.perdas)}`}>
              <Cartao className="p-4">
                {(relatorio.data?.perdasPorMotivo?.length ?? 0) === 0 ? (
                  <p className="flex flex-col items-center gap-2 py-8 text-center text-sm text-muted-foreground">
                    <PackageX className="size-6" aria-hidden="true" /> Nenhuma perda no período.
                  </p>
                ) : (
                  <ul className="space-y-3">
                    {relatorio.data?.perdasPorMotivo?.map((p) => (
                      <li key={p.motivo} className="space-y-1">
                        <div className="flex justify-between text-sm">
                          <span>
                            {p.motivo ? MOTIVOS_PERDA[p.motivo] : ''} <span className="text-muted-foreground">({p.registros})</span>
                          </span>
                          <span className="numeros font-medium">{formatarMoeda(p.custo)}</span>
                        </div>
                        <div className="h-2 overflow-hidden rounded-full bg-muted">
                          <div className="h-full rounded-full bg-destructive/70" style={{ width: `${((p.custo ?? 0) / (r?.perdas || 1)) * 100}%` }} />
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </Cartao>
            </Secao>
          </div>

          <Secao titulo="Produtos que mais venderam">
            <Filtros<Criterio>
              rotulo="Ordenar por"
              valor={criterio}
              aoMudar={setCriterio}
              opcoes={[
                { valor: 'VENDAS', rotulo: 'Valor vendido' },
                { valor: 'QUANTIDADE', rotulo: 'Quantidade' },
                { valor: 'LUCRO', rotulo: 'Lucro' },
              ]}
            />
            {ranking.isPending ? (
              <Skeleton className="h-48 rounded-2xl" />
            ) : (ranking.data?.length ?? 0) === 0 ? (
              <Cartao className="p-8 text-center text-sm text-muted-foreground">Nenhuma venda no período.</Cartao>
            ) : (
              <TabelaDados
                rotulo="Produtos que mais venderam"
                dados={ranking.data ?? []}
                colunas={COLUNAS}
                chave={(p) => p.produtoId ?? 0}
                cartao={(p) => (
                  <div className="flex items-center gap-3">
                    <div className="min-w-0 flex-1">
                      <p className="font-medium text-pretty">{p.descricao}</p>
                      <p className="numeros text-sm text-muted-foreground">
                        {formatarQuantidade(p.quantidade)} {p.unidadeMedida} · custo {formatarMoeda(p.cmv)}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="numeros font-semibold">{formatarMoeda(p.vendas)}</p>
                      <p className={cn('numeros text-sm', (p.lucroBruto ?? 0) < 0 ? 'text-destructive' : 'text-sucesso')}>
                        lucro {formatarMoeda(p.lucroBruto)}
                      </p>
                    </div>
                  </div>
                )}
              />
            )}
          </Secao>

          <p className="text-center text-xs text-pretty text-muted-foreground">
            Vendas canceladas não entram. O custo de cada venda é o custo médio do produto no momento da venda.
          </p>
        </div>
      </EstadosLista>
    </Pagina>
  )
}

function Indicador({ rotulo, valor }: { rotulo: string; valor?: number }) {
  return (
    <div className="rounded-xl bg-muted/70 p-3">
      <dt className="truncate text-xs text-muted-foreground">{rotulo}</dt>
      <dd className="numeros mt-0.5 font-semibold">{valor == null ? <Skeleton className="h-5 w-20" /> : formatarMoeda(valor)}</dd>
    </div>
  )
}
