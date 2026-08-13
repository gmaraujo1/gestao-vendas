package br.com.gestao.gestaogastos.usuario.application;

import br.com.gestao.gestaogastos.common.enums.TipoUsuario;
import br.com.gestao.gestaogastos.usuario.domain.Usuario;
import br.com.gestao.gestaogastos.usuario.domain.UsuarioValidator;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioDeleteDTO;
import br.com.gestao.gestaogastos.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoverUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioValidator usuarioValidator;

    @InjectMocks
    private RemoverUsuarioUseCase removerUsuarioUseCase;

    private Usuario usuario;
    private UsuarioDeleteDTO request;

    @BeforeEach
    void setUp() {

        usuario = new Usuario();

        request = new UsuarioDeleteDTO();
    }

    @Test
    void deveRemoverUsuarioComSucesso() {

        Long id = 1L;

        usuario.setTipoUsuario(TipoUsuario.CLIENTE);
        request.setTipoUsuario(TipoUsuario.CLIENTE);

        when(usuarioRepository.findById(id))
                .thenReturn(Optional.of(usuario));

        removerUsuarioUseCase.deletar(id, request);

        verify(usuarioRepository).findById(id);

        verify(usuarioValidator)
                .validaTipoUsuarioParaExclusao(
                        usuario.getTipoUsuario(),
                        request.getTipoUsuario()
                );

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {

        Long id = 999L;

        when(usuarioRepository.findById(id))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> removerUsuarioUseCase.deletar(id, request)
        );

        assertEquals(
                "Não foi possível encontrar o usuário",
                exception.getMessage()
        );

        verify(usuarioRepository).findById(id);

        verifyNoInteractions(usuarioValidator);

        verify(usuarioRepository, never())
                .delete(any());
    }

    @Test
    void deveLancarExcecaoQuandoTipoNaoPermiteExclusao() {

        Long id = 1L;

        usuario.setTipoUsuario(TipoUsuario.CLIENTE);
        request.setTipoUsuario(TipoUsuario.ADMIN);

        when(usuarioRepository.findById(id))
                .thenReturn(Optional.of(usuario));

        doThrow(new RuntimeException("Usuário não pode ser excluído"))
                .when(usuarioValidator)
                .validaTipoUsuarioParaExclusao(
                        usuario.getTipoUsuario(),
                        request.getTipoUsuario()
                );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> removerUsuarioUseCase.deletar(id, request)
        );

        assertEquals(
                "Usuário não pode ser excluído",
                exception.getMessage()
        );

        verify(usuarioRepository).findById(id);

        verify(usuarioValidator)
                .validaTipoUsuarioParaExclusao(
                        usuario.getTipoUsuario(),
                        request.getTipoUsuario()
                );

        verify(usuarioRepository, never())
                .delete(any());
    }
}