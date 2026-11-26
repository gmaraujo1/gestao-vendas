import { RefreshCw } from 'lucide-react'
import { useRouteError } from 'react-router'
import { EstadoVazio } from '@/components/estados/EstadoVazio'
import { Button } from '@/components/ui/button'

const CHAVE = 'gv.recarregou'

/** Depois de um deploy, o celular com o app aberto tenta baixar um pedaço que não existe mais. */
function ehVersaoAntiga(erro: unknown) {
  const msg = erro instanceof Error ? erro.message : String(erro)
  return /dynamically imported module|Importing a module script failed|error loading dynamically/i.test(msg)
}

/** Tela de erro das rotas: em vez da página de erro crua, uma mensagem e "Recarregar". */
export function ErroRota() {
  const erro = useRouteError()
  if (ehVersaoAntiga(erro) && !sessionStorage.getItem(CHAVE)) {
    // recarrega uma vez só (evita loop se o problema for outro)
    sessionStorage.setItem(CHAVE, '1')
    window.location.reload()
    return null
  }
  sessionStorage.removeItem(CHAVE)
  return (
    <div role="alert" className="grid min-h-[60svh] place-items-center">
      <EstadoVazio
        icone={RefreshCw}
        titulo="Algo deu errado nesta tela"
        descricao="Recarregue a página. Se continuar, avise quem cuida do sistema."
        acao={
          <Button size="lg" onClick={() => window.location.reload()}>
            Recarregar
          </Button>
        }
      />
    </div>
  )
}
