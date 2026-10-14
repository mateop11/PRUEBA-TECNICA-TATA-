package com.tcs.account.dto;

import com.tcs.account.domain.TipoCuenta;
import jakarta.validation.constraints.NotNull;

public record CuentaUpdateRequest(
        @NotNull(message = "El tipo de cuenta es obligatorio") TipoCuenta tipoCuenta,
        @NotNull(message = "El estado es obligatorio") Boolean estado) {
}
