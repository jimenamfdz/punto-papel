package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VentaApiTest {
    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc api(Inventario inventario, AlmacenVentas almacen) {
        return MockMvcBuilders.standaloneSetup(new VentaController(
                        new ServicioVentasPersistente(inventario, almacen)))
                .setControllerAdvice(new ProductoErrorHandler()).build();
    }

    @Test
    void registraVentaDescuentaStockYDevuelveSnapshot() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto producto = new Producto("Cuaderno", 20.0, 10);
        inventario.registrarProducto(producto);
        AlmacenVentasMemoria almacen = new AlmacenVentasMemoria();

        var respuesta = api(inventario, almacen).perform(post("/api/ventas/productos/{id}", producto.getId())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":3}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        var json = mapper.readTree(respuesta);
        assertEquals(producto.getId().toString(), json.get("productoId").asText());
        assertEquals("Cuaderno", json.get("nombreProducto").asText());
        assertEquals(3, json.get("cantidad").asInt());
        assertEquals(60.0, json.get("total").asDouble());
        assertEquals(7, producto.getExistencias());
        assertEquals(1, almacen.findAll().size());
    }

    @Test
    void productoInexistenteDevuelve404YNoGuardaVenta() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        AlmacenVentasMemoria almacen = new AlmacenVentasMemoria();

        api(inventario, almacen).perform(post("/api/ventas/productos/{id}", UUID.randomUUID())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":1}"))
                .andExpect(status().isNotFound());
        assertEquals(0, almacen.findAll().size());
    }

    @Test
    void cantidadInvalidaDevuelve400SinModificarStock() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto producto = new Producto("Lápiz", 8.5, 2);
        inventario.registrarProducto(producto);
        AlmacenVentasMemoria almacen = new AlmacenVentasMemoria();

        api(inventario, almacen).perform(post("/api/ventas/productos/{id}", producto.getId())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":3}"))
                .andExpect(status().isBadRequest());
        assertEquals(2, producto.getExistencias());
        assertEquals(0, almacen.findAll().size());
    }

    @Test
    void listaVentasGuardadas() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto producto = new Producto("Pluma", 12.0, 4);
        inventario.registrarProducto(producto);
        AlmacenVentasMemoria almacen = new AlmacenVentasMemoria();
        var servicio = new ServicioVentasPersistente(inventario, almacen);
        servicio.registrarVenta(producto.getId(), 1);

        var respuesta = MockMvcBuilders.standaloneSetup(new VentaController(servicio))
                .build().perform(get("/api/ventas"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(1, mapper.readTree(respuesta).size());
    }
}
