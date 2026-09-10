<pre>

██████╗ ██████╗  ██████╗ ██████╗ ██╗   ██╗ ██████╗████████╗     ██████╗ █████╗ ████████╗ █████╗ ██╗      ██████╗  ██████╗ ██╗   ██╗███████╗
██╔══██╗██╔══██╗██╔═══██╗██╔══██╗██║   ██║██╔════╝╚══██╔══╝    ██╔════╝██╔══██╗╚══██╔══╝██╔══██╗██║     ██╔═══██╗██╔════╝ ██║   ██║██╔════╝
██████╔╝██████╔╝██║   ██║██║  ██║██║   ██║██║        ██║       ██║     ███████║   ██║   ███████║██║     ██║   ██║██║  ███╗██║   ██║███████╗
██╔═══╝ ██╔══██╗██║   ██║██║  ██║██║   ██║██║        ██║       ██║     ██╔══██║   ██║   ██╔══██║██║     ██║   ██║██║   ██║██║   ██║╚════██║
██║     ██║  ██║╚██████╔╝██████╔╝╚██████╔╝╚██████╗   ██║       ╚██████╗██║  ██║   ██║   ██║  ██║███████╗╚██████╔╝╚██████╔╝╚██████╔╝███████║
╚═╝     ╚═╝  ╚═╝ ╚═════╝ ╚═════╝  ╚═════╝  ╚═════╝   ╚═╝        ╚═════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚══════╝ ╚═════╝  ╚═════╝  ╚═════╝ ╚══════╝

</pre>
# Getting Started

# 📘 Doc — Productcatalogus

A simple **Spring Boot REST API** to manage a product catalog (create, read, update, delete products).

This guide explains **how to run it** and **how to use it**, step by step.

---

## 1. What you need

| Tool | Version |
|------|---------|
| Java (JDK) | **21** |
| Maven | Bundled — use `mvnw` / `mvnw.cmd` |
| Database | **H2** (in-memory, nothing to install) |

Check your Java version:

```powershell
java -version
```

---

## 2. Setup — create a `.env` file

The app reads database settings from a `.env` file in the **project root** (next to `pom.xml`).

Create a file named `.env` with this content:

```dotenv
DB_DriverClass=org.h2.Driver
DB_URL=jdbc:h2:mem:productcatalogus
DB_USERNAME=sa
DB_PASSWORD=
```

> ⚠️ When running locally you need these 4 values — `application.yaml` expects them.
> They can come from a `.env` file **or** from real environment variables.
> Running with Docker? You can skip this step: the image already sets sensible defaults (see section 9).

---

## 3. Run the app

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

The app starts at 👉 **http://localhost:8080**

Other useful commands:

```powershell
.\mvnw.cmd clean install     # build + run tests
.\mvnw.cmd test              # run tests only
.\mvnw.cmd clean package     # create the JAR in target/
java -jar target\productcatalogus-0.0.1-SNAPSHOT.jar
```

---

## 4. Handy links (while the app runs)

| What | URL |
|------|-----|
| Swagger UI (try the API in the browser) | http://localhost:8080/swagger-ui/index.html |
| H2 database console | http://localhost:8080/h2-console |

For the H2 console, log in with the values from your `.env`
(JDBC URL `jdbc:h2:mem:productcatalogus`, user `sa`, empty password).

---

## 5. The Product model

### Request body (what you send)

```json
{
  "name": "Clean Code",
  "price": 59.99,
  "category": "BOOKS",
  "stock": 10
}
```

| Field | Type | Rules |
|-------|------|-------|
| `name` | text | required, not empty |
| `price` | number | required, `>= 0`, max 2 decimals, max 8 digits |
| `category` | enum | required — `BOOKS`, `ELECTRONICS`, `CLOTHING` |
| `stock` | integer | required, `>= 0` |

### Response body (what you get back)

```json
{
  "id": 1,
  "name": "Clean Code",
  "price": 59.99,
  "category": "BOOKS",
  "stock": 10,
  "createdAt": "2026-09-09T12:00:00",
  "effectivePrice": 53.99
}
```

> 💡 **effectivePrice** = the price after the automatic discount rule:
> **BOOKS priced 50 or higher get 10% off.** All other products keep their normal price.

---

## 6. API endpoints

Base path: **`/products`**

| Method | Endpoint | What it does |
|--------|----------|--------------|
| `POST` | `/products/product` | Create a new product |
| `GET` | `/products/product/{id}` | Get one product by id |
| `GET` | `/products/product` | Get a paged, filtered list |
| `PUT` | `/products/product/{id}` | Update a product |
| `DELETE` | `/products/product/{id}` | Delete a product |

### Query parameters for the list endpoint

| Param | Default | Example |
|-------|---------|---------|
| `category` | — | `category=BOOKS` |
| `minPrice` | — | `minPrice=10` |
| `maxPrice` | — | `maxPrice=100` |
| `page` | `0` | `page=1` |
| `size` | `10` | `size=25` |
| `sort` | `price,asc` | `sort=name,desc` |

---

