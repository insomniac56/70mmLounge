package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "sale_order_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleOrder::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class SaleOrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long = 0,
    val productId: Long,
    val productName: String,
    val category: String,
    val quantity: Int,
    val unitPrice: Double,
    val costPrice: Double,
    val gstRate: Double,
    val taxAmount: Double,
    val totalAmount: Double
)

data class OrderWithItems(
    @Embedded
    val order: SaleOrder,
    @Relation(
        parentColumn = "orderId",
        entityColumn = "orderId"
    )
    val items: List<SaleOrderItem>
)
