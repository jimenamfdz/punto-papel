package com.puntoypapel.inventario;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, UUID>, AlmacenProductos {
}
