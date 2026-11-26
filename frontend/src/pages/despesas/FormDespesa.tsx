import { zodResolver } from '@hookform/resolvers/zod'
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
import { useAcao } from '@/hooks/use-acao'
import { useVeiculos } from '@/hooks/use-cadastros'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio, semConteudo } from '@/lib/api/client'
import { hojeIso } from '@/lib/formatadores'
import { campoNumero, paraCampo, paraNumero } from '@/lib/numeros'
import { TIPOS_DESPESA, opcoes, type TipoDespesa } from '@/lib/rotulos'
import type { Despesa } from '@/pages/despesas/Despesas'

const esquema = z.object({
  valor: campoNumero(),
  tipo: z.enum(['GASOLINA', 'PEDAGIO', 'PNEU', 'MECANICA', 'OUTROS'], { message: 'Escolha o tipo' }),
  veiculoId: z.string(),
  data: z.string().min(1, 'Informe a data'),
  descricao: z.string().max(255),
})
type Campos = z.infer<typeof esquema>

/** Lançamento de despesa (todos) e edição/exclusão (só ADMIN). */
export function FormDespesa({ despesa, aoFechar }: { despesa: Despesa | 'nova' | null; aoFechar: () => void }) {
  const negocioId = useNegocioId()
  const atual = despesa === 'nova' ? null : despesa
  const [confirmar, setConfirmar] = useState(false)
  const { data: veiculos } = useVeiculos()
  const form = useForm<Campos>({ resolver: zodResolver(esquema) })

  useEffect(() => {
    if (!despesa) return
    form.reset({
      valor: paraCampo(atual?.valor),
      tipo: atual?.tipo,
      veiculoId: atual?.veiculoId != null ? String(atual.veiculoId) : '',
      data: atual?.data ?? hojeIso(),
      descricao: atual?.descricao ?? '',
    })
  }, [despesa, atual, form])

  const invalidar = [
    ['despesas', negocioId],
    ['dashboard', negocioId],
  ]
  const salvar = useAcao({
    executar: (c: Campos) => {
      const veiculoId = c.veiculoId ? Number(c.veiculoId) : undefined
      const corpo = { valor: paraNumero(c.valor)!, tipo: c.tipo, data: c.data, descricao: c.descricao.trim() }
      return atual?.id != null
        ? dados(
            api.PATCH('/api/despesa/{id}', {
              params: { path: { id: atual.id }, header: doNegocio(negocioId) },
              body: { ...corpo, veiculoId, semVeiculo: veiculoId == null },
            }),
          )
        : dados(
            api.POST('/api/despesa', {
              params: { header: doNegocio(negocioId) },
              body: { ...corpo, veiculoId, descricao: corpo.descricao || undefined },
            }),
          )
    },
    sucesso: atual ? 'Despesa atualizada' : 'Despesa lançada',
    invalidar,
    aoConcluir: aoFechar,
  })
  const excluir = useAcao({
    executar: (id: number) =>
      semConteudo(api.DELETE('/api/despesa/{id}', { params: { path: { id }, header: doNegocio(negocioId) } })),
    sucesso: 'Despesa excluída',
    invalidar,
    aoConcluir: aoFechar,
  })

  const { errors } = form.formState
  const veiculoAtual = useWatch({ control: form.control, name: 'veiculoId' })
  return (
    <PainelFormulario
      aberto={despesa != null}
      aoFechar={aoFechar}
      titulo={atual ? 'Editar despesa' : 'Nova despesa'}
      rodape={
        <div className="flex gap-2">
          {atual?.id != null && (
            <Button
              type="button"
              variant="destructive"
              size="icon-lg"
              aria-label="Excluir despesa"
              onClick={() => setConfirmar(true)}
            >
              <Trash2 aria-hidden="true" />
            </Button>
          )}
          <Button type="submit" form="form-despesa" size="lg" className="flex-1" disabled={salvar.isPending}>
            {salvar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
            Salvar
          </Button>
        </div>
      }
    >
      <form id="form-despesa" noValidate onSubmit={form.handleSubmit((c) => salvar.mutate(c))} className="space-y-5">
        <Campo rotulo="Valor" erro={errors.valor?.message}>
          {(p) => (
            <InputNumero
              {...p}
              autoFocus={!atual}
              prefixo="R$"
              placeholder="0,00"
              className="h-14 text-xl font-semibold"
              {...form.register('valor')}
            />
          )}
        </Campo>
        <div className="space-y-2">
          <span className="text-sm font-medium">Tipo</span>
          <Controller
            control={form.control}
            name="tipo"
            render={({ field }) => (
              <Pilulas<TipoDespesa>
                rotulo="Tipo"
                opcoes={opcoes(TIPOS_DESPESA)}
                valor={field.value}
                aoMudar={field.onChange}
              />
            )}
          />
          {errors.tipo && <p className="text-sm text-destructive">{errors.tipo.message}</p>}
        </div>
        <Campo rotulo="Veículo" opcional>
          {(p) => (
            <Controller
              control={form.control}
              name="veiculoId"
              render={({ field }) => (
                <Selecao {...p} {...field} value={field.value ?? ''}>
                  <option value="">Sem veículo</option>
                  {veiculos
                    ?.filter((v) => v.ativo || String(v.id) === veiculoAtual)
                    .map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.descricao}
                        {v.placa ? ` (${v.placa})` : ''}
                      </option>
                    ))}
                </Selecao>
              )}
            />
          )}
        </Campo>
        <Campo rotulo="Data" erro={errors.data?.message}>
          {(p) => <Input {...p} type="date" max={hojeIso()} {...form.register('data')} />}
        </Campo>
        <Campo rotulo="Descrição" opcional>
          {(p) => <Input {...p} maxLength={255} placeholder="Ex.: posto da estrada" {...form.register('descricao')} />}
        </Campo>
      </form>
      <ConfirmarAcao
        aberto={confirmar}
        aoFechar={() => setConfirmar(false)}
        titulo="Excluir esta despesa?"
        confirmar="Excluir"
        aoConfirmar={() => atual?.id != null && excluir.mutate(atual.id)}
      />
    </PainelFormulario>
  )
}
