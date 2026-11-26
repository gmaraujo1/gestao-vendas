import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { Loader2, Trash2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { ConfirmarAcao } from '@/components/ConfirmarAcao'
import { Campo } from '@/components/formulario/Campo'
import { InputNumero, Pilulas, Selecao } from '@/components/formulario/controles'
import { PainelFormulario } from '@/components/formulario/PainelFormulario'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { useAcao } from '@/hooks/use-acao'
import { useCategorias } from '@/hooks/use-cadastros'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio, semConteudo } from '@/lib/api/client'
import { campoNumero, paraCampo, paraNumero } from '@/lib/numeros'
import { UNIDADES, opcoes, type Unidade } from '@/lib/rotulos'
import type { Produto } from '@/pages/cadastros/Produtos'

const esquema = z.object({
  codigo: z.string().regex(/^\d{1,9}$/, 'Use só números'),
  descricao: z.string().trim().min(1, 'Informe o nome').max(128, 'No máximo 128 caracteres'),
  categoriaId: z.string().min(1, 'Escolha a categoria'),
  unidadeMedida: z.enum(['UN', 'CX', 'L']),
  precoMedio: campoNumero({ minimo: 'naoNegativo', casas: 4 }),
  observacao: z.string().max(100, 'No máximo 100 caracteres'),
  ativo: z.boolean(),
})
type Campos = z.infer<typeof esquema>

