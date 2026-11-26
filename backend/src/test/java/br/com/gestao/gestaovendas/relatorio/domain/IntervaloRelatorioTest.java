package br.com.gestao.gestaovendas.relatorio.domain;

import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntervaloRelatorioTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 26);

    @Test
    void periodoSemDataUsaHoje() {
        var intervalo = IntervaloRelatorio.resolver(TipoPeriodo.MES, null, null, null, HOJE);

        assertThat(intervalo.de()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(intervalo.ate()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(intervalo.tipo()).isEqualTo(TipoPeriodo.MES);
        assertThat(DescricaoPeriodo.de(intervalo)).isEqualTo("Novembro de 2026");
    }

    @Test
    void intervaloLivre() {
        var intervalo = IntervaloRelatorio.resolver(null, null, LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 9), HOJE);

        assertThat(intervalo.tipo()).isNull();
        assertThat(intervalo.dias()).isEqualTo(7);
        assertThat(intervalo.periodo().inicio()).isEqualTo(LocalDate.of(2026, 11, 3).atStartOfDay());
        assertThat(intervalo.periodo().fim()).isEqualTo(LocalDate.of(2026, 11, 10).atStartOfDay());
        assertThat(DescricaoPeriodo.de(intervalo)).isEqualTo("03/11/2026 a 09/11/2026");
    }

    @Test
    void parametrosInvalidos() {
        LocalDate d = LocalDate.of(2026, 11, 1);
        assertThatThrownBy(() -> IntervaloRelatorio.resolver(TipoPeriodo.MES, null, d, d, HOJE))
                .isInstanceOf(RequisicaoInvalidaException.class);
        assertThatThrownBy(() -> IntervaloRelatorio.resolver(null, null, d, null, HOJE))
                .isInstanceOf(RequisicaoInvalidaException.class);
        assertThatThrownBy(() -> IntervaloRelatorio.resolver(null, d, null, null, HOJE))
                .isInstanceOf(RequisicaoInvalidaException.class);
        assertThatThrownBy(() -> IntervaloRelatorio.resolver(null, null, d, d.minusDays(1), HOJE))
                .isInstanceOf(RequisicaoInvalidaException.class);
        assertThatThrownBy(() -> IntervaloRelatorio.resolver(null, null, d, d.plusYears(6), HOJE))
                .isInstanceOf(RequisicaoInvalidaException.class);
        assertThatThrownBy(() -> IntervaloRelatorio.resolver(TipoPeriodo.ANO, LocalDate.MAX, null, null, HOJE))
                .isInstanceOf(RequisicaoInvalidaException.class);
        assertThatThrownBy(() -> IntervaloRelatorio.resolver(null, null, LocalDate.of(1800, 1, 1), d, HOJE))
                .isInstanceOf(RequisicaoInvalidaException.class);
    }

    @Test
    void faixasCortadasNosLimites() {
        // maio de 2003 começa numa quinta: a primeira semana é cortada em 01/05
        var maio = IntervaloRelatorio.resolver(TipoPeriodo.MES, LocalDate.of(2003, 5, 10), null, null, HOJE);

        var semanas = maio.faixas(TipoPeriodo.SEMANA);

        assertThat(semanas).containsExactly(
                new IntervaloRelatorio.Faixa(LocalDate.of(2003, 5, 1), LocalDate.of(2003, 5, 4)),
                new IntervaloRelatorio.Faixa(LocalDate.of(2003, 5, 5), LocalDate.of(2003, 5, 11)),
                new IntervaloRelatorio.Faixa(LocalDate.of(2003, 5, 12), LocalDate.of(2003, 5, 18)),
                new IntervaloRelatorio.Faixa(LocalDate.of(2003, 5, 19), LocalDate.of(2003, 5, 25)),
                new IntervaloRelatorio.Faixa(LocalDate.of(2003, 5, 26), LocalDate.of(2003, 5, 31)));
        assertThat(maio.faixas(TipoPeriodo.DIA)).hasSize(31);
        assertThat(maio.faixas(TipoPeriodo.ANO)).containsExactly(
                new IntervaloRelatorio.Faixa(LocalDate.of(2003, 5, 1), LocalDate.of(2003, 5, 31)));
    }

    @Test
    void agrupamentoPadraoPeloTamanho() {
        assertThat(IntervaloRelatorio.resolver(TipoPeriodo.MES, null, null, null, HOJE).agrupamentoPadrao())
                .isEqualTo(TipoPeriodo.DIA);
        assertThat(IntervaloRelatorio.resolver(TipoPeriodo.TRIMESTRE, null, null, null, HOJE).agrupamentoPadrao())
                .isEqualTo(TipoPeriodo.MES);
        assertThat(IntervaloRelatorio.resolver(TipoPeriodo.ANO, null, null, null, HOJE).agrupamentoPadrao())
                .isEqualTo(TipoPeriodo.MES);
    }

    @Test
    void agrupamentoDetalhadoDemaisEhRecusado() {
        var cincoAnos = IntervaloRelatorio.resolver(null, null, LocalDate.of(2020, 1, 1), LocalDate.of(2024, 12, 31), HOJE);

        assertThatThrownBy(() -> cincoAnos.faixas(TipoPeriodo.DIA)).isInstanceOf(RequisicaoInvalidaException.class);
        assertThat(cincoAnos.faixas(TipoPeriodo.MES)).hasSize(60);
    }

    @Test
    void descricoes() {
        assertThat(DescricaoPeriodo.de(IntervaloRelatorio.resolver(TipoPeriodo.QUINZENA, LocalDate.of(2026, 3, 20), null, null, HOJE)))
                .isEqualTo("2ª quinzena de março de 2026");
        assertThat(DescricaoPeriodo.de(IntervaloRelatorio.resolver(TipoPeriodo.TRIMESTRE, LocalDate.of(2026, 5, 2), null, null, HOJE)))
                .isEqualTo("2º trimestre de 2026");
        assertThat(DescricaoPeriodo.de(IntervaloRelatorio.resolver(TipoPeriodo.SEMESTRE, LocalDate.of(2026, 8, 2), null, null, HOJE)))
                .isEqualTo("2º semestre de 2026");
        assertThat(DescricaoPeriodo.de(IntervaloRelatorio.resolver(TipoPeriodo.DIA, LocalDate.of(2026, 8, 2), null, null, HOJE)))
                .isEqualTo("02/08/2026");
    }
}
