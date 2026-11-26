import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, Plus, Tags, Trash2 } from 'lucide-react'
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
import { useCategorias } from '@/hooks/use-cadastros'
import { useNegocioId } from '@/hooks/use-negocio-id'
import { api, dados, doNegocio, semConteudo } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { colunasDe } from '@/lib/tabela'

type Categoria = components['schemas']['CategoriaResponseDTO']

const h = colunasDe<Categoria>()
const COLUNAS = [
  h.accessor('nome', { header: 'Categoria', cell: (c) => <span className="font-medium">{c.getValue()}</span> }),
  h.accessor('ativo', { header: 'Situação', cell: (c) => (c.getValue() ? <Etiqueta tom="sucesso">Ativa</Etiqueta> : <Etiqueta>Inativa</Etiqueta>) }),
]

/** Categorias dos produtos (por negócio). Só o ADMIN. */
export function Categorias() {
  const lista = useCategorias()
  const [editando, setEditando] = useState<Categoria | 'nova' | null>(null)

  return (
    <Pagina
      titulo="Categorias"
      subtitulo="Agrupam os produtos nas listas e nos filtros."
      acoes={
        <Button onClick={() => setEditando('nova')}>
          <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Nova categoria</span>
        </Button>
      }
    >
      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={(lista.data?.length ?? 0) === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={Tags}
        tituloVazio="Nenhuma categoria"
      >
        <TabelaDados
          rotulo="Categorias"
          dados={lista.data ?? []}
          colunas={COLUNAS}
          chave={(c) => c.id ?? 0}
          aoAbrir={setEditando}
          cartao={(c) => (
            <div className="flex items-center justify-between gap-3">
              <span className="font-medium">{c.nome}</span>
              {c.ativo ? <Etiqueta tom="sucesso">Ativa</Etiqueta> : <Etiqueta>Inativa</Etiqueta>}
            </div>
          )}
        />
      </EstadosLista>
      <FormCategoria categoria={editando} aoFechar={() => setEditando(null)} />
    </Pagina>
  )
}

const esquema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome').max(60, 'No máximo 60 caracteres'),
  ativo: z.boolean(),
})
type Campos = z.infer<typeof esquema>

function FormCategoria({ categoria, aoFechar }: { categoria: Categoria | 'nova' | null; aoFechar: () => void }) {
  const negocioId = useNegocioId()
  const atual = categoria === 'nova' ? null : categoria
  const [confirmar, setConfirmar] = useState(false)
  const form = useForm<Campos>({ resolver: zodResolver(esquema) })

  useEffect(() => {
    if (categoria) form.reset({ nome: atual?.nome ?? '', ativo: atual?.ativo ?? true })
  }, [categoria, atual, form])

  const invalidar = [['categorias', negocioId], ['produtos', negocioId]]
  const salvar = useAcao({
    executar: (c: Campos) =>
      atual?.id != null
        ? dados(api.PATCH('/api/categoria/{id}', { params: { path: { id: atual.id }, header: doNegocio(negocioId) }, body: { nome: c.nome.trim(), ativo: c.ativo } }))
        : dados(api.POST('/api/categoria', { params: { header: doNegocio(negocioId) }, body: { nome: c.nome.trim() } })),
    sucesso: atual ? 'Categoria atualizada' : 'Categoria criada',
    invalidar,
    aoConcluir: aoFechar,
  })
  const excluir = useAcao({
    executar: (id: number) => semConteudo(api.DELETE('/api/categoria/{id}', { params: { path: { id }, header: doNegocio(negocioId) } })),
    sucesso: 'Categoria excluída',
    invalidar,
    aoConcluir: aoFechar,
  })

  return (
    <PainelFormulario
      aberto={categoria != null}
      aoFechar={aoFechar}
      titulo={atual ? 'Editar categoria' : 'Nova categoria'}
      rodape={
        <div className="flex gap-2">
          {atual?.id != null && (
            <Button type="button" variant="destructive" size="icon-lg" aria-label="Excluir categoria" onClick={() => setConfirmar(true)}>
              <Trash2 aria-hidden="true" />
            </Button>
          )}
          <Button type="submit" form="form-categoria" size="lg" className="flex-1" disabled={salvar.isPending}>
            {salvar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
            Salvar
          </Button>
        </div>
      }
    >
      <form id="form-categoria" noValidate onSubmit={form.handleSubmit((c) => salvar.mutate(c))} className="space-y-5">
        <Campo rotulo="Nome" erro={form.formState.errors.nome?.message}>
          {(p) => <Input {...p} autoFocus placeholder="Ex.: Queijos" {...form.register('nome')} />}
        </Campo>
        {atual && (
          <Controller
            control={form.control}
            name="ativo"
            render={({ field }) => (
              <label className="flex items-center justify-between gap-4 rounded-xl border bg-card p-4">
                <span>
                  <span className="block font-medium">Categoria ativa</span>
                  <span className="block text-sm text-muted-foreground">Inativa não aparece no cadastro de produtos.</span>
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
        titulo="Excluir esta categoria?"
        descricao="Categorias com produtos não podem ser excluídas: desative ou mude os produtos de categoria."
        confirmar="Excluir"
        aoConfirmar={() => atual?.id != null && excluir.mutate(atual.id)}
      />
    </PainelFormulario>
  )
}
