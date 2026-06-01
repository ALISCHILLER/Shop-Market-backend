package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.cleanRequired
import com.msa.eshop.backend.common.createPageable
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.UpsertAddressRequest
import com.msa.eshop.backend.common.toPageResponse
import com.msa.eshop.backend.common.toUuidOrBadRequest
import com.msa.eshop.backend.common.validateGeoPair
import com.msa.eshop.backend.domain.entity.CustomerAddress
import com.msa.eshop.backend.domain.repository.CartRepository
import com.msa.eshop.backend.domain.repository.CustomerAddressRepository
import com.msa.eshop.backend.domain.repository.CustomerRepository
import com.msa.eshop.backend.service.audit.AuditLogService
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminAddressService(
    private val customerRepository: CustomerRepository,
    private val addressRepository: CustomerAddressRepository,
    private val cartRepository: CartRepository,
    private val auditLogService: AuditLogService
) {
    @Transactional(readOnly = true)
    fun findAll(customerId: UUID? = null): List<OrderAddressDto> {
        val addresses = if (customerId == null) {
            addressRepository.findAll()
        } else {
            addressRepository.findByCustomerId(customerId)
        }

        return addresses
            .sortedWith(
                compareByDescending<CustomerAddress> { it.isDefault }
                    .thenBy { it.centerName }
            )
            .map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun search(
        page: Int,
        size: Int,
        customerId: UUID?,
        search: String?,
        sortBy: String = DEFAULT_SORT_BY,
        direction: String = DEFAULT_SORT_DIRECTION
    ): PageResponseDto<OrderAddressDto> {
        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            allowedSorts = ALLOWED_SORTS
        )

        return addressRepository.searchAdminAddresses(
            customerId = customerId,
            search = search.cleanOrNull(),
            pageable = pageable
        ).toPageResponse { it.toDto() }
    }

    @Transactional
    fun create(request: UpsertAddressRequest): OrderAddressDto {
        val customerId = request.customerId.toUuidOrBadRequest("شناسه مشتری معتبر نیست")

        val customer = customerRepository.findById(customerId)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        val latitude = request.latitude ?: request.lat
        val longitude = request.longitude ?: request.lng

        validateGeoPair(latitude, longitude)

        val isFirstAddress = addressRepository.countByCustomerId(customerId) == 0L
        val shouldBeDefault = request.isDefault ?: isFirstAddress

        if (shouldBeDefault) {
            addressRepository.clearDefaultForCustomer(
                customerId = customerId,
                exceptId = null
            )
        }

        val address = CustomerAddress(
            customer = customer,
            centerName = request.centerName.trim(),
            customerAddress = request.customerAddress.cleanRequired("آدرس الزامی است"),
            customerMobile = request.customerMobile.trim(),
            customerPhone = request.customerPhone.trim(),
            latitude = latitude,
            longitude = longitude,
            isDefault = shouldBeDefault
        )

        val saved = addressRepository.save(address)
        ensureOneDefaultAddress(customerId)

        auditLogService.record(
            action = "ADDRESS_CREATED",
            entityType = ENTITY_TYPE_ADDRESS,
            entityId = saved.id?.toString(),
            oldValue = null,
            newValue = addressSnapshot(saved),
            description = "Customer address created by admin"
        )

        return saved.toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertAddressRequest): OrderAddressDto {
        val address = addressRepository.findById(id)
            .orElseThrow { NotFoundException("آدرس پیدا نشد") }

        val oldSnapshot = addressSnapshot(address)
        val oldCustomerId = address.customer?.id
        val oldIsDefault = address.isDefault

        val newCustomerId = request.customerId.toUuidOrBadRequest("شناسه مشتری معتبر نیست")

        val customer = customerRepository.findById(newCustomerId)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        val latitude = request.latitude ?: request.lat
        val longitude = request.longitude ?: request.lng

        validateGeoPair(latitude, longitude)

        val shouldBeDefault = request.isDefault ?: address.isDefault

        if (shouldBeDefault) {
            addressRepository.clearDefaultForCustomer(
                customerId = newCustomerId,
                exceptId = id
            )
        }

        address.customer = customer
        address.centerName = request.centerName.trim()
        address.customerAddress = request.customerAddress.cleanRequired("آدرس الزامی است")
        address.customerMobile = request.customerMobile.trim()
        address.customerPhone = request.customerPhone.trim()
        address.latitude = latitude
        address.longitude = longitude
        address.isDefault = shouldBeDefault

        val saved = addressRepository.save(address)

        if (oldCustomerId != null && oldCustomerId != newCustomerId) {
            ensureOneDefaultAddress(oldCustomerId)
        }

        ensureOneDefaultAddress(newCustomerId)

        auditLogService.record(
            action = "ADDRESS_UPDATED",
            entityType = ENTITY_TYPE_ADDRESS,
            entityId = saved.id?.toString(),
            oldValue = oldSnapshot,
            newValue = addressSnapshot(saved) + mapOf(
                "defaultChanged" to (oldIsDefault != saved.isDefault),
                "customerChanged" to (oldCustomerId != newCustomerId)
            ),
            description = "Customer address updated by admin"
        )

        return saved.toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val address = addressRepository.findById(id)
            .orElseThrow { NotFoundException("آدرس پیدا نشد") }

        val oldSnapshot = addressSnapshot(address)
        val customerId = address.customer?.id

        if (cartRepository.countByAddressId(id) > 0) {
            throw BadRequestException("این آدرس در سفارش استفاده شده و قابل حذف نیست")
        }

        auditLogService.record(
            action = "ADDRESS_DELETED",
            entityType = ENTITY_TYPE_ADDRESS,
            entityId = address.id?.toString() ?: id.toString(),
            oldValue = oldSnapshot,
            newValue = null,
            description = "Customer address deleted by admin"
        )

        addressRepository.delete(address)

        if (customerId != null) {
            ensureOneDefaultAddress(customerId)
        }
    }

    private fun ensureOneDefaultAddress(customerId: UUID) {
        val addresses = addressRepository.findByCustomerId(customerId)
        if (addresses.isEmpty()) return
        if (addresses.any { it.isDefault }) return

        val selected = addresses.sortedBy { it.centerName }.first()
        val oldSnapshot = addressSnapshot(selected)

        selected.isDefault = true
        val saved = addressRepository.save(selected)

        auditLogService.record(
            action = "ADDRESS_DEFAULT_CHANGED",
            entityType = ENTITY_TYPE_ADDRESS,
            entityId = saved.id?.toString(),
            oldValue = oldSnapshot,
            newValue = addressSnapshot(saved),
            description = "Default address automatically selected by system"
        )
    }

    private fun addressSnapshot(address: CustomerAddress): Map<String, Any?> =
        auditLogService.snapshotOf(
            "id" to address.id,
            "customerId" to address.customer?.id,
            "customerCode" to address.customer?.customerCode,
            "customerName" to address.customer?.customerName,
            "centerName" to address.centerName,
            "customerAddress" to address.customerAddress,
            "customerMobile" to address.customerMobile,
            "customerPhone" to address.customerPhone,
            "latitude" to address.latitude,
            "longitude" to address.longitude,
            "isDefault" to address.isDefault
        )

    private companion object {
        const val ENTITY_TYPE_ADDRESS = "CustomerAddress"
        const val DEFAULT_SORT_BY = "createdAt"
        const val DEFAULT_SORT_DIRECTION = "DESC"

        val ALLOWED_SORTS = setOf(
            "createdAt",
            "centerName",
            "isDefault"
        )
    }
}