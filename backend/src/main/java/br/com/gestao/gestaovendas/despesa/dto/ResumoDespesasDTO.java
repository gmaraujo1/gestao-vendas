package br.com.gestao.gestaovendas.despesa.dto;

import br.com.gestao.gestaovendas.despesa.domain.TipoDespesa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Totais do período, por tipo e por veículo (veiculoId null = sem veículo). */
public record ResumoDespesasDTO(
        LocalDate de,
        LocalDate ate,
        BigDecimal total,
        List<PorTipo> porTipo,
        List<PorVeiculo> porVeiculo
) {

    public record PorTipo(TipoDespesa tipo, BigDecimal total) {
    }

    public record PorVeiculo(Long veiculoId, String veiculoDescricao, BigDecimal total) {
    }
}
