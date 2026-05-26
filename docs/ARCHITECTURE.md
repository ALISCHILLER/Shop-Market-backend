# معماری پروژه

این بک‌اند به صورت **Production-ready Modular Monolith** با ریشه پکیج `com.msa.eshop.backend` طراحی شده است. برای این پروژه، این معماری از Microservice بهتر است چون دامنه فروشگاه هنوز به اندازه‌ای نیست که هزینه‌های deployment، observability، network failure و data consistency چند سرویس را توجیه کند. در عوض، کد طوری ماژولار شده که در آینده بتوان Catalog، Customer، Cart و Admin را به سرویس مستقل جدا کرد.

## تصمیم معماری

الگوی اصلی: **Clean Architecture + Hexagonal Boundaries داخل یک Modular Monolith**

یعنی:

- Controller فقط HTTP contract را می‌شناسد.
- Service شامل use-case و transaction boundary است.
- Domain شامل Entity و Repository contract سطح persistence است.
- DTOها از Entity جدا هستند تا مدل دیتابیس به API leak نشود.
- Config و Security از business logic جدا هستند.
- Migration دیتابیس با Flyway انجام می‌شود، نه Hibernate auto-create.
- خروجی API با Retrofit فعلی Android سازگار نگه داشته شده است.

## ساختار پکیج

```text
com.msa.eshop.backend
├── EshopApplication.kt
├── api                 # REST controllers / adapters
├── service             # Application services / use-cases / transactions
├── domain              # JPA entities + repository contracts
├── common              # DTOs, response envelope, exceptions
├── security            # JWT filter, token service, auth handlers
└── config              # Spring, Jackson, Security, OpenAPI config
```

## اصول حرفه‌ای رعایت‌شده

### 1. پکیج ریشه درست

طبق درخواست، root package برابر است با:

```text
com.msa.eshop.backend
```

تمام package declarationها و imports بر همین اساس تنظیم شده‌اند.

### 2. Separation of Concerns

هیچ Controller مستقیماً با Repository کار نمی‌کند. مسیر درست این است:

```text
Controller -> Service -> Repository -> Database
```

### 3. Transaction Boundary

تراکنش‌ها در Service قرار دارند، نه Controller. Read-only queryها با `@Transactional(readOnly = true)` مشخص شده‌اند.

### 4. Database-first Production Discipline

Hibernate فقط validate می‌کند:

```yaml
spring.jpa.hibernate.ddl-auto: validate
```

ساختار دیتابیس با Flyway migration ساخته می‌شود:

```text
src/main/resources/db/migration
├── V1__schema.sql
└── V2__seed_data.sql
```

### 5. Security

- JWT Bearer Authentication
- Password hashing با BCrypt
- Stateless Spring Security
- جداسازی endpointهای admin از user
- handler استاندارد برای 401 و 403

### 6. Observability

Actuator فعال است:

```text
/actuator/health
/actuator/metrics
/actuator/info
```

برای production می‌توان Prometheus/Grafana را بعداً اضافه کرد.

### 7. API Documentation

Swagger UI:

```text
/swagger-ui.html
```

OpenAPI JSON:

```text
/v3/api-docs
```

## چرا Spring Boot 3.5.x؟

برای پروژه production، شاخه پایدار Spring Boot 3.5.x انتخاب شده است؛ نه milestone، نه snapshot، نه نسخه‌ای که ممکن است dependency ecosystem آن هنوز کاملاً mature نشده باشد. Java 21 هم انتخاب شده چون LTS و مناسب production است.

## قوانین توسعه بعدی

برای توسعه حرفه‌ای‌تر، این قوانین را رعایت کن:

1. هیچ DTOای را به Entity تبدیل نکن مگر داخل mapper/use-case.
2. هیچ business ruleای داخل Controller ننویس.
3. هر تغییر دیتابیس باید migration جدید Flyway داشته باشد.
4. endpointهای جدید باید در Swagger قابل مشاهده باشند.
5. endpointهای admin باید با role `ADMIN` محافظت شوند.
6. برای queryهای سنگین، paging اضافه کن.
7. برای production واقعی، مقدار `ESHOP_JWT_SECRET` را حتماً از secret manager یا env امن بده.
