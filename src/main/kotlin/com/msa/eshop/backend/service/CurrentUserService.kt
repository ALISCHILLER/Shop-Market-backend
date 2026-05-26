package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.UnauthorizedException
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.domain.CustomerRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService(
    private val customerRepository: CustomerRepository
) {
    fun requireCustomer(): Customer {
        val code = SecurityContextHolder.getContext().authentication?.name
            ?: throw UnauthorizedException()
        return customerRepository.findByCustomerCode(code) ?: throw UnauthorizedException()
    }
}
