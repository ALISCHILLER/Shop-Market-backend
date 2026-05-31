package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.cleanRequired
import com.msa.eshop.backend.common.createPageable
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.UpsertCustomerRequest
import com.msa.eshop.backend.common.dtos.UserDto
import com.msa.eshop.backend.common.toPageResponse
import com.msa.eshop.backend.domain.repository.CartRepository
import com.msa.eshop.backend.domain.entity.Customer
import com.msa.eshop.backend.domain.repository.CustomerAddressRepository
import com.msa.eshop.backend.domain.repository.CustomerRepository
import com.msa.eshop.backend.domain.entity.CustomerRole
import com.msa.eshop.backend.service.audit.AuditLogService
import com.msa.eshop.backend.service.auth.PasswordPolicyValidator
import com.msa.eshop.backend.service.auth.RefreshTokenService
import com.msa.eshop.backend.service.toDto
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminCustomerService(
    private val customerRepository: CustomerRepository,
    private val cartRepository: CartRepository,
    private val addressRepository: CustomerAddressRepository,
    private val passwordEncoder: PasswordEncoder,
    private val passwordPolicyValidator: PasswordPolicyValidator,
    private val refreshTokenService: RefreshTokenService,
    private val auditLogService: AuditLogService
) {

    @Transactional(readOnly = true)
    fun findAll(): List<UserDto> =
        customerRepository.findAll()
            .sortedBy { it.customerCode }
            .map { it.toDto() }

    @Transactional(readOnly = true)
    fun search(
        page: Int,
        size: Int,
        search: String?,
        role: String?,
        enabled: Boolean?,
        sortBy: String = DEFAULT_SORT_BY,
        direction: String = DEFAULT_SORT_DIRECTION
    ): PageResponseDto<UserDto> {
        val normalizedRole = role
            .cleanOrNull()
            ?.let { CustomerRole.normalize(it).name }

        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            allowedSorts = ALLOWED_SORTS
        )

        return customerRepository.searchAdminCustomers(
            search = search.cleanOrNull(),
            role = normalizedRole,
            enabled = enabled,
            pageable = pageable
        ).toPageResponse { it.toDto() }
    }

    @Transactional
    fun create(request: UpsertCustomerRequest): UserDto {
        val customerCode = request.customerCode.cleanRequired("کد مشتری الزامی است")
        val customerName = request.customerName.cleanRequired("نام مشتری الزامی است")
        val normalizedRole = CustomerRole.normalize(request.role).name

        if (customerRepository.existsByCustomerCode(customerCode)) {
            throw BadRequestException("کد مشتری قبلاً ثبت شده است")
        }

        val rawPassword = request.password
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw BadRequestException("رمز عبور اولیه الزامی است")

        passwordPolicyValidator.validate(
            password = rawPassword,
            customerCode = customerCode
        )

        val customer = Customer(
            customerCode = customerCode,
            customerName = customerName,
            mobile = request.mobile.cleanOrNull(),
            phone = request.phone.cleanOrNull(),
            center = request.center.cleanOrNull(),
            nationalCode = request.nationalCode.cleanOrNull(),
            passwordHash = passwordEncoder.encode(rawPassword),
            salt = PASSWORD_ALGORITHM,
            role = normalizedRole,
            enabled = request.enabled
        ).apply {
            passwordChangeRequired = true
        }

        val savedCustomer = customerRepository.save(customer)

        auditLogService.record(
            action = "CUSTOMER_CREATED",
            entityType = ENTITY_TYPE_CUSTOMER,
            entityId = savedCustomer.id?.toString(),
            oldValue = null,
            newValue = customerSnapshot(savedCustomer),
            description = "Customer created by admin"
        )

        return savedCustomer.toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertCustomerRequest): UserDto {
        val customer = customerRepository.findById(id)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        val oldSnapshot = customerSnapshot(customer)
        val oldCustomerCode = customer.customerCode
        val oldRole = customer.role
        val oldEnabled = customer.enabled

        val customerCode = request.customerCode.cleanRequired("کد مشتری الزامی است")
        val customerName = request.customerName.cleanRequired("نام مشتری الزامی است")
        val normalizedRole = CustomerRole.normalize(request.role).name

        if (customerRepository.existsByCustomerCodeAndIdNot(customerCode, id)) {
            throw BadRequestException("کد مشتری قبلاً برای مشتری دیگری ثبت شده است")
        }

        var shouldRevokeTokens = false
        var passwordChanged = false

        customer.customerCode = customerCode
        customer.customerName = customerName
        customer.mobile = request.mobile.cleanOrNull()
        customer.phone = request.phone.cleanOrNull()
        customer.center = request.center.cleanOrNull()
        customer.nationalCode = request.nationalCode.cleanOrNull()
        customer.role = normalizedRole
        customer.enabled = request.enabled

        if (oldCustomerCode != customerCode) {
            shouldRevokeTokens = true
        }

        if (oldRole != normalizedRole) {
            shouldRevokeTokens = true
        }

        if (oldEnabled && !customer.enabled) {
            shouldRevokeTokens = true
        }

        val newPassword = request.password
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (newPassword != null) {
            passwordPolicyValidator.validate(
                password = newPassword,
                customerCode = customerCode
            )

            customer.passwordHash = passwordEncoder.encode(newPassword)
            customer.salt = PASSWORD_ALGORITHM
            customer.passwordChangeRequired = true

            passwordChanged = true
            shouldRevokeTokens = true
        }

        val savedCustomer = customerRepository.save(customer)

        if (shouldRevokeTokens) {
            refreshTokenService.revokeAllForCustomer(savedCustomer)
        }

        auditLogService.record(
            action = "CUSTOMER_UPDATED",
            entityType = ENTITY_TYPE_CUSTOMER,
            entityId = savedCustomer.id?.toString(),
            oldValue = oldSnapshot,
            newValue = customerSnapshot(savedCustomer) + mapOf(
                "passwordChanged" to passwordChanged,
                "tokensRevoked" to shouldRevokeTokens
            ),
            description = "Customer updated by admin"
        )

        return savedCustomer.toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val customer = customerRepository.findById(id)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        if (cartRepository.countByCustomerId(id) > 0) {
            throw BadRequestException("این مشتری دارای سفارش است و قابل حذف نیست")
        }

        val oldSnapshot = customerSnapshot(customer)

        val addresses = addressRepository.findByCustomerId(id)
        if (addresses.isNotEmpty()) {
            addressRepository.deleteAll(addresses)
        }

        refreshTokenService.deleteAllForCustomer(customer)

        auditLogService.record(
            action = "CUSTOMER_DELETED",
            entityType = ENTITY_TYPE_CUSTOMER,
            entityId = customer.id?.toString() ?: id.toString(),
            oldValue = oldSnapshot,
            newValue = null,
            description = "Customer deleted by admin"
        )

        customerRepository.delete(customer)
    }

    private fun customerSnapshot(customer: Customer): Map<String, Any?> =
        auditLogService.snapshotOf(
            "id" to customer.id,
            "customerCode" to customer.customerCode,
            "customerName" to customer.customerName,
            "mobile" to customer.mobile,
            "phone" to customer.phone,
            "center" to customer.center,
            "nationalCode" to customer.nationalCode,
            "role" to customer.role,
            "enabled" to customer.enabled,
            "passwordChangeRequired" to customer.passwordChangeRequired
        )

    private companion object {
        const val PASSWORD_ALGORITHM = "bcrypt"
        const val DEFAULT_SORT_BY = "createdAt"
        const val DEFAULT_SORT_DIRECTION = "DESC"
        const val ENTITY_TYPE_CUSTOMER = "Customer"

        val ALLOWED_SORTS = setOf(
            "createdAt",
            "customerCode",
            "customerName"
        )
    }
}