/** Cadastro e edição de produto (só o ADMIN chega aqui). */
export function FormProduto({ produto, aoFechar }: { produto: Produto | 'novo' | null; aoFechar: () => void }) {
  const negocioId = useNegocioId()
  const novo = produto === 'novo'
  const atual = novo ? null : produto
  const [confirmarExclusao, setConfirmarExclusao] = useState(false)
  const { data: categorias } = useCategorias()

  // sugere o próximo código livre (o maior + 1)
  const proximoCodigo = useQuery({
    queryKey: ['produtos', negocioId, 'proximo-codigo'],
    queryFn: async () => {
      const p = await dados(
        api.GET('/api/produto', {
          params: { header: doNegocio(negocioId), query: { page: 0, size: 1, sort: ['codigo,desc'] } },
        }),
      )
      return (p.conteudo?.[0]?.codigo ?? 0) + 1
    },
    enabled: novo,
  })

  const form = useForm<Campos>({ resolver: zodResolver(esquema) })
  const { errors } = form.formState

  useEffect(() => {
    if (!produto) return
    form.reset(
      novo
        ? {
            codigo: '',
            descricao: '',
            categoriaId: '',
            unidadeMedida: 'UN',
            precoMedio: '',
            observacao: '',
            ativo: true,
          }
        : {
            codigo: String(produto.codigo ?? ''),
            descricao: produto.descricao ?? '',
            categoriaId: String(produto.categoriaId ?? ''),
            unidadeMedida: produto.unidadeMedida ?? 'UN',
            precoMedio: paraCampo(produto.precoMedio),
            observacao: produto.observacao ?? '',
            ativo: produto.ativo ?? true,
          },
    )
  }, [produto, novo, form])

  useEffect(() => {
    if (novo && proximoCodigo.data && !form.getValues('codigo')) form.setValue('codigo', String(proximoCodigo.data))
  }, [novo, proximoCodigo.data, form])

  const invalidar = [
    ['produtos', negocioId],
    ['estoque', negocioId],
  ]
  const salvar = useAcao({
    executar: (c: Campos) => {
      const corpo = {
        descricao: c.descricao.trim(),
        categoriaId: Number(c.categoriaId),
        unidadeMedida: c.unidadeMedida,
        precoMedio: paraNumero(c.precoMedio) ?? 0,
        observacao: c.observacao.trim(),
      }
      return atual?.id != null
        ? dados(
            api.PATCH('/api/produto/{id}', {
              params: { path: { id: atual.id }, header: doNegocio(negocioId) },
              body: { ...corpo, ativo: c.ativo },
            }),
          )
        : dados(
            api.POST('/api/produto', {
              params: { header: doNegocio(negocioId) },
              body: { ...corpo, codigo: Number(c.codigo) },
            }),
          )
    },
    sucesso: atual ? 'Produto atualizado' : 'Produto cadastrado',
    invalidar,
    aoConcluir: aoFechar,
  })

  const excluir = useAcao({
    executar: (id: number) =>
      semConteudo(api.DELETE('/api/produto/{id}', { params: { path: { id }, header: doNegocio(negocioId) } })),
    sucesso: 'Produto excluído',
    invalidar,
    aoConcluir: aoFechar,
  })

  const categoriaEscolhida = useWatch({ control: form.control, name: 'categoriaId' })
  const categoriasVisiveis = categorias?.filter((c) => c.ativo || String(c.id) === categoriaEscolhida) ?? []

  return (
    <PainelFormulario
      aberto={produto != null}
      aoFechar={aoFechar}
      titulo={novo ? 'Novo produto' : 'Editar produto'}
      descricao={novo ? 'O custo médio é o quanto o produto custa para vocês (base do lucro).' : undefined}
      rodape={
        <div className="flex gap-2">
          {atual?.id != null && (
            <Button
              type="button"
              variant="destructive"
              size="icon-lg"
              aria-label="Excluir produto"
              onClick={() => setConfirmarExclusao(true)}
            >
              <Trash2 aria-hidden="true" />
            </Button>
          )}
          <Button type="submit" form="form-produto" size="lg" className="flex-1" disabled={salvar.isPending}>
            {salvar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
            Salvar
          </Button>
        </div>
      }
    >
      <form id="form-produto" noValidate onSubmit={form.handleSubmit((c) => salvar.mutate(c))} className="space-y-5">
        <div className="grid grid-cols-[7rem_1fr] gap-3">
          <Campo rotulo="Código" erro={errors.codigo?.message}>
            {(p) => (
              <Input {...p} inputMode="numeric" disabled={!novo} className="numeros" {...form.register('codigo')} />
            )}
          </Campo>
          <Campo rotulo="Nome" erro={errors.descricao?.message}>
            {(p) => (
              <Input {...p} autoFocus={novo} placeholder="Ex.: Leite integral 1L" {...form.register('descricao')} />
            )}
          </Campo>
        </div>

        <Campo rotulo="Categoria" erro={errors.categoriaId?.message}>
          {(p) => (
            <Controller
              control={form.control}
              name="categoriaId"
              render={({ field }) => (
                <Selecao {...p} {...field} value={field.value ?? ''}>
                  <option value="">Escolha…</option>
                  {categoriasVisiveis.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.nome}
                    </option>
                  ))}
                </Selecao>
              )}
            />
          )}
        </Campo>

        <div className="space-y-2">
          <span className="text-sm font-medium">Unidade</span>
          <Controller
            control={form.control}
            name="unidadeMedida"
            render={({ field }) => (
              <Pilulas<Unidade>
                rotulo="Unidade"
                opcoes={opcoes(UNIDADES)}
                valor={field.value}
                aoMudar={field.onChange}
              />
            )}
          />
        </div>

        <Campo rotulo="Custo médio" erro={errors.precoMedio?.message} dica="Quanto vocês pagam por unidade.">
          {(p) => <InputNumero {...p} prefixo="R$" placeholder="0,00" {...form.register('precoMedio')} />}
        </Campo>

        <Campo rotulo="Observação" opcional erro={errors.observacao?.message}>
          {(p) => <Textarea {...p} rows={2} {...form.register('observacao')} />}
        </Campo>

        {!novo && (
          <Controller
            control={form.control}
            name="ativo"
            render={({ field }) => (
              <label className="flex items-center justify-between gap-4 rounded-xl border bg-card p-4">
                <span>
                  <span className="block font-medium">Produto ativo</span>
                  <span className="block text-sm text-muted-foreground">Inativo não aparece na hora de vender.</span>
                </span>
                <Switch checked={field.value} onCheckedChange={field.onChange} />
              </label>
            )}
          />
        )}
      </form>

      <ConfirmarAcao
        aberto={confirmarExclusao}
        aoFechar={() => setConfirmarExclusao(false)}
        titulo="Excluir este produto?"
        descricao="Produtos que já tiveram venda, perda ou ajuste não podem ser excluídos: nesse caso, desative."
        confirmar="Excluir"
        aoConfirmar={() => atual?.id != null && excluir.mutate(atual.id)}
      />
    </PainelFormulario>
  )
}
