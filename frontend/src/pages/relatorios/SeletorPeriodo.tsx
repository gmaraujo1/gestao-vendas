import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Filtros } from '@/components/lista/BarraLista'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { somarDias } from '@/lib/datas'
import { hojeIso } from '@/lib/formatadores'
import { PERIODOS, type TipoPeriodo } from '@/lib/rotulos'

export type Filtro = { periodo: TipoPeriodo; data: string } | { periodo: 'LIVRE'; de: string; ate: string }

/**
 * Período do relatório: dia, semana… ano (andando para trás e para frente com as setas) ou um
 * intervalo livre. O texto ("Novembro de 2026") vem pronto da API.
 */
export function SeletorPeriodo({
  filtro,
  aoMudar,
  descricao,
  de,
  ate,
}: {
  filtro: Filtro
  aoMudar: (f: Filtro) => void
  descricao?: string
  de?: string
  ate?: string
}) {
  const hoje = hojeIso()
  return (
    <div className="space-y-3">
      <Filtros<TipoPeriodo | 'LIVRE'>
        rotulo="Tipo de período"
        valor={filtro.periodo}
        aoMudar={(p) =>
          aoMudar(p === 'LIVRE' ? { periodo: 'LIVRE', de: de ?? hoje, ate: ate ?? hoje } : { periodo: p, data: 'data' in filtro ? filtro.data : hoje })
        }
        opcoes={[
          ...(Object.entries(PERIODOS) as [TipoPeriodo, string][]).map(([valor, rotulo]) => ({ valor, rotulo })),
          { valor: 'LIVRE' as const, rotulo: 'Personalizado' },
        ]}
      />
      {filtro.periodo === 'LIVRE' ? (
        <div className="grid grid-cols-2 gap-3">
          <label className="space-y-1.5 text-sm">
            <span className="text-muted-foreground">De</span>
            <Input type="date" value={filtro.de} max={filtro.ate} onChange={(e) => e.target.value && aoMudar({ ...filtro, de: e.target.value })} />
          </label>
          <label className="space-y-1.5 text-sm">
            <span className="text-muted-foreground">Até</span>
            <Input type="date" value={filtro.ate} min={filtro.de} max={hoje} onChange={(e) => e.target.value && aoMudar({ ...filtro, ate: e.target.value })} />
          </label>
        </div>
      ) : (
        <div className="flex items-center gap-2 rounded-2xl border bg-card p-1.5 shadow-xs">
          <Button
            variant="ghost"
            size="icon-lg"
            aria-label="Período anterior"
            disabled={!de}
            onClick={() => de && aoMudar({ periodo: filtro.periodo, data: somarDias(de, -1) })}
          >
            <ChevronLeft aria-hidden="true" />
          </Button>
          <p className="flex-1 text-center font-semibold text-balance" aria-live="polite">
            {descricao ?? '…'}
          </p>
          <Button
            variant="ghost"
            size="icon-lg"
            aria-label="Próximo período"
            disabled={!ate || ate >= hoje}
            onClick={() => ate && aoMudar({ periodo: filtro.periodo, data: somarDias(ate, 1) })}
          >
            <ChevronRight aria-hidden="true" />
          </Button>
        </div>
      )}
    </div>
  )
}
