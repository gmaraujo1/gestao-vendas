package br.com.gestao.gestaovendas.venda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaRequestDTO(
        @NotNull(message = "Cliente é obrigatório")
        Long clienteId,

        @NotEmpty(message = "Informe ao menos um item")
        @Size(max = 200, message = "No máximo 200 itens por venda")
        List<@Valid @NotNull Item> itens,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String observacao,

        /* Opcional: sem valor = agora. Pode ser no passado, não no futuro. */
        LocalDateTime dataHora
) {

    public record Item(
            @NotNull(message = "Produto é obrigatório")
            Long produtoId,

            @NotNull(message = "Quantidade é obrigatória")
            @Positive(message = "Quantidade deve ser maior que zero")
            @Digits(integer = 11, fraction = 3, message = "Quantidade inválida")
            BigDecimal quantidade,

            @NotNull(message = "Preço é obrigatório")
            @PositiveOrZero(message = "Preço não pode ser negativo")
            @Digits(integer = 12, fraction = 2, message = "Preço inválido")
            BigDecimal precoUnitario
    ) {
    }
}
