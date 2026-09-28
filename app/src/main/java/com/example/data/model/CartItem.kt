package com.example.data.model

data class CartItem(
    val product: ProductItem,
    val quantity: Int = 1,
    val discountPercent: Double = 0.0
) {
    val subtotal: Double
        get() = product.sellingPrice * quantity

    val discountAmount: Double
        get() = subtotal * (discountPercent / 100.0)

    val taxableAmount: Double
        get() = (subtotal - discountAmount).coerceAtLeast(0.0)

    val taxAmount: Double
        get() = taxableAmount * (product.gstRate / 100.0)

    val cgstAmount: Double
        get() = taxAmount / 2.0

    val sgstAmount: Double
        get() = taxAmount / 2.0

    val totalAmount: Double
        get() = taxableAmount + taxAmount

    val totalCost: Double
        get() = product.costPrice * quantity
}
