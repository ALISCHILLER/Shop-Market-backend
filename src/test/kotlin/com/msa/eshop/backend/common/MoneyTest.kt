package com.msa.eshop.backend.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MoneyTest {

    @Test
    fun `of should reject negative value`() {
        assertThrows(BadRequestException::class.java) {
            Money.of(-1)
        }
    }

    @Test
    fun `multiply should calculate unit price by quantity`() {
        val result = Money.multiply(10_000, 3)

        assertEquals(30_000, result.value)
    }

    @Test
    fun `multiply should reject zero quantity`() {
        assertThrows(BadRequestException::class.java) {
            Money.multiply(10_000, 0)
        }
    }

    @Test
    fun `percent should calculate rounded percentage`() {
        val result = Money(10_000).percent(9)

        assertEquals(900, result.value)
    }

    @Test
    fun `minus should reject negative result`() {
        assertThrows(BadRequestException::class.java) {
            Money(100) - Money(200)
        }
    }
}