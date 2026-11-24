package br.com.gestao.gestaovendas.produto;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProdutoIntegrationTest extends ApiIntegrationTest {

    private static final AtomicInteger CODIGO = new AtomicInteger(500_000);

    private String token;
    private Long categoriaLaticinio;

    @BeforeEach
    void setUp() throws Exception {
        token = tokenAdmin();
        categoriaLaticinio = criarCategoria(laticinio(), "Produtos " + sufixo());
    }

    @Test
    void cadastraComCategoriaERetornaNomeDela() throws Exception {
        String body = criarProduto(laticinio(), categoriaLaticinio, "Requeijão");

        mockMvc.perform(noNegocio(get("/api/produto").param("categoriaId", categoriaLaticinio.toString()),
                        token, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].descricao").value("Requeijão"))
                .andExpect(jsonPath("$.conteudo[0].categoriaId").value(categoriaLaticinio))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void naoAceitaCategoriaDeOutroNegocio() throws Exception {
        Long categoriaPaoNino = criarCategoria(paoNino(), "Pães " + sufixo());

        mockMvc.perform(json(noNegocio(post("/api/produto"), token, laticinio()),
                        produtoJson(categoriaPaoNino, "Intruso")))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchNaoMoveProdutoParaCategoriaDeOutroNegocio() throws Exception {
        Long id = id(criarProduto(laticinio(), categoriaLaticinio, "Manteiga"));
        Long categoriaPaoNino = criarCategoria(paoNino(), "Pães " + sufixo());

        mockMvc.perform(json(noNegocio(patch("/api/produto/" + id), token, laticinio()),
                        "{\"categoriaId\": %d}".formatted(categoriaPaoNino)))
                .andExpect(status().isNotFound());
    }

    @Test
    void editaProdutoDeCategoriaQueFoiDesativadaDepois() throws Exception {
        Long id = id(criarProduto(laticinio(), categoriaLaticinio, "Pudim"));
        mockMvc.perform(json(noNegocio(patch("/api/categoria/" + categoriaLaticinio), token, laticinio()),
                "{\"ativo\": false}")).andExpect(status().isOk());

        // o front manda o formulário inteiro, com a categoria atual
        mockMvc.perform(json(noNegocio(patch("/api/produto/" + id), token, laticinio()),
                        "{\"categoriaId\": %d, \"precoMedio\": 6.10}".formatted(categoriaLaticinio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoMedio").value(6.1));
    }

    @Test
    void funcionarioConsultaMasNaoAlteraProdutosNemCategorias() throws Exception {
        Long id = id(criarProduto(laticinio(), categoriaLaticinio, "Ricota"));
        String tokenFunc = tokenFuncionario(laticinio());

        mockMvc.perform(noNegocio(get("/api/produto"), tokenFunc, laticinio())).andExpect(status().isOk());
        mockMvc.perform(noNegocio(get("/api/categoria"), tokenFunc, laticinio())).andExpect(status().isOk());
        mockMvc.perform(json(noNegocio(post("/api/produto"), tokenFunc, laticinio()),
                produtoJson(categoriaLaticinio, "Do funcionário"))).andExpect(status().isForbidden());
        mockMvc.perform(json(noNegocio(patch("/api/produto/" + id), tokenFunc, laticinio()),
                "{\"precoMedio\": 0.01}")).andExpect(status().isForbidden());
        mockMvc.perform(noNegocio(delete("/api/produto/" + id), tokenFunc, laticinio()))
                .andExpect(status().isForbidden());
        mockMvc.perform(json(noNegocio(post("/api/categoria"), tokenFunc, laticinio()),
                "{\"nome\": \"Do funcionário\"}")).andExpect(status().isForbidden());
    }

    @Test
    void naoAceitaCategoriaInativa() throws Exception {
        mockMvc.perform(json(noNegocio(patch("/api/categoria/" + categoriaLaticinio), token, laticinio()),
                "{\"ativo\": false}")).andExpect(status().isOk());

        mockMvc.perform(json(noNegocio(post("/api/produto"), token, laticinio()),
                        produtoJson(categoriaLaticinio, "Sem categoria ativa")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void patchParcialMantemOsOutrosCampos() throws Exception {
        Long id = id(criarProduto(laticinio(), categoriaLaticinio, "Doce de leite"));

        mockMvc.perform(json(noNegocio(patch("/api/produto/" + id), token, laticinio()), "{\"ativo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Doce de leite"))
                .andExpect(jsonPath("$.precoMedio").value(4.5))
                .andExpect(jsonPath("$.categoriaId").value(categoriaLaticinio))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void listagemEhPaginada() throws Exception {
        for (int i = 0; i < 3; i++) {
            criarProduto(laticinio(), categoriaLaticinio, "Item " + i);
        }

        mockMvc.perform(noNegocio(get("/api/produto")
                        .param("categoriaId", categoriaLaticinio.toString())
                        .param("size", "2").param("page", "1"), token, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void ordenacaoPorCampoInexistenteDa400() throws Exception {
        mockMvc.perform(noNegocio(get("/api/produto").param("sort", "naoExiste"), token, laticinio()))
                .andExpect(status().isBadRequest());
    }

    private String criarProduto(Long negocioId, Long categoriaId, String descricao) throws Exception {
        return mockMvc.perform(json(noNegocio(post("/api/produto"), token, negocioId),
                        produtoJson(categoriaId, descricao)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private static String produtoJson(Long categoriaId, String descricao) {
        return """
                {"codigo": %d, "descricao": "%s", "categoriaId": %d,
                 "unidadeMedida": "UN", "precoMedio": 4.50}
                """.formatted(CODIGO.incrementAndGet(), descricao, categoriaId);
    }

    private Long criarCategoria(Long negocioId, String nome) throws Exception {
        return id(mockMvc.perform(json(noNegocio(post("/api/categoria"), token, negocioId),
                        "{\"nome\": \"%s\"}".formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }
}
