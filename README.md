# EShop Backend - Spring Boot + Kotlin + Gradle Kotlin DSL

بک‌اند production-ready برای پروژه Shop Market Compose با root package زیر:

```text
com.msa.eshop.backend
```

این نسخه با معماری **Clean Architecture + Hexagonal-style Modular Monolith** طراحی شده و برای اتصال مستقیم به اپ Android فعلی آماده است.

## Stack

- Kotlin 2.3.21
- Spring Boot 3.5.14
- Gradle Kotlin DSL
- Java 21
- PostgreSQL 18
- Spring Web MVC
- Spring Security + JWT
- Spring Data JPA
- Flyway Migration
- Bean Validation
- Actuator
- springdoc OpenAPI / Swagger UI
- Testcontainers آماده برای تست دیتابیس

## ساختار اصلی

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

## اجرای سریع با Docker

```bash
cd eshop-backend
cp .env.example .env
docker compose up --build
```

## اجرای local با Gradle

```bash
cd eshop-backend
cp .env.example .env
docker compose up -d postgres
gradle clean bootRun
```

یا اگر Gradle Wrapper را ساخته‌ای:

```bash
./gradlew clean bootRun
```

## آدرس‌ها

```text
API:     http://localhost:8282
Swagger: http://localhost:8282/swagger-ui.html
Health:  http://localhost:8282/actuator/health
OpenAPI: http://localhost:8282/v3/api-docs
```

## کاربر تست

```text
customerCode: 1001
password:     123456
```

## ادمین تست

```text
customerCode: admin
password:     admin123
```

## تست سریع API

```bash
bash scripts/smoke-test.sh
```

## اتصال به Android Emulator

```kotlin
private const val BASE_URL = "http://10.0.2.2:8282/"
```
## Local Docker Run

For local development, use the default `.env.example` profile:

```bash
cp .env.example .env
docker compose up --build
```


## مستندات

- `docs/ARCHITECTURE.md` معماری و قوانین توسعه
- `docs/RUNNING.md` نحوه اجرا
- `docs/API_ENDPOINTS.md` مسیرهای API
- `docs/PROJECT_GENERATION.md` تنظیمات معادل Spring Initializr
- `docs/android-integration.md` اتصال Android
