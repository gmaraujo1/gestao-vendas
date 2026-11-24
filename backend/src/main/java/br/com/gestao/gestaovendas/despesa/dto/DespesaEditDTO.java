package br.com.gestao.gestaovendas.despesa.dto;

import br.com.gestao.gestaovendas.despesa.domain.TipoDespesa;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** PATCH: campos nulos ficam como estão. semVeiculo = true desvincula o veículo. */
public record DespesaEditDTO(
        LocalDate data,

        @Positive(message = "Valor deve ser maior que zero")
        @Digits(integer = 12, fraction = 2, message = "Valor inválido")
        BigDecimal valor,

        TipoDespesa tipo,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricao,

        Long veiculoId,

        Boolean semVeiculo
) {
}
