package com.puntoypapel.inventario;

public record ProductoRequest(String nombre, Double precio, Integer existencias) {

    public void validarCamposObligatorios() {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (precio == null) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
        if (existencias == null) {
            throw new IllegalArgumentException("Las existencias son obligatorias.");
        }
    }
}
