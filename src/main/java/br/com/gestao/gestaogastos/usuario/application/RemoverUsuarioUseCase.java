package br.com.gestao.gestaogastos.usuario.application;

import br.com.gestao.gestaogastos.usuario.domain.Usuario;
import br.com.gestao.gestaogastos.usuario.domain.UsuarioValidator;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioDeleteDTO;
import br.com.gestao.gestaogastos.usuario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoverUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioValidator usuarioValidator;

    @Transactional
    public void deletar(Long id, UsuarioDeleteDTO dto) {
        Usuario usuarioParaDeletar = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Não foi possível encontrar o usuário"));
        usuarioValidator.validaTipoUsuarioParaExclusao(usuarioParaDeletar.getTipoUsuario(), dto.getTipoUsuario());
        usuarioRepository.delete(usuarioParaDeletar);
    }
}
