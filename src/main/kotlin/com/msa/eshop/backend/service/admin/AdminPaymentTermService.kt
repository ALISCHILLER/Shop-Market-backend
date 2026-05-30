package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.PaymentTermDto
import com.msa.eshop.backend.common.UpsertPaymentTermRequest
import com.msa.eshop.backend.common.cleanRequired
import com.msa.eshop.backend.common.requirePercent
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.PaymentTerm
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminPaymentTermService(
    private val paymentTermRepository: PaymentTermRepository,
    private val cartRepository: CartRepository
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

        return paymentTermRepository.save(term).toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertPaymentTermRequest): PaymentTermDto {
        validateRequest(request)

        val term = paymentTermRepository.findById(id)
            .orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }

        term.name = request.name.cleanRequired("نام روش پرداخت الزامی است")
        term.deadLine = request.deadLine
        term.immediateDiscountPercent = request.immediateDiscountPercent
        term.receiptDiscountPercent = request.receiptDiscountPercent
        term.chequeDiscountPercent = request.chequeDiscountPercent
        term.active = request.active

        return paymentTermRepository.save(term).toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val term = paymentTermRepository.findById(id)
            .orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }

        if (cartRepository.countByPaymentTermId(id) > 0) {
            throw BadRequestException("این روش پرداخت در سفارش استفاده شده و قابل حذف نیست")
        }

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
}