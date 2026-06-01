package com.msa.eshop.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.config.AuthRateLimitProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

@Component
class AuthRateLimitFilter(
    private val properties: AuthRateLimitProperties,
    private val rateLimitService: AuthRateLimitService,
    private val clientIpResolver: ClientIpResolver,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (!properties.enabled) return true
        return resolveRule(request) == null
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val matchedRule = resolveRule(request)
        if (matchedRule == null) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = clientIpResolver.resolve(request)
        val key = "auth:${matchedRule.name}:$clientIp"
        val rule = matchedRule.rule

        val decision = rateLimitService.tryConsume(
            key = key,
            capacity = rule.capacity,
            window = Duration.ofSeconds(rule.windowSeconds)
        )

        if (!decision.allowed) {
            writeTooManyRequests(response, decision.retryAfterSeconds)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveRule(request: HttpServletRequest): MatchedRule? {
        if (request.method != HttpMethod.POST.name()) return null

        val path = request.servletPath.ifBlank { request.requestURI }

        return when (path) {
            "/api/v1/User/loginUser",
            "/api/v1/auth/login" ->
                MatchedRule("login", properties.login)

            "/api/v1/User/refresh",
            "/api/v1/auth/refresh" ->
                MatchedRule("refresh", properties.refresh)

            "/api/v1/User/logout",
            "/api/v1/auth/logout" ->
                MatchedRule("logout", properties.logout)

            "/api/v1/User/changepassword",
            "/api/v1/auth/change-password" ->
                MatchedRule("change-password", properties.changePassword)

            else -> null
        }
    }

    private fun writeTooManyRequests(
        response: HttpServletResponse,
        retryAfterSeconds: Long
    ) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.setHeader("Retry-After", retryAfterSeconds.toString())

        objectMapper.writeValue(
            response.writer,
            BaseResponse<Nothing>(
                data = null,
                hasError = true,
                message = "تعداد درخواست‌ها بیش از حد مجاز است. لطفاً کمی بعد دوباره تلاش کنید"
            )
        )
    }

    private data class MatchedRule(
        val name: String,
        val rule: AuthRateLimitProperties.Rule
    )
}