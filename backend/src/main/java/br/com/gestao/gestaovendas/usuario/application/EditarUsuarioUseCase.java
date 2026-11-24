package br.com.gestao.gestaovendas.usuario.application;

import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.domain.VinculoNegocioService;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioRequestEditDTO;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioResponseDTO;
import br.com.gestao.gestaovendas.usuario.mapper.UsuarioMapper;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EditarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final VinculoNegocioService vinculoNegocioService;

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestEditDTO request) {

        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o usuário"));

        usuarioMapper.updateEntity(usuario, request);
        if (request.getNegocioIds() != null) {
            vinculoNegocioService.definirNegocios(usuario, request.getNegocioIds());
        }
        usuario.setAtualizadoEm(LocalDateTime.now());
        Usuario atualizado = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(atualizado);
    }
}
