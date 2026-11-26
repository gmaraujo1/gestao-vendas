package br.com.gestao.gestaovendas.relatorio.dto;

import br.com.gestao.gestaovendas.relatorio.domain.TipoPeriodo;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Intervalo pedido na query string: {@code periodo} (+ {@code data}, padrão hoje)
 * ou {@code de} e {@code ate} (inclusivos). Não dá para misturar os dois.
 */
public record FiltroPeriodoDTO(
        @Schema(description = "Período de calendário que contém a data")
        TipoPeriodo periodo,

        @Schema(description = "Data de referência do período (padrão: hoje)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate data,

        @Schema(description = "Início do intervalo livre (inclusivo)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate de,

        @Schema(description = "Fim do intervalo livre (inclusivo)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate ate
) {
}
