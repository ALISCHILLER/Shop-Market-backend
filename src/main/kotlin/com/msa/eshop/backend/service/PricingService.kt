package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.Money
import com.msa.eshop.backend.common.SimulateDto
import com.msa.eshop.backend.common.requirePercent
import com.msa.eshop.backend.domain.DiscountRepository
import com.msa.eshop.backend.domain.PaymentKind
import com.msa.eshop.backend.domain.PaymentTerm
import com.msa.eshop.backend.domain.Product
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PricingService(
    private val discountRepository: DiscountRepository,
    @Value("\${app.invoice.tax-percent:9}") private val taxPercent: Int
) {
    init {
        taxPercent.requirePercent("درصد مالیات معتبر نیست")
    }

    @Transactional(readOnly = true)
    fun calculate(
        product: Product,
        quantity: Int,
        paymentTerm: PaymentTerm?,
        paymentKind: PaymentKind = PaymentKind.RECEIPT
    ): PriceLine {
        val gross = Money.multiply(product.price, quantity)

        val productDiscountPercent = findBestProductDiscountPercent(product, quantity)
        val productDiscount = gross.percent(productDiscountPercent)

        val afterProductDiscount = gross - productDiscount

        val paymentDiscountPercent = resolvePaymentDiscountPercent(paymentTerm, paymentKind)
        val paymentDiscount = afterProductDiscount.percent(paymentDiscountPercent)

        val taxableAmount = afterProductDiscount - paymentDiscount
        val tax = if (product.isTax) taxableAmount.percent(taxPercent) else Money.zero()
        val total = taxableAmount + tax

        val taxWithoutPaymentDiscount =
            if (product.isTax) afterProductDiscount.percent(taxPercent) else Money.zero()

        return PriceLine(
            product = product,
            quantity = quantity,
            gross = gross,
            productDiscountPercent = productDiscountPercent,
            productDiscount = productDiscount,
            afterProductDiscount = afterProductDiscount,
            paymentDiscountPercent = paymentDiscountPercent,
            paymentDiscount = paymentDiscount,
            taxableAmount = taxableAmount,
            tax = tax,
            taxWithoutPaymentDiscount = taxWithoutPaymentDiscount,
            total = total
        )
    }

    @Transactional(readOnly = true)
    fun simulate(
        product: Product,
        quantity: Int,
        paymentTerm: PaymentTerm?
    ): SimulateDto {
        val receipt = calculate(product, quantity, paymentTerm, PaymentKind.RECEIPT)
        val immediate = calculate(product, quantity, paymentTerm, PaymentKind.IMMEDIATE)
        val cheque = calculate(product, quantity, paymentTerm, PaymentKind.CHEQUE)

        return SimulateDto(
            convertFactor1 = product.convertFactor1,
            convertFactor2 = product.convertFactor2,
            discountPercent = receipt.productDiscountPercent,

            discount_Percent_PaymentTerm_Receipt = receipt.paymentDiscount.toPersistedInt(),
            discount_Percent_PaymentTerm_Receipt_Tax = receipt.tax.toPersistedInt(),

            discount_Percent_PaymentTerm_cheque = cheque.paymentDiscount.toPersistedInt(),
            discount_Percent_PaymentTerm_cheque_Tax = cheque.tax.toPersistedInt(),

            discount_Percent_PaymentTerm_immediate = immediate.paymentDiscount.toPersistedInt(),
            discount_Percent_PaymentTerm_immediate_Tax = immediate.tax.toPersistedInt(),

            finalPrice = receipt.gross.toPersistedInt(),
            finalPriceDiscount = receipt.afterProductDiscount.toPersistedInt(),

            fullNameKala1 = product.fullNameKala1.orEmpty(),
            fullNameKala2 = product.fullNameKala2.orEmpty(),

            id = requireNotNull(product.id).toString(),
            isTax = product.isTax,
            paymentTermId = paymentTerm?.id?.toString(),

            price = product.price,
            priceByDiscountPercent = receipt.afterProductDiscount.toPersistedInt(),
            priceByDiscountPercentAndTax =
                (receipt.afterProductDiscount + receipt.taxWithoutPaymentDiscount).toPersistedInt(),

            priceByDiscountPercentAndTax_Receipt = receipt.total.toPersistedInt(),
            priceByDiscountPercentAndTax_cheque = cheque.total.toPersistedInt(),
            priceByDiscountPercentAndTax_immediate = immediate.total.toPersistedInt(),

            priceDiscount = receipt.productDiscount.toPersistedInt(),

            productCode = product.productCode,
            productGroupCode = product.productGroupCode,
            productImage = product.productImage.orEmpty(),
            productName = product.productName.orEmpty(),

            quantity = quantity,

            unit1 = product.unit1.orEmpty(),
            unit2 = product.unit2.orEmpty(),
            unitid1 = product.unitid1.orEmpty(),
            unitid2 = product.unitid2.orEmpty()
        )
    }

    private fun findBestProductDiscountPercent(product: Product, quantity: Int): Int {
        if (!product.isDiscounts) return 0

        val productId = product.id ?: return 0

        return discountRepository.findByProductId(productId)
            .asSequence()
            .filter { quantity in it.fromNumber..it.endNumber }
            .maxByOrNull { it.discountPercent }
            ?.discountPercent
            ?: 0
    }

    private fun resolvePaymentDiscountPercent(
        paymentTerm: PaymentTerm?,
        paymentKind: PaymentKind
    ): Int {
        if (paymentTerm == null) return 0

        return when (paymentKind) {
            PaymentKind.IMMEDIATE -> paymentTerm.immediateDiscountPercent
            PaymentKind.RECEIPT -> paymentTerm.receiptDiscountPercent
            PaymentKind.CHEQUE -> paymentTerm.chequeDiscountPercent
        }.requirePercent()
    }
}

data class PriceLine(
    val product: Product,
    val quantity: Int,
    val gross: Money,
    val productDiscountPercent: Int,
    val productDiscount: Money,
    val afterProductDiscount: Money,
    val paymentDiscountPercent: Int,
    val paymentDiscount: Money,
    val taxableAmount: Money,
    val tax: Money,
    val taxWithoutPaymentDiscount: Money,
    val total: Money
) {
    val totalDiscount: Money
        get() = productDiscount + paymentDiscount
}