# Punto & Papel

Sistema de inventario y ventas para una papelería ficticia.

## Estado actual

Proyecto en desarrollo. Actualmente incluye:

- Registro y búsqueda de productos.
- Entradas y salidas de inventario.
- Registro de ventas y cálculo de totales.
- API REST inicial con Spring Boot.
- Pruebas unitarias y pruebas de controladores con JUnit.

## Tecnologías

- Java 21
- Maven
- Spring Boot 3.4.5
- JUnit 5
- Spring MVC

Todavía no se ha conectado una base de datos. PostgreSQL y JPA/Hibernate forman parte de las siguientes etapas.

## Ejecutar las pruebas

```bash
mvn test
```

## Endpoints actuales

- `GET /api/saludo`
- `GET /api/productos`
- `POST /api/productos`
- `GET /api/productos/{id}` — consulta un producto por su identificador UUID.

### Registrar y consultar un producto por ID

Inicia la aplicación con `mvn spring-boot:run` y registra un producto:

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

Los productos y sus IDs se conservan únicamente en memoria durante la ejecución.
Al reiniciar la aplicación el inventario queda vacío.

### Validación del registro de productos

`POST /api/productos` requiere los tres campos: `nombre`, `precio` y `existencias`.
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

## Autoría

Proyecto personal desarrollado paso a paso como parte de un portafolio profesional.
