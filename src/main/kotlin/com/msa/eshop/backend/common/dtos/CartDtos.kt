package com.msa.eshop.backend.common.dtos

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Size
data class SimulateModelRequest(
    val paymentTermId: String? = null,

    @field:NotNull(message = "کد کالا الزامی است")
    val productCode: Int,

    @field:Min(value = 1, message = "تعداد کالا باید بزرگ‌تر از صفر باشد")
    @field:Max(value = 1_000, message = "تعداد هر کالا نمی‌تواند بیشتر از ۱۰۰۰ باشد")
    val quantity: Int
)

data class CartSimulateRequest(
    @field:Valid
    @field:NotEmpty(message = "لیست کالاها نمی‌تواند خالی باشد")
    @field:Size(max = 100, message = "تعداد ردیف‌های سبد خرید نمی‌تواند بیشتر از ۱۰۰ باشد")
    val items: List<CartSimulateLineRequest>,

    @field:NotNull(message = "روش پرداخت الزامی است")
    val paymentTermId: UUID
)

data class CartSimulateLineRequest(
    @field:Min(value = 1, message = "کد کالا معتبر نیست")
    val productCode: Int,

    @field:Min(value = 1, message = "تعداد کالا باید بزرگ‌تر از صفر باشد")
    @field:Max(value = 1_000, message = "تعداد هر کالا نمی‌تواند بیشتر از ۱۰۰۰ باشد")
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
    @field:Max(value = 1_000, message = "تعداد هر کالا نمی‌تواند بیشتر از ۱۰۰۰ باشد")
    val quantity: Int
)

data class ReportHistoryCustomerModelRequest(
    val customerId: String = "",
    val fromDate: String = "",
    val endDate: String = ""
)

data class SimulateDto(
    val convertFactor1: Int,
    val convertFactor2: Int,
    val discountPercent: Int,
    val discount_Percent_PaymentTerm_Receipt: Long,
    val discount_Percent_PaymentTerm_Receipt_Tax: Long,
    val discount_Percent_PaymentTerm_cheque: Long,
    val discount_Percent_PaymentTerm_cheque_Tax: Long,
    val discount_Percent_PaymentTerm_immediate: Long,
    val discount_Percent_PaymentTerm_immediate_Tax: Long,
    val finalPrice: Long,
    val finalPriceDiscount: Long,
    val fullNameKala1: String,
    val fullNameKala2: String,
    val id: String,
    val isTax: Boolean,
    val paymentTermId: String?,
    val price: Long,
    val priceByDiscountPercent: Long,
    val priceByDiscountPercentAndTax: Long,
    val priceByDiscountPercentAndTax_Receipt: Long,
    val priceByDiscountPercentAndTax_cheque: Long,
    val priceByDiscountPercentAndTax_immediate: Long,
    val priceDiscount: Long,
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
    val discount: Long,
    val id: String,
    val price: Long,
    val productCode: String,
    val productImageUrl: String,
    val productName: String,
    val quantity: Int,
    val salesDate: String,
    val statusName: String,
    val tax: Long,
    val total: Long
)

data class CartSimulateResponse(
    val paymentTermId: UUID,
    val paymentTermName: String,
    val paymentKind: String,

    val subtotal: Long,
    val productDiscountTotal: Long,
    val paymentDiscountTotal: Long,
    val discountTotal: Long,
    val taxableAmount: Long,
    val taxTotal: Long,
    val total: Long,

    val items: List<CartSimulateLineResponse>
)

data class CartSimulateLineResponse(
    val productId: UUID,
    val productCode: Int,
    val productName: String,
    val quantity: Int,

    val unitPrice: Long,
    val gross: Long,

    val productDiscountPercent: Int,
    val productDiscount: Long,

    val paymentDiscountPercent: Int,
    val paymentDiscount: Long,

    val taxableAmount: Long,
    val tax: Long,
    val total: Long
)