# اتصال اپ Android به Backend

این سند نحوه اتصال اپ Android به backend جدید Shop Market را توضیح می‌دهد.

API فعلی پروژه بر اساس clean REST contract طراحی شده و مسیرهای legacy مثل `/api/v1/User`, `/api/v1/Product`, `/api/v1/Cart` دیگر استفاده نمی‌شوند.

---

## Base URL

برای Android Emulator:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8282/"
```

برای گوشی واقعی:

```kotlin
private const val BASE_URL = "http://192.168.x.x:8282/"
```

به جای `192.168.x.x`، IP سیستم توسعه را قرار بده.

---

## Cleartext HTTP در Development

اگر روی Android 9+ با HTTP کار می‌کنی، در development ممکن است نیاز باشد cleartext را فعال کنی.

در `AndroidManifest.xml`:

```xml
<application
    android:usesCleartextTraffic="true"
    ... >
</application>
```

برای production بهتر است HTTPS استفاده شود.

---

## Response Envelope

تمام responseها این قالب را دارند:

```json
{
  "data": {},
  "hasError": false,
  "message": null
}
```

در Android، بهتر است یک مدل عمومی داشته باشی:

```kotlin
data class BaseResponse<T>(
    val data: T?,
    val hasError: Boolean,
    val message: String?
)
```

---

## Auth

### Login

Endpoint:

```http
POST /api/v1/auth/login
```

Request:

```json
{
  "customerCode": "1001",
  "password": "123456"
}
```

Response:

```json
{
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "passwordChangeRequired": true
  },
  "hasError": false,
  "message": null
}
```

Kotlin models:

```kotlin
data class LoginRequest(
    val customerCode: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val passwordChangeRequired: Boolean
)
```

Retrofit:

```kotlin
interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): BaseResponse<LoginResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequest
    ): BaseResponse<RefreshTokenResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): BaseResponse<Boolean>

    @POST("api/v1/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): BaseResponse<Boolean>

    @GET("api/v1/auth/me")
    suspend fun me(): BaseResponse<UserDto>
}
```

---

## Authorization Header

بعد از login، مقدار `accessToken` را ذخیره کن و در requestهای protected بفرست:

```text
Authorization: Bearer <accessToken>
```

نمونه OkHttp Interceptor:

```kotlin
class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.accessToken()

        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
```

---

## Refresh Token

Endpoint:

```http
POST /api/v1/auth/refresh
```

Request:

```json
{
  "refreshToken": "..."
}
```

Kotlin models:

```kotlin
data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val passwordChangeRequired: Boolean
)
```

وقتی API مقدار 401 برگرداند، می‌توانی refresh token را صدا بزنی و بعد request اصلی را تکرار کنی.

---

## Catalog APIs

### Product List

```http
GET /api/v1/products
```

Query params:

```text
page=0
size=20
search=
categoryCode=
hasDiscount=
sortBy=productName
direction=ASC
```

Retrofit:

```kotlin
interface CatalogApi {

    @GET("api/v1/products")
    suspend fun products(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
        @Query("categoryCode") categoryCode: Int? = null,
        @Query("hasDiscount") hasDiscount: Boolean? = null,
        @Query("sortBy") sortBy: String = "productName",
        @Query("direction") direction: String = "ASC"
    ): BaseResponse<PageResponse<ProductDto>>

    @GET("api/v1/products/{id}")
    suspend fun product(
        @Path("id") id: String
    ): BaseResponse<ProductDto>

    @GET("api/v1/products/{id}/discounts")
    suspend fun productDiscounts(
        @Path("id") id: String
    ): BaseResponse<List<DiscountDto>>

    @GET("api/v1/product-categories")
    suspend fun productCategories(): BaseResponse<List<ProductGroupDto>>

    @GET("api/v1/banners")
    suspend fun banners(): BaseResponse<List<BannerDto>>
}
```

---

## Cart APIs

تمام APIهای cart نیاز به Authorization header دارند.

### Addresses

```http
GET /api/v1/cart/addresses
```

### Payment Terms

```http
GET /api/v1/cart/payment-terms
```

### Simulate

```http
POST /api/v1/cart/simulate
```

Request:

```json
{
  "paymentTermId": "16ccab60-279b-410a-90d1-b2673d5d1dd1",
  "items": [
    {
      "productCode": 100101,
      "quantity": 2
    }
  ]
}
```

### Checkout

```http
POST /api/v1/cart/checkout
```

Request:

```json
{
  "customerAddressId": "00000000-0000-0000-0000-000000003001",
  "paymentTermId": "16ccab60-279b-410a-90d1-b2673d5d1dd1",
  "items": [
    {
      "productCode": 100101,
      "quantity": 2
    }
  ]
}
```

Retrofit:

```kotlin
interface CartApi {

    @GET("api/v1/cart/addresses")
    suspend fun addresses(): BaseResponse<List<OrderAddressDto>>

    @GET("api/v1/cart/payment-terms")
    suspend fun paymentTerms(): BaseResponse<List<PaymentTermDto>>

    @POST("api/v1/cart/simulate")
    suspend fun simulate(
        @Body request: CartSimulateRequest
    ): BaseResponse<CartSimulateResponse>

    @POST("api/v1/cart/checkout")
    suspend fun checkout(
        @Body request: CartCheckoutRequest
    ): BaseResponse<CartCheckoutResponse>

    @GET("api/v1/cart/history")
    suspend fun history(
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): BaseResponse<List<CartHistoryDto>>

    @GET("api/v1/cart/{cartCode}")
    suspend fun details(
        @Path("cartCode") cartCode: Int
    ): BaseResponse<CartDetailsDto>
}
```

---

## Cart Kotlin Models

```kotlin
data class CartLineRequest(
    val productCode: Int,
    val quantity: Int
)

data class CartSimulateRequest(
    val paymentTermId: String,
    val items: List<CartLineRequest>
)

data class CartCheckoutRequest(
    val customerAddressId: String,
    val paymentTermId: String,
    val items: List<CartLineRequest>
)
```

---

## نکته‌های مهم برای Android

1. همه requestهای cart و admin نیاز به JWT دارند.
2. `accessToken` را در storage امن ذخیره کن.
3. `refreshToken` را جدا و امن‌تر ذخیره کن.
4. در صورت 401، refresh token را صدا بزن.
5. اگر refresh هم fail شد، کاربر را logout کن.
6. برای Emulator از `10.0.2.2` استفاده کن.
7. برای گوشی واقعی، backend و گوشی باید روی یک شبکه باشند.