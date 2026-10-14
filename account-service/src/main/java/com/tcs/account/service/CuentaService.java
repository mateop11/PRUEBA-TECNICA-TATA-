package com.tcs.account.service;

import com.tcs.account.domain.ClienteReplica;
import com.tcs.account.domain.Cuenta;
import com.tcs.account.dto.CuentaRequest;
import com.tcs.account.dto.CuentaResponse;
import com.tcs.account.dto.CuentaUpdateRequest;
import com.tcs.account.exception.BusinessException;
import com.tcs.account.exception.DuplicateResourceException;
import com.tcs.account.exception.ResourceNotFoundException;
import com.tcs.account.repository.ClienteReplicaRepository;
import com.tcs.account.repository.CuentaRepository;
import com.tcs.account.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentas;
    private final MovimientoRepository movimientos;
    private final ClienteReplicaRepository clientes;

    public CuentaService(CuentaRepository cuentas, MovimientoRepository movimientos, ClienteReplicaRepository clientes) {
        this.cuentas = cuentas;
        this.movimientos = movimientos;
        this.clientes = clientes;
    }

    @Transactional
    public CuentaResponse crear(CuentaRequest request) {
        ClienteReplica cliente = clientes.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.clienteId()));
        if (!cliente.estaActivo()) {
            throw new BusinessException("El cliente " + request.clienteId() + " está inactivo");
        }
        if (cuentas.existsByNumeroCuenta(request.numeroCuenta())) {
            throw new DuplicateResourceException("Ya existe la cuenta " + request.numeroCuenta());
        }
        Cuenta cuenta = new Cuenta(request.numeroCuenta(), request.tipoCuenta(), request.saldoInicial(),
                request.estado(), request.clienteId());
        return CuentaResponse.from(cuentas.save(cuenta), cliente.getNombre());
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> listar(String clienteId) {
        List<Cuenta> lista = (clienteId == null || clienteId.isBlank()) ? cuentas.findAll() : cuentas.findByClienteId(clienteId);
        return lista.stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public CuentaResponse obtener(String numeroCuenta) {
        return aRespuesta(buscar(numeroCuenta));
    }

    /** Solo se pueden cambiar tipo y estado; el saldo se mueve únicamente con movimientos. */
    @Transactional
    public CuentaResponse actualizar(String numeroCuenta, CuentaUpdateRequest request) {
        Cuenta cuenta = buscar(numeroCuenta);
        cuenta.setTipoCuenta(request.tipoCuenta());
        cuenta.setEstado(request.estado());
        return aRespuesta(cuentas.save(cuenta));
    }

    @Transactional
    public CuentaResponse cambiarEstado(String numeroCuenta, boolean estado) {
        Cuenta cuenta = buscar(numeroCuenta);
        cuenta.setEstado(estado);
        return aRespuesta(cuentas.save(cuenta));
    }

    @Transactional
    public void eliminar(String numeroCuenta) {
        Cuenta cuenta = buscar(numeroCuenta);
        if (movimientos.existsByCuentaId(cuenta.getId())) {
            throw new BusinessException("No se puede eliminar una cuenta con movimientos; desactívela en su lugar");
        }
        cuentas.delete(cuenta);
    }

    private Cuenta buscar(String numeroCuenta) {
        return cuentas.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));
    }

    private CuentaResponse aRespuesta(Cuenta c) {
        String nombre = clientes.findById(c.getClienteId()).map(ClienteReplica::getNombre).orElse(null);
        return CuentaResponse.from(c, nombre);
    }
}
