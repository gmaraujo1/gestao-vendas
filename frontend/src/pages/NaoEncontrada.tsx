import { Link } from 'react-router'

export function NaoEncontrada() {
  return (
    <main className="mx-auto flex min-h-svh max-w-xl flex-col justify-center gap-3 p-6">
      <h1 className="font-heading text-2xl font-semibold">Página não encontrada</h1>
      <Link to="/" className="text-primary underline-offset-4 hover:underline">
        Voltar ao início
      </Link>
    </main>
  )
}
