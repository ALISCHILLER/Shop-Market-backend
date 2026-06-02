package com.msa.eshop.backend.service.admin.mapper

import com.msa.eshop.backend.common.dtos.AdminCartSummaryDto
import com.msa.eshop.backend.common.dtos.CartDetailsLineDto
import com.msa.eshop.backend.domain.entity.Cart
import com.msa.eshop.backend.domain.entity.CartItem
import com.msa.eshop.backend.domain.entity.CartStatus
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
        item: CartItem
    ): CartDetailsLineDto =
        CartDetailsLineDto(
            id = requireNotNull(item.id),
            productId = item.product?.id,
            productCode = item.productCode,
            productName = item.productName,
            productImageUrl = item.productImageUrl,
            quantity = item.quantity,
            unitPrice = item.price,
            discount = item.discount,
            tax = item.tax,
            total = item.total
        )
}