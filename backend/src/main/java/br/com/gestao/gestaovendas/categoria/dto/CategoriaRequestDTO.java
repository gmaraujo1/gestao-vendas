package br.com.gestao.gestaovendas.categoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 60, message = "Nome deve ter no máximo 60 caracteres")
        String nome
) {
}
