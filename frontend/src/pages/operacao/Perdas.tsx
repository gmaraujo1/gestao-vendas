import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, PackageX, Plus } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { Link, useSearchParams } from 'react-router'
import { z } from 'zod'
import { Etiqueta } from '@/components/Etiqueta'
import { Campo } from '@/components/formulario/Campo'
import { InputNumero, Pilulas } from '@/components/formulario/controles'
import { PainelFormulario } from '@/components/formulario/PainelFormulario'
import { CarregarMais, EstadosLista } from '@/components/lista/EstadosLista'
import { FiltroPeriodo } from '@/components/lista/FiltroPeriodo'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Pagina } from '@/components/pagina/Pagina'
import { SeletorProduto, type ProdutoComSaldo } from '@/components/seletores/SeletorProduto'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { useAcao } from '@/hooks/use-acao'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { TAMANHO_PAGINA, usePaginado } from '@/hooks/use-paginado'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { intervaloDoAtalho, rotuloDia, horaDe, type AtalhoPeriodo } from '@/lib/datas'
import { agoraIso, formatarDataHora, formatarMoeda, formatarQuantidade } from '@/lib/formatadores'
import { campoNumero, paraNumero } from '@/lib/numeros'
import { MOTIVOS_PERDA, opcoes, type MotivoPerda } from '@/lib/rotulos'
import { colunasDe } from '@/lib/tabela'

type Movimentacao = components['schemas']['MovimentacaoResponseDTO']

const custo = (m: Movimentacao) => Math.abs(m.quantidade ?? 0) * (m.custoUnitario ?? 0)

