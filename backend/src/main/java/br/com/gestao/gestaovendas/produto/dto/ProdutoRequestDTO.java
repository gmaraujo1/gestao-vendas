package br.com.gestao.gestaovendas.produto.dto;

import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequestDTO(
        @NotNull(message = "Código é obrigatório")
        @Positive(message = "Código deve ser um valor positivo")
        Integer codigo,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 3, max = 100, message = "Descrição deve ter entre 3 e 100 caracteres")
        String descricao,

        @NotNull(message = "Categoria é obrigatória")
        Long categoriaId,

        @NotNull(message = "Unidade de medida é obrigatória")
        UnidadeMedida unidadeMedida,

        /* Custo médio inicial; depois as compras (etapa 5) recalculam. */
        @NotNull(message = "Preço médio é obrigatório")
        @PositiveOrZero(message = "Preço médio não pode ser negativo")
        @Digits(integer = 10, fraction = 4, message = "Preço médio inválido")
        BigDecimal precoMedio,

        @Size(max = 100, message = "Observação deve ter no máximo 100 caracteres")
        String observacao
) {
}
