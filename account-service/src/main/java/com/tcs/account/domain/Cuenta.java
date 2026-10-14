package com.tcs.account.domain;

import com.tcs.account.exception.SaldoNoDisponibleException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "cuenta")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private TipoCuenta tipoCuenta;

    @Column(name = "saldo_inicial", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_actual", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoActual;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "cliente_id", nullable = false, length = 50)
    private String clienteId;

    protected Cuenta() {
    }

    public Cuenta(String numeroCuenta, TipoCuenta tipoCuenta, BigDecimal saldoInicial, Boolean estado, String clienteId) {
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldoInicial = escala(saldoInicial);
        this.saldoActual = escala(saldoInicial);
        this.estado = estado == null || estado;
        this.clienteId = clienteId;
    }

    /** Regla de negocio central: el saldo nunca puede quedar negativo. */
    public void aplicarMovimiento(BigDecimal valor) {
        BigDecimal nuevoSaldo = saldoActual.add(valor);
        if (nuevoSaldo.signum() < 0) {
            throw new SaldoNoDisponibleException();
        }
        this.saldoActual = escala(nuevoSaldo);
    }

    /** Deshace el efecto de un movimiento (solo se usa con el último movimiento de la cuenta). */
    public void revertirMovimiento(BigDecimal valor) {
        this.saldoActual = escala(saldoActual.subtract(valor));
    }

    public boolean estaActiva() { return Boolean.TRUE.equals(estado); }

    private static BigDecimal escala(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(TipoCuenta tipoCuenta) { this.tipoCuenta = tipoCuenta; }
    public BigDecimal getSaldoInicial() { return saldoInicial; }
    public BigDecimal getSaldoActual() { return saldoActual; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    public String getClienteId() { return clienteId; }
}
