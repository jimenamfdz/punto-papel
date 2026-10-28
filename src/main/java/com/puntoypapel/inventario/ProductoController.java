package com.puntoypapel.inventario;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final Inventario inventario;

    public ProductoController(Inventario inventario) {
        this.inventario = inventario;
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return inventario.getProductos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarProductoPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.of(inventario.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Producto> registrarProducto(@RequestBody ProductoRequest solicitud) {
        Producto producto = new Producto(
                solicitud.nombre(),
                solicitud.precio(),
                solicitud.existencias()
        );
        inventario.registrarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }
}
