package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.domain.CartStatus
import org.springframework.stereotype.Component

@Component
class CartStatusPolicy {
    fun assertCanChange(
        current: CartStatus,
        target: CartStatus
    ) {
        if (current == target) return

        if (current == CartStatus.CANCELLED) {
            throw BadRequestException("سفارش لغو شده قابل تغییر وضعیت نیست")
        }

        if (current == CartStatus.DELIVERED) {
            throw BadRequestException("سفارش تحویل شده قابل تغییر وضعیت نیست")
        }

        if (current == CartStatus.REGISTERED && target == CartStatus.DELIVERED) {
            throw BadRequestException("سفارش ثبت شده باید ابتدا وارد مرحله بررسی شود")
        }

        if (current == CartStatus.PROCESSING && target == CartStatus.REGISTERED) {
            throw BadRequestException("سفارش در حال بررسی نمی‌تواند به وضعیت ثبت شده برگردد")
        }
    }
}