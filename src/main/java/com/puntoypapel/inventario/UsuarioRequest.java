package com.puntoypapel.inventario;

public record UsuarioRequest(String nombreUsuario, String password, Rol rol) {
    public void validar() {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }
    }
}
