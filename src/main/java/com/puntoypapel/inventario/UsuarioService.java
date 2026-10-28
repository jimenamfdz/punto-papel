package com.puntoypapel.inventario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final AlmacenUsuarios usuarios;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(AlmacenUsuarios usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional
    public UsuarioRespuesta registrar(UsuarioRequest solicitud) {
        solicitud.validar();
        String nombreNormalizado = solicitud.nombreUsuario().trim().toLowerCase();
        if (usuarios.findByNombreUsuario(nombreNormalizado).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado.");
        }
        Usuario usuario = new Usuario(nombreNormalizado,
                passwordEncoder.encode(solicitud.password()), solicitud.rol());
        return UsuarioRespuesta.desde(usuarios.save(usuario));
    }

    public UsuarioRespuesta iniciarSesion(LoginRequest solicitud) {
        solicitud.validar();
        String nombreNormalizado = solicitud.nombreUsuario().trim().toLowerCase();
        Usuario usuario = usuarios.findByNombreUsuario(nombreNormalizado)
                .filter(Usuario::isActivo)
                .filter(encontrado -> coincideContrasena(solicitud.password(), encontrado))
                .orElseThrow(CredencialesInvalidasException::new);
        return UsuarioRespuesta.desde(usuario);
    }

    public List<UsuarioRespuesta> listar() {
        return usuarios.findAll().stream().map(UsuarioRespuesta::desde).toList();
    }

    @Transactional
    public Optional<UsuarioRespuesta> cambiarEstado(UUID id, boolean activo) {
        return usuarios.findById(id).map(usuario -> {
            if (activo) usuario.activar(); else usuario.desactivar();
            return UsuarioRespuesta.desde(usuarios.save(usuario));
        });
    }

    @Transactional
    public Optional<UsuarioRespuesta> cambiarRol(UUID id, Rol rol) {
        if (rol == null) throw new IllegalArgumentException("El rol es obligatorio.");
        return usuarios.findById(id).map(usuario -> {
            usuario.cambiarRol(rol);
            return UsuarioRespuesta.desde(usuarios.save(usuario));
        });
    }

    boolean coincideContrasena(String password, Usuario usuario) {
        return passwordEncoder.matches(password, usuario.getPasswordHash());
    }
}
