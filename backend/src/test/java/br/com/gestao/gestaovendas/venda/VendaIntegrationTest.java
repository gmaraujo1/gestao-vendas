package br.com.gestao.gestaovendas.venda;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VendaIntegrationTest extends ApiIntegrationTest {

    private String admin;
    private Long produto;
    private Long cliente;

    @BeforeEach
    void setUp() throws Exception {
        admin = tokenAdmin();
        produto = novoProduto(laticinio(), "Bombom", "2.50");
        cliente = novoCliente(laticinio(), "Marcelo " + sufixo());
    }

    @Test
    void vendaDaSaidaNoEstoqueEGuardaOCustoDoMomento() throws Exception {
        definirSaldo(laticinio(), produto, "10");

        Long venda = vender(admin, cliente, produto, "3", "5.00");

        assertThat(saldo(laticinio(), produto)).isEqualByComparingTo("7");
        mockMvc.perform(noNegocio(get("/api/venda/" + venda), admin, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(15.0))
                .andExpect(jsonPath("$.status").value("REGISTRADA"))
                .andExpect(jsonPath("$.itens[0].produtoDescricao").value("Bombom"))
                .andExpect(jsonPath("$.itens[0].subtotal").value(15.0));
        mockMvc.perform(noNegocio(get("/api/estoque/produto/" + produto), admin, laticinio()))
                .andExpect(jsonPath("$.movimentacoes.conteudo[0].tipo").value("VENDA"))
                .andExpect(jsonPath("$.movimentacoes.conteudo[0].quantidade").value(-3.0))
                .andExpect(jsonPath("$.movimentacoes.conteudo[0].custoUnitario").value(2.5))
                .andExpect(jsonPath("$.movimentacoes.conteudo[0].vendaId").value(venda));
    }

    @Test
    void estoquePodeFicarNegativo() throws Exception {
        vender(admin, cliente, produto, "2", "5.00");

        assertThat(saldo(laticinio(), produto)).isEqualByComparingTo("-2");
    }

    @Test
    void cancelarDevolveAoEstoqueENaoCancelaDuasVezes() throws Exception {
        definirSaldo(laticinio(), produto, "5");
        Long venda = vender(admin, cliente, produto, "4", "5.00");

        mockMvc.perform(json(noNegocio(post("/api/venda/" + venda + "/cancelar"), admin, laticinio()),
                        "{\"motivo\": \"Lançada em dobro\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.motivoCancelamento").value("Lançada em dobro"));
        assertThat(saldo(laticinio(), produto)).isEqualByComparingTo("5");

        mockMvc.perform(json(noNegocio(post("/api/venda/" + venda + "/cancelar"), admin, laticinio()),
                        "{\"motivo\": \"De novo\"}"))
                .andExpect(status().isUnprocessableEntity());
        assertThat(saldo(laticinio(), produto)).isEqualByComparingTo("5");
    }

    @Test
    void doisCancelamentosAoMesmoTempoEstornamUmaVezSo() throws Exception {
        Long venda = vender(admin, cliente, produto, "10", "5.00");
        assertThat(saldo(laticinio(), produto)).isEqualByComparingTo("-10");

        var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        var largada = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Callable<Integer> cancelar = () -> {
            largada.await();
            return mockMvc.perform(json(noNegocio(post("/api/venda/" + venda + "/cancelar"), admin, laticinio()),
                    "{\"motivo\": \"clique duplo\"}")).andReturn().getResponse().getStatus();
        };
        var f1 = pool.submit(cancelar);
        var f2 = pool.submit(cancelar);
        largada.countDown();
        var status = java.util.List.of(f1.get(), f2.get());
        pool.shutdown();

        assertThat(status).containsExactlyInAnyOrder(200, 422);
        assertThat(saldo(laticinio(), produto)).isEqualByComparingTo("0");
    }

    @Test
    void estornoUsaOCustoDaVendaMesmoSeOCustoDoProdutoMudou() throws Exception {
        Long venda = vender(admin, cliente, produto, "2", "5.00");
        mockMvc.perform(json(noNegocio(patch("/api/produto/" + produto), admin, laticinio()), "{\"precoMedio\": 9.99}"))
                .andExpect(status().isOk());

        mockMvc.perform(json(noNegocio(post("/api/venda/" + venda + "/cancelar"), admin, laticinio()),
                "{\"motivo\": \"x\"}")).andExpect(status().isOk());

        mockMvc.perform(noNegocio(get("/api/estoque/produto/" + produto), admin, laticinio()))
                .andExpect(jsonPath("$.movimentacoes.conteudo[0].tipo").value("VENDA_ESTORNO"))
                .andExpect(jsonPath("$.movimentacoes.conteudo[0].custoUnitario").value(2.5));
    }

    @Test
    void subtotalArredondaComoACalculadora() throws Exception {
        String body = mockMvc.perform(json(noNegocio(post("/api/venda"), admin, laticinio()),
                        vendaJson(cliente, produto, "0.125", "1.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(new java.math.BigDecimal(com.jayway.jsonpath.JsonPath.read(body, "$.total").toString()))
                .isEqualByComparingTo("0.13");
    }

    @Test
    void funcionarioVendeMasNaoCancelaNemAjusta() throws Exception {
        String func = tokenFuncionario(laticinio());
        Long venda = vender(func, cliente, produto, "1", "5.00");

        mockMvc.perform(json(noNegocio(post("/api/venda/" + venda + "/cancelar"), func, laticinio()),
                "{\"motivo\": \"x\"}")).andExpect(status().isForbidden());
        mockMvc.perform(json(noNegocio(post("/api/estoque/ajuste"), func, laticinio()),
                "{\"produtoId\": %d, \"quantidadeContada\": 99}".formatted(produto))).andExpect(status().isForbidden());
    }

    @Test
    void clienteEhObrigatorioDoNegocioEAtivo() throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/venda"), admin, laticinio()), """
                        {"itens": [{"produtoId": %d, "quantidade": 1, "precoUnitario": 1}]}
                        """.formatted(produto)))
                .andExpect(status().isBadRequest());

        Long clientePaoNino = novoCliente(paoNino(), "Padaria " + sufixo());
        mockMvc.perform(json(noNegocio(post("/api/venda"), admin, laticinio()), vendaJson(clientePaoNino, produto, "1", "1")))
                .andExpect(status().isNotFound());

        mockMvc.perform(json(noNegocio(patch("/api/cliente/" + cliente), admin, laticinio()), "{\"ativo\": false}"))
                .andExpect(status().isOk());
        mockMvc.perform(json(noNegocio(post("/api/venda"), admin, laticinio()), vendaJson(cliente, produto, "1", "1")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoVendeProdutoDeOutroNegocioNemInativo() throws Exception {
        Long produtoPaoNino = novoProduto(paoNino(), "Pão", "0.50");
        mockMvc.perform(json(noNegocio(post("/api/venda"), admin, laticinio()), vendaJson(cliente, produtoPaoNino, "1", "1")))
                .andExpect(status().isNotFound());

        mockMvc.perform(json(noNegocio(patch("/api/produto/" + produto), admin, laticinio()), "{\"ativo\": false}"))
                .andExpect(status().isOk());
        mockMvc.perform(json(noNegocio(post("/api/venda"), admin, laticinio()), vendaJson(cliente, produto, "1", "1")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listaPorClienteEPeriodoEAceitaVendaRetroativaMasNaoFutura() throws Exception {
        LocalDateTime ontem = LocalDate.now().minusDays(1).atTime(10, 30);
        mockMvc.perform(json(noNegocio(post("/api/venda"), admin, laticinio()), """
                        {"clienteId": %d, "dataHora": "%s",
                         "itens": [{"produtoId": %d, "quantidade": 100, "precoUnitario": 1.50}]}
                        """.formatted(cliente, ontem, produto)))
                .andExpect(status().isCreated());

        mockMvc.perform(noNegocio(get("/api/venda")
                        .param("clienteId", cliente.toString())
                        .param("de", ontem.toLocalDate().toString())
                        .param("ate", ontem.toLocalDate().toString()), admin, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].total").value(150.0));

        mockMvc.perform(json(noNegocio(post("/api/venda"), admin, laticinio()), """
                        {"clienteId": %d, "dataHora": "%s",
                         "itens": [{"produtoId": %d, "quantidade": 1, "precoUnitario": 1}]}
                        """.formatted(cliente, LocalDateTime.now().plusDays(1), produto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void historicoProtegeProdutoEClienteDeExclusaoEUnidade() throws Exception {
        vender(admin, cliente, produto, "1", "5.00");

        mockMvc.perform(noNegocio(delete("/api/produto/" + produto), admin, laticinio()))
                .andExpect(status().isConflict());
        mockMvc.perform(noNegocio(delete("/api/cliente/" + cliente), admin, laticinio()))
                .andExpect(status().isConflict());
        mockMvc.perform(json(noNegocio(patch("/api/produto/" + produto), admin, laticinio()), "{\"unidadeMedida\": \"CX\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void vendaDeUmNegocioNaoApareceNoOutro() throws Exception {
        Long venda = vender(admin, cliente, produto, "1", "5.00");

        mockMvc.perform(noNegocio(get("/api/venda/" + venda), admin, paoNino()))
                .andExpect(status().isNotFound());
        mockMvc.perform(json(noNegocio(post("/api/venda/" + venda + "/cancelar"), admin, paoNino()),
                "{\"motivo\": \"x\"}")).andExpect(status().isNotFound());
    }

    private Long vender(String token, Long clienteId, Long produtoId, String qtd, String preco) throws Exception {
        return id(mockMvc.perform(json(noNegocio(post("/api/venda"), token, laticinio()),
                        vendaJson(clienteId, produtoId, qtd, preco)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private static String vendaJson(Long clienteId, Long produtoId, String qtd, String preco) {
        return """
                {"clienteId": %d, "itens": [{"produtoId": %d, "quantidade": %s, "precoUnitario": %s}]}
                """.formatted(clienteId, produtoId, qtd, preco);
    }
}
