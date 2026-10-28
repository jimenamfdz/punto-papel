package com.puntoypapel.inventario;

import java.util.UUID;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, UUID>, AlmacenVentas {
    List<Venta> findAllByOrderByFechaDesc();
    List<Venta> findByProductoIdOrderByFechaDesc(UUID productoId);
    List<Venta> findByFechaBetweenOrderByFechaDesc(Instant desde, Instant hasta);
    List<Venta> findByProductoIdAndFechaBetweenOrderByFechaDesc(
            UUID productoId, Instant desde, Instant hasta);
}
