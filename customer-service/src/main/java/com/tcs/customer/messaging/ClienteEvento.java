package com.tcs.customer.messaging;

/** Evento de dominio publicado cuando un cliente se crea, actualiza o elimina. */
public record ClienteEvento(String tipo, String clienteId, String nombre, boolean estado) {

    public static final String CREADO = "CREADO";
    public static final String ACTUALIZADO = "ACTUALIZADO";
    public static final String ELIMINADO = "ELIMINADO";
}
