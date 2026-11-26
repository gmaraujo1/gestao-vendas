import { useQuery } from '@tanstack/react-query'
import { MapPin, MessageCircle, Pencil, Phone, Receipt, ShoppingBasket, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { ConfirmarAcao } from '@/components/ConfirmarAcao'
import { Etiqueta } from '@/components/Etiqueta'
import { EstadosLista, CarregarMais } from '@/components/lista/EstadosLista'
import { Cartao, Pagina, Secao } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useAcao } from '@/hooks/use-acao'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { useSessao } from '@/hooks/use-sessao'
import { api, dados, doNegocio, semConteudo } from '@/lib/api/client'
import { useCarrinho } from '@/lib/carrinho'
import { horaDe, rotuloDia } from '@/lib/datas'
import { formatarMoeda } from '@/lib/formatadores'
import { formatarTelefone, iniciais, linkWhatsApp } from '@/lib/texto'
import type { Cliente } from '@/pages/cadastros/Clientes'
import { FormCliente } from '@/pages/cadastros/FormCliente'

/** Ficha do cliente: contato com um toque e o histórico de compras ("quem levou o quê"). */
export function DetalheCliente() {
  const id = Number(useParams().id)
  const negocioId = useNegocioId()
  const navigate = useNavigate()
  const { ehAdmin } = useSessao()
  const [editando, setEditando] = useState<Cliente | null>(null)
  const [excluindo, setExcluindo] = useState(false)

  const cliente = useQuery({
    queryKey: ['clientes', negocioId, 'detalhe', id],
    queryFn: () => dados(api.GET('/api/cliente/{id}', { params: { path: { id }, header: doNegocio(negocioId) } })),
  })
  const vendas = usePaginado(['vendas', negocioId, { clienteId: id }], (page) =>
    dados(api.GET('/api/venda', { params: { header: doNegocio(negocioId), query: { clienteId: id, page, size: TAMANHO_PAGINA } } })),
  )
  const excluir = useAcao({
    executar: () => semConteudo(api.DELETE('/api/cliente/{id}', { params: { path: { id }, header: doNegocio(negocioId) } })),
    sucesso: 'Cliente excluído',
    invalidar: [['clientes', negocioId]],
    aoConcluir: () => void navigate('/clientes', { replace: true }),
  })

  function venderPara() {
    const c = cliente.data
    if (!c?.id) return
    useCarrinho.getState().atualizar(negocioId, { clienteId: c.id, clienteNome: c.nome ?? '' })
    void navigate('/vendas/nova')
  }

  const c = cliente.data
  const whatsapp = linkWhatsApp(c?.telefone)

  return (
    <Pagina
      voltar="/clientes"
      titulo={c?.nome ?? <Skeleton className="h-8 w-48" />}
      subtitulo={c?.cidade}
      acoes={
        c && (
          <Button variant="outline" size="icon-lg" aria-label="Editar cliente" onClick={() => setEditando(c)}>
            <Pencil aria-hidden="true" />
          </Button>
        )
      }
    >
      {cliente.isError ? (
        <EstadosLista carregando={false} erro={cliente.error} vazio={false} tentarDeNovo={() => void cliente.refetch()} iconeVazio={Receipt} tituloVazio="">
          {null}
        </EstadosLista>
      ) : (
        <>
          <Cartao className="p-5">
            <div className="flex items-center gap-4">
              <span className="grid size-14 shrink-0 place-items-center rounded-full bg-primary text-lg font-semibold text-primary-foreground">
                {iniciais(c?.nome)}
              </span>
              <div className="min-w-0 flex-1 space-y-1 text-sm">
                {c?.telefone && (
                  <p className="numeros flex items-center gap-2">
                    <Phone className="size-4 text-muted-foreground" aria-hidden="true" />
                    {formatarTelefone(c.telefone)}
                  </p>
                )}
                {c?.endereco && (
                  <p className="flex items-center gap-2 text-pretty">
                    <MapPin className="size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
                    {c.endereco}
                  </p>
                )}
                {c?.ativo === false && <Etiqueta>Inativo</Etiqueta>}
                {c && !c.telefone && !c.endereco && <p className="text-muted-foreground">Sem contato cadastrado</p>}
              </div>
            </div>
            {c?.observacao && <p className="mt-4 rounded-xl bg-muted/70 p-3 text-sm text-pretty">{c.observacao}</p>}
            <div className="mt-4 grid grid-cols-2 gap-2">
              {c?.telefone && (
                <Button asChild variant="outline" size="lg">
                  <a href={`tel:${c.telefone.replace(/\D/g, '')}`}>
                    <Phone aria-hidden="true" /> Ligar
                  </a>
                </Button>
              )}
              {whatsapp && (
                <Button asChild variant="outline" size="lg">
                  <a href={whatsapp} target="_blank" rel="noreferrer">
                    <MessageCircle aria-hidden="true" /> WhatsApp
                  </a>
                </Button>
              )}
              <Button size="lg" className="col-span-2" onClick={venderPara} disabled={!c || c.ativo === false}>
                <ShoppingBasket aria-hidden="true" /> Vender para {c?.nome?.split(' ')[0] ?? 'este cliente'}
              </Button>
            </div>
          </Cartao>

          <Secao titulo={vendas.isSuccess ? `Compras (${vendas.total})` : 'Compras'}>
            <EstadosLista
              carregando={vendas.isPending}
              erro={vendas.error}
              vazio={vendas.itens.length === 0}
              tentarDeNovo={() => void vendas.refetch()}
              iconeVazio={Receipt}
              tituloVazio="Nenhuma compra ainda"
            >
              <Cartao className="divide-y overflow-hidden">
                {vendas.itens.map((v) => (
                  <Link
                    key={v.id}
                    to={`/vendas/${v.id}`}
                    className="flex items-center gap-3 px-4 py-3.5 transition-colors outline-none hover:bg-accent/50 focus-visible:bg-accent"
                  >
                    <div className="min-w-0 flex-1">
                      <p className="font-medium">{rotuloDia(v.dataHora)}</p>
                      <p className="text-sm text-muted-foreground">{horaDe(v.dataHora)} · venda #{v.id}</p>
                    </div>
                    {v.status === 'CANCELADA' && <Etiqueta tom="perigo">Cancelada</Etiqueta>}
                    <span className={`numeros font-semibold ${v.status === 'CANCELADA' ? 'text-muted-foreground line-through' : ''}`}>
                      {formatarMoeda(v.total)}
                    </span>
                  </Link>
                ))}
              </Cartao>
              <CarregarMais
                temMais={vendas.hasNextPage}
                carregando={vendas.isFetchingNextPage}
                aoCarregar={() => void vendas.fetchNextPage()}
                exibidos={vendas.itens.length}
                total={vendas.total}
              />
            </EstadosLista>
          </Secao>

          {ehAdmin && c && (
            <Button variant="ghost" className="w-full text-destructive" onClick={() => setExcluindo(true)}>
              <Trash2 aria-hidden="true" /> Excluir cliente
            </Button>
          )}
        </>
      )}

      <FormCliente cliente={editando} aoFechar={() => setEditando(null)} />
      <ConfirmarAcao
        aberto={excluindo}
        aoFechar={() => setExcluindo(false)}
        titulo="Excluir este cliente?"
        descricao="Clientes com vendas ou trocas não podem ser excluídos: nesse caso, desative na edição."
        confirmar="Excluir"
        aoConfirmar={() => excluir.mutate(undefined)}
      />
    </Pagina>
  )
}
