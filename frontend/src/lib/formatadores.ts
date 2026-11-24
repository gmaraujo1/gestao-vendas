/**
 * Formatação padrão do sistema (pt-BR, horário de Brasília).
 * A API manda dinheiro como número e datas ISO sem fuso (`LocalDate`/`LocalDateTime`),
 * já no horário de Brasília. Não faça contas de dinheiro no front: só exiba.
 */

export const FUSO = 'America/Sao_Paulo'

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const quantidade = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 3 })

export const formatarMoeda = (valor?: number | null) => (valor == null ? '' : moeda.format(valor))

export const formatarQuantidade = (valor?: number | null) =>
  valor == null ? '' : quantidade.format(valor)

/** "2026-11-24" ou "2026-11-24T10:30:00" -> "24/11/2026" (só texto, sem conversão de fuso). */
export function formatarData(iso?: string | null) {
  const m = iso?.match(/^(\d{4})-(\d{2})-(\d{2})/)
  return m ? `${m[3]}/${m[2]}/${m[1]}` : ''
}

/** "2026-11-24T10:30:00" (LocalDateTime) -> "24/11/2026 10:30" (só texto, sem conversão de fuso). */
export function formatarDataHora(iso?: string | null) {
  const m = iso?.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/)
  return m ? `${m[3]}/${m[2]}/${m[1]} ${m[4]}:${m[5]}` : ''
}

/** Data de hoje em Brasília, "AAAA-MM-DD" (filtros ?de=&ate=, data de despesa). */
export function hojeIso() {
  return new Date().toLocaleDateString('sv-SE', { timeZone: FUSO })
}

/**
 * Agora em Brasília como LocalDateTime, "AAAA-MM-DDTHH:mm:ss" (campo dataHora de venda/troca/perda).
 * Nunca use toISOString(): ele converte para UTC e o backend recusaria como data no futuro.
 */
export function agoraIso() {
  return new Date().toLocaleString('sv-SE', { timeZone: FUSO }).replace(' ', 'T')
}
