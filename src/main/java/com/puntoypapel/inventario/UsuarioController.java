package com.puntoypapel.inventario;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<UsuarioRespuesta> registrar(@RequestBody UsuarioRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(solicitud));
    }

    @PostMapping("/login")
    public UsuarioRespuesta iniciarSesion(@RequestBody LoginRequest solicitud) {
        return servicio.iniciarSesion(solicitud);
    }

    @GetMapping
    public List<UsuarioRespuesta> listar() {
        return servicio.listar();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioRespuesta> cambiarEstado(
            @PathVariable("id") UUID id, @RequestBody EstadoUsuarioRequest solicitud) {
        return ResponseEntity.of(servicio.cambiarEstado(id, solicitud.activo()));
    }

    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioRespuesta> cambiarRol(
            @PathVariable("id") UUID id, @RequestBody RolUsuarioRequest solicitud) {
        return ResponseEntity.of(servicio.cambiarRol(id, solicitud.rol()));
    }
}
