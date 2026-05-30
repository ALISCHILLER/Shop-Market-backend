package com.msa.eshop.backend.common.dtos

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

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