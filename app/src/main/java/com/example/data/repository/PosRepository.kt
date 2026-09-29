package com.example.data.repository

import com.example.data.db.PosDao
import com.example.data.model.CartItem
import com.example.data.model.KitchenOrderTicket
import com.example.data.model.OrderWithItems
import com.example.data.model.ProductItem
import com.example.data.model.ReportTimeRange
import com.example.data.model.RestaurantTable
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PosRepository(private val dao: PosDao) {

    val allProducts: Flow<List<ProductItem>> = dao.getAllProducts()
    val activeProducts: Flow<List<ProductItem>> = dao.getActiveProducts()
    val allOrdersWithItems: Flow<List<OrderWithItems>> = dao.getAllOrdersWithItems()

    val allTables: Flow<List<RestaurantTable>> = dao.getAllTables()
    val activeKots: Flow<List<KitchenOrderTicket>> = dao.getActiveKots()
    val allKots: Flow<List<KitchenOrderTicket>> = dao.getAllKots()
    val lowStockProducts: Flow<List<ProductItem>> = dao.getLowStockProducts()

    suspend fun addProduct(product: ProductItem): Long {
        return dao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductItem) {
        dao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductItem) {
        dao.deleteProduct(product)
    }

    suspend fun adjustStock(productId: Long, quantityChange: Int) {
        dao.adjustProductStock(productId, quantityChange)
    }

    suspend fun updateProductThreshold(productId: Long, newThreshold: Int) {
        dao.updateProductThreshold(productId, newThreshold)
    }

    suspend fun addTable(table: RestaurantTable): Long {
        return dao.insertTable(table)
    }

    suspend fun updateTable(table: RestaurantTable) {
        dao.updateTable(table)
    }

    suspend fun deleteTable(table: RestaurantTable) {
        dao.deleteTable(table)
    }

    suspend fun deleteTable(tableId: Long) {
        dao.deleteTableById(tableId)
    }

    suspend fun clearAllRunningTables() {
        dao.clearAllRunningTables()
    }

    suspend fun getTableByNumber(tableNumber: String): RestaurantTable? {
        return dao.getTableByNumber(tableNumber)
    }

    suspend fun updateTableStatus(tableId: Long, status: String, guestName: String, billAmount: Double) {
        dao.updateTableStatus(tableId, status, guestName, billAmount)
    }

    suspend fun requestTableCheckout(tableNumber: String) {
        dao.requestTableCheckout(tableNumber)
    }

    fun getTableByNumberFlow(tableNumber: String): Flow<RestaurantTable?> =
        dao.getTableByNumberFlow(tableNumber)

    fun getLatestKotForTable(tableNumber: String): Flow<KitchenOrderTicket?> =
        dao.getLatestKotForTable(tableNumber)

    suspend fun vacateTable(tableId: Long) {
        dao.vacateTable(tableId)
    }

    suspend fun updateKotStatus(kotId: Long, newStatus: String) {
        dao.updateKotStatus(kotId, newStatus)
    }

    suspend fun updateKot(kot: KitchenOrderTicket) {
        dao.updateKot(kot)
    }

    suspend fun updateKotCompletedItems(kotId: Long, completedItems: String) {
        dao.updateKotCompletedItems(kotId, completedItems)
    }

    suspend fun insertKot(kot: KitchenOrderTicket): Long {
        return dao.insertKot(kot)
    }

    suspend fun getKotCount(): Int {
        return dao.getKotCount()
    }

    suspend fun getOrderCount(): Int {
        return dao.getOrderCount()
    }

    /**
     * Submits an order placed by customer at a Table via QR Code:
     * 1. Creates KOT for the Kitchen / Bar
     * 2. Updates Table status & adds to active bill
     * 3. Deducts stock & prepares for Admin Panel live display
     */
    suspend fun submitCustomerTableOrder(
        tableNumber: String,
        zone: String,
        customerName: String,
        customerPhone: String = "",
        cartItems: List<CartItem>,
        specialNotes: String
    ): Long {
        if (cartItems.isEmpty()) return 0L

        val subtotal = cartItems.sumOf { it.subtotal }
        val tax = cartItems.sumOf { it.taxAmount }
        val total = cartItems.sumOf { it.totalAmount }
        val cost = cartItems.sumOf { it.totalCost }

        // Items summary for KOT slip
        val itemsSummary = cartItems.joinToString(" ; ") {
            "${it.quantity}x ${it.product.name}"
        }

        // Determine primary section (e.g. BAR if drinks, GRILL if steaks, otherwise KITCHEN)
        val section = when {
            cartItems.any { it.product.kitchenSection == "GRILL" } -> "GRILL"
            cartItems.all { it.product.kitchenSection == "BAR" } -> "BAR"
            else -> "KITCHEN"
        }

        val kotCount = dao.getKotCount()
        val kotNumber = "KOT-${101 + kotCount}"

        val guestDisplayName = if (customerPhone.isNotBlank()) {
            "${customerName.ifBlank { "Guest" }} ($customerPhone)"
        } else {
            customerName.ifBlank { "Table Guest" }
        }

        // 1. Insert KOT
        val kot = KitchenOrderTicket(
            kotNumber = kotNumber,
            tableNumber = tableNumber,
            zone = zone,
            customerName = guestDisplayName,
            timestamp = System.currentTimeMillis(),
            status = "NEW",
            section = section,
            itemsSummary = itemsSummary,
            specialNotes = if (customerPhone.isNotBlank()) "Mobile: $customerPhone. $specialNotes" else specialNotes
        )
        dao.insertKot(kot)

        // 2. Update Table Status & Bill
        val table = dao.getTableByNumber(tableNumber)
        if (table != null) {
            val newBill = table.currentBillAmount + total
            dao.updateTableStatus(
                tableId = table.tableId,
                status = "OCCUPIED",
                guestName = guestDisplayName,
                billAmount = newBill
            )
        }

        // 3. Deduct stock for ordered items
        for (c in cartItems) {
            dao.adjustProductStock(c.product.id, -c.quantity)
        }

        // 4. Create Sale Order for Admin Panel
        val invoiceNo = "INV-${Calendar.getInstance().get(Calendar.YEAR)}-${(1000 + (System.currentTimeMillis() % 9000))}"
        val saleOrder = SaleOrder(
            invoiceNumber = invoiceNo,
            timestamp = System.currentTimeMillis(),
            customerName = customerName.ifBlank { "Table $tableNumber" },
            customerPhone = customerPhone,
            paymentMethod = "CARD",
            subtotal = subtotal,
            taxAmount = tax,
            totalAmount = total,
            totalCost = cost,
            netProfit = (total - tax - cost).coerceAtLeast(0.0),
            tableNumber = tableNumber,
            orderType = "QR_DINE_IN",
            notes = "Customer QR Order ($kotNumber). Mobile: $customerPhone. $specialNotes"
        )
        val orderId = dao.insertOrder(saleOrder)

        val orderItems = cartItems.map { item ->
            SaleOrderItem(
                orderId = orderId,
                productId = item.product.id,
                productName = item.product.name,
                category = item.product.category,
                quantity = item.quantity,
                unitPrice = item.product.sellingPrice,
                costPrice = item.product.costPrice,
                gstRate = item.product.gstRate,
                taxAmount = item.taxAmount,
                totalAmount = item.totalAmount
            )
        }
        dao.insertOrderItems(orderItems)

        return orderId
    }

    suspend fun completeSale(
        order: SaleOrder,
        items: List<SaleOrderItem>,
        cart: List<CartItem>,
        tableNumber: String? = null
    ): Long {
        val orderId = dao.insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        dao.insertOrderItems(itemsWithOrderId)

        // Deduct inventory for each purchased item
        for (cartItem in cart) {
            dao.adjustProductStock(cartItem.product.id, -cartItem.quantity)
        }

        // Also create a KOT for the kitchen
        val kotNumber = "KOT-${101 + dao.getKotCount()}"
        val itemsSummary = cart.joinToString(" ; ") { "${it.quantity}x ${it.product.name}" }
        dao.insertKot(
            KitchenOrderTicket(
                kotNumber = kotNumber,
                tableNumber = tableNumber?.ifBlank { "Takeout / Walk-in" } ?: "Counter",
                customerName = order.customerName,
                timestamp = System.currentTimeMillis(),
                status = "NEW",
                section = "KITCHEN",
                itemsSummary = itemsSummary,
                specialNotes = order.notes
            )
        )

        // If this sale was for a table, vacate the table
        if (!tableNumber.isNullOrBlank()) {
            val table = dao.getTableByNumber(tableNumber)
            if (table != null) {
                dao.vacateTable(table.tableId)
            }
        }

        return orderId
    }

    fun getOrdersForRange(range: ReportTimeRange): Flow<List<OrderWithItems>> {
        val (start, end) = getTimeRangeBounds(range)
        return if (start == 0L && end == Long.MAX_VALUE) {
            dao.getAllOrdersWithItems()
        } else {
            dao.getOrdersWithItemsBetween(start, end)
        }
    }

    companion object {
        fun getTimeRangeBounds(range: ReportTimeRange): Pair<Long, Long> {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            return when (range) {
                ReportTimeRange.TODAY -> {
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    Pair(start, end)
                }
                ReportTimeRange.THIS_WEEK -> {
                    cal.firstDayOfWeek = Calendar.MONDAY
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    Pair(start, now + 86400000L)
                }
                ReportTimeRange.THIS_MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    Pair(start, now + 86400000L)
                }
                ReportTimeRange.ALL_TIME -> {
                    Pair(0L, Long.MAX_VALUE)
                }
            }
        }
    }
}
