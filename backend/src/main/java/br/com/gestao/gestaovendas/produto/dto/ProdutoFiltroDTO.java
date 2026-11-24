package br.com.gestao.gestaovendas.produto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoFiltroDTO(
        String descricao,
        BigDecimal precoMinimo,
        BigDecimal precoMaximo,
        LocalDate atualizadoDe,
        LocalDate atualizadoAte,
        LocalDate criadoDe,
        LocalDate criadoAte
) {
}
