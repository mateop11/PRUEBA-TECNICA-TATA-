package com.tcs.customer.controller;

import com.tcs.customer.dto.ClienteRequest;
import com.tcs.customer.dto.ClienteResponse;
import com.tcs.customer.dto.EstadoRequest;
import com.tcs.customer.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{clienteId}")
    public ClienteResponse obtener(@PathVariable String clienteId) {
        return service.obtener(clienteId);
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{clienteId}")
    public ClienteResponse actualizar(@PathVariable String clienteId, @Valid @RequestBody ClienteRequest request) {
        return service.actualizar(clienteId, request);
    }

    @PatchMapping("/{clienteId}/estado")
    public ClienteResponse cambiarEstado(@PathVariable String clienteId, @Valid @RequestBody EstadoRequest request) {
        return service.cambiarEstado(clienteId, request.estado());
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> eliminar(@PathVariable String clienteId) {
        service.eliminar(clienteId);
        return ResponseEntity.noContent().build();
    }
}
