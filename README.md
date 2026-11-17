[readme_md.md](https://github.com/user-attachments/files/34953546/readme_md.md)
# Microservicio de Usuarios (`ms-users`)

Servicio REST desarrollado con Spring Boot para gestionar el registro, inicio de sesión y validación de roles de usuarios dentro de la plataforma.

## Tecnologías

* **Java 17** & **Spring Boot 3.2.0**
* **Spring Data JPA**
* **MySQL 8.0**
* **Docker** & **Docker Compose**

## Cómo ejecutar el proyecto

### 1. Crear la red compartida
Como este servicio se comunica con el microservicio de mascotas, primero debes asegurarte de que la red externa exista en Docker:

```bash
docker network create nombre_red_ms_pets
```

### 2. Levantar los contenedores
Desde la raíz del proyecto, ejecuta:

```bash
docker-compose up --build -d
```

* **API disponible en:** `http://localhost:8082`
* **Base de datos MySQL:** Puerto `3307` (`users_db`)

## Endpoints Principales

**Ruta base:** `/api/users`

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/register` | Registra un nuevo usuario (asigna rol `CLIENT` por defecto). |
| `POST` | `/login` | Valida credenciales y retorna los datos del usuario. |
| `GET` | `/{id}/exists` | Verifica si un usuario existe por su ID (uso interno `ms-pets`). |
| `GET` | `/{id}/is-client` | Comprueba si el usuario tiene rol de cliente (uso interno `ms-pets`). |

### Ejemplo de Registro (`POST /api/users/register`)

```json
{
  "name": "Ana Gómez",
  "email": "ana@correo.com",
  "password": "password123"
}
```

### Ejemplo de Login (`POST /api/users/login`)

```json
{
  "email": "ana@correo.com",
  "password": "password123"
}
```
