import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { z } from 'zod'
import { Campo } from '@/components/formulario/Campo'
import { PainelFormulario } from '@/components/formulario/PainelFormulario'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { useAcao } from '@/hooks/use-acao'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio } from '@/lib/api/client'
import type { Cliente } from '@/pages/cadastros/Clientes'

const texto = (max: number) => z.string().max(max, `No máximo ${max} caracteres`)
const esquema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome').max(120, 'No máximo 120 caracteres'),
  telefone: texto(20),
  endereco: texto(200),
  cidade: texto(80),
  observacao: texto(255),
  ativo: z.boolean(),
})
type Campos = z.infer<typeof esquema>

/** Cadastro rápido de cliente: só o nome é obrigatório. O funcionário também usa. */
export function FormCliente({
  cliente,
  aoFechar,
  aoSalvar,
  nomeInicial,
}: {
  cliente: Cliente | 'novo' | null
  aoFechar: () => void
  aoSalvar?: (c: Cliente) => void
  nomeInicial?: string
}) {
  const negocioId = useNegocioId()
  const atual = cliente === 'novo' ? null : cliente
  const form = useForm<Campos>({ resolver: zodResolver(esquema) })

  useEffect(() => {
    if (!cliente) return
    form.reset({
      nome: atual?.nome ?? nomeInicial ?? '',
      telefone: atual?.telefone ?? '',
      endereco: atual?.endereco ?? '',
      cidade: atual?.cidade ?? '',
      observacao: atual?.observacao ?? '',
      ativo: atual?.ativo ?? true,
    })
  }, [cliente, atual, nomeInicial, form])

  const salvar = useAcao({
    executar: (c: Campos) => {
      const corpo = {
        nome: c.nome.trim(),
        telefone: c.telefone.trim(),
        endereco: c.endereco.trim(),
        cidade: c.cidade.trim(),
        observacao: c.observacao.trim(),
      }
      return atual?.id != null
        ? dados(api.PATCH('/api/cliente/{id}', { params: { path: { id: atual.id }, header: doNegocio(negocioId) }, body: { ...corpo, ativo: c.ativo } }))
        : dados(api.POST('/api/cliente', { params: { header: doNegocio(negocioId) }, body: corpo }))
    },
    sucesso: atual ? 'Cliente atualizado' : 'Cliente cadastrado',
    invalidar: [['clientes', negocioId]],
    aoConcluir: (c) => {
      aoFechar()
      aoSalvar?.(c)
    },
  })

  const { errors } = form.formState
  return (
    <PainelFormulario
      aberto={cliente != null}
      aoFechar={aoFechar}
      titulo={atual ? 'Editar cliente' : 'Novo cliente'}
      descricao={atual ? undefined : 'Só o nome é obrigatório; o resto ajuda a achar e a falar com o cliente.'}
      rodape={
        <Button type="submit" form="form-cliente" size="lg" className="w-full" disabled={salvar.isPending}>
          {salvar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
          Salvar
        </Button>
      }
    >
      <form id="form-cliente" noValidate onSubmit={form.handleSubmit((c) => salvar.mutate(c))} className="space-y-5">
        <Campo rotulo="Nome" erro={errors.nome?.message}>
          {(p) => <Input {...p} autoFocus autoCapitalize="words" placeholder="Ex.: Marcelo da padaria" {...form.register('nome')} />}
        </Campo>
        <Campo rotulo="Telefone / WhatsApp" opcional erro={errors.telefone?.message}>
          {(p) => <Input {...p} type="tel" inputMode="tel" autoComplete="tel" placeholder="(11) 98765-4321" {...form.register('telefone')} />}
        </Campo>
        <Campo rotulo="Endereço" opcional erro={errors.endereco?.message}>
          {(p) => <Input {...p} autoComplete="street-address" {...form.register('endereco')} />}
        </Campo>
        <Campo rotulo="Cidade" opcional erro={errors.cidade?.message}>
          {(p) => <Input {...p} autoCapitalize="words" {...form.register('cidade')} />}
        </Campo>
        <Campo rotulo="Observação" opcional erro={errors.observacao?.message}>
          {(p) => <Textarea {...p} rows={2} placeholder="Ex.: entrega às terças" {...form.register('observacao')} />}
        </Campo>
        {atual && (
          <Controller
            control={form.control}
            name="ativo"
            render={({ field }) => (
              <label className="flex items-center justify-between gap-4 rounded-xl border bg-card p-4">
                <span>
                  <span className="block font-medium">Cliente ativo</span>
                  <span className="block text-sm text-muted-foreground">Inativo não aparece na hora de vender.</span>
                </span>
                <Switch checked={field.value} onCheckedChange={field.onChange} />
              </label>
            )}
          />
        )}
      </form>
    </PainelFormulario>
  )
}
