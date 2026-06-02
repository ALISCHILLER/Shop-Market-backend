package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.dtos.BannerDto
import com.msa.eshop.backend.common.dtos.DiscountResultDto
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.PaymentTermDto
import com.msa.eshop.backend.common.dtos.ProductDto
import com.msa.eshop.backend.common.dtos.ProductGroupDto
import com.msa.eshop.backend.common.dtos.CartDetailsDto
import com.msa.eshop.backend.common.dtos.CartDetailsLineDto
import com.msa.eshop.backend.common.dtos.CartHistoryDto
import com.msa.eshop.backend.common.dtos.UserDto
import com.msa.eshop.backend.domain.entity.Banner
import com.msa.eshop.backend.domain.entity.Cart
import com.msa.eshop.backend.domain.entity.CartItem
import com.msa.eshop.backend.domain.entity.Customer
import com.msa.eshop.backend.domain.entity.CustomerAddress
import com.msa.eshop.backend.domain.entity.Discount
import com.msa.eshop.backend.domain.entity.PaymentTerm
import com.msa.eshop.backend.domain.entity.Product
import com.msa.eshop.backend.domain.entity.ProductCategory

fun Customer.toDto(): UserDto = UserDto(
    id = requireNotNull(id).toString(),
    customerCode = customerCode,
    customerName = customerName,
    mobile = mobile,
    phone = phone,
    center = center,
    nationalCode = nationalCode,
    role = role.uppercase(),
    enabled = enabled,
    passwordChangeRequired = passwordChangeRequired
)

fun Product.toDto(): ProductDto = ProductDto(
    id = requireNotNull(id).toString(),
    productName = productName,
    productCode = productCode,
    fullNameKala1 = fullNameKala1,
    unit1 = unit1,
    unitid1 = unitid1,
    convertFactor1 = convertFactor1,
    fullNameKala2 = fullNameKala2,
    unit2 = unit2,
    convertFactor2 = convertFactor2,
    unitid2 = unitid2,
    productGroupCode = productGroupCode,
    price = price,
    isDiscounts = isDiscounts,
    isTax = isTax,
    productImage = productImage
)

fun ProductCategory.toDto(): ProductGroupDto = ProductGroupDto(
    productCategoryCode = productCategoryCode,
    productCategoryName = productCategoryName,
    productCategoryImage = productCategoryImage,
    productCategoryImageUnselect = productCategoryImageUnselect
)

fun Banner.toDto(): BannerDto = BannerDto(
    id = requireNotNull(id).toString(),
    bannerImage = bannerImage,
    bannerName = bannerName
)

fun Discount.toDto(): DiscountResultDto = DiscountResultDto(
    id = requireNotNull(id).toString(),
    productId = requireNotNull(product?.id).toString(),
    discountPercent = discountPercent,
    fromNumber = fromNumber,
    endNumber = endNumber
)

fun CustomerAddress.toDto(): OrderAddressDto = OrderAddressDto(
    id = requireNotNull(id).toString(),
    customerId = customer?.id?.toString(),
    centerName = centerName,
    customerAddress = customerAddress,
    customerMobile = customerMobile,
    customerPhone = customerPhone,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault
)

fun PaymentTerm.toDto(): PaymentTermDto = PaymentTermDto(
    id = requireNotNull(id).toString(),
    name = name,
    deadLine = deadLine,
    paymentKind = paymentKind.name,
    paymentKindTitle = paymentKind.title,
    immediateDiscountPercent = immediateDiscountPercent,
    receiptDiscountPercent = receiptDiscountPercent,
    chequeDiscountPercent = chequeDiscountPercent,
    active = active
)

fun Cart.toHistoryDto(): CartHistoryDto =
    CartHistoryDto(
        id = requireNotNull(id),
        cartCode = cartCode,
        customerId = customer?.id,
        customerCode = customer?.customerCode.orEmpty(),
        customerName = customerNameSnapshot,
        salesDate = salesDate.toString(),
        statusCode = statusCode,
        statusName = statusName,
        statusColor = statusColor,
        subtotal = subtotal,
        discountTotal = discountTotal,
        taxTotal = taxTotal,
        total = total
    )

fun Cart.toDetailsDto(): CartDetailsDto =
    CartDetailsDto(
        id = requireNotNull(id),
        cartCode = cartCode,
        customerId = customer?.id,
        customerCode = customer?.customerCode.orEmpty(),
        customerName = customerNameSnapshot,
        customerAddress = customerAddressSnapshot,
        paymentTermId = paymentTerm?.id,
        paymentTermName = paymentTerm?.name.orEmpty(),
        statusCode = statusCode,
        statusName = statusName,
        statusColor = statusColor,
        salesDate = salesDate.toString(),
        subtotal = subtotal,
        discountTotal = discountTotal,
        taxTotal = taxTotal,
        total = total,
        items = items
            .sortedBy { it.productCode }
            .map { it.toDetailsLineDto() }
    )

fun CartItem.toDetailsLineDto(): CartDetailsLineDto =
    CartDetailsLineDto(
        id = requireNotNull(id),
        productId = product?.id,
        productCode = productCode,
        productName = productName,
        productImageUrl = productImageUrl,
        quantity = quantity,
        unitPrice = price,
        discount = discount,
        tax = tax,
        total = total
    )