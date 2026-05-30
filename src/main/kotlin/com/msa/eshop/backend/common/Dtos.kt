package com.msa.eshop.backend.common

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TokenRequest(
    val customerCode: String? = null,
    val password: String? = null
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "رمز عبور فعلی را وارد کنید")
    val oldPassword: String,

    @field:NotBlank(message = "رمز عبور جدید را وارد کنید")
    val newPassword: String
)

data class SimulateModelRequest(
    @field:NotNull(message = "کد کالا الزامی است")
    val productCode: Int,

    @field:Min(value = 1, message = "تعداد کالا باید بزرگ‌تر از صفر باشد")
    val quantity: Int
)

data class InsertCartModelRequest(
    @field:NotBlank(message = "شناسه آدرس الزامی است")
    val customerAddressId: String,

    @field:NotBlank(message = "شناسه روش پرداخت الزامی است")
    val paymentTermId: String,

    @field:NotNull(message = "کد کالا الزامی است")
    val productCode: Int,

    @field:Min(value = 1, message = "تعداد کالا باید بزرگ‌تر از صفر باشد")
    val quantity: Int
)

data class ReportHistoryCustomerModelRequest(
    val customerId: String = "",
    val fromDate: String = "",
    val endDate: String = ""
)

data class UserDto(
    val id: String,
    val customerCode: String,
    val customerName: String?,
    val mobile: String?,
    val phone: String?,
    val center: String?,
    val nationalCode: String?,
    val password: String? = null,
    val salt: String? = null,

    // Required by KMM admin/profile UI
    val role: String = "CUSTOMER",
    val enabled: Boolean = true
)

data class ProductDto(
    val id: String,
    val productName: String?,
    val productCode: Int,
    val fullNameKala1: String?,
    val unit1: String?,
    val unitid1: String?,
    val convertFactor1: Int,
    val fullNameKala2: String?,
    val unit2: String?,
    val convertFactor2: Int,
    val unitid2: String?,
    val productGroupCode: Int,
    val price: Int,
    val isDiscounts: Boolean,

    // Optional; client can ignore it, but backend should expose real value.
    val isTax: Boolean = true,

    val productImage: String?
)

data class ProductGroupDto(
    val productCategoryCode: Int,
    val productCategoryName: String?,
    val productCategoryImage: String?,
    val productCategoryImageUnselect: String?
)

data class BannerDto(
    val bannerImage: String,
    val bannerName: String,
    val id: String
)

data class DiscountResultDto(
    val discountPercent: Int,
    val endNumber: Int,
    val fromNumber: Int,
    val id: String,
    val productId: String
)

data class OrderAddressDto(
    val centerName: String,
    val customerAddress: String,
    val customerMobile: String,
    val customerPhone: String,
    val id: String,

    // Required by KMM admin/edit/map flow
    val customerId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Compatibility aliases; KMM supports both latitude/longitude and lat/lng.
    val lat: Double? = latitude,
    val lng: Double? = longitude,

    val isDefault: Boolean = false
)

data class PaymentTermDto(
    val deadLine: Int,
    val id: String,
    val name: String,

    // Required by KMM admin payment form
    val immediateDiscountPercent: Int = 0,
    val receiptDiscountPercent: Int = 0,
    val chequeDiscountPercent: Int = 0,
    val active: Boolean = true
)

data class SimulateDto(
    val convertFactor1: Int,
    val convertFactor2: Int,
    val discountPercent: Int,
    val discount_Percent_PaymentTerm_Receipt: Int,
    val discount_Percent_PaymentTerm_Receipt_Tax: Int,
    val discount_Percent_PaymentTerm_cheque: Int,
    val discount_Percent_PaymentTerm_cheque_Tax: Int,
    val discount_Percent_PaymentTerm_immediate: Int,
    val discount_Percent_PaymentTerm_immediate_Tax: Int,
    val finalPrice: Int,
    val finalPriceDiscount: Int,
    val fullNameKala1: String,
    val fullNameKala2: String,
    val id: String,
    val isTax: Boolean,
    val paymentTermId: String?,
    val price: Int,
    val priceByDiscountPercent: Int,
    val priceByDiscountPercentAndTax: Int,
    val priceByDiscountPercentAndTax_Receipt: Int,
    val priceByDiscountPercentAndTax_cheque: Int,
    val priceByDiscountPercentAndTax_immediate: Int,
    val priceDiscount: Int,
    val productCode: Int,
    val productGroupCode: Int,
    val productImage: String,
    val productName: String,
    val quantity: Int,
    val unit1: String,
    val unit2: String,
    val unitid1: String,
    val unitid2: String
)

data class ReportHistoryCustomerDto(
    val id: String,
    val customerCode: String,
    val customerName: String,
    val date: String,
    val address: String,
    val status: String,
    val color: String,
    val cartCode: Int
)

