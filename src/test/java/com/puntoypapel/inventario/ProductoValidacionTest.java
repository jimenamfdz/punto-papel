package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductoValidacionTest {

    private record Caso(String descripcion, String json, String mensaje) {
    }

    private MockMvc api(Inventario inventario) {
        return MockMvcBuilders.standaloneSetup(new ProductoController(inventario))
                .setControllerAdvice(new ProductoErrorHandler())
                .build();
    }

    @TestFactory
    Stream<DynamicTest> rechazaDatosInvalidosSinModificarInventario() {
        String jsonInvalido = "Envía un objeto JSON válido con nombre, precio y existencias del tipo correcto.";
        return List.of(
                new Caso("nombre vacío", "{\"nombre\":\"\",\"precio\":20,\"existencias\":10}",
                        "El nombre no puede estar vacío."),
                new Caso("nombre con espacios", "{\"nombre\":\"   \",\"precio\":20,\"existencias\":10}",
                        "El nombre no puede estar vacío."),
                new Caso("nombre ausente", "{\"precio\":20,\"existencias\":10}",
                        "El nombre es obligatorio."),
                new Caso("nombre nulo", "{\"nombre\":null,\"precio\":20,\"existencias\":10}",
                        "El nombre es obligatorio."),
                new Caso("precio cero", "{\"nombre\":\"Cuaderno\",\"precio\":0,\"existencias\":10}",
                        "El precio debe ser mayor que cero."),
                new Caso("precio negativo", "{\"nombre\":\"Cuaderno\",\"precio\":-1,\"existencias\":10}",
                        "El precio debe ser mayor que cero."),
                new Caso("precio ausente", "{\"nombre\":\"Cuaderno\",\"existencias\":10}",
                        "El precio es obligatorio."),
                new Caso("precio nulo", "{\"nombre\":\"Cuaderno\",\"precio\":null,\"existencias\":10}",
                        "El precio es obligatorio."),
                new Caso("precio no finito", "{\"nombre\":\"Cuaderno\",\"precio\":1e400,\"existencias\":10}",
                        "El precio debe ser mayor que cero."),
                new Caso("existencias negativas", "{\"nombre\":\"Cuaderno\",\"precio\":20,\"existencias\":-1}",
                        "Las existencias no pueden ser negativas."),
                new Caso("existencias ausentes", "{\"nombre\":\"Cuaderno\",\"precio\":20}",
                        "Las existencias son obligatorias."),
                new Caso("existencias nulas", "{\"nombre\":\"Cuaderno\",\"precio\":20,\"existencias\":null}",
                        "Las existencias son obligatorias."),
                new Caso("objeto vacío", "{}", "El nombre es obligatorio."),
                new Caso("JSON incompleto", "{", jsonInvalido),
                new Caso("cuerpo ausente", "", jsonInvalido),
                new Caso("cuerpo nulo", "null", jsonInvalido),
                new Caso("precio de tipo incorrecto", "{\"nombre\":\"Cuaderno\",\"precio\":\"abc\",\"existencias\":10}",
                        jsonInvalido),
                new Caso("existencias de tipo incorrecto", "{\"nombre\":\"Cuaderno\",\"precio\":20,\"existencias\":{}}",
                        jsonInvalido)
        ).stream().map(caso -> DynamicTest.dynamicTest(caso.descripcion(), () -> {
            Inventario inventario = new Inventario();
            Producto existente = new Producto("Lápiz", 8.5, 20);
            inventario.registrarProducto(existente);

            var respuesta = api(inventario).perform(post("/api/productos")
                            .contentType(APPLICATION_JSON).content(caso.json()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andReturn().getResponse().getContentAsString();

            ObjectMapper mapper = new ObjectMapper();
            assertEquals(mapper.valueToTree(java.util.Map.of("mensaje", caso.mensaje())),
                    mapper.readTree(respuesta));
            assertEquals(List.of(existente), inventario.getProductos());
        }));
    }

    @Test
    void permiteRegistrarProductoConCeroExistencias() throws Exception {
        Inventario inventario = new Inventario();

        api(inventario).perform(post("/api/productos").contentType(APPLICATION_JSON)
                        .content("{\"nombre\":\"Cuaderno\",\"precio\":0.5,\"existencias\":0}"))
                .andExpect(status().isCreated());

        assertEquals(1, inventario.getProductos().size());
        assertEquals(0, inventario.getProductos().get(0).getExistencias());
        assertEquals(0.5, inventario.getProductos().get(0).getPrecio());
    }

    @Test
    void devuelveMensajeClaroParaIdInvalido() throws Exception {
        var respuesta = api(new Inventario()).perform(get("/api/productos/id-invalido"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertEquals("El ID del producto debe tener formato UUID.",
                new ObjectMapper().readTree(respuesta).get("mensaje").asText());
    }
}
