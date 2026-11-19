-- Migración inicial multi-restaurante para PostgreSQL.
-- Ejecutar una sola vez sobre la base de datos después de realizar un respaldo.
-- No elimina ni modifica el contenido de los pedidos/productos existentes.

CREATE TABLE IF NOT EXISTS restaurantes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,
    administrador_id BIGINT NULL REFERENCES usuarios(id),
    logo_url VARCHAR(1000),
    color_primario VARCHAR(20) NOT NULL DEFAULT '#D6AD55',
    color_secundario VARCHAR(20) NOT NULL DEFAULT '#101010',
    color_fondo VARCHAR(20) NOT NULL DEFAULT '#101010',
    imagen_inicio_url VARCHAR(1000),
    titulo_inicio VARCHAR(180),
    descripcion_inicio TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE categorias ADD COLUMN IF NOT EXISTS restaurante_id BIGINT;
ALTER TABLE productos ADD COLUMN IF NOT EXISTS restaurante_id BIGINT;
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS restaurante_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_categorias_restaurante') THEN
        ALTER TABLE categorias ADD CONSTRAINT fk_categorias_restaurante
            FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_productos_restaurante') THEN
        ALTER TABLE productos ADD CONSTRAINT fk_productos_restaurante
            FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_pedidos_restaurante') THEN
        ALTER TABLE pedidos ADD CONSTRAINT fk_pedidos_restaurante
            FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id);
    END IF;
END $$;

-- Conserva el negocio actual como primer tenant.
INSERT INTO restaurantes (
    nombre, slug, administrador_id, titulo_inicio, descripcion_inicio
)
SELECT
    'Sabor & Fuego',
    'sabor-y-fuego',
    (SELECT id FROM usuarios WHERE LOWER(correo) = 'jdptls10@gmail.com' LIMIT 1),
    'Bienvenido a Sabor & Fuego',
    'Conoce nuestro menú y realiza tu pedido.'
WHERE NOT EXISTS (
    SELECT 1 FROM restaurantes WHERE slug = 'sabor-y-fuego'
);

-- Asigna todos los registros históricos al restaurante existente.
UPDATE categorias
SET restaurante_id = (SELECT id FROM restaurantes WHERE slug = 'sabor-y-fuego')
WHERE restaurante_id IS NULL;

UPDATE productos
SET restaurante_id = (SELECT id FROM restaurantes WHERE slug = 'sabor-y-fuego')
WHERE restaurante_id IS NULL;

UPDATE pedidos
SET restaurante_id = (SELECT id FROM restaurantes WHERE slug = 'sabor-y-fuego')
WHERE restaurante_id IS NULL;

-- Verificación: estos tres conteos de filas sin restaurante deben ser cero.
SELECT 'categorias' AS tabla, COUNT(*) AS sin_restaurante
FROM categorias WHERE restaurante_id IS NULL
UNION ALL
SELECT 'productos', COUNT(*) FROM productos WHERE restaurante_id IS NULL
UNION ALL
SELECT 'pedidos', COUNT(*) FROM pedidos WHERE restaurante_id IS NULL;
