package com.tcs.account.dto;

import com.tcs.account.domain.TipoCuenta;
import com.tcs.account.domain.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReporteEstadoCuenta(
        String clienteId,
        String cliente,
        LocalDate desde,
        LocalDate hasta,
        List<CuentaReporte> cuentas) {

    public record CuentaReporte(
            String numeroCuenta,
            TipoCuenta tipoCuenta,
            Boolean estado,
            BigDecimal saldoInicial,
            BigDecimal saldoDisponible,
            BigDecimal totalCreditos,
            BigDecimal totalDebitos,
            List<MovimientoReporte> movimientos) {
    }

    public record MovimientoReporte(
            LocalDateTime fecha,
            TipoMovimiento tipoMovimiento,
            BigDecimal valor,
            BigDecimal saldo) {
    }
}
