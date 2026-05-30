package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.Money
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

        val subtotal = priceLines.fold(Money.zero()) { acc, line -> acc + line.gross }
        val discountTotal = priceLines.fold(Money.zero()) { acc, line -> acc + line.totalDiscount }
        val taxTotal = priceLines.fold(Money.zero()) { acc, line -> acc + line.tax }
        val total = priceLines.fold(Money.zero()) { acc, line -> acc + line.total }

        val cart = Cart(
            cartCode = cartCode,
            customer = customer,
            address = address,
            paymentTerm = paymentTerm,
            customerNameSnapshot = customer.customerName.ifBlank { customer.customerCode },
            customerAddressSnapshot = address.customerAddress,
            statusName = status.title,
            statusColor = status.color,
            salesDate = LocalDate.now(),
            subtotal = subtotal.toPersistedInt(),
            discountTotal = discountTotal.toPersistedInt(),
            taxTotal = taxTotal.toPersistedInt(),
            total = total.toPersistedInt()
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
        }

        return cart
    }
}