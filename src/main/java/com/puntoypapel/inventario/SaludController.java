package com.puntoypapel.inventario;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SaludController {

    @GetMapping("/saludo")
    public String saludo() {
        return "Punto & Papel API funcionando";
    }
}
