package com.msa.eshop.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("prod")
class StartupValidation(
    private val jwtProperties: JwtProperties,
    @Value("\${app.cors.allowed-origins:*}") private val allowedOrigins: String
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val secret = jwtProperties.secret.trim()

        if (
            secret.isBlank() ||
            secret.startsWith("change-me", ignoreCase = true) ||
            secret.length < 64
        ) {
            error("ESHOP_JWT_SECRET must be configured with a strong secret of at least 64 characters in prod profile")
        }

        if (jwtProperties.expirationMinutes <= 0) {
            error("ESHOP_JWT_EXPIRATION_MINUTES must be greater than zero")
        }

        val origins = allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (origins.contains("*")) {
            error("CORS_ALLOWED_ORIGINS must not be '*' in prod profile")
        }
    }
}