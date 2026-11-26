import { SearchX } from 'lucide-react'
import { Link } from 'react-router'
import { EstadoVazio } from '@/components/estados/EstadoVazio'
import { Button } from '@/components/ui/button'

export function NaoEncontrada() {
  return (
    <main className="grid min-h-svh place-items-center">
      <EstadoVazio
        icone={SearchX}
        titulo="Página não encontrada"
        descricao="O endereço não existe ou mudou de lugar."
        acao={
          <Button asChild size="lg">
            <Link to="/">Voltar ao início</Link>
          </Button>
        }
      />
    </main>
  )
}
