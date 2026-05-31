package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.Money
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.domain.CustomerAddress
import com.msa.eshop.backend.domain.PaymentTerm
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.service.PriceLine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CartAssemblerTest {

    private val assembler = CartAssembler()

    @Test
    fun `assemble should reject empty price lines`() {
        assertThrows(BadRequestException::class.java) {
            assembler.assemble(
                cartCode = 100001,
                customer = Customer(customerCode = "C001", customerName = "Customer"),
                address = CustomerAddress(customerAddress = "Address"),
                paymentTerm = PaymentTerm(name = "Receipt"),
                priceLines = emptyList()
            )
        }
    }

    @Test
    fun `assemble should calculate cart totals`() {
        val product = Product(
            productName = "Product",
            productCode = 1001,
            price = 100_000
        )

        val line = PriceLine(
            product = product,
            quantity = 2,
            gross = Money(200_000),
            productDiscountPercent = 10,
            productDiscount = Money(20_000),
            afterProductDiscount = Money(180_000),
            paymentDiscountPercent = 5,
            paymentDiscount = Money(9_000),
            taxableAmount = Money(171_000),
            tax = Money(15_390),
            taxWithoutPaymentDiscount = Money(16_200),
            total = Money(186_390)
        )

        val cart = assembler.assemble(
            cartCode = 100001,
            customer = Customer(customerCode = "C001", customerName = "Customer"),
            address = CustomerAddress(customerAddress = "Address"),
            paymentTerm = PaymentTerm(name = "Receipt"),
            priceLines = listOf(line)
        )

        assertEquals(200_000L, cart.subtotal)
        assertEquals(29_000L, cart.discountTotal)
        assertEquals(15_390L, cart.taxTotal)
        assertEquals(186_390L, cart.total)
        assertEquals("REGISTERED", cart.statusCode)
        assertEquals(1, cart.items.size)
    }
}