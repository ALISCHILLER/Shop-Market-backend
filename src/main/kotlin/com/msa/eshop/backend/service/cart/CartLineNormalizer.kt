package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.InsertCartModelRequest
import com.msa.eshop.backend.common.SimulateModelRequest
import org.springframework.stereotype.Component

@Component
class CartLineNormalizer {
    fun normalizeSimulateLines(requests: List<SimulateModelRequest>): List<NormalizedCartLine> {
        if (requests.isEmpty()) return emptyList()

        return requests
            .groupBy { it.productCode }
            .map { (productCode, rows) ->
                val quantity = rows.sumOf { it.quantity }
                validateLine(productCode, quantity)

                NormalizedCartLine(
                    productCode = productCode,
                    quantity = quantity
                )
            }
    }

    fun normalizeCheckoutLines(requests: List<InsertCartModelRequest>): List<NormalizedCartLine> {
        if (requests.isEmpty()) {
            throw BadRequestException("سبد خرید خالی است")
        }

        validateSameHeader(requests)

        return requests
            .groupBy { it.productCode }
            .map { (productCode, rows) ->
                val quantity = rows.sumOf { it.quantity }
                validateLine(productCode, quantity)

                NormalizedCartLine(
                    productCode = productCode,
                    quantity = quantity
                )
            }
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

    private fun validateSameHeader(requests: List<InsertCartModelRequest>) {
        val header = extractCheckoutHeader(requests)

        requests.forEach { item ->
            if (item.customerAddressId.trim() != header.customerAddressId) {
                throw BadRequestException("همه آیتم‌های سبد باید یک آدرس مشترک داشته باشند")
            }

            if (item.paymentTermId.trim() != header.paymentTermId) {
                throw BadRequestException("همه آیتم‌های سبد باید یک روش پرداخت مشترک داشته باشند")
            }

            validateLine(item.productCode, item.quantity)
        }
    }

    private fun validateLine(productCode: Int, quantity: Int) {
        if (productCode <= 0) {
            throw BadRequestException("کد کالا معتبر نیست")
        }

        if (quantity <= 0) {
            throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")
        }
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