package br.com.gestao.gestaovendas.estoque.dto;

import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;

import java.math.BigDecimal;

/** Saldo atual de um produto (soma das movimentações). Pode ser negativo. */
public record SaldoProdutoDTO(
        Long produtoId,
        Integer codigo,
        String descricao,
        UnidadeMedida unidadeMedida,
        String categoriaNome,
        boolean ativo,
        BigDecimal saldo
) {
}
