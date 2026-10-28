ALTER TABLE productos
    ADD COLUMN stock_minimo INTEGER NOT NULL DEFAULT 0
        CHECK (stock_minimo >= 0);
