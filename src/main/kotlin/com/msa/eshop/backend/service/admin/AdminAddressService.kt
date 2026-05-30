package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanRequired
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.UpsertAddressRequest
import com.msa.eshop.backend.common.toUuidOrBadRequest
import com.msa.eshop.backend.common.validateGeoPair
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CustomerAddress
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminAddressService(
    private val customerRepository: CustomerRepository,
    private val addressRepository: CustomerAddressRepository,
    private val cartRepository: CartRepository
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

        return saved.toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertAddressRequest): OrderAddressDto {
        val address = addressRepository.findById(id)
            .orElseThrow { NotFoundException("آدرس پیدا نشد") }

        val oldCustomerId = address.customer?.id
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

        return saved.toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val address = addressRepository.findById(id)
            .orElseThrow { NotFoundException("آدرس پیدا نشد") }

        val customerId = address.customer?.id

        if (cartRepository.countByAddressId(id) > 0) {
            throw BadRequestException("این آدرس در سفارش استفاده شده و قابل حذف نیست")
        }

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
        selected.isDefault = true
        addressRepository.save(selected)
    }
}