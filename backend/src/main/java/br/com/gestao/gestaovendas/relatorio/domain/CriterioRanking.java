package br.com.gestao.gestaovendas.relatorio.domain;

/** Ordem do ranking de produtos mais vendidos. */
public enum CriterioRanking {
    /** Quantidade vendida (atenção: mistura unidades diferentes, como UN e L). */
    QUANTIDADE,
    /** Valor vendido. */
    VENDAS,
    /** Vendas menos o custo das mercadorias vendidas. */
    LUCRO
}
