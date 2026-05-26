# اتصال اپ اندروید به بک‌اند

این پروژه‌ی بک‌اند مسیرهای Retrofit فعلی اپ را بدون تغییر پشتیبانی می‌کند. فقط Base URL را اصلاح کن.

فایل:

```text
app/src/main/java/com/msa/eshop/di/RemoteProvidersModule.kt
```

Emulator:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8282/"
```

گوشی واقعی:

```kotlin
private const val BASE_URL = "http://192.168.x.x:8282/"
```

نکته: اگر روی Android 9+ با HTTP کار می‌کنی، پروژه فعلاً باید cleartext را اجازه بدهد. در `AndroidManifest.xml` معمولاً این گزینه لازم است:

```xml
android:usesCleartextTraffic="true"
```

بعد از login، Interceptor فعلی اپ مقدار زیر را ارسال می‌کند:

```text
Authorization: Bearer <token>
```

بنابراین endpointهای محافظت‌شده مثل پروفایل، آدرس، simulate و ثبت سفارش کار می‌کنند.
