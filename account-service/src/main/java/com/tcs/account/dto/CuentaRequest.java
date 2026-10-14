package com.tcs.account.dto;

import com.tcs.account.domain.TipoCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CuentaRequest(
        @NotBlank(message = "El número de cuenta es obligatorio") @Size(max = 20, message = "El número de cuenta admite máximo 20 caracteres") String numeroCuenta,
        @NotNull(message = "El tipo de cuenta es obligatorio") TipoCuenta tipoCuenta,
        @NotNull(message = "El saldo inicial es obligatorio") @PositiveOrZero(message = "El saldo inicial no puede ser negativo") BigDecimal saldoInicial,
        Boolean estado,
        @NotBlank(message = "El clienteId es obligatorio") String clienteId) {
}
