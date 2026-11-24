package br.com.gestao.gestaovendas.despesa.dto;

import br.com.gestao.gestaovendas.despesa.domain.TipoDespesa;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaRequestDTO(
        /* Opcional: sem valor = hoje. Não pode ser no futuro. */
        LocalDate data,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        @Digits(integer = 12, fraction = 2, message = "Valor inválido")
        BigDecimal valor,

        @NotNull(message = "Tipo é obrigatório")
        TipoDespesa tipo,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricao,

        Long veiculoId
) {
}
