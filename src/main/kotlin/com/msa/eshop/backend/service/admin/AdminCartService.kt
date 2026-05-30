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
import com.msa.eshop.backend.domain.CartItemRepository
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CartStatus
import com.msa.eshop.backend.service.admin.mapper.AdminCartMapper
import com.msa.eshop.backend.service.cart.CartStatusPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val cartStatusPolicy: CartStatusPolicy,
    private val adminCartMapper: AdminCartMapper
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
            sortBy = "createdAt",
            allowedSorts = setOf("createdAt", "salesDate", "cartCode", "total")
        )

        val result = cartRepository.findAdminCarts(
            cartCode = cartCode,
            customerSearch = customerSearch.cleanOrNull(),
            fromDate = parsedFromDate,
            toDate = parsedToDate,
            pageable = pageable
        )

        val cartIds = result.content.mapNotNull { it.id }

        val itemCounts = if (cartIds.isEmpty()) {
            emptyMap()
        } else {
            cartItemRepository.countItemsByCartIds(cartIds)
                .associate { projection ->
                    projection.cartId to projection.itemCount.toInt()
                }
        }

        return result.toPageResponse { cart ->
            adminCartMapper.toSummaryDto(
                cart = cart,
                itemCount = cart.id?.let { itemCounts[it] } ?: 0
            )
        }
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
            .map { item ->
                adminCartMapper.toDetailsDto(
                    cart = cart,
                    item = item
                )
            }
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

        cartStatusPolicy.assertCanChange(
            current = currentStatus,
            target = targetStatus
        )

        cart.statusName = targetStatus.title
        cart.statusColor = request.color.cleanOrNull() ?: targetStatus.color

        val saved = cartRepository.save(cart)

        return adminCartMapper.toSummaryDto(
            cart = saved,
            itemCount = saved.items.size
        )
    }
}