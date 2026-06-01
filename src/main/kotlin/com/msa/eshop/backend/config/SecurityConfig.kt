package com.msa.eshop.backend.config

import com.msa.eshop.backend.security.AuthRateLimitFilter
import com.msa.eshop.backend.security.JwtAuthenticationFilter
import com.msa.eshop.backend.security.RestAccessDeniedHandler
import com.msa.eshop.backend.security.RestAuthenticationEntryPoint
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val authRateLimitFilter: AuthRateLimitFilter,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
    private val restAccessDeniedHandler: RestAccessDeniedHandler,
    @param:Value("\${app.cors.allowed-origins:*}") private val allowedOrigins: String
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

    @Bean
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager =
        configuration.authenticationManager

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        val origins = allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (origins.contains("*")) {
            configuration.addAllowedOriginPattern("*")
        } else {
            configuration.allowedOrigins = origins
        }

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type", "Accept")
        configuration.exposedHeaders = listOf("Authorization", "Retry-After")
        configuration.allowCredentials = false
        configuration.maxAge = CORS_MAX_AGE_SECONDS

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
        http.cors { it.configurationSource(corsConfigurationSource()) }
        http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        http.exceptionHandling { exceptions ->
            exceptions.authenticationEntryPoint(restAuthenticationEntryPoint)
            exceptions.accessDeniedHandler(restAccessDeniedHandler)
        }

        http.authorizeHttpRequests { auth ->
            auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Legacy auth endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/User/loginUser").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/User/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/User/logout").permitAll()

                // Modern auth endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()

                // Legacy public catalog
                .requestMatchers(HttpMethod.GET, "/api/v1/Product/**", "/api/v1/Banner/**").permitAll()

                // Modern public catalog
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/product-categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/banners").permitAll()

                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        }

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        http.addFilterBefore(authRateLimitFilter, JwtAuthenticationFilter::class.java)

        return http.build()
    }

    private companion object {
        const val CORS_MAX_AGE_SECONDS = 3600L
    }
}