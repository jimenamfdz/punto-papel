package com.puntoypapel.inventario;

import java.util.List;

public interface AlmacenVentas {
    Venta save(Venta venta);
    List<Venta> findAll();
}
