import { z } from 'zod'

/**
 * Normaliza um número digitado no padrão brasileiro para o formato com ponto decimal:
 * "5,50" -> "5.50"; "1.234,56" -> "1234.56"; "1.500" -> "1500" (ponto com 3 dígitos e sem
 * vírgula é milhar); ",5" -> "0.5". Devolve null se não for número.
 */
function normalizar(texto?: string | null): string | null {
  let t = (texto ?? '').replace(/\s|R\$/g, '')
  if (!t) return null
  if (t.includes(',')) t = t.replace(/\./g, '').replace(',', '.')
  else if (/^\d{1,3}(\.\d{3})+$/.test(t)) t = t.replace(/\./g, '')
  if (t.startsWith('.')) t = `0${t}`
  if (t.endsWith('.')) t = t.slice(0, -1)
  return /^\d+(\.\d+)?$/.test(t) ? t : null
}

/** Texto digitado -> número (ou null se não for número). */
export function paraNumero(texto?: string | null): number | null {
  const n = normalizar(texto)
  return n == null ? null : Number(n)
}

/** Quantas casas decimais foram digitadas (contadas no texto, não no número). */
export function casasDecimais(texto?: string | null) {
  return normalizar(texto)?.split('.')[1]?.length ?? 0
}

/** Número da API para o campo do formulário: 5.5 -> "5,50" (dinheiro) ou "5,5" (quantidade). */
export function paraCampo(valor?: number | null, tipo: 'dinheiro' | 'quantidade' = 'dinheiro') {
  if (valor == null) return ''
  return tipo === 'dinheiro' ? valor.toFixed(2).replace('.', ',') : String(valor).replace('.', ',')
}

/**
 * Subtotal em centavos com a mesma regra do backend (BigDecimal, HALF_UP), sem erro de ponto
 * flutuante: quantidade em milésimos × preço em centavos, tudo inteiro.
 */
export function subtotalCentavos(quantidade: number, preco: number) {
  const milesimos = Math.round(quantidade * 1000)
  const centavos = Math.round(preco * 100)
  return Math.round((milesimos * centavos) / 1000)
}

type OpcoesNumero = {
  /** "positivo" (> 0) ou "naoNegativo" (>= 0). */
  minimo?: 'positivo' | 'naoNegativo'
  casas?: number
  obrigatorio?: boolean
}

/** Mensagem de erro de um número digitado, ou null se estiver certo. */
export function erroNumero(texto: string, { minimo = 'positivo', casas = 2, obrigatorio = true }: OpcoesNumero = {}) {
  if (!texto.trim()) return obrigatorio ? 'Obrigatório' : null
  const n = paraNumero(texto)
  if (n == null) return 'Número inválido'
  if (minimo === 'positivo' && n <= 0) return 'Precisa ser maior que zero'
  if (minimo === 'naoNegativo' && n < 0) return 'Não pode ser negativo'
  if (casasDecimais(texto) > casas) return `No máximo ${casas} casas decimais`
  return null
}

/** Validação (Zod) de um campo numérico em texto. */
export function campoNumero(opcoes: OpcoesNumero = {}) {
  return z.string().superRefine((texto, ctx) => {
    const erro = erroNumero(texto, opcoes)
    if (erro) ctx.addIssue({ code: 'custom', message: erro })
  })
}
