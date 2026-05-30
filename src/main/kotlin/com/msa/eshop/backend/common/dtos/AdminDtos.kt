package com.msa.eshop.backend.common.dtos

import jakarta.validation.constraints.NotBlank

data class DashboardDto(
    val customers: Long,
    val products: Long,
    val categories: Long,
    val carts: Long,
    val revenue: Long
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
    val statusCode: String,
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
    @field:NotBlank(message = "وضعیت سفارش الزامی است")
    val status: String,

    val color: String? = null
)

data class AdminCustomerSearchRequest(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null,
    val role: String? = null,
    val enabled: Boolean? = null
)

data class AdminProductSearchRequest(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null,
    val productGroupCode: Int? = null,
    val isDiscounts: Boolean? = null,
    val isTax: Boolean? = null
)