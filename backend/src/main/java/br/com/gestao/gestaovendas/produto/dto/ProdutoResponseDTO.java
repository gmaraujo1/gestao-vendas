package br.com.gestao.gestaovendas.produto.dto;

import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id,
        Integer codigo,
        String descricao,
        Long categoriaId,
        String categoriaNome,
        UnidadeMedida unidadeMedida,
        BigDecimal precoMedio,
        String observacao,
        boolean ativo
) {
}
