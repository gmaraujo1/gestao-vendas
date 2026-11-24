package br.com.gestao.gestaovendas.cliente.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH: campos nulos ficam como estão. */
public record ClienteEditDTO(
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "Nome não pode ser vazio")
        String nome,

        @Pattern(regexp = ClienteRequestDTO.TELEFONE, message = "Telefone inválido")
        String telefone,

        @Size(max = 150, message = "Endereço deve ter no máximo 150 caracteres")
        String endereco,

        @Size(max = 80, message = "Cidade deve ter no máximo 80 caracteres")
        String cidade,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String observacao,

        Boolean ativo
) {
}
