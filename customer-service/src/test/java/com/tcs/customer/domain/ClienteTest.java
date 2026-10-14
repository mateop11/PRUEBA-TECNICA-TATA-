package com.tcs.customer.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** F5: prueba unitaria de la entidad de dominio Cliente. */
class ClienteTest {

    private Cliente nuevoCliente() {
        Cliente c = new Cliente();
        c.setClienteId("jlema");
        c.setNombre("Jose Lema");
        c.setEdad(35);
        return c;
    }

    @Test
    void nuevoClienteEstaActivoPorDefecto() {
        assertThat(nuevoCliente().estaActivo()).isTrue();
    }

    @Test
    void desactivarYActivarCambianElEstado() {
        Cliente c = nuevoCliente();
        c.desactivar();
        assertThat(c.estaActivo()).isFalse();
        c.activar();
        assertThat(c.estaActivo()).isTrue();
    }

    @Test
    void noPermiteContrasenaVacia() {
        Cliente c = nuevoCliente();
        assertThatThrownBy(() -> c.cambiarContrasena("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La contraseña no puede estar vacía");
    }

    @Test
    void noPermiteEdadFueraDeRango() {
        Cliente c = nuevoCliente();
        assertThatThrownBy(() -> c.setEdad(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> c.setEdad(121)).isInstanceOf(IllegalArgumentException.class);
    }
}
