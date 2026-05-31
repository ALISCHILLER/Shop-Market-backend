package com.msa.eshop.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.msa.eshop.backend.config.JwtProperties
import com.msa.eshop.backend.domain.Customer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtTokenServiceTest {

    private val service = JwtTokenService(
        jwtProperties = JwtProperties(
            secret = "a".repeat(64),
            expirationMinutes = 60
        ),
        objectMapper = ObjectMapper().registerKotlinModule()
    )

    @Test
    fun `generate and parse token should return claims`() {
        val userId = UUID.randomUUID()

        val customer = Customer(
            customerCode = "C001",
            customerName = "Customer",
            role = "ADMIN"
        ).apply {
            id = userId
        }

        val token = service.generateToken(customer)
        val claims = service.parseToken(token)

        assertNotNull(claims)
        assertEquals("C001", claims?.subject)
        assertEquals(userId.toString(), claims?.userId)
        assertEquals("ADMIN", claims?.role)
    }

    @Test
    fun `tampered token should be rejected`() {
        val userId = UUID.randomUUID()

        val customer = Customer(
            customerCode = "C001",
            customerName = "Customer",
            role = "CUSTOMER"
        ).apply {
            id = userId
        }

        val token = service.generateToken(customer)
        val tampered = token.dropLast(2) + "xx"

        assertNull(service.parseToken(tampered))
    }
}