package com.msa.eshop.backend.api

import com.msa.eshop.backend.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class AuthControllerIntegrationTest : IntegrationTestBase() {

    @Test
    fun `login should return access and refresh tokens`() {
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerCode": "1001",
                  "password": "123456"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.hasError") { value(false) }
            jsonPath("$.data.accessToken") { exists() }
            jsonPath("$.data.refreshToken") { exists() }
            jsonPath("$.data.tokenType") { value("Bearer") }
        }
    }

    @Test
    fun `login should reject wrong password`() {
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerCode": "1001",
                  "password": "wrong-password"
                }
            """.trimIndent()
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `me should require authentication`() {
        mockMvc.get("/api/v1/auth/me")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}