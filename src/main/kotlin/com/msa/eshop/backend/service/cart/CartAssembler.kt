package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.domain.Cart
import com.msa.eshop.backend.domain.CartItem
import com.msa.eshop.backend.domain.CartStatus
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.domain.CustomerAddress
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CartAssembler {

    fun assemble(
        cartCode: Int,
        customer: Customer,
        address: CustomerAddress,
        pricingResult: CartPricingResult
    ): Cart {
        if (pricingResult.priceLines.isEmpty()) {
            throw BadRequestException("سبد خرید خالی است")
        }

        val paymentTerm = pricingResult.paymentTerm
            ?: throw BadRequestException("روش پرداخت برای ثبت سفارش الزامی است")

        val cart = Cart(
            cartCode = cartCode,
            customer = customer,
            address = address,
            paymentTerm = paymentTerm,
            customerNameSnapshot = customer.customerName.ifBlank { customer.customerCode },
            customerAddressSnapshot = address.customerAddress,
            salesDate = LocalDate.now(),
            subtotal = pricingResult.subtotal.toPersistedLong(),
            discountTotal = pricingResult.discountTotal.toPersistedLong(),
            taxTotal = pricingResult.taxTotal.toPersistedLong(),
            total = pricingResult.total.toPersistedLong()
        )

        cart.applyStatus(CartStatus.REGISTERED)

        pricingResult.priceLines.forEach { line ->
            cart.addItem(
                CartItem(
                    product = line.product,
                    productCode = line.product.productCode,
                    productName = line.product.productName.orEmpty(),
                    productImageUrl = line.product.productImage,
                    quantity = line.quantity,
                    price = line.product.price,
                    discount = line.totalDiscount.toPersistedLong(),
                    tax = line.tax.toPersistedLong(),
                    total = line.total.toPersistedLong()
                )
            )
        }

        return cart
    }
}