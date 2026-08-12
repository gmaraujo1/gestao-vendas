package br.com.gestao.gestaogastos.usuario.domain;

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
        if(emailExistente) {
            throw new ConflictException("Email já existente!");
        }
    }

    public void  validaNomeUsuario(String nome) {
        Boolean existeNome = usuarioRepository.existsByNome(nome);
        if(existeNome) {
            throw new ConflictException("Nome já existente!");
        }
    }

}

