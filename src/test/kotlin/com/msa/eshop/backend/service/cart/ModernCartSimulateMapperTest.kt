package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.Money
import com.msa.eshop.backend.domain.entity.PaymentKind
import com.msa.eshop.backend.domain.entity.PaymentTerm
import com.msa.eshop.backend.domain.entity.Product
import com.msa.eshop.backend.service.PriceLine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class ModernCartSimulateMapperTest {

    @Test
    fun `toCartSimulateResponse should map pricing result correctly`() {
        val paymentTermId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        val paymentTerm = PaymentTerm(
            name = "Receipt",
            deadLine = 30
        ).apply {
            id = paymentTermId
        }

        val product = Product(
            productName = "Product",
            productCode = 1001,
            price = 100_000L
        ).apply {
            id = productId
        }

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

        val result = CartPricingResult(
            paymentTerm = paymentTerm,
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

        val response = result.toCartSimulateResponse()

        assertEquals(paymentTermId, response.paymentTermId)
        assertEquals("Receipt", response.paymentTermName)
        assertEquals("RECEIPT", response.paymentKind)
        assertEquals(200_000L, response.subtotal)
        assertEquals(29_000L, response.discountTotal)
        assertEquals(15_390L, response.taxTotal)
        assertEquals(186_390L, response.total)

        assertEquals(1, response.items.size)
        assertEquals(productId, response.items.first().productId)
        assertEquals(1001, response.items.first().productCode)
    }
}