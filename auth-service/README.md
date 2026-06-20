# Auth Service

## 1. Propósito del microservicio

`auth-service` administra la autenticación de usuarios en DDAA Platform. Su función principal es integrar login con Google OAuth2/OpenID Connect, validar el dominio permitido y mantener datos internos del usuario autenticado.

Este microservicio separa la seguridad y la identidad del resto del dominio de negocio. Así, `ddaa-service` puede enfocarse en derechos de agua y no en el flujo de login.

## 2. Rol dentro de la arquitectura

`auth-service` cumple las siguientes responsabilidades:

- iniciar sesión mediante Google;
- validar que el usuario pertenezca al dominio corporativo permitido;
- consultar la sesión activa mediante `/auth/me`;
- persistir usuarios internos en base de datos;
- generar token JWT para comunicar identidad y rol;
- exponer endpoints de apoyo para pruebas y administración básica.

El gateway consulta este servicio para decidir si una petición protegida puede continuar.

## 3. Tecnologías principales

- Java 17.
- Spring Boot.
- Spring Security.
- OAuth2 Client.
- Spring Data JPA.
- SQL Server.
- Eureka Client.
- Springdoc OpenAPI / Swagger.
- JJWT.
- JUnit 5.
- Mockito.
- AssertJ.

## 4. Puerto local

```text
http://localhost:8081
```

## 5. Endpoints principales

```text
GET  /auth/test
GET  /auth/login
GET  /auth/error
GET  /auth/me
POST /auth/users/test
GET  /auth/users
GET  /oauth2/authorization/google
GET  /logout
```

El flujo OAuth completo se prueba desde navegador porque requiere redirecciones, cookies y callback de Google.

## 6. Funcionamiento general

1. El usuario inicia sesión desde el frontend o gateway.
2. Spring Security redirige a Google mediante OAuth2.
3. Google responde con los datos básicos del usuario.
4. El servicio valida el dominio permitido.
5. Si el usuario es válido, se consulta o registra su información interna.
6. El endpoint `/auth/me` retorna el estado de sesión y datos útiles para el frontend.
7. El gateway usa `/auth/me` para proteger rutas internas.

## 7. Configuración local

El servicio lee configuración desde `local.properties` o variables de entorno.

Variables relevantes:

```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ddaa_auth;encrypt=true;trustServerCertificate=true
DB_USER=ddaa_user
DB_PASSWORD=tu_password
GOOGLE_CLIENT_ID=tu_google_client_id
GOOGLE_CLIENT_SECRET=tu_google_client_secret
ALLOWED_GOOGLE_DOMAIN=camanchaca.cl
FRONTEND_SUCCESS_URL=http://localhost:5173/
JWT_SECRET=clave_local_larga_para_desarrollo
JWT_EXPIRATION_MINUTES=60
EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka/
```

No se deben versionar credenciales reales.

## 8. Swagger / OpenAPI

Con el servicio levantado:

```text
http://localhost:8081/swagger-ui.html
http://localhost:8081/v3/api-docs
```

Swagger documenta los endpoints REST propios del servicio. El flujo real con Google se valida principalmente desde navegador.

## 9. Ejecución local

Desde la carpeta del servicio:

```powershell
cd auth-service
.\mvnw.cmd spring-boot:run
```

Desde la raíz del proyecto:

```powershell
mvn -f auth-service\pom.xml spring-boot:run
```

Se recomienda levantar antes `eureka-server`.

## 10. Pruebas automatizadas

### `AuthControllerTest`

Valida el contrato básico del controller sin levantar todo Spring Security. Cubre:

- respuesta de `/auth/test`;
- mensaje informativo de login;
- respuesta controlada de error;
- sesión anónima cuando no hay principal autenticado;
- sesión autenticada con datos de Google;
- sesión autenticada con datos internos persistidos;
- generación de token simulado mediante `JwtService`;
- creación de usuario de prueba;
- listado de usuarios.

Estas pruebas usan mocks de `UserService`, `JwtService` y `OAuth2User`.

## 11. Ejecutar pruebas

```powershell
mvn -f auth-service\pom.xml test
```

## 12. Observaciones

- `auth-service` es la fuente de verdad para identidad de usuario.
- El dominio permitido se configura externamente.
- El gateway depende de `/auth/me` para proteger `/api/**` y `/bff/ddaa/**`.
- La base de autenticación está separada de la base de datos del dominio DDAA.
