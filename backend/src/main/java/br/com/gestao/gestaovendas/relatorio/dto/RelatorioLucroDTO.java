package br.com.gestao.gestaovendas.relatorio.dto;

import br.com.gestao.gestaovendas.despesa.dto.ResumoDespesasDTO;
import br.com.gestao.gestaovendas.relatorio.domain.TipoPeriodo;

import java.time.LocalDate;
import java.util.List;

/**
 * Relatório de lucro e prejuízo: o mesmo conteúdo do PDF, em JSON.
 *
 * @param periodo          período de calendário pedido, ou null para intervalo livre
 * @param descricaoPeriodo texto pronto para a tela (ex.: "Novembro de 2026")
 * @param detalhamento     o intervalo quebrado por dia, semana ou mês (ver {@code agrupamento})
 */
public record RelatorioLucroDTO(
        TipoPeriodo periodo,
        String descricaoPeriodo,
        LocalDate de,
        LocalDate ate,
        ResultadoDTO resultado,
        TipoPeriodo agrupamento,
        List<PontoSerieDTO> detalhamento,
        List<ResumoDespesasDTO.PorTipo> despesasPorTipo,
        List<PerdaPorMotivoDTO> perdasPorMotivo,
        List<ProdutoVendidoDTO> produtosMaisVendidos
) {
}
