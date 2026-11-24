package br.com.gestao.gestaovendas.usuario.dto;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDeleteDTO {

    private Long id;

    TipoUsuario tipoUsuario;

}
