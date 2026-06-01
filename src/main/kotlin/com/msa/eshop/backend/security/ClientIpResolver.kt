package com.msa.eshop.backend.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ClientIpResolver(
    @param:Value("\${app.security.trust-forwarded-headers:false}")
    private val trustForwardedHeaders: Boolean
) {
    fun resolve(request: HttpServletRequest): String {
        if (trustForwardedHeaders) {
            forwardedFor(request)?.let { return it }
            realIp(request)?.let { return it }
            forwardedHeader(request)?.let { return it }
        }

        return request.remoteAddr
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: UNKNOWN_IP
    }

    private fun forwardedFor(request: HttpServletRequest): String? =
        request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.take(MAX_IP_LENGTH)

    private fun realIp(request: HttpServletRequest): String? =
        request.getHeader("X-Real-IP")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.take(MAX_IP_LENGTH)

    private fun forwardedHeader(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("Forwarded")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val forPart = forwarded
            .split(";")
            .firstOrNull { it.trim().startsWith("for=", ignoreCase = true) }
            ?.substringAfter("=", "")
            ?.trim()
            ?.trim('"')
            ?.takeIf { it.isNotBlank() }

        return forPart?.take(MAX_IP_LENGTH)
    }

    private companion object {
        const val UNKNOWN_IP = "unknown"
        const val MAX_IP_LENGTH = 64
    }
}