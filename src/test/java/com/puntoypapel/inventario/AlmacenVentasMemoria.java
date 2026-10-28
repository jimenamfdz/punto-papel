package com.puntoypapel.inventario;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.UUID;
import java.util.Comparator;

class AlmacenVentasMemoria implements AlmacenVentas {
    private final List<Venta> ventas = new ArrayList<>();

    @Override
    public Venta save(Venta venta) {
        ventas.add(venta);
        return venta;
    }

    @Override
    public List<Venta> findAll() {
        return List.copyOf(ventas);
    }

    @Override
    public List<Venta> findAllByOrderByFechaDesc() {
        return ordenadas(ventas);
    }

    @Override
    public List<Venta> findByProductoIdOrderByFechaDesc(UUID productoId) {
        return ordenadas(ventas.stream().filter(v -> productoId.equals(v.getProductoId())).toList());
    }

    @Override
    public List<Venta> findByFechaBetweenOrderByFechaDesc(Instant desde, Instant hasta) {
        return ordenadas(ventas.stream().filter(v -> enRango(v, desde, hasta)).toList());
    }

    @Override
    public List<Venta> findByProductoIdAndFechaBetweenOrderByFechaDesc(
            UUID productoId, Instant desde, Instant hasta) {
        return ordenadas(ventas.stream()
                .filter(v -> productoId.equals(v.getProductoId()) && enRango(v, desde, hasta)).toList());
    }

    private static boolean enRango(Venta venta, Instant desde, Instant hasta) {
        return !venta.getFecha().isBefore(desde) && venta.getFecha().isBefore(hasta);
    }

    private static List<Venta> ordenadas(List<Venta> valores) {
        return valores.stream().sorted(Comparator.comparing(Venta::getFecha).reversed()).toList();
    }
}
