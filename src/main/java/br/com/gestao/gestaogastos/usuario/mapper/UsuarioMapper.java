package br.com.gestao.gestaogastos.usuario.mapper;

import br.com.gestao.gestaogastos.usuario.domain.Usuario;
import br.com.gestao.gestaogastos.usuario.domain.UsuarioValidator;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioRequestDTO;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioRequestEditDTO;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@RequiredArgsConstructor

@Component
public class UsuarioMapper {

    private final UsuarioValidator usuarioValidator;

    public Usuario toEntity(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuarioValidator.validaNomeUsuario(dto.getNome());
        usuario.setNome(dto.getNome());
        usuarioValidator.validaEmailUsuario(dto.getEmail());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setTipoUsuario(dto.getTipoUsuario());
        return usuario;
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipoUsuario()
        );
    }

    public void updateEntity(Usuario usuario, UsuarioRequestEditDTO dto){
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
    }

}
