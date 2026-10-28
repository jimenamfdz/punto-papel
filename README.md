# Punto & Papel

Sistema de inventario y ventas para una papelería ficticia.

## Estado actual

Proyecto en desarrollo. Actualmente incluye:

- Registro y búsqueda de productos.
- Entradas y salidas de inventario.
- Registro de ventas y cálculo de totales.
- API REST con registro, consulta y actualización de productos.
- Persistencia de productos en PostgreSQL y migraciones versionadas con Flyway.
- Pruebas unitarias y pruebas de controladores con JUnit.

## Tecnologías

- Java 21
- Maven
- Spring Boot 3.4.5
- JUnit 5
- Spring MVC
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway

Los productos y las ventas ya se guardan en PostgreSQL mediante transacciones.

## Ejecutar las pruebas

```bash
mvn test
```

## Endpoints actuales

- `GET /api/saludo`
- `GET /api/productos`
- `POST /api/productos`
- `GET /api/productos/{id}` — consulta un producto por su identificador UUID.
- `PUT /api/productos/{id}` — actualiza nombre, precio y existencias conservando el ID.
- `POST /api/ventas/productos/{productoId}` — registra una venta y descuenta existencias.
- `GET /api/ventas` — consulta el historial de ventas, con filtros opcionales.
- `GET /api/ventas/resumen` — devuelve totales y desglose agrupado por producto.

### Registrar y consultar un producto por ID

Inicia la aplicación con la configuración de PostgreSQL indicada abajo y registra un producto:

```bash
curl -X POST http://localhost:8080/api/productos \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Cuaderno","precio":20.0,"existencias":10}'
```

La respuesta incluye un `id` generado automáticamente, además de `nombre`, `precio`
y `existencias`. El listado de productos también incluye este identificador.
Dos productos pueden tener el mismo nombre y tendrán IDs diferentes.

Copia el `id` de la respuesta y úsalo en la consulta:

```bash
curl -i http://localhost:8080/api/productos/REEMPLAZA_CON_EL_ID
```

La consulta devuelve `200 OK` y el producto si existe, `404 Not Found` si el UUID
no está registrado y `400 Bad Request` si el identificador tiene un formato inválido.

Los productos y sus IDs se conservan en PostgreSQL al reiniciar la aplicación.
La actualización se guarda dentro de una transacción.

### Actualizar un producto

Usa el ID de un producto ya registrado y envía los tres campos:

```bash
curl -i -X PUT http://localhost:8080/api/productos/REEMPLAZA_CON_EL_ID \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Cuaderno profesional","precio":45.5,"existencias":15}'
```

La respuesta es `200 OK` con el producto actualizado y su mismo `id`. El valor de
`existencias` reemplaza el total actual; no se suma al inventario. La consulta por
ID y el listado reflejan los nuevos datos. No se crea un producto adicional.

Si el ID no existe, devuelve `404 Not Found`. Un ID mal formado o datos inválidos
devuelven `400 Bad Request`. Si la actualización se rechaza, el producto conserva
todos sus valores anteriores.

### Registrar una venta

Envía el ID del producto y la cantidad que se vende:

```bash
curl -i -X POST http://localhost:8080/api/ventas/productos/REEMPLAZA_CON_EL_ID \
  -H 'Content-Type: application/json' \
  -d '{"cantidad":2}'
```

La respuesta es `201 Created` e incluye el ID de la venta, el producto, el precio
unitario, la cantidad, el total y la fecha. La cantidad se descuenta del inventario
dentro de la misma transacción que guarda la venta. La venta conserva el nombre y
precio vigentes en el momento de la operación.

Una cantidad cero, negativa, ausente o mayor que las existencias devuelve `400` y
no modifica el inventario ni crea una venta. Si el producto no existe, devuelve
`404`. El historial completo se consulta con `GET /api/ventas`.

Puedes filtrar el historial por producto y por fechas inclusivas:

```bash
curl 'http://localhost:8080/api/ventas?productoId=REEMPLAZA_CON_EL_ID&desde=2026-10-01&hasta=2026-10-31'
```

`productoId` usa un UUID. `desde` y `hasta` usan el formato `YYYY-MM-DD` y ambos
días se incluyen en la búsqueda. Los resultados se ordenan del más reciente al
más antiguo. Un rango invertido o una fecha con formato inválido devuelve `400`.

Para obtener el resumen general usa:

```bash
curl 'http://localhost:8080/api/ventas/resumen'
```

La respuesta contiene `totalVentas`, `unidadesVendidas`, `importeTotal` y
`porProducto`. Cada elemento de `porProducto` incluye el producto, sus unidades,
el importe acumulado y el número de operaciones. El resumen acepta los mismos
parámetros opcionales `productoId`, `desde` y `hasta` del historial.

### Validación del registro y la actualización de productos

Tanto `POST /api/productos` como `PUT /api/productos/{id}` requieren los tres campos:
`nombre`, `precio` y `existencias`.
El nombre no puede estar vacío ni contener solo espacios, el precio debe ser un
número finito mayor que cero y las existencias no pueden ser negativas. Las
existencias en cero son válidas. Los campos omitidos o con valor `null` se rechazan.

Los datos inválidos devuelven `400 Bad Request` con un mensaje en JSON y no agregan
ningún producto al inventario. Por ejemplo, al enviar un precio de cero:

```json
{"mensaje":"El precio debe ser mayor que cero."}
```

Si se omiten las existencias, la respuesta es:

```json
{"mensaje":"Las existencias son obligatorias."}
```

Un cuerpo vacío, JSON mal formado o un tipo de dato incompatible también devuelve
`400` con un mensaje que solicita un objeto JSON válido. Un ID con formato inválido
devuelve `400` con el mensaje `El ID del producto debe tener formato UUID.`

