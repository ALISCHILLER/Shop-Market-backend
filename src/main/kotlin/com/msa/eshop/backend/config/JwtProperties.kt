package com.msa.eshop.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    var secret: String = "change-me-to-a-very-long-secret-at-least-32-bytes",
    var expirationMinutes: Long = 1440
)
