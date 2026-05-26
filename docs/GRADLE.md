# Gradle Kotlin DSL Notes

این پروژه از ساختار Spring Initializr برای Gradle - Kotlin استفاده می‌کند:

- `settings.gradle.kts`
- `build.gradle.kts`
- `src/main/kotlin`
- `src/test/kotlin`
- Java toolchain روی 21
- Kotlin JVM target روی 21
- `bootRun`, `bootJar`, `test`

## دستورات مهم

```bash
./gradlew bootRun
./gradlew test
./gradlew clean bootJar
./gradlew dependencies
```

## ساخت Wrapper استاندارد

```bash
gradle wrapper --gradle-version 9.1.0
```

بعد از اجرای دستور بالا، فایل `gradle/wrapper/gradle-wrapper.jar` ساخته می‌شود و پروژه روی همه سیستم‌ها بدون نصب Gradle اجرا می‌شود.
