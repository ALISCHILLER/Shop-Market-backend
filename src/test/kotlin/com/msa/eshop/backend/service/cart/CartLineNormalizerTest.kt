package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.dtos.SimulateModelRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import com.msa.eshop.backend.common.dtos.CartSimulateLineRequest

class CartLineNormalizerTest {

    private val normalizer = CartLineNormalizer()

    @Test
    fun `extractSimulateHeader should return shared payment term id`() {
        val requests = listOf(
            SimulateModelRequest(
                paymentTermId = "11111111-1111-1111-1111-111111111111",
                productCode = 1001,
                quantity = 1
            ),
            SimulateModelRequest(
                paymentTermId = "11111111-1111-1111-1111-111111111111",
                productCode = 1002,
                quantity = 2
            )
        )

        val header = normalizer.extractSimulateHeader(requests)

        assertEquals(
            "11111111-1111-1111-1111-111111111111",
            header.paymentTermId
        )
    }

    @Test
    fun `extractSimulateHeader should return null when payment term id is not provided`() {
        val requests = listOf(
            SimulateModelRequest(
                paymentTermId = null,
                productCode = 1001,
                quantity = 1
            ),
            SimulateModelRequest(
                paymentTermId = null,
                productCode = 1002,
                quantity = 2
            )
        )

        val header = normalizer.extractSimulateHeader(requests)

        assertNull(header.paymentTermId)
    }

    @Test
    fun `extractSimulateHeader should reject different payment term ids`() {
        val requests = listOf(
            SimulateModelRequest(
                paymentTermId = "11111111-1111-1111-1111-111111111111",
                productCode = 1001,
                quantity = 1
            ),
            SimulateModelRequest(
                paymentTermId = "22222222-2222-2222-2222-222222222222",
                productCode = 1002,
                quantity = 2
            )
        )

        assertThrows(BadRequestException::class.java) {
            normalizer.extractSimulateHeader(requests)
        }
    }

    @Test
    fun `normalizeSimulateLines should merge duplicate product codes`() {
        val requests = listOf(
            SimulateModelRequest(
                paymentTermId = null,
                productCode = 1001,
                quantity = 1
            ),
            SimulateModelRequest(
                paymentTermId = null,
                productCode = 1001,
                quantity = 2
            )
        )

        val lines = normalizer.normalizeSimulateLines(requests)

        assertEquals(1, lines.size)
        assertEquals(1001, lines.first().productCode)
        assertEquals(3, lines.first().quantity)
    }

    @Test
    fun `normalizeSimulateLines should reject empty cart`() {
        assertThrows(BadRequestException::class.java) {
            normalizer.normalizeSimulateLines(emptyList())
        }
    }

    @Test
    fun `normalizeSimulateLines should reject invalid quantity`() {
        val requests = listOf(
            SimulateModelRequest(
                paymentTermId = null,
                productCode = 1001,
                quantity = 0
            )
        )

        assertThrows(BadRequestException::class.java) {
            normalizer.normalizeSimulateLines(requests)
        }
    }

    @Test
    fun `normalizeSimulateLines should reject invalid product code`() {
        val requests = listOf(
            SimulateModelRequest(
                paymentTermId = null,
                productCode = 0,
                quantity = 1
            )
        )

        assertThrows(BadRequestException::class.java) {
            normalizer.normalizeSimulateLines(requests)
        }
    }
    @Test
    fun `normalizeModernLines should merge duplicate product codes`() {
        val lines = listOf(
            CartSimulateLineRequest(productCode = 1001, quantity = 1),
            CartSimulateLineRequest(productCode = 1001, quantity = 2),
            CartSimulateLineRequest(productCode = 1002, quantity = 1)
        )

        val result = normalizer.normalizeModernLines(lines)

        assertEquals(2, result.size)
        assertEquals(1001, result[0].productCode)
        assertEquals(3, result[0].quantity)
        assertEquals(1002, result[1].productCode)
        assertEquals(1, result[1].quantity)
    }
}