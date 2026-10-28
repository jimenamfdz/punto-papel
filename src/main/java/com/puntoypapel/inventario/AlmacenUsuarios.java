package com.puntoypapel.inventario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlmacenUsuarios {
    Usuario save(Usuario usuario);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findById(UUID id);
    List<Usuario> findAll();
}
