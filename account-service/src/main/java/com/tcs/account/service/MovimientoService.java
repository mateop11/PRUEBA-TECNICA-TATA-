package com.tcs.account.service;

import com.tcs.account.domain.Cuenta;
import com.tcs.account.domain.Movimiento;
import com.tcs.account.domain.TipoMovimiento;
import com.tcs.account.dto.MovimientoRequest;
import com.tcs.account.dto.MovimientoResponse;
import com.tcs.account.exception.BusinessException;
import com.tcs.account.exception.ResourceNotFoundException;
import com.tcs.account.repository.CuentaRepository;
import com.tcs.account.repository.MovimientoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoService {

    private final CuentaRepository cuentas;
    private final MovimientoRepository movimientos;

    public MovimientoService(CuentaRepository cuentas, MovimientoRepository movimientos) {
        this.cuentas = cuentas;
        this.movimientos = movimientos;
    }

    /** F2 y F3: registra el movimiento, actualiza el saldo y rechaza retiros sin fondos. */
    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request) {
        BigDecimal valor = valorConSigno(request);
        Cuenta cuenta = cuentaBloqueada(request.numeroCuenta());
        validarCuentaActiva(cuenta);

        cuenta.aplicarMovimiento(valor); // lanza SaldoNoDisponibleException si no alcanza
        LocalDateTime fecha = request.fecha() != null ? request.fecha() : LocalDateTime.now();
        Movimiento movimiento = new Movimiento(cuenta, fecha, valor, cuenta.getSaldoActual());
        return MovimientoResponse.from(movimientos.save(movimiento));
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listar(String numeroCuenta) {
        List<Movimiento> lista = (numeroCuenta == null || numeroCuenta.isBlank())
                ? movimientos.findAll(Sort.by(Sort.Direction.DESC, "fecha", "id"))
                : movimientos.findByCuentaNumeroCuentaOrderByFechaDescIdDesc(numeroCuenta);
        return lista.stream().map(MovimientoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MovimientoResponse obtener(Long id) {
        return MovimientoResponse.from(buscar(id));
    }

    /**
     * Para no romper la cadena de saldos, solo se puede corregir el último movimiento de una cuenta:
     * se revierte su efecto y se aplica el nuevo valor.
     */
    @Transactional
    public MovimientoResponse actualizar(Long id, MovimientoRequest request) {
        Movimiento movimiento = buscar(id);
        if (!movimiento.getCuenta().getNumeroCuenta().equals(request.numeroCuenta())) {
            throw new BusinessException("No se puede cambiar la cuenta de un movimiento");
        }
        Cuenta cuenta = cuentaBloqueada(movimiento.getCuenta().getNumeroCuenta());
        exigirUltimoMovimiento(cuenta, movimiento, "modificar");
        validarCuentaActiva(cuenta);

        BigDecimal nuevoValor = valorConSigno(request);
        cuenta.revertirMovimiento(movimiento.getValor());
        cuenta.aplicarMovimiento(nuevoValor);
        movimiento.registrar(nuevoValor, cuenta.getSaldoActual());
        return MovimientoResponse.from(movimientos.save(movimiento));
    }

    @Transactional
    public void eliminar(Long id) {
        Movimiento movimiento = buscar(id);
        Cuenta cuenta = cuentaBloqueada(movimiento.getCuenta().getNumeroCuenta());
        exigirUltimoMovimiento(cuenta, movimiento, "eliminar");
        cuenta.revertirMovimiento(movimiento.getValor());
        movimientos.delete(movimiento);
    }

    private BigDecimal valorConSigno(MovimientoRequest request) {
        BigDecimal valor = request.valor();
        if (valor.signum() == 0) {
            throw new BusinessException("El valor del movimiento no puede ser cero");
        }
        TipoMovimiento tipo = request.tipoMovimiento();
        if (tipo == TipoMovimiento.RETIRO && valor.signum() > 0) {
            return valor.negate();
        }
        if (tipo == TipoMovimiento.DEPOSITO && valor.signum() < 0) {
            throw new BusinessException("Un depósito debe tener valor positivo");
        }
        return valor;
    }

    private Cuenta cuentaBloqueada(String numeroCuenta) {
        return cuentas.findByNumeroCuentaForUpdate(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));
    }

    private void validarCuentaActiva(Cuenta cuenta) {
        if (!cuenta.estaActiva()) {
            throw new BusinessException("La cuenta " + cuenta.getNumeroCuenta() + " está inactiva");
        }
    }

    private void exigirUltimoMovimiento(Cuenta cuenta, Movimiento movimiento, String accion) {
        Movimiento ultimo = movimientos.findFirstByCuentaIdOrderByIdDesc(cuenta.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no tiene movimientos"));
        if (!ultimo.getId().equals(movimiento.getId())) {
            throw new BusinessException("Solo se puede " + accion + " el último movimiento de la cuenta");
        }
    }

    private Movimiento buscar(Long id) {
        return movimientos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado: " + id));
    }
}
