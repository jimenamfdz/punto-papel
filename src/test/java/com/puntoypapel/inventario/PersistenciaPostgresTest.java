package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
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
        var constructor = HttpRequest.newBuilder(URI.create(
                        "http://127.0.0.1:" + app.getWebServer().getPort() + ruta))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json");
        if (!ruta.startsWith("/api/usuarios")) {
            constructor.header("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                    "admin:secreto123".getBytes()));
        }
        var request = constructor.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo)).build();
        return cliente.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> peticionSinCredenciales(ServletWebServerApplicationContext app,
                                                         String metodo, String ruta) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(
                        "http://127.0.0.1:" + app.getWebServer().getPort() + ruta))
                .timeout(Duration.ofSeconds(15))
                .method(metodo, HttpRequest.BodyPublishers.noBody()).build();
        return cliente.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void conservaProductoYActualizacionTrasReiniciarAplicacion() throws Exception {
        String nombre = "Cuaderno prueba " + UUID.randomUUID();
        String id;
        JsonNode registrado;
        try (var app = iniciarAplicacion()) {
            assertEquals(200, peticionSinCredenciales(app, "GET", "/v3/api-docs").statusCode());
            assertEquals(401, peticionSinCredenciales(app, "GET", "/api/productos").statusCode());
            // El registro es público; después todas las operaciones usan Basic Auth.
            peticion(app, "POST", "/api/usuarios",
                    "{\"nombreUsuario\":\"admin\",\"password\":\"secreto123\",\"rol\":\"ADMIN\"}");
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
                    mapper.writeValueAsString(new ProductoRequest(nombre + " actualizado", 45.5, 3)));
            assertEquals(200, respuesta.statusCode());
            actualizado = mapper.readTree(respuesta.body());
            assertEquals(id, actualizado.get("id").asText());
            assertEquals(45.5, actualizado.get("precio").asDouble());
            assertEquals(3, actualizado.get("existencias").asInt());
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

                var venta = peticion(app, "POST", "/api/ventas/productos/" + id, "{\"cantidad\":2}");
                assertEquals(201, venta.statusCode());
                assertEquals(id, mapper.readTree(venta.body()).get("productoId").asText());
                assertEquals(2, mapper.readTree(venta.body()).get("cantidad").asInt());
                assertEquals(91.0, mapper.readTree(venta.body()).get("total").asDouble());
                var ventas = peticion(app, "GET", "/api/ventas", null);
                assertEquals(200, ventas.statusCode());
                assertEquals(1, mapper.readTree(ventas.body()).size());
            } finally {
                app.getBean(VentaRepository.class).deleteAll();
                app.getBean(ProductoRepository.class).deleteById(UUID.fromString(id));
                app.getBean(UsuarioRepository.class).deleteAll();
            }
        }
    }
}
