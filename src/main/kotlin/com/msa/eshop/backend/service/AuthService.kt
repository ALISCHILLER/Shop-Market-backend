package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.ChangePasswordRequest
import com.msa.eshop.backend.common.TokenRequest
import com.msa.eshop.backend.common.TokenResponse
import com.msa.eshop.backend.common.UnauthorizedException
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.security.JwtTokenService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val customerRepository: CustomerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService,
    private val currentUserService: CurrentUserService
) {
    @Transactional(readOnly = true)
    fun login(request: TokenRequest): TokenResponse {
        val customerCode = request.customerCode?.trim().orEmpty()
        val password = request.password?.trim().orEmpty()

        if (customerCode.isBlank() || password.isBlank()) {
            throw BadRequestException("کد مشتری و رمز عبور الزامی است")
        }

        val customer = customerRepository.findByCustomerCode(customerCode)
            ?: throw UnauthorizedException("کد مشتری یا رمز عبور اشتباه است")

        if (!customer.enabled) {
            throw UnauthorizedException("حساب کاربری غیرفعال است")
        }

        if (!passwordEncoder.matches(password, customer.passwordHash)) {
            throw UnauthorizedException("کد مشتری یا رمز عبور اشتباه است")
        }

        return TokenResponse(jwtTokenService.generateToken(customer))
    }

    @Transactional
    fun changePassword(request: ChangePasswordRequest): Boolean {
        val customer = currentUserService.requireCustomer()

        val oldPassword = request.oldPassword.trim()
        val newPassword = request.newPassword.trim()

        if (oldPassword.isBlank() || newPassword.isBlank()) {
            throw BadRequestException("رمز عبور فعلی و جدید الزامی است")
        }

        if (!passwordEncoder.matches(oldPassword, customer.passwordHash)) {
            throw BadRequestException("رمز عبور فعلی اشتباه است")
        }

        if (newPassword.length < 6) {
            throw BadRequestException("رمز عبور جدید باید حداقل ۶ کاراکتر باشد")
        }

        if (oldPassword == newPassword) {
            throw BadRequestException("رمز عبور جدید نباید با رمز عبور قبلی یکسان باشد")
        }

        customer.passwordHash = passwordEncoder.encode(newPassword)
        customer.salt = "bcrypt"

        customerRepository.save(customer)
        return true
    }
}