package br.com.gestao.gestaovendas.common.text;

import java.util.Locale;

public final class Emails {

    private Emails() {
    }

    /** E-mail sem espaços e em minúsculas: "Mae@X.com " e "mae@x.com" são a mesma conta. */
    public static String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
