package br.com.gestao.gestaovendas.cliente.dto;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String telefone,
        String endereco,
        String cidade,
        String observacao,
        boolean ativo
) {
}
