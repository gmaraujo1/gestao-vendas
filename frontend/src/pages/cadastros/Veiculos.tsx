import { zodResolver } from '@hookform/resolvers/zod'
import { Car, Loader2, Plus, Trash2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { z } from 'zod'
import { ConfirmarAcao } from '@/components/ConfirmarAcao'
import { Etiqueta } from '@/components/Etiqueta'
import { Campo } from '@/components/formulario/Campo'
import { PainelFormulario } from '@/components/formulario/PainelFormulario'
import { EstadosLista } from '@/components/lista/EstadosLista'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Pagina } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { useAcao } from '@/hooks/use-acao'
import { useVeiculos } from '@/hooks/use-cadastros'
import { api, dados, semConteudo } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { colunasDe } from '@/lib/tabela'

type Veiculo = components['schemas']['VeiculoResponseDTO']

const h = colunasDe<Veiculo>()
const COLUNAS = [
  h.accessor('descricao', { header: 'Veículo', cell: (c) => <span className="font-medium">{c.getValue()}</span> }),
  h.accessor('placa', { header: 'Placa', cell: (c) => <span className="numeros tracking-wide">{c.getValue() ?? '—'}</span> }),
  h.accessor('ativo', { header: 'Situação', cell: (c) => (c.getValue() ? <Etiqueta tom="sucesso">Ativo</Etiqueta> : <Etiqueta>Inativo</Etiqueta>) }),
]

/** Veículos (os mesmos nos dois negócios). Só o ADMIN cadastra. */
export function Veiculos() {
  const lista = useVeiculos()
  const [editando, setEditando] = useState<Veiculo | 'novo' | null>(null)
  return (
    <Pagina
      titulo="Veículos"
      subtitulo="Usados para separar as despesas (gasolina, pedágio, pneus...)."
      acoes={
        <Button onClick={() => setEditando('novo')}>
          <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Novo veículo</span>
        </Button>
      }
    >
      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={(lista.data?.length ?? 0) === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={Car}
        tituloVazio="Nenhum veículo"
      >
        <TabelaDados
          rotulo="Veículos"
          dados={lista.data ?? []}
          colunas={COLUNAS}
          chave={(v) => v.id ?? 0}
          aoAbrir={setEditando}
          cartao={(v) => (
            <div className="flex items-center gap-3">
              <span className="grid size-10 place-items-center rounded-xl bg-accent text-accent-foreground">
                <Car className="size-5" aria-hidden="true" />
              </span>
              <div className="min-w-0 flex-1">
                <p className="font-medium">{v.descricao}</p>
                <p className="numeros text-sm tracking-wide text-muted-foreground">{v.placa ?? 'Sem placa'}</p>
              </div>
              {!v.ativo && <Etiqueta>Inativo</Etiqueta>}
            </div>
          )}
        />
      </EstadosLista>
      <FormVeiculo veiculo={editando} aoFechar={() => setEditando(null)} />
    </Pagina>
  )
}

const esquema = z.object({
  descricao: z.string().trim().min(1, 'Informe o nome').max(60, 'No máximo 60 caracteres'),
  placa: z.string().trim().max(10, 'Placa inválida'),
  ativo: z.boolean(),
})
type Campos = z.infer<typeof esquema>

function FormVeiculo({ veiculo, aoFechar }: { veiculo: Veiculo | 'novo' | null; aoFechar: () => void }) {
  const atual = veiculo === 'novo' ? null : veiculo
  const [confirmar, setConfirmar] = useState(false)
  const form = useForm<Campos>({ resolver: zodResolver(esquema) })

  useEffect(() => {
    if (veiculo) form.reset({ descricao: atual?.descricao ?? '', placa: atual?.placa ?? '', ativo: atual?.ativo ?? true })
  }, [veiculo, atual, form])

  const invalidar = [['veiculos']]
  const salvar = useAcao({
    executar: (c: Campos) =>
      atual?.id != null
        ? dados(api.PATCH('/api/veiculo/{id}', { params: { path: { id: atual.id } }, body: { descricao: c.descricao.trim(), placa: c.placa.trim(), ativo: c.ativo } }))
        : dados(api.POST('/api/veiculo', { body: { descricao: c.descricao.trim(), placa: c.placa.trim() || undefined } })),
    sucesso: atual ? 'Veículo atualizado' : 'Veículo cadastrado',
    invalidar,
    aoConcluir: aoFechar,
  })
  const excluir = useAcao({
    executar: (id: number) => semConteudo(api.DELETE('/api/veiculo/{id}', { params: { path: { id } } })),
    sucesso: 'Veículo excluído',
    invalidar,
    aoConcluir: aoFechar,
  })

  return (
    <PainelFormulario
      aberto={veiculo != null}
      aoFechar={aoFechar}
      titulo={atual ? 'Editar veículo' : 'Novo veículo'}
      rodape={
        <div className="flex gap-2">
          {atual?.id != null && (
            <Button type="button" variant="destructive" size="icon-lg" aria-label="Excluir veículo" onClick={() => setConfirmar(true)}>
              <Trash2 aria-hidden="true" />
            </Button>
          )}
          <Button type="submit" form="form-veiculo" size="lg" className="flex-1" disabled={salvar.isPending}>
            {salvar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
            Salvar
          </Button>
        </div>
      }
    >
      <form id="form-veiculo" noValidate onSubmit={form.handleSubmit((c) => salvar.mutate(c))} className="space-y-5">
        <Campo rotulo="Nome" erro={form.formState.errors.descricao?.message}>
          {(p) => <Input {...p} autoFocus placeholder="Ex.: Fiorino" {...form.register('descricao')} />}
        </Campo>
        <Campo rotulo="Placa" opcional erro={form.formState.errors.placa?.message}>
          {(p) => <Input {...p} autoCapitalize="characters" placeholder="ABC1D23" className="uppercase" {...form.register('placa')} />}
        </Campo>
        {atual && (
          <Controller
            control={form.control}
            name="ativo"
            render={({ field }) => (
              <label className="flex items-center justify-between gap-4 rounded-xl border bg-card p-4">
                <span>
                  <span className="block font-medium">Veículo ativo</span>
                  <span className="block text-sm text-muted-foreground">Inativo não aparece ao lançar despesas.</span>
                </span>
                <Switch checked={field.value} onCheckedChange={field.onChange} />
              </label>
            )}
          />
        )}
      </form>
      <ConfirmarAcao
        aberto={confirmar}
        aoFechar={() => setConfirmar(false)}
        titulo="Excluir este veículo?"
        descricao="Veículos com despesas lançadas não podem ser excluídos: nesse caso, desative."
        confirmar="Excluir"
        aoConfirmar={() => atual?.id != null && excluir.mutate(atual.id)}
      />
    </PainelFormulario>
  )
}
