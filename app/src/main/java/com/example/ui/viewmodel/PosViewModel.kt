package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.DatabaseSeeder
import com.example.data.db.PosDatabase
import com.example.data.model.CartItem
import com.example.data.model.CategorySaleStat
import com.example.data.model.GstSlabSummary
import com.example.data.model.KitchenOrderTicket
import com.example.data.model.OrderWithItems
import com.example.data.model.PaymentMethodBreakdown
import com.example.data.model.ProductItem
import com.example.data.model.ReportTimeRange
import com.example.data.model.RestaurantTable
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import com.example.data.model.SalesSummary
import com.example.data.model.TopSellingProduct
import com.example.data.repository.PosRepository
import com.example.util.TableUrlGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class PosViewModel(application: Application) : AndroidViewModel(application) {

    val repository: PosRepository

    init {
        val db = PosDatabase.getDatabase(application)
        repository = PosRepository(db.posDao())

        // Seed initial inventory and demo orders if empty
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseSeeder.seedIfNeeded(db.posDao())
        }
    }

    // --- Inventory / Products ---
    val allProducts: StateFlow<List<ProductItem>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProducts: StateFlow<List<ProductItem>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun setSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // Filtered products for POS catalog
    val filteredCatalogProducts: StateFlow<List<ProductItem>> = combine(
        activeProducts,
        _productSearchQuery,
        _selectedCategory
    ) { products, query, cat ->
        products.filter { p ->
            val matchesCategory = (cat == "All" || p.category.equals(cat, ignoreCase = true))
            val matchesSearch = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.sku.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dark / Day Mode Toggle ---
    private val _isDarkMode = MutableStateFlow(true) // Default to club/lounge night mode!
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // --- Low Stock & Raw Ingredient Notification System ---
    val lowStockProducts: StateFlow<List<ProductItem>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockRawIngredients: StateFlow<List<ProductItem>> = repository.lowStockProducts
        .map { items -> items.filter { it.isIngredient } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showLowStockAlertSheet = MutableStateFlow(false)
    val showLowStockAlertSheet: StateFlow<Boolean> = _showLowStockAlertSheet.asStateFlow()

    fun openLowStockAlertSheet() {
        _showLowStockAlertSheet.value = true
    }

    fun closeLowStockAlertSheet() {
        _showLowStockAlertSheet.value = false
    }

    fun quickRestock(productId: Long, amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adjustStock(productId, amount)
        }
    }

    fun updateProductThreshold(productId: Long, newThreshold: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProductThreshold(productId, newThreshold.coerceAtLeast(1))
        }
    }

    // --- Restaurant & Club Tables ---
    val allTables: StateFlow<List<RestaurantTable>> = repository.allTables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTableForQr = MutableStateFlow<RestaurantTable?>(null)
    val selectedTableForQr: StateFlow<RestaurantTable?> = _selectedTableForQr.asStateFlow()

    fun openTableQr(table: RestaurantTable) {
        _selectedTableForQr.value = table
    }

    fun closeTableQr() {
        _selectedTableForQr.value = null
    }

    fun addTable(number: String, zone: String, capacity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanNumber = number.trim()
            val cleanZone = zone.trim().ifBlank { "Club area" }
            val payload = TableUrlGenerator.createDynamicTableUrl(cleanNumber, cleanZone)
            repository.addTable(
                RestaurantTable(
                    tableNumber = cleanNumber,
                    zone = cleanZone,
                    capacity = capacity.coerceAtLeast(1),
                    status = "AVAILABLE",
                    qrPayload = payload
                )
            )
        }
    }

    fun vacateTable(tableId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.vacateTable(tableId)
        }
    }

    fun deleteTable(tableId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTable(tableId)
        }
    }

    fun clearAllRunningTables() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllRunningTables()
        }
    }

    // --- Kitchen Order Tickets (KOT) & Kitchen Staff Pending Items ---
    val activeKots: StateFlow<List<KitchenOrderTicket>> = repository.activeKots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKots: StateFlow<List<KitchenOrderTicket>> = repository.allKots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isKitchenAudioEnabled = MutableStateFlow(true)
    val isKitchenAudioEnabled: StateFlow<Boolean> = _isKitchenAudioEnabled.asStateFlow()

    fun toggleKitchenAudio() {
        _isKitchenAudioEnabled.value = !_isKitchenAudioEnabled.value
    }

    fun updateKotStatus(kotId: Long, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateKotStatus(kotId, newStatus)
        }
    }

    fun toggleKotItemCompleted(kot: KitchenOrderTicket, itemStr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentCompleted = kot.completedItems
                .split(" ; ")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toMutableList()

            val cleanItem = itemStr.trim()
            if (currentCompleted.contains(cleanItem)) {
                currentCompleted.remove(cleanItem)
            } else {
                currentCompleted.add(cleanItem)
            }

            val updatedStr = currentCompleted.joinToString(" ; ")
            repository.updateKotCompletedItems(kot.kotId, updatedStr)

            // Auto-advance status if all items are prepared
            val allItems = kot.itemsSummary.split(" ; ").map { it.trim() }.filter { it.isNotBlank() }
            if (allItems.isNotEmpty() && allItems.all { currentCompleted.contains(it) }) {
                if (kot.status != "READY" && kot.status != "SERVED") {
                    repository.updateKotStatus(kot.kotId, "READY")
                }
            } else if (kot.status == "NEW" && currentCompleted.isNotEmpty()) {
                repository.updateKotStatus(kot.kotId, "PREPARING")
            }
        }
    }

    fun markAllKotItemsReady(kot: KitchenOrderTicket) {
        viewModelScope.launch(Dispatchers.IO) {
            val allItems = kot.itemsSummary.split(" ; ").map { it.trim() }.filter { it.isNotBlank() }
            val completedStr = allItems.joinToString(" ; ")
            repository.updateKotCompletedItems(kot.kotId, completedStr)
            repository.updateKotStatus(kot.kotId, "READY")
        }
    }

    // --- Customer Table QR Self-Ordering Flow ---
    val activeRunningTables: StateFlow<List<RestaurantTable>> = repository.allTables
        .map { list -> list.filter { it.isOccupied || it.currentBillAmount > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeCustomerTable = MutableStateFlow<RestaurantTable?>(null)
    val activeCustomerTable: StateFlow<RestaurantTable?> = _activeCustomerTable.asStateFlow()

    private val _customerCartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val customerCartItems: StateFlow<List<CartItem>> = _customerCartItems.asStateFlow()

    private val _customerGuestName = MutableStateFlow("")
    val customerGuestName: StateFlow<String> = _customerGuestName.asStateFlow()

    private val _customerSpecialNotes = MutableStateFlow("")
    val customerSpecialNotes: StateFlow<String> = _customerSpecialNotes.asStateFlow()

    private val _customerOrderPlacedSuccess = MutableStateFlow(false)
    val customerOrderPlacedSuccess: StateFlow<Boolean> = _customerOrderPlacedSuccess.asStateFlow()

    fun requestTableCheckout(tableNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val table = repository.getTableByNumber(tableNumber.trim())
            if (table != null) {
                repository.updateTableStatus(
                    tableId = table.tableId,
                    status = "CHECKOUT_REQUESTED",
                    guestName = table.currentGuestName,
                    billAmount = table.currentBillAmount
                )
            }
        }
    }

    data class ParsedKotItem(val name: String, val qty: Int)

    fun parseItemSummaryLine(line: String): ParsedKotItem {
        val trimmed = line.trim()
        val regex1 = Regex("""^(\d+)\s*[xX]\s*(.+)""")
        val match1 = regex1.find(trimmed)
        if (match1 != null) {
            val qty = match1.groupValues[1].toIntOrNull() ?: 1
            val name = match1.groupValues[2].trim()
            return ParsedKotItem(name, qty)
        }
        val regex2 = Regex("""^(.+?)\s*[xX]\s*(\d+)$""")
        val match2 = regex2.find(trimmed)
        if (match2 != null) {
            val name = match2.groupValues[1].trim()
            val qty = match2.groupValues[2].toIntOrNull() ?: 1
            return ParsedKotItem(name, qty)
        }
        return ParsedKotItem(trimmed, 1)
    }

    fun updateKotItemQuantity(kot: KitchenOrderTicket, itemLine: String, delta: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = kot.itemsSummary.split(";").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            val targetParsed = parseItemSummaryLine(itemLine)
            val index = items.indexOfFirst {
                val p = parseItemSummaryLine(it)
                p.name.equals(targetParsed.name, ignoreCase = true)
            }
            if (index >= 0) {
                val current = parseItemSummaryLine(items[index])
                val newQty = current.qty + delta
                if (newQty <= 0) {
                    items.removeAt(index)
                } else {
                    items[index] = "${newQty}x ${current.name}"
                }
                val updatedSummary = items.joinToString(" ; ")
                val updatedKot = kot.copy(itemsSummary = updatedSummary)
                repository.updateKot(updatedKot)

                // Adjust table bill
                val productsList = repository.allProducts.firstOrNull() ?: emptyList()
                val prod = productsList.find { it.name.equals(current.name, ignoreCase = true) }
                if (prod != null) {
                    val table = repository.getTableByNumber(kot.tableNumber)
                    if (table != null) {
                        val billDelta = delta * prod.sellingPrice
                        val newBill = (table.currentBillAmount + billDelta).coerceAtLeast(0.0)
                        repository.updateTableStatus(table.tableId, table.status, table.currentGuestName, newBill)
                    }
                }
            }
        }
    }

    data class TableDraft(
        val tableNumber: String,
        val customerName: String = "",
        val customerPhone: String = "",
        val orderType: String = "Dine-in",
        val guestCount: Int = 2,
        val serverName: String = "",
        val orderNotes: String = "",
        val cartItems: List<CartItem> = emptyList(),
        val billDiscountPercent: Double = 0.0
    )

    private val _tableDrafts = mutableMapOf<String, TableDraft>()

    fun saveCurrentTableDraft() {
        val table = _selectedTableNumber.value.trim()
        if (table.isNotBlank()) {
            _tableDrafts[table] = TableDraft(
                tableNumber = table,
                customerName = _customerName.value,
                customerPhone = _customerPhone.value,
                orderType = _selectedOrderType.value,
                guestCount = _guestCount.value,
                serverName = _serverName.value,
                orderNotes = _orderNotes.value,
                cartItems = _cartItems.value,
                billDiscountPercent = _billDiscountPercent.value
            )
        }
    }

    private fun syncCurrentTableBillAndDraft() {
        val tableNum = _selectedTableNumber.value.trim()
        saveCurrentTableDraft()
        if (tableNum.isNotBlank()) {
            val total = _cartItems.value.sumOf { it.totalAmount }
            viewModelScope.launch(Dispatchers.IO) {
                val table = repository.getTableByNumber(tableNum)
                if (table != null) {
                    repository.updateTableStatus(
                        tableId = table.tableId,
                        status = if (table.status == "AVAILABLE") "OCCUPIED" else table.status,
                        guestName = _customerName.value.ifBlank { table.currentGuestName },
                        billAmount = total
                    )
                }
            }
        }
    }

    fun loadTableIntoPosCart(table: RestaurantTable) {
        saveCurrentTableDraft()
        val tableNum = table.tableNumber.trim()
        _selectedTableNumber.value = tableNum
        _selectedOrderType.value = "Dine-in"

        val draft = _tableDrafts[tableNum]
        if (draft != null) {
            _cartItems.value = draft.cartItems
            _customerName.value = draft.customerName.ifBlank { table.currentGuestName }
            _customerPhone.value = draft.customerPhone
            _guestCount.value = draft.guestCount
            _serverName.value = draft.serverName
            _orderNotes.value = draft.orderNotes
            _billDiscountPercent.value = draft.billDiscountPercent
        } else {
            _customerName.value = table.currentGuestName
            _customerPhone.value = ""
            _orderNotes.value = ""
            _billDiscountPercent.value = 0.0

            viewModelScope.launch(Dispatchers.IO) {
                val activeKotsForTable = repository.allKots.firstOrNull()?.filter {
                    it.tableNumber.equals(tableNum, ignoreCase = true) && it.status != "CANCELLED"
                } ?: emptyList()

                val productsList = repository.allProducts.firstOrNull() ?: emptyList()
                val newCart = mutableListOf<CartItem>()

                for (kot in activeKotsForTable) {
                    val items = kot.itemsSummary.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                    for (itemLine in items) {
                        val parsed = parseItemSummaryLine(itemLine)
                        val prod = productsList.find { it.name.equals(parsed.name, ignoreCase = true) }
                        if (prod != null) {
                            val existing = newCart.indexOfFirst { it.product.id == prod.id }
                            if (existing >= 0) {
                                newCart[existing] = newCart[existing].copy(quantity = newCart[existing].quantity + parsed.qty)
                            } else {
                                newCart.add(CartItem(product = prod, quantity = parsed.qty))
                            }
                        }
                    }
                }

                if (newCart.isNotEmpty()) {
                    _cartItems.value = newCart
                } else if (table.currentBillAmount > 0) {
                    val customDish = productsList.firstOrNull() ?: ProductItem(
                        name = "Table ${table.tableNumber} Food & Beverages",
                        category = "Dine-in",
                        sku = "TAB-${table.tableNumber}",
                        sellingPrice = table.currentBillAmount,
                        stockQuantity = 999
                    )
                    _cartItems.value = listOf(CartItem(product = customDish.copy(sellingPrice = table.currentBillAmount), quantity = 1))
                } else {
                    _cartItems.value = emptyList()
                }
                saveCurrentTableDraft()
            }
        }
    }

    fun startCustomerTableOrder(table: RestaurantTable) {
        _activeCustomerTable.value = table
        _customerCartItems.value = emptyList()
        _customerGuestName.value = table.currentGuestName
        _customerGuestPhone.value = ""
        _customerSpecialNotes.value = ""
        _customerOrderPlacedSuccess.value = false
    }

    fun openCustomerOrderingForTableNumber(tableNumber: String, zone: String = "Club area") {
        viewModelScope.launch {
            val tablesList = repository.allTables.firstOrNull() ?: emptyList()
            val table = tablesList.find { it.tableNumber.equals(tableNumber.trim(), ignoreCase = true) }
                ?: RestaurantTable(tableNumber = tableNumber.trim(), zone = zone, capacity = 4)
            startCustomerTableOrder(table)
        }
    }

    fun exitCustomerOrdering() {
        _activeCustomerTable.value = null
        _customerCartItems.value = emptyList()
        _customerGuestName.value = ""
        _customerGuestPhone.value = ""
        _customerSpecialNotes.value = ""
        _customerOrderPlacedSuccess.value = false
    }

    private val _customerGuestPhone = MutableStateFlow("")
    val customerGuestPhone: StateFlow<String> = _customerGuestPhone.asStateFlow()

    fun setCustomerGuestName(name: String) {
        _customerGuestName.value = name
    }

    fun setCustomerGuestPhone(phone: String) {
        _customerGuestPhone.value = phone
    }

    fun setCustomerSpecialNotes(notes: String) {
        _customerSpecialNotes.value = notes
    }

    fun addCustomerCartItem(product: ProductItem) {
        val current = _customerCartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            if (existing.quantity < product.stockQuantity) {
                current[index] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            if (product.stockQuantity > 0) {
                current.add(CartItem(product = product, quantity = 1))
            }
        }
        _customerCartItems.value = current
    }

    fun updateCustomerCartQuantity(productId: Long, delta: Int) {
        val current = _customerCartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else if (newQty <= item.product.stockQuantity) {
                current[index] = item.copy(quantity = newQty)
            }
            _customerCartItems.value = current
        }
    }

    fun submitCustomerOrder(onSuccess: () -> Unit) {
        val table = _activeCustomerTable.value ?: return
        val items = _customerCartItems.value
        if (items.isEmpty()) return

        val guestName = _customerGuestName.value.ifBlank { "Guest at ${table.tableNumber}" }
        val guestPhone = _customerGuestPhone.value.trim()
        val notes = _customerSpecialNotes.value

        viewModelScope.launch(Dispatchers.IO) {
            repository.submitCustomerTableOrder(
                tableNumber = table.tableNumber,
                zone = table.zone,
                customerName = guestName,
                customerPhone = guestPhone,
                cartItems = items,
                specialNotes = notes
            )
            _customerOrderPlacedSuccess.value = true
            _customerCartItems.value = emptyList()
            launch(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    // --- POS Cart State ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _billDiscountPercent = MutableStateFlow(0.0)
    val billDiscountPercent: StateFlow<Double> = _billDiscountPercent.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    private val _selectedTableNumber = MutableStateFlow("")
    val selectedTableNumber: StateFlow<String> = _selectedTableNumber.asStateFlow()

    private val _selectedOrderType = MutableStateFlow("Dine-in") // Dine-in, Takeaway, Delivery
    val selectedOrderType: StateFlow<String> = _selectedOrderType.asStateFlow()

    private val _guestCount = MutableStateFlow(2)
    val guestCount: StateFlow<Int> = _guestCount.asStateFlow()

    private val _serverName = MutableStateFlow("")
    val serverName: StateFlow<String> = _serverName.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    private val _paymentMethod = MutableStateFlow("CASH") // CASH, CARD, UPI
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _cashTendered = MutableStateFlow(0.0)
    val cashTendered: StateFlow<Double> = _cashTendered.asStateFlow()

    private val _lastCompletedOrder = MutableStateFlow<OrderWithItems?>(null)
    val lastCompletedOrder: StateFlow<OrderWithItems?> = _lastCompletedOrder.asStateFlow()

    fun addToCart(product: ProductItem) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            // check available stock
            if (existing.quantity < product.stockQuantity) {
                currentList[index] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            if (product.stockQuantity > 0) {
                currentList.add(CartItem(product = product, quantity = 1))
            }
        }
        _cartItems.value = currentList
        syncCurrentTableBillAndDraft()
    }

    fun updateCartItemQuantity(productId: Long, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else if (newQty <= item.product.stockQuantity) {
                currentList[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = currentList
            syncCurrentTableBillAndDraft()
        }
    }

    fun updateCartItemDiscount(productId: Long, discountPercent: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(
                discountPercent = discountPercent.coerceIn(0.0, 100.0)
            )
            _cartItems.value = currentList
            syncCurrentTableBillAndDraft()
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
        syncCurrentTableBillAndDraft()
    }

    fun clearCart() {
        val tableNum = _selectedTableNumber.value.trim()
        _cartItems.value = emptyList()
        _billDiscountPercent.value = 0.0
        _customerName.value = ""
        _customerPhone.value = ""
        _cashTendered.value = 0.0
        _paymentMethod.value = "CASH"
        if (tableNum.isNotBlank()) {
            _tableDrafts.remove(tableNum)
            viewModelScope.launch(Dispatchers.IO) {
                val table = repository.getTableByNumber(tableNum)
                if (table != null) {
                    repository.updateTableStatus(table.tableId, "AVAILABLE", "", 0.0)
                }
            }
        }
    }

    fun startNewOrder(
        customerName: String,
        customerPhone: String,
        tableNumber: String,
        orderType: String = "Dine-in",
        guestCount: Int = 2,
        serverName: String = "",
        notes: String = ""
    ) {
        saveCurrentTableDraft()
        val tableNum = tableNumber.trim()
        _selectedTableNumber.value = tableNum
        _selectedOrderType.value = orderType.trim()
        _customerName.value = customerName.trim()
        _customerPhone.value = customerPhone.trim()
        _guestCount.value = guestCount.coerceAtLeast(1)
        _serverName.value = serverName.trim()
        _orderNotes.value = notes.trim()
        _billDiscountPercent.value = 0.0
        _cartItems.value = emptyList()

        if (tableNum.isNotBlank() && orderType == "Dine-in") {
            viewModelScope.launch(Dispatchers.IO) {
                val table = repository.getTableByNumber(tableNum)
                if (table != null) {
                    val displayName = customerName.ifBlank { "Table ${table.tableNumber}" }
                    repository.updateTableStatus(
                        tableId = table.tableId,
                        status = "OCCUPIED",
                        guestName = displayName,
                        billAmount = 0.0
                    )
                }
            }
            saveCurrentTableDraft()
        }
    }

    fun sendKotToKitchen(onSuccess: (() -> Unit)? = null) {
        val tableNum = _selectedTableNumber.value.trim()
        val items = _cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val table = if (tableNum.isNotBlank()) repository.getTableByNumber(tableNum) else null
            val zone = table?.zone ?: "Club area"
            val custName = _customerName.value.ifBlank { table?.currentGuestName?.ifBlank { "Table $tableNum" } ?: "Order #${100 + (System.currentTimeMillis() % 900)}" }
            val phone = _customerPhone.value
            val notes = _orderNotes.value
            val total = items.sumOf { it.totalAmount }
            val itemsSummary = items.joinToString(" ; ") { "${it.quantity}x ${it.product.name}" }

            val activeKotsForTable = if (tableNum.isNotBlank()) {
                repository.allKots.firstOrNull()?.filter {
                    it.tableNumber.equals(tableNum, ignoreCase = true) &&
                    it.status != "SERVED" && it.status != "CANCELLED"
                } ?: emptyList()
            } else emptyList()

            if (activeKotsForTable.isNotEmpty()) {
                val latest = activeKotsForTable.first()
                val updatedKot = latest.copy(
                    customerName = if (phone.isNotBlank()) "$custName ($phone)" else custName,
                    itemsSummary = itemsSummary,
                    specialNotes = if (notes.isNotBlank()) notes else latest.specialNotes
                )
                repository.updateKot(updatedKot)
            } else {
                val kotCount = repository.getKotCount()
                val section = when {
                    items.any { it.product.kitchenSection == "GRILL" } -> "GRILL"
                    items.all { it.product.kitchenSection == "BAR" } -> "BAR"
                    else -> "KITCHEN"
                }
                val kot = KitchenOrderTicket(
                    kotNumber = "KOT-${101 + kotCount}",
                    tableNumber = tableNum.ifBlank { "Takeaway" },
                    zone = zone,
                    customerName = if (phone.isNotBlank()) "$custName ($phone)" else custName,
                    timestamp = System.currentTimeMillis(),
                    status = "NEW",
                    section = section,
                    itemsSummary = itemsSummary,
                    specialNotes = notes
                )
                repository.insertKot(kot)
            }

            if (table != null) {
                repository.updateTableStatus(
                    tableId = table.tableId,
                    status = "OCCUPIED",
                    guestName = custName,
                    billAmount = total
                )
            }
            saveCurrentTableDraft()

            launch(Dispatchers.Main) {
                onSuccess?.invoke()
            }
        }
    }

    fun clearActiveOrder() {
        clearCart()
        _selectedTableNumber.value = ""
        _selectedOrderType.value = "Dine-in"
        _guestCount.value = 2
        _serverName.value = ""
        _orderNotes.value = ""
    }

    fun setSelectedTableNumber(table: String) {
        _selectedTableNumber.value = table.trim()
    }

    fun setSelectedOrderType(type: String) {
        _selectedOrderType.value = type.trim()
    }

    fun setBillDiscountPercent(percent: Double) {
        _billDiscountPercent.value = percent.coerceIn(0.0, 100.0)
    }

    fun setCustomerDetails(name: String, phone: String) {
        _customerName.value = name
        _customerPhone.value = phone
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun setCashTendered(amount: Double) {
        _cashTendered.value = amount
    }

    fun dismissReceipt() {
        _lastCompletedOrder.value = null
    }

    fun viewOrderReceipt(order: OrderWithItems) {
        _lastCompletedOrder.value = order
    }

    // --- Cart Totals Calculations ---
    val cartSubtotal: StateFlow<Double> = _cartItems.combine(_billDiscountPercent) { items, _ ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemDiscounts: StateFlow<Double> = _cartItems.combine(_billDiscountPercent) { items, _ ->
        items.sumOf { it.discountAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartBillDiscountAmount: StateFlow<Double> = combine(_cartItems, _billDiscountPercent) { items, billDisc ->
        val afterItemDisc = items.sumOf { it.taxableAmount }
        afterItemDisc * (billDisc / 100.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotalDiscount: StateFlow<Double> = combine(cartItemDiscounts, cartBillDiscountAmount) { itemDisc, billDisc ->
        itemDisc + billDisc
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTaxTotal: StateFlow<Double> = combine(_cartItems, _billDiscountPercent) { items, billDisc ->
        val billFactor = 1.0 - (billDisc / 100.0)
        items.sumOf { item ->
            val effectiveTaxable = item.taxableAmount * billFactor
            effectiveTaxable * (item.product.gstRate / 100.0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartGrandTotal: StateFlow<Double> = combine(_cartItems, _billDiscountPercent) { items, billDisc ->
        val billFactor = 1.0 - (billDisc / 100.0)
        var total = 0.0
        for (item in items) {
            val effectiveTaxable = item.taxableAmount * billFactor
            val gst = effectiveTaxable * (item.product.gstRate / 100.0)
            total += (effectiveTaxable + gst)
        }
        total
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun completeCheckout(onSuccess: (OrderWithItems) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val subtotal = items.sumOf { it.subtotal }
        val billDiscPercent = _billDiscountPercent.value
        val billFactor = 1.0 - (billDiscPercent / 100.0)
        val billDiscAmount = items.sumOf { it.taxableAmount } * (billDiscPercent / 100.0)
        val itemDiscounts = items.sumOf { it.discountAmount }
        val totalDiscount = itemDiscounts + billDiscAmount

        var totalTax = 0.0
        var grandTotal = 0.0
        var totalCost = 0.0

        val orderItems = mutableListOf<SaleOrderItem>()

        for (item in items) {
            val effectiveTaxable = item.taxableAmount * billFactor
            val itemTax = effectiveTaxable * (item.product.gstRate / 100.0)
            val lineTotal = effectiveTaxable + itemTax
            val lineCost = item.product.costPrice * item.quantity

            totalTax += itemTax
            grandTotal += lineTotal
            totalCost += lineCost

            orderItems.add(
                SaleOrderItem(
                    productId = item.product.id,
                    productName = item.product.name,
                    category = item.product.category,
                    quantity = item.quantity,
                    unitPrice = item.product.sellingPrice,
                    costPrice = item.product.costPrice,
                    gstRate = item.product.gstRate,
                    taxAmount = itemTax,
                    totalAmount = lineTotal
                )
            )
        }

        val netProfit = (grandTotal - totalTax - totalCost).coerceAtLeast(0.0)
        val invoiceNo = "INV-${Calendar.getInstance().get(Calendar.YEAR)}-${(1000 + (System.currentTimeMillis() % 9000))}"
        val tableNum = _selectedTableNumber.value
        val orderTypeVal = _selectedOrderType.value

        val order = SaleOrder(
            invoiceNumber = invoiceNo,
            timestamp = System.currentTimeMillis(),
            customerName = _customerName.value.ifBlank { if (tableNum.isNotBlank()) "Table $tableNum" else "Walk-in Customer" },
            customerPhone = _customerPhone.value,
            paymentMethod = _paymentMethod.value,
            subtotal = subtotal,
            discountPercent = billDiscPercent,
            discountAmount = totalDiscount,
            taxAmount = totalTax,
            totalAmount = grandTotal,
            totalCost = totalCost,
            netProfit = netProfit,
            tableNumber = tableNum,
            orderType = orderTypeVal,
            notes = buildString {
                if (_paymentMethod.value == "CASH" && _cashTendered.value > 0) {
                    val change = (_cashTendered.value - grandTotal).coerceAtLeast(0.0)
                    append("Cash tendered ₹${String.format("%.2f", _cashTendered.value)}, Change ₹${String.format("%.2f", change)}. ")
                }
                if (_serverName.value.isNotBlank()) append("Server: ${_serverName.value}. ")
                if (_guestCount.value > 0 && orderTypeVal == "Dine-in") append("Pax: ${_guestCount.value}. ")
                if (_orderNotes.value.isNotBlank()) append("Notes: ${_orderNotes.value}")
            }.trim()
        )

        viewModelScope.launch(Dispatchers.IO) {
            val orderId = repository.completeSale(order, orderItems, items)
            if (tableNum.isNotBlank()) {
                _tableDrafts.remove(tableNum)
                val table = repository.getTableByNumber(tableNum)
                if (table != null) {
                    repository.vacateTable(table.tableId)
                }
                val kotsForTable = repository.allKots.firstOrNull()?.filter {
                    it.tableNumber.equals(tableNum, ignoreCase = true) && it.status != "CANCELLED"
                } ?: emptyList()
                for (kot in kotsForTable) {
                    repository.updateKotStatus(kot.kotId, "SERVED")
                }
            }
            val completedOrderWithItems = OrderWithItems(
                order = order.copy(orderId = orderId),
                items = orderItems.map { it.copy(orderId = orderId) }
            )
            _lastCompletedOrder.value = completedOrderWithItems
            clearCart()
            _selectedTableNumber.value = ""
            _selectedOrderType.value = "Dine-in"
            _guestCount.value = 2
            _serverName.value = ""
            _orderNotes.value = ""
            launch(Dispatchers.Main) {
                onSuccess(completedOrderWithItems)
            }
        }
    }

    // --- Product CRUD ---
    fun saveProduct(
        id: Long = 0,
        name: String,
        category: String,
        sku: String,
        sellingPrice: Double,
        costPrice: Double = 0.0,
        stockQuantity: Int = 50,
        reorderLevel: Int = 10,
        gstRate: Double = 5.0,
        unit: String = "portion",
        kitchenSection: String = "KITCHEN",
        isIngredient: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val product = ProductItem(
                id = id,
                name = name.trim(),
                category = category.trim(),
                sku = sku.trim(),
                sellingPrice = sellingPrice,
                costPrice = costPrice,
                stockQuantity = stockQuantity,
                reorderLevel = reorderLevel,
                gstRate = gstRate,
                unit = unit.trim().ifBlank { "portion" },
                kitchenSection = kitchenSection.trim().ifBlank { "KITCHEN" },
                isRawIngredient = isIngredient
            )
            if (id == 0L) {
                repository.addProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(product: ProductItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProduct(product)
        }
    }

    fun adjustStock(productId: Long, delta: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adjustStock(productId, delta)
        }
    }

    // --- Real-Time Sales Reporting State ---
    private val _selectedReportRange = MutableStateFlow(ReportTimeRange.TODAY)
    val selectedReportRange: StateFlow<ReportTimeRange> = _selectedReportRange.asStateFlow()

    fun setReportTimeRange(range: ReportTimeRange) {
        _selectedReportRange.value = range
    }

    val allOrdersWithItems: StateFlow<List<OrderWithItems>> = repository.allOrdersWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentSalesSummary: StateFlow<SalesSummary> = _selectedReportRange.flatMapLatest { range ->
        repository.getOrdersForRange(range)
    }.combine(_selectedReportRange) { orders, range ->
        calculateSalesSummary(range, orders)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SalesSummary(
            timeRange = ReportTimeRange.TODAY,
            orderCount = 0,
            grossSales = 0.0,
            totalDiscounts = 0.0,
            totalGst = 0.0,
            totalCostOfGoods = 0.0,
            netProfit = 0.0,
            averageOrderValue = 0.0,
            gstSlabs = emptyList(),
            categoryBreakdown = emptyList(),
            topSellingProducts = emptyList(),
            paymentBreakdown = PaymentMethodBreakdown(0.0, 0.0, 0.0, 0, 0, 0),
            orders = emptyList()
        )
    )

    private fun calculateSalesSummary(range: ReportTimeRange, orders: List<OrderWithItems>): SalesSummary {
        val count = orders.size
        val grossSales = orders.sumOf { it.order.totalAmount }
        val totalDiscounts = orders.sumOf { it.order.discountAmount }
        val totalGst = orders.sumOf { it.order.taxAmount }
        val totalCost = orders.sumOf { it.order.totalCost }
        val netProfit = orders.sumOf { it.order.netProfit }
        val aov = if (count > 0) grossSales / count else 0.0

        // Payment Breakdown
        var cashTotal = 0.0
        var cardTotal = 0.0
        var upiTotal = 0.0
        var cashCount = 0
        var cardCount = 0
        var upiCount = 0

        for (o in orders) {
            when (o.order.paymentMethod.uppercase()) {
                "CASH" -> {
                    cashTotal += o.order.totalAmount
                    cashCount++
                }
                "CARD", "DEBIT_CARD", "CREDIT_CARD" -> {
                    cardTotal += o.order.totalAmount
                    cardCount++
                }
                "UPI", "ONLINE" -> {
                    upiTotal += o.order.totalAmount
                    upiCount++
                }
                else -> {
                    cashTotal += o.order.totalAmount
                    cashCount++
                }
            }
        }

        val paymentBreakdown = PaymentMethodBreakdown(
            cashTotal = cashTotal,
            cardTotal = cardTotal,
            upiTotal = upiTotal,
            cashCount = cashCount,
            cardCount = cardCount,
            upiCount = upiCount
        )

        // GST Slabs Breakdown
        val slabMap = mutableMapOf<Double, Pair<Double, Double>>() // rate -> (taxableBase, totalTax)
        for (order in orders) {
            for (item in order.items) {
                val existing = slabMap.getOrDefault(item.gstRate, Pair(0.0, 0.0))
                val taxable = (item.totalAmount - item.taxAmount).coerceAtLeast(0.0)
                slabMap[item.gstRate] = Pair(
                    existing.first + taxable,
                    existing.second + item.taxAmount
                )
            }
        }

        val gstSlabs = slabMap.map { (rate, pair) ->
            GstSlabSummary(
                rate = rate,
                taxableAmount = pair.first,
                cgstAmount = pair.second / 2.0,
                sgstAmount = pair.second / 2.0,
                totalTax = pair.second
            )
        }.sortedBy { it.rate }

        // Category Breakdown
        val categoryMap = mutableMapOf<String, Pair<Int, Double>>()
        for (order in orders) {
            for (item in order.items) {
                val cat = item.category.ifBlank { "General" }
                val current = categoryMap.getOrDefault(cat, Pair(0, 0.0))
                categoryMap[cat] = Pair(
                    current.first + item.quantity,
                    current.second + item.totalAmount
                )
            }
        }

        val categoryBreakdown = categoryMap.map { (category, data) ->
            val share = if (grossSales > 0) (data.second / grossSales) * 100.0 else 0.0
            CategorySaleStat(
                category = category,
                itemCount = data.first,
                totalRevenue = data.second,
                percentageOfSales = share
            )
        }.sortedByDescending { it.totalRevenue }

        // Top Selling Products
        val productSalesMap = mutableMapOf<String, Triple<String, Int, Double>>()
        for (order in orders) {
            for (item in order.items) {
                val current = productSalesMap[item.productName] ?: Triple(item.category, 0, 0.0)
                productSalesMap[item.productName] = Triple(
                    item.category,
                    current.second + item.quantity,
                    current.third + item.totalAmount
                )
            }
        }

        val topSellingProducts = productSalesMap.map { (name, data) ->
            TopSellingProduct(
                productName = name,
                category = data.first,
                quantitySold = data.second,
                totalRevenue = data.third
            )
        }.sortedByDescending { it.quantitySold }.take(10)

        return SalesSummary(
            timeRange = range,
            orderCount = count,
            grossSales = grossSales,
            totalDiscounts = totalDiscounts,
            totalGst = totalGst,
            totalCostOfGoods = totalCost,
            netProfit = netProfit,
            averageOrderValue = aov,
            gstSlabs = gstSlabs,
            categoryBreakdown = categoryBreakdown,
            topSellingProducts = topSellingProducts,
            paymentBreakdown = paymentBreakdown,
            orders = orders
        )
    }
}
