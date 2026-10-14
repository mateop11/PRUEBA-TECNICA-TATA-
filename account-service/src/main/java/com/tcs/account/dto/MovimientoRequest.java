package com.tcs.account.dto;

import com.tcs.account.domain.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * valor: positivo = depósito, negativo = retiro.
 * tipoMovimiento (opcional): si se envía "Retiro" con valor positivo, se interpreta como débito.
 * fecha (opcional): por defecto es el momento actual; permite cargar históricos de prueba.
 */
public record MovimientoRequest(
        @NotBlank(message = "El número de cuenta es obligatorio") String numeroCuenta,
        @NotNull(message = "El valor es obligatorio") BigDecimal valor,
        TipoMovimiento tipoMovimiento,
        LocalDateTime fecha) {
}
