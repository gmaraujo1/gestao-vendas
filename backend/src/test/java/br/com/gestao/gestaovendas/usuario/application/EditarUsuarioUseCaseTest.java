package br.com.gestao.gestaovendas.usuario.application;

import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.domain.UsuarioValidator;
import br.com.gestao.gestaovendas.usuario.domain.VinculoNegocioService;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioRequestEditDTO;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioResponseDTO;
import br.com.gestao.gestaovendas.usuario.mapper.UsuarioMapper;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private VinculoNegocioService vinculoNegocioService;

    @Mock
    private UsuarioValidator usuarioValidator;

    @InjectMocks
    private EditarUsuarioUseCase editarUsuarioUseCase;

    private Usuario usuario;
    private UsuarioRequestEditDTO request;
    private UsuarioResponseDTO response;

    @BeforeEach
    void setUp() {

        usuario = new Usuario();

        request = new UsuarioRequestEditDTO();

        response = new UsuarioResponseDTO();
    }

    @Test
    void deveEditarUsuarioComSucesso() {

        Long id = 1L;

        when(usuarioRepository.findById(id))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        when(usuarioMapper.toResponse(usuario))
                .thenReturn(response);

        UsuarioResponseDTO resultado =
                editarUsuarioUseCase.atualizar(id, request);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        assertNotNull(usuario.getAtualizadoEm());

        verify(usuarioRepository).findById(id);
        verify(usuarioMapper).updateEntity(usuario, request);
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponse(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {

        Long id = 999L;

        when(usuarioRepository.findById(id))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> editarUsuarioUseCase.atualizar(id, request)
        );

        assertEquals(
                "Não foi possível encontrar o usuário",
                exception.getMessage()
        );

        verify(usuarioRepository).findById(id);

        verify(usuarioMapper, never())
                .updateEntity(any(), any());

        verify(usuarioRepository, never())
                .save(any());
    }
}