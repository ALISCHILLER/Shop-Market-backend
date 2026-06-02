# API Endpoints

## Public APIs

### Auth

| Method | Path | Auth | Description |
|---|---|---:|---|
| POST | `/api/v1/auth/login` | No | Login and receive access/refresh tokens |
| POST | `/api/v1/auth/refresh` | No | Rotate refresh token and receive a new access token |
| POST | `/api/v1/auth/logout` | No | Revoke refresh token |
| POST | `/api/v1/auth/change-password` | Yes | Change current user's password |
| GET | `/api/v1/auth/me` | Yes | Current user profile |

### Catalog

| Method | Path | Auth | Description |
|---|---|---:|---|
| GET | `/api/v1/products` | No | Paged product list |
| GET | `/api/v1/products/{id}` | No | Product details |
| GET | `/api/v1/products/{id}/discounts` | No | Product discounts |
| GET | `/api/v1/product-categories` | No | Product categories |
| GET | `/api/v1/banners` | No | Active banners |

### Cart

| Method | Path | Auth | Description |
|---|---|---:|---|
| GET | `/api/v1/cart/addresses` | Yes | Current customer's addresses |
| GET | `/api/v1/cart/payment-terms` | Yes | Active payment terms |
| POST | `/api/v1/cart/simulate` | Yes | Simulate cart pricing |
| POST | `/api/v1/cart/checkout` | Yes | Submit cart order |
| GET | `/api/v1/cart/history` | Yes | Current customer's order history |
| GET | `/api/v1/cart/{cartCode}` | Yes | Cart details |

## Admin APIs

All admin APIs require an ADMIN JWT.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/admin/products` | Paged product search |
| POST | `/api/v1/admin/products` | Create product |
| PUT | `/api/v1/admin/products/{id}` | Update product |
| DELETE | `/api/v1/admin/products/{id}` | Delete product |
| GET | `/api/v1/admin/customers` | Paged customer search |
| POST | `/api/v1/admin/customers` | Create customer |
| PUT | `/api/v1/admin/customers/{id}` | Update customer |
| DELETE | `/api/v1/admin/customers/{id}` | Delete customer |
| GET | `/api/v1/admin/addresses` | Paged address search |
| POST | `/api/v1/admin/addresses` | Create address |
| PUT | `/api/v1/admin/addresses/{id}` | Update address |
| DELETE | `/api/v1/admin/addresses/{id}` | Delete address |
| GET | `/api/v1/admin/discounts` | Paged discount search |
| POST | `/api/v1/admin/discounts` | Create discount |
| PUT | `/api/v1/admin/discounts/{id}` | Update discount |
| DELETE | `/api/v1/admin/discounts/{id}` | Delete discount |
| GET | `/api/v1/admin/carts` | Paged cart search |
| GET | `/api/v1/admin/carts/{cartCode}` | Cart lines |
| PUT | `/api/v1/admin/carts/{cartCode}/status` | Update cart status |
| GET | `/api/v1/admin/banners` | Banner list |
| POST | `/api/v1/admin/banners` | Create banner |
| PUT | `/api/v1/admin/banners/{id}` | Update banner |
| DELETE | `/api/v1/admin/banners/{id}` | Delete banner |
| GET | `/api/v1/admin/payment-terms` | Payment term list |
| POST | `/api/v1/admin/payment-terms` | Create payment term |
| PUT | `/api/v1/admin/payment-terms/{id}` | Update payment term |
| DELETE | `/api/v1/admin/payment-terms/{id}` | Delete payment term |
| GET | `/api/v1/admin/product-groups` | Product group list |
| POST | `/api/v1/admin/product-groups` | Create or update product group |
| DELETE | `/api/v1/admin/product-groups/{code}` | Delete product group |
| GET | `/api/v1/admin/dashboard` | Dashboard summary |
| GET | `/api/v1/admin/audit-logs/page` | Paged audit logs |