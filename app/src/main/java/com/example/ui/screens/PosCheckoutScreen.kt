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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
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
    var productToDelete by remember { mutableStateOf<ProductItem?>(null) }

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
                                label = { Text(labelText, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate900,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = Slate700
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) Slate900 else Slate200
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
                    // Search Bar, Add Dish & Cart Quick Button
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
                            placeholder = { Text("Search dish or SKU...", style = MaterialTheme.typography.bodyMedium) },
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

                        // + Add Dish Button
                        Button(
                            onClick = { showAddDishDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier.height(52.dp).testTag("pos_add_dish_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                    .clickable { isCartOpen = true }
                                    .testTag("open_cart_button"),
                                color = if (totalItemsCount > 0) Slate900 else Slate100,
                                contentColor = if (totalItemsCount > 0) Color.White else Slate700
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Open Cart",
                                        modifier = Modifier.size(24.dp)
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
                                label = { Text(labelText, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate900,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = Slate700
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) Slate900 else Slate200
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

                    // GST badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Sky100)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "GST ${product.gstRate.toInt()}%",
                            color = Sky600,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

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

            // Price & Add/Qty stepper & Delete
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = CurrencyFormatter.format(product.sellingPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (product.isOutOfStock) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                modifier = Modifier.size(28.dp).testTag("decrement_${product.id}")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Emerald600, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$currentCartQty",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald600,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = onIncrement,
                                enabled = currentCartQty < product.stockQuantity,
                                modifier = Modifier.size(28.dp).testTag("increment_${product.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Emerald600, modifier = Modifier.size(16.dp))
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
                                Icon(Icons.Default.Add, contentDescription = "Add to Cart", modifier = Modifier.size(14.dp), tint = Emerald600)
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

    val customerName by viewModel.customerName.collectAsStateWithLifecycle()
    val customerPhone by viewModel.customerPhone.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val cashTendered by viewModel.cashTendered.collectAsStateWithLifecycle()

    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCustomerInputs by remember { mutableStateOf(customerName.isNotBlank() || customerPhone.isNotBlank()) }

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
                Text(
                    text = "Order Cart",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "${cartItems.sumOf { it.quantity }} items selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
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
                    Icon(Icons.Default.Close, contentDescription = "Close Cart")
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
            colors = CardDefaults.cardColors(containerColor = Slate100),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = Slate600)
                    Text(CurrencyFormatter.format(subtotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Slate900)
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
                            color = if (totalDiscount > 0) Emerald600 else Slate600
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
                                color = Sky600
                            )
                        }
                    }

                    Text(
                        "- " + CurrencyFormatter.format(totalDiscount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (totalDiscount > 0) Emerald600 else Slate500
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // GST breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("GST Tax", style = MaterialTheme.typography.bodyMedium, color = Slate600)
                        Text(
                            "CGST ${CurrencyFormatter.format(taxTotal / 2)} + SGST ${CurrencyFormatter.format(taxTotal / 2)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }
                    Text(CurrencyFormatter.format(taxTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Slate900)
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(8.dp))

                // Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
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
            color = Slate900
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

        Spacer(modifier = Modifier.height(20.dp))

        // Complete Sale Button
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
                text = "Complete Sale • ${CurrencyFormatter.format(grandTotal)}",
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
        border = androidx.compose.foundation.BorderStroke(0.8.dp, Slate200),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${CurrencyFormatter.format(item.product.sellingPrice)} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• GST ${item.product.gstRate.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Sky600,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Qty Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
            ) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(
                    onClick = onIncrement,
                    enabled = item.quantity < item.product.stockQuantity,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Line Total
            Text(
                text = CurrencyFormatter.format(item.totalAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Slate400, modifier = Modifier.size(18.dp))
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
            containerColor = if (isSelected) Slate900 else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Slate900 else Slate200
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
                tint = if (isSelected) Color.White else Slate700,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else Slate800
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
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    customPercentInput = preset.toString()
                                },
                            color = if (customPercentInput == preset.toString()) Slate900 else Slate100
                        ) {
                            Text(
                                text = "${preset.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (customPercentInput == preset.toString()) Color.White else Slate800,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                    ) {
                        Text("Apply")
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
