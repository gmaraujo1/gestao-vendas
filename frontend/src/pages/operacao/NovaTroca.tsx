import { ArrowDownLeft, ArrowUpRight, Loader2, PackagePlus, Repeat, UserRound, X } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import { BarraAcao } from '@/components/formulario/BarraAcao'
import { Contador } from '@/components/formulario/Contador'
import { Pilulas } from '@/components/formulario/controles'
import { Cartao, Pagina, Secao } from '@/components/pagina/Pagina'
import { SeletorCliente } from '@/components/seletores/SeletorCliente'
import { SeletorProduto } from '@/components/seletores/SeletorProduto'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { useAcao } from '@/hooks/use-acao'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio } from '@/lib/api/client'
import { erroNumero, paraNumero } from '@/lib/numeros'
import { MOTIVOS_PERDA, opcoes, type MotivoPerda } from '@/lib/rotulos'
import { iniciais } from '@/lib/texto'
import { cn } from '@/lib/utils'
import type { Cliente } from '@/pages/cadastros/Clientes'

type Linha = { produtoId: number; descricao: string; unidade?: string; quantidade: string }
type Devolvido = Linha & { destino: 'ESTOQUE' | 'PERDA'; motivo?: MotivoPerda }

const DESTINOS = [
  { valor: 'ESTOQUE' as const, rotulo: 'Volta ao estoque' },
  { valor: 'PERDA' as const, rotulo: 'Vira perda' },
]

/**
 * Troca: o que o cliente devolveu (volta ao estoque ou vira perda) e o que levou no lugar.
 * Ao devolver um produto, o mesmo já entra como "entregue" (o caso mais comum).
 */
