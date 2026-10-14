package com.tcs.account.util;

import java.text.Normalizer;
import java.util.Locale;

public final class Textos {

    private Textos() {
    }

    /** Quita tildes, espacios sobrantes y pasa a mayúsculas: "Depósito " -> "DEPOSITO". */
    public static String normalizar(String valor) {
        String sinTildes = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toUpperCase(Locale.ROOT);
    }
}
