package br.com.gestao.gestaovendas.common.text;

public final class Textos {

    private Textos() {
    }

    /** "  abc " -> "abc"; "" ou só espaços -> null. */
    public static String limpar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
