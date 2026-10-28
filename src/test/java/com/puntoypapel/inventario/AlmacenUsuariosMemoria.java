package com.puntoypapel.inventario;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

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

    @Override
    public Optional<Usuario> findById(UUID id) {
        return usuarios.values().stream().filter(usuario -> usuario.getId().equals(id)).findFirst();
    }

    @Override
    public List<Usuario> findAll() {
        return List.copyOf(usuarios.values());
    }
}
