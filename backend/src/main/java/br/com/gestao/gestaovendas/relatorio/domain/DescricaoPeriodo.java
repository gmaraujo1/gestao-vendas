package br.com.gestao.gestaovendas.relatorio.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Texto do período para a tela e o PDF: "Novembro de 2026", "2º trimestre de 2026"... */
public final class DescricaoPeriodo {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DescricaoPeriodo() {
    }

    public static String de(IntervaloRelatorio intervalo) {
        LocalDate de = intervalo.de();
        if (intervalo.tipo() == null) {
            return de.equals(intervalo.ate())
                    ? DATA.format(de)
                    : DATA.format(de) + " a " + DATA.format(intervalo.ate());
        }
        return switch (intervalo.tipo()) {
            case DIA -> DATA.format(de);
            case SEMANA -> "Semana de " + DATA.format(de) + " a " + DATA.format(intervalo.ate());
            case QUINZENA -> (de.getDayOfMonth() == 1 ? "1ª" : "2ª") + " quinzena de " + mes(de) + " de " + de.getYear();
            case MES -> maiuscula(mes(de)) + " de " + de.getYear();
            case TRIMESTRE -> ((de.getMonthValue() - 1) / 3 + 1) + "º trimestre de " + de.getYear();
            case SEMESTRE -> (de.getMonthValue() == 1 ? "1º" : "2º") + " semestre de " + de.getYear();
            case ANO -> "Ano de " + de.getYear();
        };
    }

    private static String mes(LocalDate data) {
        return data.getMonth().getDisplayName(TextStyle.FULL, PT_BR);
    }

    private static String maiuscula(String texto) {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
