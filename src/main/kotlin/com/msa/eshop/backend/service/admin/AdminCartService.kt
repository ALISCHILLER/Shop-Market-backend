package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.createPageable
import com.msa.eshop.backend.common.dtos.AdminCartSummaryDto
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.ReportCartDetailsDto
import com.msa.eshop.backend.common.dtos.UpdateCartStatusRequest
import com.msa.eshop.backend.common.parseClientDateOrNull
import com.msa.eshop.backend.common.toPageResponse
import com.msa.eshop.backend.domain.entity.Cart
import com.msa.eshop.backend.domain.entity.CartStatus
import com.msa.eshop.backend.domain.repository.CartItemRepository
import com.msa.eshop.backend.domain.repository.CartRepository
import com.msa.eshop.backend.service.admin.mapper.AdminCartMapper
import com.msa.eshop.backend.service.audit.AuditLogService
import com.msa.eshop.backend.service.cart.CartStatusPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val cartStatusPolicy: CartStatusPolicy,
    private val adminCartMapper: AdminCartMapper,
    private val auditLogService: AuditLogService
) {
    @Transactional(readOnly = true)
    fun findAll(
        page: Int,
        size: Int,
        cartCode: Int?,
        customerSearch: String?,
        status: String?,
        fromDate: String?,
        toDate: String?,
        sortBy: String = "createdAt",
        direction: String = "DESC"
    ): PageResponseDto<AdminCartSummaryDto> {
        if (cartCode != null && cartCode <= 0) {
            throw BadRequestException("کد سفارش معتبر نیست")
        }

        val parsedFromDate = fromDate.parseClientDateOrNull()
        val parsedToDate = toDate.parseClientDateOrNull()

        if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
            throw BadRequestException("تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد")
        }

        val normalizedStatus = status
            .cleanOrNull()
            ?.let { CartStatus.normalize(it).code }

        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            allowedSorts = setOf("createdAt", "salesDate", "cartCode", "total")
        )

        val result = cartRepository.findAdminCarts(
            cartCode = cartCode,
            customerSearch = customerSearch.cleanOrNull(),
            statusCode = normalizedStatus,
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

        val oldSnapshot = cartStatusSnapshot(cart)

        val currentStatus = CartStatus.normalize(
            cart.statusCode.ifBlank { cart.statusName }
        )

        val targetStatus = CartStatus.normalize(request.status)

        cartStatusPolicy.assertCanChange(
            current = currentStatus,
            target = targetStatus
        )

        cart.applyStatus(
            status = targetStatus,
            customColor = request.color.cleanOrNull()
        )

        val saved = cartRepository.save(cart)

        auditLogService.record(
            action = "CART_STATUS_UPDATED",
            entityType = ENTITY_TYPE_CART,
            entityId = saved.id?.toString(),
            oldValue = oldSnapshot,
            newValue = cartStatusSnapshot(saved),
            description = "Cart status updated by admin"
        )

        return adminCartMapper.toSummaryDto(
            cart = saved,
            itemCount = saved.items.size
        )
    }

    private fun cartStatusSnapshot(cart: Cart): Map<String, Any?> =
        auditLogService.snapshotOf(
            "id" to cart.id,
            "cartCode" to cart.cartCode,
            "customerId" to cart.customer?.id,
            "customerCode" to cart.customer?.customerCode,
            "customerName" to cart.customerNameSnapshot,
            "statusCode" to cart.statusCode,
            "statusName" to cart.statusName,
            "statusColor" to cart.statusColor,
            "total" to cart.total
        )

    private companion object {
        const val ENTITY_TYPE_CART = "Cart"
    }
}