package com.msa.eshop.backend.api

import com.fasterxml.jackson.databind.JsonNode
import com.msa.eshop.backend.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

class RefreshTokenReuseDetectionIntegrationTest : IntegrationTestBase() {

    @Test
    fun `reusing replaced refresh token should revoke token family`() {
        val loginJson = login("1001", "123456")
        val firstRefreshToken = loginJson["data"]["refreshToken"].asText()

        val firstRefreshJson = refresh(firstRefreshToken)
        val secondRefreshToken = firstRefreshJson["data"]["refreshToken"].asText()

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "refreshToken": "$firstRefreshToken"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "refreshToken": "$secondRefreshToken"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    private fun login(customerCode: String, password: String): JsonNode {
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

        return objectMapper.readTree(response)
    }

    private fun refresh(refreshToken: String): JsonNode {
        val response = mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "refreshToken": "$refreshToken"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        return objectMapper.readTree(response)
    }
}