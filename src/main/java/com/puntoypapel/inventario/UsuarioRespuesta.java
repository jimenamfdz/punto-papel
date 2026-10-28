package com.puntoypapel.inventario;

import java.time.Instant;
import java.util.UUID;

public record UsuarioRespuesta(
        UUID id, String nombreUsuario, Rol rol, boolean activo, Instant creadoEn) {
    public static UsuarioRespuesta desde(Usuario usuario) {
        return new UsuarioRespuesta(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol(),
                usuario.isActivo(), usuario.getCreadoEn());
    }
}
