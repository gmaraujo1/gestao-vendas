package br.com.gestao.gestaovendas.relatorio.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TipoPeriodoTest {

    @ParameterizedTest(name = "{0} de {1} = {2} a {3}")
    @CsvSource({
            "DIA,       2026-11-26, 2026-11-26, 2026-11-26",
            // 26/11/2026 é quinta: semana de segunda a domingo
            "SEMANA,    2026-11-26, 2026-11-23, 2026-11-29",
            "SEMANA,    2026-11-23, 2026-11-23, 2026-11-29",
            "SEMANA,    2026-11-29, 2026-11-23, 2026-11-29",
            // semana que atravessa o ano
            "SEMANA,    2026-01-01, 2025-12-29, 2026-01-04",
            "QUINZENA,  2026-11-15, 2026-11-01, 2026-11-15",
            "QUINZENA,  2026-11-16, 2026-11-16, 2026-11-30",
            "QUINZENA,  2028-02-20, 2028-02-16, 2028-02-29",
            "MES,       2026-02-10, 2026-02-01, 2026-02-28",
            "TRIMESTRE, 2026-11-26, 2026-10-01, 2026-12-31",
            "TRIMESTRE, 2026-04-01, 2026-04-01, 2026-06-30",
            "TRIMESTRE, 2026-03-31, 2026-01-01, 2026-03-31",
            "SEMESTRE,  2026-06-30, 2026-01-01, 2026-06-30",
            "SEMESTRE,  2026-07-01, 2026-07-01, 2026-12-31",
            "ANO,       2026-11-26, 2026-01-01, 2026-12-31"
    })
    void intervaloQueContemAData(TipoPeriodo tipo, LocalDate data, LocalDate inicio, LocalDate fim) {
        assertThat(tipo.inicio(data)).isEqualTo(inicio);
        assertThat(tipo.fim(data)).isEqualTo(fim);
    }
}
