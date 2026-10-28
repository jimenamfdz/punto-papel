package com.puntoypapel.inventario;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

public interface AlmacenVentas {
    Venta save(Venta venta);
    List<Venta> findAll();
    List<Venta> findAllByOrderByFechaDesc();
    List<Venta> findByProductoIdOrderByFechaDesc(UUID productoId);
    List<Venta> findByFechaBetweenOrderByFechaDesc(Instant desde, Instant hasta);
    List<Venta> findByProductoIdAndFechaBetweenOrderByFechaDesc(
            UUID productoId, Instant desde, Instant hasta);
}
