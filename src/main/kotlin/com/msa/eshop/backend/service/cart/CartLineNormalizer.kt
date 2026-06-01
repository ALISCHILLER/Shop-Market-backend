package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.dtos.CartSimulateLineRequest
import com.msa.eshop.backend.common.dtos.InsertCartModelRequest
import com.msa.eshop.backend.common.dtos.SimulateModelRequest
import org.springframework.stereotype.Component

@Component
class CartLineNormalizer {

    fun normalizeSimulateLines(requests: List<SimulateModelRequest>): List<NormalizedCartLine> {
        validateLineCount(requests.size)

        requests.forEach {
            validateLine(it.productCode, it.quantity)
        }

        return requests
            .groupBy { it.productCode }
            .map { (productCode, rows) ->
                val quantity = safeQuantitySum(rows.map { it.quantity })
                validateLine(productCode, quantity)

                NormalizedCartLine(
                    productCode = productCode,
                    quantity = quantity
                )
            }
            .sortedBy { it.productCode }
    }

    fun normalizeCheckoutLines(requests: List<InsertCartModelRequest>): List<NormalizedCartLine> {
        validateLineCount(requests.size)
        validateSameCheckoutHeader(requests)

        requests.forEach {
            validateLine(it.productCode, it.quantity)
        }

        return requests
            .groupBy { it.productCode }
            .map { (productCode, rows) ->
                val quantity = safeQuantitySum(rows.map { it.quantity })
                validateLine(productCode, quantity)

                NormalizedCartLine(
                    productCode = productCode,
                    quantity = quantity
                )
            }
            .sortedBy { it.productCode }
    }

    fun extractCheckoutHeader(requests: List<InsertCartModelRequest>): CheckoutHeader {
        if (requests.isEmpty()) {
            throw BadRequestException("سبد خرید خالی است")
        }

        val first = requests.first()

        return CheckoutHeader(
            customerAddressId = first.customerAddressId.trim().ifBlank {
                throw BadRequestException("شناسه آدرس الزامی است")
            },
            paymentTermId = first.paymentTermId.trim().ifBlank {
                throw BadRequestException("شناسه روش پرداخت الزامی است")
            }
        )
    }

    fun extractSimulateHeader(requests: List<SimulateModelRequest>): SimulateHeader {
        if (requests.isEmpty()) {
            throw BadRequestException("سبد خرید خالی است")
        }

        val paymentTermIds = requests
            .mapNotNull { it.paymentTermId?.trim()?.takeIf { id -> id.isNotBlank() } }
            .distinct()

        if (paymentTermIds.size > 1) {
            throw BadRequestException("همه آیتم‌های شبیه‌سازی باید یک روش پرداخت مشترک داشته باشند")
        }

        return SimulateHeader(
            paymentTermId = paymentTermIds.firstOrNull()
        )
    }

    fun normalizeModernLines(lines: List<CartSimulateLineRequest>): List<NormalizedCartLine> {
        validateLineCount(lines.size)

        lines.forEach {
            validateLine(it.productCode, it.quantity)
        }

        return lines
            .groupBy { it.productCode }
            .map { (productCode, rows) ->
                val quantity = safeQuantitySum(rows.map { it.quantity })
                validateLine(productCode, quantity)

                NormalizedCartLine(
                    productCode = productCode,
                    quantity = quantity
                )
            }
            .sortedBy { it.productCode }
    }

    private fun validateSameCheckoutHeader(requests: List<InsertCartModelRequest>) {
        val header = extractCheckoutHeader(requests)

        requests.forEach { item ->
            if (item.customerAddressId.trim() != header.customerAddressId) {
                throw BadRequestException("همه آیتم‌های سبد باید یک آدرس مشترک داشته باشند")
            }

            if (item.paymentTermId.trim() != header.paymentTermId) {
                throw BadRequestException("همه آیتم‌های سبد باید یک روش پرداخت مشترک داشته باشند")
            }
        }
    }

    private fun validateLineCount(size: Int) {
        if (size <= 0) {
            throw BadRequestException("سبد خرید خالی است")
        }

        if (size > MAX_CART_LINES) {
            throw BadRequestException("تعداد ردیف‌های سبد خرید نمی‌تواند بیشتر از $MAX_CART_LINES باشد")
        }
    }

    private fun validateLine(productCode: Int, quantity: Int) {
        if (productCode <= 0) {
            throw BadRequestException("کد کالا معتبر نیست")
        }

        if (quantity <= 0) {
            throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")
        }

        if (quantity > MAX_QUANTITY_PER_PRODUCT) {
            throw BadRequestException("تعداد هر کالا نمی‌تواند بیشتر از $MAX_QUANTITY_PER_PRODUCT باشد")
        }
    }

    private fun safeQuantitySum(quantities: List<Int>): Int {
        val sum = quantities.fold(0L) { acc, value ->
            Math.addExact(acc, value.toLong())
        }

        if (sum > Int.MAX_VALUE) {
            throw BadRequestException("تعداد کالا بیش از حد مجاز است")
        }

        return sum.toInt()
    }

    private companion object {
        const val MAX_CART_LINES = 100
        const val MAX_QUANTITY_PER_PRODUCT = 1_000
    }
}

data class NormalizedCartLine(
    val productCode: Int,
    val quantity: Int
)

data class CheckoutHeader(
    val customerAddressId: String,
    val paymentTermId: String
)

data class SimulateHeader(
    val paymentTermId: String?
)