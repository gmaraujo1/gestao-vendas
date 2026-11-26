import { useQuery } from '@tanstack/react-query'
import { Ban, CircleCheck, Plus, Receipt } from 'lucide-react'
import { useState } from 'react'
import { Link, useLocation, useParams } from 'react-router'
import { ConfirmarAcao } from '@/components/ConfirmarAcao'
import { Etiqueta } from '@/components/Etiqueta'
import { EstadosLista } from '@/components/lista/EstadosLista'
import { Cartao, Pagina, Secao } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { useAcao } from '@/hooks/use-acao'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { useSessao } from '@/hooks/use-sessao'
import { api, dados, doNegocio } from '@/lib/api/client'
import { formatarDataHora, formatarMoeda, formatarQuantidade } from '@/lib/formatadores'

/** Venda: itens, total e cancelamento (só ADMIN, com motivo; o estoque volta). */
export function DetalheVenda() {
  const id = Number(useParams().id)
  const negocioId = useNegocioId()
  const { ehAdmin } = useSessao()
  const recemCriada = (useLocation().state as { recemCriada?: boolean } | null)?.recemCriada
  const [cancelando, setCancelando] = useState(false)
  const [motivo, setMotivo] = useState('')

  const venda = useQuery({
    queryKey: ['vendas', negocioId, 'detalhe', id],
    queryFn: () => dados(api.GET('/api/venda/{id}', { params: { path: { id }, header: doNegocio(negocioId) } })),
  })
  const cancelar = useAcao({
    executar: () =>
      dados(api.POST('/api/venda/{id}/cancelar', { params: { path: { id }, header: doNegocio(negocioId) }, body: { motivo: motivo.trim() } })),
    sucesso: 'Venda cancelada; o estoque foi devolvido',
    invalidar: [['vendas', negocioId], ['estoque', negocioId], ['dashboard', negocioId]],
    aoConcluir: () => {
      setCancelando(false)
      setMotivo('')
    },
  })

  const v = venda.data
  const cancelada = v?.status === 'CANCELADA'

  return (
    <Pagina
      voltar="/vendas"
      titulo={`Venda #${id}`}
      subtitulo={v ? formatarDataHora(v.dataHora) : undefined}
      acoes={cancelada ? <Etiqueta tom="perigo">Cancelada</Etiqueta> : v ? <Etiqueta tom="sucesso">Registrada</Etiqueta> : null}
    >
      {recemCriada && !cancelada && (
        <div role="status" className="flex items-center gap-3 rounded-2xl bg-sucesso/12 p-4 text-sucesso motion-safe:animate-in motion-safe:zoom-in-95">
          <CircleCheck className="size-6 shrink-0" aria-hidden="true" />
          <p className="flex-1 font-medium">Venda registrada!</p>
          <Button asChild size="sm">
            <Link to="/vendas/nova">
              <Plus aria-hidden="true" /> Outra
            </Link>
          </Button>
        </div>
      )}

      <EstadosLista
        carregando={venda.isPending}
        erro={venda.error}
        vazio={false}
        tentarDeNovo={() => void venda.refetch()}
        iconeVazio={Receipt}
        tituloVazio=""
      >
        {v && (
          <>
            <Cartao className="p-5">
              <p className="text-sm text-muted-foreground">Cliente</p>
              <Link to={`/clientes/${v.clienteId}`} className="text-lg font-semibold underline-offset-4 hover:underline">
                {v.clienteNome}
              </Link>
              <p className="mt-4 text-sm text-muted-foreground">Total</p>
              <p className={`numeros text-3xl font-semibold tracking-tight ${cancelada ? 'text-muted-foreground line-through' : ''}`}>
                {formatarMoeda(v.total)}
              </p>
              {v.observacao && <p className="mt-4 rounded-xl bg-muted/70 p-3 text-sm text-pretty">{v.observacao}</p>}
            </Cartao>

            {cancelada && (
              <div className="rounded-2xl border border-destructive/30 bg-destructive/5 p-4 text-sm">
                <p className="font-medium text-destructive">Cancelada em {formatarDataHora(v.canceladaEm)}</p>
                <p className="mt-1 text-pretty">{v.motivoCancelamento}</p>
              </div>
            )}

            <Secao titulo={`Itens (${v.itens?.length ?? 0})`}>
              <Cartao className="divide-y overflow-hidden">
                {v.itens?.map((i) => (
                  <div key={i.produtoId} className="flex items-center gap-3 px-4 py-3.5">
                    <div className="min-w-0 flex-1">
                      <p className="font-medium text-pretty">{i.produtoDescricao}</p>
                      <p className="numeros text-sm text-muted-foreground">
                        {formatarQuantidade(i.quantidade)} × {formatarMoeda(i.precoUnitario)}
                      </p>
                    </div>
                    <span className="numeros font-semibold">{formatarMoeda(i.subtotal)}</span>
                  </div>
                ))}
              </Cartao>
            </Secao>

            {ehAdmin && !cancelada && (
              <Button variant="ghost" className="w-full text-destructive" onClick={() => setCancelando(true)}>
                <Ban aria-hidden="true" /> Cancelar venda
              </Button>
            )}
          </>
        )}
        {venda.isPending && <Skeleton className="h-40 rounded-2xl" />}
      </EstadosLista>

      <ConfirmarAcao
        aberto={cancelando}
        aoFechar={() => setCancelando(false)}
        titulo="Cancelar esta venda?"
        descricao="Os produtos voltam para o estoque e a venda sai dos relatórios. Não dá para desfazer."
        confirmar={cancelar.isPending ? 'Cancelando…' : 'Cancelar venda'}
        desabilitado={motivo.trim().length < 3 || cancelar.isPending}
        aoConfirmar={() => cancelar.mutate(undefined)}
      >
        <div className="space-y-2">
          <Label htmlFor="motivo-cancelamento">Motivo</Label>
          <Textarea
            id="motivo-cancelamento"
            rows={2}
            maxLength={255}
            placeholder="Ex.: lançada em dobro"
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
          />
          {motivo.trim().length < 3 && <p className="text-sm text-muted-foreground">Escreva o motivo para poder cancelar.</p>}
        </div>
      </ConfirmarAcao>
    </Pagina>
  )
}
