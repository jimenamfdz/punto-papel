package com.puntoypapel.inventario;

import java.util.List;
import java.util.UUID;

public record ResumenVentas(
        long totalVentas,
        long unidadesVendidas,
        double importeTotal,
        List<ProductoResumen> porProducto) {

    public record ProductoResumen(
            UUID productoId,
            String nombreProducto,
            long unidadesVendidas,
            double importeTotal,
            long numeroVentas) {
    }
}
