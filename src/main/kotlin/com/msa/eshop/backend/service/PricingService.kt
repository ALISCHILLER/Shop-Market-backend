package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.Money
import com.msa.eshop.backend.common.dtos.SimulateDto
import com.msa.eshop.backend.common.requirePercent
import com.msa.eshop.backend.domain.entity.Discount
import com.msa.eshop.backend.domain.repository.DiscountRepository
import com.msa.eshop.backend.domain.entity.PaymentKind
import com.msa.eshop.backend.domain.entity.PaymentTerm
import com.msa.eshop.backend.domain.entity.Product
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

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
        val discounts = product.id
            ?.let { discountRepository.findByProductId(it) }
            .orEmpty()

        return calculateInternal(
            product = product,
            quantity = quantity,
            paymentTerm = paymentTerm,
            paymentKind = paymentKind,
            productDiscounts = discounts
        )
    }

    @Transactional(readOnly = true)
    fun calculateBatch(
        requests: List<PricingRequest>,
        paymentTerm: PaymentTerm?,
        paymentKind: PaymentKind = PaymentKind.RECEIPT
    ): List<PriceLine> {
        if (requests.isEmpty()) return emptyList()

        val discountsByProductId = loadDiscountsByProductId(
            requests.map { it.product }
        )

        return requests.map { request ->
            val productId = requireNotNull(request.product.id) {
                "Product must be persisted before pricing"
            }

            calculateInternal(
                product = request.product,
                quantity = request.quantity,
                paymentTerm = paymentTerm,
                paymentKind = request.paymentKind ?: paymentKind,
                productDiscounts = discountsByProductId[productId].orEmpty()
            )
        }
    }

    @Transactional(readOnly = true)
    fun simulate(
        product: Product,
        quantity: Int,
        paymentTerm: PaymentTerm?
    ): SimulateDto {
        val discounts = product.id
            ?.let { discountRepository.findByProductId(it) }
            .orEmpty()

        return buildSimulateDto(
            product = product,
            quantity = quantity,
            paymentTerm = paymentTerm,
            productDiscounts = discounts
        )
    }

    @Transactional(readOnly = true)
    fun simulateBatch(
        requests: List<PricingRequest>,
        paymentTerm: PaymentTerm?
    ): List<SimulateDto> {
        if (requests.isEmpty()) return emptyList()

        val discountsByProductId = loadDiscountsByProductId(
            requests.map { it.product }
        )

        return requests.map { request ->
            val productId = requireNotNull(request.product.id) {
                "Product must be persisted before simulation"
            }

            buildSimulateDto(
                product = request.product,
                quantity = request.quantity,
                paymentTerm = paymentTerm,
                productDiscounts = discountsByProductId[productId].orEmpty()
            )
        }
    }

    private fun buildSimulateDto(
        product: Product,
        quantity: Int,
        paymentTerm: PaymentTerm?,
        productDiscounts: List<Discount>
    ): SimulateDto {
        val receipt = calculateInternal(
            product = product,
            quantity = quantity,
            paymentTerm = paymentTerm,
            paymentKind = PaymentKind.RECEIPT,
            productDiscounts = productDiscounts
        )

        val immediate = calculateInternal(
            product = product,
            quantity = quantity,
            paymentTerm = paymentTerm,
            paymentKind = PaymentKind.IMMEDIATE,
            productDiscounts = productDiscounts
        )

        val cheque = calculateInternal(
            product = product,
            quantity = quantity,
            paymentTerm = paymentTerm,
            paymentKind = PaymentKind.CHEQUE,
            productDiscounts = productDiscounts
        )

        return SimulateDto(
            convertFactor1 = product.convertFactor1,
            convertFactor2 = product.convertFactor2,
            discountPercent = receipt.productDiscountPercent,

            discount_Percent_PaymentTerm_Receipt = receipt.paymentDiscount.toPersistedLong(),
            discount_Percent_PaymentTerm_Receipt_Tax = receipt.tax.toPersistedLong(),

            discount_Percent_PaymentTerm_cheque = cheque.paymentDiscount.toPersistedLong(),
            discount_Percent_PaymentTerm_cheque_Tax = cheque.tax.toPersistedLong(),

            discount_Percent_PaymentTerm_immediate = immediate.paymentDiscount.toPersistedLong(),
            discount_Percent_PaymentTerm_immediate_Tax = immediate.tax.toPersistedLong(),

            finalPrice = receipt.gross.toPersistedLong(),
            finalPriceDiscount = receipt.afterProductDiscount.toPersistedLong(),

            fullNameKala1 = product.fullNameKala1.orEmpty(),
            fullNameKala2 = product.fullNameKala2.orEmpty(),

            id = requireNotNull(product.id).toString(),
            isTax = product.isTax,
            paymentTermId = paymentTerm?.id?.toString(),

            price = product.price,
            priceByDiscountPercent = receipt.afterProductDiscount.toPersistedLong(),
            priceByDiscountPercentAndTax =
                (receipt.afterProductDiscount + receipt.taxWithoutPaymentDiscount).toPersistedLong(),

            priceByDiscountPercentAndTax_Receipt = receipt.total.toPersistedLong(),
            priceByDiscountPercentAndTax_cheque = cheque.total.toPersistedLong(),
            priceByDiscountPercentAndTax_immediate = immediate.total.toPersistedLong(),

            priceDiscount = receipt.productDiscount.toPersistedLong(),

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

    private fun calculateInternal(
        product: Product,
        quantity: Int,
        paymentTerm: PaymentTerm?,
        paymentKind: PaymentKind,
        productDiscounts: List<Discount>
    ): PriceLine {
        val gross = Money.multiply(product.price, quantity)

        val productDiscountPercent = findBestProductDiscountPercent(
            product = product,
            quantity = quantity,
            discounts = productDiscounts
        )

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

    private fun findBestProductDiscountPercent(
        product: Product,
        quantity: Int,
        discounts: List<Discount>
    ): Int {
        if (!product.isDiscounts) return 0

        return discounts
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

    private fun loadDiscountsByProductId(products: List<Product>): Map<UUID, List<Discount>> {
        val productIds = products
            .mapNotNull { it.id }
            .toSet()

        if (productIds.isEmpty()) return emptyMap()

        return discountRepository.findByProductIdIn(productIds)
            .groupBy { requireNotNull(it.product?.id) }
    }
}

data class PricingRequest(
    val product: Product,
    val quantity: Int,
    val paymentKind: PaymentKind? = null
)

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