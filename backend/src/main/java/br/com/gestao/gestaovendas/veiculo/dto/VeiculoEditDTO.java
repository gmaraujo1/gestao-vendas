package br.com.gestao.gestaovendas.veiculo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH: campos nulos ficam como estão. */
public record VeiculoEditDTO(
        @Size(max = 60, message = "Descrição deve ter no máximo 60 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "Descrição não pode ser vazia")
        String descricao,

        @Pattern(regexp = VeiculoRequestDTO.PLACA, message = "Placa inválida (ex.: ABC1D23 ou ABC-1234)")
        String placa,

        Boolean ativo
) {
}
