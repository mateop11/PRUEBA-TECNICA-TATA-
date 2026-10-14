package com.tcs.account.repository;

import com.tcs.account.domain.Cuenta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteId(String clienteId);

    /** Bloqueo pesimista: serializa movimientos concurrentes sobre la misma cuenta. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.numeroCuenta = :numero")
    Optional<Cuenta> findByNumeroCuentaForUpdate(@Param("numero") String numero);
}
