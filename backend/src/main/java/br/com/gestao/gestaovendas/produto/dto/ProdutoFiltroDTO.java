package br.com.gestao.gestaovendas.produto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoFiltroDTO(
        String descricao,
        Long categoriaId,
        Boolean ativo,
        BigDecimal precoMinimo,
        BigDecimal precoMaximo,
        LocalDate atualizadoDe,
        LocalDate atualizadoAte,
        LocalDate criadoDe,
        LocalDate criadoAte
) {

    public static ProdutoFiltroDTO vazio() {
        return new ProdutoFiltroDTO(null, null, null, null, null, null, null, null, null);
    }
}
