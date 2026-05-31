package com.msa.eshop.backend.service.auth

import com.msa.eshop.backend.common.BadRequestException
import org.springframework.stereotype.Component

@Component
class PasswordPolicyValidator {

    fun validate(
        password: String,
        customerCode: String? = null
    ) {
        val normalizedPassword = password.trim()

        if (normalizedPassword.length < MIN_PASSWORD_LENGTH) {
            throw BadRequestException("رمز عبور باید حداقل $MIN_PASSWORD_LENGTH کاراکتر باشد")
        }

        if (!normalizedPassword.any { it.isLetter() }) {
            throw BadRequestException("رمز عبور باید حداقل یک حرف داشته باشد")
        }

        if (!normalizedPassword.any { it.isDigit() }) {
            throw BadRequestException("رمز عبور باید حداقل یک عدد داشته باشد")
        }

        if (customerCode != null && normalizedPassword.equals(customerCode, ignoreCase = true)) {
            throw BadRequestException("رمز عبور نباید با کد مشتری یکسان باشد")
        }

        if (normalizedPassword.lowercase() in WEAK_PASSWORDS) {
            throw BadRequestException("رمز عبور بیش از حد ساده است")
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8

        val WEAK_PASSWORDS = setOf(
            "123456",
            "12345678",
            "123456789",
            "password",
            "admin123",
            "qwerty123",
            "11111111",
            "00000000"
        )
    }
}