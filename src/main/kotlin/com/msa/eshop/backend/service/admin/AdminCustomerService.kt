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
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.domain.CustomerRole
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
    private val refreshTokenService: RefreshTokenService
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
        sortBy: String = "createdAt",
        direction: String = "DESC"
    ): PageResponseDto<UserDto> {
        val normalizedRole = role
            .cleanOrNull()
            ?.let { CustomerRole.normalize(it).name }

        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            allowedSorts = setOf("createdAt", "customerCode", "customerName")
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
            role = CustomerRole.normalize(request.role).name,
            enabled = request.enabled
        ).apply {
            passwordChangeRequired = true
        }

        return customerRepository.save(customer).toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertCustomerRequest): UserDto {
        val customer = customerRepository.findById(id)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

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

        request.password
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { newPassword ->
                passwordPolicyValidator.validate(
                    password = newPassword,
                    customerCode = customerCode
                )

                customer.passwordHash = passwordEncoder.encode(newPassword)
                customer.salt = PASSWORD_ALGORITHM
                customer.passwordChangeRequired = true

                shouldRevokeTokens = true
            }

        val savedCustomer = customerRepository.save(customer)

        if (shouldRevokeTokens) {
            refreshTokenService.revokeAllForCustomer(savedCustomer)
        }

        return savedCustomer.toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val customer = customerRepository.findById(id)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        if (cartRepository.countByCustomerId(id) > 0) {
            throw BadRequestException("این مشتری دارای سفارش است و قابل حذف نیست")
        }

        val addresses = addressRepository.findByCustomerId(id)
        if (addresses.isNotEmpty()) {
            addressRepository.deleteAll(addresses)
        }

        refreshTokenService.deleteAllForCustomer(customer)

        customerRepository.delete(customer)
    }

    private companion object {
        const val PASSWORD_ALGORITHM = "bcrypt"
    }
}