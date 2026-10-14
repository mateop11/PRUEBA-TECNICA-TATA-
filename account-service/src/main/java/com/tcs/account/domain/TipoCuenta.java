package com.tcs.account.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tcs.account.util.Textos;

public enum TipoCuenta {
    AHORRO("Ahorro"),
    CORRIENTE("Corriente");

    private final String etiqueta;

    TipoCuenta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @JsonValue
    public String getEtiqueta() {
        return etiqueta;
    }

    /** Acepta "Ahorro", "Ahorros", "corriente", etc. (el enunciado usa ambas formas). */
    @JsonCreator
    public static TipoCuenta desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String n = Textos.normalizar(valor);
        if (n.endsWith("S")) {
            n = n.substring(0, n.length() - 1);
        }
        try {
            return valueOf(n);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de cuenta inválido: " + valor + " (use Ahorro o Corriente)");
        }
    }
}
