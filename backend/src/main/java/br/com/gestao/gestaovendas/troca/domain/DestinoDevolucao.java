package br.com.gestao.gestaovendas.troca.domain;

/** O que acontece com o produto que o cliente devolveu. Quem registra a troca escolhe. */
public enum DestinoDevolucao {
    /** Volta para venda. */
    ESTOQUE,
    /** Não serve mais (vencido, furado...): entra e sai como perda. */
    PERDA
}
