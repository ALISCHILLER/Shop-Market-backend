package com.msa.eshop.backend.config

import com.msa.eshop.backend.security.AuthRateLimitFilter
import com.msa.eshop.backend.security.JwtAuthenticationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterRegistrationConfig {

    @Bean
    fun jwtAuthenticationFilterRegistration(
        filter: JwtAuthenticationFilter
    ): FilterRegistrationBean<JwtAuthenticationFilter> =
        FilterRegistrationBean(filter).apply {
            isEnabled = false
        }

    @Bean
    fun authRateLimitFilterRegistration(
        filter: AuthRateLimitFilter
    ): FilterRegistrationBean<AuthRateLimitFilter> =
        FilterRegistrationBean(filter).apply {
            isEnabled = false
        }
}