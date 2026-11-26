import { Lock } from 'lucide-react'
import { Link } from 'react-router'
import { Button } from '@/components/ui/button'
import { EstadoVazio } from '@/components/estados/EstadoVazio'

export function SemPermissao() {
  return (
    <EstadoVazio
      icone={Lock}
      titulo="Só para administradores"
      descricao="Esta parte do sistema é usada pelos donos. Se precisar dela, peça acesso a eles."
      acao={
        <Button asChild variant="outline" size="lg">
          <Link to="/">Voltar ao início</Link>
        </Button>
      }
    />
  )
}
