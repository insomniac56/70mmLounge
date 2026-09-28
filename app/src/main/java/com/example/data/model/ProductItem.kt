package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val sku: String = "",
    val sellingPrice: Double,
    val costPrice: Double = 0.0,
    val stockQuantity: Int,
    val reorderLevel: Int = 5,
    val gstRate: Double = 5.0, // Standard GST slab e.g. 0.0, 5.0, 12.0, 18.0, 28.0
    val unit: String = "pcs",
    val kitchenSection: String = "KITCHEN", // KITCHEN, BAR, GRILL
    val isRawIngredient: Boolean = false,
    val isActive: Boolean = true
) {
    val isLowStock: Boolean
        get() = stockQuantity <= reorderLevel

    val isOutOfStock: Boolean
        get() = stockQuantity <= 0

    val deficitQuantity: Int
        get() = (reorderLevel - stockQuantity).coerceAtLeast(0)

    val isIngredient: Boolean
        get() = isRawIngredient || category.contains("Raw", ignoreCase = true) || category.contains("Ingredient", ignoreCase = true)
}
