package br.com.gestao.gestaovendas.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Pattern(regexp = ClienteRequestDTO.TELEFONE, message = "Telefone inválido")
        String telefone,

        @Size(max = 150, message = "Endereço deve ter no máximo 150 caracteres")
        String endereco,

        @Size(max = 80, message = "Cidade deve ter no máximo 80 caracteres")
        String cidade,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String observacao
) {
    /** Vazio (limpa o campo) ou dígitos, espaço, parênteses, hífen e +, de 8 a 20. Ex.: (11) 98765-4321 */
    static final String TELEFONE = "^$|^[0-9+()\\-\\s]{8,20}$";
}
