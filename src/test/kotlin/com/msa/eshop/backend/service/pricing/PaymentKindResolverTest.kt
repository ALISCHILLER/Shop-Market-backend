package com.msa.eshop.backend.service.pricing

import com.msa.eshop.backend.domain.entity.PaymentKind
import com.msa.eshop.backend.domain.entity.PaymentTerm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentKindResolverTest {

    private val resolver = PaymentKindResolver()

    @Test
    fun `resolve should use persisted payment kind immediate`() {
        val term = PaymentTerm(
            name = "هر نامی",
            deadLine = 30,
            paymentKind = PaymentKind.IMMEDIATE
        )

        assertEquals(PaymentKind.IMMEDIATE, resolver.resolve(term))
    }

    @Test
    fun `resolve should use persisted payment kind receipt`() {
        val term = PaymentTerm(
            name = "نقدی ولی نوع رسید است",
            deadLine = 30,
            paymentKind = PaymentKind.RECEIPT
        )

        assertEquals(PaymentKind.RECEIPT, resolver.resolve(term))
    }

    @Test
    fun `resolve should use persisted payment kind cheque`() {
        val term = PaymentTerm(
            name = "رسید ولی نوع چک است",
            deadLine = 60,
            paymentKind = PaymentKind.CHEQUE
        )

        assertEquals(PaymentKind.CHEQUE, resolver.resolve(term))
    }
}