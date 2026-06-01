package com.msa.eshop.backend.service.auth

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.UnauthorizedException
import com.msa.eshop.backend.domain.entity.Customer
import com.msa.eshop.backend.domain.entity.RefreshToken
import com.msa.eshop.backend.domain.repository.RefreshTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.util.Base64

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    @param:Value("\${app.auth.refresh-token-expiration-days:30}")
    private val refreshTokenExpirationDays: Long
) {

    @Transactional
    fun create(
        customer: Customer,
        ipAddress: String?,
        userAgent: String?
    ): String {
        val rawToken = generateRawToken()
        val refreshToken = buildRefreshToken(
            rawToken = rawToken,
            customer = customer,
            ipAddress = ipAddress,
            userAgent = userAgent
        )

        refreshTokenRepository.save(refreshToken)

        return rawToken
    }

    @Transactional(readOnly = true)
    fun validate(rawToken: String): RefreshToken {
        val tokenHash = hash(rawToken)

        val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw BadRequestException("Refresh token معتبر نیست")

        if (!refreshToken.isActive()) {
            throw BadRequestException("Refresh token منقضی یا غیرفعال شده است")
        }

        return refreshToken
    }

    @Transactional
    fun rotate(
        rawToken: String,
        ipAddress: String?,
        userAgent: String?
    ): RefreshTokenRotation {
        val tokenHash = hash(rawToken)
        val now = OffsetDateTime.now()

        val currentToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
            ?: throw BadRequestException("Refresh token معتبر نیست")

        if (!currentToken.isActive(now)) {
            throw BadRequestException("Refresh token منقضی یا غیرفعال شده است")
        }

        val customer = currentToken.customer

        if (!customer.enabled) {
            currentToken.revoke(now)
            throw UnauthorizedException("حساب کاربری غیرفعال است")
        }

        currentToken.revoke(now)

        val newRawToken = generateRawToken()
        val newRefreshToken = buildRefreshToken(
            rawToken = newRawToken,
            customer = customer,
            ipAddress = ipAddress,
            userAgent = userAgent
        )

        refreshTokenRepository.save(newRefreshToken)

        return RefreshTokenRotation(
            customer = customer,
            refreshToken = newRawToken
        )
    }

    @Transactional
    fun revoke(rawToken: String) {
        val tokenHash = hash(rawToken)

        val refreshToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
            ?: return

        if (refreshToken.revokedAt == null) {
            refreshToken.revoke()
        }
    }

    @Transactional
    fun revokeAllForCustomer(customer: Customer) {
        val customerId = requireNotNull(customer.id)

        refreshTokenRepository.findByCustomerIdAndRevokedAtIsNull(customerId)
            .forEach { refreshToken ->
                refreshToken.revoke()
            }
    }

    @Transactional
    fun deleteAllForCustomer(customer: Customer) {
        val customerId = requireNotNull(customer.id)
        refreshTokenRepository.deleteByCustomerId(customerId)
    }

    private fun buildRefreshToken(
        rawToken: String,
        customer: Customer,
        ipAddress: String?,
        userAgent: String?
    ): RefreshToken = RefreshToken(
        customer = customer,
        tokenHash = hash(rawToken),
        expiresAt = OffsetDateTime.now().plusDays(refreshTokenExpirationDays),
        ipAddress = ipAddress?.take(MAX_IP_LENGTH),
        userAgent = userAgent?.take(MAX_USER_AGENT_LENGTH)
    )

    private fun generateRawToken(): String {
        val bytes = ByteArray(RAW_TOKEN_BYTES)
        secureRandom.nextBytes(bytes)

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    private fun hash(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(rawToken.toByteArray(Charsets.UTF_8))

        return Base64.getEncoder().encodeToString(digest)
    }

    private companion object {
        const val RAW_TOKEN_BYTES = 64
        const val MAX_IP_LENGTH = 64
        const val MAX_USER_AGENT_LENGTH = 500
        val secureRandom = SecureRandom()
    }
}

data class RefreshTokenRotation(
    val customer: Customer,
    val refreshToken: String
)