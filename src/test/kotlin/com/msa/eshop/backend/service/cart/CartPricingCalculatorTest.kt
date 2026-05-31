package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.domain.DiscountRepository
import com.msa.eshop.backend.domain.PaymentKind
import com.msa.eshop.backend.domain.PaymentTerm
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.domain.ProductRepository
import com.msa.eshop.backend.service.PricingService
import com.msa.eshop.backend.service.catalog.ProductResolver
import com.msa.eshop.backend.service.pricing.PaymentKindResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import com.msa.eshop.backend.common.BadRequestException
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.UUID

class CartPricingCalculatorTest {

    private val paymentTermRepository = Mockito.mock(PaymentTermRepository::class.java)
    private val productRepository = Mockito.mock(ProductRepository::class.java)
    private val discountRepository = Mockito.mock(DiscountRepository::class.java)

    private val productResolver = ProductResolver(productRepository)
    private val pricingService = PricingService(discountRepository, 9)
    private val paymentKindResolver = PaymentKindResolver()

    private val calculator = CartPricingCalculator(
        paymentTermRepository = paymentTermRepository,
        productResolver = productResolver,
        pricingService = pricingService,
        paymentKindResolver = paymentKindResolver
    )

    @Test
    fun `calculate should return aggregated totals`() {
        val paymentTermId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        val paymentTerm = PaymentTerm(
            name = "رسیدی",
            deadLine = 30,
            receiptDiscountPercent = 5,
            active = true
        ).apply {
            id = paymentTermId
        }

        val product = Product(
            productName = "Product",
            productCode = 1001,
            price = 100_000L,
            isDiscounts = false,
            isTax = true
        ).apply {
            id = productId
        }

        Mockito.`when`(paymentTermRepository.findByIdAndActiveTrue(paymentTermId))
            .thenReturn(paymentTerm)

        Mockito.`when`(productRepository.findByProductCodeIn(setOf(1001)))
            .thenReturn(listOf(product))

        Mockito.`when`(discountRepository.findByProductIdIn(setOf(productId)))
            .thenReturn(emptyList())

        val result = calculator.calculate(
            CartPricingRequest(
                paymentTermId = paymentTermId,
                lines = listOf(
                    NormalizedCartLine(
                        productCode = 1001,
                        quantity = 2
                    )
                )
            )
        )

        assertEquals(paymentTermId, result.paymentTerm.id)
        assertEquals(PaymentKind.RECEIPT, result.paymentKind)

        assertEquals(200_000L, result.subtotal.value)
        assertEquals(0L, result.productDiscountTotal.value)
        assertEquals(10_000L, result.paymentDiscountTotal.value)
        assertEquals(10_000L, result.discountTotal.value)
        assertEquals(190_000L, result.taxableAmount.value)
        assertEquals(17_100L, result.taxTotal.value)
        assertEquals(207_100L, result.total.value)
    }

    @Test
    fun `calculate should reject inactive or missing payment term`() {
        val paymentTermId = UUID.randomUUID()

        Mockito.`when`(paymentTermRepository.findByIdAndActiveTrue(paymentTermId))
            .thenReturn(null)

        assertThrows(BadRequestException::class.java) {
            calculator.calculate(
                CartPricingRequest(
                    paymentTermId = paymentTermId,
                    lines = listOf(
                        NormalizedCartLine(
                            productCode = 1001,
                            quantity = 1
                        )
                    )
                )
            )
        }
    }
}