package com.puntoypapel.inventario;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 80)
    private String nombreUsuario;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private Instant creadoEn;

    protected Usuario() {
        // Requerido por JPA para recuperar usuarios.
    }

    public Usuario(String nombreUsuario, String passwordHash, Rol rol) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (nombreUsuario.length() > 80 || nombreUsuario.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("El nombre de usuario no es válido.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("La contraseña debe almacenarse como hash.");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = true;
        this.creadoEn = Instant.now();
    }

    @PrePersist
    void asignarValoresIniciales() {
        if (creadoEn == null) creadoEn = Instant.now();
    }

    public UUID getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public Instant getCreadoEn() { return creadoEn; }

    public void desactivar() { activo = false; }
    public void activar() { activo = true; }
    public void cambiarRol(Rol nuevoRol) {
        if (nuevoRol == null) throw new IllegalArgumentException("El rol es obligatorio.");
        rol = nuevoRol;
    }
}
