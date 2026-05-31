package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.domain.entity.Cart
import com.msa.eshop.backend.domain.entity.Customer
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CartAccessPolicy {
    fun assertCanReadCustomerHistory(current: Customer, targetCustomerId: UUID) {
        if (current.isAdmin()) return

        if (current.id != targetCustomerId) {
            throw BadRequestException("دسترسی به گزارش این مشتری مجاز نیست")
        }
    }

    fun assertCanReadCart(current: Customer, cart: Cart) {
        if (current.isAdmin()) return

        if (cart.customer?.id != current.id) {
            throw BadRequestException("دسترسی به جزئیات این سفارش مجاز نیست")
        }
    }

    private fun Customer.isAdmin(): Boolean =
        role.equals("ADMIN", ignoreCase = true)
}