package com.msa.eshop.backend.common.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class CartLineRequest(
    @field:Min(value = 1, message = "کد کالا معتبر نیست")
    val productCode: Int,

    @field:Min(value = 1, message = "تعداد کالا باید بزرگ‌تر از صفر باشد")
    @field:Max(value = 1_000, message = "تعداد هر کالا نمی‌تواند بیشتر از ۱۰۰۰ باشد")
    val quantity: Int
)

data class CartSimulateRequest(
    @field:NotNull(message = "شناسه روش پرداخت الزامی است")
    val paymentTermId: UUID,

    @field:Valid
    @field:NotEmpty(message = "لیست کالاها نمی‌تواند خالی باشد")
    @field:Size(max = 100, message = "تعداد ردیف‌های سبد خرید نمی‌تواند بیشتر از ۱۰۰ باشد")
    val items: List<CartLineRequest>
)

data class CartCheckoutRequest(
    @field:NotNull(message = "شناسه آدرس الزامی است")
    val customerAddressId: UUID,

    @field:NotNull(message = "شناسه روش پرداخت الزامی است")
    val paymentTermId: UUID,

    @field:Valid
    @field:NotEmpty(message = "لیست کالاها نمی‌تواند خالی باشد")
    @field:Size(max = 100, message = "تعداد ردیف‌های سبد خرید نمی‌تواند بیشتر از ۱۰۰ باشد")
    val items: List<CartLineRequest>
)

data class CartCheckoutResponse(
    val cartId: UUID,
    val cartCode: Int,
    val statusCode: String,
    val statusName: String,
    val subtotal: Long,
    val discountTotal: Long,
    val taxTotal: Long,
    val total: Long
)

data class CartSimulateResponse(
    val paymentTermId: UUID,
    val paymentTermName: String,
    val paymentKind: String,
    val paymentKindTitle: String,

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

data class CartHistoryDto(
    val id: UUID,
    val cartCode: Int,
    val customerId: UUID?,
    val customerCode: String,
    val customerName: String,
    val salesDate: String,
    val statusCode: String,
    val statusName: String,
    val statusColor: String,
    val subtotal: Long,
    val discountTotal: Long,
    val taxTotal: Long,
    val total: Long
)

data class CartDetailsDto(
    val id: UUID,
    val cartCode: Int,
    val customerId: UUID?,
    val customerCode: String,
    val customerName: String,
    val customerAddress: String,
    val paymentTermId: UUID?,
    val paymentTermName: String,
    val statusCode: String,
    val statusName: String,
    val statusColor: String,
    val salesDate: String,
    val subtotal: Long,
    val discountTotal: Long,
    val taxTotal: Long,
    val total: Long,
    val items: List<CartDetailsLineDto>
)

data class CartDetailsLineDto(
    val id: UUID,
    val productId: UUID?,
    val productCode: Int,
    val productName: String,
    val productImageUrl: String?,
    val quantity: Int,
    val unitPrice: Long,
    val discount: Long,
    val tax: Long,
    val total: Long
)