package com.tcs.account.service;

import com.tcs.account.domain.ClienteReplica;
import com.tcs.account.domain.Cuenta;
import com.tcs.account.domain.Movimiento;
import com.tcs.account.dto.ReporteEstadoCuenta;
import com.tcs.account.dto.ReporteEstadoCuenta.CuentaReporte;
import com.tcs.account.dto.ReporteEstadoCuenta.MovimientoReporte;
import com.tcs.account.exception.BusinessException;
import com.tcs.account.exception.ResourceNotFoundException;
import com.tcs.account.repository.ClienteReplicaRepository;
import com.tcs.account.repository.CuentaRepository;
import com.tcs.account.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** F4: reporte "Estado de cuenta" por cliente y rango de fechas. */
@Service
public class ReporteService {

    private final ClienteReplicaRepository clientes;
    private final CuentaRepository cuentas;
    private final MovimientoRepository movimientos;

    public ReporteService(ClienteReplicaRepository clientes, CuentaRepository cuentas, MovimientoRepository movimientos) {
        this.clientes = clientes;
        this.cuentas = cuentas;
        this.movimientos = movimientos;
    }

    /** @param rango formato "yyyy-MM-dd,yyyy-MM-dd" (inclusive en ambos extremos); con una sola fecha se toma ese día */
    @Transactional(readOnly = true)
    public ReporteEstadoCuenta estadoDeCuenta(String clienteId, String rango) {
        LocalDate[] fechas = parsearRango(rango);
        ClienteReplica cliente = clientes.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));

        List<CuentaReporte> detalle = cuentas.findByClienteId(clienteId).stream()
                .map(c -> armarCuenta(c, fechas[0], fechas[1]))
                .toList();
        return new ReporteEstadoCuenta(clienteId, cliente.getNombre(), fechas[0], fechas[1], detalle);
    }

    private CuentaReporte armarCuenta(Cuenta cuenta, LocalDate desde, LocalDate hasta) {
        List<Movimiento> lista = movimientos.findByCuentaIdAndFechaBetweenOrderByFechaAscIdAsc(
                cuenta.getId(), desde.atStartOfDay(), hasta.atTime(LocalTime.MAX));

        BigDecimal creditos = lista.stream().map(Movimiento::getValor)
                .filter(v -> v.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debitos = lista.stream().map(Movimiento::getValor)
                .filter(v -> v.signum() < 0).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<MovimientoReporte> detalle = lista.stream()
                .map(m -> new MovimientoReporte(m.getFecha(), m.getTipoMovimiento(), m.getValor(), m.getSaldo()))
                .toList();
        return new CuentaReporte(cuenta.getNumeroCuenta(), cuenta.getTipoCuenta(), cuenta.getEstado(),
                cuenta.getSaldoInicial(), cuenta.getSaldoActual(), creditos, debitos, detalle);
    }

    private LocalDate[] parsearRango(String rango) {
        String[] partes = rango.split(",");
        if (partes.length < 1 || partes.length > 2) {
            throw new BusinessException("Formato de fecha inválido. Use fecha=yyyy-MM-dd,yyyy-MM-dd");
        }
        try {
            LocalDate desde = LocalDate.parse(partes[0].trim());
            LocalDate hasta = partes.length == 2 ? LocalDate.parse(partes[1].trim()) : desde;
            if (hasta.isBefore(desde)) {
                throw new BusinessException("La fecha final no puede ser anterior a la inicial");
            }
            return new LocalDate[]{desde, hasta};
        } catch (DateTimeParseException ex) {
            throw new BusinessException("Formato de fecha inválido. Use fecha=yyyy-MM-dd,yyyy-MM-dd");
        }
    }
}
