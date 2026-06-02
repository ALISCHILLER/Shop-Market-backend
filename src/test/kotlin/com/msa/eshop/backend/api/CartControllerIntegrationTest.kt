package com.msa.eshop.backend.api

import com.msa.eshop.backend.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class CartControllerIntegrationTest : IntegrationTestBase() {

    @Test
    fun `cart endpoints should require authentication`() {
        mockMvc.get("/api/v1/cart/payment-terms")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `authenticated customer should simulate cart`() {
        val token = loginCustomer()

        mockMvc.post("/api/v1/cart/simulate") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "paymentTermId": "16ccab60-279b-410a-90d1-b2673d5d1dd1",
                  "items": [
                    {
                      "productCode": 100101,
                      "quantity": 2
                    }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.hasError") { value(false) }
            jsonPath("$.data.total") { exists() }
            jsonPath("$.data.items") { exists() }
        }
    }

    private fun loginCustomer(): String {
        val response = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerCode": "1001",
                  "password": "123456"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        val json = objectMapper.readTree(response)
        return json["data"]["accessToken"].asText()
    }
}