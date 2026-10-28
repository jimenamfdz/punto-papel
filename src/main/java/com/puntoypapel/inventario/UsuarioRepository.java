package com.puntoypapel.inventario;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>, AlmacenUsuarios {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}
