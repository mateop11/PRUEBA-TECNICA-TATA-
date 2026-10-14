package com.tcs.customer.repository;

import com.tcs.customer.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByClienteId(String clienteId);

    boolean existsByClienteId(String clienteId);

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndClienteIdNot(String identificacion, String clienteId);
}
