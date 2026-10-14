package com.tcs.account.controller;

import com.tcs.account.dto.ReporteEstadoCuenta;
import com.tcs.account.service.ReporteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    /** Ejemplo: GET /api/reportes?fecha=2022-02-01,2022-02-28&clienteId=mmontalvo */
    @GetMapping
    public ReporteEstadoCuenta estadoDeCuenta(@RequestParam String fecha, @RequestParam String clienteId) {
        return service.estadoDeCuenta(clienteId, fecha);
    }
}
