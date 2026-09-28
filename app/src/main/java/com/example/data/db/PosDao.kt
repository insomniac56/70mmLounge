package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.KitchenOrderTicket
import com.example.data.model.OrderWithItems
import com.example.data.model.ProductItem
import com.example.data.model.RestaurantTable
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {

    // Products
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM products WHERE stockQuantity <= reorderLevel ORDER BY stockQuantity ASC")
    fun getLowStockProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductItem>)

    @Update
    suspend fun updateProduct(product: ProductItem)

    @Delete
    suspend fun deleteProduct(product: ProductItem)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :quantityChange WHERE id = :productId")
    suspend fun adjustProductStock(productId: Long, quantityChange: Int)

    @Query("UPDATE products SET reorderLevel = :newThreshold WHERE id = :productId")
    suspend fun updateProductThreshold(productId: Long, newThreshold: Int)

    // Restaurant Tables
    @Query("SELECT * FROM restaurant_tables ORDER BY zone ASC, tableNumber ASC")
    fun getAllTables(): Flow<List<RestaurantTable>>

    @Query("SELECT * FROM restaurant_tables WHERE tableId = :id LIMIT 1")
    suspend fun getTableById(id: Long): RestaurantTable?

    @Query("SELECT * FROM restaurant_tables WHERE tableNumber = :number LIMIT 1")
    suspend fun getTableByNumber(number: String): RestaurantTable?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: RestaurantTable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTables(tables: List<RestaurantTable>)

    @Update
    suspend fun updateTable(table: RestaurantTable)

    @Delete
    suspend fun deleteTable(table: RestaurantTable)

    @Query("DELETE FROM restaurant_tables WHERE tableId = :tableId")
    suspend fun deleteTableById(tableId: Long)

    @Query("UPDATE restaurant_tables SET status = 'AVAILABLE', currentGuestName = '', currentBillAmount = 0.0, activeOrderId = NULL")
    suspend fun clearAllRunningTables()

    @Query("UPDATE restaurant_tables SET status = :status, currentGuestName = :guestName, currentBillAmount = :billAmount WHERE tableId = :tableId")
    suspend fun updateTableStatus(tableId: Long, status: String, guestName: String, billAmount: Double)

    @Query("UPDATE restaurant_tables SET status = 'CHECKOUT_REQUESTED' WHERE tableNumber = :tableNumber")
    suspend fun requestTableCheckout(tableNumber: String)

    @Query("SELECT * FROM restaurant_tables WHERE tableNumber = :tableNumber LIMIT 1")
    fun getTableByNumberFlow(tableNumber: String): Flow<RestaurantTable?>

    @Query("UPDATE restaurant_tables SET status = 'AVAILABLE', currentGuestName = '', currentBillAmount = 0.0, activeOrderId = NULL WHERE tableId = :tableId")
    suspend fun vacateTable(tableId: Long)

    @Query("SELECT COUNT(*) FROM restaurant_tables")
    suspend fun getTableCount(): Int

    // Kitchen Order Tickets (KOT)
    @Query("SELECT * FROM kitchen_order_tickets ORDER BY timestamp DESC")
    fun getAllKots(): Flow<List<KitchenOrderTicket>>

    @Query("SELECT * FROM kitchen_order_tickets WHERE tableNumber = :tableNumber ORDER BY timestamp DESC LIMIT 1")
    fun getLatestKotForTable(tableNumber: String): Flow<KitchenOrderTicket?>

    @Query("SELECT * FROM kitchen_order_tickets WHERE status != 'SERVED' ORDER BY timestamp DESC")
    fun getActiveKots(): Flow<List<KitchenOrderTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKot(kot: KitchenOrderTicket): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKots(kots: List<KitchenOrderTicket>)

    @Update
    suspend fun updateKot(kot: KitchenOrderTicket)

    @Query("UPDATE kitchen_order_tickets SET itemsSummary = :itemsSummary WHERE kotId = :kotId")
    suspend fun updateKotItemsSummary(kotId: Long, itemsSummary: String)

    @Query("UPDATE kitchen_order_tickets SET status = :newStatus WHERE kotId = :kotId")
    suspend fun updateKotStatus(kotId: Long, newStatus: String)

    @Query("UPDATE kitchen_order_tickets SET completedItems = :completedItems WHERE kotId = :kotId")
    suspend fun updateKotCompletedItems(kotId: Long, completedItems: String)

    @Query("SELECT COUNT(*) FROM kitchen_order_tickets")
    suspend fun getKotCount(): Int

    // Orders & Transactions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: SaleOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<SaleOrderItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOrders(orders: List<SaleOrder>)

    @Transaction
    @Query("SELECT * FROM sale_orders ORDER BY timestamp DESC")
    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM sale_orders WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getOrdersWithItemsBetween(startTime: Long, endTime: Long): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM sale_orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderWithItemsById(orderId: Long): OrderWithItems?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT COUNT(*) FROM products WHERE sku = 'SOP-01'")
    suspend fun checkRestaurantProductsSeeded(): Int

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM restaurant_tables")
    suspend fun deleteAllTables()

    @Query("SELECT COUNT(*) FROM sale_orders")
    suspend fun getOrderCount(): Int
}
