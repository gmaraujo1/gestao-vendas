package br.com.gestao.gestaovendas.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Perdas do intervalo (avulsas e de trocas), por motivo e pelos produtos que mais perderam. */
public record ResumoPerdasDTO(
        LocalDate de,
        LocalDate ate,
        BigDecimal custoTotal,
        List<PerdaPorMotivoDTO> porMotivo,
        List<PerdaPorProdutoDTO> porProduto
) {
}