## 7. Examples (copy & paste)

### Create a product

```powershell
curl -X POST http://localhost:8080/products/product `
  -H "Content-Type: application/json" `
  -d '{ \"name\": \"Clean Code\", \"price\": 59.99, \"category\": \"BOOKS\", \"stock\": 10 }'
```

### Get one product

```powershell
curl http://localhost:8080/products/product/1
```

### List with filters

```powershell
curl "http://localhost:8080/products/product?category=BOOKS&minPrice=10&maxPrice=100&page=0&size=5&sort=price,desc"
```

Paged response looks like this:

```json
{
  "items": [ { "id": 1, "name": "Clean Code", "...": "..." } ],
  "page": 0,
  "size": 5,
  "totalItems": 1,
  "totalPages": 1
}
```

### Update a product

```powershell
curl -X PUT http://localhost:8080/products/product/1 `
  -H "Content-Type: application/json" `
  -d '{ \"name\": \"Clean Architecture\", \"price\": 45.00, \"category\": \"BOOKS\", \"stock\": 3 }'
```

### Delete a product

```powershell
curl -X DELETE http://localhost:8080/products/product/1
```

---

## 8. Error responses

All errors come back in the same shape:

```json
{
  "error": "Validation failed",
  "messages": ["name cannot be empty", "stock must be >= 0"]
}
```

| Status | When it happens |
|--------|-----------------|
| `400 Bad Request` | Request body fails validation |
| `404 Not Found` | The product id does not exist |
| `500 Internal Server Error` | Anything unexpected |

---

## 9. Run with Docker 🐳

You do **not** need Java or Maven installed — Docker builds everything for you.
You also do **not** need a `.env` file: the settings are passed as environment
variables, and the app now starts fine when `.env` is missing.

### Option A — Docker Compose (easiest)

```powershell
docker compose up --build
```

Then open http://localhost:8080/swagger-ui/index.html

Stop it again:

```powershell
docker compose down
```

### Option B — plain Docker

```powershell
# 1. Build the image
docker build -t productcatalogus:latest .

# 2. Run it
docker run -d --name productcatalogus -p 8080:8080 productcatalogus:latest

# 3. Watch the logs
docker logs -f productcatalogus

# 4. Stop and remove
docker rm -f productcatalogus
```

### Configuration

All values have defaults, but any of them can be overridden with `-e`:

| Variable | Default |
|----------|---------|
| `DB_DriverClass` | `org.h2.Driver` |
| `DB_URL` | `jdbc:h2:mem:productcatalogus` |
| `DB_USERNAME` | `sa` |
| `DB_PASSWORD` | *(empty)* |
| `JAVA_OPTS` | *(empty)* |

```powershell
docker run -d --name productcatalogus -p 8080:8080 `
  -e DB_URL="jdbc:h2:mem:mydb" `
  -e JAVA_OPTS="-Xmx512m" `
  productcatalogus:latest
```

Use a different **host** port if 8080 is already taken:

```powershell
docker run -d --name productcatalogus -p 8081:8080 productcatalogus:latest
# app is now on http://localhost:8081
```

### How the image is built

| Stage | Base image | What happens |
|-------|-----------|--------------|
| `build` | `maven:3.9-eclipse-temurin-21` | Downloads dependencies (cached layer), builds the jar |
| `runtime` | `eclipse-temurin:21-jre-alpine` | Small JRE-only image, runs the jar as a **non-root** user |

Because dependencies are cached in their own layer, rebuilding after a code
change only re-runs the compile step and is fast.

---

## 10. Project structure

```
src/main/java/com/yasin/productcatalogus/
├─ ProductcatalogusApplication.java   # entry point (loads .env first)
├─ controller/                        # REST endpoints + error handler
├─ service/                           # business logic (+ impl/)
├─ repository/                        # Spring Data JPA repository
├─ model/
│  ├─ dto/                            # request/response objects
│  ├─ entity/                         # JPA entity (Product)
│  ├─ enums/                          # Category
│  └─ mapper/                         # MapStruct mapping + price rules
└─ utilities/                         # .env loader, custom exception
```

---

## 11. Testing

```powershell
.\mvnw.cmd test
```

Tests use their own in-memory H2 database (`src/test/resources/application.yaml`),
so they never touch your development data. Reports land in `target/surefire-reports/`.

---

## 12. Troubleshooting

| Problem | Fix |
|---------|-----|
| App fails to start with "Could not resolve placeholder `DB_URL`" | Create the `.env` file (see step 2), or pass the variables as environment variables |
| `Port 8080 is already in use` | Change `server.port` in `src/main/resources/application.yaml`, or map another host port in Docker: `-p 8081:8080` |
| `failed to connect to the docker API` | Docker Desktop is not running — start it first |
| Data disappears after restart | Expected — H2 runs in memory. Use a file URL like `jdbc:h2:file:./data/catalog` to persist |
| Wrong Java version error | Install JDK 21 and make sure `JAVA_HOME` points to it |
| Changes not picked up | DevTools restarts automatically; if not, restart the app |


