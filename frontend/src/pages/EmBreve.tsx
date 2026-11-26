import { Hammer } from 'lucide-react'
import { Link } from 'react-router'
import { EstadoVazio } from '@/components/estados/EstadoVazio'
import { Button } from '@/components/ui/button'

/** Telas das próximas partes da etapa 8 (cadastros, vendas, estoque...). */
export function EmBreve({ titulo }: { titulo: string }) {
  return (
    <EstadoVazio
      icone={Hammer}
      titulo={titulo}
      descricao="Esta tela está sendo construída e chega nas próximas atualizações."
      acao={
        <Button asChild variant="outline" size="lg">
          <Link to="/">Voltar ao início</Link>
        </Button>
      }
    />
  )
}
