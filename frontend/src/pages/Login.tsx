import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Eye, EyeOff, Loader2, TriangleAlert } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate } from 'react-router'
import { z } from 'zod'
import { EmblemaApp } from '@/components/marca/EmblemaApp'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useSessao } from '@/hooks/use-sessao'
import { api, dados } from '@/lib/api/client'
import { ApiError } from '@/lib/api/erros'
import { useSessaoStore } from '@/lib/sessao'

const esquema = z.object({
  email: z.email('Informe um e-mail válido'),
  senha: z.string().min(1, 'Informe a senha'),
})

type Campos = z.infer<typeof esquema>

function mensagemDeErro(erro: unknown) {
  if (erro instanceof ApiError) {
    return erro.status === 401 ? 'E-mail ou senha incorretos.' : erro.message
  }
  return 'Não foi possível falar com o servidor. Confira a internet e tente de novo.'
}

export function Login() {
  const { login, negocioId } = useSessao()
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const form = useForm<Campos>({ resolver: zodResolver(esquema), defaultValues: { email: '', senha: '' } })

  const entrar = useMutation({
    mutationFn: (campos: Campos) => dados(api.POST('/api/auth/login', { body: campos })),
    // com o login salvo, o Navigate abaixo leva à escolha do negócio
    onSuccess: (resposta) => useSessaoStore.getState().entrar(resposta),
  })

  if (login) return <Navigate to={negocioId == null ? '/negocio' : '/'} replace />

  const { errors } = form.formState

  return (
    <main className="pt-seguro pb-seguro flex min-h-svh flex-col px-5">
      <div className="mx-auto flex w-full max-w-sm flex-1 flex-col justify-center py-10 motion-safe:animate-in motion-safe:fade-in-0 motion-safe:slide-in-from-bottom-3 motion-safe:duration-500">
        <div className="mb-8 flex flex-col items-center text-center">
          <EmblemaApp className="mb-5 size-16 drop-shadow-sm" />
          <h1 className="text-2xl font-semibold tracking-tight">Gestão Vendas</h1>
          <p className="mt-1 text-muted-foreground">Laticínio e Pão Nino</p>
        </div>

        <form
          noValidate
          onSubmit={form.handleSubmit((campos) => entrar.mutate(campos))}
          className="space-y-5 rounded-3xl border bg-card p-6 shadow-sm"
        >
          {entrar.isError && (
            <div
              role="alert"
              className="flex items-start gap-2.5 rounded-xl bg-destructive/10 p-3 text-sm text-destructive"
            >
              <TriangleAlert className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
              {mensagemDeErro(entrar.error)}
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="email">E-mail</Label>
            <Input
              id="email"
              type="email"
              inputMode="email"
              autoComplete="username"
              autoCapitalize="none"
              spellCheck={false}
              placeholder="voce@exemplo.com"
              aria-invalid={!!errors.email}
              aria-describedby={errors.email ? 'email-erro' : undefined}
              {...form.register('email')}
            />
            {errors.email && (
              <p id="email-erro" className="text-sm text-destructive">
                {errors.email.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="senha">Senha</Label>
            <div className="relative">
              <Input
                id="senha"
                type={mostrarSenha ? 'text' : 'password'}
                autoComplete="current-password"
                className="pr-12"
                aria-invalid={!!errors.senha}
                aria-describedby={errors.senha ? 'senha-erro' : undefined}
                {...form.register('senha')}
              />
              <button
                type="button"
                onClick={() => setMostrarSenha((v) => !v)}
                aria-label={mostrarSenha ? 'Esconder senha' : 'Mostrar senha'}
                aria-pressed={mostrarSenha}
                className="absolute inset-y-0 right-0 grid w-12 place-items-center rounded-r-lg text-muted-foreground outline-none hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50"
              >
                {mostrarSenha ? <EyeOff className="size-5" aria-hidden="true" /> : <Eye className="size-5" aria-hidden="true" />}
              </button>
            </div>
            {errors.senha && (
              <p id="senha-erro" className="text-sm text-destructive">
                {errors.senha.message}
              </p>
            )}
          </div>

          <Button type="submit" size="lg" className="w-full" disabled={entrar.isPending}>
            {entrar.isPending && <Loader2 className="animate-spin" aria-hidden="true" />}
            {entrar.isPending ? 'Entrando…' : 'Entrar'}
          </Button>
        </form>
      </div>
    </main>
  )
}
