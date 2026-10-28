package com.puntoypapel.inventario;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("El usuario o la contraseña no son válidos.");
    }
}
