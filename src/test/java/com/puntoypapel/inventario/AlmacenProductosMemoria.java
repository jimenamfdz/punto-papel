package com.puntoypapel.inventario;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Almacenamiento aislado para las pruebas unitarias; la aplicación usa PostgreSQL.
class AlmacenProductosMemoria implements AlmacenProductos {
    private final Map<UUID, Producto> productos = new LinkedHashMap<>();

    @Override
    public Producto save(Producto producto) {
        productos.put(producto.getId(), producto);
        return producto;
    }

    @Override
    public List<Producto> findAll() {
        return List.copyOf(productos.values());
    }

    @Override
    public Optional<Producto> findById(UUID id) {
        return Optional.ofNullable(productos.get(id));
    }
}
