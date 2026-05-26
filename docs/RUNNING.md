# نحوه اجرای پروژه

## پیش‌نیازها

- JDK 21
- Docker + Docker Compose
- Gradle 9.x برای اجرای local بدون Docker

## روش 1: اجرای کامل با Docker Compose

این روش ساده‌ترین و تمیزترین راه اجراست، چون هم PostgreSQL و هم API بالا می‌آیند.

```bash
cd eshop-backend
cp .env.example .env
docker compose up --build
```

آدرس‌ها:

```text
API:     http://localhost:8282
Swagger: http://localhost:8282/swagger-ui.html
Health:  http://localhost:8282/actuator/health
```

## روش 2: اجرای دیتابیس با Docker و API با Gradle

```bash
cd eshop-backend
cp .env.example .env
docker compose up -d postgres
gradle clean bootRun
```

اگر Gradle Wrapper را روی سیستم ساختی:

```bash
./gradlew clean bootRun
```

## روش 3: ساخت فایل jar

```bash
gradle clean bootJar
java -jar build/libs/eshop-backend.jar
```

## Smoke Test

بعد از بالا آمدن API:

```bash
bash scripts/smoke-test.sh
```

## کاربرهای تست

```text
customerCode: 1001
password: 123456
```

```text
customerCode: admin
password: admin123
```

## اتصال Android Emulator

در پروژه Android:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8282/"
```

برای موبایل واقعی، IP سیستم را قرار بده:

```kotlin
private const val BASE_URL = "http://192.168.1.X:8282/"
```
