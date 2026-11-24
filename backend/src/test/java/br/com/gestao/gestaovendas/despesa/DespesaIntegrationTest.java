package br.com.gestao.gestaovendas.despesa;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DespesaIntegrationTest extends ApiIntegrationTest {

    private String admin;
    private Long veiculo;
    /** Dia exclusivo por teste para os totais não somarem despesas de outros testes. */
    private LocalDate dia;

    @BeforeEach
    void setUp() throws Exception {
        admin = tokenAdmin();
        veiculo = id(mockMvc.perform(json(comToken(post("/api/veiculo"), admin),
                        "{\"descricao\": \"Teste %s\"}".formatted(sufixo())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        dia = LocalDate.of(2020, 1, 1).plusDays(Math.abs(sufixo().hashCode() % 2000));
    }

    @Test
    void resumoSomaPorTipoEPorVeiculo() throws Exception {
        registrar(admin, laticinio(), "{\"data\": \"%s\", \"valor\": 150.00, \"tipo\": \"GASOLINA\", \"veiculoId\": %d}"
                .formatted(dia, veiculo));
        registrar(admin, laticinio(), "{\"data\": \"%s\", \"valor\": 50.00, \"tipo\": \"GASOLINA\", \"veiculoId\": %d}"
                .formatted(dia, veiculo));
        registrar(admin, laticinio(), "{\"data\": \"%s\", \"valor\": 12.40, \"tipo\": \"PEDAGIO\"}".formatted(dia));
        registrar(admin, paoNino(), "{\"data\": \"%s\", \"valor\": 999, \"tipo\": \"PNEU\"}".formatted(dia));

        mockMvc.perform(noNegocio(get("/api/despesa/resumo").param("de", dia.toString()).param("ate", dia.toString()),
                        admin, laticinio()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(212.4))
                .andExpect(jsonPath("$.porTipo[0].tipo").value("GASOLINA"))
                .andExpect(jsonPath("$.porTipo[0].total").value(200.0))
                .andExpect(jsonPath("$.porVeiculo[?(@.veiculoId == %d)].total".formatted(veiculo)).value(200.0));

        mockMvc.perform(noNegocio(get("/api/despesa").param("de", dia.toString()).param("ate", dia.toString())
                        .param("tipo", "GASOLINA"), admin, laticinio()))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.conteudo[0].veiculoDescricao").exists());
    }

    @Test
    void funcionarioLancaMasNaoEditaNemExclui() throws Exception {
        String func = tokenFuncionario(laticinio());
        Long id = registrar(func, laticinio(), "{\"valor\": 80, \"tipo\": \"GASOLINA\", \"veiculoId\": %d}".formatted(veiculo));

        mockMvc.perform(json(noNegocio(patch("/api/despesa/" + id), func, laticinio()), "{\"valor\": 1}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(noNegocio(delete("/api/despesa/" + id), func, laticinio()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEditaDesvinculaVeiculoEExclui() throws Exception {
        Long id = registrar(admin, laticinio(), "{\"valor\": 300, \"tipo\": \"MECANICA\", \"veiculoId\": %d}".formatted(veiculo));

        mockMvc.perform(json(noNegocio(patch("/api/despesa/" + id), admin, laticinio()),
                        "{\"semVeiculo\": true, \"descricao\": \"troca de óleo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculoId").doesNotExist())
                .andExpect(jsonPath("$.valor").value(300.0))
                .andExpect(jsonPath("$.descricao").value("troca de óleo"));

        mockMvc.perform(noNegocio(delete("/api/despesa/" + id), admin, paoNino()))
                .andExpect(status().isNotFound());
        mockMvc.perform(noNegocio(delete("/api/despesa/" + id), admin, laticinio()))
                .andExpect(status().isNoContent());
    }

    @Test
    void veiculoComDespesaNaoEhExcluido() throws Exception {
        registrar(admin, laticinio(), "{\"valor\": 10, \"tipo\": \"PEDAGIO\", \"veiculoId\": %d}".formatted(veiculo));

        mockMvc.perform(comToken(delete("/api/veiculo/" + veiculo), admin))
                .andExpect(status().isConflict());
    }

    @Test
    void validacoes() throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/despesa"), admin, laticinio()),
                        "{\"valor\": 10, \"tipo\": \"GASOLINA\", \"data\": \"%s\"}".formatted(LocalDate.now().plusDays(2))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(noNegocio(post("/api/despesa"), admin, laticinio()), "{\"valor\": 0, \"tipo\": \"GASOLINA\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(noNegocio(post("/api/despesa"), admin, laticinio()), "{\"valor\": 10, \"tipo\": \"LANCHE\"}"))
                .andExpect(status().isBadRequest());
    }

    private Long registrar(String token, Long negocioId, String body) throws Exception {
        return id(mockMvc.perform(json(noNegocio(post("/api/despesa"), token, negocioId), body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }
}
