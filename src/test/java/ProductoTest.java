package com.puntoypapel.inventario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ProductoTest {

    @Test
    void productosIgualesTienenIdsDistintos() {
        Producto primero = new Producto("Cuaderno", 20.0, 10);
        Producto segundo = new Producto("Cuaderno", 20.0, 10);

        assertNotEquals(primero.getId(), segundo.getId());
    }

    @Test
    void idSeConservaAlModificarExistencias() {
        Producto producto = new Producto("Cuaderno", 20.0, 10);
        var id = producto.getId();

        producto.agregarExistencias(5);
        producto.vender(3);

        assertEquals(id, producto.getId());
    }

    @Test
    void venderTresUnidadesDeDiezDejaSiete() {
        Producto producto = new Producto("Cuaderno", 20.0, 10);

        producto.vender(3);

        assertEquals(7, producto.getExistencias());
    }

    @Test
    void venderMasUnidadesDeLasDisponiblesLanzaError() {
        Producto producto = new Producto("Cuaderno", 20.0, 4);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> producto.vender(6)
        );

        assertEquals("No hay suficientes existencias.", error.getMessage());
        assertEquals(4, producto.getExistencias());
    }

    @Test
    void venderCeroUnidadesLanzaError() {
        Producto producto = new Producto("Cuaderno", 20.0, 10);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> producto.vender(0)
        );

        assertEquals("La cantidad debe ser mayor que cero.", error.getMessage());
        assertEquals(10, producto.getExistencias());
    }

    @Test
    void venderCantidadNegativaLanzaError() {
        Producto producto = new Producto("Cuaderno", 20.0, 10);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> producto.vender(-2)
        );

        assertEquals("La cantidad debe ser mayor que cero.", error.getMessage());
        assertEquals(10, producto.getExistencias());
    }

    @Test
    void crearProductoSinNombreLanzaError() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Producto("   ", 20.0, 10)
        );

        assertEquals("El nombre no puede estar vacío.", error.getMessage());
    }

    @Test
    void crearProductoConExistenciasNegativasLanzaError() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Producto("Cuaderno", 20.0, -1)
        );

        assertEquals("Las existencias no pueden ser negativas.", error.getMessage());
    }

    @Test
    void crearProductoConPrecioCeroLanzaError() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Producto("Cuaderno", 0.0, 10)
        );

        assertEquals("El precio debe ser mayor que cero.", error.getMessage());
    }

    @Test
    void agregarExistenciasAumentaElInventario() {
        Producto producto = new Producto("Cuaderno", 20.0, 10);

        producto.agregarExistencias(5);

        assertEquals(15, producto.getExistencias());
    }

    @Test
    void agregarCeroExistenciasLanzaError() {
        Producto producto = new Producto("Cuaderno", 20.0, 10);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> producto.agregarExistencias(0)
        );

        assertEquals("La cantidad a agregar debe ser mayor que cero.", error.getMessage());
        assertEquals(10, producto.getExistencias());
    }

}
