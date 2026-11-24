package br.com.gestao.gestaovendas.estoque.dto;

import br.com.gestao.gestaovendas.estoque.domain.MotivoPerda;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PerdaRequestDTO(
        @NotNull(message = "Produto é obrigatório")
        Long produtoId,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        @Digits(integer = 11, fraction = 3, message = "Quantidade inválida")
        BigDecimal quantidade,

        @NotNull(message = "Motivo é obrigatório")
        MotivoPerda motivo,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String observacao,

        /* Opcional: sem valor = agora. */
        LocalDateTime dataHora
) {
}
