# معماری پروژه

این backend به صورت **Production-oriented Modular Monolith** با root package زیر طراحی شده است:

```text
com.msa.eshop.backend
```

برای وضعیت فعلی پروژه، Modular Monolith انتخاب بهتری از Microservice است؛ چون دامنه فروشگاه هنوز آن‌قدر بزرگ نیست که هزینه‌های deployment، observability، network failure، distributed transaction و data consistency چند سرویس مستقل را توجیه کند.

در عوض، ساختار کد به‌گونه‌ای طراحی شده که در آینده بتوان بخش‌هایی مثل Catalog، Customer، Cart یا Admin را به سرویس مستقل جدا کرد.

---

## الگوی کلی

الگوی فعلی پروژه:

```text
Controller -> Service -> Repository -> Database
```

این پروژه از نظر ساختار یک **Layered Modular Monolith** است.

منظور:

- Controller فقط HTTP contract را می‌شناسد.
- Service شامل use-case، business rule و transaction boundary است.
- Repository مسئول access به دیتابیس است.
- Entityها مدل persistence هستند.
- DTOها contract API هستند و نباید با Entity یکی شوند.
- Config و Security از business logic جدا هستند.
- Migration دیتابیس با Flyway انجام می‌شود.

---

## ساختار package

```text
com.msa.eshop.backend
├── EshopApplication.kt
├── api
├── service
├── domain
├── common
├── security
└── config
```

### `api`

REST Controllerها.

قانون:

```text
Controller نباید مستقیم Repository را صدا بزند.
Controller نباید business rule داشته باشد.
Controller فقط request/response و validation اولیه را مدیریت کند.
```

### `service`

Use-caseها، business logic و transaction boundary.

قانون:

```text
Transaction فقط در Service باشد.
Use-caseهای مهم باید در Serviceهای جدا و قابل تست نوشته شوند.
```

### `domain`

Entityها و Repository interfaceها.

قانون:

```text
Entity فقط persistence model است.
DTO نباید به Entity leak شود.
```

### `common`

DTOها، exceptionها، response envelope و utilityها.

### `security`

JWT، filterها، auth handlers، current user و security policies.

### `config`

Spring config، Jackson config، OpenAPI، SecurityConfig و startup validation.

---

## API Contract جدید

پروژه از endpointهای clean REST استفاده می‌کند.

### Auth

```text
/api/v1/auth/**
```

### Catalog

```text
/api/v1/products
/api/v1/product-categories
/api/v1/banners
```

### Cart

```text
/api/v1/cart/**
```

### Admin

```text
/api/v1/admin/**
```

مسیرهای legacy مثل موارد زیر دیگر بخشی از contract نیستند:

```text
/api/v1/User/**
/api/v1/Product/**
/api/v1/Banner/**
/api/v1/Cart/**
```

---

## Database Migration

ساختار دیتابیس با Flyway مدیریت می‌شود.

ساختار migrationها:

```text
src/main/resources/db/migration
├── common
│   └── V1__schema.sql
└── dev
    └── V100__dev_seed_data.sql
```

### common

شامل schema اصلی و چیزهایی است که در همه محیط‌ها لازم هستند.

### dev

شامل seed data مخصوص local/dev است.

در profile `dev`:

```text
common + dev
```

اجرا می‌شود.

در profile `prod`:

```text
common
```

اجرا می‌شود.

---

## چرا seed از schema جدا شده؟

چون اطلاعات dev مثل کاربر تست، admin تست و passwordهای ساده نباید وارد production شوند.

در محیط dev، passwordهای seed با `SeedPasswordInitializer` به BCrypt تبدیل می‌شوند.

در محیط‌های strict مثل prod، اگر credential ناامن وارد شود، `ProductionDataGuard` باید برنامه را fail-fast کند.

---

## Security

پروژه از JWT Bearer Authentication استفاده می‌کند.

ویژگی‌ها:

```text
Stateless session
JWT access token
Refresh token با hash ذخیره‌شده
BCrypt password hashing
Role-based access برای ADMIN
Rate limit برای endpointهای auth
Current user loading از دیتابیس
Reject کردن user disabled
Reject کردن role mismatch
```

---

## Auth Rate Limit

Endpointهای زیر rate limited هستند:

```text
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
POST /api/v1/auth/change-password
```

Rate limit در حافظه و با bucket داخلی مدیریت می‌شود.

برای deploymentهای چند instance، در آینده باید Redis یا یک distributed rate limiter اضافه شود.

---

## Cart Design

Cart به چند بخش جدا شده است:

```text
CartService
CartQueryService
CartCheckoutService
CartLineNormalizer
CartPricingCalculator
CartAssembler
CartAccessPolicy
CartCodeGenerator
```

هدف:

```text
جدا کردن query از checkout
جدا کردن pricing از persistence
جدا کردن access policy از controller
قابل تست شدن cart logic
```

---

## PaymentKind

نوع پرداخت از روی نام روش پرداخت تشخیص داده نمی‌شود.

روش پرداخت خودش فیلد زیر را دارد:

```text
paymentKind: IMMEDIATE | RECEIPT | CHEQUE
```

این تصمیم جلوی fragile business logic را می‌گیرد.

---

## Admin APIs

برای queryهای سنگین، paging اجباری است:

```text
GET /api/v1/admin/products
GET /api/v1/admin/customers
GET /api/v1/admin/addresses
GET /api/v1/admin/discounts
GET /api/v1/admin/carts
```

لیست‌های کوچک‌تر می‌توانند بدون paging باشند:

```text
GET /api/v1/admin/banners
GET /api/v1/admin/payment-terms
GET /api/v1/admin/product-groups
```

---

## Observability

Actuator فعال است:

```text
/actuator/health
/actuator/metrics
/actuator/info
```

در production می‌توان Prometheus/Grafana را اضافه کرد.

---

## OpenAPI

Swagger UI:

```text
/swagger-ui.html
```

OpenAPI JSON:

```text
/v3/api-docs
```

در profile `prod` بهتر است Swagger غیرفعال باشد.

---

## قوانین توسعه بعدی

1. Controller مستقیم Repository را صدا نزند.
2. Business rule داخل Controller نوشته نشود.
3. DTO و Entity یکی نشوند.
4. هر تغییر دیتابیس باید migration داشته باشد.
5. Admin endpoint باید role `ADMIN` بخواهد.
6. Queryهای سنگین باید paging داشته باشند.
7. Secretها نباید hardcode شوند.
8. Password و token نباید در response یا audit خام ذخیره شوند.
9. APIهای جدید باید در docs و Postman ثبت شوند.
10. تست‌های cart/auth/security باید با هر refactor آپدیت شوند.