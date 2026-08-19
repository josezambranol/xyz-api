# API de Gestión de Camiones — Empresa XYZ

API REST segura desarrollada con **Spring Boot 3.3.5**, **Spring Security 6**, **Spring Data JPA** y base de datos **H2**.

Este repositorio contiene dos implementaciones de seguridad desacopladas y organizadas en ramas independientes para fines de portafolio y evaluación técnica:

1. **Rama `main`**: Autenticación Básica (**HTTP Basic Auth**).
2. **Rama `feature/jwt-auth`**: Autenticación basada en Tokens (**JWT — JSON Web Tokens** con JJWT).

---

## Integrantes del Proyecto

- **Jose Daniel Zambrano Luna**
- **Rafael Eduardo Sarmiento Peña**

---

## 🔀 Estructura de Ramas y Cómo Alternar

Ambas ramas coexisten en el repositorio remoto de GitHub de forma independiente:

```bash
# Cambiar a la versión con Autenticación Básica
git checkout main

# Cambiar a la versión con Autenticación JWT
git checkout feature/jwt-auth
```

---

## 📋 Matriz de Roles y Permisos

Los endpoints de negocio `/api/**` requieren autenticación y aplican control de acceso basado en roles (**RBAC**):

| Operación | Método HTTP | Endpoint | Rol Requerido |
|---|---|---|---|
| **Crear Camión** | `POST` | `/api/camiones` | `ADMIN` |
| **Crear Conductor** | `POST` | `/api/conductores` | `ADMIN` |
| **Listar Camiones** | `GET` | `/api/camiones` | `ADMIN` o `SUPERVISOR` |
| **Listar Conductores** | `GET` | `/api/conductores` | `ADMIN` o `SUPERVISOR` |
| **Asociar Conductor a Camión** | `PUT` | `/api/camiones/{camionId}/conductor/{conductorId}` | `SUPERVISOR` |

### Usuarios y Credenciales Semilla (H2)

Al iniciar la aplicación, se crean automáticamente los siguientes usuarios encriptados con **BCrypt**:

| Usuario | Contraseña | Rol | Descripción |
|---|---|---|---|
| `admin` | `Admin123!` | `ROLE_ADMIN` | Acceso a creación y consulta |
| `supervisor` | `Supervisor123!` | `ROLE_SUPERVISOR` | Acceso a consulta y asignación de conductor |

---

## 1️⃣ Guía Paso a Paso: Autenticación Básica (Rama `main`)

### ¿Cómo funciona en el código?
- **[`SecurityConfig.java`](src/main/java/com/xyz/gestioncamiones/config/security/SecurityConfig.java)**: Configura `.httpBasic(Customizer.withDefaults())` y gestiona la sesión como `STATELESS`.
- **[`UsuarioDetailsService.java`](src/main/java/com/xyz/gestioncamiones/config/security/UsuarioDetailsService.java)**: Implementa `UserDetailsService` cargando el usuario y su rol desde la base de datos H2.
- Las credenciales se envían en cada solicitud HTTP codificadas en Base64 dentro del header `Authorization: Basic <base64(usuario:contraseña)>`.

### Pruebas Paso a Paso en Postman:

1. **Configuración de Autorización en Postman**:
   - En cualquier petición (`GET`, `POST`, `PUT`), ve a la pestaña **Authorization**.
   - Selecciona **Type**: `Basic Auth`.
   - **Username**: `admin` (o `supervisor`).
   - **Password**: `Admin123!` (o `Supervisor123!`).

2. **Crear un camión (`POST /api/camiones`)**:
   - **Auth**: `Basic Auth` con usuario `admin`.
   - **Headers**: `Content-Type: application/json`
   - **Body (raw JSON)**:
     ```json
     {
       "placa": "XYZ123",
       "tipoVehiculo": "Tractocamión"
     }
     ```
   - **Respuesta esperada**: `201 Created`.

3. **Listar camiones (`GET /api/camiones`)**:
   - **Auth**: `Basic Auth` con usuario `supervisor` o `admin`.
   - **Respuesta esperada**: `200 OK` con la lista de camiones.

4. **Verificación de seguridad**:
   - Petición sin credenciales $\rightarrow$ `401 Unauthorized`.
   - Petición `POST /api/camiones` autenticado como `supervisor` $\rightarrow$ `403 Forbidden` (Solo `ADMIN` tiene permisos).

---

## 2️⃣ Guía Paso a Paso: Autenticación JWT (Rama `feature/jwt-auth`)

### ¿Cómo funciona en el código?
La arquitectura JWT se compone de los siguientes módulos:

