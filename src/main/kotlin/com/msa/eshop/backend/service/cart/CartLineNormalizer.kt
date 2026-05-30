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

    private fun validateSameHeader(requests: List<InsertCartModelRequest>) {
        val first = requests.first()

        if (first.customerAddressId.isBlank()) {
            throw BadRequestException("شناسه آدرس الزامی است")
        }

        if (first.paymentTermId.isBlank()) {
            throw BadRequestException("شناسه روش پرداخت الزامی است")
        }

        requests.forEach { item ->
            if (item.customerAddressId.trim() != first.customerAddressId.trim()) {
                throw BadRequestException("همه آیتم‌های سبد باید یک آدرس مشترک داشته باشند")
            }

            if (item.paymentTermId.trim() != first.paymentTermId.trim()) {
                throw BadRequestException("همه آیتم‌های سبد باید یک روش پرداخت مشترک داشته باشند")
            }
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