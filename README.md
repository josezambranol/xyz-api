# API de gestión de camiones — XYZ

API REST con Spring Boot, Spring Security, Spring Data JPA y H2. No existe ningún
endpoint público ni se usa `permitAll()`. La consola web de H2 está deshabilitada.

## Requisitos y ejecución

- Java 21
- Maven 3.9+

```bash
mvn clean package
mvn spring-boot:run
```

La aplicación inicia en `http://localhost:8080`. La base persistente queda en
`./data/xyzdb`. Los usuarios se insertan en H2 durante el primer arranque y sus
contraseñas se almacenan con BCrypt.

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `Admin123!` | `ADMIN` |
| `supervisor` | `Supervisor123!` | `SUPERVISOR` |

Estas credenciales son únicamente para desarrollo académico. Deben sustituirse en
un despliegue real.

## Permisos

| Operación | Método y ruta | Rol |
|---|---|---|
| Crear camión | `POST /api/camiones` | ADMIN |
| Crear conductor | `POST /api/conductores` | ADMIN |
| Listar camiones | `GET /api/camiones` | ADMIN o SUPERVISOR |
| Listar conductores | `GET /api/conductores` | ADMIN o SUPERVISOR |
| Asociar conductor | `PUT /api/camiones/{camionId}/conductor/{conductorId}` | SUPERVISOR |

Sin autenticación se responde `401 Unauthorized`. Un usuario autenticado que no
tenga el rol requerido recibe `403 Forbidden`.

## Pruebas en Postman

En la pestaña **Authorization**, seleccionar **Basic Auth** y usar las credenciales
de la tabla anterior. En las operaciones POST agregar `Content-Type: application/json`.

Crear camión como `admin`:

```http
POST /api/camiones
```

```json
{
  "placa": "ABC123",
  "tipoVehiculo": "Tractocamión"
}
```

Crear conductor como `admin`:

```http
POST /api/conductores
```

```json
{
  "nombre": "Laura Gómez"
}
```

Asociar como `supervisor` (no requiere body):

```http
PUT /api/camiones/1/conductor/1
```

Consultar como cualquiera de los dos usuarios:

```http
GET /api/camiones
GET /api/conductores
```

Respuesta de un camión asociado:

```json
{
  "id": 1,
  "placa": "ABC123",
  "tipoVehiculo": "Tractocamión",
  "conductor": {
    "id": 1,
    "nombre": "Laura Gómez"
  }
}
```
