import { Check, PackageSearch } from 'lucide-react'
import { useState } from 'react'
import { CampoBusca } from '@/components/lista/BarraLista'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { PainelFormulario } from '@/components/formulario/PainelFormulario'
import { Button } from '@/components/ui/button'
import { useAtraso } from '@/hooks/use-atraso'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { formatarQuantidade } from '@/lib/formatadores'
import { cn } from '@/lib/utils'

export type ProdutoComSaldo = components['schemas']['SaldoProdutoDTO']

/**
 * Busca de produto com o saldo em estoque ao lado (dá para ver o que tem antes de vender).
 * Fica aberto para escolher vários seguidos; os já escolhidos aparecem marcados.
 */
export function SeletorProduto({
  aberto,
  aoFechar,
  aoEscolher,
  escolhidos = [],
  titulo = 'Adicionar produtos',
}: {
  aberto: boolean
  aoFechar: () => void
  aoEscolher: (p: ProdutoComSaldo) => void
  escolhidos?: number[]
  titulo?: string
}) {
  const negocioId = useNegocioId()
  const [busca, setBusca] = useState('')
  const termo = useAtraso(busca.trim(), 250)
  const lista = usePaginado(
    ['estoque', negocioId, { descricao: termo, ativo: true }],
    (page) =>
      dados(
        api.GET('/api/estoque', {
          params: { header: doNegocio(negocioId), query: { descricao: termo || undefined, ativo: true, page, size: TAMANHO_PAGINA } },
        }),
      ),
    { enabled: aberto },
  )

  return (
    <PainelFormulario
      aberto={aberto}
      aoFechar={aoFechar}
      titulo={titulo}
      rodape={
        <Button size="lg" className="w-full" onClick={aoFechar}>
          {escolhidos.length > 0 ? `Pronto (${escolhidos.length} ${escolhidos.length === 1 ? 'item' : 'itens'})` : 'Fechar'}
        </Button>
      }
    >
      <div className="space-y-3">
        <CampoBusca valor={busca} aoMudar={setBusca} rotulo="Buscar produto" placeholder="Nome do produto" />
        <EstadosLista
          carregando={lista.isPending}
          erro={lista.error}
          vazio={lista.itens.length === 0}
          tentarDeNovo={() => void lista.refetch()}
          iconeVazio={PackageSearch}
          tituloVazio="Nenhum produto encontrado"
          descricaoVazio="Só aparecem produtos ativos."
        >
          <ul className="divide-y overflow-hidden rounded-2xl border bg-card">
            {lista.itens.map((p) => {
              const marcado = escolhidos.includes(p.produtoId ?? -1)
              const saldo = p.saldo ?? 0
              return (
                <li key={p.produtoId}>
                  <button
                    type="button"
                    onClick={() => aoEscolher(p)}
                    aria-pressed={marcado}
                    className={cn(
                      'flex min-h-16 w-full items-center gap-3 px-4 py-3 text-left transition-colors outline-none hover:bg-accent/50 focus-visible:bg-accent',
                      marcado && 'bg-accent/70',
                    )}
                  >
                    <div className="min-w-0 flex-1">
                      <p className="font-medium text-pretty">{p.descricao}</p>
                      <p className="text-sm text-muted-foreground">
                        #{p.codigo} · {p.categoriaNome}
                      </p>
                    </div>
                    <span className={cn('numeros text-sm', saldo <= 0 ? 'text-destructive' : 'text-muted-foreground')}>
                      {formatarQuantidade(saldo)} {p.unidadeMedida}
                    </span>
                    <span
                      className={cn(
                        'grid size-7 shrink-0 place-items-center rounded-full border transition-colors',
                        marcado ? 'border-primary bg-primary text-primary-foreground' : 'border-input',
                      )}
                      aria-hidden="true"
                    >
                      {marcado && <Check className="size-4" />}
                    </span>
                  </button>
                </li>
              )
            })}
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
  )
}
