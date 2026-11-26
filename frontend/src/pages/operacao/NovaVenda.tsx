import { ChevronDown, Loader2, PackagePlus, ShoppingBasket, Trash2, UserRound, X } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router'
import { BarraAcao } from '@/components/formulario/BarraAcao'
import { Contador } from '@/components/formulario/Contador'
import { InputNumero } from '@/components/formulario/controles'
import { ConfirmarAcao } from '@/components/ConfirmarAcao'
import { Cartao, Pagina, Secao } from '@/components/pagina/Pagina'
import { SeletorCliente } from '@/components/seletores/SeletorCliente'
import { SeletorProduto } from '@/components/seletores/SeletorProduto'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { useAcao } from '@/hooks/use-acao'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio } from '@/lib/api/client'
import { useCarrinho, type ItemCarrinho } from '@/lib/carrinho'
import { agoraIso, formatarMoeda } from '@/lib/formatadores'
import { erroNumero, paraNumero, subtotalCentavos } from '@/lib/numeros'
import { iniciais } from '@/lib/texto'
import { cn } from '@/lib/utils'

const SEM_ITENS: ItemCarrinho[] = []

function validarItem(i: ItemCarrinho) {
  const preco = erroNumero(i.preco, { minimo: 'naoNegativo', casas: 2 })
  return {
    quantidade: erroNumero(i.quantidade, { casas: 3 }) && `Quantidade: ${erroNumero(i.quantidade, { casas: 3 })?.toLowerCase()}`,
    preco: preco === 'Obrigatório' ? 'Informe o preço' : preco && `Preço: ${preco.toLowerCase()}`,
  }
}

const centavosDoItem = (i: ItemCarrinho) => subtotalCentavos(paraNumero(i.quantidade) ?? 0, paraNumero(i.preco) ?? 0)

/**
 * Venda rápida: cliente, produtos (com o saldo de cada um), quantidade e preço. O rascunho fica
 * salvo no aparelho (Zustand) e o último preço de cada produto vira a sugestão da próxima venda.
 */
