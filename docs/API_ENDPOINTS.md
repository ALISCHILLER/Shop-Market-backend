# API Endpoints

این سند contract نهایی API پروژه را توضیح می‌دهد.

تمام responseها در قالب envelope زیر برمی‌گردند:

```json
{
  "data": {},
  "hasError": false,
  "message": null
}
```

---

## Public APIs

---

## Auth

### Login

```http
POST /api/v1/auth/login
```

Auth لازم ندارد.

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
    "accessToken": "jwt-access-token",
    "refreshToken": "refresh-token",
    "tokenType": "Bearer",
    "passwordChangeRequired": true
  },
  "hasError": false,
  "message": null
}
```

---

### Refresh Token

```http
POST /api/v1/auth/refresh
```

Auth لازم ندارد.

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "data": {
    "accessToken": "new-jwt-access-token",
    "refreshToken": "new-refresh-token",
    "tokenType": "Bearer",
    "passwordChangeRequired": false
  },
  "hasError": false,
  "message": null
}
```

---

### Logout

```http
POST /api/v1/auth/logout
```

Auth لازم ندارد.

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "data": true,
  "hasError": false,
  "message": null
}
```

---

### Change Password

```http
POST /api/v1/auth/change-password
```

Auth لازم دارد.

Header:

```text
Authorization: Bearer <accessToken>
```

Request:

```json
{
  "oldPassword": "123456",
  "newPassword": "NewStrongPassword123"
}
```

Response:

```json
{
  "data": true,
  "hasError": false,
  "message": null
}
```

---

### Current User

```http
GET /api/v1/auth/me
```

Auth لازم دارد.

Response:

```json
{
  "data": {
    "id": "uuid",
    "customerCode": "1001",
    "customerName": "فروشگاه نمونه زرمارکت",
    "mobile": "09120000001",
    "phone": "02144000001",
    "center": "تهران غرب",
    "nationalCode": "0012345678",
    "role": "CUSTOMER",
    "enabled": true,
    "passwordChangeRequired": false
  },
  "hasError": false,
  "message": null
}
```

---

## Catalog

### Product List

```http
GET /api/v1/products
```

Auth لازم ندارد.

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

Example:

```http
GET /api/v1/products?page=0&size=20&search=برنج
```

Response:

```json
{
  "data": {
    "items": [],
    "page": 0,
    "size": 20,
    "totalItems": 0,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false
  },
  "hasError": false,
  "message": null
}
```

---

### Product Details

```http
GET /api/v1/products/{id}
```

Auth لازم ندارد.

---

### Product Discounts

```http
GET /api/v1/products/{id}/discounts
```

Auth لازم ندارد.

---

### Product Categories

```http
GET /api/v1/product-categories
```

Auth لازم ندارد.

---

### Banners

```http
GET /api/v1/banners
```

Auth لازم ندارد.

---

## Cart

تمام endpointهای cart نیاز به JWT دارند.

Header:

```text
Authorization: Bearer <accessToken>
```

---

### Customer Addresses

```http
GET /api/v1/cart/addresses
```

آدرس‌های مشتری جاری را برمی‌گرداند.

---

### Payment Terms

```http
GET /api/v1/cart/payment-terms
```

روش‌های پرداخت فعال را برمی‌گرداند.

---

### Simulate Cart

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

Response:

```json
{
  "data": {
    "paymentTermId": "16ccab60-279b-410a-90d1-b2673d5d1dd1",
    "paymentTermName": "رسید ۳۰ روزه",
    "paymentKind": "RECEIPT",
    "paymentKindTitle": "رسید",
    "subtotal": 17000000,
    "productDiscountTotal": 510000,
    "paymentDiscountTotal": 329800,
    "discountTotal": 839800,
    "taxableAmount": 16160200,
    "taxTotal": 1454418,
    "total": 17614618,
    "items": []
  },
  "hasError": false,
  "message": null
}
```

---

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

Response:

```json
{
  "data": {
    "cartId": "uuid",
    "cartCode": 100001,
    "statusCode": "REGISTERED",
    "statusName": "ثبت شده",
    "subtotal": 17000000,
    "discountTotal": 839800,
    "taxTotal": 1454418,
    "total": 17614618
  },
  "hasError": false,
  "message": null
}
```

---

### Cart History

```http
GET /api/v1/cart/history
```

Query params:

```text
fromDate=
toDate=
```

---

### Cart Details

```http
GET /api/v1/cart/{cartCode}
```

---

# Admin APIs

تمام admin endpointها نیاز به کاربر با role `ADMIN` دارند.

Header:

```text
Authorization: Bearer <adminAccessToken>
```

---

## Admin Products

### Paged Search

```http
GET /api/v1/admin/products
```

Query params:

```text
page=0
size=20
search=
productGroupCode=
isDiscounts=
isTax=
sortBy=productName
direction=ASC
```

### Create

```http
POST /api/v1/admin/products
```

### Update

```http
PUT /api/v1/admin/products/{id}
```

### Delete

```http
DELETE /api/v1/admin/products/{id}
```

---

## Admin Customers

### Paged Search

```http
GET /api/v1/admin/customers
```

Query params:

```text
page=0
size=20
search=
role=
enabled=
sortBy=createdAt
direction=DESC
```

### Create

```http
POST /api/v1/admin/customers
```

### Update

```http
PUT /api/v1/admin/customers/{id}
```

### Delete

```http
DELETE /api/v1/admin/customers/{id}
```

---

## Admin Addresses

### Paged Search

```http
GET /api/v1/admin/addresses
```

Query params:

```text
page=0
size=20
customerId=
search=
sortBy=createdAt
direction=DESC
```

### Create

```http
POST /api/v1/admin/addresses
```

### Update

```http
PUT /api/v1/admin/addresses/{id}
```

### Delete

```http
DELETE /api/v1/admin/addresses/{id}
```

---

## Admin Discounts

### Paged Search

```http
GET /api/v1/admin/discounts
```

Query params:

```text
page=0
size=20
productIdOrCode=
search=
sortBy=fromNumber
direction=ASC
```

### Create

```http
POST /api/v1/admin/discounts
```

### Update

```http
PUT /api/v1/admin/discounts/{id}
```

### Delete

```http
DELETE /api/v1/admin/discounts/{id}
```

---

## Admin Carts

### Paged Search

```http
GET /api/v1/admin/carts
```

### Cart Lines

```http
GET /api/v1/admin/carts/{cartCode}
```

### Update Status

```http
PUT /api/v1/admin/carts/{cartCode}/status
```

Request:

```json
{
  "status": "PROCESSING",
  "color": "#1565C0"
}
```

---

## Admin Banners

```http
GET    /api/v1/admin/banners
POST   /api/v1/admin/banners
PUT    /api/v1/admin/banners/{id}
DELETE /api/v1/admin/banners/{id}
```

---

## Admin Payment Terms

```http
GET    /api/v1/admin/payment-terms
POST   /api/v1/admin/payment-terms
PUT    /api/v1/admin/payment-terms/{id}
DELETE /api/v1/admin/payment-terms/{id}
```

---

## Admin Product Groups

```http
GET    /api/v1/admin/product-groups
POST   /api/v1/admin/product-groups
DELETE /api/v1/admin/product-groups/{code}
```

---

## Admin Dashboard

```http
GET /api/v1/admin/dashboard
```

---

## Admin Audit Logs

```http
GET /api/v1/admin/audit-logs/page
```

Query params:

```text
page=0
size=20
action=
entityType=
entityId=
actorCustomerCode=
fromDate=
toDate=
sortBy=createdAt
direction=DESC
```