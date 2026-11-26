import { zodResolver } from '@hookform/resolvers/zod'
import { useInfiniteQuery } from '@tanstack/react-query'
import { ClipboardCheck, History, Loader2 } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useParams } from 'react-router'
import { z } from 'zod'
import { Campo } from '@/components/formulario/Campo'
import { InputNumero } from '@/components/formulario/controles'
import { PainelFormulario } from '@/components/formulario/PainelFormulario'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { Cartao, Pagina, Secao } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { useAcao } from '@/hooks/use-acao'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA } from '@/hooks/use-paginado'
import { useSessao } from '@/hooks/use-sessao'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { horaDe, rotuloDia } from '@/lib/datas'
import { formatarQuantidade } from '@/lib/formatadores'
import { campoNumero, paraNumero } from '@/lib/numeros'
import { MOTIVOS_PERDA, TIPOS_MOVIMENTACAO } from '@/lib/rotulos'
import { cn } from '@/lib/utils'

type Movimentacao = components['schemas']['MovimentacaoResponseDTO']

function origem(m: Movimentacao) {
  if (m.vendaId != null) return { para: `/vendas/${m.vendaId}`, texto: `venda #${m.vendaId}` }
  if (m.trocaId != null) return { para: `/trocas/${m.trocaId}`, texto: `troca #${m.trocaId}` }
  return null
}

/** Saldo e extrato (entradas e saídas) de um produto; o ADMIN informa a contagem real. */
export function ExtratoProduto() {
  const produtoId = Number(useParams().id)
  const negocioId = useNegocioId()
  const { ehAdmin } = useSessao()
  const [ajustando, setAjustando] = useState(false)

  const extrato = useInfiniteQuery({
    queryKey: ['estoque', negocioId, 'extrato', produtoId],
    queryFn: ({ pageParam }) =>
      dados(
        api.GET('/api/estoque/produto/{produtoId}', {
          params: { path: { produtoId }, header: doNegocio(negocioId), query: { page: pageParam, size: TAMANHO_PAGINA } },
        }),
      ),
    initialPageParam: 0,
    getNextPageParam: (u) => {
      const p = u.movimentacoes
      return (p?.pagina ?? 0) + 1 < (p?.totalPaginas ?? 0) ? (p?.pagina ?? 0) + 1 : undefined
    },
  })
  const primeira = extrato.data?.pages[0]
  const movimentacoes = useMemo(() => extrato.data?.pages.flatMap((p) => p.movimentacoes?.conteudo ?? []) ?? [], [extrato.data])
  const saldo = primeira?.saldo ?? 0

  return (
    <Pagina voltar="/estoque" titulo={primeira?.descricao ?? <Skeleton className="h-8 w-48" />}>
      <Cartao className="overflow-hidden">
        <div className="faixa-marca h-1" aria-hidden="true" />
        <div className="flex items-end justify-between gap-4 p-5">
          <div>
            <p className="text-sm text-muted-foreground">Em estoque agora</p>
            {primeira ? (
              <p className={cn('numeros text-4xl font-semibold tracking-tight', saldo < 0 && 'text-destructive')}>{formatarQuantidade(saldo)}</p>
            ) : (
              <Skeleton className="mt-1 h-10 w-24" />
            )}
            {saldo < 0 && <p className="mt-1 text-sm text-destructive">Saldo negativo: vendeu mais do que estava lançado.</p>}
          </div>
          {ehAdmin && (
            <Button variant="outline" onClick={() => setAjustando(true)} disabled={!primeira}>
              <ClipboardCheck aria-hidden="true" /> Informar estoque
            </Button>
          )}
        </div>
      </Cartao>

      <Secao titulo="Entradas e saídas">
        <EstadosLista
          carregando={extrato.isPending}
          erro={extrato.error}
          vazio={movimentacoes.length === 0}
          tentarDeNovo={() => void extrato.refetch()}
          iconeVazio={History}
          tituloVazio="Nenhuma movimentação ainda"
          descricaoVazio={ehAdmin ? 'Use "Informar estoque" para lançar a contagem inicial.' : undefined}
        >
          <Cartao className="divide-y overflow-hidden">
            {movimentacoes.map((m) => {
              const q = m.quantidade ?? 0
              const o = origem(m)
              return (
                <div key={m.id} className="flex items-center gap-3 px-4 py-3.5">
                  <div className="min-w-0 flex-1">
                    <p className="font-medium">
                      {m.tipo ? TIPOS_MOVIMENTACAO[m.tipo] : ''}
                      {m.motivoPerda && <span className="font-normal text-muted-foreground"> · {MOTIVOS_PERDA[m.motivoPerda]}</span>}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {rotuloDia(m.dataHora)} {horaDe(m.dataHora)}
                      {o && (
                        <>
                          {' · '}
                          <Link to={o.para} className="underline underline-offset-4">
                            {o.texto}
                          </Link>
                        </>
                      )}
                    </p>
                    {m.observacao && <p className="mt-0.5 text-sm text-pretty text-muted-foreground">{m.observacao}</p>}
                  </div>
                  <span className={cn('numeros font-semibold', q > 0 ? 'text-sucesso' : 'text-foreground')}>
                    {q > 0 ? '+' : '−'}
                    {formatarQuantidade(Math.abs(q))}
                  </span>
                </div>
              )
            })}
          </Cartao>
          <CarregarMais
            temMais={extrato.hasNextPage}
            carregando={extrato.isFetchingNextPage}
            aoCarregar={() => void extrato.fetchNextPage()}
            exibidos={movimentacoes.length}
            total={primeira?.movimentacoes?.totalElementos ?? 0}
          />
        </EstadosLista>
      </Secao>

      <FormAjuste aberto={ajustando} aoFechar={() => setAjustando(false)} produtoId={produtoId} saldoAtual={saldo} />
    </Pagina>
  )
}

