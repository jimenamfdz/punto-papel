package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoControllerTest {

    private static org.springframework.test.web.servlet.ResultMatcher jsonIgualA(String esperado) {
        return resultado -> {
            ObjectMapper mapper = new ObjectMapper();
            assertEquals(mapper.readTree(esperado),
                    mapper.readTree(resultado.getResponse().getContentAsString()));
        };
    }

    @Test
    void listaProductosEnFormatoJson() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto producto = new Producto("Cuaderno", 20.0, 10);
        inventario.registrarProducto(producto);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(inventario))
                .build();

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonIgualA("[{\"id\":\"" + producto.getId()
                        + "\",\"nombre\":\"Cuaderno\",\"precio\":20.0,\"existencias\":10}]"));
    }

    @Test
    void registraProductoDesdeJson() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(inventario))
                .build();

        var respuesta = mockMvc.perform(post("/api/productos")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombre\":\"Lápiz\",\"precio\":8.5,\"existencias\":20}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertEquals(1, inventario.getProductos().size());
        Producto producto = inventario.getProductos().get(0);
        var json = new ObjectMapper().readTree(respuesta);
        assertEquals(producto.getId().toString(), json.get("id").asText());
        assertEquals("Lápiz", json.get("nombre").asText());
        assertEquals(8.5, json.get("precio").asDouble());
        assertEquals(20, json.get("existencias").asInt());

        mockMvc.perform(get("/api/productos/{id}", json.get("id").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonIgualA(respuesta));
    }

    @Test
    void buscaElProductoCorrectoPorIdAunqueSeRepitaElNombre() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        inventario.registrarProducto(new Producto("Cuaderno", 20.0, 10));
        Producto producto = new Producto("Cuaderno", 35.0, 5);
        inventario.registrarProducto(producto);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(inventario))
                .build();

        mockMvc.perform(get("/api/productos/{id}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonIgualA("{\"id\":\"" + producto.getId()
                        + "\",\"nombre\":\"Cuaderno\",\"precio\":35.0,\"existencias\":5}"));
    }

    @Test
    void devuelve404SiElIdNoExiste() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        inventario.registrarProducto(new Producto("Cuaderno", 20.0, 10));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(inventario))
                .build();

        mockMvc.perform(get("/api/productos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void devuelve400SiElIdTieneFormatoInvalido() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(new Inventario(new AlmacenProductosMemoria())))
                .build();

        mockMvc.perform(get("/api/productos/no-es-un-uuid"))
                .andExpect(status().isBadRequest());
    }
}
