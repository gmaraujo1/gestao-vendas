package br.com.gestao.gestaovendas.estoque;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EstoqueIntegrationTest extends ApiIntegrationTest {

    private String admin;
    private Long produto;

    @BeforeEach
    void setUp() throws Exception {
        admin = tokenAdmin();
        produto = novoProduto(laticinio(), "Queijo " + sufixo(), "20.00");
    }

    @Test
    void ajusteGravaSoADiferenca() throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/estoque/ajuste"), admin, laticinio()),
                        "{\"produtoId\": %d, \"quantidadeContada\": 12.5}".formatted(produto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAnterior").value(0))
                .andExpect(jsonPath("$.diferenca").value(12.5));

        mockMvc.perform(json(noNegocio(post("/api/estoque/ajuste"), admin, laticinio()),
                        "{\"produtoId\": %d, \"quantidadeContada\": 12.5}".formatted(produto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diferenca").value(0))
                .andExpect(jsonPath("$.movimentacaoId").doesNotExist());

        mockMvc.perform(json(noNegocio(post("/api/estoque/ajuste"), admin, laticinio()),
                        "{\"produtoId\": %d, \"quantidadeContada\": 10}".formatted(produto)))
                .andExpect(jsonPath("$.diferenca").value(-2.5));
        assertThat(saldo(laticinio(), produto)).isEqualByComparingTo("10");
    }

    @Test
    void perdaAvulsaBaixaOEstoqueEApareceNaListaDoPeriodo() throws Exception {
        definirSaldo(laticinio(), produto, "4");
        String func = tokenFuncionario(laticinio());

        mockMvc.perform(json(noNegocio(post("/api/perda"), func, laticinio()), """
                        {"produtoId": %d, "quantidade": 1, "motivo": "FURADO", "observacao": "caixa amassada"}
                        """.formatted(produto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidade").value(-1.0))
                .andExpect(jsonPath("$.custoUnitario").value(20.0));
        assertThat(saldo(laticinio(), produto)).isEqualByComparingTo("3");

        String hoje = LocalDate.now().toString();
        mockMvc.perform(noNegocio(get("/api/perda").param("de", hoje).param("ate", hoje), func, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[?(@.produtoId == %d)].motivoPerda".formatted(produto)).value("FURADO"));

        String amanha = LocalDate.now().plusDays(1).toString();
        mockMvc.perform(noNegocio(get("/api/perda").param("de", amanha).param("ate", amanha), func, laticinio()))
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void perdaSemMotivoOuComQuantidadeZeroDa400() throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/perda"), admin, laticinio()),
                        "{\"produtoId\": %d, \"quantidade\": 1}".formatted(produto)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(noNegocio(post("/api/perda"), admin, laticinio()),
                        "{\"produtoId\": %d, \"quantidade\": 0, \"motivo\": \"VENCIDO\"}".formatted(produto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaDeSaldosTrazProdutoSemMovimentacaoComZeroESoDoNegocio() throws Exception {
        String descricao = "Sem mov " + sufixo();
        novoProduto(laticinio(), descricao, "1.00");
        novoProduto(paoNino(), descricao, "1.00");

        mockMvc.perform(noNegocio(get("/api/estoque").param("descricao", descricao), admin, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].saldo").value(0));
    }

    @Test
    void saldoDeCadaNegocioSoSomaAsProprias() throws Exception {
        String descricao = "Com mov " + sufixo();
        Long doLaticinio = novoProduto(laticinio(), descricao, "1.00");
        Long doPaoNino = novoProduto(paoNino(), descricao, "1.00");
        definirSaldo(laticinio(), doLaticinio, "7");
        definirSaldo(paoNino(), doPaoNino, "40");

        mockMvc.perform(noNegocio(get("/api/estoque").param("descricao", descricao), admin, laticinio()))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].saldo").value(7.0));
        mockMvc.perform(noNegocio(get("/api/estoque").param("descricao", descricao), admin, paoNino()))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].saldo").value(40.0));
    }

    @Test
    void extratoDeProdutoDeOutroNegocioDa404() throws Exception {
        mockMvc.perform(noNegocio(get("/api/estoque/produto/" + produto), admin, paoNino()))
                .andExpect(status().isNotFound());
    }
}
