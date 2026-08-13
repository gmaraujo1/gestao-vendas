package br.com.gestao.gestaogastos.usuario.dto;

import br.com.gestao.gestaogastos.common.enums.TipoUsuario;
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
