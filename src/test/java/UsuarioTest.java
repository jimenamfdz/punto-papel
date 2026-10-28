package com.puntoypapel.inventario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioTest {
    @Test
    void creaUsuarioActivoConRolYFecha() {
        Usuario usuario = new Usuario("ana", "$2a$hash-no-es-una-contrasena", Rol.ADMIN);

        assertTrue(usuario.isActivo());
        assertEquals("ana", usuario.getNombreUsuario());
        assertEquals(Rol.ADMIN, usuario.getRol());
        assertEquals("$2a$hash-no-es-una-contrasena", usuario.getPasswordHash());
        assertTrue(usuario.getCreadoEn() != null);
    }

    @Test
    void puedeDesactivarYActivarUsuario() {
        Usuario usuario = new Usuario("vendedor", "hash", Rol.VENDEDOR);

        usuario.desactivar();
        assertTrue(!usuario.isActivo());
        usuario.activar();
        assertTrue(usuario.isActivo());
    }

    @Test
    void rechazaDatosDeUsuarioInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Usuario("", "hash", Rol.ADMIN));
        assertThrows(IllegalArgumentException.class, () -> new Usuario("nombre con espacios", "hash", Rol.ADMIN));
        assertThrows(IllegalArgumentException.class, () -> new Usuario("ana", "", Rol.ADMIN));
        assertThrows(IllegalArgumentException.class, () -> new Usuario("ana", "hash", null));
    }
}
