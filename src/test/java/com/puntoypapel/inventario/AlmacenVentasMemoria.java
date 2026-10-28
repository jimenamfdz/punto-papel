package com.puntoypapel.inventario;

import java.util.ArrayList;
import java.util.List;

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
}