const esquema = z.object({ contagem: campoNumero({ minimo: 'naoNegativo', casas: 3 }), observacao: z.string().max(255) })
type Campos = z.infer<typeof esquema>

/** "Informar estoque": a contagem real; o sistema lança só a diferença. */
function FormAjuste({ aberto, aoFechar, produtoId, saldoAtual }: { aberto: boolean; aoFechar: () => void; produtoId: number; saldoAtual: number }) {
  const negocioId = useNegocioId()
  const form = useForm<Campos>({ resolver: zodResolver(esquema) })
  useEffect(() => {
    if (aberto) form.reset({ contagem: '', observacao: '' })
  }, [aberto, form])
  const contagem = paraNumero(useWatch({ control: form.control, name: 'contagem' }))
  const diferenca = contagem == null ? null : Math.round((contagem - saldoAtual) * 1000) / 1000

  const ajustar = useAcao({
    executar: (c: Campos) =>
      dados(
        api.POST('/api/estoque/ajuste', {
          params: { header: doNegocio(negocioId) },
          body: { produtoId, quantidadeContada: paraNumero(c.contagem)!, observacao: c.observacao.trim() || undefined },
        }),
      ),
    sucesso: (r) => (r.diferenca === 0 ? 'Contagem confere: nada mudou' : 'Estoque ajustado'),
    invalidar: [['estoque', negocioId]],
    aoConcluir: aoFechar,
  })

  return (
    <PainelFormulario
      aberto={aberto}
      aoFechar={aoFechar}
      titulo="Informar estoque"
      descricao="Conte o que tem de verdade. O sistema lança só a diferença."
      rodape={
        <Button type="submit" form="form-ajuste" size="lg" className="w-full" disabled={ajustar.isPending}>
          {ajustar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
          Confirmar contagem
        </Button>
      }
    >
      <form id="form-ajuste" noValidate onSubmit={form.handleSubmit((c) => ajustar.mutate(c))} className="space-y-5">
        <Campo rotulo="Quantidade contada" erro={form.formState.errors.contagem?.message}>
          {(p) => <InputNumero {...p} autoFocus {...form.register('contagem')} />}
        </Campo>
        <div className="grid grid-cols-2 gap-3 text-sm">
          <div className="rounded-xl bg-muted/70 p-3">
            <p className="text-muted-foreground">No sistema</p>
            <p className="numeros text-lg font-semibold">{formatarQuantidade(saldoAtual)}</p>
          </div>
          <div className="rounded-xl bg-muted/70 p-3">
            <p className="text-muted-foreground">Diferença</p>
            <p className={cn('numeros text-lg font-semibold', diferenca != null && diferenca < 0 && 'text-destructive', diferenca != null && diferenca > 0 && 'text-sucesso')}>
              {diferenca == null ? '—' : `${diferenca > 0 ? '+' : ''}${formatarQuantidade(diferenca)}`}
            </p>
          </div>
        </div>
        <Campo rotulo="Observação" opcional>
          {(p) => <Textarea {...p} rows={2} maxLength={255} placeholder="Ex.: contagem de sexta" {...form.register('observacao')} />}
        </Campo>
      </form>
    </PainelFormulario>
  )
}
