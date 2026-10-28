package com.puntoypapel.inventario;

import java.util.Optional;

public interface AlmacenUsuarios {
    Usuario save(Usuario usuario);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}
