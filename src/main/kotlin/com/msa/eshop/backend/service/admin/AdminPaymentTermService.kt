package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanRequired
import com.msa.eshop.backend.common.dtos.PaymentTermDto
import com.msa.eshop.backend.common.dtos.UpsertPaymentTermRequest
import com.msa.eshop.backend.common.requirePercent
import com.msa.eshop.backend.domain.entity.PaymentTerm
import com.msa.eshop.backend.domain.repository.CartRepository
import com.msa.eshop.backend.domain.repository.PaymentTermRepository
import com.msa.eshop.backend.service.audit.AuditLogService
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminPaymentTermService(
    private val paymentTermRepository: PaymentTermRepository,
    private val cartRepository: CartRepository,
    private val auditLogService: AuditLogService
) {

    @Transactional(readOnly = true)
    fun findAll(): List<PaymentTermDto> =
        paymentTermRepository.findAllByOrderByDeadLineAscNameAsc()
            .map { it.toDto() }

    @Transactional
    fun create(request: UpsertPaymentTermRequest): PaymentTermDto {
        validateRequest(request)

        val term = PaymentTerm(
            name = request.name.cleanRequired("نام روش پرداخت الزامی است"),
            deadLine = request.deadLine,
            immediateDiscountPercent = request.immediateDiscountPercent,
            receiptDiscountPercent = request.receiptDiscountPercent,
            chequeDiscountPercent = request.chequeDiscountPercent,
            active = request.active
        )

        val savedTerm = paymentTermRepository.save(term)

        auditLogService.record(
            action = "PAYMENT_TERM_CREATED",
            entityType = ENTITY_TYPE_PAYMENT_TERM,
            entityId = savedTerm.id?.toString(),
            oldValue = null,
            newValue = paymentTermSnapshot(savedTerm),
            description = "Payment term created by admin"
        )

        return savedTerm.toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertPaymentTermRequest): PaymentTermDto {
        validateRequest(request)

        val term = paymentTermRepository.findById(id)
            .orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }

        val oldSnapshot = paymentTermSnapshot(term)

        term.name = request.name.cleanRequired("نام روش پرداخت الزامی است")
        term.deadLine = request.deadLine
        term.immediateDiscountPercent = request.immediateDiscountPercent
        term.receiptDiscountPercent = request.receiptDiscountPercent
        term.chequeDiscountPercent = request.chequeDiscountPercent
        term.active = request.active

        val savedTerm = paymentTermRepository.save(term)

        auditLogService.record(
            action = "PAYMENT_TERM_UPDATED",
            entityType = ENTITY_TYPE_PAYMENT_TERM,
            entityId = savedTerm.id?.toString(),
            oldValue = oldSnapshot,
            newValue = paymentTermSnapshot(savedTerm),
            description = "Payment term updated by admin"
        )

        return savedTerm.toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val term = paymentTermRepository.findById(id)
            .orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }

        if (cartRepository.countByPaymentTermId(id) > 0) {
            throw BadRequestException("این روش پرداخت در سفارش استفاده شده و قابل حذف نیست")
        }

        val oldSnapshot = paymentTermSnapshot(term)

        auditLogService.record(
            action = "PAYMENT_TERM_DELETED",
            entityType = ENTITY_TYPE_PAYMENT_TERM,
            entityId = term.id?.toString(),
            oldValue = oldSnapshot,
            newValue = null,
            description = "Payment term deleted by admin"
        )

        paymentTermRepository.delete(term)
    }

    private fun validateRequest(request: UpsertPaymentTermRequest) {
        request.name.cleanRequired("نام روش پرداخت الزامی است")

        if (request.deadLine < 0) {
            throw BadRequestException("مهلت پرداخت معتبر نیست")
        }

        request.immediateDiscountPercent.requirePercent()
        request.receiptDiscountPercent.requirePercent()
        request.chequeDiscountPercent.requirePercent()
    }

    private fun paymentTermSnapshot(paymentTerm: PaymentTerm): Map<String, Any?> =
        auditLogService.snapshotOf(
            "id" to paymentTerm.id,
            "name" to paymentTerm.name,
            "deadLine" to paymentTerm.deadLine,
            "receiptDiscountPercent" to paymentTerm.receiptDiscountPercent,
            "chequeDiscountPercent" to paymentTerm.chequeDiscountPercent,
            "immediateDiscountPercent" to paymentTerm.immediateDiscountPercent,
            "active" to paymentTerm.active
        )

    private companion object {
        const val ENTITY_TYPE_PAYMENT_TERM = "PaymentTerm"
    }
}