const h = colunasDe<Movimentacao>()
const COLUNAS = [
  h.accessor('dataHora', { header: 'Data', cell: (c) => <span className="numeros">{formatarDataHora(c.getValue())}</span> }),
  h.accessor('produtoDescricao', { header: 'Produto', cell: (c) => <span className="font-medium">{c.getValue()}</span> }),
  h.accessor('quantidade', { header: 'Quantidade', meta: { direita: true }, cell: (c) => formatarQuantidade(Math.abs(c.getValue() ?? 0)) }),
  h.accessor('motivoPerda', {
    header: 'Motivo',
    cell: (c) => (
      <span className="flex items-center gap-2">
        <Etiqueta tom="perigo">{c.getValue() ? MOTIVOS_PERDA[c.getValue()!] : ''}</Etiqueta>
        {c.row.original.trocaId != null && <Etiqueta>Troca #{c.row.original.trocaId}</Etiqueta>}
      </span>
    ),
  }),
  h.display({ id: 'custo', header: 'Custo', meta: { direita: true }, cell: (c) => formatarMoeda(custo(c.row.original)) }),
]

/** Perdas (vencido, furado...): as avulsas e as que vieram de trocas. */
export function Perdas() {
  const negocioId = useNegocioId()
  const [params, setParams] = useSearchParams()
  const [periodo, setPeriodo] = useState<AtalhoPeriodo>('mes')
  const filtros = intervaloDoAtalho(periodo)
  const lista = usePaginado(['perdas', negocioId, filtros], (page) =>
    dados(api.GET('/api/perda', { params: { header: doNegocio(negocioId), query: { ...filtros, page, size: TAMANHO_PAGINA } } })),
  )
  const totalCusto = lista.itens.reduce((s, m) => s + custo(m), 0)
  const nova = params.get('nova') === '1'
  const abrirNova = (v: boolean) => setParams(v ? { nova: '1' } : {}, { replace: true })

  return (
    <Pagina
      titulo="Perdas"
      subtitulo={lista.isSuccess && lista.total > 0 ? `${lista.total} no período · ${formatarMoeda(totalCusto)}${lista.hasNextPage ? '+' : ''} em custo` : undefined}
      acoes={
        <Button onClick={() => abrirNova(true)}>
          <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Registrar perda</span>
        </Button>
      }
    >
      <FiltroPeriodo valor={periodo} aoMudar={setPeriodo} />
      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={lista.itens.length === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={PackageX}
        tituloVazio="Nenhuma perda no período"
        descricaoVazio="Que bom! Produto vencido, furado ou avariado se registra aqui."
      >
        <TabelaDados
          rotulo="Perdas"
          dados={lista.itens}
          colunas={COLUNAS}
          chave={(m) => m.id ?? 0}
          cartao={(m) => (
            <div className="flex items-start gap-3">
              <div className="min-w-0 flex-1">
                <p className="font-medium text-pretty">{m.produtoDescricao}</p>
                <p className="text-sm text-muted-foreground">
                  {rotuloDia(m.dataHora)} {horaDe(m.dataHora)}
                  {m.trocaId != null && (
                    <>
                      {' · '}
                      <Link to={`/trocas/${m.trocaId}`} className="underline underline-offset-4">
                        troca #{m.trocaId}
                      </Link>
                    </>
                  )}
                </p>
                <div className="mt-2">
                  <Etiqueta tom="perigo">{m.motivoPerda ? MOTIVOS_PERDA[m.motivoPerda] : ''}</Etiqueta>
                </div>
              </div>
              <div className="text-right">
                <p className="numeros font-semibold">−{formatarQuantidade(Math.abs(m.quantidade ?? 0))}</p>
                <p className="numeros text-sm text-muted-foreground">{formatarMoeda(custo(m))}</p>
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
      <FormPerda aberto={nova} aoFechar={() => abrirNova(false)} />
    </Pagina>
  )
}

const esquema = z.object({
  quantidade: campoNumero({ casas: 3 }),
  motivo: z.enum(['VENCIDO', 'FURADO', 'AVARIADO', 'OUTRO'], { message: 'Escolha o motivo' }),
  observacao: z.string().max(255),
  dataHora: z.string(),
})
type Campos = z.infer<typeof esquema>

function FormPerda({ aberto, aoFechar }: { aberto: boolean; aoFechar: () => void }) {
  const negocioId = useNegocioId()
  const [produto, setProduto] = useState<ProdutoComSaldo | null>(null)
  const [escolhendo, setEscolhendo] = useState(false)
  const [semProduto, setSemProduto] = useState(false)
  const form = useForm<Campos>({ resolver: zodResolver(esquema) })

  useEffect(() => {
    if (aberto) form.reset({ quantidade: '1', motivo: undefined, observacao: '', dataHora: '' })
  }, [aberto, form])

  function fechar() {
    setProduto(null)
    setSemProduto(false)
    aoFechar()
  }

  const registrar = useAcao({
    executar: (c: Campos) =>
      dados(
        api.POST('/api/perda', {
          params: { header: doNegocio(negocioId) },
          body: {
            produtoId: produto!.produtoId!,
            quantidade: paraNumero(c.quantidade)!,
            motivo: c.motivo,
            observacao: c.observacao.trim() || undefined,
            dataHora: c.dataHora ? `${c.dataHora}:00` : undefined,
          },
        }),
      ),
    sucesso: 'Perda registrada',
    invalidar: [['perdas', negocioId], ['estoque', negocioId], ['dashboard', negocioId]],
    aoConcluir: fechar,
  })

  const { errors } = form.formState
  return (
    <>
      <PainelFormulario
        aberto={aberto && !escolhendo}
        aoFechar={fechar}
        titulo="Registrar perda"
        descricao="O produto sai do estoque e o custo entra nos relatórios."
        rodape={
          <Button type="submit" form="form-perda" size="lg" className="w-full" disabled={registrar.isPending}>
            {registrar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
            Registrar perda
          </Button>
        }
      >
        <form
          id="form-perda"
          noValidate
          onSubmit={form.handleSubmit((c) => {
            if (!produto) return setSemProduto(true)
            registrar.mutate(c)
          })}
          className="space-y-5"
        >
          <div className="space-y-2">
            <span className="text-sm font-medium">Produto</span>
            <button
              type="button"
              onClick={() => setEscolhendo(true)}
              className={`flex min-h-14 w-full items-center justify-between gap-3 rounded-xl border bg-card px-4 py-3 text-left outline-none focus-visible:ring-3 focus-visible:ring-ring/50 ${semProduto && !produto ? 'border-destructive' : ''}`}
            >
              {produto ? (
                <span>
                  <span className="block font-medium">{produto.descricao}</span>
                  <span className="numeros block text-sm text-muted-foreground">
                    Em estoque: {formatarQuantidade(produto.saldo)} {produto.unidadeMedida}
                  </span>
                </span>
              ) : (
                <span className="text-muted-foreground">Escolher produto…</span>
              )}
            </button>
            {semProduto && !produto && <p className="text-sm text-destructive">Escolha o produto</p>}
          </div>
          <Campo rotulo="Quantidade perdida" erro={errors.quantidade?.message}>
            {(p) => <InputNumero {...p} sufixo={produto?.unidadeMedida} {...form.register('quantidade')} />}
          </Campo>
          <div className="space-y-2">
            <span className="text-sm font-medium">Motivo</span>
            <Controller
              control={form.control}
              name="motivo"
              render={({ field }) => (
                <Pilulas<MotivoPerda> rotulo="Motivo" opcoes={opcoes(MOTIVOS_PERDA)} valor={field.value} aoMudar={field.onChange} />
              )}
            />
            {errors.motivo && <p className="text-sm text-destructive">{errors.motivo.message}</p>}
          </div>
          <Campo rotulo="Observação" opcional>
            {(p) => <Textarea {...p} rows={2} maxLength={255} {...form.register('observacao')} />}
          </Campo>
          <Campo rotulo="Data e hora" opcional dica="Em branco = agora.">
            {(p) => <Input {...p} type="datetime-local" max={agoraIso().slice(0, 16)} {...form.register('dataHora')} />}
          </Campo>
        </form>
      </PainelFormulario>
      <SeletorProduto
        titulo="Qual produto?"
        aberto={escolhendo}
        aoFechar={() => setEscolhendo(false)}
        escolhidos={produto?.produtoId != null ? [produto.produtoId] : []}
        aoEscolher={(p) => {
          setProduto(p)
          setEscolhendo(false)
        }}
      />
    </>
  )
}
