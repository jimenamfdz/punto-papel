package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductoCategoriaTest {

    @Test
    void registraProductoConCategoria() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ProductoController(inventario)).build();

        String respuesta = mockMvc.perform(post("/api/productos")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombre\":\"Cuaderno\",\"precio\":20,\"existencias\":5,\"categoria\":\"Papelería\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertEquals("Papelería", new ObjectMapper().readTree(respuesta).get("categoria").asText());
    }
}
