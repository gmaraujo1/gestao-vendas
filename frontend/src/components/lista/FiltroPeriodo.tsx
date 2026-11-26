import { Filtros } from '@/components/lista/BarraLista'
import { ATALHOS_PERIODO, type AtalhoPeriodo } from '@/lib/datas'

/** Atalhos de período das listas: Hoje, 7 dias, Este mês, 30 dias, Tudo. */
export function FiltroPeriodo({ valor, aoMudar }: { valor: AtalhoPeriodo; aoMudar: (v: AtalhoPeriodo) => void }) {
  return (
    <Filtros<AtalhoPeriodo>
      rotulo="Período"
      valor={valor}
      aoMudar={aoMudar}
      opcoes={[...ATALHOS_PERIODO.map((a) => ({ valor: a.id as AtalhoPeriodo, rotulo: a.rotulo })), { valor: 'tudo', rotulo: 'Tudo' }]}
    />
  )
}
