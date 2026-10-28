CREATE TABLE ventas (
    id UUID PRIMARY KEY,
    producto_id UUID NOT NULL REFERENCES productos(id),
    nombre_producto VARCHAR(255) NOT NULL,
    precio_unitario DOUBLE PRECISION NOT NULL CHECK (precio_unitario > 0),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    total DOUBLE PRECISION NOT NULL CHECK (total > 0),
    fecha TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ventas_producto_id ON ventas(producto_id);
CREATE INDEX idx_ventas_fecha ON ventas(fecha);
