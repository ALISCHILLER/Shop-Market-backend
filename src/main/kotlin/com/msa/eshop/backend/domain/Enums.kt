package com.msa.eshop.backend.domain

import com.msa.eshop.backend.common.BadRequestException

enum class CustomerRole {
    ADMIN,
    CUSTOMER;

    companion object {
        fun normalize(value: String?): CustomerRole {
            return when (value?.trim()?.uppercase()?.removePrefix("ROLE_")) {
                "ADMIN" -> ADMIN
                null, "", "CUSTOMER" -> CUSTOMER
                else -> throw BadRequestException("نقش کاربر معتبر نیست")
            }
        }
    }
}

enum class PaymentKind {
    IMMEDIATE,
    RECEIPT,
    CHEQUE
}

enum class CartStatus(
    val title: String,
    val color: String
) {
    REGISTERED("ثبت شده", "#2E7D32"),
    PROCESSING("در حال بررسی", "#1565C0"),
    CANCELLED("لغو شده", "#C62828"),
    DELIVERED("تحویل شده", "#00695C")
}