package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.UnauthorizedException
import com.msa.eshop.backend.common.dtos.ChangePasswordRequest
import com.msa.eshop.backend.common.dtos.LoginDataDto
import com.msa.eshop.backend.common.dtos.LogoutRequest
import com.msa.eshop.backend.common.dtos.RefreshTokenRequest
import com.msa.eshop.backend.common.dtos.RefreshTokenResponseDto
import com.msa.eshop.backend.common.dtos.TokenRequest
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
        request: TokenRequest,
        ipAddress: String?,
        userAgent: String?
    ): LoginDataDto {
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

        val token = jwtTokenService.generateToken(customer)

        val refreshToken = refreshTokenService.create(
            customer = customer,
            ipAddress = ipAddress,
            userAgent = userAgent
        )

        return LoginDataDto(
            token = token,
            refreshToken = refreshToken,
            passwordChangeRequired = customer.passwordChangeRequired
        )
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

    @Transactional
    fun refreshToken(
        request: RefreshTokenRequest,
        ipAddress: String?,
        userAgent: String?
    ): RefreshTokenResponseDto {
        val rotation = refreshTokenService.rotate(
            rawToken = request.refreshToken,
            ipAddress = ipAddress,
            userAgent = userAgent
        )

        val customer = rotation.customer
        val newAccessToken = jwtTokenService.generateToken(customer)

        return RefreshTokenResponseDto(
            token = newAccessToken,
            refreshToken = rotation.refreshToken,
            passwordChangeRequired = customer.passwordChangeRequired
        )
    }

    @Transactional
    fun logout(request: LogoutRequest): Boolean {
        refreshTokenService.revoke(request.refreshToken)
        return true
    }

    private companion object {
        const val PASSWORD_ALGORITHM = "bcrypt"
    }
}