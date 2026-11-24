package br.com.gestao.gestaovendas.usuario.application;

import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.domain.UsuarioValidator;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioDeleteDTO;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
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
