package com.puntoypapel.inventario;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    private final ServicioVentasPersistente servicio;

    public VentaController(ServicioVentasPersistente servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/productos/{productoId}")
    public ResponseEntity<Venta> registrarVenta(
            @PathVariable("productoId") UUID productoId, @RequestBody VentaRequest solicitud) {
        solicitud.validar();
        return servicio.registrarVenta(productoId, solicitud.cantidad())
                .map(venta -> ResponseEntity.status(HttpStatus.CREATED).body(venta))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Venta> listarVentas() {
        return servicio.getVentas();
    }
}
