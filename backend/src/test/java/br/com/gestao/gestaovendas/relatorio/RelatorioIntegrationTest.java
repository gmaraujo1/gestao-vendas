package br.com.gestao.gestaovendas.relatorio;

import br.com.gestao.gestaovendas.support.ApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lucro com dados conhecidos. Cada teste monta o mesmo cenário em maio de um ano só dele
 * (os testes compartilham o banco; outras classes não lançam nada nos anos 1990).
 *
 * <p>Cenário de maio (Laticínio): Queijo custa 20,00 e Leite custa 3,3333.
 * <ul>
 *   <li>02/05 venda 1: 2 Queijo × 35,00 + 4 Leite × 5,00 = 90,00 (custo 40,00 + 13,33)</li>
 *   <li>10/05 despesa de gasolina 40,00</li>
 *   <li>15/05 perda de 9 Leite vencidos (custo 30,00)</li>
 *   <li>16/05 troca: 1 Queijo furado devolvido vira perda (custo 20,00) e sai 1 Queijo novo</li>
 *   <li>20/05 venda 2: 1 Queijo × 30,00 (custo 20,00)</li>
 *   <li>21/05 venda 3: cancelada (não conta)</li>
 *   <li>31/05 despesa de pedágio 12,50</li>
 *   <li>01/06 venda e despesa fora do mês; venda no Pão Nino no mesmo dia (não contam)</li>
 * </ul>
 * Maio: vendas 120,00; custo 73,33; lucro bruto 46,67; despesas 52,50; resultado -5,83;
 * perdas 50,00; resultado com perdas -55,83.
 */
class RelatorioIntegrationTest extends ApiIntegrationTest {

    private String admin;
    private Long queijo;
    private Long leite;
    private String nomeQueijo;
    private String nomeLeite;

    @BeforeEach
    void setUp() throws Exception {
        admin = tokenAdmin();
        nomeQueijo = "Queijo " + sufixo();
        nomeLeite = "Leite " + sufixo();
        queijo = novoProduto(laticinio(), nomeQueijo, "20.00");
        leite = novoProduto(laticinio(), nomeLeite, "3.3333");
    }

    @Test
    void lucroDoMesComVendasDespesasEPerdas() throws Exception {
        cenario(1995);

        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()).param("periodo", "MES").param("data", "1995-05-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("MES"))
                .andExpect(jsonPath("$.descricaoPeriodo").value("Maio de 1995"))
                .andExpect(jsonPath("$.de").value("1995-05-01"))
                .andExpect(jsonPath("$.ate").value("1995-05-31"))
                .andExpect(jsonPath("$.resultado.numeroVendas").value(2))
                .andExpect(jsonPath("$.resultado.vendas").value(120.00))
                .andExpect(jsonPath("$.resultado.cmv").value(73.33))
                .andExpect(jsonPath("$.resultado.lucroBruto").value(46.67))
                .andExpect(jsonPath("$.resultado.despesas").value(52.50))
                .andExpect(jsonPath("$.resultado.resultado").value(-5.83))
                .andExpect(jsonPath("$.resultado.perdas").value(50.00))
                .andExpect(jsonPath("$.resultado.resultadoComPerdas").value(-55.83))
                // detalhamento por dia, com os dias sem movimento zerados
                .andExpect(jsonPath("$.agrupamento").value("DIA"))
                .andExpect(jsonPath("$.detalhamento.length()").value(31))
                .andExpect(jsonPath("$.detalhamento[1].inicio").value("1995-05-02"))
                .andExpect(jsonPath("$.detalhamento[1].vendas").value(90.00))
                .andExpect(jsonPath("$.detalhamento[1].cmv").value(53.33))
                .andExpect(jsonPath("$.detalhamento[2].vendas").value(0))
                .andExpect(jsonPath("$.detalhamento[20].numeroVendas").value(0))
                .andExpect(jsonPath("$.despesasPorTipo[0].tipo").value("GASOLINA"))
                .andExpect(jsonPath("$.despesasPorTipo[0].total").value(40.00))
                .andExpect(jsonPath("$.despesasPorTipo[1].tipo").value("PEDAGIO"))
                .andExpect(jsonPath("$.perdasPorMotivo[0].motivo").value("VENCIDO"))
                .andExpect(jsonPath("$.perdasPorMotivo[0].custo").value(30.00))
                .andExpect(jsonPath("$.perdasPorMotivo[1].motivo").value("FURADO"))
                .andExpect(jsonPath("$.perdasPorMotivo[1].custo").value(20.00))
                // no relatório, os produtos são ordenados pelo valor vendido
                .andExpect(jsonPath("$.produtosMaisVendidos[0].descricao").value(nomeQueijo))
                .andExpect(jsonPath("$.produtosMaisVendidos[0].quantidade").value(3))
                .andExpect(jsonPath("$.produtosMaisVendidos[0].vendas").value(100.00))
                .andExpect(jsonPath("$.produtosMaisVendidos[0].lucroBruto").value(40.00))
                .andExpect(jsonPath("$.produtosMaisVendidos[1].descricao").value(nomeLeite));

        // o Pão Nino só vê a venda dele
        mockMvc.perform(relatorio("/api/relatorio/lucro", paoNino()).param("periodo", "MES").param("data", "1995-05-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado.numeroVendas").value(1))
                .andExpect(jsonPath("$.resultado.vendas").value(7.00))
                .andExpect(jsonPath("$.resultado.despesas").value(0))
                .andExpect(jsonPath("$.resultado.perdas").value(0));
    }

