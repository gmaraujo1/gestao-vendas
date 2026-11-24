package br.com.gestao.gestaovendas.usuario.controller;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import br.com.gestao.gestaovendas.config.SecurityConfig;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtualArgumentResolver;
import br.com.gestao.gestaovendas.usuario.application.CriarUsuarioUseCase;
import br.com.gestao.gestaovendas.usuario.application.EditarUsuarioUseCase;
import br.com.gestao.gestaovendas.usuario.application.ListarUsuarioUseCase;
import br.com.gestao.gestaovendas.usuario.application.RemoverUsuarioUseCase;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UsuarioControllerTest {

    private static RequestPostProcessor admin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor funcionario() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NegocioAtualArgumentResolver negocioAtualArgumentResolver;

    @MockitoBean
    private CriarUsuarioUseCase criarUsuarioUseCase;

    @MockitoBean
    private EditarUsuarioUseCase editarUsuarioUseCase;

    @MockitoBean
    private RemoverUsuarioUseCase removerUsuarioUseCase;

    @MockitoBean
    private ListarUsuarioUseCase listarUsuarioUseCase;

    @Test
    void deveCriarUsuarioComSucesso() throws Exception {

        UsuarioResponseDTO response = new UsuarioResponseDTO();

        when(criarUsuarioUseCase.criar(any()))
                .thenReturn(response);

        String json = """
                {
                    "nome": "João da Silva",
                    "email": "joao@email.com",
                    "senha": "12345678",
                    "tipoUsuario": "FUNCIONARIO",
                    "negocioIds": [1]
                }
                """;

        mockMvc.perform(
                        post("/api/usuario/criar").with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void deveRetornarBadRequestQuandoDadosForemInvalidos() throws Exception {

        String json = """
                {
                    "nome": "",
                    "email": "email-invalido",
                    "senha": "123",
                    "tipoUsuario": null
                }
                """;

        mockMvc.perform(
                        post("/api/usuario/criar").with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveEditarUsuarioComSucesso() throws Exception {

        UsuarioResponseDTO response = new UsuarioResponseDTO();

        when(editarUsuarioUseCase.atualizar(
                eq(1L),
                any()
        )).thenReturn(response);

        String json = """
                {
                    "nome": "João Atualizado"
                }
                """;

        mockMvc.perform(
                        patch("/api/usuario/1/editar").with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveListarUsuariosComSucesso() throws Exception {

        UsuarioResponseDTO usuario1 = new UsuarioResponseDTO(
                1L,
                "João",
                "joao@email.com",
                TipoUsuario.FUNCIONARIO,
                Set.of(1L)
        );

        UsuarioResponseDTO usuario2 = new UsuarioResponseDTO(
                2L,
                "Maria",
                "maria@email.com",
                TipoUsuario.FUNCIONARIO,
                Set.of(1L)
        );

        when(listarUsuarioUseCase.listar())
                .thenReturn(List.of(usuario1, usuario2));

        mockMvc.perform(
                        get("/api/usuario/listar").with(admin())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("João"))
                .andExpect(jsonPath("$[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$[1].nome").value("Maria"))
                .andExpect(jsonPath("$[1].email").value("maria@email.com"));
    }

    @Test
    void deveRemoverUsuarioComSucesso() throws Exception {

        mockMvc.perform(delete("/api/usuario/1/remover").with(admin()))
                .andExpect(status().isNoContent());

        verify(removerUsuarioUseCase).deletar(1L);
    }

    @Test
    void funcionarioNaoPodeGerenciarUsuarios() throws Exception {

        mockMvc.perform(delete("/api/usuario/1/remover").with(funcionario()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(removerUsuarioUseCase);
    }

    @Test
    void semTokenRetornaUnauthorized() throws Exception {

        mockMvc.perform(get("/api/usuario/listar"))
                .andExpect(status().isUnauthorized());
    }
}