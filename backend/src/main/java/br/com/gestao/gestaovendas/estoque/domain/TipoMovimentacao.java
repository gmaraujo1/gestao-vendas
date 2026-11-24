package br.com.gestao.gestaovendas.estoque.domain;

/** Sinal da quantidade: entradas positivas, saídas negativas. AJUSTE pode ser os dois. */
public enum TipoMovimentacao {
    VENDA,
    VENDA_ESTORNO,
    PERDA,
    TROCA_ENTRADA,
    TROCA_SAIDA,
    AJUSTE,
    /** Etapa 5. */
    COMPRA
}
