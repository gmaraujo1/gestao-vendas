package br.com.gestao.gestaovendas;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O front gera o cliente a partir deste JSON: ele precisa subir sem login e trazer o header do negócio. */
class OpenApiIntegrationTest extends ApiIntegrationTest {

    @Test
    void apiDocsPublicoComHeaderDeNegocioEPaginacaoAberta() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/venda'].post.parameters[*].name").value(hasItem("X-Negocio-Id")))
                .andExpect(jsonPath("$.paths['/api/produto'].get.parameters[*].name").value(hasItem("page")))
                .andExpect(jsonPath("$.paths['/api/veiculo'].get.parameters[*].name")
                        .value(org.hamcrest.Matchers.not(hasItem("X-Negocio-Id"))))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                // records aninhados com o mesmo nome (Item) não podem se sobrescrever no schema
                .andExpect(jsonPath("$.components.schemas.VendaItemRequestDTO.properties.precoUnitario").exists())
                .andExpect(jsonPath("$.components.schemas.VendaItemResponseDTO.properties.produtoDescricao").exists());
    }
}
