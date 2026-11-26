import { PackageSearch, Plus } from 'lucide-react'
import { useState } from 'react'
import { Etiqueta } from '@/components/Etiqueta'
import { CampoBusca, Filtros } from '@/components/lista/BarraLista'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Pagina } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { useAtraso } from '@/hooks/use-atraso'
import { useCategorias } from '@/hooks/use-cadastros'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { useSessao } from '@/hooks/use-sessao'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { formatarMoeda } from '@/lib/formatadores'
import { colunasDe } from '@/lib/tabela'
import { FormProduto } from '@/pages/cadastros/FormProduto'

export type Produto = components['schemas']['ProdutoResponseDTO']
type Situacao = 'ativos' | 'inativos' | 'todos'

const h = colunasDe<Produto>()
const COLUNAS = [
  h.accessor('codigo', { header: 'Código', cell: (c) => <span className="numeros text-muted-foreground">{c.getValue()}</span> }),
  h.accessor('descricao', {
    header: 'Produto',
    cell: (c) => (
      <span className="flex items-center gap-2 font-medium">
        {c.getValue()}
        {c.row.original.ativo === false && <Etiqueta>Inativo</Etiqueta>}
      </span>
    ),
  }),
  h.accessor('categoriaNome', { header: 'Categoria', cell: (c) => <span className="text-muted-foreground">{c.getValue()}</span> }),
  h.accessor('unidadeMedida', { header: 'Unidade' }),
  h.accessor('precoMedio', { header: 'Custo médio', meta: { direita: true }, cell: (c) => formatarMoeda(c.getValue()) }),
]

export function Produtos() {
  const negocioId = useNegocioId()
  const { ehAdmin } = useSessao()
  const [busca, setBusca] = useState('')
  const [situacao, setSituacao] = useState<Situacao>('ativos')
  const [categoriaId, setCategoriaId] = useState<string>('todas')
  const [editando, setEditando] = useState<Produto | 'novo' | null>(null)
  const termo = useAtraso(busca.trim())
  const { data: categorias } = useCategorias()

  const filtros = {
    descricao: termo || undefined,
    ativo: situacao === 'todos' ? undefined : situacao === 'ativos',
    categoriaId: categoriaId === 'todas' ? undefined : Number(categoriaId),
  }
  const lista = usePaginado(['produtos', negocioId, filtros], (page) =>
    dados(
      api.GET('/api/produto', {
        params: { header: doNegocio(negocioId), query: { ...filtros, page, size: TAMANHO_PAGINA } },
      }),
    ),
  )

  return (
    <Pagina
      titulo="Produtos"
      subtitulo={lista.isSuccess ? `${lista.total} ${lista.total === 1 ? 'produto' : 'produtos'}` : undefined}
      acoes={
        ehAdmin && (
          <Button onClick={() => setEditando('novo')}>
            <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Novo produto</span>
          </Button>
        )
      }
    >
      <div className="space-y-3">
        <CampoBusca valor={busca} aoMudar={setBusca} rotulo="Buscar produto" placeholder="Buscar pelo nome" />
        <Filtros
          rotulo="Situação"
          valor={situacao}
          aoMudar={setSituacao}
          opcoes={[
            { valor: 'ativos', rotulo: 'Ativos' },
            { valor: 'inativos', rotulo: 'Inativos' },
            { valor: 'todos', rotulo: 'Todos' },
          ]}
        />
        {categorias && categorias.length > 1 && (
          <Filtros
            rotulo="Categoria"
            valor={categoriaId}
            aoMudar={setCategoriaId}
            opcoes={[
              { valor: 'todas', rotulo: 'Todas as categorias' },
              ...categorias.map((c) => ({ valor: String(c.id), rotulo: c.nome ?? '' })),
            ]}
          />
        )}
      </div>

      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={lista.itens.length === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={PackageSearch}
        tituloVazio={termo ? 'Nenhum produto encontrado' : 'Nenhum produto cadastrado'}
        descricaoVazio={termo ? 'Tente outro nome ou limpe os filtros.' : ehAdmin ? 'Cadastre o primeiro produto para começar a vender.' : undefined}
        acaoVazio={
          ehAdmin && !termo ? (
            <Button size="lg" onClick={() => setEditando('novo')}>
              <Plus aria-hidden="true" /> Novo produto
            </Button>
          ) : undefined
        }
      >
        <TabelaDados
          rotulo="Produtos"
          dados={lista.itens}
          colunas={COLUNAS}
          chave={(p) => p.id ?? 0}
          aoAbrir={ehAdmin ? setEditando : undefined}
          cartao={(p) => (
            <div className="flex items-start gap-3">
              <div className="min-w-0 flex-1">
                <p className="font-medium text-pretty">{p.descricao}</p>
                <p className="mt-0.5 text-sm text-muted-foreground">
                  #{p.codigo} · {p.categoriaNome} · {p.unidadeMedida}
                </p>
              </div>
              <div className="flex flex-col items-end gap-1">
                <span className="numeros text-sm font-medium">{formatarMoeda(p.precoMedio)}</span>
                {p.ativo === false && <Etiqueta>Inativo</Etiqueta>}
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

      <FormProduto produto={editando} aoFechar={() => setEditando(null)} />
    </Pagina>
  )
}
