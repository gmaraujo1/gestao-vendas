package br.com.gestao.gestaovendas.relatorio.domain;

import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Intervalo fechado [de, ate] de um relatório. Vem de um período de calendário
 * ({@code periodo=MES&data=2026-11-10}) ou de datas livres ({@code de=...&ate=...}).
 *
 * @param tipo período de calendário, ou null quando o intervalo é livre
 */
public record IntervaloRelatorio(TipoPeriodo tipo, LocalDate de, LocalDate ate) {

    /** Teto do intervalo livre: mantém as consultas e o PDF de tamanho razoável. */
    static final int MAXIMO_DIAS = 366 * 5;
    /** Teto de pontos de uma série (ex.: agrupar por dia um intervalo de 5 anos). */
    static final int MAXIMO_FAIXAS = 400;
    /** Datas aceitas: evita estouro de LocalDate/timestamp com valores absurdos (500 em vez de 400). */
    static final LocalDate MENOR_DATA = LocalDate.of(1900, 1, 1);
    static final LocalDate MAIOR_DATA = LocalDate.of(2999, 12, 31);

    public static IntervaloRelatorio resolver(TipoPeriodo periodo, LocalDate data, LocalDate de, LocalDate ate,
                                              LocalDate hoje) {
        if (periodo != null) {
            if (de != null || ate != null) {
                throw new RequisicaoInvalidaException("Informe o período ou as datas (de/ate), não os dois");
            }
            LocalDate referencia = data == null ? hoje : validar(data);
            return new IntervaloRelatorio(periodo, periodo.inicio(referencia), periodo.fim(referencia));
        }
        if (data != null) {
            throw new RequisicaoInvalidaException("O parâmetro data só vale junto com o período");
        }
        if (de == null || ate == null) {
            throw new RequisicaoInvalidaException("Informe o período (ex.: periodo=MES) ou as datas de e ate");
        }
        validar(de);
        validar(ate);
        if (ate.isBefore(de)) {
            throw new RequisicaoInvalidaException("A data final é anterior à inicial");
        }
        if (ChronoUnit.DAYS.between(de, ate) + 1 > MAXIMO_DIAS) {
            throw new RequisicaoInvalidaException("O intervalo pode ter no máximo 5 anos");
        }
        return new IntervaloRelatorio(null, de, ate);
    }

    private static LocalDate validar(LocalDate data) {
        if (data.isBefore(MENOR_DATA) || data.isAfter(MAIOR_DATA)) {
            throw new RequisicaoInvalidaException("Data fora do intervalo aceito (1900 a 2999)");
        }
        return data;
    }

    /** Limites para as consultas por data/hora ([inicio, fim)). */
    public Periodo periodo() {
        return Periodo.de(de, ate);
    }

    public long dias() {
        return ChronoUnit.DAYS.between(de, ate) + 1;
    }

    /** Detalhamento padrão: por dia até um mês, por semana até dois meses, por mês até dois anos. */
    public TipoPeriodo agrupamentoPadrao() {
        long dias = dias();
        if (dias <= 31) {
            return TipoPeriodo.DIA;
        }
        if (dias <= 62) {
            return TipoPeriodo.SEMANA;
        }
        if (dias <= 731) {
            return TipoPeriodo.MES;
        }
        return TipoPeriodo.TRIMESTRE;
    }

    /**
     * Divide o intervalo em faixas do agrupamento, cortando a primeira e a última nos limites
     * (ex.: semana de segunda a domingo, mas o mês começa numa quinta).
     */
    public List<Faixa> faixas(TipoPeriodo agrupamento) {
        List<Faixa> faixas = new ArrayList<>();
        LocalDate inicio = de;
        while (!inicio.isAfter(ate)) {
            if (faixas.size() == MAXIMO_FAIXAS) {
                throw new RequisicaoInvalidaException(
                        "Agrupamento detalhado demais para o intervalo; escolha um maior (ex.: MES)");
            }
            LocalDate fim = agrupamento.fim(inicio);
            faixas.add(new Faixa(inicio, fim.isAfter(ate) ? ate : fim));
            inicio = fim.plusDays(1);
        }
        return faixas;
    }

    public record Faixa(LocalDate inicio, LocalDate fim) {
    }
}
