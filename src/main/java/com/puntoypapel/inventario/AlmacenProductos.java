package com.puntoypapel.inventario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlmacenProductos {
    Producto save(Producto producto);
    List<Producto> findAll();
    Optional<Producto> findById(UUID id);
}
