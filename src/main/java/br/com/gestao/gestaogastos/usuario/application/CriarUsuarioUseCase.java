package br.com.gestao.gestaogastos.usuario.application;

import br.com.gestao.gestaogastos.usuario.domain.Usuario;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioRequestDTO;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioResponseDTO;
import br.com.gestao.gestaogastos.usuario.mapper.UsuarioMapper;
import br.com.gestao.gestaogastos.usuario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CriarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO request) {

        Usuario novoUsuario = usuarioMapper.toEntity(request);
        novoUsuario.setCriadoEm(LocalDateTime.now());
        novoUsuario.setAtualizadoEm(LocalDateTime.now());

        Usuario salvo = usuarioRepository.save(novoUsuario);

        return usuarioMapper.toResponse(salvo);
    }
}
