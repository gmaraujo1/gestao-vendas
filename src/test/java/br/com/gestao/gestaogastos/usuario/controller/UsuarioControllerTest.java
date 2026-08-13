package br.com.gestao.gestaogastos.usuario.controller;

import br.com.gestao.gestaogastos.common.enums.TipoUsuario;
import br.com.gestao.gestaogastos.usuario.application.CriarUsuarioUseCase;
import br.com.gestao.gestaogastos.usuario.application.EditarUsuarioUseCase;
import br.com.gestao.gestaogastos.usuario.application.ListarUsuarioUseCase;
import br.com.gestao.gestaogastos.usuario.application.RemoverUsuarioUseCase;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                    "nome": "Jo達o da Silva",
                    "email": "joao@email.com",
                    "senha": "123456",
                    "tipoUsuario": "CLIENTE"
                }
                """;

        mockMvc.perform(
                        post("/api/usuario/criar")
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
                        post("/api/usuario/criar")
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
                    "nome": "Jo達o Atualizado"
                }
                """;

        mockMvc.perform(
                        patch("/api/usuario/1/editar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveListarUsuariosComSucesso() throws Exception {

        UsuarioResponseDTO usuario1 = new UsuarioResponseDTO(
                1L,
                "Jo達o",
                "joao@email.com",
                TipoUsuario.CLIENTE
        );

        UsuarioResponseDTO usuario2 = new UsuarioResponseDTO(
                2L,
                "Maria",
                "maria@email.com",
                TipoUsuario.CLIENTE
        );

        when(listarUsuarioUseCase.listar())
                .thenReturn(List.of(usuario1, usuario2));

        mockMvc.perform(
                        get("/api/usuario/listar")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Jo達o"))
                .andExpect(jsonPath("$[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$[1].nome").value("Maria"))
                .andExpect(jsonPath("$[1].email").value("maria@email.com"));
    }

    @Test
    void deveRemoverUsuarioComSucesso() throws Exception {

        String json = """
                {
                    "id": 1,
                    "nome": "Administrador",
                    "tipoUsuario": "ADMIN"
                }
                """;

        mockMvc.perform(
                        delete("/api/usuario/1/remover")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNoContent());
    }
}