package com.msa.eshop.backend.service.cart

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.dtos.CartCheckoutRequest
import com.msa.eshop.backend.common.dtos.CartCheckoutResponse
import com.msa.eshop.backend.domain.entity.Cart
import com.msa.eshop.backend.domain.entity.CartIdempotencyKey
import com.msa.eshop.backend.domain.entity.Customer
import com.msa.eshop.backend.domain.repository.CartIdempotencyKeyRepository
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.Base64

@Service
class CartIdempotencyService(
    private val repository: CartIdempotencyKeyRepository,
    private val objectMapper: ObjectMapper
) {

    fun normalizeKey(idempotencyKey: String?): String? =
        idempotencyKey
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.take(MAX_IDEMPOTENCY_KEY_LENGTH)

    fun requestHash(request: CartCheckoutRequest): String {
        val canonical = CanonicalCheckoutRequest(
            customerAddressId = request.customerAddressId.toString(),
            paymentTermId = request.paymentTermId.toString(),
            items = request.items
                .groupBy { it.productCode }
                .map { (productCode, rows) ->
                    CanonicalCheckoutLine(
                        productCode = productCode,
                        quantity = rows.sumOf { it.quantity }
                    )
                }
                .sortedBy { it.productCode }
        )

        val bytes = objectMapper.writeValueAsBytes(canonical)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(digest)
    }

    fun findExisting(
        customer: Customer,
        idempotencyKey: String,
        requestHash: String
    ): ExistingIdempotencyResult? {
        val customerId = requireNotNull(customer.id)

        val existing = repository.findForUpdate(
            customerId = customerId,
            endpoint = CHECKOUT_ENDPOINT,
            idempotencyKey = idempotencyKey
        ) ?: return null

        if (existing.requestHash != requestHash) {
            throw BadRequestException("کلید Idempotency برای درخواست متفاوتی استفاده شده است")
        }

        val cart = existing.cart

        if (existing.isCompleted() && cart != null) {
            return ExistingIdempotencyResult(
                response = cart.toCheckoutResponse(idempotencyKey)
            )
        }

        throw BadRequestException("درخواست checkout با همین Idempotency-Key در حال پردازش است")
    }

    fun createProcessing(
        customer: Customer,
        idempotencyKey: String,
        requestHash: String
    ): CartIdempotencyKey =
        repository.save(
            CartIdempotencyKey(
                customer = customer,
                endpoint = CHECKOUT_ENDPOINT,
                idempotencyKey = idempotencyKey,
                requestHash = requestHash
            )
        )

    fun complete(
        record: CartIdempotencyKey?,
        cart: Cart
    ) {
        if (record == null) return

        record.markCompleted(cart)
        repository.save(record)
    }

    fun fail(record: CartIdempotencyKey?) {
        if (record == null) return

        record.markFailed()
        repository.save(record)
    }

    private fun Cart.toCheckoutResponse(idempotencyKey: String): CartCheckoutResponse =
        CartCheckoutResponse(
            cartId = requireNotNull(id),
            cartCode = cartCode,
            statusCode = statusCode,
            statusName = statusName,
            subtotal = subtotal,
            discountTotal = discountTotal,
            taxTotal = taxTotal,
            total = total,
            idempotencyKey = idempotencyKey
        )

    private data class CanonicalCheckoutRequest(
        val customerAddressId: String,
        val paymentTermId: String,
        val items: List<CanonicalCheckoutLine>
    )

    private data class CanonicalCheckoutLine(
        val productCode: Int,
        val quantity: Int
    )

    companion object {
        const val CHECKOUT_ENDPOINT = "/api/v1/cart/checkout"
        const val MAX_IDEMPOTENCY_KEY_LENGTH = 128
    }
}

data class ExistingIdempotencyResult(
    val response: CartCheckoutResponse
)