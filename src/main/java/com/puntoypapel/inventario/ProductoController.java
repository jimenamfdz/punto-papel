package com.puntoypapel.inventario;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final Inventario inventario;

    public ProductoController(Inventario inventario) {
        this.inventario = inventario;
    }

    @GetMapping
    public List<Producto> listarProductos(
            @RequestParam(value = "categoria", required = false) String categoria) {
        return inventario.getProductosPorCategoria(categoria);
    }

    @GetMapping("/stock-bajo")
    public List<Producto> listarProductosConStockBajo() {
        return inventario.getProductosConStockBajo();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarProductoPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.of(inventario.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable("id") UUID id, @RequestBody ProductoRequest solicitud) {
        solicitud.validarCamposObligatorios();
        return ResponseEntity.of(inventario.actualizarProducto(
                id, solicitud.nombre(), solicitud.precio(), solicitud.existencias(), solicitud.categoria()));
    }

    @PatchMapping("/{id}/stock-minimo")
    public ResponseEntity<Producto> actualizarStockMinimo(
            @PathVariable("id") UUID id, @RequestBody StockMinimoRequest solicitud) {
        solicitud.validar();
        return ResponseEntity.of(inventario.actualizarStockMinimo(id, solicitud.stockMinimo()));
    }

    @PostMapping
    public ResponseEntity<Producto> registrarProducto(@RequestBody ProductoRequest solicitud) {
        solicitud.validarCamposObligatorios();
        Producto producto = new Producto(
                solicitud.nombre(),
                solicitud.precio(),
                solicitud.existencias(),
                solicitud.categoria()
        );
        inventario.registrarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }
}
