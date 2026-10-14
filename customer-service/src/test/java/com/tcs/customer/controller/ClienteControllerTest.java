package com.tcs.customer.controller;

import com.tcs.customer.dto.ClienteResponse;
import com.tcs.customer.exception.ResourceNotFoundException;
import com.tcs.customer.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ClienteService service;

    @Test
    void crearClienteDevuelve201() throws Exception {
        when(service.crear(any())).thenReturn(new ClienteResponse(1L, "jlema", "Jose Lema", "Masculino", 35,
                "1710000001", "Otavalo sn y principal", "098254785", true));

        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"Jose Lema","genero":"Masculino","edad":35,"identificacion":"1710000001",
                         "direccion":"Otavalo sn y principal","telefono":"098254785",
                         "clienteId":"jlema","contrasena":"1234","estado":true}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value("jlema"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void crearClienteInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"","genero":"Masculino","edad":35,"identificacion":"1","clienteId":"x"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Datos de entrada inválidos"));
    }

    @Test
    void clienteInexistenteDevuelve404() throws Exception {
        when(service.obtener("nadie")).thenThrow(new ResourceNotFoundException("Cliente no encontrado: nadie"));

        mvc.perform(get("/api/clientes/nadie"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado: nadie"));
    }
}