export function NovaVenda() {
  const negocioId = useNegocioId()
  const navigate = useNavigate()
  const rascunho = useCarrinho((s) => s.porNegocio[negocioId])
  const carrinho = useCarrinho.getState()
  const itens = rascunho?.itens ?? SEM_ITENS
  const [escolhendoCliente, setEscolhendoCliente] = useState(false)
  const [escolhendoProduto, setEscolhendoProduto] = useState(false)
  const [mais, setMais] = useState(Boolean(rascunho?.observacao || rascunho?.dataHora))
  const [tentou, setTentou] = useState(false)
  const [limpando, setLimpando] = useState(false)

  const total = useMemo(() => itens.reduce((soma, i) => soma + centavosDoItem(i), 0) / 100, [itens])
  const erros = itens.map(validarItem)
  const valido = rascunho?.clienteId != null && itens.length > 0 && erros.every((e) => !e.quantidade && !e.preco)

  const registrar = useAcao({
    executar: () =>
      dados(
        api.POST('/api/venda', {
          params: { header: doNegocio(negocioId) },
          body: {
            clienteId: rascunho!.clienteId!,
            observacao: rascunho?.observacao.trim() || undefined,
            dataHora: rascunho?.dataHora ? `${rascunho.dataHora}:00` : undefined,
            itens: itens.map((i) => ({
              produtoId: i.produtoId,
              quantidade: paraNumero(i.quantidade)!,
              precoUnitario: paraNumero(i.preco)!,
            })),
          },
        }),
      ),
    sucesso: (v) => `Venda de ${formatarMoeda(v.total)} registrada`,
    invalidar: [['vendas', negocioId], ['estoque', negocioId], ['dashboard', negocioId]],
    aoConcluir: (v) => {
      carrinho.lembrarPrecos(negocioId, itens)
      carrinho.limpar(negocioId)
      void navigate(`/vendas/${v.id}`, { replace: true, state: { recemCriada: true } })
    },
  })

  function finalizar() {
    setTentou(true)
    if (valido) registrar.mutate(undefined)
  }

  return (
    <Pagina
      voltar="/vendas"
      titulo="Nova venda"
      acoes={
        (itens.length > 0 || rascunho?.clienteId) && (
          <Button variant="ghost" onClick={() => setLimpando(true)}>
            <Trash2 aria-hidden="true" /> Limpar
          </Button>
        )
      }
    >
      <Secao titulo="Cliente">
        <button
          type="button"
          onClick={() => setEscolhendoCliente(true)}
          className={cn(
            'flex w-full items-center gap-3 rounded-2xl border bg-card p-4 text-left shadow-xs transition-[border-color] outline-none hover:border-primary/40 focus-visible:ring-3 focus-visible:ring-ring/50',
            tentou && !rascunho?.clienteId && 'border-destructive',
          )}
        >
          {rascunho?.clienteId ? (
            <>
              <span className="grid size-11 shrink-0 place-items-center rounded-full bg-primary text-sm font-semibold text-primary-foreground">
                {iniciais(rascunho.clienteNome ?? '')}
              </span>
              <span className="min-w-0 flex-1">
                <span className="block truncate font-semibold">{rascunho.clienteNome}</span>
                <span className="block text-sm text-muted-foreground">Toque para trocar</span>
              </span>
            </>
          ) : (
            <>
              <span className="grid size-11 shrink-0 place-items-center rounded-full bg-accent text-accent-foreground">
                <UserRound className="size-5" aria-hidden="true" />
              </span>
              <span className="flex-1">
                <span className="block font-medium">Escolher cliente</span>
                <span className={cn('block text-sm', tentou ? 'text-destructive' : 'text-muted-foreground')}>
                  {tentou ? 'O cliente é obrigatório' : 'Quem está comprando?'}
                </span>
              </span>
            </>
          )}
          <ChevronDown className="size-5 -rotate-90 text-muted-foreground" aria-hidden="true" />
        </button>
      </Secao>

      <Secao
        titulo={itens.length ? `Produtos (${itens.length})` : 'Produtos'}
        acao={
          itens.length > 0 && (
            <Button variant="ghost" size="sm" onClick={() => setEscolhendoProduto(true)}>
              <PackagePlus aria-hidden="true" /> Adicionar
            </Button>
          )
        }
      >
        {itens.length === 0 ? (
          <button
            type="button"
            onClick={() => setEscolhendoProduto(true)}
            className={cn(
              'flex w-full flex-col items-center gap-2 rounded-2xl border-2 border-dashed p-8 text-center transition-colors outline-none hover:border-primary/50 hover:bg-accent/40 focus-visible:ring-3 focus-visible:ring-ring/50',
              tentou && 'border-destructive/60',
            )}
          >
            <span className="grid size-12 place-items-center rounded-2xl bg-accent text-accent-foreground">
              <PackagePlus className="size-6" aria-hidden="true" />
            </span>
            <span className="font-medium">Adicionar produtos</span>
            <span className="text-sm text-muted-foreground">Busque pelo nome; dá para escolher vários.</span>
          </button>
        ) : (
          <ul className="space-y-2.5">
            {itens.map((item, idx) => {
              const erro = tentou ? erros[idx] : undefined
              const subtotal = centavosDoItem(item) / 100
              return (
                <li key={item.produtoId}>
                  <Cartao className="p-4">
                    <div className="flex items-start gap-2">
                      <p className="flex-1 pt-1 font-medium text-pretty">{item.descricao}</p>
                      <Button
                        variant="ghost"
                        size="icon"
                        aria-label={`Tirar ${item.descricao}`}
                        onClick={() => carrinho.removerItem(negocioId, item.produtoId)}
                      >
                        <X aria-hidden="true" />
                      </Button>
                    </div>
                    <div className="mt-3 grid grid-cols-2 gap-3">
                      <div className="space-y-1">
                        <span className="text-xs text-muted-foreground">Quantidade ({item.unidade ?? 'UN'})</span>
                        <Contador
                          rotulo={`quantidade de ${item.descricao}`}
                          valor={item.quantidade}
                          invalido={!!erro?.quantidade}
                          aoMudar={(v) => carrinho.alterarItem(negocioId, item.produtoId, { quantidade: v })}
                        />
                      </div>
                      <div className="space-y-1">
                        <span className="text-xs text-muted-foreground">Preço unitário</span>
                        <InputNumero
                          aria-label={`Preço de ${item.descricao}`}
                          aria-invalid={!!erro?.preco || undefined}
                          prefixo="R$"
                          placeholder="0,00"
                          value={item.preco}
                          onChange={(e) => carrinho.alterarItem(negocioId, item.produtoId, { preco: e.target.value })}
                        />
                      </div>
                    </div>
                    <div className="mt-3 flex items-center justify-between text-sm">
                      <span className="text-destructive">{erro?.quantidade ?? erro?.preco}</span>
                      <span className="numeros text-muted-foreground">
                        Subtotal <span className="font-semibold text-foreground">{formatarMoeda(subtotal)}</span>
                      </span>
                    </div>
                  </Cartao>
                </li>
              )
            })}
          </ul>
        )}
      </Secao>

      <div>
        <button
          type="button"
          onClick={() => setMais((v) => !v)}
          aria-expanded={mais}
          className="flex h-10 items-center gap-1.5 px-1 text-sm font-medium text-muted-foreground outline-none hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50"
        >
          <ChevronDown className={cn('size-4 transition-transform', mais && 'rotate-180')} aria-hidden="true" />
          Data e observação
        </button>
        {mais && (
          <Cartao className="mt-2 space-y-4 p-4">
            <div className="space-y-2">
              <Label htmlFor="venda-data">Data e hora</Label>
              <Input
                id="venda-data"
                type="datetime-local"
                max={agoraIso().slice(0, 16)}
                value={rascunho?.dataHora ?? ''}
                onChange={(e) => carrinho.atualizar(negocioId, { dataHora: e.target.value })}
              />
              <p className="text-sm text-muted-foreground">Deixe em branco para agora. Use para lançar vendas de outro dia.</p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="venda-obs">Observação</Label>
              <Textarea
                id="venda-obs"
                rows={2}
                maxLength={255}
                value={rascunho?.observacao ?? ''}
                onChange={(e) => carrinho.atualizar(negocioId, { observacao: e.target.value })}
              />
            </div>
          </Cartao>
        )}
      </div>

      <BarraAcao>
        <div className="min-w-0 flex-1">
          <p className="text-xs text-muted-foreground">Total</p>
          <p className="numeros truncate text-2xl font-semibold tracking-tight">{formatarMoeda(total)}</p>
        </div>
        <Button size="lg" className="px-6" onClick={finalizar} disabled={registrar.isPending}>
          {registrar.isPending ? <Loader2 className="animate-spin" aria-hidden="true" /> : <ShoppingBasket aria-hidden="true" />}
          Finalizar venda
        </Button>
      </BarraAcao>

      <SeletorCliente
        aberto={escolhendoCliente}
        aoFechar={() => setEscolhendoCliente(false)}
        aoEscolher={(c) => c.id != null && carrinho.atualizar(negocioId, { clienteId: c.id, clienteNome: c.nome ?? '' })}
      />
      <SeletorProduto
        aberto={escolhendoProduto}
        aoFechar={() => setEscolhendoProduto(false)}
        escolhidos={itens.map((i) => i.produtoId)}
        aoEscolher={(p) =>
          p.produtoId != null &&
          (itens.some((i) => i.produtoId === p.produtoId)
            ? carrinho.removerItem(negocioId, p.produtoId)
            : carrinho.adicionarItem(negocioId, { produtoId: p.produtoId, descricao: p.descricao ?? '', unidade: p.unidadeMedida }))
        }
      />
      <ConfirmarAcao
        aberto={limpando}
        aoFechar={() => setLimpando(false)}
        titulo="Começar de novo?"
        descricao="O cliente e os produtos desta venda serão tirados."
        confirmar="Limpar"
        aoConfirmar={() => {
          carrinho.limpar(negocioId)
          setTentou(false)
        }}
      />
    </Pagina>
  )
}
