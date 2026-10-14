package com.tcs.account.service;

import com.tcs.account.domain.Cuenta;
import com.tcs.account.domain.TipoCuenta;
import com.tcs.account.domain.TipoMovimiento;
import com.tcs.account.dto.MovimientoRequest;
import com.tcs.account.dto.MovimientoResponse;
import com.tcs.account.exception.BusinessException;
import com.tcs.account.exception.SaldoNoDisponibleException;
import com.tcs.account.repository.CuentaRepository;
import com.tcs.account.repository.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private CuentaRepository cuentas;

    @Mock
    private MovimientoRepository movimientos;

    @InjectMocks
    private MovimientoService service;

    private Cuenta cuentaCon(String saldo) {
        return new Cuenta("225487", TipoCuenta.CORRIENTE, new BigDecimal(saldo), true, "mmontalvo");
    }

    @Test
    void depositoActualizaSaldoYQuedaRegistrado() {
        when(cuentas.findByNumeroCuentaForUpdate("225487")).thenReturn(Optional.of(cuentaCon("100")));
        when(movimientos.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MovimientoResponse r = service.registrar(new MovimientoRequest("225487", new BigDecimal("600"), null, null));

        assertThat(r.tipoMovimiento()).isEqualTo(TipoMovimiento.DEPOSITO);
        assertThat(r.valor()).isEqualByComparingTo("600");
        assertThat(r.saldo()).isEqualByComparingTo("700");
        verify(movimientos).save(any());
    }

    @Test
    void retiroSinSaldoLanzaErrorYNoGuardaMovimiento() {
        when(cuentas.findByNumeroCuentaForUpdate("225487")).thenReturn(Optional.of(cuentaCon("100")));

        assertThatThrownBy(() -> service.registrar(new MovimientoRequest("225487", new BigDecimal("-500"), null, null)))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
        verify(movimientos, never()).save(any());
    }

    @Test
    void tipoRetiroConValorPositivoSeInterpretaComoDebito() {
        when(cuentas.findByNumeroCuentaForUpdate("225487")).thenReturn(Optional.of(cuentaCon("2000")));
        when(movimientos.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MovimientoResponse r = service.registrar(
                new MovimientoRequest("225487", new BigDecimal("575"), TipoMovimiento.RETIRO, null));

        assertThat(r.valor()).isEqualByComparingTo("-575");
        assertThat(r.saldo()).isEqualByComparingTo("1425");
        assertThat(r.tipoMovimiento()).isEqualTo(TipoMovimiento.RETIRO);
    }

    @Test
    void valorCeroEsRechazado() {
        assertThatThrownBy(() -> service.registrar(new MovimientoRequest("225487", BigDecimal.ZERO, null, null)))
                .isInstanceOf(BusinessException.class);
    }
}
