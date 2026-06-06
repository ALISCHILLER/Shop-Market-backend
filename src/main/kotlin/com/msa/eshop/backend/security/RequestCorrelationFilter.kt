package com.msa.eshop.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestCorrelationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)
            ?.takeIf { it.isNotBlank() }
            ?.take(MAX_REQUEST_ID_LENGTH)
            ?: UUID.randomUUID().toString()

        MDC.put("requestId", requestId)
        MDC.put("httpMethod", request.method)
        MDC.put("httpPath", request.requestURI)

        response.setHeader(REQUEST_ID_HEADER, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }

    private companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val MAX_REQUEST_ID_LENGTH = 128
    }
}