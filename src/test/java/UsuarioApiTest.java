package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioApiTest {
    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc api(AlmacenUsuariosMemoria almacen) {
        return MockMvcBuilders.standaloneSetup(new UsuarioController(new UsuarioService(almacen)))
                .setControllerAdvice(new ProductoErrorHandler()).build();
    }

    @Test
    void registraUsuarioDevuelveDatosPublicosYGuardaHash() throws Exception {
        AlmacenUsuariosMemoria almacen = new AlmacenUsuariosMemoria();
        var respuesta = api(almacen).perform(post("/api/usuarios")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"Ana\",\"password\":\"secreto123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        var json = mapper.readTree(respuesta);
        assertFalse(json.has("password"));
        assertFalse(json.has("passwordHash"));
        Usuario usuario = almacen.findByNombreUsuario("ana").orElseThrow();
        assertTrue(usuario.getPasswordHash().startsWith("$2"));
        assertFalse(usuario.getPasswordHash().equals("secreto123"));
        assertTrue(new UsuarioService(almacen).coincideContrasena("secreto123", usuario));
    }

    @Test
    void rechazaUsuarioDuplicado() throws Exception {
        AlmacenUsuariosMemoria almacen = new AlmacenUsuariosMemoria();
        var api = api(almacen);
        api.perform(post("/api/usuarios").contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ana\",\"password\":\"secreto123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isCreated());
        api.perform(post("/api/usuarios").contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ANA\",\"password\":\"otra1234\",\"rol\":\"VENDEDOR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaContrasenaCorta() throws Exception {
        api(new AlmacenUsuariosMemoria()).perform(post("/api/usuarios")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ana\",\"password\":\"corta\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void iniciaSesionConContrasenaCorrecta() throws Exception {
        AlmacenUsuariosMemoria almacen = new AlmacenUsuariosMemoria();
        var api = api(almacen);
        api.perform(post("/api/usuarios")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"Ana\",\"password\":\"secreto123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isCreated());

        var respuesta = api.perform(post("/api/usuarios/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ANA\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        var json = mapper.readTree(respuesta);
        assertEquals("ana", json.get("nombreUsuario").asText());
        assertEquals("ADMIN", json.get("rol").asText());
        assertFalse(json.has("password"));
        assertFalse(json.has("passwordHash"));
    }

    @Test
    void rechazaContrasenaIncorrecta() throws Exception {
        AlmacenUsuariosMemoria almacen = new AlmacenUsuariosMemoria();
        var api = api(almacen);
        api.perform(post("/api/usuarios")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ana\",\"password\":\"secreto123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isCreated());

        api.perform(post("/api/usuarios/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ana\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaUsuarioInactivo() throws Exception {
        AlmacenUsuariosMemoria almacen = new AlmacenUsuariosMemoria();
        var usuario = new UsuarioService(almacen).registrar(
                new UsuarioRequest("ana", "secreto123", Rol.VENDEDOR));
        Usuario guardado = almacen.findByNombreUsuario(usuario.nombreUsuario()).orElseThrow();
        guardado.desactivar();

        api(almacen).perform(post("/api/usuarios/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ana\",\"password\":\"secreto123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administradorPuedeListarDesactivarYCambiarRol() throws Exception {
        AlmacenUsuariosMemoria almacen = new AlmacenUsuariosMemoria();
        var servicio = new UsuarioService(almacen);
        UsuarioRespuesta registrado = servicio.registrar(new UsuarioRequest("ana", "secreto123", Rol.VENDEDOR));
        var api = api(almacen);

        api.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
        api.perform(patch("/api/usuarios/{id}/estado", registrado.id())
                        .contentType(APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isOk());
        api.perform(patch("/api/usuarios/{id}/rol", registrado.id())
                        .contentType(APPLICATION_JSON).content("{\"rol\":\"ADMIN\"}"))
                .andExpect(status().isOk());

        Usuario actualizado = almacen.findById(registrado.id()).orElseThrow();
        assertFalse(actualizado.isActivo());
        assertEquals(Rol.ADMIN, actualizado.getRol());
    }
}
