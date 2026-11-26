import { Bar, CartesianGrid, Cell, ComposedChart, Line, Pie, PieChart, XAxis, YAxis } from 'recharts'
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from '@/components/ui/chart'
import type { components } from '@/lib/api/schema'
import { formatarMoeda } from '@/lib/formatadores'
import { TIPOS_DESPESA, type TipoPeriodo } from '@/lib/rotulos'

type Ponto = components['schemas']['PontoSerieDTO']
type PorTipo = components['schemas']['PorTipo']

const compacto = new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 })

function rotuloPonto(p: Ponto, agrupamento?: TipoPeriodo) {
  const [, mes, dia] = (p.inicio ?? '').split('-')
  if (agrupamento === 'MES') {
    return new Date(`${p.inicio}T12:00:00`).toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')
  }
  if (agrupamento === 'TRIMESTRE' || agrupamento === 'SEMESTRE' || agrupamento === 'ANO') return `${mes}/${p.inicio?.slice(2, 4)}`
  return `${dia}/${mes}`
}

const CONFIG_SERIE = {
  vendas: { label: 'Vendas', color: 'var(--chart-1)' },
  resultado: { label: 'Resultado', color: 'var(--chart-3)' },
} satisfies ChartConfig

/** Vendas (barras) e resultado (linha) ao longo do período. */
export function GraficoSerie({ pontos, agrupamento }: { pontos: Ponto[]; agrupamento?: TipoPeriodo }) {
  const dados = pontos.map((p) => ({ ...p, rotulo: rotuloPonto(p, agrupamento) }))
  return (
    <ChartContainer config={CONFIG_SERIE} className="aspect-auto h-64 w-full">
      <ComposedChart data={dados} margin={{ left: 4, right: 4, top: 8 }}>
        <CartesianGrid vertical={false} />
        <XAxis dataKey="rotulo" tickLine={false} axisLine={false} tickMargin={8} minTickGap={16} />
        <YAxis tickLine={false} axisLine={false} width={44} tickFormatter={(v: number) => compacto.format(v)} />
        <ChartTooltip
          cursor={false}
          content={
            <ChartTooltipContent
              labelFormatter={(_, itens) => {
                const p = itens?.[0]?.payload as Ponto | undefined
                return p?.inicio === p?.fim ? p?.inicio?.split('-').reverse().join('/') : `${p?.inicio?.split('-').reverse().join('/')} a ${p?.fim?.split('-').reverse().join('/')}`
              }}
              formatter={(valor, nome) => (
                <div className="flex w-full justify-between gap-4">
                  <span className="text-muted-foreground">{CONFIG_SERIE[nome as keyof typeof CONFIG_SERIE]?.label}</span>
                  <span className="numeros font-medium">{formatarMoeda(Number(valor))}</span>
                </div>
              )}
            />
          }
        />
        <Bar dataKey="vendas" fill="var(--color-vendas)" radius={[6, 6, 0, 0]} maxBarSize={36} />
        <Line dataKey="resultado" stroke="var(--color-resultado)" strokeWidth={2.5} dot={false} type="monotone" />
      </ComposedChart>
    </ChartContainer>
  )
}

const CORES = ['var(--chart-1)', 'var(--chart-2)', 'var(--chart-3)', 'var(--chart-4)', 'var(--chart-5)']

/** Rosca das despesas por tipo, com a legenda em lista (legível no celular). */
export function GraficoDespesas({ porTipo }: { porTipo: PorTipo[] }) {
  const total = porTipo.reduce((s, t) => s + (t.total ?? 0), 0)
  const config = Object.fromEntries(porTipo.map((t, i) => [t.tipo ?? i, { label: t.tipo ? TIPOS_DESPESA[t.tipo] : '', color: CORES[i % CORES.length] }])) satisfies ChartConfig
  return (
    <div className="flex flex-col items-center gap-4 sm:flex-row">
      <ChartContainer config={config} className="aspect-square h-44 shrink-0">
        <PieChart>
          <ChartTooltip content={<ChartTooltipContent hideLabel nameKey="tipo" formatter={(v, n) => `${config[n as string]?.label}: ${formatarMoeda(Number(v))}`} />} />
          <Pie data={porTipo} dataKey="total" nameKey="tipo" innerRadius={48} outerRadius={80} strokeWidth={2} stroke="var(--card)">
            {porTipo.map((t, i) => (
              <Cell key={t.tipo} fill={CORES[i % CORES.length]} />
            ))}
          </Pie>
        </PieChart>
      </ChartContainer>
      <ul className="w-full space-y-2 text-sm">
        {porTipo.map((t, i) => (
          <li key={t.tipo} className="flex items-center gap-2.5">
            <span className="size-3 shrink-0 rounded-sm" style={{ background: CORES[i % CORES.length] }} aria-hidden="true" />
            <span className="flex-1">{t.tipo ? TIPOS_DESPESA[t.tipo] : ''}</span>
            <span className="numeros text-muted-foreground">{total ? Math.round(((t.total ?? 0) / total) * 100) : 0}%</span>
            <span className="numeros w-24 text-right font-medium">{formatarMoeda(t.total)}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
