ALTER TABLE productos
    ADD COLUMN categoria VARCHAR(100) NOT NULL DEFAULT ''
        CHECK (length(categoria) <= 100);
