import type { components } from '@/lib/api/schema'

type S = components['schemas']

export type TipoDespesa = NonNullable<S['DespesaResponseDTO']['tipo']>
export type MotivoPerda = NonNullable<S['PerdaRequestDTO']['motivo']>
export type Unidade = NonNullable<S['ProdutoResponseDTO']['unidadeMedida']>
export type TipoMovimentacao = NonNullable<S['MovimentacaoResponseDTO']['tipo']>
export type TipoPeriodo = NonNullable<S['SerieDTO']['agrupamento']>

export const TIPOS_DESPESA: Record<TipoDespesa, string> = {
  GASOLINA: 'Gasolina',
  PEDAGIO: 'Pedágio',
  PNEU: 'Pneu',
  MECANICA: 'Mecânica',
  OUTROS: 'Outros',
}

export const MOTIVOS_PERDA: Record<MotivoPerda, string> = {
  VENCIDO: 'Vencido',
  FURADO: 'Furado',
  AVARIADO: 'Avariado',
  OUTRO: 'Outro',
}

export const UNIDADES: Record<Unidade, string> = { UN: 'Unidade', CX: 'Caixa', L: 'Litro' }

export const TIPOS_MOVIMENTACAO: Record<TipoMovimentacao, string> = {
  VENDA: 'Venda',
  VENDA_ESTORNO: 'Venda cancelada',
  PERDA: 'Perda',
  TROCA_ENTRADA: 'Devolvido em troca',
  TROCA_SAIDA: 'Entregue em troca',
  AJUSTE: 'Ajuste de contagem',
  COMPRA: 'Compra',
}

export const PERIODOS: Record<TipoPeriodo, string> = {
  DIA: 'Dia',
  SEMANA: 'Semana',
  QUINZENA: 'Quinzena',
  MES: 'Mês',
  TRIMESTRE: 'Trimestre',
  SEMESTRE: 'Semestre',
  ANO: 'Ano',
}

export const opcoes = <T extends string>(mapa: Record<T, string>) =>
  (Object.entries(mapa) as [T, string][]).map(([valor, rotulo]) => ({ valor, rotulo }))
