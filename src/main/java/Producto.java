package com.puntoypapel.inventario;

import java.util.UUID;

public class Producto {
    private final UUID id = UUID.randomUUID();
    private String nombre;
    private double precio;
    private int existencias;

    public Producto(String nombre, double precio, int existencias) {
        actualizar(nombre, precio, existencias);
    }

    public void actualizar(String nombre, double precio, int existencias) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (existencias < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        }
        if (!Double.isFinite(precio) || precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        this.nombre = nombre;
        this.precio = precio;
        this.existencias = existencias;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getExistencias() {
        return existencias;
    }

    public double getPrecio() {
        return precio;
    }

    public void agregarExistencias(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor que cero.");
        }
        existencias = existencias + cantidad;
    }

    public void vender(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (cantidad > existencias) {
            throw new IllegalArgumentException("No hay suficientes existencias.");
        }
        existencias = existencias - cantidad;
    }
}
