package br.com.gestao.gestaovendas.troca;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrocaIntegrationTest extends ApiIntegrationTest {

    private Long leite;
    private Long iogurte;
    private Long cliente;

    @BeforeEach
    void setUp() throws Exception {
        leite = novoProduto(laticinio(), "Leite", "3.00");
        iogurte = novoProduto(laticinio(), "Iogurte", "2.00");
        cliente = novoCliente(laticinio(), "Dona Maria " + sufixo());
        definirSaldo(laticinio(), leite, "5");
        definirSaldo(laticinio(), iogurte, "5");
    }

    @Test
    void devolvidoVoltaOuViraPerdaEEntregueSai() throws Exception {
        String func = tokenFuncionario(laticinio());

        String body = mockMvc.perform(json(noNegocio(post("/api/troca"), func, laticinio()), """
                        {"clienteId": %d,
                         "devolvidos": [
                           {"produtoId": %d, "quantidade": 1, "destino": "ESTOQUE"},
                           {"produtoId": %d, "quantidade": 2, "destino": "PERDA", "motivoPerda": "VENCIDO"}],
                         "entregues": [{"produtoId": %d, "quantidade": 3}]}
                        """.formatted(cliente, leite, iogurte, iogurte)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movimentacoes[*].tipo")
                        .value(containsInAnyOrder("TROCA_ENTRADA", "TROCA_ENTRADA", "PERDA", "TROCA_SAIDA")))
                .andReturn().getResponse().getContentAsString();

        // leite: 5 + 1 ; iogurte: 5 + 2 - 2 (perda) - 3 (entregue)
        assertThat(saldo(laticinio(), leite)).isEqualByComparingTo("6");
        assertThat(saldo(laticinio(), iogurte)).isEqualByComparingTo("2");

        Long troca = id(body);
        mockMvc.perform(noNegocio(get("/api/troca/" + troca), func, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movimentacoes.length()").value(4));
        mockMvc.perform(noNegocio(get("/api/perda"), func, laticinio()))
                .andExpect(jsonPath("$.conteudo[?(@.trocaId == %d)].motivoPerda".formatted(troca)).value("VENCIDO"));
    }

    @Test
    void perdaSemMotivoDa400() throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/troca"), tokenAdmin(), laticinio()), """
                        {"clienteId": %d,
                         "devolvidos": [{"produtoId": %d, "quantidade": 1, "destino": "PERDA"}]}
                        """.formatted(cliente, leite)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void semDevolvidoDa400() throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/troca"), tokenAdmin(), laticinio()), """
                        {"clienteId": %d, "devolvidos": [], "entregues": [{"produtoId": %d, "quantidade": 1}]}
                        """.formatted(cliente, leite)))
                .andExpect(status().isBadRequest());
    }
}
