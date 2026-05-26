package com.msa.eshop.backend.common

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TokenRequest(
    val customerCode: String?,
    val password: String?
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
    val customerId: String,
    val fromDate: String,
    val endDate: String
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
    val salt: String? = null
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
    val id: String
)

data class PaymentTermDto(
    val deadLine: Int,
    val id: String,
    val name: String
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
    val paymentTermId: Any?,
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
    val centerName: String,
    @field:NotBlank(message = "آدرس الزامی است")
    val customerAddress: String,
    val customerMobile: String,
    val customerPhone: String
)

data class UpsertProductGroupRequest(
    val productCategoryCode: Int,
    val productCategoryName: String?,
    val productCategoryImage: String?,
    val productCategoryImageUnselect: String?
)

data class UpsertProductRequest(
    val productName: String?,
    val productCode: Int,
    val fullNameKala1: String?,
    val unit1: String?,
    val unitid1: String?,
    val convertFactor1: Int = 1,
    val fullNameKala2: String?,
    val unit2: String?,
    val convertFactor2: Int = 1,
    val unitid2: String?,
    val productGroupCode: Int,
    val price: Int,
    val isDiscounts: Boolean = false,
    val productImage: String?
)

data class UpsertDiscountRequest(
    val productId: String,
    val discountPercent: Int,
    val fromNumber: Int,
    val endNumber: Int
)

data class UpsertBannerRequest(
    val bannerImage: String,
    val bannerName: String
)

data class UpsertPaymentTermRequest(
    val name: String,
    val deadLine: Int,
    val immediateDiscountPercent: Int = 0,
    val receiptDiscountPercent: Int = 0,
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