    @Test
    void quinzenaEIntervaloLivre() throws Exception {
        cenario(1996);

        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()).param("periodo", "QUINZENA").param("data", "1996-05-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricaoPeriodo").value("2ª quinzena de maio de 1996"))
                .andExpect(jsonPath("$.de").value("1996-05-16"))
                .andExpect(jsonPath("$.resultado.vendas").value(30.00))
                .andExpect(jsonPath("$.resultado.cmv").value(20.00))
                .andExpect(jsonPath("$.resultado.despesas").value(12.50))
                .andExpect(jsonPath("$.resultado.resultado").value(-2.50))
                .andExpect(jsonPath("$.resultado.perdas").value(20.00))
                .andExpect(jsonPath("$.resultado.resultadoComPerdas").value(-22.50));

        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()).param("de", "1996-05-02").param("ate", "1996-05-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").doesNotExist())
                .andExpect(jsonPath("$.resultado.numeroVendas").value(1))
                .andExpect(jsonPath("$.resultado.vendas").value(90.00))
                .andExpect(jsonPath("$.resultado.resultado").value(36.67));
    }

    @Test
    void dashboardSerieRankingEPerdas() throws Exception {
        cenario(1997);

        // maio de 1997 começa numa quinta: a 1ª semana vai de 01 a 04
        mockMvc.perform(relatorio("/api/dashboard/serie", laticinio())
                        .param("periodo", "MES").param("data", "1997-05-01").param("agrupamento", "SEMANA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agrupamento").value("SEMANA"))
                .andExpect(jsonPath("$.pontos.length()").value(5))
                .andExpect(jsonPath("$.pontos[0].inicio").value("1997-05-01"))
                .andExpect(jsonPath("$.pontos[0].fim").value("1997-05-04"))
                .andExpect(jsonPath("$.pontos[0].vendas").value(90.00))
                .andExpect(jsonPath("$.pontos[1].despesas").value(40.00))
                .andExpect(jsonPath("$.pontos[2].perdas").value(50.00))
                .andExpect(jsonPath("$.pontos[3].vendas").value(30.00))
                .andExpect(jsonPath("$.pontos[4].fim").value("1997-05-31"))
                .andExpect(jsonPath("$.pontos[4].despesas").value(12.50))
                .andExpect(jsonPath("$.pontos[4].resultado").value(-12.50))
                .andExpect(jsonPath("$.total.resultado").value(-5.83));

        String maio = "1997-05-01";
        // padrão: por quantidade (Leite 4 > Queijo 3)
        mockMvc.perform(relatorio("/api/dashboard/produtos-mais-vendidos", laticinio()).param("de", maio).param("ate", "1997-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].descricao").value(nomeLeite))
                .andExpect(jsonPath("$[0].quantidade").value(4))
                .andExpect(jsonPath("$[0].cmv").value(13.33));
        mockMvc.perform(relatorio("/api/dashboard/produtos-mais-vendidos", laticinio()).param("de", maio).param("ate", "1997-05-31")
                        .param("criterio", "LUCRO").param("limite", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].descricao").value(nomeQueijo))
                .andExpect(jsonPath("$[0].lucroBruto").value(40.00));

        mockMvc.perform(relatorio("/api/dashboard/perdas", laticinio()).param("periodo", "MES").param("data", maio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custoTotal").value(50.00))
                .andExpect(jsonPath("$.porMotivo[0].motivo").value("VENCIDO"))
                .andExpect(jsonPath("$.porMotivo[0].registros").value(1))
                .andExpect(jsonPath("$.porProduto[0].descricao").value(nomeLeite))
                .andExpect(jsonPath("$.porProduto[0].quantidade").value(9))
                .andExpect(jsonPath("$.porProduto[0].custo").value(30.00))
                .andExpect(jsonPath("$.porProduto[1].descricao").value(nomeQueijo));
    }

    @Test
    void pdfDoRelatorio() throws Exception {
        cenario(1998);

        var resposta = mockMvc.perform(relatorio("/api/relatorio/lucro/pdf", laticinio())
                        .param("periodo", "MES").param("data", "1998-05-17"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"lucro-laticinio-1998-05-01_1998-05-31.pdf\""))
                .andReturn().getResponse();

        byte[] pdf = resposta.getContentAsByteArray();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        String texto = textoDoPdf(pdf);
        assertThat(texto)
                .contains("Maio de 1998")
                .contains("120,00")   // vendas
                .contains("73,33")    // custo das mercadorias vendidas
                .contains("-R$")      // prejuízo
                .contains("55,83")    // resultado com perdas
                .contains(nomeQueijo);
    }

    @Test
    void validacoesDoPeriodo() throws Exception {
        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()).param("periodo", "MES").param("de", "2026-01-01"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()).param("de", "2026-01-01"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()).param("de", "2026-02-01").param("ate", "2026-01-01"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()).param("periodo", "BIMESTRE"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(relatorio("/api/dashboard/serie", laticinio())
                        .param("de", "2020-01-01").param("ate", "2024-12-31").param("agrupamento", "DIA"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(relatorio("/api/dashboard/produtos-mais-vendidos", laticinio())
                        .param("periodo", "MES").param("limite", "0"))
                .andExpect(status().isBadRequest());
        // sem data: período atual
        mockMvc.perform(relatorio("/api/relatorio/lucro", laticinio()).param("periodo", "ANO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agrupamento").value("MES"))
                .andExpect(jsonPath("$.detalhamento.length()").value(12));
    }

    @Test
    void soAdminVeRelatoriosEDashboard() throws Exception {
        String func = tokenFuncionario(laticinio());

        for (String url : new String[]{"/api/relatorio/lucro", "/api/relatorio/lucro/pdf", "/api/dashboard/serie",
                "/api/dashboard/produtos-mais-vendidos", "/api/dashboard/perdas"}) {
            mockMvc.perform(noNegocio(get(url).param("periodo", "MES"), func, laticinio()))
                    .andExpect(status().isForbidden());
        }
        mockMvc.perform(comToken(get("/api/relatorio/lucro").param("periodo", "MES"), admin))
                .andExpect(status().isBadRequest());
    }

    // --- cenário ---

    private void cenario(int ano) throws Exception {
        Long cliente = novoCliente(laticinio(), "Cliente " + sufixo());

        vender(laticinio(), cliente, ano + "-05-02T10:00:00",
                item(queijo, "2", "35.00"), item(leite, "4", "5.00"));
        despesa(laticinio(), ano + "-05-10", "40.00", "GASOLINA");
        perda(leite, "9", "VENCIDO", ano + "-05-15T09:00:00");
        mockMvc.perform(json(noNegocio(post("/api/troca"), admin, laticinio()), """
                        {"clienteId": %d, "dataHora": "%d-05-16T09:00:00",
                         "devolvidos": [{"produtoId": %d, "quantidade": 1, "destino": "PERDA", "motivoPerda": "FURADO"}],
                         "entregues": [{"produtoId": %d, "quantidade": 1}]}
                        """.formatted(cliente, ano, queijo, queijo)))
                .andExpect(status().isCreated());
        vender(laticinio(), cliente, ano + "-05-20T23:59:00", item(queijo, "1", "30.00"));
        Long cancelada = vender(laticinio(), cliente, ano + "-05-21T08:00:00", item(queijo, "10", "35.00"));
        mockMvc.perform(json(noNegocio(post("/api/venda/" + cancelada + "/cancelar"), admin, laticinio()),
                        "{\"motivo\": \"Lançada errado\"}"))
                .andExpect(status().isOk());
        despesa(laticinio(), ano + "-05-31", "12.50", "PEDAGIO");

        // fora do mês ou de outro negócio
        vender(laticinio(), cliente, ano + "-06-01T00:00:00", item(leite, "1", "5.00"));
        despesa(laticinio(), ano + "-06-01", "99.00", "OUTROS");
        Long pao = novoProduto(paoNino(), "Pão " + sufixo(), "1.00");
        Long clientePao = novoCliente(paoNino(), "Cliente " + sufixo());
        vender(paoNino(), clientePao, ano + "-05-02T10:00:00", item(pao, "7", "1.00"));
    }

    private Long vender(Long negocio, Long cliente, String dataHora, String... itens) throws Exception {
        return id(mockMvc.perform(json(noNegocio(post("/api/venda"), admin, negocio),
                        "{\"clienteId\": %d, \"dataHora\": \"%s\", \"itens\": [%s]}"
                                .formatted(cliente, dataHora, String.join(",", itens))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private static String item(Long produto, String quantidade, String preco) {
        return "{\"produtoId\": %d, \"quantidade\": %s, \"precoUnitario\": %s}".formatted(produto, quantidade, preco);
    }

    private void despesa(Long negocio, String data, String valor, String tipo) throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/despesa"), admin, negocio),
                        "{\"data\": \"%s\", \"valor\": %s, \"tipo\": \"%s\"}".formatted(data, valor, tipo)))
                .andExpect(status().isCreated());
    }

    private void perda(Long produto, String quantidade, String motivo, String dataHora) throws Exception {
        mockMvc.perform(json(noNegocio(post("/api/perda"), admin, laticinio()),
                        "{\"produtoId\": %d, \"quantidade\": %s, \"motivo\": \"%s\", \"dataHora\": \"%s\"}"
                                .formatted(produto, quantidade, motivo, dataHora)))
                .andExpect(status().isCreated());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder relatorio(String url, Long negocio) {
        return noNegocio(get(url), admin, negocio);
    }

    private static String textoDoPdf(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        try {
            PdfTextExtractor extrator = new PdfTextExtractor(reader);
            StringBuilder texto = new StringBuilder();
            for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
                texto.append(extrator.getTextFromPage(pagina)).append('\n');
            }
            // o formato de moeda usa espaço não separável (R$ 1,00)
            return texto.toString().replace(' ', ' ');
        } finally {
            reader.close();
        }
    }
}
