package br.com.gestao.gestaogastos.usuario.controller;

import br.com.gestao.gestaogastos.common.enums.TipoUsuario;
import br.com.gestao.gestaogastos.usuario.application.CriarUsuarioUseCase;
import br.com.gestao.gestaogastos.usuario.application.EditarUsuarioUseCase;
import br.com.gestao.gestaogastos.usuario.application.RemoverUsuarioUseCase; // Importado o novo UseCase
import br.com.gestao.gestaogastos.usuario.dto.UsuarioResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete; // Importado para testar o delete
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

    @Test
    void deveCriarUsuarioComSucesso() throws Exception {

        UsuarioResponseDTO response = new UsuarioResponseDTO();

        when(criarUsuarioUseCase.criar(any()))
                .thenReturn(response);

        String json = """
                {
                    "nome": "João da Silva",
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
                    "nome": "João Atualizado"
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
