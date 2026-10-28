package com.puntoypapel.inventario;

public record LoginRequest(String nombreUsuario, String password) {
    public void validar() {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
    }
}
