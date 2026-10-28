package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductoActualizacionTest {

    private MockMvc api(Inventario inventario) {
        return MockMvcBuilders.standaloneSetup(new ProductoController(inventario))
                .setControllerAdvice(new ProductoErrorHandler())
                .build();
    }

    @Test
    void actualizaSoloElProductoElegidoConservandoSuId() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto producto = new Producto("Cuaderno", 20.0, 10);
        Producto otro = new Producto("Cuaderno", 35.0, 5);
        inventario.registrarProducto(producto);
        inventario.registrarProducto(otro);
        UUID id = producto.getId();
        MockMvc mockMvc = api(inventario);

        var respuesta = mockMvc.perform(put("/api/productos/{id}", id)
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombre\":\"Cuaderno profesional\",\"precio\":45.5,\"existencias\":0}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        var esperado = mapper.readTree("{\"id\":\"" + id
                + "\",\"nombre\":\"Cuaderno profesional\",\"precio\":45.5,\"existencias\":0}");
        assertEquals(esperado, mapper.readTree(respuesta));
        assertEquals(id, producto.getId());
        assertEquals(2, inventario.getProductos().size());
        assertEquals("Cuaderno", otro.getNombre());
        assertEquals(35.0, otro.getPrecio());
        assertEquals(5, otro.getExistencias());

        var consulta = mockMvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(esperado, mapper.readTree(consulta));
        var listado = mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(esperado, mapper.readTree(listado).get(0));
        assertEquals(producto, inventario.buscarPorNombre("Cuaderno profesional").orElseThrow());
    }

    @Test
    void devuelve404SinCrearProductoSiElIdNoExiste() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto producto = new Producto("Lápiz", 8.5, 20);
        inventario.registrarProducto(producto);

        api(inventario).perform(put("/api/productos/{id}", UUID.randomUUID())
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombre\":\"Cuaderno\",\"precio\":20,\"existencias\":10}"))
                .andExpect(status().isNotFound());

        assertEquals(List.of(producto), inventario.getProductos());
        assertEquals("Lápiz", producto.getNombre());
        assertEquals(8.5, producto.getPrecio());
        assertEquals(20, producto.getExistencias());
    }

    @Test
    void rechazaIdConFormatoInvalido() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        var respuesta = api(inventario).perform(put("/api/productos/id-invalido")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombre\":\"Cuaderno\",\"precio\":20,\"existencias\":10}"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertEquals("El ID del producto debe tener formato UUID.",
                new ObjectMapper().readTree(respuesta).get("mensaje").asText());
        assertEquals(0, inventario.getProductos().size());
    }
}
