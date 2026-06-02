package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.UnauthorizedException
import com.msa.eshop.backend.common.dtos.ChangePasswordRequest
import com.msa.eshop.backend.common.dtos.LoginRequest
import com.msa.eshop.backend.common.dtos.LoginResponse
import com.msa.eshop.backend.common.dtos.LogoutRequest
import com.msa.eshop.backend.common.dtos.RefreshTokenRequest
import com.msa.eshop.backend.common.dtos.RefreshTokenResponse
import com.msa.eshop.backend.domain.repository.CustomerRepository
import com.msa.eshop.backend.security.JwtTokenService
import com.msa.eshop.backend.service.auth.PasswordPolicyValidator
import com.msa.eshop.backend.service.auth.RefreshTokenService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val customerRepository: CustomerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService,
    private val currentUserService: CurrentUserService,
    private val passwordPolicyValidator: PasswordPolicyValidator,
    private val refreshTokenService: RefreshTokenService
) {

    @Transactional
    fun login(
        request: LoginRequest,
        ipAddress: String?,
        userAgent: String?
    ): LoginResponse {
        val customerCode = request.customerCode.trim()
        val password = request.password.trim()

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

        val accessToken = jwtTokenService.generateToken(customer)

        val refreshToken = refreshTokenService.create(
            customer = customer,
            ipAddress = ipAddress,
            userAgent = userAgent
        )

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            passwordChangeRequired = customer.passwordChangeRequired
        )
    }

    @Transactional
    fun refreshToken(
        request: RefreshTokenRequest,
        ipAddress: String?,
        userAgent: String?
    ): RefreshTokenResponse {
        val rotation = refreshTokenService.rotate(
            rawToken = request.refreshToken,
            ipAddress = ipAddress,
            userAgent = userAgent
        )

        val customer = rotation.customer

        return RefreshTokenResponse(
            accessToken = jwtTokenService.generateToken(customer),
            refreshToken = rotation.refreshToken,
            passwordChangeRequired = customer.passwordChangeRequired
        )
    }

    @Transactional
    fun logout(request: LogoutRequest): Boolean {
        refreshTokenService.revoke(request.refreshToken)
        return true
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

        if (oldPassword == newPassword) {
            throw BadRequestException("رمز عبور جدید نباید با رمز عبور قبلی یکسان باشد")
        }

        passwordPolicyValidator.validate(
            password = newPassword,
            customerCode = customer.customerCode
        )

        customer.passwordHash = passwordEncoder.encode(newPassword)
        customer.salt = PASSWORD_ALGORITHM
        customer.passwordChangeRequired = false

        refreshTokenService.revokeAllForCustomer(customer)
        customerRepository.save(customer)

        return true
    }

    private companion object {
        const val PASSWORD_ALGORITHM = "bcrypt"
    }
}