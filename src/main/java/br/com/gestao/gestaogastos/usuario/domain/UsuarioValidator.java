package br.com.gestao.gestaogastos.usuario.domain;

import br.com.gestao.gestaogastos.common.enums.TipoUsuario;
import br.com.gestao.gestaogastos.exception.AccessRecusedException;
import br.com.gestao.gestaogastos.exception.ConflictException;
import br.com.gestao.gestaogastos.usuario.repository.UsuarioRepository;
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

    public void validaTipoUsuarioParaExclusao(TipoUsuario usuarioParaDeletar, TipoUsuario usuarioResponsavel) {
        if (usuarioResponsavel != TipoUsuario.ADMIN) {
            throw new AccessRecusedException("Apenas usuários do tipo ADMIN podem deletar outro usuário");
        }
        if (usuarioParaDeletar == TipoUsuario.ADMIN) {
            throw new AccessRecusedException("Não é possível remover um usuário do tipo ADMIN");
        }
    }
}

