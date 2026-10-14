package com.tcs.account.messaging;

/** Mismo contrato JSON que publica customer-service. */
public record ClienteEvento(String tipo, String clienteId, String nombre, boolean estado) {

    public static final String ELIMINADO = "ELIMINADO";
}
