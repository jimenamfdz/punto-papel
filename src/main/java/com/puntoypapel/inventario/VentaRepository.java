package com.puntoypapel.inventario;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, UUID> {
}
