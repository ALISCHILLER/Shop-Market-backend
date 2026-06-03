package com.msa.eshop.backend.api

import com.msa.eshop.backend.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class CartCheckoutIntegrationTest : IntegrationTestBase() {

    @Test
    fun `customer should checkout and read cart details`() {
        val token = login("1001", "123456")

        val checkoutResponse = mockMvc.post("/api/v1/cart/checkout") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerAddressId": "00000000-0000-0000-0000-000000003001",
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
            jsonPath("$.data.cartCode") { exists() }
            jsonPath("$.data.total") { exists() }
        }.andReturn().response.contentAsString

        val cartCode = objectMapper.readTree(checkoutResponse)["data"]["cartCode"].asInt()

        mockMvc.get("/api/v1/cart/$cartCode") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.cartCode") { value(cartCode) }
            jsonPath("$.data.items") { exists() }
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