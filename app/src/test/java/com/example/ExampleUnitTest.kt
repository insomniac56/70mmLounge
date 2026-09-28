package com.example

import com.example.data.model.ProductItem
import com.example.util.TableUrlGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDynamicTableUrlGeneration() {
        val url = TableUrlGenerator.createDynamicTableUrl("T-1", "Main Dining")
        assertTrue(url.contains("table=T-1"))
        assertTrue(url.contains("zone=Main%20Dining"))
        assertTrue(url.contains("phone=8987477773"))
        assertTrue(url.contains("token="))

        val deepLink = TableUrlGenerator.createTableDeepLink("VIP-1", "VIP Lounge")
        assertTrue(deepLink.startsWith("70mm://table/VIP-1"))
        assertTrue(deepLink.contains("zone=VIP%20Lounge"))
        assertTrue(deepLink.contains("token="))
    }

    @Test
    fun testRawIngredientAndLowStockThreshold() {
        val lowStockIngredient = ProductItem(
            name = "Fresh Buffalo Mozzarella",
            category = "Raw Ingredients",
            sku = "RAW-007",
            sellingPrice = 12.0,
            costPrice = 5.8,
            stockQuantity = 1,
            reorderLevel = 5,
            unit = "kg",
            isRawIngredient = true
        )

        assertTrue(lowStockIngredient.isIngredient)
        assertTrue(lowStockIngredient.isLowStock)
        assertFalse(lowStockIngredient.isOutOfStock)
        assertEquals(4, lowStockIngredient.deficitQuantity)

        val adequateProduct = ProductItem(
            name = "Dom Pérignon Vintage Brut",
            category = "Bottle Service",
            sku = "BOT-001",
            sellingPrice = 340.0,
            costPrice = 180.0,
            stockQuantity = 10,
            reorderLevel = 2,
            unit = "bottle"
        )

        assertFalse(adequateProduct.isIngredient)
        assertFalse(adequateProduct.isLowStock)
        assertEquals(0, adequateProduct.deficitQuantity)
    }
}
