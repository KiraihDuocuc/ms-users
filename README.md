[readme_md.md](https://github.com/user-attachments/files/34953374/readme_md.md)
# Microservicio de Usuarios (`ms-users`)

Microservicio encargado de la gestión de usuarios, autenticación (registro e inicio de sesión) y validación de roles para el ecosistema de la aplicación (comunicándose con el frontend en React y el microservicio de mascotas `ms-pets`).

## 🚀 Tecnologías Utilizadas

* **Java 17**
* **Spring Boot 3.2.0** (Spring Web, Spring Data JPA)
* **MySQL 8.0**
* **Lombok**
* **Docker & Docker Compose**
* **Maven**

---

## 📋 Requisitos Previos

* [Docker](https://www.docker.com/) y [Docker Compose](https://docs.docker.com/compose/) instalados.
* Red externa de Docker creada (compartida con el microservicio de mascotas):
  ```bash
  docker network create nombre_red_ms_pets
  ```
* *(Opcional para ejecución local sin Docker)* JDK 17 y MySQL 8.0 corriendo localmente.

---

## ⚙️ Variables de Entorno

El proyecto utiliza las siguientes variables de entorno configuradas en el `docker-compose.yml`:

| Variable | Descripción | Valor por defecto (Docker) |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | URL de conexión JDBC a la base de datos MySQL | `jdbc:mysql://ms-users-db:3306/users_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `rootpassword` |

---

## 🐳 Despliegue con Docker Compose

1. Clona este repositorio y navega hasta la carpeta raíz del proyecto:
   ```bash
   git clone https://github.com/KiraihDuocuc/ms-users.git
   cd ms-users
   ```

2. Asegúrate de que la red externa exista (si aún no has levantado `ms-pets`):
   ```bash
   docker network create nombre_red_ms_pets
   ```

3. Construye y levanta los contenedores (aplicación + base de datos):
   ```bash
   docker-compose up --build -d
   ```

4. El servicio estará disponible en: `http://localhost:8082`
   * **Base de datos MySQL (acceso externo):** `localhost:3307`

---

## 🔌 Endpoints de la API

**Base URL:** `http://localhost:8082/api/users`

### 1. Endpoints para el Frontend

#### Registrar un nuevo usuario
* **Método:** `POST`
* **Ruta:** `/register`
* **Body (JSON):**
  ```json
  {
    "name": "Juan Pérez",
    "email": "juan@ejemplo.com",
    "password": "miPassword123",
    "role": "CLIENT"
  }
  ```
  *(Nota: Si no se envía el campo `role`, se asigna `"CLIENT"` por defecto).*
* **Respuestas:**
  * `200 OK`: Retorna el objeto del usuario creado (omitiendo la contraseña).
  * `400 Bad Request`: Si el correo ya está registrado o los datos son inválidos.

#### Iniciar sesión (Login)
* **Método:** `POST`
* **Ruta:** `/login`
* **Body (JSON):**
  ```json
  {
    "email": "juan@ejemplo.com",
    "password": "miPassword123"
  }
  ```
* **Respuestas:**
  * `200 OK`: Retorna los datos del usuario autenticado (`id`, `name`, `email`, `role`).
  * `401 Unauthorized`: `"Credenciales inválidas"`

---

### 2. Endpoints de Comunicación Interna (para `ms-pets`)

#### Verificar si un usuario existe
* **Método:** `GET`
* **Ruta:** `/{id}/exists`
* **Respuesta (`200 OK`):** `true` o `false` (Boolean)

#### Verificar si un usuario tiene rol de Cliente
* **Método:** `GET`
* **Ruta:** `/{id}/is-client`
* **Respuesta (`200 OK`):** `true` o `false` (Boolean)

---

## 📁 Estructura del Proyecto

```text
src/
└── main/
    ├── java/
    │   └── microservicio/
    │       └── user/
    │           ├── controller/
    │           │   └── UserController.java    # Controlador REST
    │           ├── model/
    │           │   └── User.java              # Entidad JPA (Tabla users)
    │           ├── repository/
    │           │   └── UserRepository.java    # Acceso a datos con Spring Data
    │           ├── service/
    │           │   └── UserService.java       # Lógica de negocio y validaciones
    │           └── UserApplication.java       # Clase principal de Spring Boot
    └── resources/
        └── application.properties             # Configuración de puerto y JPA
```
