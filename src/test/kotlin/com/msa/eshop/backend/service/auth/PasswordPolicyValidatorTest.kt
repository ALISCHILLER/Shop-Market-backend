package com.msa.eshop.backend.service.auth

import com.msa.eshop.backend.common.BadRequestException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PasswordPolicyValidatorTest {

    private val validator = PasswordPolicyValidator()

    @Test
    fun `validate should accept strong password`() {
        assertDoesNotThrow {
            validator.validate(
                password = "StrongPass123",
                customerCode = "C001"
            )
        }
    }

    @Test
    fun `validate should reject short password`() {
        assertThrows(BadRequestException::class.java) {
            validator.validate("A1")
        }
    }

    @Test
    fun `validate should reject password without digit`() {
        assertThrows(BadRequestException::class.java) {
            validator.validate("StrongPassword")
        }
    }

    @Test
    fun `validate should reject password without letter`() {
        assertThrows(BadRequestException::class.java) {
            validator.validate("12345678")
        }
    }

    @Test
    fun `validate should reject weak password`() {
        assertThrows(BadRequestException::class.java) {
            validator.validate("admin123")
        }
    }

    @Test
    fun `validate should reject password equal to customer code`() {
        assertThrows(BadRequestException::class.java) {
            validator.validate(
                password = "C001",
                customerCode = "C001"
            )
        }
    }
}

