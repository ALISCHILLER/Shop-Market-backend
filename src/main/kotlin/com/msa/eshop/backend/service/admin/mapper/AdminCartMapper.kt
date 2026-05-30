package com.msa.eshop.backend.service.admin.mapper


import com.msa.eshop.backend.common.dtos.AdminCartSummaryDto
import com.msa.eshop.backend.common.dtos.ReportCartDetailsDto
import com.msa.eshop.backend.domain.Cart
import com.msa.eshop.backend.domain.CartItem
import com.msa.eshop.backend.domain.CartStatus
import org.springframework.stereotype.Component

@Component
class AdminCartMapper {
    fun toSummaryDto(
        cart: Cart,
        itemCount: Int
    ): AdminCartSummaryDto {
        val status = CartStatus.normalize(
            cart.statusCode.ifBlank { cart.statusName }
        )

        return AdminCartSummaryDto(
            id = requireNotNull(cart.id).toString(),
            cartCode = cart.cartCode,
            customerId = cart.customer?.id?.toString(),
            customerCode = cart.customer?.customerCode.orEmpty(),
            customerName = cart.customerNameSnapshot,
            customerAddress = cart.customerAddressSnapshot,
            paymentTermId = cart.paymentTerm?.id?.toString(),
            paymentTermName = cart.paymentTerm?.name.orEmpty(),
            statusCode = status.code,
            statusName = cart.statusName.ifBlank { status.title },
            statusColor = cart.statusColor.ifBlank { status.color },
            salesDate = cart.salesDate.toString(),
            subtotal = cart.subtotal,
            discountTotal = cart.discountTotal,
            taxTotal = cart.taxTotal,
            total = cart.total,
            itemCount = itemCount,
            createdAt = cart.createdAt.toString()
        )
    }

    fun toDetailsDto(
        cart: Cart,
        item: CartItem
    ): ReportCartDetailsDto =
        ReportCartDetailsDto(
            id = requireNotNull(item.id).toString(),
            cartCode = cart.cartCode,
            customerAddress = cart.customerAddressSnapshot,
            customerName = cart.customerNameSnapshot,
            discount = item.discount,
            price = item.price,
            productCode = item.productCode.toString(),
            productImageUrl = item.productImageUrl.orEmpty(),
            productName = item.productName,
            quantity = item.quantity,
            salesDate = cart.salesDate.toString(),
            statusName = cart.statusName,
            tax = item.tax,
            total = item.total
        )
}