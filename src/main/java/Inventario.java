package com.puntoypapel.inventario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class Inventario {
    private final List<Producto> productos = new ArrayList<>();

    public void registrarProducto(Producto producto) {
        productos.add(producto);
    }

    public List<Producto> getProductos() {
        return List.copyOf(productos);
    }

    public Optional<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }

        String nombreNormalizado = nombre.trim().toLowerCase();
        return productos.stream()
                .filter(producto -> producto.getNombre().trim().toLowerCase().equals(nombreNormalizado))
                .findFirst();
    }
}
