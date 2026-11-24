package br.com.gestao.gestaovendas.estoque.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** "Informar estoque": a contagem real; o sistema grava a diferença. */
public record AjusteRequestDTO(
        @NotNull(message = "Produto é obrigatório")
        Long produtoId,

        @NotNull(message = "Quantidade contada é obrigatória")
        @PositiveOrZero(message = "A contagem não pode ser negativa")
        @Digits(integer = 11, fraction = 3, message = "Quantidade inválida")
        BigDecimal quantidadeContada,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String observacao
) {
}
