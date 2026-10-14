package com.tcs.customer.dto;

import jakarta.validation.constraints.*;

public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El género es obligatorio") String genero,
        @NotNull(message = "La edad es obligatoria") @Min(value = 0, message = "La edad mínima es 0") @Max(value = 120, message = "La edad máxima es 120") Integer edad,
        @NotBlank(message = "La identificación es obligatoria") @Size(max = 20, message = "La identificación admite máximo 20 caracteres") String identificacion,
        String direccion,
        String telefono,
        @NotBlank(message = "El clienteId es obligatorio") @Size(max = 50, message = "El clienteId admite máximo 50 caracteres") String clienteId,
        @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres") String contrasena,
        Boolean estado) {
}
