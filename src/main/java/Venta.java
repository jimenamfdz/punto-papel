package com.puntoypapel.inventario;

public class Venta {
    private final String nombreProducto;
    private final double precioUnitario;
    private final int cantidad;
    private final double total;

    public Venta(String nombreProducto, double precioUnitario, int cantidad) {
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.total = precioUnitario * cantidad;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getTotal() {
        return total;
    }
}
