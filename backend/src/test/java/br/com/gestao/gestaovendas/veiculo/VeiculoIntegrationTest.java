package br.com.gestao.gestaovendas.veiculo;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeiculoIntegrationTest extends ApiIntegrationTest {

    @Test
    void fiorinoECaminhaoJaVemCadastradosEFuncionarioConsultaSemHeaderDeNegocio() throws Exception {
        String tokenFunc = tokenFuncionario(laticinio());

        mockMvc.perform(comToken(get("/api/veiculo"), tokenFunc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao").value(hasItems("Fiorino", "Caminhão")));
    }

    @Test
    void funcionarioNaoCadastraVeiculo() throws Exception {
        String tokenFunc = tokenFuncionario(laticinio());

        mockMvc.perform(json(comToken(post("/api/veiculo"), tokenFunc), "{\"descricao\": \"Moto\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void placaEhNormalizadaENaoRepete() throws Exception {
        String token = tokenAdmin();
        String placa = placaAleatoria();

        mockMvc.perform(json(comToken(post("/api/veiculo"), token),
                        "{\"descricao\": \"Kombi\", \"placa\": \"%s\"}".formatted(placa.toLowerCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value(placa.replace("-", "")));

        mockMvc.perform(json(comToken(post("/api/veiculo"), token),
                        "{\"descricao\": \"Outra\", \"placa\": \"%s\"}".formatted(placa)))
                .andExpect(status().isConflict());
    }

    @Test
    void placaInvalidaDa400() throws Exception {
        mockMvc.perform(json(comToken(post("/api/veiculo"), tokenAdmin()),
                        "{\"descricao\": \"Carro\", \"placa\": \"12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminDesativaVeiculo() throws Exception {
        String token = tokenAdmin();
        Long id = id(mockMvc.perform(json(comToken(post("/api/veiculo"), token), "{\"descricao\": \"Reserva\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(json(comToken(patch("/api/veiculo/" + id), token), "{\"ativo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Reserva"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    private static String placaAleatoria() {
        var r = ThreadLocalRandom.current();
        String letras = "" + (char) ('A' + r.nextInt(26)) + (char) ('A' + r.nextInt(26)) + (char) ('A' + r.nextInt(26));
        return letras + "-" + r.nextInt(10) + (char) ('A' + r.nextInt(26)) + r.nextInt(10) + r.nextInt(10);
    }
}
