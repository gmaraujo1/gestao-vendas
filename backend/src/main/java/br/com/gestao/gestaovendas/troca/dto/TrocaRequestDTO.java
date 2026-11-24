package br.com.gestao.gestaovendas.troca.dto;

import br.com.gestao.gestaovendas.estoque.domain.MotivoPerda;
import br.com.gestao.gestaovendas.troca.domain.DestinoDevolucao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TrocaRequestDTO(
        @NotNull(message = "Cliente é obrigatório")
        Long clienteId,

        @NotEmpty(message = "Informe ao menos um produto devolvido")
        @Size(max = 100, message = "No máximo 100 itens devolvidos")
        List<@Valid @NotNull Devolvido> devolvidos,

        /* Pode ser vazia (só devolução). */
        @Size(max = 100, message = "No máximo 100 itens entregues")
        List<@Valid @NotNull Entregue> entregues,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String observacao,

        LocalDateTime dataHora
) {

    public record Devolvido(
            @NotNull(message = "Produto é obrigatório")
            Long produtoId,

            @NotNull(message = "Quantidade é obrigatória")
            @Positive(message = "Quantidade deve ser maior que zero")
            @Digits(integer = 11, fraction = 3, message = "Quantidade inválida")
            BigDecimal quantidade,

            @NotNull(message = "Informe se o devolvido volta ao estoque ou vira perda")
            DestinoDevolucao destino,

            /* Obrigatório quando destino = PERDA. */
            MotivoPerda motivoPerda
    ) {
        @AssertTrue(message = "Informe o motivo da perda do produto devolvido")
        boolean isMotivoInformadoQuandoPerda() {
            return destino != DestinoDevolucao.PERDA || motivoPerda != null;
        }
    }

    public record Entregue(
            @NotNull(message = "Produto é obrigatório")
            Long produtoId,

            @NotNull(message = "Quantidade é obrigatória")
            @Positive(message = "Quantidade deve ser maior que zero")
            @Digits(integer = 11, fraction = 3, message = "Quantidade inválida")
            BigDecimal quantidade
    ) {
    }
}
