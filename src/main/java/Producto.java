package com.puntoypapel.inventario;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private double precio;
    @Column(nullable = false)
    private int existencias;
    @Column(nullable = false)
    private String categoria;
    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    protected Producto() {
        // Requerido por JPA para recuperar productos de la base de datos.
    }

    public Producto(String nombre, double precio, int existencias) {
        this(nombre, precio, existencias, "");
    }

    public Producto(String nombre, double precio, int existencias, String categoria) {
        actualizar(nombre, precio, existencias, categoria);
    }

    public void actualizar(String nombre, double precio, int existencias) {
        actualizar(nombre, precio, existencias, categoria);
    }

    public void actualizar(String nombre, double precio, int existencias, String categoria) {
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
        this.categoria = categoria == null ? "" : categoria.trim();
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

    @com.fasterxml.jackson.annotation.JsonInclude(
            com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
    public String getCategoria() {
        return categoria;
    }

    @com.fasterxml.jackson.annotation.JsonInclude(
            com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT)
    public int getStockMinimo() {
        return stockMinimo;
    }

    public void configurarStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        this.stockMinimo = stockMinimo;
    }

    public boolean tieneStockBajo() {
        return existencias <= stockMinimo;
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
