CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nombre_usuario VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMIN', 'VENDEDOR')),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_usuarios_nombre_usuario ON usuarios(nombre_usuario);
