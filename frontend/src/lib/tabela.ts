import { createColumnHelper, tableFeatures, type RowData } from '@tanstack/react-table'

/**
 * TanStack Table v9: as listas usam só o núcleo (a ordenação e a paginação vêm da API).
 * Fica no escopo do módulo, como pede a v9 (entradas estáveis).
 */
export const recursosTabela = tableFeatures({})
export type RecursosTabela = typeof recursosTabela

export const colunasDe = <T extends RowData>() => createColumnHelper<RecursosTabela, T>()
