package com.tcs.customer;

import com.tcs.customer.messaging.ClienteEvento;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba de integración: controlador + servicio + JPA (H2) + publicación del evento. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClienteIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void creaClienteLoConsultaYPublicaEvento() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"Marianela Montalvo","genero":"Femenino","edad":30,"identificacion":"1710000002",
                         "direccion":"Amazonas y NNUU","telefono":"097548965",
                         "clienteId":"mmontalvo","contrasena":"5678","estado":true}"""))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/clientes/mmontalvo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.estado").value(true));

        verify(rabbitTemplate).convertAndSend(eq("cliente.exchange"), eq("cliente.evento"), any(ClienteEvento.class));
    }

    @Test
    void rechazaClienteDuplicado() throws Exception {
        String body = """
                {"nombre":"Juan Osorio","genero":"Masculino","edad":28,"identificacion":"1710000003",
                 "clienteId":"josorio","contrasena":"1245"}""";
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}
