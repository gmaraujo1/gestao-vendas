package br.com.gestao.gestaovendas.produto.dto;

import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** PATCH: campos nulos ficam como estão. O código não muda. */
public record ProdutoRequestEditDTO(
        @Size(min = 3, max = 100, message = "Descrição deve ter entre 3 e 100 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "Descrição não pode ser vazia")
        String descricao,

        Long categoriaId,

        UnidadeMedida unidadeMedida,

        @PositiveOrZero(message = "Preço médio não pode ser negativo")
        @Digits(integer = 10, fraction = 4, message = "Preço médio inválido")
        BigDecimal precoMedio,

        @Size(max = 100, message = "Observação deve ter no máximo 100 caracteres")
        String observacao,

        Boolean ativo
) {
}
