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

## Autoría

Proyecto personal desarrollado paso a paso como parte de un portafolio profesional.
