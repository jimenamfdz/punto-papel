package com.puntoypapel.inventario;

import java.util.ArrayList;
import java.util.List;

public class ServicioVentas {
    private final List<Venta> ventas = new ArrayList<>();

    public void registrarVenta(Producto producto, int cantidad) {
        producto.vender(cantidad);
        ventas.add(new Venta(producto.getNombre(), producto.getPrecio(), cantidad));
    }

    public List<Venta> getVentas() {
        return List.copyOf(ventas);
    }
}
