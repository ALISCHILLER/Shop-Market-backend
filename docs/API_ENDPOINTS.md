# API Endpoints

## Mobile-compatible APIs

| Method | Path | Auth | Description |
|---|---|---:|---|
| POST | `/api/v1/User/loginUser` | No | دریافت JWT |
| GET | `/api/v1/User/CustomerProfile` | Yes | پروفایل مشتری |
| GET | `/api/v1/User/GetCustomerAddress` | Yes | آدرس‌های مشتری |
| POST | `/api/v1/User/changepassword` | Yes | تغییر رمز عبور |
| GET | `/api/v1/Product/GetListKala` | No | لیست کالاها |
| GET | `/api/v1/Product/GetProductCategory` | No | دسته‌بندی کالاها |
| GET | `/api/v1/Product/GetListDiscounts?ProductID=...` | No | تخفیف‌های کالا |
| GET | `/api/v1/Banner/GetBanner` | No | بنرها |
| POST | `/api/v1/Cart/GetCartSimulateRsult` | Yes | محاسبه قیمت، تخفیف و مالیات |
| GET | `/api/v1/Cart/GetPaymentTerm` | Yes | شرایط پرداخت |
| POST | `/api/v1/Cart/InsertCart` | Yes | ثبت سفارش |
| POST | `/api/v1/Cart/ReportHistoryCustomer` | Yes | تاریخچه سفارش‌ها |
| GET | `/api/v1/Cart/ReportCartDetails?CartCode=...` | Yes | جزئیات سفارش |

## Admin APIs

تمام مسیرهای زیر نیازمند JWT ادمین هستند:

```text
/api/v1/admin/**
```

قابلیت‌ها:

- داشبورد
- CRUD مشتری
- CRUD آدرس
- CRUD دسته‌بندی
- CRUD محصول
- CRUD بنر
- CRUD تخفیف
- CRUD شرایط پرداخت
