package com.msa.eshop.backend.service.pricing

import com.msa.eshop.backend.domain.entity.PaymentKind
import com.msa.eshop.backend.domain.entity.PaymentTerm
import org.springframework.stereotype.Component

@Component
class PaymentKindResolver {
    fun resolve(paymentTerm: PaymentTerm): PaymentKind {
        val normalizedName = paymentTerm.name
            .trim()
            .lowercase()

        return when {
            paymentTerm.deadLine <= 0 -> PaymentKind.IMMEDIATE
            normalizedName.contains("نقد") -> PaymentKind.IMMEDIATE
            normalizedName.contains("cash") -> PaymentKind.IMMEDIATE
            normalizedName.contains("چک") -> PaymentKind.CHEQUE
            normalizedName.contains("cheque") -> PaymentKind.CHEQUE
            normalizedName.contains("check") -> PaymentKind.CHEQUE
            else -> PaymentKind.RECEIPT
        }
    }
}