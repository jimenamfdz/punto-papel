package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductoStockBajoTest {

    @Test
    void listaProductosCuandoAlcanzanElStockMinimo() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto bajo = new Producto("Lápiz", 8.5, 3);
        bajo.configurarStockMinimo(3);
        Producto suficiente = new Producto("Cuaderno", 20.0, 10);
        suficiente.configurarStockMinimo(3);
        inventario.registrarProducto(bajo);
        inventario.registrarProducto(suficiente);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ProductoController(inventario)).build();

        String respuesta = mockMvc.perform(get("/api/productos/stock-bajo"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var json = new ObjectMapper().readTree(respuesta);
        assertEquals(1, json.size());
        assertEquals("Lápiz", json.get(0).get("nombre").asText());
        assertEquals(3, json.get(0).get("stockMinimo").asInt());
    }

    @Test
    void actualizaElStockMinimoDesdeLaApi() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto producto = new Producto("Goma", 5.0, 4);
        inventario.registrarProducto(producto);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ProductoController(inventario)).build();

        mockMvc.perform(patch("/api/productos/{id}/stock-minimo", producto.getId())
                        .contentType(APPLICATION_JSON)
                        .content("{\"stockMinimo\":5}"))
                .andExpect(status().isOk());

        assertEquals(5, producto.getStockMinimo());
        assertEquals(1, inventario.getProductosConStockBajo().size());
    }
}
