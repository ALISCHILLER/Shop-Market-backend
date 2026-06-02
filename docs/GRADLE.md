# Gradle Kotlin DSL Notes

این پروژه با Gradle Kotlin DSL ساخته شده و از Gradle Wrapper استفاده می‌کند.

فایل‌های اصلی:

```text
settings.gradle.kts
build.gradle.kts
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.properties
```

---

## نسخه‌ها

- Java toolchain: 21
- Kotlin JVM target: 21
- Spring Boot: 3.5.14
- Gradle Wrapper: 9.1.0

---

## دستورات مهم

### اجرای پروژه

```bash
./gradlew bootRun
```

در ویندوز PowerShell:

```powershell
./gradlew bootRun
```

---

### اجرای تست‌ها

```bash
./gradlew test
```

یا:

```bash
./gradlew clean test
```

---

### ساخت jar

```bash
./gradlew clean bootJar
```

خروجی در مسیر زیر ساخته می‌شود:

```text
build/libs/
```

اجرای jar:

```bash
java -jar build/libs/eshop-backend.jar
```

---

### دیدن dependencyها

```bash
./gradlew dependencies
```

برای یک configuration خاص:

```bash
./gradlew dependencies --configuration runtimeClasspath
```

---

### پاک‌سازی build

```bash
./gradlew clean
```

---

## Java 21

برای اجرای Gradle و پروژه، Java 21 لازم است.

چک کردن نسخه Java:

```bash
java -version
```

باید خروجی مشابه این باشد:

```text
openjdk version "21..."
```

اگر Java 8 یا 11 فعال باشد، Gradle یا Spring Boot ممکن است اجرا نشود.

---

## تنظیم موقت JAVA_HOME در PowerShell

مثال:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

بعد:

```powershell
java -version
```

---

## اجرای بدون Docker

اگر Docker نداری، مشکلی نیست. PostgreSQL را local نصب کن و با profile `dev` اجرا کن:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:SPRING_DOCKER_COMPOSE_ENABLED="false"

$env:DB_URL="jdbc:postgresql://localhost:5432/eshop"
$env:DB_USERNAME="eshop"
$env:DB_PASSWORD="eshop"
```

بعد:

```powershell
./gradlew bootRun
```

---

## Spring Boot Docker Compose Integration

در پروژه dependency زیر وجود دارد:

```kotlin
developmentOnly("org.springframework.boot:spring-boot-docker-compose")
```

اگر `docker-compose.yml` در root پروژه باشد، Spring Boot ممکن است هنگام `bootRun` تلاش کند Docker Compose را اجرا کند.

برای سیستم‌هایی که Docker ندارند، این property باید false باشد:

```yaml
spring:
  docker:
    compose:
      enabled: false
```

یا در PowerShell:

```powershell
$env:SPRING_DOCKER_COMPOSE_ENABLED="false"
```

---

## ساخت Gradle Wrapper

اگر wrapper وجود نداشت:

```bash
gradle wrapper --gradle-version 9.1.0
```

بعد از آن، همیشه از wrapper استفاده کن:

```bash
./gradlew bootRun
```

نه از Gradle نصب‌شده روی سیستم.

---

## Configuration Cache

برای سریع‌تر شدن build می‌توان بعداً configuration cache را بررسی کرد:

```bash
./gradlew test --configuration-cache
```

اگر همه pluginها و taskها سازگار باشند، می‌توان آن را دائمی کرد.

---

## نکته درباره Testcontainers

تست‌هایی که از Testcontainers استفاده می‌کنند، برای اجرا به Docker نیاز دارند.

برای سیستم بدون Docker، تست‌های Testcontainers باید با تنظیم زیر نوشته شوند:

```kotlin
@Testcontainers(disabledWithoutDocker = true)
```

این باعث می‌شود در نبود Docker تست مربوطه skip شود، نه اینکه کل build خراب شود.