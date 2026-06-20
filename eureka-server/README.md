# Eureka Server

## 1. Propósito del microservicio

`eureka-server` es el servidor de descubrimiento de servicios de DDAA Platform. Su objetivo es permitir que los microservicios se registren y puedan encontrarse usando nombres lógicos en vez de URLs fijas.

Este servicio no contiene lógica de negocio. Es una pieza de infraestructura para que la arquitectura de microservicios funcione de forma desacoplada.

## 2. Rol dentro de la arquitectura

Eureka permite que servicios como `api-gateway`, `auth-service`, `ddaa-service` y `notification-service` se registren automáticamente.

Gracias a esto, el gateway puede enrutar solicitudes usando direcciones como:

```text
lb://auth-service
lb://ddaa-service
```

En vez de depender directamente de `localhost:8081` o `localhost:8082`.

## 3. Tecnologías principales

- Java 17.
- Spring Boot.
- Spring Cloud Netflix Eureka Server.
- Spring Actuator.
- JUnit 5.

## 4. Puerto local

```text
http://localhost:8761
```

La consola web de Eureka queda disponible en:

```text
http://localhost:8761
```

## 5. Funcionamiento general

1. Se levanta `eureka-server`.
2. Cada microservicio cliente se inicia y se registra en Eureka.
3. Eureka mantiene un registro de servicios activos.
4. El gateway y otros clientes pueden resolver servicios por nombre lógico.
5. Si un servicio cambia de puerto o ubicación en otro ambiente, la arquitectura no necesita cambiar rutas internas manualmente.

## 6. Configuración principal

El servidor Eureka no se registra a sí mismo como cliente:

```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

Esto es esperado porque este módulo actúa como servidor de registro.

## 7. Ejecución local

Desde la carpeta del servicio:

```powershell
cd eureka-server
.\mvnw.cmd spring-boot:run
```

Desde la raíz del proyecto:

```powershell
mvn -f eureka-server\pom.xml spring-boot:run
```

Este debe ser el primer servicio levantado en desarrollo local.

## 8. Validación manual

Después de levantar los demás microservicios, ingresar a:

```text
http://localhost:8761
```

Ahí deberían aparecer registrados servicios como:

```text
API-GATEWAY
AUTH-SERVICE
DDAA-SERVICE
NOTIFICATION-SERVICE
```

## 9. Pruebas automatizadas

### `EurekaServerApplicationTests`

Valida que el contexto de Spring Boot cargue correctamente con la configuración del servidor Eureka.

## 10. Ejecutar pruebas

```powershell
mvn -f eureka-server\pom.xml test
```

## 11. Observaciones

- Eureka es infraestructura, no dominio.
- Debe levantarse antes que los servicios clientes.
- Facilita el patrón Service Discovery dentro del proyecto.
