package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.DiscountResultDto
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.UpsertDiscountRequest
import com.msa.eshop.backend.common.requireMin
import com.msa.eshop.backend.common.requirePercent
import com.msa.eshop.backend.domain.Discount
import com.msa.eshop.backend.domain.DiscountRepository
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.domain.ProductRepository
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminDiscountService(
    private val discountRepository: DiscountRepository,
    private val productRepository: ProductRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): List<DiscountResultDto> =
        discountRepository.findAllByOrderByFromNumberAsc()
            .map { it.toDto() }

    @Transactional
    fun create(request: UpsertDiscountRequest): DiscountResultDto {
        validateRequest(request)

        val product = request.productId.findProduct()
        val productId = requireNotNull(product.id)

        validateNoOverlap(
            productId = productId,
            fromNumber = request.fromNumber,
            endNumber = request.endNumber,
            exceptDiscountId = null
        )

        val discount = Discount(
            product = product,
            discountPercent = request.discountPercent,
            fromNumber = request.fromNumber,
            endNumber = request.endNumber
        )

        return discountRepository.save(discount).toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertDiscountRequest): DiscountResultDto {
        validateRequest(request)

        val discount = discountRepository.findById(id)
            .orElseThrow { NotFoundException("تخفیف پیدا نشد") }

        val product = request.productId.findProduct()
        val productId = requireNotNull(product.id)

        validateNoOverlap(
            productId = productId,
            fromNumber = request.fromNumber,
            endNumber = request.endNumber,
            exceptDiscountId = id
        )

        discount.product = product
        discount.discountPercent = request.discountPercent
        discount.fromNumber = request.fromNumber
        discount.endNumber = request.endNumber

        return discountRepository.save(discount).toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val discount = discountRepository.findById(id)
            .orElseThrow { NotFoundException("تخفیف پیدا نشد") }

        discountRepository.delete(discount)
    }

    private fun validateRequest(request: UpsertDiscountRequest) {
        request.discountPercent.requirePercent()
        request.fromNumber.requireMin(1, "حداقل تعداد معتبر نیست")
        request.endNumber.requireMin(
            request.fromNumber,
            "حداکثر تعداد باید بزرگ‌تر یا مساوی حداقل تعداد باشد"
        )
    }

    private fun validateNoOverlap(
        productId: UUID,
        fromNumber: Int,
        endNumber: Int,
        exceptDiscountId: UUID?
    ) {
        val hasOverlap = discountRepository.findByProductId(productId)
            .asSequence()
            .filter { it.id != exceptDiscountId }
            .any { existing ->
                fromNumber <= existing.endNumber && endNumber >= existing.fromNumber
            }

        if (hasOverlap) {
            throw BadRequestException("بازه تخفیف با تخفیف دیگری برای همین کالا تداخل دارد")
        }
    }

    private fun String.findProduct(): Product {
        val value = trim()

        val asUuid = runCatching { UUID.fromString(value) }.getOrNull()
        if (asUuid != null) {
            return productRepository.findById(asUuid)
                .orElseThrow { NotFoundException("کالا پیدا نشد") }
        }

        val asCode = value.toIntOrNull()
            ?: throw BadRequestException("شناسه کالا معتبر نیست")

        return productRepository.findByProductCode(asCode)
            ?: throw NotFoundException("کالا پیدا نشد")
    }
}