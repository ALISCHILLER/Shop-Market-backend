package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.Money
import com.msa.eshop.backend.domain.entity.Customer
import com.msa.eshop.backend.domain.entity.CustomerAddress
import com.msa.eshop.backend.domain.entity.PaymentKind
import com.msa.eshop.backend.domain.entity.PaymentTerm
import com.msa.eshop.backend.domain.entity.Product
import com.msa.eshop.backend.service.PriceLine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CartPricingConsistencyTest {

    private val assembler = CartAssembler()

    @Test
    fun `cart assembler should persist exactly same totals as pricing result`() {
        val pricingResult = createPricingResult()

        val cart = assembler.assemble(
            cartCode = 100001,
            customer = Customer(
                customerCode = "C001",
                customerName = "Customer"
            ),
            address = CustomerAddress(
                customerAddress = "Address"
            ),
            pricingResult = pricingResult
        )

        assertEquals(pricingResult.subtotal.toPersistedLong(), cart.subtotal)
        assertEquals(pricingResult.discountTotal.toPersistedLong(), cart.discountTotal)
        assertEquals(pricingResult.taxTotal.toPersistedLong(), cart.taxTotal)
        assertEquals(pricingResult.total.toPersistedLong(), cart.total)
    }

    private fun createPricingResult(): CartPricingResult {
        val product = Product(
            productName = "Product",
            productCode = 1001,
            price = 100_000L,
            isTax = true
        )

        val line = PriceLine(
            product = product,
            quantity = 2,
            gross = Money(200_000),
            productDiscountPercent = 10,
            productDiscount = Money(20_000),
            afterProductDiscount = Money(180_000),
            paymentDiscountPercent = 5,
            paymentDiscount = Money(9_000),
            taxableAmount = Money(171_000),
            tax = Money(15_390),
            taxWithoutPaymentDiscount = Money(16_200),
            total = Money(186_390)
        )

        return CartPricingResult(
            paymentTerm = PaymentTerm(name = "Receipt"),
            paymentKind = PaymentKind.RECEIPT,
            priceLines = listOf(line),

            subtotal = Money(200_000),

            productDiscountTotal = Money(20_000),
            paymentDiscountTotal = Money(9_000),
            discountTotal = Money(29_000),

            taxableAmount = Money(171_000),
            taxTotal = Money(15_390),
            total = Money(186_390)
        )
    }
}