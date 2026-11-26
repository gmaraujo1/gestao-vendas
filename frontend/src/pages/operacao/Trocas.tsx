import { ArrowLeftRight, Plus } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { FiltroPeriodo } from '@/components/lista/FiltroPeriodo'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Pagina } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { horaDe, intervaloDoAtalho, rotuloDia, type AtalhoPeriodo } from '@/lib/datas'
import { formatarDataHora } from '@/lib/formatadores'
import { colunasDe } from '@/lib/tabela'

type TrocaResumo = components['schemas']['TrocaResumoDTO']

const h = colunasDe<TrocaResumo>()
const COLUNAS = [
  h.accessor('id', { header: 'Troca', cell: (c) => <span className="numeros text-muted-foreground">#{c.getValue()}</span> }),
  h.accessor('dataHora', { header: 'Data', cell: (c) => <span className="numeros">{formatarDataHora(c.getValue())}</span> }),
  h.accessor('clienteNome', { header: 'Cliente', cell: (c) => <span className="font-medium">{c.getValue()}</span> }),
  h.accessor('observacao', { header: 'Observação', cell: (c) => <span className="text-muted-foreground">{c.getValue()}</span> }),
]

export function Trocas() {
  const negocioId = useNegocioId()
  const navigate = useNavigate()
  const [periodo, setPeriodo] = useState<AtalhoPeriodo>('mes')
  const filtros = intervaloDoAtalho(periodo)
  const lista = usePaginado(['trocas', negocioId, filtros], (page) =>
    dados(api.GET('/api/troca', { params: { header: doNegocio(negocioId), query: { ...filtros, page, size: TAMANHO_PAGINA } } })),
  )
  return (
    <Pagina
      titulo="Trocas"
      subtitulo="O cliente devolve um produto e leva outro."
      acoes={
        <Button asChild>
          <Link to="/trocas/nova">
            <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Nova troca</span>
          </Link>
        </Button>
      }
    >
      <FiltroPeriodo valor={periodo} aoMudar={setPeriodo} />
      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={lista.itens.length === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={ArrowLeftRight}
        tituloVazio="Nenhuma troca no período"
      >
        <TabelaDados
          rotulo="Trocas"
          dados={lista.itens}
          colunas={COLUNAS}
          chave={(t) => t.id ?? 0}
          aoAbrir={(t) => void navigate(`/trocas/${t.id}`)}
          cartao={(t) => (
            <div className="flex items-center gap-3">
              <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground">
                <ArrowLeftRight className="size-5" aria-hidden="true" />
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{t.clienteNome}</p>
                <p className="truncate text-sm text-muted-foreground">
                  {rotuloDia(t.dataHora)} {horaDe(t.dataHora)}
                  {t.observacao ? ` · ${t.observacao}` : ''}
                </p>
              </div>
            </div>
          )}
        />
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
