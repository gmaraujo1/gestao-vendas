package br.com.gestao.gestaovendas.venda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoRequestDTO(
        @NotBlank(message = "Informe o motivo do cancelamento")
        @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
        String motivo
) {
}
