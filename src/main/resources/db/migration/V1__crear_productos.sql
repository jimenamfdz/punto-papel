CREATE TABLE productos (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL CHECK (length(trim(nombre)) > 0),
    precio DOUBLE PRECISION NOT NULL CHECK (precio > 0 AND precio < 'Infinity'::double precision),
    existencias INTEGER NOT NULL CHECK (existencias >= 0)
);
