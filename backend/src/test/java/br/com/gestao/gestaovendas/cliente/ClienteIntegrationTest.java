package br.com.gestao.gestaovendas.cliente;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClienteIntegrationTest extends ApiIntegrationTest {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = tokenAdmin();
    }

    @Test
    void cadastraEBuscaPorParteDoNome() throws Exception {
        String sobrenome = "Silva" + sufixo();
        criar(laticinio(), """
                {"nome": "Marcelo %s", "telefone": "(11) 98765-4321",
                 "endereco": "Rua A, 10", "cidade": "Itu", "observacao": "paga no fim do mês"}
                """.formatted(sobrenome));

        mockMvc.perform(noNegocio(get("/api/cliente").param("nome", sobrenome.toLowerCase()), token, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].telefone").value("(11) 98765-4321"))
                .andExpect(jsonPath("$.conteudo[0].cidade").value("Itu"));
    }

    @Test
    void funcionarioDoLaticinioNaoVeClienteDoPaoNino() throws Exception {
        Long id = criar(paoNino(), "{\"nome\": \"Padaria Central\"}");
        String tokenFunc = tokenFuncionario(laticinio());

        mockMvc.perform(noNegocio(get("/api/cliente/" + id), tokenFunc, paoNino()))
                .andExpect(status().isForbidden());
        mockMvc.perform(noNegocio(get("/api/cliente/" + id), tokenFunc, laticinio()))
                .andExpect(status().isNotFound());
    }

    @Test
    void funcionarioCadastraEEditaClienteMasNaoExclui() throws Exception {
        String tokenFunc = tokenFuncionario(laticinio());

        Long id = id(mockMvc.perform(json(noNegocio(post("/api/cliente"), tokenFunc, laticinio()),
                        "{\"nome\": \"Cliente do funcionário\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(json(noNegocio(patch("/api/cliente/" + id), tokenFunc, laticinio()),
                        "{\"cidade\": \"Itu\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(noNegocio(delete("/api/cliente/" + id), tokenFunc, laticinio()))
                .andExpect(status().isForbidden());
    }

    @Test
    void telefoneInvalidoDa400() throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/cliente"), token, laticinio()),
                        "{\"nome\": \"Fulano\", \"telefone\": \"abc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchParcialEDesativar() throws Exception {
        Long id = criar(laticinio(), "{\"nome\": \"Joana\", \"cidade\": \"Sorocaba\"}");

        mockMvc.perform(json(noNegocio(patch("/api/cliente/" + id), token, laticinio()),
                        "{\"telefone\": \"11999990000\", \"ativo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joana"))
                .andExpect(jsonPath("$.cidade").value("Sorocaba"))
                .andExpect(jsonPath("$.telefone").value("11999990000"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void patchComTextoVazioLimpaOCampo() throws Exception {
        Long id = criar(laticinio(), "{\"nome\": \" Ana \", \"telefone\": \"11999990000\", \"cidade\": \"Itu\"}");

        mockMvc.perform(json(noNegocio(patch("/api/cliente/" + id), token, laticinio()),
                        "{\"telefone\": \"\", \"cidade\": \"  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.telefone").doesNotExist())
                .andExpect(jsonPath("$.cidade").doesNotExist());
    }

    @Test
    void paginacaoComNomesRepetidosNaoRepeteNemPulaCliente() throws Exception {
        String nome = "Homônimo " + sufixo();
        for (int i = 0; i < 5; i++) {
            criar(laticinio(), "{\"nome\": \"%s\"}".formatted(nome));
        }
        java.util.Set<Integer> vistos = new java.util.HashSet<>();
        for (int pagina = 0; pagina < 3; pagina++) {
            String body = mockMvc.perform(noNegocio(get("/api/cliente")
                            .param("nome", nome).param("size", "2").param("page", String.valueOf(pagina)),
                            token, laticinio()))
                    .andReturn().getResponse().getContentAsString();
            java.util.List<Integer> ids = com.jayway.jsonpath.JsonPath.read(body, "$.conteudo[*].id");
            vistos.addAll(ids);
        }
        org.assertj.core.api.Assertions.assertThat(vistos).hasSize(5);
    }

    @Test
    void ordenacaoPorCampoInexistenteDa400() throws Exception {
        mockMvc.perform(noNegocio(get("/api/cliente").param("sort", "naoExiste"), token, laticinio()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscaComPorcentagemNaoCasaTudo() throws Exception {
        criar(laticinio(), "{\"nome\": \"Cliente comum %s\"}".formatted(sufixo()));

        mockMvc.perform(noNegocio(get("/api/cliente").param("nome", "%"), token, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void removeSoNoProprioNegocio() throws Exception {
        Long id = criar(laticinio(), "{\"nome\": \"Temporário\"}");

        mockMvc.perform(noNegocio(delete("/api/cliente/" + id), token, paoNino()))
                .andExpect(status().isNotFound());
        mockMvc.perform(noNegocio(delete("/api/cliente/" + id), token, laticinio()))
                .andExpect(status().isNoContent());
    }

    private Long criar(Long negocioId, String body) throws Exception {
        return id(mockMvc.perform(json(noNegocio(post("/api/cliente"), token, negocioId), body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }
}
