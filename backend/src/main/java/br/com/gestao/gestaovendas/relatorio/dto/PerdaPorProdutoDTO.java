package br.com.gestao.gestaovendas.relatorio.dto;

import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;

import java.math.BigDecimal;

public record PerdaPorProdutoDTO(
        Long produtoId,
        Integer codigo,
        String descricao,
        UnidadeMedida unidadeMedida,
        BigDecimal quantidade,
        BigDecimal custo
) {
}
