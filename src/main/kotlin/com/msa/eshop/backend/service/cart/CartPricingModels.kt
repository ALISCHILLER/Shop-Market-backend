package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.Money
import com.msa.eshop.backend.domain.entity.PaymentKind
import com.msa.eshop.backend.domain.entity.PaymentTerm
import com.msa.eshop.backend.service.PriceLine
import java.util.UUID

data class CartPricingRequest(
    val paymentTermId: UUID,
    val lines: List<NormalizedCartLine>
)

data class CartPricingResult(
    val paymentTerm: PaymentTerm,
    val paymentKind: PaymentKind,
    val priceLines: List<PriceLine>,

    val subtotal: Money,

    val productDiscountTotal: Money,
    val paymentDiscountTotal: Money,
    val discountTotal: Money,

    val taxableAmount: Money,
    val taxTotal: Money,
    val total: Money
)