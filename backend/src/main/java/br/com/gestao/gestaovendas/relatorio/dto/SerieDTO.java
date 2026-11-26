package br.com.gestao.gestaovendas.relatorio.dto;

import br.com.gestao.gestaovendas.relatorio.domain.TipoPeriodo;

import java.time.LocalDate;
import java.util.List;

/** Vendas, custos, despesas, perdas e resultado ao longo do intervalo, com o total no fim. */
public record SerieDTO(
        LocalDate de,
        LocalDate ate,
        TipoPeriodo agrupamento,
        List<PontoSerieDTO> pontos,
        ResultadoDTO total
) {
}
