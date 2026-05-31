package com.msa.eshop.backend.service

import com.msa.eshop.backend.domain.Discount
import com.msa.eshop.backend.domain.DiscountRepository
import com.msa.eshop.backend.domain.PaymentKind
import com.msa.eshop.backend.domain.PaymentTerm
import com.msa.eshop.backend.domain.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class PricingServiceTest {

    private val discountRepository = Mockito.mock(DiscountRepository::class.java)
    private val pricingService = PricingService(
        discountRepository = discountRepository,
        taxPercent = 9
    )

    @Test
    fun `calculate should apply product discount payment discount and tax`() {
        val productId = UUID.randomUUID()

        val product = Product(
            productName = "Test Product",
            productCode = 1001,
            price = 100_000,
            isDiscounts = true,
            isTax = true
        ).apply {
            id = productId
        }

        val discount = Discount(
            product = product,
            discountPercent = 10,
            fromNumber = 1,
            endNumber = 10
        )

        val paymentTerm = PaymentTerm(
            name = "رسیدی",
            deadLine = 30,
            receiptDiscountPercent = 5,
            active = true
        )

        Mockito.`when`(discountRepository.findByProductId(productId))
            .thenReturn(listOf(discount))

        val result = pricingService.calculate(
            product = product,
            quantity = 2,
            paymentTerm = paymentTerm,
            paymentKind = PaymentKind.RECEIPT
        )

        assertEquals(200_000L, result.gross.value)
        assertEquals(10, result.productDiscountPercent)
        assertEquals(20_000L, result.productDiscount.value)
        assertEquals(180_000L, result.afterProductDiscount.value)
        assertEquals(5, result.paymentDiscountPercent)
        assertEquals(9_000L, result.paymentDiscount.value)
        assertEquals(171_000L, result.taxableAmount.value)
        assertEquals(15_390L, result.tax.value)
        assertEquals(186_390L, result.total.value)
    }

    @Test
    fun `calculate should not apply tax when product is not taxable`() {
        val productId = UUID.randomUUID()

        val product = Product(
            productName = "No Tax Product",
            productCode = 1002,
            price = 100_000L,
            isDiscounts = false,
            isTax = false
        ).apply {
            id = productId
        }

        Mockito.`when`(discountRepository.findByProductId(productId))
            .thenReturn(emptyList())

        val result = pricingService.calculate(
            product = product,
            quantity = 1,
            paymentTerm = null,
            paymentKind = PaymentKind.RECEIPT
        )

        assertEquals(100_000L, result.gross.value)
        assertEquals(0L, result.tax.value)
        assertEquals(100_000L, result.total.value)
    }
}