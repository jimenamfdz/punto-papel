package com.puntoypapel.inventario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventarioTest {

    @Test
    void inventarioComienzaVacio() {
        Inventario inventario = new Inventario();

        assertEquals(0, inventario.getProductos().size());
    }

    @Test
    void inventarioRegistraUnProducto() {
        Inventario inventario = new Inventario();
        Producto producto = new Producto("Cuaderno", 20.0, 10);

        inventario.registrarProducto(producto);

        assertEquals(1, inventario.getProductos().size());
        assertEquals("Cuaderno", inventario.getProductos().get(0).getNombre());
    }

    @Test
    void buscaProductoSinDistinguirMayusculasNiEspacios() {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(new Producto("Cuaderno", 20.0, 10));

        var resultado = inventario.buscarPorNombre("  CUADERNO ");

        assertTrue(resultado.isPresent());
        assertEquals("Cuaderno", resultado.get().getNombre());
    }

    @Test
    void devuelveResultadoVacioCuandoNoEncuentraProducto() {
        Inventario inventario = new Inventario();

        var resultado = inventario.buscarPorNombre("Lápiz");

        assertTrue(resultado.isEmpty());
    }
}
