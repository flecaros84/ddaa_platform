# API Gateway

## 1. Propósito del microservicio

`api-gateway` es el punto único de entrada al backend de DDAA Platform. Su responsabilidad es recibir las peticiones del frontend, enrutar solicitudes hacia los microservicios internos y entregar una capa BFF (*Backend for Frontend*) para simplificar el consumo desde React.

Este servicio no contiene la lógica principal del negocio DDAA ni administra usuarios directamente. Su función es coordinar el acceso, proteger rutas y adaptar respuestas para el frontend.

## 2. Rol dentro de la arquitectura

Dentro de la arquitectura de microservicios, este módulo cumple tres funciones principales:

1. **Gateway:** centraliza el acceso HTTP al sistema.
2. **Seguridad perimetral:** valida sesión antes de permitir acceso a rutas protegidas.
3. **BFF:** entrega endpoints pensados para el frontend, evitando que React tenga que conocer todos los endpoints internos.

El gateway se registra en Eureka y se comunica con otros servicios usando nombres lógicos, por ejemplo:

```text
http://auth-service
http://ddaa-service
```

## 3. Tecnologías principales

- Java 17.
- Spring Boot.
- Spring Cloud Gateway WebFlux.
- Spring Cloud Netflix Eureka Client.
- WebClient.
- JUnit 5.
- Mockito.
- AssertJ.

## 4. Puerto local

```text
http://localhost:8080
```

## 5. Rutas principales

### Rutas hacia `auth-service`

```text
/auth/**
/oauth2/**
/login/**
/logout
```

Estas rutas permiten iniciar sesión con Google, consultar sesión y cerrar sesión.

### Rutas hacia `ddaa-service`

```text
/api/**
```

Estas rutas exponen el microservicio de dominio a través del gateway.

### Rutas BFF

```text
GET    /bff/session
GET    /bff/ddaa
GET    /bff/ddaa/form-options
GET    /bff/ddaa/{id}
POST   /bff/ddaa
PUT    /bff/ddaa/{id}
DELETE /bff/ddaa/{id}
```

El endpoint `/bff/ddaa/form-options` agrupa catálogos usados por el formulario DDAA, como comunas, titulares, instalaciones, cuencas, subcuencas y fuentes.

## 6. Funcionamiento general

1. El frontend realiza una petición al gateway.
2. El filtro de seguridad revisa si la ruta requiere autenticación.
3. Si la ruta está protegida, el gateway consulta `/auth/me` en `auth-service`.
4. Si la sesión es válida, la petición continúa.
5. El gateway reenvía la solicitud al servicio interno correspondiente.
6. En las rutas BFF, el gateway adapta la respuesta para que sea más cómoda para el frontend.

## 7. Seguridad aplicada

El componente `AuthenticatedApiFilter` protege:

```text
/api/**
/bff/ddaa/**
```

El filtro reenvía headers de sesión, como `Cookie` y `Authorization`, hacia `auth-service`. Si la sesión no es válida o `auth-service` falla, responde `401 Unauthorized`.

## 8. Ejecución local

Desde la carpeta del servicio:

```powershell
cd api-gateway
.\mvnw.cmd spring-boot:run
```

Desde la raíz del proyecto:

```powershell
mvn -f api-gateway\pom.xml spring-boot:run
```

Se recomienda levantar antes:

1. `eureka-server`
2. `auth-service`
3. `ddaa-service`
4. `api-gateway`

## 9. Pruebas automatizadas

Este microservicio tiene pruebas unitarias enfocadas en BFF y seguridad.

### `BffControllerTest`

Valida que el controller delegue correctamente al service en los endpoints:

- sesión;
- listado DDAA;
- opciones de formulario;
- detalle;
- creación;
- actualización;
- eliminación.

### `BffServiceTest`

Valida la lógica del BFF sin levantar servicios reales. Usa `WebClient` simulado mediante `ExchangeFunction` para probar:

- consulta de sesión a `auth-service`;
- respuesta anónima si falla autenticación;
- proxy hacia `ddaa-service`;
- agregación de catálogos;
- conservación de códigos HTTP;
- manejo de errores desde backend.

### `AuthenticatedApiFilterTest`

Valida la seguridad del gateway:

- rutas públicas no consultan `auth-service`;
- rutas protegidas continúan si la sesión está autenticada;
- rutas protegidas responden `401` si la sesión es anónima;
- errores de `auth-service` producen rechazo seguro;
- el filtro se ejecuta con alta precedencia.

## 10. Ejecutar pruebas

```powershell
mvn -f api-gateway\pom.xml test
```

## 11. Observaciones

- Este servicio no debe contener lógica de negocio DDAA.
- Las rutas internas se resuelven por nombre lógico mediante Eureka.
- La capa BFF ayuda a desacoplar el frontend de la estructura interna de microservicios.
