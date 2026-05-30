package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.domain.Cart
import com.msa.eshop.backend.domain.CartItem
import com.msa.eshop.backend.domain.CartStatus
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.domain.CustomerAddress
import com.msa.eshop.backend.domain.PaymentTerm
import com.msa.eshop.backend.service.PriceLine
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CartAssembler {
    fun assemble(
        cartCode: Int,
        customer: Customer,
        address: CustomerAddress,
        paymentTerm: PaymentTerm,
        priceLines: List<PriceLine>
    ): Cart {
        if (priceLines.isEmpty()) {
            throw BadRequestException("سبد خرید خالی است")
        }

        val status = CartStatus.REGISTERED

        val cart = Cart(
            cartCode = cartCode,
            customer = customer,
            address = address,
            paymentTerm = paymentTerm,
            customerNameSnapshot = customer.customerName.ifBlank { customer.customerCode },
            customerAddressSnapshot = address.customerAddress,
            statusName = status.title,
            statusColor = status.color,
            salesDate = LocalDate.now()
        )

        priceLines.forEach { line ->
            cart.addItem(
                CartItem(
                    product = line.product,
                    productCode = line.product.productCode,
                    productName = line.product.productName.orEmpty(),
                    productImageUrl = line.product.productImage,
                    quantity = line.quantity,
                    price = line.product.price,
                    discount = line.totalDiscount.toPersistedInt(),
                    tax = line.tax.toPersistedInt(),
                    total = line.total.toPersistedInt()
                )
            )

            cart.subtotal = safePlus(
                current = cart.subtotal,
                value = line.gross.toPersistedInt(),
                message = "جمع مبلغ سفارش بیش از حد مجاز است"
            )

            cart.discountTotal = safePlus(
                current = cart.discountTotal,
                value = line.totalDiscount.toPersistedInt(),
                message = "جمع تخفیف سفارش بیش از حد مجاز است"
            )

            cart.taxTotal = safePlus(
                current = cart.taxTotal,
                value = line.tax.toPersistedInt(),
                message = "جمع مالیات سفارش بیش از حد مجاز است"
            )

            cart.total = safePlus(
                current = cart.total,
                value = line.total.toPersistedInt(),
                message = "جمع نهایی سفارش بیش از حد مجاز است"
            )
        }

        return cart
    }

    private fun safePlus(current: Int, value: Int, message: String): Int {
        val result = current.toLong() + value.toLong()

        if (result > Int.MAX_VALUE) {
            throw BadRequestException(message)
        }

        return result.toInt()
    }
}