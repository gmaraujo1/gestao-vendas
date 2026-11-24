package br.com.gestao.gestaovendas.usuario.domain;

import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioValidatorTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioValidator usuarioValidator;

    @BeforeEach
    void setUp() {
        usuarioValidator = new UsuarioValidator(usuarioRepository);
    }

    @Test
    void devePermitirEmailQuandoNaoExiste() {

        String email = "usuario@email.com";

        when(usuarioRepository.existsByEmail(email))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                usuarioValidator.validaEmailUsuario(email)
        );

        verify(usuarioRepository).existsByEmail(email);
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {

        String email = "usuario@email.com";

        when(usuarioRepository.existsByEmail(email))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> usuarioValidator.validaEmailUsuario(email)
        );

        assertEquals("Email já existente!", exception.getMessage());

        verify(usuarioRepository).existsByEmail(email);
    }

    @Test
    void devePermitirNomeQuandoNaoExiste() {

        String nome = "João da Silva";

        when(usuarioRepository.existsByNome(nome))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                usuarioValidator.validaNomeUsuario(nome)
        );

        verify(usuarioRepository).existsByNome(nome);
    }

    @Test
    void deveLancarExcecaoQuandoNomeJaExiste() {

        String nome = "João da Silva";

        when(usuarioRepository.existsByNome(nome))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> usuarioValidator.validaNomeUsuario(nome)
        );

        assertEquals("Nome já existente!", exception.getMessage());

        verify(usuarioRepository).existsByNome(nome);
    }
}