1. **Dependencias JJWT ([`pom.xml`](pom.xml))**:
   - `io.jsonwebtoken:jjwt-api:0.12.6`
   - `io.jsonwebtoken:jjwt-impl:0.12.6`
   - `io.jsonwebtoken:jjwt-jackson:0.12.6`
2. **Utilidad [`JwtUtil.java`](src/main/java/com/xyz/gestioncamiones/config/security/JwtUtil.java)**:
   - Genera tokens firmados criptográficamente mediante algoritmo HMAC-SHA256 con fecha de emisión y expiración (24h).
   - Incluye claims personalizados (roles del usuario).
   - Extrae el username y valida la integridad y vigencia del token.
3. **Filtro [`JwtFilter.java`](src/main/java/com/xyz/gestioncamiones/config/security/JwtFilter.java)**:
   - Hereda de `OncePerRequestFilter`.
   - Intercepta solicitudes HTTP, extrae el token del header `Authorization: Bearer <token>`, valida el token y almacena la autenticación en el `SecurityContextHolder`.
4. **Configuración [`SecurityConfig.java`](src/main/java/com/xyz/gestioncamiones/config/security/SecurityConfig.java)**:
   - Registra el `JwtFilter` antes del `UsernamePasswordAuthenticationFilter`.
   - Define acceso público (`.permitAll()`) únicamente para la ruta de autenticación `/auth/**`.
   - Protege todas las demás rutas bajo autenticación JWT y RBAC.
5. **Controlador [`AuthController.java`](src/main/java/com/xyz/gestioncamiones/controller/AuthController.java)**:
   - Expone `POST /auth/login` para autenticar credenciales vía `AuthenticationManager` y devolver el token JWT generado.

---

### Pruebas Paso a Paso en Postman:

#### Paso 1: Iniciar Sesión para Obtener el Token JWT

- **Método**: `POST`
- **URL**: `http://localhost:8080/auth/login`
- **Headers**:
  - `Content-Type`: `application/json`
- **Body (raw JSON)**:
  ```json
  {
    "username": "admin",
    "password": "Admin123!"
  }
  ```
- **Respuesta (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzg4MzEzMTUxLCJleHAiOjE3ODgzOTk1NTF9...",
    "tipo": "Bearer",
    "username": "admin",
    "rol": "ROLE_ADMIN"
  }
  ```
> Copia el valor del campo `token`.

---

#### Paso 2: Consumir Endpoints Protegidos con el Token

En cualquier solicitud posterior:
- Ve a la pestaña **Authorization**.
- Selecciona **Type**: `Bearer Token`.
- Pega en el campo **Token** el JWT obtenido en el Paso 1.

*(O manualmente en la pestaña Headers: `Authorization: Bearer eyJhbGci...`)*

##### Ejemplo 1: Listar Camiones (`GET /api/camiones`)
- **Headers**: `Authorization: Bearer <token_admin_o_supervisor>`
- **Respuesta (200 OK)**:
  ```json
  [
    {
      "id": 1,
      "placa": "XYZ888",
      "tipoVehiculo": "Tractomula",
      "conductor": null
    }
  ]
  ```

##### Ejemplo 2: Crear Camión (`POST /api/camiones`)
- **Headers**:
  - `Authorization: Bearer <token_admin>`
  - `Content-Type: application/json`
- **Body (raw JSON)**:
  ```json
  {
    "placa": "COL456",
    "tipoVehiculo": "Camión Rígido"
  }
  ```
- **Respuesta (201 Created)**: Retorna el camión recién creado.

##### Ejemplo 3: Prueba de Roles y Rechazo (403 Forbidden)
- Realiza un `POST /api/camiones` usando el token del usuario `supervisor`.
- **Respuesta (403 Forbidden)**:
  ```json
  {
    "status": 403,
    "error": "Forbidden",
    "mensaje": "No tiene permisos para esta operación"
  }
  ```

##### Ejemplo 4: Prueba sin Token o con Token Expirado (401 Unauthorized)
- Realiza cualquier petición a `/api/camiones` sin enviar el header `Authorization`.
- **Respuesta (401 Unauthorized)**:
  ```json
  {
    "status": 401,
    "error": "Unauthorized",
    "mensaje": "Token JWT no proporcionado o inválido"
  }
  ```

---

## 🛠️ Requisitos y Comandos de Ejecución

- **Java JDK**: 21
- **Apache Maven**: 3.9+

### Compilación y Ejecución:

```bash
# Compilar y empaquetar
mvn clean package

# Ejecutar tests automatizados
mvn test

# Iniciar la aplicación
mvn spring-boot:run
```

La aplicación quedará disponible en `http://localhost:8080`.
La base de datos persistente H2 se almacena en el directorio `./data/xyzdb`.
