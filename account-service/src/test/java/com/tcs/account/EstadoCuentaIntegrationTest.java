package com.tcs.account;

import com.tcs.account.domain.ClienteReplica;
import com.tcs.account.repository.ClienteReplicaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** F6: prueba de integración del flujo cuenta -> movimientos -> reporte con JPA real (H2). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstadoCuentaIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClienteReplicaRepository replicas;

    @Test
    void flujoCompletoDeCuentaMovimientosYReporte() throws Exception {
        replicas.save(new ClienteReplica("mmontalvo", "Marianela Montalvo", true));

        mvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"numeroCuenta":"225487","tipoCuenta":"Corriente","saldoInicial":100,
                         "estado":true,"clienteId":"mmontalvo"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente").value("Marianela Montalvo"));

        mvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"numeroCuenta":"225487","valor":600,"fecha":"2022-02-10T10:00:00"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(700.0));

        mvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"numeroCuenta":"225487","valor":-10000,"fecha":"2022-02-11T10:00:00"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));

        mvc.perform(get("/api/reportes").param("fecha", "2022-02-01,2022-02-28").param("clienteId", "mmontalvo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("225487"))
                .andExpect(jsonPath("$.cuentas[0].saldoDisponible").value(700.0))
                .andExpect(jsonPath("$.cuentas[0].movimientos.length()").value(1))
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].valor").value(600.0));
    }

    @Test
    void noPermiteCuentaParaClienteInexistente() throws Exception {
        mvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"numeroCuenta":"999999","tipoCuenta":"Ahorros","saldoInicial":0,"clienteId":"fantasma"}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado: fantasma"));
    }
}
