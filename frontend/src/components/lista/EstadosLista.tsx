import { Loader2, RefreshCw, type LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import { EstadoVazio } from '@/components/estados/EstadoVazio'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { mensagemDeErro } from '@/lib/api/erros'

/** Carregando, erro, vazio ou a lista, sempre na mesma ordem e com a mesma cara em todas as telas. */
export function EstadosLista({
  carregando,
  erro,
  vazio,
  tentarDeNovo,
  iconeVazio,
  tituloVazio,
  descricaoVazio,
  acaoVazio,
  children,
}: {
  carregando: boolean
  erro: unknown
  vazio: boolean
  tentarDeNovo: () => void
  iconeVazio: LucideIcon
  tituloVazio: string
  descricaoVazio?: string
  acaoVazio?: ReactNode
  children: ReactNode
}) {
  if (carregando) {
    return (
      <div role="status" aria-busy="true" className="space-y-2.5">
        <span className="sr-only">Carregando…</span>
        {[0, 1, 2, 3].map((i) => (
          <Skeleton key={i} className="h-20 rounded-2xl" style={{ opacity: 1 - i * 0.2 }} />
        ))}
      </div>
    )
  }
  if (erro) {
    return (
      <div role="alert">
        <EstadoVazio
          icone={RefreshCw}
          titulo="Não deu para carregar"
          descricao={mensagemDeErro(erro)}
          acao={
            <Button size="lg" variant="outline" onClick={tentarDeNovo}>
              Tentar de novo
            </Button>
          }
        />
      </div>
    )
  }
  if (vazio) return <EstadoVazio icone={iconeVazio} titulo={tituloVazio} descricao={descricaoVazio} acao={acaoVazio} />
  return <>{children}</>
}

/** Botão no fim da lista para buscar a próxima página. */
export function CarregarMais({
  temMais,
  carregando,
  aoCarregar,
  exibidos,
  total,
}: {
  temMais: boolean
  carregando: boolean
  aoCarregar: () => void
  exibidos: number
  total: number
}) {
  if (total === 0) return null
  return (
    <div className="flex flex-col items-center gap-2 pt-2">
      <p className="text-xs text-muted-foreground">
        {exibidos} de {total}
      </p>
      {temMais && (
        <Button variant="outline" onClick={aoCarregar} disabled={carregando}>
          {carregando && <Loader2 className="animate-spin" aria-hidden="true" />}
          Carregar mais
        </Button>
      )}
    </div>
  )
}
