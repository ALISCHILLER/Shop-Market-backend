package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.dtos.CartSimulateLineResponse
import com.msa.eshop.backend.common.dtos.CartSimulateResponse

fun CartPricingResult.toCartSimulateResponse(): CartSimulateResponse =
    CartSimulateResponse(
        paymentTermId = requireNotNull(paymentTerm.id),
        paymentTermName = paymentTerm.name,
        paymentKind = paymentKind.name,
        paymentKindTitle = paymentKind.title,

        subtotal = subtotal.toPersistedLong(),
        productDiscountTotal = productDiscountTotal.toPersistedLong(),
        paymentDiscountTotal = paymentDiscountTotal.toPersistedLong(),
        discountTotal = discountTotal.toPersistedLong(),
        taxableAmount = taxableAmount.toPersistedLong(),
        taxTotal = taxTotal.toPersistedLong(),
        total = total.toPersistedLong(),

        items = priceLines.map { line ->
            CartSimulateLineResponse(
                productId = requireNotNull(line.product.id),
                productCode = line.product.productCode,
                productName = line.product.productName.orEmpty(),
                quantity = line.quantity,
                unitPrice = line.product.price,
                gross = line.gross.toPersistedLong(),
                productDiscountPercent = line.productDiscountPercent,
                productDiscount = line.productDiscount.toPersistedLong(),
                paymentDiscountPercent = line.paymentDiscountPercent,
                paymentDiscount = line.paymentDiscount.toPersistedLong(),
                taxableAmount = line.taxableAmount.toPersistedLong(),
                tax = line.tax.toPersistedLong(),
                total = line.total.toPersistedLong()
            )
        }
    )