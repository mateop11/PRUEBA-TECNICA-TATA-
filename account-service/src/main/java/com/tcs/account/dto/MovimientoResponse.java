package com.tcs.account.dto;

import com.tcs.account.domain.Movimiento;
import com.tcs.account.domain.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        LocalDateTime fecha,
        String numeroCuenta,
        TipoMovimiento tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo) {

    public static MovimientoResponse from(Movimiento m) {
        return new MovimientoResponse(m.getId(), m.getFecha(), m.getCuenta().getNumeroCuenta(),
                m.getTipoMovimiento(), m.getValor(), m.getSaldo());
    }
}
