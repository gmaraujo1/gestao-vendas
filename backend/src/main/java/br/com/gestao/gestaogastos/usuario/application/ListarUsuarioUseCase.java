package br.com.gestao.gestaogastos.usuario.application;

import br.com.gestao.gestaogastos.usuario.dto.UsuarioResponseDTO;
import br.com.gestao.gestaogastos.usuario.mapper.UsuarioMapper;
import br.com.gestao.gestaogastos.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toResponse).toList();
    }
}
