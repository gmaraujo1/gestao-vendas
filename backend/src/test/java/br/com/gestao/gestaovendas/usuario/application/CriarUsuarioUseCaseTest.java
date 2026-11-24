package br.com.gestao.gestaovendas.usuario.application;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.domain.UsuarioValidator;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioRequestDTO;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioResponseDTO;
import br.com.gestao.gestaovendas.usuario.mapper.UsuarioMapper;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private CriarUsuarioUseCase criarUsuarioUseCase;

    private UsuarioRequestDTO request;
    private Usuario usuario;
    private UsuarioResponseDTO response;

    @BeforeEach
    void setUp() {

        request = new UsuarioRequestDTO(
                "João da Silva",
                "joao@email.com",
                "123456",
                TipoUsuario.CLIENTE
        );

        usuario = new Usuario();

        response = new UsuarioResponseDTO();
    }

    @Test
    void deveCriarUsuarioComSucesso() {

        when(usuarioMapper.toEntity(request))
                .thenReturn(usuario);

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        when(usuarioMapper.toResponse(usuario))
                .thenReturn(response);

        UsuarioResponseDTO resultado =
                criarUsuarioUseCase.criar(request);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        assertNotNull(usuario.getCriadoEm());
        assertNotNull(usuario.getAtualizadoEm());

        verify(usuarioValidator).validaNomeUsuario(request.getNome());
        verify(usuarioValidator).validaEmailUsuario(request.getEmail());
        verify(usuarioMapper).toEntity(request);
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponse(usuario);
    }

    @Test
    void deveSalvarUsuarioApenasUmaVez() {

        when(usuarioMapper.toEntity(request))
                .thenReturn(usuario);

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        when(usuarioMapper.toResponse(usuario))
                .thenReturn(response);

        criarUsuarioUseCase.criar(request);

        verify(usuarioRepository, times(1))
                .save(usuario);
    }
}