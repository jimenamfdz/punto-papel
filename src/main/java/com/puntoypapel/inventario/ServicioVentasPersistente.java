package com.puntoypapel.inventario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioVentasPersistente {
    private final Inventario inventario;
    private final AlmacenVentas ventas;

    public ServicioVentasPersistente(Inventario inventario, AlmacenVentas ventas) {
        this.inventario = inventario;
        this.ventas = ventas;
    }

    @Transactional
    public Optional<Venta> registrarVenta(UUID productoId, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        return inventario.buscarPorId(productoId).map(producto -> {
            producto.vender(cantidad);
            return ventas.save(new Venta(producto.getId(), producto.getNombre(), producto.getPrecio(), cantidad));
        });
    }

    @Transactional(readOnly = true)
    public List<Venta> getVentas() {
        return List.copyOf(ventas.findAllByOrderByFechaDesc());
    }

    @Transactional(readOnly = true)
    public List<Venta> buscarVentas(UUID productoId, Instant desde, Instant hasta) {
        if (desde != null && hasta != null && !desde.isBefore(hasta)) {
            throw new IllegalArgumentException("La fecha inicial debe ser anterior a la fecha final.");
        }
        if (productoId != null && desde != null && hasta != null) {
            return List.copyOf(ventas.findByProductoIdAndFechaBetweenOrderByFechaDesc(
                    productoId, desde, hasta));
        }
        if (productoId != null) {
            return List.copyOf(ventas.findByProductoIdOrderByFechaDesc(productoId));
        }
        if (desde != null && hasta != null) {
            return List.copyOf(ventas.findByFechaBetweenOrderByFechaDesc(desde, hasta));
        }
        return getVentas();
    }
}
