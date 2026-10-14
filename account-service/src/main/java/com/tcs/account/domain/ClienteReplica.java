package com.tcs.account.domain;

import jakarta.persistence.*;

/**
 * Copia local y mínima del cliente, alimentada por eventos del microservicio de clientes.
 * Permite validar clientes y armar reportes sin llamadas síncronas entre servicios.
 */
@Entity
@Table(name = "cliente_replica")
public class ClienteReplica {

    @Id
    @Column(name = "cliente_id", length = 50)
    private String clienteId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private Boolean estado = true;

    protected ClienteReplica() {
    }

    public ClienteReplica(String clienteId, String nombre, boolean estado) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.estado = estado;
    }

    public String getClienteId() { return clienteId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
    public boolean estaActivo() { return Boolean.TRUE.equals(estado); }
}
