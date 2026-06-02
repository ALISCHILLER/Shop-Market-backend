package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.dtos.CartLineRequest
import org.springframework.stereotype.Component

@Component
class CartLineNormalizer {

    fun normalize(lines: List<CartLineRequest>): List<NormalizedCartLine> {
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