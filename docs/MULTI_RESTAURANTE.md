# Plataforma multi-restaurante

## Funcionalidad incorporada

- Cada restaurante tiene un identificador único (slug) y una URL pública: `/restaurante/{slug}`.
- Cada restaurante tiene identidad visual independiente: nombre, logo, colores, imagen de portada, título y descripción.
- El catálogo, las categorías y los pedidos se consultan por `restaurante_id`.
- El carrito de compra se guarda por separado para cada restaurante dentro de la sesión.
- Al crear un pedido, el servidor vuelve a comprobar que cada producto pertenece al restaurante de la URL.
- El historial del cliente se consulta por cliente y restaurante.
- Cada restaurante tiene un panel en `/restaurante-admin/{slug}` para personalización, productos, categorías y estados de pedidos.
- El administrador general puede crear restaurantes y asignar responsables desde `/plataforma/restaurantes`.

## Roles y responsables

1. El administrador general es la cuenta `jdptls10@gmail.com`.
2. El administrador general crea un restaurante. Inicialmente queda como responsable.
3. Para asignar otro responsable, esa persona primero debe crear su cuenta desde el sitio.
4. En `/plataforma/restaurantes`, el administrador general indica el slug del restaurante y el correo ya registrado del responsable.
5. El responsable debe cerrar sesión e iniciar sesión nuevamente para que su sesión recoja el rol actualizado.
6. Los paneles heredados de Sabor & Fuego quedan restringidos al administrador general. Los responsables de otros restaurantes usan su ruta `/restaurante-admin/{slug}`.

## Migración de PostgreSQL

La aplicación usa `spring.jpa.hibernate.ddl-auto=none` y no tiene Flyway configurado. Por tanto, el archivo `src/main/resources/db/migration/V2__multi_restaurante.sql` es una migración SQL manual; no se ejecuta automáticamente.

Antes de desplegar:

1. Realizar un respaldo completo de PostgreSQL.
2. Ejecutar el archivo SQL en la base de datos objetivo.
3. Revisar el resultado final de la consulta de verificación. Los conteos `sin_restaurante` de categorías, productos y pedidos deben ser cero.
4. Comprobar que existe el restaurante `sabor-y-fuego` y que su responsable es `jdptls10@gmail.com`.
5. No crear restaurantes adicionales hasta que la migración haya terminado correctamente.

La migración conserva los registros existentes y los asigna al tenant Sabor & Fuego. Añade claves foráneas, índices y restricciones NOT NULL después de completar la asignación histórica.

## Pruebas previas a producción

- Compilar con Java 21: `mvnw.cmd clean package` en Windows o `./mvnw clean package` en Linux/macOS.
- Iniciar la aplicación contra una copia de la base de datos migrada.
- Abrir `/restaurante/sabor-y-fuego` y confirmar que se muestran los productos existentes.
- Crear un restaurante de prueba y comprobar que su menú comienza vacío.
- Crear productos y categorías en el restaurante de prueba y verificar que no aparecen en Sabor & Fuego.
- Crear un pedido en cada restaurante y verificar que los paneles e historiales muestran solo los pedidos correspondientes.
- Probar que un responsable no puede abrir el panel de otro restaurante ni los paneles globales heredados.
- Cambiar logo, colores, portada y textos; recargar la página pública y comprobar que los cambios persisten.
- Probar login, registro, carrito independiente y cierre de sesión.

## Estado de integración

No fusionar ni desplegar esta rama hasta que la compilación y las pruebas anteriores se hayan ejecutado satisfactoriamente. La migración no se ha ejecutado en producción desde este cambio.
