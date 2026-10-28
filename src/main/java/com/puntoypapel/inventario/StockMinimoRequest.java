package com.puntoypapel.inventario;

public record StockMinimoRequest(Integer stockMinimo) {

    public void validar() {
        if (stockMinimo == null) {
            throw new IllegalArgumentException("El stock mínimo es obligatorio.");
        }
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
    }
}
