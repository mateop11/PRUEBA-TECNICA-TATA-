package com.tcs.account.controller;

import com.tcs.account.dto.CuentaRequest;
import com.tcs.account.dto.CuentaResponse;
import com.tcs.account.dto.CuentaUpdateRequest;
import com.tcs.account.dto.EstadoRequest;
import com.tcs.account.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService service;

    public CuentaController(CuentaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CuentaResponse> listar(@RequestParam(required = false) String clienteId) {
        return service.listar(clienteId);
    }

    @GetMapping("/{numeroCuenta}")
    public CuentaResponse obtener(@PathVariable String numeroCuenta) {
        return service.obtener(numeroCuenta);
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{numeroCuenta}")
    public CuentaResponse actualizar(@PathVariable String numeroCuenta, @Valid @RequestBody CuentaUpdateRequest request) {
        return service.actualizar(numeroCuenta, request);
    }

    @PatchMapping("/{numeroCuenta}/estado")
    public CuentaResponse cambiarEstado(@PathVariable String numeroCuenta, @Valid @RequestBody EstadoRequest request) {
        return service.cambiarEstado(numeroCuenta, request.estado());
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroCuenta) {
        service.eliminar(numeroCuenta);
        return ResponseEntity.noContent().build();
    }
}
