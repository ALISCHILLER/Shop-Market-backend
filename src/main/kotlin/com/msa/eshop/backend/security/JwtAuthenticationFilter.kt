package com.msa.eshop.backend.security

import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.domain.CustomerRole
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val customerRepository: CustomerRepository
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (SecurityContextHolder.getContext().authentication != null) {
            filterChain.doFilter(request, response)
            return
        }

        val token = extractBearerToken(request)

        if (token != null) {
            authenticateByToken(
                token = token,
                request = request
            )
        }

        filterChain.doFilter(request, response)
    }

    private fun authenticateByToken(
        token: String,
        request: HttpServletRequest
    ) {
        val claims = jwtTokenService.parseToken(token) ?: return

        val claimUserId = runCatching {
            UUID.fromString(claims.userId)
        }.getOrNull() ?: return

        val customer = customerRepository.findByCustomerCode(claims.subject) ?: return

        if (!customer.enabled) return
        if (customer.id != claimUserId) return

        val tokenRole = CustomerRole.normalize(claims.role)
        val currentRole = CustomerRole.normalize(customer.role)

        if (tokenRole != currentRole) return

        val authorities = listOf(
            SimpleGrantedAuthority("ROLE_${currentRole.name}")
        )

        val authentication = UsernamePasswordAuthenticationToken(
            customer.customerCode,
            null,
            authorities
        )

        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null

        if (!header.startsWith("Bearer ", ignoreCase = true)) {
            return null
        }

        return header.substringAfter("Bearer ", "")
            .trim()
            .takeIf { it.isNotBlank() }
    }
}