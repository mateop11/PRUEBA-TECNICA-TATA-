package com.tcs.account.repository;

import com.tcs.account.domain.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuentaIdAndFechaBetweenOrderByFechaAscIdAsc(Long cuentaId, LocalDateTime desde, LocalDateTime hasta);

    List<Movimiento> findByCuentaNumeroCuentaOrderByFechaDescIdDesc(String numeroCuenta);

    Optional<Movimiento> findFirstByCuentaIdOrderByIdDesc(Long cuentaId);

    boolean existsByCuentaId(Long cuentaId);
}
