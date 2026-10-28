package com.puntoypapel.inventario;

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

    boolean coincideContrasena(String password, Usuario usuario) {
        return passwordEncoder.matches(password, usuario.getPasswordHash());
    }
}
