package br.com.gestao.gestaovendas.common.dto;

import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Intervalo [inicio, fim) a partir de datas opcionais da tela (de/ate inclusivos).
 * Sem data, usa limites bem largos: as queries não precisam tratar null.
 */
public record Periodo(LocalDateTime inicio, LocalDateTime fim) {

    private static final LocalDateTime MINIMO = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MAXIMO = LocalDateTime.of(9999, 1, 1, 0, 0);

    public static Periodo de(LocalDate de, LocalDate ate) {
        if (de != null && ate != null && ate.isBefore(de)) {
            throw new RequisicaoInvalidaException("A data final é anterior à inicial");
        }
        return new Periodo(
                de == null ? MINIMO : de.atStartOfDay(),
                ate == null ? MAXIMO : ate.plusDays(1).atStartOfDay());
    }

    public LocalDate inicioData() {
        return inicio.toLocalDate();
    }

    /** Último dia incluído. */
    public LocalDate fimData() {
        return fim.toLocalDate().minusDays(1);
    }
}
