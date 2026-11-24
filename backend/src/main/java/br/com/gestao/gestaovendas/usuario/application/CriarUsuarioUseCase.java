package br.com.gestao.gestaovendas.usuario.application;

import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.domain.UsuarioValidator;
import br.com.gestao.gestaovendas.usuario.domain.VinculoNegocioService;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioRequestDTO;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioResponseDTO;
import br.com.gestao.gestaovendas.usuario.mapper.UsuarioMapper;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CriarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioValidator usuarioValidator;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final VinculoNegocioService vinculoNegocioService;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO request) {
        usuarioValidator.validaNomeUsuario(request.getNome());
        usuarioValidator.validaEmailUsuario(request.getEmail());

        Usuario novoUsuario = usuarioMapper.toEntity(request);
        novoUsuario.setSenha(passwordEncoder.encode(request.getSenha()));
        vinculoNegocioService.definirNegocios(novoUsuario, request.getNegocioIds());
        novoUsuario.setCriadoEm(LocalDateTime.now());
        novoUsuario.setAtualizadoEm(LocalDateTime.now());

        Usuario salvo = usuarioRepository.save(novoUsuario);

        return usuarioMapper.toResponse(salvo);
    }
}
