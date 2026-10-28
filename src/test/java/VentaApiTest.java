package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneOffset;
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

    @Test
    void filtraHistorialPorProductoYFecha() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto primero = new Producto("Pluma", 12.0, 4);
        Producto segundo = new Producto("Goma", 5.0, 4);
        inventario.registrarProducto(primero);
        inventario.registrarProducto(segundo);
        AlmacenVentasMemoria almacen = new AlmacenVentasMemoria();
        var api = api(inventario, almacen);
        api.perform(post("/api/ventas/productos/{id}", primero.getId())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":1}"))
                .andExpect(status().isCreated());
        api.perform(post("/api/ventas/productos/{id}", segundo.getId())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":1}"))
                .andExpect(status().isCreated());
        String hoy = LocalDate.now(ZoneOffset.UTC).toString();

        var respuesta = api.perform(get("/api/ventas")
                        .param("productoId", primero.getId().toString())
                        .param("desde", hoy).param("hasta", hoy))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        var ventas = mapper.readTree(respuesta);
        assertEquals(1, ventas.size());
        assertEquals(primero.getId().toString(), ventas.get(0).get("productoId").asText());
    }

    @Test
    void rechazaRangoDeFechasInvertido() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        var respuesta = api(inventario, new AlmacenVentasMemoria())
                .perform(get("/api/ventas").param("desde", "2026-10-20").param("hasta", "2026-10-19"))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        assertEquals("La fecha inicial debe ser anterior a la fecha final.",
                mapper.readTree(respuesta).get("mensaje").asText());
    }

    @Test
    void calculaResumenGeneralYPorProducto() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto cuaderno = new Producto("Cuaderno", 20.0, 10);
        Producto lapiz = new Producto("Lápiz", 8.0, 10);
        inventario.registrarProducto(cuaderno);
        inventario.registrarProducto(lapiz);
        AlmacenVentasMemoria almacen = new AlmacenVentasMemoria();
        var api = api(inventario, almacen);
        api.perform(post("/api/ventas/productos/{id}", cuaderno.getId())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":2}"))
                .andExpect(status().isCreated());
        api.perform(post("/api/ventas/productos/{id}", cuaderno.getId())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":1}"))
                .andExpect(status().isCreated());
        api.perform(post("/api/ventas/productos/{id}", lapiz.getId())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":4}"))
                .andExpect(status().isCreated());

        var respuesta = api.perform(get("/api/ventas/resumen"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var resumen = mapper.readTree(respuesta);
        assertEquals(3, resumen.get("totalVentas").asInt());
        assertEquals(7, resumen.get("unidadesVendidas").asInt());
        assertEquals(92.0, resumen.get("importeTotal").asDouble());
        assertEquals(2, resumen.get("porProducto").size());
        var resumenCuaderno = java.util.stream.StreamSupport.stream(
                        resumen.get("porProducto").spliterator(), false)
                .filter(item -> cuaderno.getId().toString().equals(item.get("productoId").asText()))
                .findFirst().orElseThrow();
        var resumenLapiz = java.util.stream.StreamSupport.stream(
                        resumen.get("porProducto").spliterator(), false)
                .filter(item -> lapiz.getId().toString().equals(item.get("productoId").asText()))
                .findFirst().orElseThrow();
        assertEquals(3, resumenCuaderno.get("unidadesVendidas").asInt());
        assertEquals(60.0, resumenCuaderno.get("importeTotal").asDouble());
        assertEquals(2, resumenCuaderno.get("numeroVentas").asInt());
        assertEquals(1, resumenLapiz.get("numeroVentas").asInt());
    }

    @Test
    void resumenRespetaFiltrosDeProductoYFecha() throws Exception {
        Inventario inventario = new Inventario(new AlmacenProductosMemoria());
        Producto producto = new Producto("Goma", 5.0, 3);
        inventario.registrarProducto(producto);
        AlmacenVentasMemoria almacen = new AlmacenVentasMemoria();
        var api = api(inventario, almacen);
        api.perform(post("/api/ventas/productos/{id}", producto.getId())
                        .contentType(APPLICATION_JSON).content("{\"cantidad\":2}"))
                .andExpect(status().isCreated());
        String hoy = LocalDate.now(ZoneOffset.UTC).toString();

        var respuesta = api.perform(get("/api/ventas/resumen")
                        .param("productoId", producto.getId().toString())
                        .param("desde", hoy).param("hasta", hoy))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var resumen = mapper.readTree(respuesta);
        assertEquals(1, resumen.get("totalVentas").asInt());
        assertEquals(2, resumen.get("unidadesVendidas").asInt());
        assertEquals(10.0, resumen.get("importeTotal").asDouble());
    }
}
