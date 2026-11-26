import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { Eye, EyeOff, Loader2, Plus, Trash2, UserCog } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { z } from 'zod'
import { ConfirmarAcao } from '@/components/ConfirmarAcao'
import { Etiqueta } from '@/components/Etiqueta'
import { Campo } from '@/components/formulario/Campo'
import { Pilulas } from '@/components/formulario/controles'
import { PainelFormulario } from '@/components/formulario/PainelFormulario'
import { EstadosLista } from '@/components/lista/EstadosLista'
import { TabelaDados } from '@/components/lista/TabelaDados'
import { Pagina } from '@/components/pagina/Pagina'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { useAcao } from '@/hooks/use-acao'
import { useMeusNegocios } from '@/hooks/use-negocios'
import { useSessao } from '@/hooks/use-sessao'
import { api, dados, semConteudo } from '@/lib/api/client'
import type { components } from '@/lib/api/schema'
import { marcaDe } from '@/lib/marca'
import { colunasDe } from '@/lib/tabela'
import { iniciais } from '@/lib/texto'
import { cn } from '@/lib/utils'

type Usuario = components['schemas']['UsuarioResponseDTO']
type Tipo = NonNullable<Usuario['tipoUsuario']>

const TIPOS: { valor: Tipo; rotulo: string }[] = [
  { valor: 'FUNCIONARIO', rotulo: 'Funcionário' },
  { valor: 'ADMIN', rotulo: 'Administrador' },
]

function useNomesNegocios() {
  const { data } = useMeusNegocios()
  return useMemo(
    () => (ids?: number[]) =>
      (ids ?? []).map((id) => marcaDe(data?.find((n) => n.id === id)?.codigo)?.nomeCurto ?? `#${id}`).join(', '),
    [data],
  )
}

const h = colunasDe<Usuario>()

/** Contas de acesso (pais e funcionário). Só o ADMIN. */
export function Usuarios() {
  const { login } = useSessao()
  const nomesNegocios = useNomesNegocios()
  const [editando, setEditando] = useState<Usuario | 'novo' | null>(null)
  const lista = useQuery({ queryKey: ['usuarios'], queryFn: () => dados(api.GET('/api/usuario/listar')) })

  const meuId = login?.usuarioId
  // a v9 pede colunas estáveis: só mudam quando mudam o usuário logado ou os nomes dos negócios
  const colunas = useMemo(
    () => [
      h.accessor('nome', {
        header: 'Nome',
        cell: (c) => (
          <span className="flex items-center gap-2 font-medium">
            {c.getValue()}
            {c.row.original.id === meuId && <Etiqueta tom="marca">Você</Etiqueta>}
          </span>
        ),
      }),
      h.accessor('email', {
        header: 'E-mail',
        cell: (c) => <span className="text-muted-foreground">{c.getValue()}</span>,
      }),
      h.accessor('tipoUsuario', {
        header: 'Perfil',
        cell: (c) => (c.getValue() === 'ADMIN' ? 'Administrador' : 'Funcionário'),
      }),
      h.accessor('negocioIds', { header: 'Negócios', cell: (c) => nomesNegocios(c.getValue()) }),
    ],
    [meuId, nomesNegocios],
  )

  return (
    <Pagina
      titulo="Usuários"
      subtitulo="Quem pode entrar no sistema e em quais negócios."
      acoes={
        <Button onClick={() => setEditando('novo')}>
          <Plus aria-hidden="true" /> <span className="max-sm:sr-only">Novo usuário</span>
        </Button>
      }
    >
      <EstadosLista
        carregando={lista.isPending}
        erro={lista.error}
        vazio={(lista.data?.length ?? 0) === 0}
        tentarDeNovo={() => void lista.refetch()}
        iconeVazio={UserCog}
        tituloVazio="Nenhum usuário"
      >
        <TabelaDados
          rotulo="Usuários"
          dados={lista.data ?? []}
          colunas={colunas}
          chave={(u) => u.id ?? 0}
          aoAbrir={setEditando}
          cartao={(u) => (
            <div className="flex items-center gap-3">
              <span className="grid size-11 shrink-0 place-items-center rounded-full bg-accent text-sm font-semibold text-accent-foreground">
                {iniciais(u.nome)}
              </span>
              <div className="min-w-0 flex-1">
                <p className="flex items-center gap-2 font-medium">
                  <span className="truncate">{u.nome}</span>
                  {u.id === login?.usuarioId && <Etiqueta tom="marca">Você</Etiqueta>}
                </p>
                <p className="truncate text-sm text-muted-foreground">
                  {u.tipoUsuario === 'ADMIN' ? 'Administrador' : 'Funcionário'} · {nomesNegocios(u.negocioIds)}
                </p>
              </div>
            </div>
          )}
        />
      </EstadosLista>
      <FormUsuario usuario={editando} aoFechar={() => setEditando(null)} />
    </Pagina>
  )
}

