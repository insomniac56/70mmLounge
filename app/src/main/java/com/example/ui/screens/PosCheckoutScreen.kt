package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.TakeoutDining
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CartItem
import com.example.data.model.ProductItem
import com.example.data.model.RestaurantTable
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose500
import com.example.ui.theme.Sky100
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.PosViewModel
import com.example.util.CurrencyFormatter
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosCheckoutScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredCatalogProducts.collectAsStateWithLifecycle()
    val allActiveProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val cartTaxTotal by viewModel.cartTaxTotal.collectAsStateWithLifecycle()
    val cartTotalDiscount by viewModel.cartTotalDiscount.collectAsStateWithLifecycle()
    val cartGrandTotal by viewModel.cartGrandTotal.collectAsStateWithLifecycle()

    val totalItemsCount = cartItems.sumOf { it.quantity }
    var isCartOpen by remember { mutableStateOf(false) }
    var showAddDishDialog by remember { mutableStateOf(false) }
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductItem?>(null) }

    val selectedTableNumber by viewModel.selectedTableNumber.collectAsStateWithLifecycle()
    val selectedOrderType by viewModel.selectedOrderType.collectAsStateWithLifecycle()
    val guestCount by viewModel.guestCount.collectAsStateWithLifecycle()
    val customerName by viewModel.customerName.collectAsStateWithLifecycle()
    val customerPhone by viewModel.customerPhone.collectAsStateWithLifecycle()
    val serverName by viewModel.serverName.collectAsStateWithLifecycle()
    val orderNotes by viewModel.orderNotes.collectAsStateWithLifecycle()
    val allTables by viewModel.allTables.collectAsStateWithLifecycle()
    val activeRunningTables by viewModel.activeRunningTables.collectAsStateWithLifecycle()

    val defaultCategories = listOf(
        "All",
        "Soups",
        "Appetizers & Chaap",
        "Tandoor & Kebabs",
        "Chinese",
        "Noodles",
        "Main Course (Veg & Dal)",
        "Main Course (Non-Veg)",
        "Rice & Biryani",
        "Indian Bread",
        "Papad, Salad & Raita",
        "Special Mocktails",
        "Beverages & Shakes",
        "Desserts"
    )
    val categories = (defaultCategories + allActiveProducts.map { it.category }).distinct()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 768.dp

        if (isWideScreen) {
            // DESKTOP DUAL-PANE RESPONSIVE POS LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Pane: Catalog, Categories, Search & Add Dish
                Column(
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight()
                ) {
                    // Search & Add Dish Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showNewOrderDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier.height(52.dp).testTag("pos_new_order_button_desktop")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Order", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pos_search_input"),
                            placeholder = { Text("Search dishes by name or SKU...", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate500)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Slate500)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Slate900,
                                unfocusedBorderColor = Slate200,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Button(
                            onClick = { showAddDishDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier.height(52.dp).testTag("pos_add_dish_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Dish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (selectedTableNumber.isNotBlank() || customerName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Emerald500.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            color = Emerald600.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = Emerald500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = buildString {
                                            if (selectedTableNumber.isNotBlank()) append("Table $selectedTableNumber • ")
                                            if (customerName.isNotBlank()) append("$customerName • ")
                                            append(selectedOrderType)
                                            if (guestCount > 0 && selectedOrderType == "Dine-in") append(" ($guestCount Guests)")
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Change",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500,
                                        modifier = Modifier
                                            .clickable { showNewOrderDialog = true }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                    Text(
                                        text = "Clear",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Rose500,
                                        modifier = Modifier
                                            .clickable { viewModel.clearActiveOrder() }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Categories Horizontal Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in categories) {
                            val isSelected = (selectedCategory == cat)
                            val count = if (cat == "All") allActiveProducts.size else allActiveProducts.count { it.category.equals(cat, ignoreCase = true) }
                            val labelText = if (count > 0) "$cat ($count)" else cat

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedCategory(cat) },
                                label = {
                                    Text(
                                        text = labelText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald600,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF242F31),
                                    labelColor = Color(0xFFE2E8F0)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) Emerald500 else Color(0xFF4B5B5E)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("filter_chip_$cat")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Active Category Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory == "All") "All Dishes (${products.size})" else "$selectedCategory • ${products.size} Dishes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedCategory != "All" || searchQuery.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.setSelectedCategory("All")
                                    viewModel.setSearchQuery("")
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Show All Dishes", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Adaptive Grid of Dishes for Desktop
                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No dishes found", style = MaterialTheme.typography.titleMedium, color = Slate600)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Click '+ Add Dish' to create a new menu item", style = MaterialTheme.typography.bodySmall, color = Slate400)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 220.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(products, key = { it.id }) { product ->
                                val cartItem = cartItems.find { it.product.id == product.id }
                                ProductCatalogGridCard(
                                    product = product,
                                    currentCartQty = cartItem?.quantity ?: 0,
                                    onAddToCart = { viewModel.addToCart(product) },
                                    onIncrement = { viewModel.updateCartItemQuantity(product.id, 1) },
                                    onDecrement = { viewModel.updateCartItemQuantity(product.id, -1) },
                                    onDelete = { productToDelete = product }
                                )
                            }
                        }
                    }
                }

                // Right Pane: Persistent Desktop Register / Cart & Tender
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    CartCheckoutSheetContent(
                        viewModel = viewModel,
                        onClose = { viewModel.clearCart() }
                    )
                }
            }
        } else {
            // MOBILE / COMPACT SCREEN LAYOUT
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Top Strip: + New Order Button & Horizontal Active Running Tables
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showNewOrderDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("pos_new_order_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Order", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }

                        // Running table chips
                        for (rt in activeRunningTables) {
                            val isSelected = (selectedTableNumber == rt.tableNumber)
                            val isReq = rt.isCheckoutRequested
                            Surface(
                                onClick = { viewModel.loadTableIntoPosCart(rt) },
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected || isReq) 1.8.dp else 1.dp,
                                        color = if (isReq) Amber500 else if (isSelected) Emerald500 else Slate700,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .testTag("pos_table_chip_${rt.tableNumber}"),
                                color = if (isReq) Amber500.copy(alpha = 0.2f)
                                        else if (isSelected) Emerald600.copy(alpha = 0.2f)
                                        else Slate900
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                        .fillMaxHeight(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isReq) Amber500 else Emerald500)
                                    )
                                    Text(
                                        text = "T-${rt.tableNumber}" + if (rt.currentGuestName.isNotBlank()) " (${rt.currentGuestName.take(8)})" else "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "₹${String.format("%.0f", rt.currentBillAmount)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = if (isReq) Amber500 else Emerald600
                                    )
                                    if (isReq) {
                                        Text(
                                            text = "• Complete Order 🔔",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = Amber500,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Full-width Search Bar, Add Dish & Cart Quick Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pos_search_input"),
                            placeholder = {
                                Text(
                                    "Search dishes or SKU...",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate500, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Slate500, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate400,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // + Add Dish Button
                        Surface(
                            onClick = { showAddDishDialog = true },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .testTag("pos_add_dish_button"),
                            color = Slate900,
                            contentColor = Emerald600
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Dish",
                                    tint = Emerald600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Quick Cart Access Icon Button
                        BadgedBox(
                            badge = {
                                if (totalItemsCount > 0) {
                                    Badge(
                                        containerColor = Emerald600,
                                        contentColor = Color.White
                                    ) {
                                        Text("$totalItemsCount")
                                    }
                                }
                            }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, if (totalItemsCount > 0) Emerald500 else Slate700, RoundedCornerShape(12.dp))
                                    .clickable { isCartOpen = true }
                                    .testTag("open_cart_button"),
                                color = if (totalItemsCount > 0) Emerald600 else Slate900,
                                contentColor = if (totalItemsCount > 0) Color.White else Emerald600
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Open Cart",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedTableNumber.isNotBlank() || customerName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Emerald500.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            color = Emerald600.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = Emerald500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = buildString {
                                            if (selectedTableNumber.isNotBlank()) append("Table $selectedTableNumber • ")
                                            if (customerName.isNotBlank()) append("$customerName • ")
                                            append(selectedOrderType)
                                            if (guestCount > 0 && selectedOrderType == "Dine-in") append(" ($guestCount Pax)")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Edit",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500,
                                        modifier = Modifier
                                            .clickable { showNewOrderDialog = true }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "Clear",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Rose500,
                                        modifier = Modifier
                                            .clickable { viewModel.clearActiveOrder() }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Horizontal Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in categories) {
                            val isSelected = (selectedCategory == cat)
                            val count = if (cat == "All") allActiveProducts.size else allActiveProducts.count { it.category.equals(cat, ignoreCase = true) }
                            val labelText = if (count > 0) "$cat ($count)" else cat

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedCategory(cat) },
                                label = {
                                    Text(
                                        text = labelText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald600,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF242F31),
                                    labelColor = Color(0xFFE2E8F0)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) Emerald500 else Color(0xFF4B5B5E)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("filter_chip_$cat")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Active Category Title & Dishes Count Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory == "All") "All Dishes (${products.size})" else "$selectedCategory • ${products.size} Dishes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedCategory != "All" || searchQuery.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.setSelectedCategory("All")
                                    viewModel.setSearchQuery("")
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Show All Dishes", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Product Catalog List
                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No products found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Slate600
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try adjusting your search or category filter",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(products, key = { it.id }) { product ->
                                val cartItem = cartItems.find { it.product.id == product.id }
                                ProductCatalogRow(
                                    product = product,
                                    currentCartQty = cartItem?.quantity ?: 0,
                                    onAddToCart = { viewModel.addToCart(product) },
                                    onIncrement = { viewModel.updateCartItemQuantity(product.id, 1) },
                                    onDecrement = { viewModel.updateCartItemQuantity(product.id, -1) },
                                    onDelete = { productToDelete = product }
                                )
                            }
                        }
                    }
                }

                // Floating Bottom Cart Bar (if items in cart)
                AnimatedVisibility(
                    visible = totalItemsCount > 0,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { isCartOpen = true }
                            .testTag("bottom_cart_summary_bar"),
                        color = Slate900,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Emerald600),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$totalItemsCount",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Current Order",
                                        color = Slate400,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(cartGrandTotal),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = { isCartOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("checkout_cart_button")
                            ) {
                                Text("Checkout", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Modal Bottom Sheet for Cart & Checkout Tender (mobile)
            if (isCartOpen) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { isCartOpen = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    CartCheckoutSheetContent(
                        viewModel = viewModel,
                        onClose = { isCartOpen = false }
                    )
                }
            }
        }

        // New Order Dialog
        if (showNewOrderDialog) {
            NewOrderDialog(
                allTables = allTables,
                currentTableNumber = "",
                currentOrderType = "Dine-in",
                currentCustomerName = "",
                currentCustomerPhone = "",
                currentGuestCount = 2,
                currentServerName = "",
                currentNotes = "",
                onDismiss = { showNewOrderDialog = false },
                onStartOrder = { name, phone, table, orderType, pax, server, notes ->
                    viewModel.startNewOrder(
                        customerName = name,
                        customerPhone = phone,
                        tableNumber = table,
                        orderType = orderType,
                        guestCount = pax,
                        serverName = server,
                        notes = notes
                    )
                    showNewOrderDialog = false
                }
            )
        }

        // Add Dish Dialog
        if (showAddDishDialog) {
            AddDishDialog(
                existingCategories = categories.filter { it != "All" },
                onDismiss = { showAddDishDialog = false },
                onSave = { name, category, sku, price, station, gst ->
                    viewModel.saveProduct(
                        name = name,
                        category = category,
                        sku = sku,
                        sellingPrice = price,
                        kitchenSection = station,
                        gstRate = gst
                    )
                    showAddDishDialog = false
                }
            )
        }

        // Delete Dish Confirmation Dialog
        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Delete Dish from Menu?") },
                text = {
                    Text("Are you sure you want to delete '${productToDelete!!.name}' (${productToDelete!!.sku.ifBlank { "No SKU" }}) from the restaurant menu? This cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(productToDelete!!)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                    ) {
                        Text("Delete Dish")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProductCatalogRow(
    product: ProductItem,
    currentCartQty: Int,
    onAddToCart: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Slate200, RoundedCornerShape(12.dp))
            .clickable(enabled = !product.isOutOfStock) {
                if (currentCartQty == 0) onAddToCart() else onIncrement()
            }
            .testTag("product_item_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isOutOfStock) Color(0xFFF8FAFC).copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (product.sku.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = product.sku,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isOutOfStock) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category & Kitchen Station
                    Text(
                        text = "${product.category} • ${product.kitchenSection}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    // Stock status badge
                    if (product.isOutOfStock) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Rose100)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Out of stock",
                                color = Rose500,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (product.isLowStock) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Amber100)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Only ${product.stockQuantity} left",
                                color = Amber500,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Price & Add/Qty stepper & Delete (stable layout preventing shift)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.widthIn(min = 96.dp)
            ) {
                Text(
                    text = CurrencyFormatter.format(product.sellingPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    color = if (product.isOutOfStock) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentCartQty > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Emerald600.copy(alpha = 0.15f))
                                .border(1.dp, Emerald600, RoundedCornerShape(8.dp))
                        ) {
                            IconButton(
                                onClick = onDecrement,
                                modifier = Modifier.size(28.dp).testTag("decrement_${product.id}")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Emerald500, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$currentCartQty",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = onIncrement,
                                enabled = currentCartQty < product.stockQuantity,
                                modifier = Modifier.size(28.dp).testTag("increment_${product.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Emerald500, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !product.isOutOfStock, onClick = onAddToCart),
                            color = if (product.isOutOfStock) Slate800 else Emerald600,
                            contentColor = if (product.isOutOfStock) Slate500 else Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add to Cart",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (product.isOutOfStock) Slate500 else Color.White
                                )
                                Text("ADD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).testTag("delete_dish_${product.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Dish",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCatalogGridCard(
    product: ProductItem,
    currentCartQty: Int,
    onAddToCart: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Slate200, RoundedCornerShape(14.dp))
            .clickable(enabled = !product.isOutOfStock) {
                if (currentCartQty == 0) onAddToCart() else onIncrement()
            }
            .testTag("product_grid_item_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isOutOfStock) Color(0xFFF8FAFC).copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top: SKU badge + Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (product.sku.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = product.sku,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Dish",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dish Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (product.isOutOfStock) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category & Station
            Text(
                text = "${product.category} • ${product.kitchenSection}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom: Price & Add Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyFormatter.format(product.sellingPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (product.isOutOfStock) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                )

                if (currentCartQty > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald50)
                            .border(1.dp, Emerald100, RoundedCornerShape(8.dp))
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Emerald600, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "$currentCartQty",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = onIncrement,
                            enabled = currentCartQty < product.stockQuantity,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Emerald600, modifier = Modifier.size(14.dp))
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !product.isOutOfStock, onClick = onAddToCart),
                        color = if (product.isOutOfStock) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (product.isOutOfStock) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp), tint = Emerald600)
                            Text("ADD", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartCheckoutSheetContent(
    viewModel: PosViewModel,
    onClose: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val subtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val totalDiscount by viewModel.cartTotalDiscount.collectAsStateWithLifecycle()
    val billDiscountPercent by viewModel.billDiscountPercent.collectAsStateWithLifecycle()
    val taxTotal by viewModel.cartTaxTotal.collectAsStateWithLifecycle()
    val grandTotal by viewModel.cartGrandTotal.collectAsStateWithLifecycle()

    val selectedTableNumber by viewModel.selectedTableNumber.collectAsStateWithLifecycle()
    val customerName by viewModel.customerName.collectAsStateWithLifecycle()
    val customerPhone by viewModel.customerPhone.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val cashTendered by viewModel.cashTendered.collectAsStateWithLifecycle()

    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCustomerInputs by remember { mutableStateOf(customerName.isNotBlank() || customerPhone.isNotBlank()) }
    var kotSentNotice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cart Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (selectedTableNumber.isNotBlank()) "Table $selectedTableNumber Cart" else "Order Cart",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (selectedTableNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Emerald600.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Running Table",
                                color = Emerald600,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${cartItems.sumOf { it.quantity }} items selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                TextButton(
                    onClick = { viewModel.clearCart() },
                    modifier = Modifier.testTag("clear_cart_button")
                ) {
                    Text("Clear All", color = Rose500)
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Cart", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cart items list
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Your cart is empty", color = Slate400)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (item in cartItems) {
                    CartLineItemCard(
                        item = item,
                        onIncrement = { viewModel.updateCartItemQuantity(item.product.id, 1) },
                        onDecrement = { viewModel.updateCartItemQuantity(item.product.id, -1) },
                        onRemove = { viewModel.removeFromCart(item.product.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Slate200)
        Spacer(modifier = Modifier.height(12.dp))

        // Customer Details Accordion
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCustomerInputs = !showCustomerInputs }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Customer Details (Optional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Slate800
            )
            Text(
                text = if (showCustomerInputs) "Hide" else "Add",
                style = MaterialTheme.typography.labelMedium,
                color = Sky600,
                fontWeight = FontWeight.Bold
            )
        }

        if (showCustomerInputs) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { viewModel.setCustomerDetails(it, customerPhone) },
                    modifier = Modifier.weight(1f).testTag("customer_name_input"),
                    label = { Text("Customer Name") },
                    placeholder = { Text("Walk-in") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { viewModel.setCustomerDetails(customerName, it) },
                    modifier = Modifier.weight(1f).testTag("customer_phone_input"),
                    label = { Text("Phone") },
                    placeholder = { Text("555-0100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order Bill Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(subtotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Discount Row with Quick Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Bill Discount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (totalDiscount > 0) Emerald600 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = { showDiscountDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (billDiscountPercent > 0) "${billDiscountPercent.toInt()}% Edit" else "+ Add %",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald600
                            )
                        }
                    }

                    Text(
                        "- " + CurrencyFormatter.format(totalDiscount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (totalDiscount > 0) Emerald600 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // GST breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("GST Tax", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "CGST ${CurrencyFormatter.format(taxTotal / 2)} + SGST ${CurrencyFormatter.format(taxTotal / 2)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                    Text(CurrencyFormatter.format(taxTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                // Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(CurrencyFormatter.format(grandTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Emerald600)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Method Selector
        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethodCard(
                label = "Cash",
                icon = Icons.Default.Payments,
                isSelected = paymentMethod == "CASH",
                onClick = { viewModel.setPaymentMethod("CASH") },
                modifier = Modifier.width(96.dp).testTag("payment_cash")
            )
            PaymentMethodCard(
                label = "Online (UPI)",
                icon = Icons.Default.QrCode,
                isSelected = paymentMethod == "ONLINE" || paymentMethod == "UPI",
                onClick = { viewModel.setPaymentMethod("ONLINE") },
                modifier = Modifier.width(110.dp).testTag("payment_online")
            )
            PaymentMethodCard(
                label = "Debit Card",
                icon = Icons.Default.CreditCard,
                isSelected = paymentMethod == "DEBIT_CARD",
                onClick = { viewModel.setPaymentMethod("DEBIT_CARD") },
                modifier = Modifier.width(104.dp).testTag("payment_debit_card")
            )
            PaymentMethodCard(
                label = "Credit Card",
                icon = Icons.Default.CreditCard,
                isSelected = paymentMethod == "CREDIT_CARD" || paymentMethod == "CARD",
                onClick = { viewModel.setPaymentMethod("CREDIT_CARD") },
                modifier = Modifier.width(104.dp).testTag("payment_credit_card")
            )
        }

        // Online UPI QR Box
        if (paymentMethod == "ONLINE" || paymentMethod == "UPI") {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Emerald50)
                    .border(1.dp, Emerald100, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "UPI QR Code • Instant Scan & Pay",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Emerald600
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Scan using Google Pay, PhonePe, Paytm or BHIM",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Text(
                        text = "UPI ID: 70mmlounge@upi",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate900
                    )
                }
            }
        }

        // Debit Card / Credit Card Terminal Info Box
        if (paymentMethod == "DEBIT_CARD" || paymentMethod == "CREDIT_CARD" || paymentMethod == "CARD") {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = if (paymentMethod == "DEBIT_CARD") "Debit Card Swipe / Tap Terminal" else "Credit Card POS Machine",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Insert chip card or tap on POS machine. Collect printed card receipt slip.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    fontSize = 11.sp
                )
            }
        }

        // Cash Change Calculator
        if (paymentMethod == "CASH") {
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Cash Tendered & Change",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick cash shortcuts in Rupees: Exact, Ceil to 50, 100, 500
                val exact = grandTotal
                val round50 = (ceil(grandTotal / 50.0) * 50.0).coerceAtLeast(50.0)
                val round100 = (ceil(grandTotal / 100.0) * 100.0).coerceAtLeast(100.0)
                val round500 = (ceil(grandTotal / 500.0) * 500.0).coerceAtLeast(500.0)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickCashButton("Exact", exact) { viewModel.setCashTendered(exact) }
                    QuickCashButton(CurrencyFormatter.formatWhole(round50), round50) { viewModel.setCashTendered(round50) }
                    QuickCashButton(CurrencyFormatter.formatWhole(round100), round100) { viewModel.setCashTendered(round100) }
                    QuickCashButton(CurrencyFormatter.formatWhole(round500), round500) { viewModel.setCashTendered(round500) }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = if (cashTendered > 0) String.format("%.2f", cashTendered) else "",
                    onValueChange = {
                        val amount = it.toDoubleOrNull() ?: 0.0
                        viewModel.setCashTendered(amount)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("cash_tendered_input"),
                    label = { Text("Tendered Cash Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                val changeDue = (cashTendered - grandTotal).coerceAtLeast(0.0)
                if (cashTendered >= grandTotal && grandTotal > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald100)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Change to Return:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600
                        )
                        Text(
                            text = CurrencyFormatter.format(changeDue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Emerald600
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Send KOT to Kitchen Button (for running / dine-in tables)
        if (selectedTableNumber.isNotBlank()) {
            Button(
                onClick = {
                    viewModel.sendKotToKitchen {
                        kotSentNotice = true
                    }
                },
                enabled = cartItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_kot_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (kotSentNotice) Emerald600 else Amber500,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    if (kotSentNotice) Icons.Default.Check else Icons.Default.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (kotSentNotice) "KOT Sent to Kitchen! ✓" else "Send KOT to Kitchen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Complete Sale / Settle Bill Button
        Button(
            onClick = {
                viewModel.completeCheckout {
                    onClose()
                }
            },
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("complete_sale_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (selectedTableNumber.isNotBlank())
                    "Checkout Table $selectedTableNumber • ${CurrencyFormatter.format(grandTotal)}"
                else
                    "Complete Sale • ${CurrencyFormatter.format(grandTotal)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    // Bill Discount Quick Picker Dialog
    if (showDiscountDialog) {
        DiscountPickerDialog(
            currentPercent = billDiscountPercent,
            onDismiss = { showDiscountDialog = false },
            onApply = { percent ->
                viewModel.setBillDiscountPercent(percent)
                showDiscountDialog = false
            }
        )
    }
}

@Composable
private fun CartLineItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${CurrencyFormatter.format(item.product.sellingPrice)} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Qty Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(30.dp)) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(
                    onClick = onIncrement,
                    enabled = item.quantity < item.product.stockQuantity,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Emerald600,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Line Total
            Text(
                text = CurrencyFormatter.format(item.totalAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = Rose500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Emerald600 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Emerald600 else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickCashButton(
    label: String,
    amount: Double,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = Slate200
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate800,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DiscountPickerDialog(
    currentPercent: Double,
    onDismiss: () -> Unit,
    onApply: (Double) -> Unit
) {
    var customPercentInput by remember { mutableStateOf(if (currentPercent > 0) currentPercent.toString() else "") }
    val presets = listOf(0.0, 5.0, 10.0, 15.0, 20.0)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Discount, contentDescription = "Discount", tint = Emerald600)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply Bill Discount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (preset in presets) {
                        val isPresetSelected = (customPercentInput == preset.toString())
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    customPercentInput = preset.toString()
                                },
                            color = if (isPresetSelected) Emerald600 else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPresetSelected) Emerald600 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${preset.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPresetSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customPercentInput,
                    onValueChange = { customPercentInput = it },
                    label = { Text("Custom Discount Percentage (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsed = customPercentInput.toDoubleOrNull() ?: 0.0
                            onApply(parsed)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald600,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddDishDialog(
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        sku: String,
        price: Double,
        station: String,
        gstRate: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Main Course (Veg & Dal)") }
    var sku by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var station by remember { mutableStateOf("KITCHEN") }
    var gstRate by remember { mutableStateOf(5.0) }

    val quickCategories = listOf(
        "Soups",
        "Appetizers & Chaap",
        "Tandoor & Kebabs",
        "Chinese",
        "Noodles",
        "Main Course (Veg & Dal)",
        "Main Course (Non-Veg)",
        "Indian Bread",
        "Rice & Biryani",
        "Papad, Salad & Raita",
        "Special Mocktails",
        "Beverages & Shakes",
        "Desserts"
    )

    val stations = listOf("KITCHEN", "GRILL", "BAR")
    val gstOptions = listOf(0.0, 5.0, 12.0, 18.0)

    val isPriceValid = (priceInput.toDoubleOrNull() ?: 0.0) > 0.0
    val isFormValid = name.trim().isNotBlank() && category.trim().isNotBlank() && isPriceValid

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "+ Add New Menu Item",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "70MM Lounge Restaurant & Bar Menu",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dish Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name *") },
                    placeholder = { Text("e.g. Paneer Tikka Masala") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dish_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category selection
                Text(
                    text = "Category (Select or type custom)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Quick chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (cat in quickCategories) {
                        val isSelected = (category.equals(cat, ignoreCase = true))
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate900,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SKU Code & Selling Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it.uppercase() },
                        label = { Text("Dish Code / SKU") },
                        placeholder = { Text("e.g. MCV-15") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dish_sku_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price (₹) *") },
                        placeholder = { Text("250.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dish_price_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Kitchen Station
                Text(
                    text = "Kitchen Prep Station",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (st in stations) {
                        val isSelected = (station == st)
                        FilterChip(
                            selected = isSelected,
                            onClick = { station = st },
                            label = { Text(st, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (st == "BAR") Sky600 else Slate900,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GST Rate
                Text(
                    text = "GST Tax Rate",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (rate in gstOptions) {
                        val isSelected = (gstRate == rate)
                        FilterChip(
                            selected = isSelected,
                            onClick = { gstRate = rate },
                            label = { Text("${rate.toInt()}% GST") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate900,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val price = priceInput.toDoubleOrNull() ?: 0.0
                            val finalSku = sku.trim().ifBlank {
                                val prefix = when (station) {
                                    "BAR" -> "BAR"
                                    "GRILL" -> "GRL"
                                    else -> "DSH"
                                }
                                "$prefix-${(10 + (System.currentTimeMillis() % 90))}"
                            }
                            onSave(name.trim(), category.trim(), finalSku, price, station, gstRate)
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_dish_button")
                    ) {
                        Text("Save Dish to Menu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NewOrderDialog(
    allTables: List<RestaurantTable>,
    currentTableNumber: String,
    currentOrderType: String,
    currentCustomerName: String,
    currentCustomerPhone: String,
    currentGuestCount: Int,
    currentServerName: String,
    currentNotes: String,
    onDismiss: () -> Unit,
    onStartOrder: (name: String, phone: String, table: String, orderType: String, pax: Int, server: String, notes: String) -> Unit
) {
    var orderType by remember { mutableStateOf(currentOrderType.ifBlank { "Dine-in" }) }
    var selectedTable by remember { mutableStateOf(currentTableNumber) }
    var customerName by remember { mutableStateOf(currentCustomerName) }
    var customerPhone by remember { mutableStateOf(currentCustomerPhone) }
    var guestCount by remember { mutableStateOf(if (currentGuestCount > 0) currentGuestCount else 2) }
    var serverName by remember { mutableStateOf(currentServerName) }
    var notes by remember { mutableStateOf(currentNotes) }
    var selectedZone by remember { mutableStateOf("All") }

    val orderTypes = listOf("Dine-in", "Takeaway", "Delivery")
    val zones = listOf("All") + allTables.map { it.zone }.distinct().filter { it.isNotBlank() }
    val filteredTables = if (selectedZone == "All") allTables else allTables.filter { it.zone.equals(selectedZone, ignoreCase = true) }

    Dialog(
        onDismissRequest = { /* ignore clicks outside */ },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = Emerald600.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Emerald600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "New Order",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Table selection & customer details",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Order Type Selector
                Text(
                    text = "Order Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (type in orderTypes) {
                        val isSelected = (orderType == type)
                        val icon = when (type) {
                            "Takeaway" -> Icons.Default.TakeoutDining
                            "Delivery" -> Icons.Default.DeliveryDining
                            else -> Icons.Default.TableBar
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    orderType = type
                                    if (type != "Dine-in") selectedTable = ""
                                },
                            color = if (isSelected) Emerald600 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Dine-in: Table Selection
                if (orderType == "Dine-in") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTable.isNotBlank()) "Selected: Table $selectedTable" else "Select Table (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTable.isNotBlank()) Emerald600 else MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedTable.isNotBlank()) {
                            Text(
                                text = "Clear",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Rose500,
                                modifier = Modifier.clickable { selectedTable = "" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Zone filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (z in zones) {
                            val isSel = (selectedZone == z)
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedZone = z },
                                label = { Text(z, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald600,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tables horizontal scroll list
                    if (filteredTables.isEmpty()) {
                        Text(
                            text = "No tables found in this area",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (t in filteredTables) {
                                val isChosen = (selectedTable == t.tableNumber)
                                val isOccupied = t.isOccupied || t.currentBillAmount > 0
                                Surface(
                                    modifier = Modifier
                                        .width(88.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isChosen) 2.dp else 1.dp,
                                            color = if (isChosen) Emerald500 else if (isOccupied) Amber500 else Slate200,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedTable = if (selectedTable == t.tableNumber) "" else t.tableNumber
                                            if (customerName.isBlank() && t.currentGuestName.isNotBlank()) {
                                                customerName = t.currentGuestName
                                            }
                                        },
                                    color = if (isChosen) Emerald600.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = t.tableNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${t.capacity} Seats",
                                            fontSize = 10.sp,
                                            color = Slate500
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isOccupied) Amber500 else Emerald500)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (isOccupied) "Running" else "Free",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOccupied) Amber500 else Emerald500
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Number of Guests (Pax)
                    Text(
                        text = "Number of Guests (Pax)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            IconButton(onClick = { if (guestCount > 1) guestCount-- }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Slate700)
                            }
                            Text(
                                text = "$guestCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(onClick = { guestCount++ }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Slate700)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (p in listOf(1, 2, 4, 6, 8)) {
                                Surface(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { guestCount = p },
                                    color = if (guestCount == p) Emerald600 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    contentColor = if (guestCount == p) Color.White else MaterialTheme.colorScheme.onSurface
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$p", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer Details Section
                Text(
                    text = "Customer Details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer / Guest Name") },
                    placeholder = { Text("e.g. Rohan Sharma") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) customerPhone = it },
                    label = { Text("Mobile Number (Optional)") },
                    placeholder = { Text("10-digit number for invoice") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text("Captain / Server") },
                    placeholder = { Text("e.g. Captain 1") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Requests / Notes") },
                    placeholder = { Text("e.g. Less spicy, extra sauce") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onStartOrder(customerName, customerPhone, selectedTable, orderType, guestCount, serverName, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("start_new_order_submit_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
