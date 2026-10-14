package com.tcs.customer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "persona_id")
public class Cliente extends Persona {

    @Column(name = "cliente_id", nullable = false, unique = true, length = 50)
    private String clienteId;

    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(nullable = false)
    private Boolean estado = true;

    public Cliente() {
    }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getContrasena() { return contrasena; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public void activar() { this.estado = true; }

    public void desactivar() { this.estado = false; }

    public boolean estaActivo() { return Boolean.TRUE.equals(estado); }

    /** Recibe la contraseña ya codificada (hash). */
    public void cambiarContrasena(String contrasenaCodificada) {
        if (contrasenaCodificada == null || contrasenaCodificada.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        this.contrasena = contrasenaCodificada;
    }
}
