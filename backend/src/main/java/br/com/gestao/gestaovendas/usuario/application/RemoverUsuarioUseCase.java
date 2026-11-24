package br.com.gestao.gestaovendas.usuario.application;

import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.domain.UsuarioValidator;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoverUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioValidator usuarioValidator;

    @Transactional
    public void deletar(Long id) {
        Usuario usuarioParaDeletar = usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o usuário"));
        usuarioValidator.validaTipoUsuarioParaExclusao(usuarioParaDeletar.getTipoUsuario());
        usuarioRepository.delete(usuarioParaDeletar);
    }
}
