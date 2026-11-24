package br.com.gestao.gestaovendas.usuario.application;

import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioResponseDTO;
import br.com.gestao.gestaovendas.usuario.mapper.UsuarioMapper;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private ListarUsuarioUseCase listarUsuarioUseCase;

    private Usuario usuario1;
    private Usuario usuario2;

    private UsuarioResponseDTO response1;
    private UsuarioResponseDTO response2;

    @BeforeEach
    void setUp() {

        usuario1 = new Usuario();
        usuario2 = new Usuario();

        response1 = new UsuarioResponseDTO();
        response2 = new UsuarioResponseDTO();
    }

    @Test
    void deveListarTodosOsUsuariosComSucesso() {

        when(usuarioRepository.findAll())
                .thenReturn(List.of(usuario1, usuario2));

        when(usuarioMapper.toResponse(usuario1))
                .thenReturn(response1);

        when(usuarioMapper.toResponse(usuario2))
                .thenReturn(response2);

        List<UsuarioResponseDTO> resultado =
                listarUsuarioUseCase.listar();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        assertEquals(response1, resultado.get(0));
        assertEquals(response2, resultado.get(1));

        verify(usuarioRepository).findAll();
        verify(usuarioMapper).toResponse(usuario1);
        verify(usuarioMapper).toResponse(usuario2);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremUsuarios() {

        when(usuarioRepository.findAll())
                .thenReturn(List.of());

        List<UsuarioResponseDTO> resultado =
                listarUsuarioUseCase.listar();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(usuarioRepository).findAll();

        verifyNoInteractions(usuarioMapper);
    }
}