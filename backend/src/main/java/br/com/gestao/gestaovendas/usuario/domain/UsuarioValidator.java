package br.com.gestao.gestaovendas.usuario.domain;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import br.com.gestao.gestaovendas.exception.AccessRecusedException;
import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioValidator {

    private final UsuarioRepository usuarioRepository;

    public void validaEmailUsuario(String email) {
        Boolean emailExistente = usuarioRepository.existsByEmail(email);
        if (emailExistente) {
            throw new ConflictException("Email já existente!");
        }
    }

    public void validaNomeUsuario(String nome) {
        Boolean existeNome = usuarioRepository.existsByNome(nome);
        if (existeNome) {
            throw new ConflictException("Nome já existente!");
        }
    }

    /** Só ADMIN chega aqui (ver SecurityConfig); mesmo assim, um ADMIN não remove outro. */
    public void validaTipoUsuarioParaExclusao(TipoUsuario usuarioParaDeletar) {
        if (usuarioParaDeletar == TipoUsuario.ADMIN) {
            throw new AccessRecusedException("Não é possível remover um usuário do tipo ADMIN");
        }
    }
}

