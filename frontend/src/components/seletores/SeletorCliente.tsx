import { Plus, Users } from 'lucide-react'
import { useState } from 'react'
import { CampoBusca } from '@/components/lista/BarraLista'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { PainelFormulario } from '@/components/formulario/PainelFormulario'
import { Button } from '@/components/ui/button'
import { useAtraso } from '@/hooks/use-atraso'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { api, dados, doNegocio } from '@/lib/api/client'
import { formatarTelefone, iniciais } from '@/lib/texto'
import type { Cliente } from '@/pages/cadastros/Clientes'
import { FormCliente } from '@/pages/cadastros/FormCliente'

/** Busca de cliente com cadastro rápido ("não achei: cadastrar"). */
export function SeletorCliente({
  aberto,
  aoFechar,
  aoEscolher,
}: {
  aberto: boolean
  aoFechar: () => void
  aoEscolher: (c: Cliente) => void
}) {
  const negocioId = useNegocioId()
  const [busca, setBusca] = useState('')
  const [cadastrando, setCadastrando] = useState(false)
  const termo = useAtraso(busca.trim(), 250)
  const lista = usePaginado(
    ['clientes', negocioId, { nome: termo, ativo: true }],
    (page) =>
      dados(api.GET('/api/cliente', { params: { header: doNegocio(negocioId), query: { nome: termo || undefined, ativo: true, page, size: TAMANHO_PAGINA } } })),
    { enabled: aberto },
  )

  function escolher(c: Cliente) {
    aoEscolher(c)
    setBusca('')
    aoFechar()
  }

  return (
    <>
      <PainelFormulario aberto={aberto && !cadastrando} aoFechar={aoFechar} titulo="Escolher cliente">
        <div className="space-y-3">
          <CampoBusca valor={busca} aoMudar={setBusca} rotulo="Buscar cliente" placeholder="Nome do cliente" />
          <Button variant="outline" size="lg" className="w-full justify-start" onClick={() => setCadastrando(true)}>
            <Plus aria-hidden="true" /> {termo ? `Cadastrar “${termo}”` : 'Cadastrar cliente novo'}
          </Button>
          <EstadosLista
            carregando={lista.isPending}
            erro={lista.error}
            vazio={lista.itens.length === 0}
            tentarDeNovo={() => void lista.refetch()}
            iconeVazio={Users}
            tituloVazio="Nenhum cliente encontrado"
          >
            <ul className="divide-y overflow-hidden rounded-2xl border bg-card">
              {lista.itens.map((c) => (
                <li key={c.id}>
                  <button
                    type="button"
                    onClick={() => escolher(c)}
                    className="flex min-h-16 w-full items-center gap-3 px-4 py-3 text-left transition-colors outline-none hover:bg-accent/50 focus-visible:bg-accent"
                  >
                    <span className="grid size-10 shrink-0 place-items-center rounded-full bg-accent text-sm font-semibold text-accent-foreground">
                      {iniciais(c.nome)}
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="block truncate font-medium">{c.nome}</span>
                      <span className="block truncate text-sm text-muted-foreground">
                        {[formatarTelefone(c.telefone), c.cidade].filter(Boolean).join(' · ')}
                      </span>
                    </span>
                  </button>
                </li>
              ))}
            </ul>
            <CarregarMais
              temMais={lista.hasNextPage}
              carregando={lista.isFetchingNextPage}
              aoCarregar={() => void lista.fetchNextPage()}
              exibidos={lista.itens.length}
              total={lista.total}
            />
          </EstadosLista>
        </div>
      </PainelFormulario>
      <FormCliente
        cliente={cadastrando ? 'novo' : null}
        nomeInicial={termo}
        aoFechar={() => setCadastrando(false)}
        aoSalvar={escolher}
      />
    </>
  )
}
