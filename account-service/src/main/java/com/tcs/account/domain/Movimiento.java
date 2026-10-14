package com.tcs.account.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    /** Saldo disponible de la cuenta justo después de este movimiento. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    protected Movimiento() {
    }

    public Movimiento(Cuenta cuenta, LocalDateTime fecha, BigDecimal valor, BigDecimal saldo) {
        this.cuenta = cuenta;
        this.fecha = fecha;
        registrar(valor, saldo);
    }

    /** Fija valor, saldo resultante y tipo (derivado del signo del valor). */
    public void registrar(BigDecimal valor, BigDecimal saldo) {
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        this.saldo = saldo.setScale(2, RoundingMode.HALF_UP);
        this.tipoMovimiento = valor.signum() >= 0 ? TipoMovimiento.DEPOSITO : TipoMovimiento.RETIRO;
    }

    public Long getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getSaldo() { return saldo; }
    public Cuenta getCuenta() { return cuenta; }
}
