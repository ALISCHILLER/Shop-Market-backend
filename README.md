# EShop Backend - Spring Boot + Kotlin

Backend فروشگاهی پروژه Shop Market با root package زیر:

```text
com.msa.eshop.backend
```

این پروژه یک **Production-oriented Modular Monolith** است که با Kotlin، Spring Boot، PostgreSQL، Flyway، JWT و Gradle ساخته شده است.

هدف پروژه، ارائه یک API تمیز، امن و قابل توسعه برای فروشگاه، اپلیکیشن موبایل، پنل ادمین و توسعه‌های آینده است.

---

## Stack

- Kotlin 2.3.21
- Spring Boot 3.5.14
- Gradle Kotlin DSL
- Java 21
- PostgreSQL
- Spring Web MVC
- Spring Security + JWT
- Spring Data JPA
- Flyway Migration
- Bean Validation
- Actuator
- springdoc OpenAPI / Swagger UI
- Testcontainers برای integration test

---

## ساختار اصلی پروژه

```text
src/main/kotlin/com/msa/eshop/backend
├── EshopApplication.kt
├── api
├── service
├── domain
├── common
├── security
└── config
```

توضیح کلی:

```text
api      -> REST Controllers
service  -> Use-cases, business logic, transactions
domain   -> JPA entities and repositories
common   -> DTOs, exceptions, response envelope, utilities
security -> JWT, filters, auth handlers
config   -> Spring, Jackson, OpenAPI, Security config
```

---

## API Contract اصلی

### Auth

```text
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
POST /api/v1/auth/change-password
GET  /api/v1/auth/me
```

### Catalog

```text
GET /api/v1/products
GET /api/v1/products/{id}
GET /api/v1/products/{id}/discounts
GET /api/v1/product-categories
GET /api/v1/banners
```

### Cart

```text
GET  /api/v1/cart/addresses
GET  /api/v1/cart/payment-terms
POST /api/v1/cart/simulate
POST /api/v1/cart/checkout
GET  /api/v1/cart/history
GET  /api/v1/cart/{cartCode}
```

### Admin

```text
GET    /api/v1/admin/products
POST   /api/v1/admin/products
PUT    /api/v1/admin/products/{id}
DELETE /api/v1/admin/products/{id}

GET    /api/v1/admin/customers
POST   /api/v1/admin/customers
PUT    /api/v1/admin/customers/{id}
DELETE /api/v1/admin/customers/{id}

GET    /api/v1/admin/addresses
POST   /api/v1/admin/addresses
PUT    /api/v1/admin/addresses/{id}
DELETE /api/v1/admin/addresses/{id}

GET    /api/v1/admin/discounts
POST   /api/v1/admin/discounts
PUT    /api/v1/admin/discounts/{id}
DELETE /api/v1/admin/discounts/{id}

GET    /api/v1/admin/carts
GET    /api/v1/admin/carts/{cartCode}
PUT    /api/v1/admin/carts/{cartCode}/status

GET    /api/v1/admin/banners
POST   /api/v1/admin/banners
PUT    /api/v1/admin/banners/{id}
DELETE /api/v1/admin/banners/{id}

GET    /api/v1/admin/payment-terms
POST   /api/v1/admin/payment-terms
PUT    /api/v1/admin/payment-terms/{id}
DELETE /api/v1/admin/payment-terms/{id}

GET    /api/v1/admin/product-groups
POST   /api/v1/admin/product-groups
DELETE /api/v1/admin/product-groups/{code}

GET    /api/v1/admin/dashboard
GET    /api/v1/admin/audit-logs/page
```

---

## اجرای local بدون Docker

اگر Docker Desktop روی سیستم نصب نیست یا ویندوز با Docker Desktop سازگار نیست، پروژه را با PostgreSQL لوکال اجرا کن.

### 1. نصب پیش‌نیازها

- JDK 21
- PostgreSQL local
- Gradle Wrapper موجود در پروژه

### 2. ساخت دیتابیس PostgreSQL

وارد `psql` شو و این دستورات را اجرا کن:

```sql
create user eshop with password 'eshop';
create database eshop owner eshop;
```

داخل دیتابیس `eshop`:

```sql
grant all on schema public to eshop;
grant all privileges on database eshop to eshop;
```

### 3. تنظیم env در PowerShell

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:SPRING_DOCKER_COMPOSE_ENABLED="false"

$env:DB_URL="jdbc:postgresql://localhost:5432/eshop"
$env:DB_USERNAME="eshop"
$env:DB_PASSWORD="eshop"

$env:ESHOP_JWT_SECRET="local-development-secret-only-not-for-production-please-change-before-deploying"
$env:ESHOP_JWT_EXPIRATION_MINUTES="1440"
$env:ESHOP_REFRESH_TOKEN_EXPIRATION_DAYS="30"

$env:CORS_ALLOWED_ORIGINS="*"
$env:ESHOP_TRUST_FORWARDED_HEADERS="false"
```

### 4. اجرای پروژه

```bash
./gradlew bootRun
```

---

## اجرای تست‌ها

```bash
./gradlew clean test
```

نکته: برای اجرای integration testهایی که Testcontainers دارند، Docker لازم است. اگر Docker نداری، تست‌هایی که با `@Testcontainers(disabledWithoutDocker = true)` نوشته شده‌اند در نبود Docker skip می‌شوند.

---

## اجرای با Docker Compose

اگر روی سیستم Docker Desktop فعال است:

```bash
cp .env.docker.example .env
docker compose up --build
```

در حالت Docker، API روی پورت زیر بالا می‌آید:

```text
http://localhost:8282
```

---

## آدرس‌های مهم

```text
API:     http://localhost:8282
Swagger: http://localhost:8282/swagger-ui.html
Health:  http://localhost:8282/actuator/health
OpenAPI: http://localhost:8282/v3/api-docs
```

---

## کاربر تست local/dev

```text
customerCode: 1001
password:     123456
```

---

## ادمین تست local/dev

```text
customerCode: admin
password:     admin123
```

این credentialها فقط برای محیط `dev` هستند و نباید در production استفاده شوند.

---

## تست سریع API

بعد از بالا آمدن پروژه:

```bash
bash scripts/smoke-test.sh
```

---

## اتصال به Android Emulator

برای Android Emulator:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8282/"
```

برای گوشی واقعی، IP سیستم توسعه را قرار بده:

```kotlin
private const val BASE_URL = "http://192.168.x.x:8282/"
```

---

## Migration

Migrationها با Flyway مدیریت می‌شوند.

ساختار فعلی:

```text
src/main/resources/db/migration
├── common
│   └── V1__schema.sql
└── dev
    └── V100__dev_seed_data.sql
```

در profile `dev`، هم `common` اجرا می‌شود هم `dev`.

در profile `prod`، فقط `common` اجرا می‌شود.

---

## مستندات

- `docs/ARCHITECTURE.md`
- `docs/RUNNING.md`
- `docs/API_ENDPOINTS.md`
- `docs/PROJECT_GENERATION.md`
- `docs/GRADLE.md`
- `docs/android-integration.md`
- `docs/postman_collection.json`