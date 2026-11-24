package br.com.gestao.gestaovendas.veiculo.domain;

import java.util.Locale;

public final class Placas {

    private Placas() {
    }

    /** "abc-1234" -> "ABC1234": sem hífen e em maiúsculas, para comparar e evitar duplicadas. */
    public static String normalizar(String placa) {
        if (placa == null || placa.isBlank()) {
            return null;
        }
        return placa.replace("-", "").trim().toUpperCase(Locale.ROOT);
    }
}
