package com.puntoypapel.inventario;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VentaPersistenciaTest {

    @Test
    void conservaProductoCantidadPrecioTotalYFecha() {
        UUID productoId = UUID.randomUUID();

        Venta venta = new Venta(productoId, "Cuaderno", 20.0, 3);

        assertNotNull(venta.getId());
        assertEquals(productoId, venta.getProductoId());
        assertEquals("Cuaderno", venta.getNombreProducto());
        assertEquals(20.0, venta.getPrecioUnitario());
        assertEquals(3, venta.getCantidad());
        assertEquals(60.0, venta.getTotal());
        assertNotNull(venta.getFecha());
    }

    @Test
    void rechazaVentaSinProducto() {
        assertThrows(IllegalArgumentException.class,
                () -> new Venta(null, "Cuaderno", 20.0, 1));
    }

    @Test
    void rechazaCantidadNoPositiva() {
        assertThrows(IllegalArgumentException.class,
                () -> new Venta(UUID.randomUUID(), "Cuaderno", 20.0, 0));
    }
}