## Configurar PostgreSQL local

Se requieren Java 21 o posterior, Maven, Bash y PostgreSQL 17. En macOS con Homebrew:

```bash
brew install postgresql@17
bash scripts/postgres-local.sh start
```

El script crea una instancia exclusiva del proyecto en `.local/postgres`, escucha
solo en `localhost:5432` y usa contraseñas SCRAM para conexiones TCP. La conexión
administrativa por socket usa la identidad del usuario del sistema. Si ya tienes
otro servidor en ese puerto, detén esa instancia o usa tu instalación existente;
no ejecutes ambas en el mismo puerto. Para otra ruta de instalación, define `PG_BIN`.

En una instalación nueva, crea un usuario de aplicación sin privilegios de
administración y dos bases. Entra a PostgreSQL con:

```bash
/opt/homebrew/opt/postgresql@17/bin/psql -h /tmp -d postgres
```

Dentro de `psql`, ejecuta lo siguiente. `\password` pide la contraseña de forma
interactiva; no la escribas en los comandos SQL ni la publiques:

```sql
CREATE ROLE punto_papel LOGIN;
\password punto_papel
CREATE DATABASE punto_papel OWNER punto_papel;
CREATE DATABASE punto_papel_test OWNER punto_papel;
\q
```

Solo en una instalación nueva, copia `.env.example` a `.env` y configura la misma
contraseña. Si `.env` ya existe, conserva su configuración:

```bash
cp .env.example .env
chmod 600 .env
```

`.env` y `.local/` están excluidos de Git. No guardes credenciales en
`application.properties`. Las variables disponibles son:

| Variable | Uso |
| --- | --- |
| `DB_URL` | Conexión PostgreSQL; por defecto `jdbc:postgresql://localhost:5432/punto_papel` |
| `DB_USERNAME` | Usuario de aplicación; por defecto `punto_papel` |
| `DB_PASSWORD` | Contraseña obligatoria, sin valor predeterminado |
| `DB_INTEGRATION_TEST_URL` | Base exclusiva de pruebas: `jdbc:postgresql://localhost:5432/punto_papel_test` |

## Ejecutar la aplicación

Desde la carpeta del proyecto, con `.env` configurado:

```bash
bash scripts/postgres-local.sh start
set -a
source .env
set +a
mvn spring-boot:run
```

Después abre [http://localhost:8080](http://localhost:8080) para usar la interfaz
de inventario. Esta primera pantalla permite listar, registrar y editar productos;
los cambios se envían a la misma API REST y se guardan en PostgreSQL.

La interfaz se sirve desde `src/main/resources/static` y no requiere un servidor
frontend separado.

## Usuarios y roles

La base de datos ya incluye la tabla `usuarios`, con nombre de usuario único,
contraseña almacenada como hash, estado activo y uno de estos roles: `ADMIN` o
`VENDEDOR`.

Para registrar un usuario envía una contraseña de al menos ocho caracteres a
`POST /api/usuarios`:

```bash
curl -i -X POST http://localhost:8080/api/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"nombreUsuario":"ana","password":"secreto123","rol":"VENDEDOR"}'
```

La respuesta devuelve el identificador, nombre normalizado, rol, estado y fecha
de creación, pero nunca la contraseña ni su hash. Las contraseñas se almacenan
con BCrypt y los nombres de usuario duplicados (sin distinguir mayúsculas) se
rechazan.

El inicio de sesión se realiza con `POST /api/usuarios/login`:

```bash
curl -i -X POST http://localhost:8080/api/usuarios/login \
  -H 'Content-Type: application/json' \
  -d '{"nombreUsuario":"ana","password":"secreto123"}'
```

Las credenciales incorrectas y los usuarios inactivos reciben `401 Unauthorized`
con el mismo mensaje, sin revelar si el nombre de usuario existe.

Nunca se guardarán contraseñas en texto plano ni dentro del repositorio.

Spring Boot recibe las variables del entorno; no carga `.env` por sí solo.
Al iniciar, Flyway aplica `V1__crear_productos.sql` una sola vez. Hibernate valida
que el esquema corresponda a las entidades, sin borrar ni recrear tablas.
Las siguientes modificaciones del esquema se agregarán como nuevas migraciones.

Para detener la aplicación usa `Ctrl+C`. Para detener la instancia local:

```bash
bash scripts/postgres-local.sh stop
```

Los datos quedan guardados en `.local/postgres`; conserva esa carpeta al reiniciar.

## Prueba de persistencia con PostgreSQL

`mvn test` ejecuta las 66 pruebas unitarias sin requerir PostgreSQL. La prueba de
integración se omite si no se proporciona una URL de pruebas. Para ejecutar las
67 pruebas, inicia PostgreSQL y carga las variables:

```bash
bash scripts/postgres-local.sh start
set -a
source .env
set +a
mvn test
```

`PersistenciaPostgresTest` solo se activa cuando `DB_INTEGRATION_TEST_URL` apunta a
una base llamada `punto_papel_test`. Usa HTTP real y reinicia la aplicación dos
veces para comprobar el registro, la consulta y la actualización conservando el
UUID. También verifica que una actualización inválida no modifica los datos.
Elimina únicamente el producto que creó y no utiliza la base `punto_papel`.

Referencias de configuración: [inicialización de bases de datos en Spring Boot](https://docs.spring.io/spring-boot/3.4/how-to/data-initialization.html)
y [autenticación de PostgreSQL](https://www.postgresql.org/docs/17/auth-password.html).

## Autoría

Proyecto personal desarrollado paso a paso como parte de un portafolio profesional.
