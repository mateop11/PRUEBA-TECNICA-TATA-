package com.tcs.account.dto;

import com.tcs.account.domain.Cuenta;
import com.tcs.account.domain.TipoCuenta;

import java.math.BigDecimal;

public record CuentaResponse(
        Long id,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldoDisponible,
        Boolean estado,
        String clienteId,
        String cliente) {

    public static CuentaResponse from(Cuenta c, String nombreCliente) {
        return new CuentaResponse(c.getId(), c.getNumeroCuenta(), c.getTipoCuenta(), c.getSaldoInicial(),
                c.getSaldoActual(), c.getEstado(), c.getClienteId(), nombreCliente);
    }
}
