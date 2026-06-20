# Frontend

## 1. Propósito del módulo

`frontend` es la aplicación web de DDAA Platform. Está construida con React y Vite, y permite que el usuario interactúe con el sistema desde el navegador.

Su responsabilidad principal es entregar una interfaz simple para:

- iniciar sesión con Google;
- consultar la sesión activa del usuario;
- listar derechos de aprovechamiento de aguas;
- crear nuevos registros DDAA;
- editar registros existentes;
- eliminar registros;
- consultar el detalle de un derecho de agua seleccionado.

Aunque no es un microservicio backend, cumple un rol central dentro de la plataforma porque es el punto de entrada visual para el usuario final.

## 2. Rol dentro de la arquitectura

El frontend no se comunica directamente con cada microservicio interno. En desarrollo, Vite redirige las rutas principales hacia `api-gateway`, que corre en `http://localhost:8080`.

Esto permite que React consuma endpoints como:

```text
/bff/session
/bff/ddaa
/bff/ddaa/{id}
/bff/ddaa/form-options
/oauth2/authorization/google
/logout
```

El gateway se encarga de enrutar esas solicitudes hacia los servicios correspondientes.

Flujo general:

```text
Usuario
  ↓
Frontend React + Vite
  ↓
API Gateway / BFF
  ↓
Auth Service / DDAA Service
```

## 3. Tecnologías principales

El módulo usa:

- **React** para construir la interfaz de usuario.
- **Vite** como servidor de desarrollo y herramienta de build.
- **ESLint** para revisión estática de código.
- **Vitest** para pruebas unitarias.
- **React Testing Library** para pruebas de componentes.

## 4. Estructura principal

Estructura resumida del módulo:

```text
frontend/
├── public/
├── src/
│   ├── assets/
│   ├── pages/
│   │   ├── HomePage.jsx
│   │   ├── HomePage.test.jsx
│   │   ├── LoginPage.jsx
│   │   └── LoginPage.test.jsx
│   ├── services/
│   │   ├── authService.js
│   │   ├── authService.test.js
│   │   ├── ddaaService.js
│   │   └── ddaaService.test.js
│   ├── App.jsx
│   ├── main.jsx
│   ├── App.css
│   └── index.css
├── package.json
└── vite.config.js
```

## 5. Funcionamiento general

### 5.1. Carga inicial de la aplicación

`App.jsx` consulta la sesión activa mediante `fetchCurrentUser()`.

Si el usuario está autenticado, se muestra `HomePage`. Si no existe sesión válida, se muestra `LoginPage`.

```text
App.jsx
  ├── consulta /bff/session
  ├── si hay sesión: muestra HomePage
  └── si no hay sesión: muestra LoginPage
```

### 5.2. Login con Google

`LoginPage` muestra una pantalla de acceso y delega el inicio de sesión al backend mediante la ruta:

```text
/oauth2/authorization/google
```

Esa ruta es atendida por el gateway y el servicio de autenticación.

### 5.3. Manejo de sesión y JWT

`authService.js` consulta `/bff/session`. Si el backend entrega un `accessToken`, el frontend lo guarda en `localStorage` con la clave:

```text
ddaa_access_token
```

Luego `ddaaService.js` recupera ese token y lo agrega en el header `Authorization` para las llamadas protegidas.

### 5.4. Gestión de DDAA

`HomePage.jsx` contiene la interfaz principal de gestión.

Desde esta pantalla se puede:

- cargar el listado inicial de DDAA;
- consultar opciones de formulario;
- seleccionar un registro y ver su detalle;
- crear un nuevo derecho;
- editar un derecho existente;
- eliminar un derecho previa confirmación.

El frontend consume principalmente rutas BFF, no rutas internas del microservicio `ddaa-service`.

## 6. Configuración de proxy en desarrollo

El archivo `vite.config.js` configura el servidor de desarrollo en el puerto `5173`.

También define proxy hacia `api-gateway` en `http://localhost:8080` para rutas como:

```text
/auth
/api
/bff
oauth2
login
logout
```

