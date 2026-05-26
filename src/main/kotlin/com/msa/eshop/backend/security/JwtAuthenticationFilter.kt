package com.msa.eshop.backend.security

import com.msa.eshop.backend.domain.CustomerRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
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
        val header = request.getHeader("Authorization").orEmpty()
        if (header.startsWith("Bearer ", ignoreCase = true) && SecurityContextHolder.getContext().authentication == null) {
            val token = header.substringAfter(" ").trim()
            val claims = jwtTokenService.parseToken(token)
            if (claims != null) {
                val customer = customerRepository.findByCustomerCode(claims.subject)
                if (customer != null && customer.enabled) {
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${customer.role.uppercase()}"))
                    val authentication = UsernamePasswordAuthenticationToken(customer.customerCode, null, authorities)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}
