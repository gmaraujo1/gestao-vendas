package br.com.gestao.gestaovendas.usuario;

import br.com.gestao.gestaovendas.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class UsuarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String tokenAdmin;
    private Long laticinio;

    @BeforeEach
    void setUp() throws Exception {
        tokenAdmin = login("admin@teste.com", "senha-admin-teste");
        String negocios = mockMvc.perform(auth(get("/api/negocio/meus")))
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(negocios, "$[?(@.codigo == 'LATICINIO')].id");
        laticinio = ids.get(0).longValue();
    }

    @Test
    void patchParcialMantemCamposNaoEnviados() throws Exception {
        String sufixo = sufixo();
        Long id = criar("Fulano " + sufixo, "fulano." + sufixo + "@teste.com");

        mockMvc.perform(auth(patch("/api/usuario/" + id + "/editar"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"negocioIds\": [%d]}".formatted(laticinio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Fulano " + sufixo))
                .andExpect(jsonPath("$.email").value("fulano." + sufixo + "@teste.com"));
    }

    @Test
    void editarParaEmailDeOutroUsuarioRetorna409() throws Exception {
        String sufixo = sufixo();
        Long id = criar("Ciclano " + sufixo, "ciclano." + sufixo + "@teste.com");

        mockMvc.perform(auth(patch("/api/usuario/" + id + "/editar"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ADMIN@teste.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void emailEhNormalizadoNoCadastroENoLogin() throws Exception {
        String sufixo = sufixo();
        criar("Beltrano " + sufixo, "Beltrano." + sufixo + "@TESTE.com");

        login("beltrano." + sufixo + "@teste.com", "senha-forte-123");
        login("BELTRANO." + sufixo + "@teste.COM", "senha-forte-123");
    }

    @Test
    void headerDeNegocioNaoNumericoRetorna400() throws Exception {
        mockMvc.perform(auth(get("/api/produto")).header("X-Negocio-Id", "abc"))
                .andExpect(status().isBadRequest());
    }

    private Long criar(String nome, String email) throws Exception {
        String body = mockMvc.perform(auth(post("/api/usuario/criar"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "%s", "email": "%s", "senha": "senha-forte-123",
                                 "tipoUsuario": "FUNCIONARIO", "negocioIds": [%d]}
                                """.formatted(nome, email, laticinio)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req) {
        return req.header("Authorization", "Bearer " + tokenAdmin);
    }

    private static String sufixo() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String login(String email, String senha) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
