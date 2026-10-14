package com.tcs.customer.service;

import com.tcs.customer.domain.Cliente;
import com.tcs.customer.dto.ClienteRequest;
import com.tcs.customer.dto.ClienteResponse;
import com.tcs.customer.exception.BusinessException;
import com.tcs.customer.exception.DuplicateResourceException;
import com.tcs.customer.exception.ResourceNotFoundException;
import com.tcs.customer.messaging.ClienteEvento;
import com.tcs.customer.repository.ClienteRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventos;

    public ClienteService(ClienteRepository repository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventos) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.eventos = eventos;
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (request.contrasena() == null || request.contrasena().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria");
        }
        if (repository.existsByClienteId(request.clienteId())) {
            throw new DuplicateResourceException("Ya existe un cliente con clienteId " + request.clienteId());
        }
        if (repository.existsByIdentificacion(request.identificacion())) {
            throw new DuplicateResourceException("Ya existe una persona con identificación " + request.identificacion());
        }
        Cliente cliente = new Cliente();
        cliente.setClienteId(request.clienteId());
        copiarDatosPersona(cliente, request);
        cliente.cambiarContrasena(passwordEncoder.encode(request.contrasena()));
        cliente.setEstado(request.estado() == null || request.estado());

        Cliente guardado = repository.save(cliente);
        publicar(ClienteEvento.CREADO, guardado);
        return ClienteResponse.from(guardado);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repository.findAll().stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(String clienteId) {
        return ClienteResponse.from(buscar(clienteId));
    }

    @Transactional
    public ClienteResponse actualizar(String clienteId, ClienteRequest request) {
        Cliente cliente = buscar(clienteId);
        if (!clienteId.equals(request.clienteId())) {
            throw new BusinessException("El clienteId no se puede modificar");
        }
        if (repository.existsByIdentificacionAndClienteIdNot(request.identificacion(), clienteId)) {
            throw new DuplicateResourceException("Ya existe una persona con identificación " + request.identificacion());
        }
        copiarDatosPersona(cliente, request);
        if (request.contrasena() != null && !request.contrasena().isBlank()) {
            cliente.cambiarContrasena(passwordEncoder.encode(request.contrasena()));
        }
        if (request.estado() != null) {
            cliente.setEstado(request.estado());
        }
        Cliente guardado = repository.save(cliente);
        publicar(ClienteEvento.ACTUALIZADO, guardado);
        return ClienteResponse.from(guardado);
    }

    @Transactional
    public ClienteResponse cambiarEstado(String clienteId, boolean estado) {
        Cliente cliente = buscar(clienteId);
        if (estado) {
            cliente.activar();
        } else {
            cliente.desactivar();
        }
        Cliente guardado = repository.save(cliente);
        publicar(ClienteEvento.ACTUALIZADO, guardado);
        return ClienteResponse.from(guardado);
    }

    @Transactional
    public void eliminar(String clienteId) {
        Cliente cliente = buscar(clienteId);
        repository.delete(cliente);
        publicar(ClienteEvento.ELIMINADO, cliente);
    }

    private Cliente buscar(String clienteId) {
        return repository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));
    }

    private void copiarDatosPersona(Cliente cliente, ClienteRequest r) {
        cliente.setNombre(r.nombre());
        cliente.setGenero(r.genero());
        cliente.setEdad(r.edad());
        cliente.setIdentificacion(r.identificacion());
        cliente.setDireccion(r.direccion());
        cliente.setTelefono(r.telefono());
    }

    private void publicar(String tipo, Cliente c) {
        eventos.publishEvent(new ClienteEvento(tipo, c.getClienteId(), c.getNombre(), c.estaActivo()));
    }
}
