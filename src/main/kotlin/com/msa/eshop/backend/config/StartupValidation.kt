package com.msa.eshop.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class StartupValidation(
    private val environment: Environment,
    private val jwtProperties: JwtProperties,
    @param:Value("\${app.cors.allowed-origins:*}") private val allowedOrigins: String,
    @param:Value("\${app.auth.refresh-token-expiration-days:30}") private val refreshTokenExpirationDays: Long,
    @param:Value("\${app.security.trust-forwarded-headers:false}") private val trustForwardedHeaders: Boolean
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        validateCommonSettings()

        if (isStrictRuntimeProfile()) {
            validateStrictRuntimeSettings()
        }
    }

    private fun validateCommonSettings() {
        if (jwtProperties.expirationMinutes <= 0) {
            error("ESHOP_JWT_EXPIRATION_MINUTES must be greater than zero")
        }

        if (refreshTokenExpirationDays <= 0) {
            error("ESHOP_REFRESH_TOKEN_EXPIRATION_DAYS must be greater than zero")
        }
    }

    private fun validateStrictRuntimeSettings() {
        val secret = jwtProperties.secret.trim()

        if (
            secret.isBlank() ||
            secret.length < MIN_STRONG_JWT_SECRET_LENGTH ||
            isPlaceholderSecret(secret)
        ) {
            error(
                "ESHOP_JWT_SECRET must be configured with a real strong random secret " +
                        "of at least $MIN_STRONG_JWT_SECRET_LENGTH characters outside dev/test/local profiles"
            )
        }

        if (jwtProperties.expirationMinutes > MAX_STRICT_ACCESS_TOKEN_MINUTES) {
            error("ESHOP_JWT_EXPIRATION_MINUTES must be <= $MAX_STRICT_ACCESS_TOKEN_MINUTES outside dev/test/local profiles")
        }

        if (refreshTokenExpirationDays > MAX_STRICT_REFRESH_TOKEN_DAYS) {
            error("ESHOP_REFRESH_TOKEN_EXPIRATION_DAYS must be <= $MAX_STRICT_REFRESH_TOKEN_DAYS outside dev/test/local profiles")
        }

        val origins = allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (origins.isEmpty() || origins.any { it == "*" }) {
            error("CORS_ALLOWED_ORIGINS must contain explicit trusted origins outside dev/test/local profiles")
        }

        if (trustForwardedHeaders && !hasTrustedProxyProfile()) {
            error("app.security.trust-forwarded-headers=true is allowed only when traffic is behind a trusted reverse proxy")
        }
    }

    private fun isStrictRuntimeProfile(): Boolean {
        val activeProfiles = environment.activeProfiles.map { it.lowercase() }.toSet()

        if (activeProfiles.isEmpty()) {
            return false
        }

        return activeProfiles.none { it in RELAXED_PROFILES }
    }

    private fun hasTrustedProxyProfile(): Boolean {
        val activeProfiles = environment.activeProfiles.map { it.lowercase() }.toSet()

        return activeProfiles.any { it in TRUSTED_PROXY_PROFILES }
    }

    private fun isPlaceholderSecret(secret: String): Boolean {
        val normalized = secret.lowercase()

        return PLACEHOLDER_SECRET_PATTERNS.any { pattern ->
            normalized.contains(pattern)
        }
    }

    private companion object {
        const val MIN_STRONG_JWT_SECRET_LENGTH = 64
        const val MAX_STRICT_ACCESS_TOKEN_MINUTES = 120L
        const val MAX_STRICT_REFRESH_TOKEN_DAYS = 90L

        val RELAXED_PROFILES = setOf("dev", "test", "local")
        val TRUSTED_PROXY_PROFILES = setOf("prod", "staging")

        val PLACEHOLDER_SECRET_PATTERNS = setOf(
            "change-me",
            "change_me",
            "changeme",
            "change-this",
            "change_this",
            "changethis",
            "please-change",
            "replace-me",
            "replace_this",
            "replace-this",
            "replace-with",
            "your-secret",
            "sample-secret",
            "example-secret",
            "default-secret",
            "production-secret",
            "prod-secret",
            "jwt-secret",
            "real-random",
            "real_random",
            "random-64",
            "random_64",
            "64-plus",
            "64_plus"
        )
    }
}