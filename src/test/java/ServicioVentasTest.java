package com.puntoypapel.inventario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioVentasTest {

    @Test
    void registraVentaYDescuentaExistencias() {
        Producto producto = new Producto("Cuaderno", 20.0, 10);
        ServicioVentas servicio = new ServicioVentas();

        servicio.registrarVenta(producto, 3);

        assertEquals(7, producto.getExistencias());
        assertEquals(1, servicio.getVentas().size());
        assertEquals("Cuaderno", servicio.getVentas().get(0).getNombreProducto());
        assertEquals(3, servicio.getVentas().get(0).getCantidad());
        assertEquals(20.0, servicio.getVentas().get(0).getPrecioUnitario());
        assertEquals(60.0, servicio.getVentas().get(0).getTotal());
    }

    @Test
    void ventaInvalidaNoSeGuarda() {
        Producto producto = new Producto("Cuaderno", 20.0, 2);
        ServicioVentas servicio = new ServicioVentas();

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.registrarVenta(producto, 3)
        );

        assertEquals(0, servicio.getVentas().size());
        assertEquals(2, producto.getExistencias());
    }
}
