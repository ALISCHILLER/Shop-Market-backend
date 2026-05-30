package com.msa.eshop.backend.security

import com.msa.eshop.backend.domain.CustomerRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

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
            val claims = jwtTokenService.parseToken(token)

            if (claims != null) {
                val customer = customerRepository.findByCustomerCode(claims.subject)

                if (customer != null && customer.enabled) {
                    val authorities = listOf(
                        SimpleGrantedAuthority("ROLE_${customer.role.uppercase().removePrefix("ROLE_")}")
                    )

                    val authentication = UsernamePasswordAuthenticationToken(
                        customer.customerCode,
                        null,
                        authorities
                    )

                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }

        filterChain.doFilter(request, response)
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