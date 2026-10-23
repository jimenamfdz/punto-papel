package com.puntoypapel.inventario;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoControllerTest {

    @Test
    void listaProductosEnFormatoJson() throws Exception {
        Inventario inventario = new Inventario();
        inventario.registrarProducto(new Producto("Cuaderno", 20.0, 10));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(inventario))
                .build();

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"nombre\":\"Cuaderno\",\"precio\":20.0,\"existencias\":10}]"));
    }

    @Test
    void registraProductoDesdeJson() throws Exception {
        Inventario inventario = new Inventario();
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(inventario))
                .build();

        mockMvc.perform(post("/api/productos")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombre\":\"Lápiz\",\"precio\":8.5,\"existencias\":20}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"nombre\":\"Lápiz\",\"precio\":8.5,\"existencias\":20}"));

        assertEquals(1, inventario.getProductos().size());
    }
}
