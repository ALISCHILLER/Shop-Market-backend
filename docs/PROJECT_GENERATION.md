# تنظیمات معادل Spring Initializr

این پروژه با قراردادهای Spring Initializr و ساختار Gradle Kotlin DSL تنظیم شده است.

اگر بخواهی skeleton پروژه را دوباره از Spring Initializr بسازی، این تنظیمات را انتخاب کن.

---

## Project Metadata

```text
Project: Gradle - Kotlin
Language: Kotlin
Spring Boot: 3.5.14
Group: com.msa.eshop.backend
Artifact: eshop-backend
Name: eshop-backend
Package name: com.msa.eshop.backend
Packaging: Jar
Java: 21
```

---

## Dependencies

```text
Spring Web
Spring Security
Spring Data JPA
Validation
PostgreSQL Driver
Flyway Migration
Actuator
Springdoc OpenAPI
Testcontainers
```

همچنین در پروژه dependency مخصوص توسعه زیر استفاده می‌شود:

```text
Spring Boot Docker Compose
```

نکته: این dependency فقط برای development است و در محیط‌هایی که Docker ندارند باید با property زیر غیرفعال شود:

```yaml
spring:
  docker:
    compose:
      enabled: false
```

---

## ساختار دایرکتوری پیشنهادی

```text
src
├── main
│   ├── kotlin
│   │   └── com/msa/eshop/backend
│   │       ├── api
│   │       ├── common
│   │       ├── config
│   │       ├── domain
│   │       ├── security
│   │       └── service
│   └── resources
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-docker.yml
│       ├── application-prod.yml
│       └── db/migration
│           ├── common
│           │   └── V1__schema.sql
│           └── dev
│               └── V100__dev_seed_data.sql
└── test
    └── kotlin
        └── com/msa/eshop/backend
```

---

## Root Package

تمام packageها باید با این ریشه شروع شوند:

```text
com.msa.eshop.backend
```

مثال:

```kotlin
package com.msa.eshop.backend.api
```

---

## Java/Kotlin Target

در `build.gradle.kts` باید Java toolchain روی 21 باشد:

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

برای Kotlin:

```kotlin
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
```

---

## Database

Database اصلی PostgreSQL است.

Hibernate نباید schema بسازد. باید فقط validate کند:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

ساخت schema با Flyway انجام می‌شود:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: false
    locations:
      - classpath:db/migration/common
```

در profile dev:

```yaml
spring:
  flyway:
    locations:
      - classpath:db/migration/common
      - classpath:db/migration/dev
```

---

## API Design

API فعلی clean REST است.

مسیرهای اصلی:

```text
/api/v1/auth
/api/v1/products
/api/v1/product-categories
/api/v1/banners
/api/v1/cart
/api/v1/admin
```

مسیرهای legacy زیر نباید دوباره ایجاد شوند:

```text
/api/v1/User
/api/v1/Product
/api/v1/Banner
/api/v1/Cart
```

---

## Security Design

- JWT Bearer Authentication
- Refresh token rotation
- BCrypt password hashing
- Role-based admin access
- Stateless session
- Rate limit برای auth endpoints

---

## Response Envelope

تمام APIها باید از قالب زیر استفاده کنند:

```kotlin
data class BaseResponse<T>(
    val data: T?,
    val hasError: Boolean = false,
    val message: String? = null
)
```

---

## Migration Rule

چون پروژه هنوز production database ندارد، schema اصلی در یک migration تمیز نگهداری می‌شود:

```text
db/migration/common/V1__schema.sql
```

Seed مخصوص dev جداست:

```text
db/migration/dev/V100__dev_seed_data.sql
```

در آینده، بعد از operational شدن پروژه، migrationهای جدید باید افزایشی باشند و migrationهای قبلی نباید تغییر کنند.

---

## بعد از ساخت skeleton

بعد از ساخت skeleton با Spring Initializr:

1. packageها را مطابق `com.msa.eshop.backend` تنظیم کن.
2. Gradle dependencies پروژه فعلی را منتقل کن.
3. فایل‌های `application*.yml` را منتقل کن.
4. migrationها را به مسیر `db/migration/common` و `db/migration/dev` منتقل کن.
5. Controller/Service/Entity/Repositoryها را منتقل کن.
6. تست‌ها را منتقل کن.
7. `./gradlew clean test` را اجرا کن.