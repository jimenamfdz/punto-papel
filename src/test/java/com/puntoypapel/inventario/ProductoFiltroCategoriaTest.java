package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductoFiltroCategoriaTest {

    @Test
    void filtraProductosPorCategoriaSinDistinguirMayusculas() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        inventario.registrarProducto(new Producto("Cuaderno", 20.0, 5, "Papelería"));
        inventario.registrarProducto(new Producto("Mochila", 250.0, 2, "Accesorios"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ProductoController(inventario)).build();

        String respuesta = mockMvc.perform(get("/api/productos").param("categoria", "PAPELERÍA"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var json = new ObjectMapper().readTree(respuesta);
        assertEquals(1, json.size());
        assertEquals("Cuaderno", json.get(0).get("nombre").asText());
    }
}
