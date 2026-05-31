package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.domain.repository.DiscountRepository
import com.msa.eshop.backend.domain.entity.PaymentTerm
import com.msa.eshop.backend.domain.entity.Product
import com.msa.eshop.backend.service.PricingRequest
import com.msa.eshop.backend.service.PricingService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class LegacyCartSimulateTest {

    private val discountRepository = Mockito.mock(DiscountRepository::class.java)
    private val pricingService = PricingService(
        discountRepository = discountRepository,
        taxPercent = 9
    )

    @Test
    fun `legacy simulate should calculate different totals for receipt cheque and immediate`() {
        val productId = UUID.randomUUID()

        val product = Product(
            productName = "Product",
            productCode = 1001,
            price = 100_000L,
            isDiscounts = false,
            isTax = true
        ).apply {
            id = productId
        }

        val paymentTerm = PaymentTerm(
            name = "Test Term",
            deadLine = 30,
            receiptDiscountPercent = 5,
            chequeDiscountPercent = 0,
            immediateDiscountPercent = 10,
            active = true
        )

        Mockito.`when`(discountRepository.findByProductIdIn(setOf(productId)))
            .thenReturn(emptyList())

        val result = pricingService.simulateBatch(
            requests = listOf(
                PricingRequest(
                    product = product,
                    quantity = 1
                )
            ),
            paymentTerm = paymentTerm
        ).first()

        assertTrue(
            result.priceByDiscountPercentAndTax_immediate < result.priceByDiscountPercentAndTax_Receipt
        )

        assertTrue(
            result.priceByDiscountPercentAndTax_Receipt < result.priceByDiscountPercentAndTax_cheque
        )
    }
}