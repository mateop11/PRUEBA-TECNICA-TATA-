package com.tcs.customer.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull(message = "El estado es obligatorio") Boolean estado) {
}
