package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.AdminCartSummaryDto
import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.PageResponseDto
import com.msa.eshop.backend.common.ReportCartDetailsDto
import com.msa.eshop.backend.common.UpdateCartStatusRequest
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.createPageable
import com.msa.eshop.backend.common.parseClientDateOrNull
import com.msa.eshop.backend.common.toPageResponse
import com.msa.eshop.backend.domain.Cart
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CartStatus
import com.msa.eshop.backend.service.toDetailsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCartService(
    private val cartRepository: CartRepository
) {
    @Transactional(readOnly = true)
    fun findAll(
        page: Int,
        size: Int,
        cartCode: Int?,
        customerSearch: String?,
        fromDate: String?,
        toDate: String?
    ): PageResponseDto<AdminCartSummaryDto> {
        if (cartCode != null && cartCode <= 0) {
            throw BadRequestException("کد سفارش معتبر نیست")
        }

        val parsedFromDate = fromDate.parseClientDateOrNull()
        val parsedToDate = toDate.parseClientDateOrNull()

        if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
            throw BadRequestException("تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد")
        }

        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = "createdAt"
        )

        val result = cartRepository.findAdminCarts(
            cartCode = cartCode,
            customerSearch = customerSearch.cleanOrNull(),
            fromDate = parsedFromDate,
            toDate = parsedToDate,
            pageable = pageable
        )

        return result.toPageResponse { it.toAdminSummaryDto() }
    }

    @Transactional(readOnly = true)
    fun details(cartCode: Int): List<ReportCartDetailsDto> {
        if (cartCode <= 0) {
            throw BadRequestException("کد سفارش معتبر نیست")
        }

        val cart = cartRepository.findByCartCode(cartCode)
            ?: throw NotFoundException("سفارش پیدا نشد")

        return cart.items
            .sortedBy { it.productCode }
            .map { it.toDetailsDto(cart) }
    }

    @Transactional
    fun updateStatus(
        cartCode: Int,
        request: UpdateCartStatusRequest
    ): AdminCartSummaryDto {
        if (cartCode <= 0) {
            throw BadRequestException("کد سفارش معتبر نیست")
        }

        val cart = cartRepository.findByCartCode(cartCode)
            ?: throw NotFoundException("سفارش پیدا نشد")

        val currentStatus = CartStatus.normalize(cart.statusName)
        val targetStatus = CartStatus.normalize(request.status)

        validateTransition(
            current = currentStatus,
            target = targetStatus
        )

        cart.statusName = targetStatus.title
        cart.statusColor = request.color.cleanOrNull() ?: targetStatus.color

        return cartRepository.save(cart).toAdminSummaryDto()
    }

    private fun validateTransition(
        current: CartStatus,
        target: CartStatus
    ) {
        if (current == target) return

        if (current == CartStatus.CANCELLED) {
            throw BadRequestException("سفارش لغو شده قابل تغییر وضعیت نیست")
        }

        if (current == CartStatus.DELIVERED) {
            throw BadRequestException("سفارش تحویل شده قابل تغییر وضعیت نیست")
        }

        if (current == CartStatus.REGISTERED && target == CartStatus.DELIVERED) {
            throw BadRequestException("سفارش ثبت شده باید ابتدا وارد مرحله بررسی شود")
        }
    }

    private fun Cart.toAdminSummaryDto(): AdminCartSummaryDto =
        AdminCartSummaryDto(
            id = requireNotNull(id).toString(),
            cartCode = cartCode,
            customerId = customer?.id?.toString(),
            customerCode = customer?.customerCode.orEmpty(),
            customerName = customerNameSnapshot,
            customerAddress = customerAddressSnapshot,
            paymentTermId = paymentTerm?.id?.toString(),
            paymentTermName = paymentTerm?.name.orEmpty(),
            statusName = statusName,
            statusColor = statusColor,
            salesDate = salesDate.toString(),
            subtotal = subtotal,
            discountTotal = discountTotal,
            taxTotal = taxTotal,
            total = total,
            itemCount = items.size,
            createdAt = createdAt.toString()
        )
}