package com.tcs.account.domain;

import com.tcs.account.exception.SaldoNoDisponibleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaTest {

    private Cuenta cuenta(String saldo) {
        return new Cuenta("225487", TipoCuenta.CORRIENTE, new BigDecimal(saldo), true, "mmontalvo");
    }

    @Test
    void depositoAumentaElSaldo() {
        Cuenta c = cuenta("100");
        c.aplicarMovimiento(new BigDecimal("600"));
        assertThat(c.getSaldoActual()).isEqualByComparingTo("700");
    }

    @Test
    void retiroDisminuyeElSaldo() {
        Cuenta c = cuenta("2000");
        c.aplicarMovimiento(new BigDecimal("-575"));
        assertThat(c.getSaldoActual()).isEqualByComparingTo("1425");
    }

    @Test
    void retiroMayorAlSaldoLanzaSaldoNoDisponible() {
        Cuenta c = cuenta("100");
        assertThatThrownBy(() -> c.aplicarMovimiento(new BigDecimal("-100.01")))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
        assertThat(c.getSaldoActual()).isEqualByComparingTo("100");
    }

    @Test
    void retiroDelSaldoExactoEsValido() {
        Cuenta c = cuenta("540");
        c.aplicarMovimiento(new BigDecimal("-540"));
        assertThat(c.getSaldoActual()).isEqualByComparingTo("0");
    }

    @Test
    void tipoDeCuentaAceptaSingularYPlural() {
        assertThat(TipoCuenta.desde("Ahorro")).isEqualTo(TipoCuenta.AHORRO);
        assertThat(TipoCuenta.desde("Ahorros")).isEqualTo(TipoCuenta.AHORRO);
        assertThat(TipoCuenta.desde("corriente")).isEqualTo(TipoCuenta.CORRIENTE);
    }
}
