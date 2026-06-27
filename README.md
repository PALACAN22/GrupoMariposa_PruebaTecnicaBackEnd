# ----- Biblioteca - Prueba Técnica Backend -----

Sistema de gestión de biblioteca con dos servicios independientes que se comunican por HTTP.

## ----- Cómo levantar Y Correr el proyecto -----

Primero copiar los archivos de configuración:

```bash
cp service-a-library/.env.example service-a-library/.env
cp service-b-loans/.env.example service-b-loans/.env
```

Luego levantar todo:

-Si es la primera vez en ejecución:

```bash
docker compose up --build
```
-Si no es la primera vez en ejecución:

```bash
docker compose up 
```

La primera vez tarda unos minutos porque descarga las imágenes y compila el proyecto Java. Una vez que está corriendo:

- Servicio A (Java): http://localhost:8080
- Servicio B (Go): http://localhost:8081
- Swagger UI: http://localhost:8080/swagger-ui.html

Usuario seed disponible: `admin@biblioteca.com` / `Admin123!`

Para parar sin perder datos: `docker compose down`  
Para resetear todo incluyendo las bases de datos: `docker compose down -v`




## ----- Arquitectura -----

        Cliente (Postman)
                │
  ┌──────────────────────────┐
  │   SERVICIO A (Java)      │   :8080
  │   - Login / usuarios     │
  │   - Libros (CRUD)        │
  │   - Orquesta préstamos   │
  └──────────┬───────────────┘
             │ HTTP
             │ (1) Petición: "registra este préstamo"
  ┌──────────────────────────┐
  │   SERVICIO B (Go)        │   :8081
  │   - Préstamos / devol.   │
  └──────────┬───────────────┘
             │ HTTP
             │ (2) Validación: "¿este libro existe y tiene copias libres?"
             │      
  ┌──────────────────────────┐
  │   de vuelta al           │
  │   SERVICIO A             │
  └──────────────────────────┘



Cada servicio tiene su propia base de datos PostgreSQL. Se realizó esto así para futuras expansiones u otras APIs.

- **Servicio A**: maneja libros, usuarios y autenticación (JWT). Es el único punto de entrada para el cliente.
- **Servicio B**: maneja únicamente préstamos. Antes de registrar uno, le consulta al Servicio A si el libro existe y tiene copias disponibles.




## ----- Flujo completo: login → consultar libro → préstamo -----

**1. Login**

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@biblioteca.com",
  "password": "Admin123!"
}
```

La respuesta incluye un `token` JWT que hay que usar en los siguientes requests.

**2. Consultar libros disponibles**

```
GET http://localhost:8080/api/books?available=true
Authorization: Bearer <token>
```

También se puede filtrar por autor o género: `?author=Borges` o `?genre=Ficcion`.

**3. Crear un préstamo**

```
POST http://localhost:8080/api/loans
Authorization: Bearer <token>
Content-Type: application/json

{
  "bookId": 1
}
```

Lo que pasa internamente: Servicio A recibe el request → llama a Servicio B con userId + bookId → Servicio B verifica disponibilidad con Servicio A → si está OK registra el préstamo → Servicio A descuenta una copia del libro.

**4. Ver mis préstamos activos**

```
GET http://localhost:8080/api/loans/me/active
Authorization: Bearer <token>
```

**5. Devolver un libro**

```
PATCH http://localhost:8080/api/loans/1/return
Authorization: Bearer <token>
```



## ----- Decisiones técnicas Aplicadas -----

### Servicio A - Java / Spring Boot - Con mejor dominio que NodeJS

**Spring Boot 3.2 con JPA/Hibernate**: el stack que más conozco para Java. Spring Security hace el manejo de roles bastante directo, JPA/Hibernate con poca manipulación pero conociendo la lógica de sus librerías.

**Flyway para migraciones**: Se obtó por tener el esquema bajo control de versiones. Con `ddl-auto: validate`, Hibernate solo verifica que las entidades coincidan con la BD, no la modifica.

**RestTemplate con timeouts**: las llamadas a Servicio B tienen timeout de 3s (conexión) y 5s (lectura). Si Servicio B no responde, el request devuelve 503 en lugar de quedar colgado indefinidamente, con ello se evita procesos congelados para el usuario final.

**Orden del flujo de préstamos**: primero Servicio B registra el préstamo y después Servicio A descuenta la copia. Si fuera al revés (primero descontar, después registrar) y Servicio B fallara, la copia quedaría descontada sin que exista ningún préstamo. Así, en el peor caso queda un préstamo registrado con la copia sin descontar, lo cual se puede detectar y corregir con una aplicación de troubleshooting manual.

### Servicio B - Go

**Gin como framework HTTP**: elegí Gin porque tiene binding de JSON y manejo de rutas directo sin ser pesado. La alternativa era `net/http` puro, pero el parsing manual de parámetros hubiera sido más código sin beneficio real para este caso y el poco tiempo de manupulación del lenguaje Go para mi persona.

**database/sql + pgx en lugar de GORM**: Se obtó por no usar un ORM para tener las queries explícitas y entender exactamente qué se está ejecutando en la BD.

**Interfaces definidas en el paquete `service`**: `LoanRepository` y `LibraryClient` están declaradas dentro del paquete que las consume, no donde se implementan. Es el patrón estándar en Go y permite testear la lógica de negocio con fakes sin necesitar base de datos ni el Servicio A corriendo.

**BD separada para Servicio B**: Se tenía la opción de utilizar la misma BD de Servicio A con una tabla separada, pero tener su propia PostgreSQL hace claro que el ownership de los datos de préstamos es de Servicio B. Si algún día se despliega en otro servidor u otro servicio, no depende de nada externo.

**Si Servicio A está caído**: Servicio B rechaza la creación del préstamo con 502. Se aplicó esto como medida de contingencia y así no aceptar el préstamo a ciegas porque sin verificar el libro podría registrarse un préstamo de un libro inexistente.



### Consistencia entre servicios

No hay transacciones distribuidas. Si el paso de descontar copias en Servicio A falla después de que Servicio B ya registró el préstamo, queda una inconsistencia que se logea para reconciliación manual.



## Compilación de los servicios por separado

Se utilizó VSC para el desarrollo de este proyecto:

**Servicio A (Java)**

.\mvnw.cmd clean compile
.\mvnw.cmd clean verify


**Servicio B (Go)** 

go run ./cmd/api
go build ./...


## Correr los tests

**Servicio A (Java)** — requiere Maven o Docker:

```bash
cd service-a-library
docker run --rm -v ${PWD}:/app -w /app maven:3.9-eclipse-temurin-17 mvn test
```

**Servicio B (Go)** — requiere Go o Docker:

```bash
cd service-b-loans
docker run --rm -v ${PWD}:/app -w /app golang:1.21-alpine go test ./... -v
```




## ----- Qué no llegué a hacer -----

- Tests de integración (solo hay unitarios)
- Rate limiting
