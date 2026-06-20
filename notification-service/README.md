# Notification Service

## 1. Propósito del microservicio

`notification-service` es el microservicio encargado de consumir eventos del dominio DDAA y transformarlos en notificaciones. Su objetivo es desacoplar las acciones de comunicación del microservicio de negocio.

En vez de que `ddaa-service` envíe correos directamente, publica eventos en RabbitMQ. Luego `notification-service` los consume y ejecuta la acción de notificación correspondiente.

## 2. Rol dentro de la arquitectura

Este servicio cumple el rol de consumidor asíncrono de eventos.

Flujo esperado:

1. Se crea, actualiza o elimina un DDAA en `ddaa-service`.
2. `ddaa-service` publica un evento en RabbitMQ.
3. `notification-service` escucha la cola configurada.
4. El listener recibe el evento.
5. El servicio construye y envía una notificación por correo.

## 3. Tecnologías principales

- Java 17.
- Spring Boot.
- Spring Web MVC.
- Spring AMQP / RabbitMQ.
- Spring Mail.
- Eureka Client.
- Spring Actuator.
- JUnit 5.

## 4. Puerto local

```text
http://localhost:8083
```

Este puerto evita conflicto con:

- `api-gateway`: 8080;
- `auth-service`: 8081;
- `ddaa-service`: 8082.

## 5. Componentes principales

### Configuración RabbitMQ

La clase de configuración define exchange, cola y bindings usados para recibir eventos DDAA.

### Eventos

El paquete `event` contiene:

- `DdaaEvent`: estructura del mensaje recibido;
- `DdaaEventType`: tipo de evento, por ejemplo creación, actualización o eliminación;
- `DdaaEventListener`: consumidor que escucha eventos desde RabbitMQ.

### Servicio de correo

El paquete `service` contiene la lógica encargada de preparar y enviar notificaciones por email.

## 6. Configuración local

Variables relevantes:

```properties
DDAA_RABBITMQ_HOST=localhost
DDAA_RABBITMQ_PORT=5672
DDAA_RABBITMQ_USERNAME=ddaa
DDAA_RABBITMQ_PASSWORD=ddaa
DDAA_MAIL_HOST=smtp.gmail.com
DDAA_MAIL_PORT=587
DDAA_MAIL_USERNAME=correo_pruebas@gmail.com
DDAA_MAIL_PASSWORD=app_password
DDAA_MAIL_FROM=correo_pruebas@gmail.com
DDAA_MAIL_TO=destinatario_prueba@dominio.cl
EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka/
```

No se deben subir contraseñas SMTP ni credenciales reales al repositorio.

## 7. Funcionamiento general

1. El servicio se registra en Eureka.
2. Se conecta a RabbitMQ usando las variables configuradas.
3. Declara o usa la cola de eventos DDAA.
4. Escucha mensajes generados por `ddaa-service`.
5. Cuando llega un evento, genera una notificación legible.
6. Envía el correo usando la configuración SMTP.

## 8. Ejecución local

Desde la carpeta del servicio:

```powershell
cd notification-service
.\mvnw.cmd spring-boot:run
```

Desde la raíz del proyecto:

```powershell
mvn -f notification-service\pom.xml spring-boot:run
```

Se recomienda levantar antes:

1. `eureka-server`
2. RabbitMQ
3. `ddaa-service`
4. `notification-service`

## 9. Pruebas automatizadas

### `NotificationServiceApplicationTests`

Valida que el contexto de Spring Boot cargue correctamente.

Este test confirma que la configuración base del microservicio es válida, aunque no reemplaza una prueba de integración completa contra RabbitMQ o SMTP.

## 10. Ejecutar pruebas

```powershell
mvn -f notification-service\pom.xml test
```

## 11. Observaciones

- Este servicio permite separar eventos de negocio y acciones de notificación.
- RabbitMQ evita acoplar directamente `ddaa-service` con envío de correos.
- Para pruebas reales de email conviene usar una cuenta dedicada y credenciales de aplicación.
- En una etapa futura puede ampliarse para auditoría, plantillas HTML o múltiples destinatarios.
