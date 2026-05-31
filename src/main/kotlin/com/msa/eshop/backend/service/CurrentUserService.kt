package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.UnauthorizedException
import com.msa.eshop.backend.domain.entity.Customer
import com.msa.eshop.backend.domain.repository.CustomerRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CurrentUserService(
    private val customerRepository: CustomerRepository
) {
    @Transactional(readOnly = true)
    fun requireCustomer(): Customer {
        val customerCode = SecurityContextHolder.getContext()
            .authentication
            ?.name
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw UnauthorizedException()

        val customer = customerRepository.findByCustomerCode(customerCode)
            ?: throw UnauthorizedException()

        if (!customer.enabled) {
            throw UnauthorizedException("حساب کاربری غیرفعال است")
        }

        return customer
    }

    fun isAdmin(customer: Customer): Boolean =
        customer.role.equals("ADMIN", ignoreCase = true)
}