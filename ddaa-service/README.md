# DDAA Service

## 1. Propósito del microservicio

`ddaa-service` contiene la lógica principal de negocio de DDAA Platform. Su responsabilidad es administrar derechos de aprovechamiento de aguas, consultar catálogos, entregar detalles asociados y publicar eventos cuando se crean, actualizan o eliminan derechos.

Este servicio representa el núcleo del dominio de derechos de agua.

## 2. Rol dentro de la arquitectura

`ddaa-service` se encarga de:

- listar derechos DDAA;
- consultar detalle de un DDAA;
- crear nuevos registros;
- actualizar registros existentes;
- eliminar registros;
- consultar catálogos usados por formularios;
- acceder a SQL Server mediante JPA, JDBC y consultas específicas;
- aplicar caché con Redis para consultas relevantes;
- publicar eventos de dominio mediante RabbitMQ.

El frontend no consume este servicio directamente en el flujo normal. La comunicación pasa por `api-gateway` y por la capa BFF.

## 3. Tecnologías principales

- Java 17.
- Spring Boot.
- Spring Web MVC.
- Spring Data JPA.
- Spring JDBC.
- SQL Server.
- H2 para pruebas.
- Eureka Client.
- Springdoc OpenAPI / Swagger.
- Spring Security Resource Server.
- Redis Cache.
- RabbitMQ / Spring AMQP.
- JUnit 5.
- AssertJ.

## 4. Puerto local

```text
http://localhost:8082
```

## 5. Endpoints principales

### Derechos DDAA

```text
GET    /api/ddaa
GET    /api/ddaa/{id}
GET    /api/ddaa/{id}/expedientes
POST   /api/ddaa
PUT    /api/ddaa/{id}
DELETE /api/ddaa/{id}
```

### Catálogos

```text
GET /api/catalogos/comunas
GET /api/catalogos/ruts
GET /api/catalogos/instalaciones
GET /api/catalogos/cuencas
GET /api/catalogos/subcuencas
GET /api/catalogos/fuentes
```

## 6. Funcionamiento general

1. `api-gateway` recibe la solicitud del frontend.
2. El gateway valida la sesión con `auth-service`.
3. Si la sesión es válida, el gateway reenvía la solicitud a `ddaa-service`.
4. `ddaa-service` ejecuta la lógica de negocio correspondiente.
5. El servicio consulta SQL Server usando repositorios JPA o consultas JDBC.
6. Algunas consultas pueden quedar en caché Redis.
7. Si hay una operación de escritura, se invalida la caché relacionada.
8. En operaciones relevantes se publica un evento DDAA hacia RabbitMQ.

## 7. Configuración local

Variables relevantes:

```properties
DDAA_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ddaa;encrypt=true;trustServerCertificate=true
DDAA_DB_USER=ddaa_user
DDAA_DB_PASSWORD=tu_password
DDAA_DB_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver
DDAA_SQL_INIT_MODE=never
DDAA_JPA_DDL_AUTO=none
DDAA_SAMPLE_DATA_ENABLED=false
DDAA_REDIS_HOST=localhost
DDAA_REDIS_PORT=6379
DDAA_CACHE_TTL=10m
DDAA_RABBITMQ_HOST=localhost
DDAA_RABBITMQ_PORT=5672
DDAA_RABBITMQ_USERNAME=ddaa
DDAA_RABBITMQ_PASSWORD=ddaa
JWT_SECRET=clave_local_larga_para_desarrollo
EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka/
```

Para trabajar contra una base real existente, se recomienda:

```properties
DDAA_JPA_DDL_AUTO=none
DDAA_SAMPLE_DATA_ENABLED=false
```

Esto evita que Hibernate intente modificar tablas reales o cargar datos de ejemplo.

## 8. Caché con Redis

El servicio usa Redis como proveedor de caché. La configuración define:

- tipo de caché: Redis;
- TTL configurable con `DDAA_CACHE_TTL`;
- prefijo de claves `ddaa-service::`;
- no almacenar valores nulos.

La finalidad es reducir consultas repetidas sobre datos relativamente estables, como catálogos y consultas frecuentes.

## 9. Eventos con RabbitMQ

`ddaa-service` publica eventos cuando ocurren cambios relevantes sobre un derecho de agua.

Tipos de evento:

```text
CREATED
UPDATED
DELETED
```

Los eventos incluyen datos como identificador del DDAA, comuna, titular, fuente, estado y fecha de ocurrencia. La publicación después del commit evita emitir eventos de cambios que luego pudieran revertirse en base de datos.

## 10. Swagger / OpenAPI

Con el servicio levantado:

```text
http://localhost:8082/swagger-ui.html
http://localhost:8082/v3/api-docs
```

Swagger permite revisar y probar los contratos REST del microservicio.

## 11. Ejecución local

Desde la carpeta del servicio:

```powershell
cd ddaa-service
mvn spring-boot:run
```

Desde la raíz del proyecto:

```powershell
mvn -f ddaa-service\pom.xml spring-boot:run
```

Se recomienda levantar antes:

1. `eureka-server`
2. SQL Server
3. Redis, si se probará caché
4. RabbitMQ, si se probarán eventos

## 12. Pruebas automatizadas

### `DdaaCrudIntegrationTest`

Prueba el ciclo principal del dominio:

1. prepara datos referenciales mínimos;
2. crea un DDAA;
3. consulta el registro creado;
4. actualiza el registro;
5. elimina el registro.

La prueba usa H2 para evitar depender de SQL Server real durante la ejecución automatizada.

### `DdaaServiceApplicationTests`

Valida que el contexto de Spring Boot cargue correctamente.

## 13. Ejecutar pruebas

```powershell
mvn -f ddaa-service\pom.xml test
```

## 14. Observaciones

- Este servicio concentra la lógica del dominio DDAA.
- Las consultas complejas pueden depender de nombres reales de columnas en SQL Server.
- Redis mejora rendimiento en consultas repetidas.
- RabbitMQ desacopla el dominio respecto de futuras acciones como notificaciones o auditoría.
