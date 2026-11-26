package br.com.gestao.gestaovendas.relatorio.dto;

import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;

import java.math.BigDecimal;

public record ProdutoVendidoDTO(
        Long produtoId,
        Integer codigo,
        String descricao,
        UnidadeMedida unidadeMedida,
        BigDecimal quantidade,
        BigDecimal vendas,
        BigDecimal cmv,
        BigDecimal lucroBruto
) {

    public static ProdutoVendidoDTO de(Long produtoId, Integer codigo, String descricao, UnidadeMedida unidade,
                                       BigDecimal quantidade, BigDecimal vendas, BigDecimal cmv) {
        return new ProdutoVendidoDTO(produtoId, codigo, descricao, unidade, quantidade, vendas, cmv,
                vendas.subtract(cmv));
    }
}
