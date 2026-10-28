package com.puntoypapel.inventario;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    private UUID id = UUID.randomUUID();
    private UUID productoId;
    @Column(nullable = false)
    private final String nombreProducto;
    @Column(nullable = false)
    private final double precioUnitario;
    @Column(nullable = false)
    private final int cantidad;
    @Column(nullable = false)
    private final double total;
    @Column(nullable = false)
    private Instant fecha;

    protected Venta() {
        this.nombreProducto = null;
        this.precioUnitario = 0;
        this.cantidad = 0;
        this.total = 0;
    }

    public Venta(String nombreProducto, double precioUnitario, int cantidad) {
        validarDatos(nombreProducto, precioUnitario, cantidad);
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.total = precioUnitario * cantidad;
        this.fecha = Instant.now();
    }

    public Venta(UUID productoId, String nombreProducto, double precioUnitario, int cantidad) {
        if (productoId == null) {
            throw new IllegalArgumentException("El producto de la venta es obligatorio.");
        }
        validarDatos(nombreProducto, precioUnitario, cantidad);
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.total = precioUnitario * cantidad;
        this.fecha = Instant.now();
    }

    private static void validarDatos(String nombreProducto, double precioUnitario, int cantidad) {
        if (nombreProducto == null || nombreProducto.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        if (!Double.isFinite(precioUnitario) || precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
    }

    @PrePersist
    void asignarFechaSiFalta() {
        if (fecha == null) {
            fecha = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductoId() {
        return productoId;
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

    public Instant getFecha() {
        return fecha;
    }
}
