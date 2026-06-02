package com.msa.eshop.backend.api.admin

import com.fasterxml.jackson.databind.JsonNode
import com.msa.eshop.backend.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class AdminAuthorizationIntegrationTest : IntegrationTestBase() {

    @Test
    fun `customer token should not access admin endpoint`() {
        val token = login(
            customerCode = "1001",
            password = "123456"
        )

        mockMvc.get("/api/v1/admin/products") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `admin token should access admin endpoint`() {
        val token = login(
            customerCode = "admin",
            password = "admin123"
        )

        mockMvc.get("/api/v1/admin/products") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
        }
    }

    private fun login(
        customerCode: String,
        password: String
    ): String {
        val response = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerCode": "$customerCode",
                  "password": "$password"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        val json = objectMapper.readTree(response)
        return json["data"]["accessToken"].asText()
    }
}