data class ReportCartDetailsDto(
    val cartCode: Int,
    val customerAddress: String,
    val customerName: String,
    val discount: Int,
    val id: String,
    val price: Int,
    val productCode: String,
    val productImageUrl: String,
    val productName: String,
    val quantity: Int,
    val salesDate: String,
    val statusName: String,
    val tax: Int,
    val total: Int
)

data class UpsertCustomerRequest(
    @field:NotBlank(message = "کد مشتری الزامی است")
    val customerCode: String,

    @field:NotBlank(message = "نام مشتری الزامی است")
    val customerName: String,

    val mobile: String? = null,
    val phone: String? = null,
    val center: String? = null,
    val nationalCode: String? = null,
    val password: String? = null,
    val role: String = "CUSTOMER",
    val enabled: Boolean = true
)

data class UpsertAddressRequest(
    @field:NotBlank(message = "شناسه مشتری الزامی است")
    val customerId: String,

    val centerName: String = "",

    @field:NotBlank(message = "آدرس الزامی است")
    val customerAddress: String,

    val customerMobile: String = "",
    val customerPhone: String = "",

    val latitude: Double? = null,
    val longitude: Double? = null,

    // Compatibility aliases, useful if future clients send lat/lng.
    val lat: Double? = null,
    val lng: Double? = null,

    val isDefault: Boolean? = null
)

data class UpsertProductGroupRequest(
    @field:Min(value = 1, message = "کد دسته‌بندی معتبر نیست")
    val productCategoryCode: Int,

    val productCategoryName: String?,
    val productCategoryImage: String?,
    val productCategoryImageUnselect: String?
)

data class UpsertProductRequest(
    val productName: String?,

    @field:Min(value = 1, message = "کد کالا معتبر نیست")
    val productCode: Int,

    val fullNameKala1: String?,
    val unit1: String?,
    val unitid1: String?,
    val convertFactor1: Int = 1,
    val fullNameKala2: String?,
    val unit2: String?,
    val convertFactor2: Int = 1,
    val unitid2: String?,

    @field:Min(value = 1, message = "کد گروه کالا معتبر نیست")
    val productGroupCode: Int,

    @field:Min(value = 0, message = "قیمت کالا معتبر نیست")
    val price: Int,

    val isDiscounts: Boolean = false,
    val isTax: Boolean = true,
    val productImage: String?
)

data class UpsertDiscountRequest(
    @field:NotBlank(message = "شناسه کالا الزامی است")
    val productId: String,

    @field:Min(value = 0, message = "درصد تخفیف معتبر نیست")
    @field:Max(value = 100, message = "درصد تخفیف معتبر نیست")
    val discountPercent: Int,

    @field:Min(value = 1, message = "حداقل تعداد معتبر نیست")
    val fromNumber: Int,

    @field:Min(value = 1, message = "حداکثر تعداد معتبر نیست")
    val endNumber: Int
)

data class UpsertBannerRequest(
    @field:NotBlank(message = "تصویر بنر الزامی است")
    val bannerImage: String,

    @field:NotBlank(message = "نام بنر الزامی است")
    val bannerName: String
)

data class UpsertPaymentTermRequest(
    @field:NotBlank(message = "نام روش پرداخت الزامی است")
    val name: String,

    @field:Min(value = 0, message = "مهلت پرداخت معتبر نیست")
    val deadLine: Int,

    @field:Min(value = 0, message = "درصد تخفیف معتبر نیست")
    @field:Max(value = 100, message = "درصد تخفیف معتبر نیست")
    val immediateDiscountPercent: Int = 0,

    @field:Min(value = 0, message = "درصد تخفیف معتبر نیست")
    @field:Max(value = 100, message = "درصد تخفیف معتبر نیست")
    val receiptDiscountPercent: Int = 0,

    @field:Min(value = 0, message = "درصد تخفیف معتبر نیست")
    @field:Max(value = 100, message = "درصد تخفیف معتبر نیست")
    val chequeDiscountPercent: Int = 0,

    val active: Boolean = true
)

data class DashboardDto(
    val customers: Long,
    val products: Long,
    val categories: Long,
    val carts: Long,
    val revenue: Long
)
data class PageMetaDto(
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class PageResponseDto<T>(
    val items: List<T>,
    val meta: PageMetaDto
)

data class AdminCartSummaryDto(
    val id: String,
    val cartCode: Int,
    val customerId: String?,
    val customerCode: String,
    val customerName: String,
    val customerAddress: String,
    val paymentTermId: String?,
    val paymentTermName: String,
    val statusName: String,
    val statusColor: String,
    val salesDate: String,
    val subtotal: Int,
    val discountTotal: Int,
    val taxTotal: Int,
    val total: Int,
    val itemCount: Int,
    val createdAt: String
)

data class UpdateCartStatusRequest(
    val status: String,
    val color: String? = null
)
