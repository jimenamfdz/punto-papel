package com.puntoypapel.inventario;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = {ProductoController.class, VentaController.class})
public class ProductoErrorHandler {

    public record ErrorRespuesta(String mensaje) {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta(error.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido() {
        return ResponseEntity.badRequest().body(new ErrorRespuesta(
                "Envía un objeto JSON válido con nombre, precio y existencias del tipo correcto."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> idInvalido() {
        return ResponseEntity.badRequest().body(new ErrorRespuesta(
                "El ID del producto debe tener formato UUID."));
    }
}
