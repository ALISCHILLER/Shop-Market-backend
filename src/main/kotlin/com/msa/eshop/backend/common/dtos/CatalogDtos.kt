package com.msa.eshop.backend.common.dtos

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

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
    val price: Long,
    val isDiscounts: Boolean,
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

data class PaymentTermDto(
    val deadLine: Int,
    val id: String,
    val name: String,
    val immediateDiscountPercent: Int = 0,
    val receiptDiscountPercent: Int = 0,
    val chequeDiscountPercent: Int = 0,
    val active: Boolean = true
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
    val price: Long,

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