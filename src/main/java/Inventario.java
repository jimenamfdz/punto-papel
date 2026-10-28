package com.puntoypapel.inventario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class Inventario {
    private final AlmacenProductos productos;

    public Inventario(AlmacenProductos productos) {
        this.productos = productos;
    }

    @Transactional
    public void registrarProducto(Producto producto) {
        productos.save(producto);
    }

    public List<Producto> getProductos() {
        return List.copyOf(productos.findAll());
    }

    public List<Producto> getProductosPorCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return getProductos();
        }
        String categoriaNormalizada = categoria.trim().toLowerCase();
        return productos.findAll().stream()
                .filter(producto -> producto.getCategoria().trim().toLowerCase()
                        .equals(categoriaNormalizada))
                .toList();
    }

    public List<Producto> getProductosConStockBajo() {
        return productos.findAll().stream()
                .filter(Producto::tieneStockBajo)
                .toList();
    }

    public Optional<Producto> buscarPorId(UUID id) {
        return productos.findById(id);
    }

    @Transactional
    public Optional<Producto> actualizarProducto(UUID id, String nombre, double precio, int existencias) {
        return actualizarProducto(id, nombre, precio, existencias, null);
    }

    @Transactional
    public Optional<Producto> actualizarProducto(
            UUID id, String nombre, double precio, int existencias, String categoria) {
        return productos.findById(id).map(producto -> {
            if (categoria == null) {
                producto.actualizar(nombre, precio, existencias);
            } else {
                producto.actualizar(nombre, precio, existencias, categoria);
            }
            return productos.save(producto);
        });
    }

    @Transactional
    public Optional<Producto> actualizarStockMinimo(UUID id, int stockMinimo) {
        return productos.findById(id).map(producto -> {
            producto.configurarStockMinimo(stockMinimo);
            return productos.save(producto);
        });
    }

    public Optional<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }

        String nombreNormalizado = nombre.trim().toLowerCase();
        return productos.findAll().stream()
                .filter(producto -> producto.getNombre().trim().toLowerCase().equals(nombreNormalizado))
                .findFirst();
    }
}
