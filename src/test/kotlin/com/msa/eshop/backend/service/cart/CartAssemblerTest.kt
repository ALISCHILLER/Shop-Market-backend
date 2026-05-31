package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.Money
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.domain.CustomerAddress
import com.msa.eshop.backend.domain.PaymentKind
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
        val pricingResult = CartPricingResult(
            paymentTerm = PaymentTerm(name = "Receipt"),
            paymentKind = PaymentKind.RECEIPT,
            priceLines = emptyList(),
            subtotal = Money.zero(),
            productDiscountTotal = Money.zero(),
            paymentDiscountTotal = Money.zero(),
            discountTotal = Money.zero(),
            taxableAmount = Money.zero(),
            taxTotal = Money.zero(),
            total = Money.zero()
        )

        assertThrows(BadRequestException::class.java) {
            assembler.assemble(
                cartCode = 100001,
                customer = createCustomer(),
                address = createAddress(),
                pricingResult = pricingResult
            )
        }
    }


    @Test
    fun `assemble should calculate cart totals from pricing result`() {
        val pricingResult = CartPricingResult(
            paymentTerm = PaymentTerm(name = "Receipt"),
            paymentKind = PaymentKind.RECEIPT,
            priceLines = listOf(createPriceLine()),
            subtotal = Money(200_000),
            productDiscountTotal = Money(20_000),
            paymentDiscountTotal = Money(9_000),
            discountTotal = Money(29_000),
            taxableAmount = Money(171_000),
            taxTotal = Money(15_390),
            total = Money(186_390)
        )

        val cart = assembler.assemble(
            cartCode = 100001,
            customer = createCustomer(),
            address = createAddress(),
            pricingResult = pricingResult
        )

        assertEquals(100001, cart.cartCode)
        assertEquals(200_000L, cart.subtotal)
        assertEquals(29_000L, cart.discountTotal)
        assertEquals(15_390L, cart.taxTotal)
        assertEquals(186_390L, cart.total)
        assertEquals("REGISTERED", cart.statusCode)
        assertEquals(1, cart.items.size)
    }

    @Test
    fun `assemble should create cart item from price line`() {
        val pricingResult = CartPricingResult(
            paymentTerm = PaymentTerm(name = "Receipt"),
            paymentKind = PaymentKind.RECEIPT,
            priceLines = listOf(createPriceLine()),
            subtotal = Money(200_000),
            productDiscountTotal = Money(20_000),
            paymentDiscountTotal = Money(9_000),
            discountTotal = Money(29_000),
            taxableAmount = Money(171_000),
            taxTotal = Money(15_390),
            total = Money(186_390)
        )

        val cart = assembler.assemble(
            cartCode = 100001,
            customer = createCustomer(),
            address = createAddress(),
            pricingResult = pricingResult
        )

        val item = cart.items.first()

        assertEquals(1001, item.productCode)
        assertEquals("Product", item.productName)
        assertEquals(2, item.quantity)
        assertEquals(100_000L, item.price)
        assertEquals(29_000L, item.discount)
        assertEquals(15_390L, item.tax)
        assertEquals(186_390L, item.total)
    }

    private fun createCustomer(): Customer {
        return Customer(
            customerCode = "C001",
            customerName = "Customer"
        )
    }

    private fun createAddress(): CustomerAddress {
        return CustomerAddress(
            customerAddress = "Address"
        )
    }

    private fun createProduct(): Product {
        return Product(
            productName = "Product",
            productCode = 1001,
            price = 100_000L
        )
    }

    private fun createPriceLine(): PriceLine {
        val product = createProduct()

        return PriceLine(
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
    }
}