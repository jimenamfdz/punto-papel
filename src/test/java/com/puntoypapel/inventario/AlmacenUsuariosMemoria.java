package com.puntoypapel.inventario;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class AlmacenUsuariosMemoria implements AlmacenUsuarios {
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();

    @Override
    public Usuario save(Usuario usuario) {
        usuarios.put(usuario.getNombreUsuario(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
        return Optional.ofNullable(usuarios.get(nombreUsuario));
    }
}
