package com.tcs.account.controller;

import com.tcs.account.domain.TipoMovimiento;
import com.tcs.account.dto.MovimientoResponse;
import com.tcs.account.exception.SaldoNoDisponibleException;
import com.tcs.account.service.MovimientoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovimientoController.class)
class MovimientoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MovimientoService service;

    @Test
    void registrarDepositoDevuelve201() throws Exception {
        when(service.registrar(any())).thenReturn(new MovimientoResponse(1L, LocalDateTime.of(2022, 2, 10, 10, 0),
                "225487", TipoMovimiento.DEPOSITO, new BigDecimal("600.00"), new BigDecimal("700.00")));

        mvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"225487\",\"valor\":600}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(700.0))
                .andExpect(jsonPath("$.tipoMovimiento").value("Deposito"));
    }

    @Test
    void retiroSinSaldoDevuelve400ConMensaje() throws Exception {
        when(service.registrar(any())).thenThrow(new SaldoNoDisponibleException());

        mvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"225487\",\"valor\":-9999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));
    }

    @Test
    void faltaNumeroDeCuentaDevuelve400() throws Exception {
        mvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Datos de entrada inválidos"));
    }
}
