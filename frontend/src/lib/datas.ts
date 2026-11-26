import { hojeIso } from '@/lib/formatadores'

/**
 * Contas com datas "AAAA-MM-DD" (LocalDate da API) sem passar por fuso: tudo em UTC puro,
 * só para somar dias e achar o começo do mês.
 */
const paraData = (iso: string) => new Date(`${iso}T00:00:00Z`)
const paraIso = (d: Date) => d.toISOString().slice(0, 10)

export function somarDias(iso: string, dias: number) {
  const d = paraData(iso)
  d.setUTCDate(d.getUTCDate() + dias)
  return paraIso(d)
}

export function inicioDoMes(iso: string) {
  return `${iso.slice(0, 8)}01`
}

export type Intervalo = { de: string; ate: string }

/** Atalhos dos filtros das listas. */
export const ATALHOS_PERIODO = [
  { id: 'hoje', rotulo: 'Hoje', intervalo: (): Intervalo => ({ de: hojeIso(), ate: hojeIso() }) },
  { id: '7dias', rotulo: '7 dias', intervalo: (): Intervalo => ({ de: somarDias(hojeIso(), -6), ate: hojeIso() }) },
  { id: 'mes', rotulo: 'Este mês', intervalo: (): Intervalo => ({ de: inicioDoMes(hojeIso()), ate: hojeIso() }) },
  { id: '30dias', rotulo: '30 dias', intervalo: (): Intervalo => ({ de: somarDias(hojeIso(), -29), ate: hojeIso() }) },
] as const

export type AtalhoPeriodo = (typeof ATALHOS_PERIODO)[number]['id'] | 'tudo'

export function intervaloDoAtalho(id: AtalhoPeriodo): Partial<Intervalo> {
  return ATALHOS_PERIODO.find((a) => a.id === id)?.intervalo() ?? {}
}

/** "2026-11-26T14:05:00" -> "14:05". */
export function horaDe(iso?: string | null) {
  return iso?.slice(11, 16) ?? ''
}

/** Rótulo amigável do dia: "Hoje", "Ontem" ou "26/11/2026". */
export function rotuloDia(iso?: string | null) {
  if (!iso) return ''
  const dia = iso.slice(0, 10)
  const hoje = hojeIso()
  if (dia === hoje) return 'Hoje'
  if (dia === somarDias(hoje, -1)) return 'Ontem'
  return `${dia.slice(8, 10)}/${dia.slice(5, 7)}/${dia.slice(0, 4)}`
}
