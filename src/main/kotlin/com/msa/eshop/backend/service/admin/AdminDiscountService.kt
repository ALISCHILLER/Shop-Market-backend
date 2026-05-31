package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.dtos.DiscountResultDto
import com.msa.eshop.backend.common.dtos.UpsertDiscountRequest
import com.msa.eshop.backend.common.requireMin
import com.msa.eshop.backend.common.requirePercent
import com.msa.eshop.backend.domain.entity.Discount
import com.msa.eshop.backend.domain.repository.DiscountRepository
import com.msa.eshop.backend.service.audit.AuditLogService
import com.msa.eshop.backend.service.catalog.ProductResolver
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminDiscountService(
    private val discountRepository: DiscountRepository,
    private val productResolver: ProductResolver,
    private val auditLogService: AuditLogService
) {

    @Transactional(readOnly = true)
    fun findAll(): List<DiscountResultDto> =
        discountRepository.findAllByOrderByFromNumberAsc()
            .map { it.toDto() }

    @Transactional
    fun create(request: UpsertDiscountRequest): DiscountResultDto {
        validateRequest(request)

        val product = productResolver.requireByIdOrCode(request.productId)
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

        val savedDiscount = discountRepository.save(discount)

        auditLogService.record(
            action = "DISCOUNT_CREATED",
            entityType = ENTITY_TYPE_DISCOUNT,
            entityId = savedDiscount.id?.toString(),
            oldValue = null,
            newValue = discountSnapshot(savedDiscount),
            description = "Discount created by admin"
        )

        return savedDiscount.toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertDiscountRequest): DiscountResultDto {
        validateRequest(request)

        val discount = discountRepository.findById(id)
            .orElseThrow { NotFoundException("تخفیف پیدا نشد") }

        val oldSnapshot = discountSnapshot(discount)

        val product = productResolver.requireByIdOrCode(request.productId)
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

        val savedDiscount = discountRepository.save(discount)

        auditLogService.record(
            action = "DISCOUNT_UPDATED",
            entityType = ENTITY_TYPE_DISCOUNT,
            entityId = savedDiscount.id?.toString(),
            oldValue = oldSnapshot,
            newValue = discountSnapshot(savedDiscount),
            description = "Discount updated by admin"
        )

        return savedDiscount.toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val discount = discountRepository.findById(id)
            .orElseThrow { NotFoundException("تخفیف پیدا نشد") }

        val oldSnapshot = discountSnapshot(discount)

        auditLogService.record(
            action = "DISCOUNT_DELETED",
            entityType = ENTITY_TYPE_DISCOUNT,
            entityId = discount.id?.toString(),
            oldValue = oldSnapshot,
            newValue = null,
            description = "Discount deleted by admin"
        )

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
        val hasOverlap = discountRepository.existsOverlappingRange(
            productId = productId,
            fromNumber = fromNumber,
            endNumber = endNumber,
            exceptDiscountId = exceptDiscountId
        )

        if (hasOverlap) {
            throw BadRequestException("بازه تخفیف با تخفیف دیگری برای همین کالا تداخل دارد")
        }
    }

    private fun discountSnapshot(discount: Discount): Map<String, Any?> =
        auditLogService.snapshotOf(
            "id" to discount.id,
            "productId" to discount.product?.id,
            "productCode" to discount.product?.productCode,
            "productName" to discount.product?.productName,
            "fromNumber" to discount.fromNumber,
            "endNumber" to discount.endNumber,
            "discountPercent" to discount.discountPercent
        )

    private companion object {
        const val ENTITY_TYPE_DISCOUNT = "Discount"
    }
}