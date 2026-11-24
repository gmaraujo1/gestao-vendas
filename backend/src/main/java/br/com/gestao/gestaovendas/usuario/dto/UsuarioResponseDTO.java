package br.com.gestao.gestaovendas.usuario.dto;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;

    private String nome;

    private String email;

    private TipoUsuario tipoUsuario;

    private Set<Long> negocioIds;

}
