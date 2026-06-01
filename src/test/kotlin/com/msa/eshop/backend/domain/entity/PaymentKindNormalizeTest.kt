package com.msa.eshop.backend.domain.entity

import com.msa.eshop.backend.common.BadRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PaymentKindNormalizeTest {

    @Test
    fun `normalize should accept enum names`() {
        assertEquals(PaymentKind.IMMEDIATE, PaymentKind.normalize("IMMEDIATE"))
        assertEquals(PaymentKind.RECEIPT, PaymentKind.normalize("RECEIPT"))
        assertEquals(PaymentKind.CHEQUE, PaymentKind.normalize("CHEQUE"))
    }

    @Test
    fun `normalize should accept persian aliases`() {
        assertEquals(PaymentKind.IMMEDIATE, PaymentKind.normalize("نقدی"))
        assertEquals(PaymentKind.RECEIPT, PaymentKind.normalize("رسید"))
        assertEquals(PaymentKind.CHEQUE, PaymentKind.normalize("چک"))
    }

    @Test
    fun `normalize should reject invalid value`() {
        assertThrows(BadRequestException::class.java) {
            PaymentKind.normalize("invalid")
        }
    }
}