export function NovaTroca() {
  const negocioId = useNegocioId()
  const navigate = useNavigate()
  const [cliente, setCliente] = useState<Cliente | null>(null)
  const [devolvidos, setDevolvidos] = useState<Devolvido[]>([])
  const [entregues, setEntregues] = useState<Linha[]>([])
  const [observacao, setObservacao] = useState('')
  const [seletor, setSeletor] = useState<'cliente' | 'devolvido' | 'entregue' | null>(null)
  const [tentou, setTentou] = useState(false)

  const qtdValida = (q: string) => !erroNumero(q, { casas: 3 })
  const valido =
    cliente != null &&
    devolvidos.length > 0 &&
    devolvidos.every((d) => qtdValida(d.quantidade) && (d.destino === 'ESTOQUE' || d.motivo)) &&
    entregues.every((e) => qtdValida(e.quantidade))

  const registrar = useAcao({
    executar: () =>
      dados(
        api.POST('/api/troca', {
          params: { header: doNegocio(negocioId) },
          body: {
            clienteId: cliente!.id!,
            observacao: observacao.trim() || undefined,
            devolvidos: devolvidos.map((d) => ({
              produtoId: d.produtoId,
              quantidade: paraNumero(d.quantidade)!,
              destino: d.destino,
              motivoPerda: d.destino === 'PERDA' ? d.motivo : undefined,
            })),
            entregues: entregues.map((e) => ({ produtoId: e.produtoId, quantidade: paraNumero(e.quantidade)! })),
          },
        }),
      ),
    sucesso: 'Troca registrada',
    invalidar: [['trocas', negocioId], ['estoque', negocioId], ['perdas', negocioId], ['dashboard', negocioId]],
    aoConcluir: (t) => void navigate(`/trocas/${t.id}`, { replace: true }),
  })

  const mudarDevolvido = (id: number, m: Partial<Devolvido>) =>
    setDevolvidos((l) => l.map((d) => (d.produtoId === id ? { ...d, ...m } : d)))
  const mudarEntregue = (id: number, m: Partial<Linha>) =>
    setEntregues((l) => l.map((e) => (e.produtoId === id ? { ...e, ...m } : e)))

  return (
    <Pagina voltar="/trocas" titulo="Nova troca">
      <Secao titulo="Cliente">
        <button
          type="button"
          onClick={() => setSeletor('cliente')}
          className={cn(
            'flex w-full items-center gap-3 rounded-2xl border bg-card p-4 text-left shadow-xs outline-none hover:border-primary/40 focus-visible:ring-3 focus-visible:ring-ring/50',
            tentou && !cliente && 'border-destructive',
          )}
        >
          <span className="grid size-11 shrink-0 place-items-center rounded-full bg-accent text-sm font-semibold text-accent-foreground">
            {cliente ? iniciais(cliente.nome) : <UserRound className="size-5" aria-hidden="true" />}
          </span>
          <span className="min-w-0 flex-1">
            <span className="block truncate font-medium">{cliente?.nome ?? 'Escolher cliente'}</span>
            <span className={cn('block text-sm', tentou && !cliente ? 'text-destructive' : 'text-muted-foreground')}>
              {cliente ? 'Toque para trocar' : tentou ? 'O cliente é obrigatório' : 'Quem está trocando?'}
            </span>
          </span>
        </button>
      </Secao>

      <Secao
        titulo={
          <span className="flex items-center gap-1.5">
            <ArrowDownLeft className="size-4" aria-hidden="true" /> O cliente devolveu
          </span>
        }
        acao={
          <Button variant="ghost" size="sm" onClick={() => setSeletor('devolvido')}>
            <PackagePlus aria-hidden="true" /> Adicionar
          </Button>
        }
      >
        {devolvidos.length === 0 ? (
          <button
            type="button"
            onClick={() => setSeletor('devolvido')}
            className={cn(
              'w-full rounded-2xl border-2 border-dashed p-6 text-center text-sm text-muted-foreground outline-none hover:bg-accent/40 focus-visible:ring-3 focus-visible:ring-ring/50',
              tentou && 'border-destructive/60',
            )}
          >
            Toque para escolher o produto devolvido
          </button>
        ) : (
          devolvidos.map((d) => (
            <Cartao key={d.produtoId} className="space-y-3 p-4">
              <div className="flex items-start gap-2">
                <p className="flex-1 pt-1 font-medium">{d.descricao}</p>
                <Button variant="ghost" size="icon" aria-label={`Tirar ${d.descricao}`} onClick={() => setDevolvidos((l) => l.filter((x) => x.produtoId !== d.produtoId))}>
                  <X aria-hidden="true" />
                </Button>
              </div>
              <Contador rotulo={`quantidade devolvida de ${d.descricao}`} valor={d.quantidade} invalido={tentou && !qtdValida(d.quantidade)} aoMudar={(v) => mudarDevolvido(d.produtoId, { quantidade: v })} />
              {tentou && !qtdValida(d.quantidade) && <p className="text-sm text-destructive">Informe a quantidade</p>}
              <Pilulas rotulo="Destino" opcoes={DESTINOS} valor={d.destino} aoMudar={(v) => mudarDevolvido(d.produtoId, { destino: v })} />
              {d.destino === 'PERDA' && (
                <div className="space-y-1.5">
                  <span className={cn('text-sm', tentou && !d.motivo ? 'text-destructive' : 'text-muted-foreground')}>Motivo da perda</span>
                  <Pilulas<MotivoPerda> rotulo="Motivo da perda" opcoes={opcoes(MOTIVOS_PERDA)} valor={d.motivo} aoMudar={(v) => mudarDevolvido(d.produtoId, { motivo: v })} />
                </div>
              )}
            </Cartao>
          ))
        )}
      </Secao>

      <Secao
        titulo={
          <span className="flex items-center gap-1.5">
            <ArrowUpRight className="size-4" aria-hidden="true" /> O cliente levou
          </span>
        }
        acao={
          <Button variant="ghost" size="sm" onClick={() => setSeletor('entregue')}>
            <PackagePlus aria-hidden="true" /> Adicionar
          </Button>
        }
      >
        {entregues.length === 0 ? (
          <p className="rounded-2xl border border-dashed p-4 text-center text-sm text-muted-foreground">
            Nada (só devolução).
          </p>
        ) : (
          entregues.map((e) => (
            <Cartao key={e.produtoId} className="flex items-center gap-3 p-3 pl-4">
              <p className="min-w-0 flex-1 font-medium">{e.descricao}</p>
              <Contador className="w-40" rotulo={`quantidade entregue de ${e.descricao}`} valor={e.quantidade} invalido={tentou && !qtdValida(e.quantidade)} aoMudar={(v) => mudarEntregue(e.produtoId, { quantidade: v })} />
              <Button variant="ghost" size="icon" aria-label={`Tirar ${e.descricao}`} onClick={() => setEntregues((l) => l.filter((x) => x.produtoId !== e.produtoId))}>
                <X aria-hidden="true" />
              </Button>
            </Cartao>
          ))
        )}
      </Secao>

      <div className="space-y-2">
        <Label htmlFor="troca-obs">Observação (opcional)</Label>
        <Textarea id="troca-obs" rows={2} maxLength={255} value={observacao} onChange={(e) => setObservacao(e.target.value)} placeholder="Ex.: leite perto de vencer" />
      </div>

      <BarraAcao>
        <p className="min-w-0 flex-1 text-sm text-muted-foreground">
          {devolvidos.length} devolvido{devolvidos.length === 1 ? '' : 's'} · {entregues.length} entregue{entregues.length === 1 ? '' : 's'}
        </p>
        <Button
          size="lg"
          className="px-6"
          disabled={registrar.isPending}
          onClick={() => {
            setTentou(true)
            if (valido) registrar.mutate(undefined)
          }}
        >
          {registrar.isPending ? <Loader2 className="animate-spin" aria-hidden="true" /> : <Repeat aria-hidden="true" />}
          Registrar troca
        </Button>
      </BarraAcao>

      <SeletorCliente aberto={seletor === 'cliente'} aoFechar={() => setSeletor(null)} aoEscolher={setCliente} />
      <SeletorProduto
        titulo={seletor === 'entregue' ? 'O que o cliente levou?' : 'O que o cliente devolveu?'}
        aberto={seletor === 'devolvido' || seletor === 'entregue'}
        aoFechar={() => setSeletor(null)}
        escolhidos={(seletor === 'entregue' ? entregues : devolvidos).map((l) => l.produtoId)}
        aoEscolher={(p) => {
          if (p.produtoId == null) return
          const linha: Linha = { produtoId: p.produtoId, descricao: p.descricao ?? '', unidade: p.unidadeMedida, quantidade: '1' }
          if (seletor === 'entregue') {
            setEntregues((l) => (l.some((x) => x.produtoId === linha.produtoId) ? l.filter((x) => x.produtoId !== linha.produtoId) : [...l, linha]))
          } else {
            const jaTem = devolvidos.some((x) => x.produtoId === linha.produtoId)
            setDevolvidos((l) => (jaTem ? l.filter((x) => x.produtoId !== linha.produtoId) : [...l, { ...linha, destino: 'ESTOQUE' }]))
            if (!jaTem) setEntregues((l) => (l.some((x) => x.produtoId === linha.produtoId) ? l : [...l, linha]))
          }
        }}
      />
    </Pagina>
  )
}
