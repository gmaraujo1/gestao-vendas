import { Package } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import { Etiqueta } from '@/components/Etiqueta'
import { CampoBusca, Filtros } from '@/components/lista/BarraLista'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Pagina } from '@/components/pagina/Pagina'
import { useAtraso } from '@/hooks/use-atraso'
import { useCategorias } from '@/hooks/use-cadastros'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { formatarQuantidade } from '@/lib/formatadores'
import { colunasDe } from '@/lib/tabela'
import { cn } from '@/lib/utils'

type Saldo = components['schemas']['SaldoProdutoDTO']

function Quantidade({ s, grande }: { s: Saldo; grande?: boolean }) {
  const saldo = s.saldo ?? 0
  return (
    <span className={cn('numeros font-semibold', grande && 'text-lg', saldo < 0 ? 'text-destructive' : saldo === 0 && 'text-muted-foreground')}>
      {formatarQuantidade(saldo)} <span className="text-xs font-normal text-muted-foreground">{s.unidadeMedida}</span>
    </span>
  )
}

const h = colunasDe<Saldo>()
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
  h.accessor('saldo', { header: 'Em estoque', meta: { direita: true }, cell: (c) => <Quantidade s={c.row.original} /> }),
]

/** Saldo de cada produto (soma das movimentações). Toque para ver o extrato. */
export function Estoque() {
  const negocioId = useNegocioId()
  const navigate = useNavigate()
  const [busca, setBusca] = useState('')
  const [categoriaId, setCategoriaId] = useState('todas')
  const termo = useAtraso(busca.trim())
  const { data: categorias } = useCategorias(true)
  const filtros = { descricao: termo || undefined, ativo: true, categoriaId: categoriaId === 'todas' ? undefined : Number(categoriaId) }
  const lista = usePaginado(['estoque', negocioId, filtros], (page) =>
    dados(api.GET('/api/estoque', { params: { header: doNegocio(negocioId), query: { ...filtros, page, size: TAMANHO_PAGINA } } })),
  )
  const negativos = lista.itens.filter((s) => (s.saldo ?? 0) < 0).length

  return (
    <Pagina
      titulo="Estoque"
      subtitulo={negativos > 0 ? `${negativos} ${negativos === 1 ? 'produto' : 'produtos'} com saldo negativo: confira a contagem.` : 'Toque num produto para ver as entradas e saídas.'}
    >
      <div className="space-y-3">
        <CampoBusca valor={busca} aoMudar={setBusca} rotulo="Buscar produto" placeholder="Buscar pelo nome" />
        {categorias && categorias.length > 1 && (
          <Filtros
            rotulo="Categoria"
            valor={categoriaId}
            aoMudar={setCategoriaId}
            opcoes={[{ valor: 'todas', rotulo: 'Todas' }, ...categorias.map((c) => ({ valor: String(c.id), rotulo: c.nome ?? '' }))]}
          />
        )}
      </div>
      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={lista.itens.length === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={Package}
        tituloVazio={termo ? 'Nenhum produto encontrado' : 'Nenhum produto ativo'}
      >
        <TabelaDados
          rotulo="Estoque"
          dados={lista.itens}
          colunas={COLUNAS}
          chave={(s) => s.produtoId ?? 0}
          aoAbrir={(s) => void navigate(`/estoque/${s.produtoId}`)}
          cartao={(s) => (
            <div className="flex items-center gap-3">
              <div className="min-w-0 flex-1">
                <p className="font-medium text-pretty">{s.descricao}</p>
                <p className="text-sm text-muted-foreground">
                  #{s.codigo} · {s.categoriaNome}
                </p>
              </div>
              <Quantidade s={s} grande />
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
