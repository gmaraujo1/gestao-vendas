package br.com.gestao.gestaovendas.common.text;

/** Escapa % e _ para que a busca trate esses caracteres como texto (usar com escape '\'). */
public final class Like {

    private Like() {
    }

    public static String escapar(String termo) {
        return termo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
