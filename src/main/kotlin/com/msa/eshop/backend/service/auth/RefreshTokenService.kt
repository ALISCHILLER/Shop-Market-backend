package com.msa.eshop.backend.service.auth

import com.msa.eshop.backend.common.BadRequestException
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
        val tokenHash = hash(rawToken)

        val refreshToken = RefreshToken(
            customer = customer,
            tokenHash = tokenHash,
            expiresAt = OffsetDateTime.now().plusDays(refreshTokenExpirationDays),
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
    fun revoke(rawToken: String) {
        val tokenHash = hash(rawToken)

        val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
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

    private fun generateRawToken(): String {
        val bytes = ByteArray(64)
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
        val secureRandom = SecureRandom()
    }
}