package com.msa.eshop.backend.service.pricing

import com.msa.eshop.backend.domain.entity.PaymentKind
import com.msa.eshop.backend.domain.entity.PaymentTerm
import org.springframework.stereotype.Component

@Component
class PaymentKindResolver {

    fun resolve(paymentTerm: PaymentTerm): PaymentKind =
        paymentTerm.paymentKind
}