const esquema = z
  .object({
    nome: z.string().trim().min(1, 'Informe o nome').max(120),
    email: z.email('E-mail inválido'),
    senha: z.string(),
    tipoUsuario: z.enum(['ADMIN', 'FUNCIONARIO']),
    negocioIds: z.array(z.number()).min(1, 'Escolha ao menos um negócio'),
    novo: z.boolean(),
  })
  .refine((c) => !c.novo || c.senha.length >= 8, { path: ['senha'], message: 'Mínimo de 8 caracteres' })
type Campos = z.infer<typeof esquema>

function FormUsuario({ usuario, aoFechar }: { usuario: Usuario | 'novo' | null; aoFechar: () => void }) {
  const { login } = useSessao()
  const { data: negocios } = useMeusNegocios()
  const atual = usuario === 'novo' ? null : usuario
  const souEu = atual?.id === login?.usuarioId
  const [verSenha, setVerSenha] = useState(false)
  const [confirmar, setConfirmar] = useState(false)
  const form = useForm<Campos>({ resolver: zodResolver(esquema) })

  useEffect(() => {
    if (!usuario) return
    form.reset({
      nome: atual?.nome ?? '',
      email: atual?.email ?? '',
      senha: '',
      tipoUsuario: atual?.tipoUsuario ?? 'FUNCIONARIO',
      negocioIds: atual?.negocioIds ?? [],
      novo: !atual,
    })
  }, [usuario, atual, form])

  const salvar = useAcao({
    executar: (c: Campos) =>
      atual?.id != null
        ? dados(
            api.PATCH('/api/usuario/{id}/editar', {
              params: { path: { id: atual.id } },
              body: { nome: c.nome.trim(), email: c.email, negocioIds: c.negocioIds },
            }),
          )
        : dados(
            api.POST('/api/usuario/criar', {
              body: {
                nome: c.nome.trim(),
                email: c.email,
                senha: c.senha,
                tipoUsuario: c.tipoUsuario,
                negocioIds: c.negocioIds,
              },
            }),
          ),
    sucesso: atual ? 'Usuário atualizado' : 'Usuário criado',
    invalidar: [['usuarios']],
    aoConcluir: aoFechar,
  })
  const excluir = useAcao({
    executar: (id: number) => semConteudo(api.DELETE('/api/usuario/{id}/remover', { params: { path: { id } } })),
    sucesso: 'Usuário removido',
    invalidar: [['usuarios']],
    aoConcluir: aoFechar,
  })

  const { errors } = form.formState
  return (
    <PainelFormulario
      aberto={usuario != null}
      aoFechar={aoFechar}
      titulo={atual ? 'Editar usuário' : 'Novo usuário'}
      descricao={atual ? 'O perfil e a senha não mudam por aqui.' : 'Passe o e-mail e a senha para a pessoa entrar.'}
      rodape={
        <div className="flex gap-2">
          {atual?.id != null && !souEu && atual.tipoUsuario !== 'ADMIN' && (
            <Button
              type="button"
              variant="destructive"
              size="icon-lg"
              aria-label="Remover usuário"
              onClick={() => setConfirmar(true)}
            >
              <Trash2 aria-hidden="true" />
            </Button>
          )}
          <Button type="submit" form="form-usuario" size="lg" className="flex-1" disabled={salvar.isPending}>
            {salvar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
            Salvar
          </Button>
        </div>
      }
    >
      <form id="form-usuario" noValidate onSubmit={form.handleSubmit((c) => salvar.mutate(c))} className="space-y-5">
        <Campo rotulo="Nome" erro={errors.nome?.message}>
          {(p) => <Input {...p} autoFocus autoCapitalize="words" {...form.register('nome')} />}
        </Campo>
        <Campo rotulo="E-mail" erro={errors.email?.message}>
          {(p) => (
            <Input
              {...p}
              type="email"
              inputMode="email"
              autoCapitalize="none"
              autoComplete="off"
              {...form.register('email')}
            />
          )}
        </Campo>
        {!atual && (
          <>
            <Campo rotulo="Senha" erro={errors.senha?.message} dica="Mínimo de 8 caracteres.">
              {(p) => (
                <div className="relative">
                  <Input
                    {...p}
                    type={verSenha ? 'text' : 'password'}
                    autoComplete="new-password"
                    className="pr-12"
                    {...form.register('senha')}
                  />
                  <button
                    type="button"
                    onClick={() => setVerSenha((v) => !v)}
                    aria-label={verSenha ? 'Esconder senha' : 'Mostrar senha'}
                    aria-pressed={verSenha}
                    className="absolute inset-y-0 right-0 grid w-12 place-items-center text-muted-foreground outline-none hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50"
                  >
                    {verSenha ? (
                      <EyeOff className="size-5" aria-hidden="true" />
                    ) : (
                      <Eye className="size-5" aria-hidden="true" />
                    )}
                  </button>
                </div>
              )}
            </Campo>
            <fieldset className="space-y-2">
              <legend className="text-sm font-medium">Perfil</legend>
              <Controller
                control={form.control}
                name="tipoUsuario"
                render={({ field }) => (
                  <Pilulas rotulo="Perfil" opcoes={TIPOS} valor={field.value} aoMudar={field.onChange} />
                )}
              />
              <p className="text-sm text-muted-foreground">
                O funcionário registra vendas, trocas, perdas e despesas; não vê relatórios nem altera cadastros.
              </p>
            </fieldset>
          </>
        )}
        <fieldset className="space-y-2">
          <legend className="text-sm font-medium">Negócios</legend>
          <Controller
            control={form.control}
            name="negocioIds"
            render={({ field }) => (
              <div className="grid gap-2">
                {negocios?.map((n) => {
                  const marcado = field.value?.includes(n.id ?? -1)
                  return (
                    <label
                      key={n.id}
                      className={cn(
                        'flex h-12 cursor-pointer items-center gap-3 rounded-xl border bg-card px-4 transition-colors',
                        marcado && 'border-primary bg-accent',
                      )}
                    >
                      <input
                        type="checkbox"
                        className="size-5 accent-(--primary)"
                        checked={marcado}
                        onChange={(e) =>
                          field.onChange(
                            e.target.checked
                              ? [...(field.value ?? []), n.id]
                              : (field.value ?? []).filter((x) => x !== n.id),
                          )
                        }
                      />
                      {marcaDe(n.codigo)?.nomeCurto ?? n.nome}
                    </label>
                  )
                })}
              </div>
            )}
          />
          {errors.negocioIds && <p className="text-sm text-destructive">{errors.negocioIds.message}</p>}
        </fieldset>
      </form>
      <ConfirmarAcao
        aberto={confirmar}
        aoFechar={() => setConfirmar(false)}
        titulo="Remover este usuário?"
        descricao="A pessoa não vai mais conseguir entrar. O que ela já lançou continua no sistema."
        confirmar="Remover"
        aoConfirmar={() => atual?.id != null && excluir.mutate(atual.id)}
      />
    </PainelFormulario>
  )
}
