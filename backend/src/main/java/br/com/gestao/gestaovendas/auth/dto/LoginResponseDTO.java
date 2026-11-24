package br.com.gestao.gestaovendas.auth.dto;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;

import java.time.Instant;

public record LoginResponseDTO(
        String token,
        Instant expiraEm,
        Long usuarioId,
        String nome,
        TipoUsuario tipoUsuario
) {
}
