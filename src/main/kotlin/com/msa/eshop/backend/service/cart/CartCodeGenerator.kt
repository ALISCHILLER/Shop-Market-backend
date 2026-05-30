package com.msa.eshop.backend.service.cart

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class CartCodeGenerator(
    private val entityManager: EntityManager
) {
    fun next(): Int =
        (entityManager
            .createNativeQuery("select nextval('cart_code_seq')")
            .singleResult as Number)
            .toInt()
}