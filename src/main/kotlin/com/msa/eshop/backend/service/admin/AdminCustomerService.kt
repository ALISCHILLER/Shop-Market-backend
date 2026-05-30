package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.UpsertCustomerRequest
import com.msa.eshop.backend.common.UserDto
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.cleanRequired
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.domain.CustomerRole
import com.msa.eshop.backend.service.toDto
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminCustomerService(
    private val customerRepository: CustomerRepository,
    private val addressRepository: CustomerAddressRepository,
    private val cartRepository: CartRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional(readOnly = true)
    fun findAll(): List<UserDto> =
        customerRepository.findAll()
            .sortedBy { it.customerCode }
            .map { it.toDto() }

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
            ?: DEFAULT_PASSWORD

        validatePassword(rawPassword)

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
        )

        return customerRepository.save(customer).toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertCustomerRequest): UserDto {
        val customer = customerRepository.findById(id)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        val customerCode = request.customerCode.cleanRequired("کد مشتری الزامی است")
        val customerName = request.customerName.cleanRequired("نام مشتری الزامی است")

        if (customerRepository.existsByCustomerCodeAndIdNot(customerCode, id)) {
            throw BadRequestException("کد مشتری قبلاً برای مشتری دیگری ثبت شده است")
        }

        customer.customerCode = customerCode
        customer.customerName = customerName
        customer.mobile = request.mobile.cleanOrNull()
        customer.phone = request.phone.cleanOrNull()
        customer.center = request.center.cleanOrNull()
        customer.nationalCode = request.nationalCode.cleanOrNull()
        customer.role = CustomerRole.normalize(request.role).name
        customer.enabled = request.enabled

        request.password
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { newPassword ->
                validatePassword(newPassword)
                customer.passwordHash = passwordEncoder.encode(newPassword)
                customer.salt = PASSWORD_ALGORITHM
            }

        return customerRepository.save(customer).toDto()
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

        customerRepository.delete(customer)
    }

    private fun validatePassword(password: String) {
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw BadRequestException("رمز عبور باید حداقل ۶ کاراکتر باشد")
        }
    }

    private companion object {
        const val DEFAULT_PASSWORD = "123456"
        const val PASSWORD_ALGORITHM = "bcrypt"
        const val MIN_PASSWORD_LENGTH = 6
    }
}