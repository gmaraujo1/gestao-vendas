package br.com.gestao.gestaovendas.veiculo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VeiculoRequestDTO(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 60, message = "Descrição deve ter no máximo 60 caracteres")
        String descricao,

        @Pattern(regexp = VeiculoRequestDTO.PLACA, message = "Placa inválida (ex.: ABC1D23 ou ABC-1234)")
        String placa
) {
    /** Vazio (sem placa) ou Mercosul (ABC1D23) ou antiga (ABC-1234 / ABC1234), sem diferenciar maiúsculas. */
    static final String PLACA = "^$|^[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}$";
}