Esto permite trabajar localmente con React en un puerto y el backend en otro sin cambiar las rutas usadas por la aplicación.

## 7. Requisitos para ejecutar

Antes de iniciar el frontend deben estar disponibles, al menos:

- `api-gateway` en el puerto `8080`;
- `auth-service` para autenticación;
- `ddaa-service` para gestión de derechos;
- `eureka-server` si se ejecuta la arquitectura completa con descubrimiento de servicios.

También se requiere tener instalado Node.js y npm.

## 8. Ejecución local

Desde el directorio `frontend`:

```bash
npm install
npm run dev
```

La aplicación queda disponible normalmente en:

```text
http://localhost:5173
```

## 9. Build de producción

Para generar una versión compilada:

```bash
npm run build
```

El resultado queda en la carpeta:

```text
dist/
```

Para previsualizar el build:

```bash
npm run preview
```

## 10. Lint

Para ejecutar revisión estática:

```bash
npm run lint
```

Este comando ayuda a detectar problemas de estilo, imports no usados y errores comunes en componentes React.

## 11. Pruebas implementadas

El frontend contiene pruebas unitarias para servicios y componentes.

### 11.1. Pruebas de servicios

Ubicación:

```text
src/services/authService.test.js
src/services/ddaaService.test.js
```

Estas pruebas validan:

- consulta de sesión activa mediante `/bff/session`;
- redirección al login de Google;
- cierre de sesión mediante `/logout`;
- consumo del listado DDAA;
- consulta de detalle por ID;
- consulta de opciones de formulario;
- creación mediante `POST`;
- actualización mediante `PUT`;
- eliminación mediante `DELETE`;
- manejo de respuestas `204 No Content`;
- propagación de errores enviados por el backend.

### 11.2. Pruebas de componentes

Ubicación:

```text
src/pages/LoginPage.test.jsx
src/pages/HomePage.test.jsx
```

Estas pruebas validan:

- renderizado de la pantalla de login;
- ejecución de la acción de login con Google;
- carga inicial del listado DDAA;
- carga de catálogos para formulario;
- carga de detalle del registro seleccionado;
- cierre de sesión desde la pantalla principal;
- creación de registros;
- edición de registros;
- eliminación con confirmación;
- cancelación de eliminación;
- visualización de errores de carga;
- bloqueo del formulario cuando faltan catálogos obligatorios.

## 12. Ejecución de pruebas

Las pruebas están escritas con Vitest y React Testing Library.

El comando esperado para ejecutarlas es:

```bash
npm test
```

Si el script todavía no aparece en `package.json`, se debe agregar en la sección `scripts`:

```json
"test": "vitest run",
"test:watch": "vitest"
```

Y asegurar las dependencias de testing:

```bash
npm install -D vitest jsdom @testing-library/react @testing-library/jest-dom @testing-library/user-event
```

Para usar `toBeInTheDocument()` y otros matchers de Testing Library, se recomienda mantener un archivo de configuración de pruebas, por ejemplo:

```text
src/test/setup.js
```

con:

```js
import '@testing-library/jest-dom/vitest';
```

## 13. Relación con otros módulos

| Módulo | Relación con frontend |
| --- | --- |
| `api-gateway` | Punto de entrada HTTP y BFF consumido por React. |
| `auth-service` | Gestiona login Google, sesión y JWT. |
| `ddaa-service` | Entrega datos de derechos de agua y catálogos. |
| `eureka-server` | Permite descubrimiento entre servicios backend. |
| `notification-service` | No es consumido directamente por React; opera de forma asíncrona desde eventos RabbitMQ. |

## 14. Resumen técnico

`frontend` entrega la capa visual de DDAA Platform. Su diseño mantiene al navegador desacoplado de los microservicios internos, usando `api-gateway` como intermediario.

La aplicación ya contempla autenticación, almacenamiento local del JWT, consumo de rutas BFF, operaciones CRUD sobre derechos de agua y pruebas unitarias orientadas a servicios y componentes principales.
