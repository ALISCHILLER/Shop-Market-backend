package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.dtos.CartLineRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CartLineNormalizerTest {

    private val normalizer = CartLineNormalizer()

    @Test
    fun `normalize should merge duplicate product codes`() {
        val result = normalizer.normalize(
            listOf(
                CartLineRequest(productCode = 1001, quantity = 1),
                CartLineRequest(productCode = 1001, quantity = 2),
                CartLineRequest(productCode = 1002, quantity = 1)
            )
        )

        assertEquals(2, result.size)

        assertEquals(1001, result[0].productCode)
        assertEquals(3, result[0].quantity)

        assertEquals(1002, result[1].productCode)
        assertEquals(1, result[1].quantity)
    }

    @Test
    fun `normalize should sort lines by product code`() {
        val result = normalizer.normalize(
            listOf(
                CartLineRequest(productCode = 2002, quantity = 1),
                CartLineRequest(productCode = 1001, quantity = 1),
                CartLineRequest(productCode = 3003, quantity = 1)
            )
        )

        assertEquals(listOf(1001, 2002, 3003), result.map { it.productCode })
    }

    @Test
    fun `normalize should reject empty cart`() {
        assertThrows(BadRequestException::class.java) {
            normalizer.normalize(emptyList())
        }
    }

    @Test
    fun `normalize should reject invalid product code`() {
        assertThrows(BadRequestException::class.java) {
            normalizer.normalize(
                listOf(
                    CartLineRequest(productCode = 0, quantity = 1)
                )
            )
        }
    }

    @Test
    fun `normalize should reject zero quantity`() {
        assertThrows(BadRequestException::class.java) {
            normalizer.normalize(
                listOf(
                    CartLineRequest(productCode = 1001, quantity = 0)
                )
            )
        }
    }

    @Test
    fun `normalize should reject negative quantity`() {
        assertThrows(BadRequestException::class.java) {
            normalizer.normalize(
                listOf(
                    CartLineRequest(productCode = 1001, quantity = -1)
                )
            )
        }
    }

    @Test
    fun `normalize should reject quantity greater than max per product`() {
        assertThrows(BadRequestException::class.java) {
            normalizer.normalize(
                listOf(
                    CartLineRequest(productCode = 1001, quantity = 1_001)
                )
            )
        }
    }

    @Test
    fun `normalize should reject merged quantity greater than max per product`() {
        assertThrows(BadRequestException::class.java) {
            normalizer.normalize(
                listOf(
                    CartLineRequest(productCode = 1001, quantity = 600),
                    CartLineRequest(productCode = 1001, quantity = 500)
                )
            )
        }
    }

    @Test
    fun `normalize should reject more than max cart lines`() {
        val lines = (1..101).map { index ->
            CartLineRequest(
                productCode = index,
                quantity = 1
            )
        }

        assertThrows(BadRequestException::class.java) {
            normalizer.normalize(lines)
        }
    }
}