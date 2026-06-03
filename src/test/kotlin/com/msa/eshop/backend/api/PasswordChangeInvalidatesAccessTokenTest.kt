package com.msa.eshop.backend.api

import com.msa.eshop.backend.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class PasswordChangeInvalidatesAccessTokenTest : IntegrationTestBase() {

    @Test
    fun `changing password should invalidate previous access token`() {
        val oldToken = login("1001", "123456")

        mockMvc.post("/api/v1/auth/change-password") {
            header("Authorization", "Bearer $oldToken")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "oldPassword": "123456",
                  "newPassword": "NewStrongPassword123"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }

        mockMvc.get("/api/v1/auth/me") {
            header("Authorization", "Bearer $oldToken")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    private fun login(customerCode: String, password: String): String {
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

        return objectMapper.readTree(response)["data"]["accessToken"].asText()
    }
}