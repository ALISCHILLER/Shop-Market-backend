# نحوه اجرای پروژه

این سند روش‌های اجرای backend را توضیح می‌دهد.

پروژه را می‌توان به دو روش اجرا کرد:

```text
1. Local بدون Docker، با PostgreSQL نصب‌شده روی سیستم
2. Docker Compose، برای سیستم‌هایی که Docker Desktop دارند
```

---

## پیش‌نیازها

### حالت local بدون Docker

- JDK 21
- PostgreSQL local
- Gradle Wrapper

### حالت Docker

- JDK 21 برای build local
- Docker Desktop
- Docker Compose

---

## روش 1: اجرای local بدون Docker

این روش برای سیستم‌هایی مناسب است که Docker ندارند یا Docker Desktop روی ویندوزشان نصب نمی‌شود.

---

### 1. ساخت دیتابیس PostgreSQL

وارد `psql` شو:

```bash
psql -U postgres
```

بعد:

```sql
create user eshop with password 'eshop';
create database eshop owner eshop;
```

داخل دیتابیس:

```sql
\c eshop
grant all on schema public to eshop;
grant all privileges on database eshop to eshop;
```

خروج:

```sql
\q
```

---

### 2. تنظیم env در PowerShell

در root پروژه:

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

$env:ESHOP_AUTH_RATE_LIMIT_ENABLED="true"
```

---

### 3. اجرای پروژه

```bash
./gradlew bootRun
```

اگر همه‌چیز درست باشد، API روی این آدرس بالا می‌آید:

```text
http://localhost:8282
```

---

## روش 2: اجرای با Docker Compose

اگر Docker Desktop روی سیستم نصب و فعال است:

```bash
cp .env.docker.example .env
docker compose up --build
```

یا برای اجرای در background:

```bash
docker compose up -d --build
```

برای توقف و حذف volumeها:

```bash
docker compose down -v
```

---

## اجرای تست‌ها

```bash
./gradlew clean test
```

نکته: بعضی integration testها ممکن است Testcontainers داشته باشند. این تست‌ها برای اجرا نیاز به Docker دارند، مگر اینکه با `disabledWithoutDocker` تنظیم شده باشند.

---

## اجرای jar

ساخت jar:

```bash
./gradlew clean bootJar
```

اجرای jar:

```bash
java -jar build/libs/eshop-backend.jar
```

در اجرای jar هم باید envهای دیتابیس و JWT تنظیم شده باشند.

---

## آدرس‌های مهم

```text
API:     http://localhost:8282
Swagger: http://localhost:8282/swagger-ui.html
Health:  http://localhost:8282/actuator/health
OpenAPI: http://localhost:8282/v3/api-docs
```

---

## کاربرهای تست در profile dev

### Customer

```text
customerCode: 1001
password: 123456
```

### Admin

```text
customerCode: admin
password: admin123
```

---

## Smoke Test

بعد از بالا آمدن API:

```bash
bash scripts/smoke-test.sh
```

اگر روی Windows هستی و Bash نداری، می‌توانی endpointها را با PowerShell یا Postman تست کنی.

---

## تست دستی با curl

### Products

```bash
curl http://localhost:8282/api/v1/products
```

### Categories

```bash
curl http://localhost:8282/api/v1/product-categories
```

### Banners

```bash
curl http://localhost:8282/api/v1/banners
```

### Login

```bash
curl -X POST http://localhost:8282/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"customerCode":"admin","password":"admin123"}'
```

در PowerShell:

```powershell
curl -Method POST http://localhost:8282/api/v1/auth/login `
  -ContentType "application/json" `
  -Body "{\"customerCode\":\"admin\",\"password\":\"admin123\"}"
```

---

## مشکل رایج: Docker command not found

اگر این خطا را دیدی:

```text
docker : The term 'docker' is not recognized
```

یعنی Docker نصب نیست یا در PATH نیست.

اگر Docker نمی‌خواهی، این property را تنظیم کن:

```powershell
$env:SPRING_DOCKER_COMPOSE_ENABLED="false"
```

و با PostgreSQL local اجرا کن.

---

## مشکل رایج: Docker Desktop incompatible Windows

اگر Docker Desktop خطای ناسازگاری Windows داد، Docker را کنار بگذار و از روش local بدون Docker استفاده کن.

---

## مشکل رایج: Gradle requires JVM 17 or later

اگر این خطا را دیدی:

```text
Gradle requires JVM 17 or later
```

یعنی Java فعال روی سیستم قدیمی است.

چک کن:

```bash
java -version
```

باید Java 21 باشد.

---

## مشکل رایج: Flyway checksum یا migration conflict

چون پروژه هنوز production database ندارد، در local/dev می‌توانی دیتابیس را reset کنی:

```sql
drop database if exists eshop;
create database eshop owner eshop;
```

بعد دوباره پروژه را اجرا کن.

---

## اتصال Android Emulator

برای Android Emulator:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8282/"
```

برای گوشی واقعی:

```kotlin
private const val BASE_URL = "http://192.168.1.X:8282/"
```