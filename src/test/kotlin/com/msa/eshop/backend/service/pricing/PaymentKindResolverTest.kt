package com.msa.eshop.backend.service.pricing

import com.msa.eshop.backend.domain.PaymentKind
import com.msa.eshop.backend.domain.PaymentTerm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentKindResolverTest {

    private val resolver = PaymentKindResolver()

    @Test
    fun `deadLine zero should resolve to immediate`() {
        val term = PaymentTerm(
            name = "پرداخت فوری",
            deadLine = 0
        )

        assertEquals(PaymentKind.IMMEDIATE, resolver.resolve(term))
    }

    @Test
    fun `cash name should resolve to immediate`() {
        val term = PaymentTerm(
            name = "cash payment",
            deadLine = 10
        )

        assertEquals(PaymentKind.IMMEDIATE, resolver.resolve(term))
    }

    @Test
    fun `cheque name should resolve to cheque`() {
        val term = PaymentTerm(
            name = "پرداخت چکی",
            deadLine = 30
        )

        assertEquals(PaymentKind.CHEQUE, resolver.resolve(term))
    }

    @Test
    fun `unknown deferred payment should resolve to receipt`() {
        val term = PaymentTerm(
            name = "پرداخت اعتباری",
            deadLine = 30
        )

        assertEquals(PaymentKind.RECEIPT, resolver.resolve(term))
    }
}