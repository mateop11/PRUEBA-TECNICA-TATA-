package com.tcs.account.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tcs.account.util.Textos;

public enum TipoMovimiento {
    DEPOSITO("Deposito"),
    RETIRO("Retiro");

    private final String etiqueta;

    TipoMovimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @JsonValue
    public String getEtiqueta() {
        return etiqueta;
    }

    @JsonCreator
    public static TipoMovimiento desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return valueOf(Textos.normalizar(valor));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + valor + " (use Deposito o Retiro)");
        }
    }
}
