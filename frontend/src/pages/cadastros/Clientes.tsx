import { Plus, Users } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import { Etiqueta } from '@/components/Etiqueta'
import { CampoBusca, Filtros } from '@/components/lista/BarraLista'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Pagina } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { useAtraso } from '@/hooks/use-atraso'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { colunasDe } from '@/lib/tabela'
import { formatarTelefone, iniciais } from '@/lib/texto'
import { FormCliente } from '@/pages/cadastros/FormCliente'

export type Cliente = components['schemas']['ClienteResponseDTO']

const h = colunasDe<Cliente>()
const COLUNAS = [
  h.accessor('nome', {
    header: 'Cliente',
    cell: (c) => (
      <span className="flex items-center gap-2 font-medium">
        {c.getValue()}
        {c.row.original.ativo === false && <Etiqueta>Inativo</Etiqueta>}
      </span>
    ),
  }),
  h.accessor('telefone', { header: 'Telefone', cell: (c) => <span className="numeros">{formatarTelefone(c.getValue())}</span> }),
  h.accessor('cidade', { header: 'Cidade', cell: (c) => <span className="text-muted-foreground">{c.getValue()}</span> }),
]

export function Clientes() {
  const negocioId = useNegocioId()
  const navigate = useNavigate()
  const [busca, setBusca] = useState('')
  const [situacao, setSituacao] = useState<'ativos' | 'todos'>('ativos')
  const [novo, setNovo] = useState(false)
  const termo = useAtraso(busca.trim())

  const filtros = { nome: termo || undefined, ativo: situacao === 'ativos' ? true : undefined }
  const lista = usePaginado(['clientes', negocioId, filtros], (page) =>
    dados(api.GET('/api/cliente', { params: { header: doNegocio(negocioId), query: { ...filtros, page, size: TAMANHO_PAGINA } } })),
  )

  return (
    <Pagina
      titulo="Clientes"
      subtitulo={lista.isSuccess ? `${lista.total} ${lista.total === 1 ? 'cliente' : 'clientes'}` : undefined}
      acoes={
        <Button onClick={() => setNovo(true)}>
          <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Novo cliente</span>
        </Button>
      }
    >
      <div className="space-y-3">
        <CampoBusca valor={busca} aoMudar={setBusca} rotulo="Buscar cliente" placeholder="Buscar pelo nome" />
        <Filtros
          rotulo="Situação"
          valor={situacao}
          aoMudar={setSituacao}
          opcoes={[
            { valor: 'ativos', rotulo: 'Ativos' },
            { valor: 'todos', rotulo: 'Todos' },
          ]}
        />
      </div>
      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={lista.itens.length === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={Users}
        tituloVazio={termo ? 'Nenhum cliente encontrado' : 'Nenhum cliente cadastrado'}
        descricaoVazio={termo ? 'Confira o nome ou cadastre um novo.' : 'Cadastre os clientes para saber quem comprou o quê.'}
        acaoVazio={
          <Button size="lg" onClick={() => setNovo(true)}>
            <Plus aria-hidden="true" /> Novo cliente
          </Button>
        }
      >
        <TabelaDados
          rotulo="Clientes"
          dados={lista.itens}
          colunas={COLUNAS}
          chave={(c) => c.id ?? 0}
          aoAbrir={(c) => void navigate(`/clientes/${c.id}`)}
          cartao={(c) => (
            <div className="flex items-center gap-3">
              <span className="grid size-11 shrink-0 place-items-center rounded-full bg-accent text-sm font-semibold text-accent-foreground">
                {iniciais(c.nome)}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{c.nome}</p>
                <p className="truncate text-sm text-muted-foreground">
                  {[formatarTelefone(c.telefone), c.cidade].filter(Boolean).join(' · ') || 'Sem contato'}
                </p>
              </div>
              {c.ativo === false && <Etiqueta>Inativo</Etiqueta>}
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
      <FormCliente
        cliente={novo ? 'novo' : null}
        aoFechar={() => setNovo(false)}
        aoSalvar={(c) => c.id != null && void navigate(`/clientes/${c.id}`)}
      />
    </Pagina>
  )
}
