package com.puntoypapel.inventario;

public record VentaRequest(Integer cantidad) {
    public void validar() {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad es obligatoria.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
    }
}
