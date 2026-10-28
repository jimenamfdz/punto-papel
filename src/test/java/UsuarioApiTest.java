package com.puntoypapel.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
