package com.msa.eshop.backend.domain.entity

import com.msa.eshop.backend.common.BadRequestException

enum class CustomerRole {
    ADMIN,
    CUSTOMER;

    companion object {
        fun normalize(value: String?): CustomerRole {
            val normalized = value
                ?.trim()
                ?.uppercase()
                ?.removePrefix("ROLE_")
                ?.takeIf { it.isNotBlank() }
                ?: return CUSTOMER

            return when (normalized) {
                "ADMIN" -> ADMIN
                "CUSTOMER" -> CUSTOMER
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
    val code: String,
    val title: String,
    val color: String
) {
    REGISTERED("REGISTERED", "ثبت شده", "#2E7D32"),
    PROCESSING("PROCESSING", "در حال بررسی", "#1565C0"),
    CANCELLED("CANCELLED", "لغو شده", "#C62828"),
    DELIVERED("DELIVERED", "تحویل شده", "#00695C");

    companion object {
        fun normalize(value: String?): CartStatus {
            val raw = value
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: throw BadRequestException("وضعیت سفارش الزامی است")

            val normalized = raw
                .uppercase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("‌", "")

            return entries.firstOrNull { status ->
                status.name == normalized ||
                        status.code == normalized ||
                        status.title == raw
            } ?: when (raw) {
                "ثبت", "ثبت‌شده", "ثبت شده" -> REGISTERED
                "بررسی", "درحال بررسی", "در حال بررسی", "پردازش" -> PROCESSING
                "لغو", "لغو‌شده", "لغو شده", "باطل" -> CANCELLED
                "تحویل", "تحویل‌شده", "تحویل شده", "ارسال شده" -> DELIVERED
                else -> throw BadRequestException("وضعیت سفارش معتبر نیست")
            }
        }
    }
}