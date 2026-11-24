package br.com.gestao.gestaovendas.categoria.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH: campos nulos ficam como estão. */
public record CategoriaEditDTO(
        @Size(max = 60, message = "Nome deve ter no máximo 60 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "Nome não pode ser vazio")
        String nome,
        Boolean ativo
) {
}
