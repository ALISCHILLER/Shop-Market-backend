package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.Money
import com.msa.eshop.backend.domain.entity.PaymentTerm
import com.msa.eshop.backend.domain.repository.PaymentTermRepository
import com.msa.eshop.backend.service.PricingRequest
import com.msa.eshop.backend.service.PricingService
import com.msa.eshop.backend.service.catalog.ProductResolver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CartPricingCalculator(
    private val paymentTermRepository: PaymentTermRepository,
    private val productResolver: ProductResolver,
    private val pricingService: PricingService,
) {

    @Transactional(readOnly = true)
    fun calculate(request: CartPricingRequest): CartPricingResult {
        if (request.lines.isEmpty()) {
            throw BadRequestException("سبد خرید خالی است")
        }

        val paymentTerm = resolvePaymentTerm(request.paymentTermId)
        val paymentKind = paymentTerm.paymentKind

        val productsByCode = productResolver.requireByCodes(
            request.lines.map { it.productCode }
        )

        val pricingRequests = request.lines.map { line ->
            PricingRequest(
                product = productsByCode.getValue(line.productCode),
                quantity = line.quantity,
                paymentKind = paymentKind
            )
        }

        val priceLines = pricingService.calculateBatch(
            requests = pricingRequests,
            paymentTerm = paymentTerm,
            paymentKind = paymentKind
        )

        val subtotal = priceLines.fold(Money.zero()) { acc, line ->
            acc + line.gross
        }

        val productDiscountTotal = priceLines.fold(Money.zero()) { acc, line ->
            acc + line.productDiscount
        }

        val paymentDiscountTotal = priceLines.fold(Money.zero()) { acc, line ->
            acc + line.paymentDiscount
        }

        val discountTotal = productDiscountTotal + paymentDiscountTotal

        val taxableAmount = priceLines.fold(Money.zero()) { acc, line ->
            acc + line.taxableAmount
        }

        val taxTotal = priceLines.fold(Money.zero()) { acc, line ->
            acc + line.tax
        }

        val total = priceLines.fold(Money.zero()) { acc, line ->
            acc + line.total
        }

        return CartPricingResult(
            paymentTerm = paymentTerm,
            paymentKind = paymentKind,
            priceLines = priceLines,
            subtotal = subtotal,
            productDiscountTotal = productDiscountTotal,
            paymentDiscountTotal = paymentDiscountTotal,
            discountTotal = discountTotal,
            taxableAmount = taxableAmount,
            taxTotal = taxTotal,
            total = total
        )
    }

    private fun resolvePaymentTerm(paymentTermId: UUID): PaymentTerm {
        return paymentTermRepository.findByIdAndActiveTrue(paymentTermId)
            ?: throw BadRequestException("روش پرداخت انتخاب‌شده معتبر یا فعال نیست")
    }
}