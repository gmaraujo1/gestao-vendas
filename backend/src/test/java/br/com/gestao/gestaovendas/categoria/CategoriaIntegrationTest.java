package br.com.gestao.gestaovendas.categoria;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoriaIntegrationTest extends ApiIntegrationTest {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = tokenAdmin();
    }

    @Test
    void laticinioJaVemComCategoriasSugeridas() throws Exception {
        mockMvc.perform(noNegocio(get("/api/categoria"), token, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(hasItems("Leites", "Queijos", "Iogurtes")));
    }

    @Test
    void categoriaDeUmNegocioNaoApareceNoOutro() throws Exception {
        String nome = "Pães " + sufixo();
        criar(paoNino(), nome);

        mockMvc.perform(noNegocio(get("/api/categoria"), token, laticinio()))
                .andExpect(jsonPath("$[*].nome").value(not(hasItem(nome))));
    }

    @Test
    void nomeRepetidoNoMesmoNegocioDa409SemDiferenciarMaiusculas() throws Exception {
        String nome = "Manteigas " + sufixo();
        criar(laticinio(), nome);

        mockMvc.perform(json(noNegocio(post("/api/categoria"), token, laticinio()),
                        "{\"nome\": \"%s\"}".formatted(nome.toUpperCase())))
                .andExpect(status().isConflict());
    }

    @Test
    void desativadaSomeDaListaDeAtivas() throws Exception {
        String nome = "Sazonais " + sufixo();
        Long id = criar(laticinio(), nome);

        mockMvc.perform(json(noNegocio(patch("/api/categoria/" + id), token, laticinio()), "{\"ativo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(noNegocio(get("/api/categoria").param("apenasAtivas", "true"), token, laticinio()))
                .andExpect(jsonPath("$[*].nome").value(not(hasItem(nome))));
    }

    @Test
    void naoRemoveCategoriaComProduto() throws Exception {
        Long categoria = criar(laticinio(), "Com produto " + sufixo());
        mockMvc.perform(json(noNegocio(post("/api/produto"), token, laticinio()), """
                        {"codigo": %d, "descricao": "Coalhada", "categoriaId": %d,
                         "unidadeMedida": "UN", "precoMedio": 3.00}
                        """.formatted(Math.abs(sufixo().hashCode()) % 1_000_000 + 1000, categoria)))
                .andExpect(status().isCreated());

        mockMvc.perform(noNegocio(delete("/api/categoria/" + categoria), token, laticinio()))
                .andExpect(status().isConflict());
    }

    @Test
    void removeCategoriaVaziaENaoAlcancaOutroNegocio() throws Exception {
        Long id = criar(laticinio(), "Vazia " + sufixo());

        mockMvc.perform(noNegocio(delete("/api/categoria/" + id), token, paoNino()))
                .andExpect(status().isNotFound());
        mockMvc.perform(noNegocio(delete("/api/categoria/" + id), token, laticinio()))
                .andExpect(status().isNoContent());
    }

    private Long criar(Long negocioId, String nome) throws Exception {
        String body = mockMvc.perform(json(noNegocio(post("/api/categoria"), token, negocioId),
                        "{\"nome\": \"%s\"}".formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return id(body);
    }
}
