package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Se ejecuta únicamente contra la base de pruebas, nunca contra punto_papel.
@EnabledIfEnvironmentVariable(named = "DB_INTEGRATION_TEST_URL",
        matches = "jdbc:postgresql://[^/]+/punto_papel_test")
class PersistenciaPostgresTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    private ServletWebServerApplicationContext iniciarAplicacion() {
        return (ServletWebServerApplicationContext) new SpringApplicationBuilder(PuntoPapelApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1",
                        "--spring.datasource.url=" + System.getenv("DB_INTEGRATION_TEST_URL"),
                        "--spring.main.banner-mode=off", "--logging.level.root=WARN");
    }

    private HttpResponse<String> peticion(ServletWebServerApplicationContext app,
                                          String metodo, String ruta, String cuerpo) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(
                        "http://127.0.0.1:" + app.getWebServer().getPort() + ruta))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo)).build();
        return cliente.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void conservaProductoYActualizacionTrasReiniciarAplicacion() throws Exception {
        String nombre = "Cuaderno prueba " + UUID.randomUUID();
        String id;
        JsonNode registrado;
        try (var app = iniciarAplicacion()) {
            var respuesta = peticion(app, "POST", "/api/productos",
                    mapper.writeValueAsString(new ProductoRequest(nombre, 20.0, 10)));
            assertEquals(201, respuesta.statusCode());
            registrado = mapper.readTree(respuesta.body());
            id = registrado.get("id").asText();
        }

        JsonNode actualizado;
        try (var app = iniciarAplicacion()) {
            var consulta = peticion(app, "GET", "/api/productos/" + id, null);
            assertEquals(200, consulta.statusCode());
            assertEquals(registrado, mapper.readTree(consulta.body()));

            var respuesta = peticion(app, "PUT", "/api/productos/" + id,
                    mapper.writeValueAsString(new ProductoRequest(nombre + " actualizado", 45.5, 0)));
            assertEquals(200, respuesta.statusCode());
            actualizado = mapper.readTree(respuesta.body());
            assertEquals(id, actualizado.get("id").asText());
            assertEquals(45.5, actualizado.get("precio").asDouble());
            assertEquals(0, actualizado.get("existencias").asInt());
        }

        try (var app = iniciarAplicacion()) {
            try {
                var consulta = peticion(app, "GET", "/api/productos/" + id, null);
                assertEquals(200, consulta.statusCode());
                assertEquals(actualizado, mapper.readTree(consulta.body()));

                var invalida = peticion(app, "PUT", "/api/productos/" + id,
                        mapper.writeValueAsString(new ProductoRequest("No guardar", -1.0, 7)));
                assertEquals(400, invalida.statusCode());
                assertEquals(actualizado, mapper.readTree(
                        peticion(app, "GET", "/api/productos/" + id, null).body()));
                var lista = mapper.readTree(peticion(app, "GET", "/api/productos", null).body());
                int coincidencias = 0;
                for (JsonNode producto : lista) {
                    if (producto.get("id").asText().equals(id)) {
                        assertEquals(actualizado, producto);
                        coincidencias++;
                    }
                }
                assertEquals(1, coincidencias);
                assertEquals(404, peticion(app, "GET", "/api/productos/" + UUID.randomUUID(), null).statusCode());
            } finally {
                app.getBean(ProductoRepository.class).deleteById(UUID.fromString(id));
            }
        }
    }
}
