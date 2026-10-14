package com.tcs.customer.dto;

import com.tcs.customer.domain.Cliente;

public record ClienteResponse(
        Long id,
        String clienteId,
        String nombre,
        String genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono,
        Boolean estado) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getClienteId(), c.getNombre(), c.getGenero(), c.getEdad(),
                c.getIdentificacion(), c.getDireccion(), c.getTelefono(), c.getEstado());
    }
}
