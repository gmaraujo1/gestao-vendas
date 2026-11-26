package br.com.gestao.gestaovendas.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Um ponto da série (um dia, uma semana, um mês...), achatado para os gráficos. */
public record PontoSerieDTO(
        LocalDate inicio,
        LocalDate fim,
        long numeroVendas,
        BigDecimal vendas,
        BigDecimal cmv,
        BigDecimal lucroBruto,
        BigDecimal despesas,
        BigDecimal resultado,
        BigDecimal perdas,
        BigDecimal resultadoComPerdas
) {

    public static PontoSerieDTO de(LocalDate inicio, LocalDate fim, ResultadoDTO r) {
        return new PontoSerieDTO(inicio, fim, r.numeroVendas(), r.vendas(), r.cmv(), r.lucroBruto(),
                r.despesas(), r.resultado(), r.perdas(), r.resultadoComPerdas());
    }
}
