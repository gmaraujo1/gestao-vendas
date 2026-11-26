package br.com.gestao.gestaovendas.relatorio.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Períodos de calendário dos relatórios. Cada um sabe em que intervalo uma data cai.
 * Também serve de agrupamento nas séries do dashboard (vendas por dia, por mês...).
 * A semana vai de segunda a domingo; a quinzena é 1 a 15 ou 16 ao fim do mês.
 */
public enum TipoPeriodo {

    DIA {
        @Override
        public LocalDate inicio(LocalDate data) {
            return data;
        }

        @Override
        public LocalDate fim(LocalDate data) {
            return data;
        }
    },
    SEMANA {
        @Override
        public LocalDate inicio(LocalDate data) {
            return data.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        @Override
        public LocalDate fim(LocalDate data) {
            return inicio(data).plusDays(6);
        }
    },
    QUINZENA {
        @Override
        public LocalDate inicio(LocalDate data) {
            return data.withDayOfMonth(data.getDayOfMonth() <= 15 ? 1 : 16);
        }

        @Override
        public LocalDate fim(LocalDate data) {
            return data.getDayOfMonth() <= 15
                    ? data.withDayOfMonth(15)
                    : data.with(TemporalAdjusters.lastDayOfMonth());
        }
    },
    MES {
        @Override
        public LocalDate inicio(LocalDate data) {
            return data.withDayOfMonth(1);
        }

        @Override
        public LocalDate fim(LocalDate data) {
            return data.with(TemporalAdjusters.lastDayOfMonth());
        }
    },
    TRIMESTRE {
        @Override
        public LocalDate inicio(LocalDate data) {
            return primeiroDiaDoBloco(data, 3);
        }

        @Override
        public LocalDate fim(LocalDate data) {
            return inicio(data).plusMonths(3).minusDays(1);
        }
    },
    SEMESTRE {
        @Override
        public LocalDate inicio(LocalDate data) {
            return primeiroDiaDoBloco(data, 6);
        }

        @Override
        public LocalDate fim(LocalDate data) {
            return inicio(data).plusMonths(6).minusDays(1);
        }
    },
    ANO {
        @Override
        public LocalDate inicio(LocalDate data) {
            return data.withDayOfYear(1);
        }

        @Override
        public LocalDate fim(LocalDate data) {
            return data.with(TemporalAdjusters.lastDayOfYear());
        }
    };

    /** Primeiro dia do período que contém a data. */
    public abstract LocalDate inicio(LocalDate data);

    /** Último dia (inclusivo) do período que contém a data. */
    public abstract LocalDate fim(LocalDate data);

    /** Blocos de meses alinhados ao ano: trimestres jan-mar, abr-jun...; semestres jan-jun, jul-dez. */
    private static LocalDate primeiroDiaDoBloco(LocalDate data, int meses) {
        int mesInicial = ((data.getMonthValue() - 1) / meses) * meses + 1;
        return LocalDate.of(data.getYear(), mesInicial, 1);
    }
}
