package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@Composable
fun InventoryScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val lowStockRawIngredients by viewModel.lowStockRawIngredients.collectAsStateWithLifecycle()
    val showLowStockSheet by viewModel.showLowStockAlertSheet.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showOnlyLowStock by remember { mutableStateOf(false) }

    var productToEdit by remember { mutableStateOf<ProductItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductItem?>(null) }
    var productForStockAdjust by remember { mutableStateOf<ProductItem?>(null) }
    var productForThresholdEdit by remember { mutableStateOf<ProductItem?>(null) }
    var showNotificationCenter by remember { mutableStateOf(false) }

    val categories = listOf("All") + allProducts.map { it.category }.distinct().filter { it.isNotBlank() }

    val filteredProducts = allProducts.filter { p ->
        val matchesCategory = (selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true)
        val matchesLowStock = !showOnlyLowStock || p.isLowStock
        matchesCategory && matchesSearch && matchesLowStock
    }

    val totalStockValue = allProducts.sumOf { it.sellingPrice * it.stockQuantity }
    val lowStockCount = allProducts.count { it.isLowStock }
    val rawIngredientsLowCount = lowStockRawIngredients.size

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // URGENT LOW-STOCK ALERT NOTIFICATION BANNER
            if (lowStockCount > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (rawIngredientsLowCount > 0) Rose500 else Amber500,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showNotificationCenter = true },
                    color = if (rawIngredientsLowCount > 0) Rose100.copy(alpha = 0.8f) else Amber100.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Low Stock Alert",
                                tint = if (rawIngredientsLowCount > 0) Rose500 else Amber500,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (rawIngredientsLowCount > 0) {
                                        "⚠️ CRITICAL: $rawIngredientsLowCount Raw Ingredients Below Threshold!"
                                    } else {
                                        "⚠️ LOW STOCK ALERT: $lowStockCount items below threshold!"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Alert suppliers, restock raw materials or adjust thresholds.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showNotificationCenter = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rawIngredientsLowCount > 0) Rose500 else Amber500
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Alerts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Header Inventory KPI Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total SKUs
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Items", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        Text(
                            "${allProducts.size} SKUs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }

                // Inventory Value
                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Stock Value", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        Text(
                            CurrencyFormatter.format(totalStockValue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }

                // Low Stock Pill Filter
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showOnlyLowStock = !showOnlyLowStock },
                    colors = CardDefaults.cardColors(
                        containerColor = if (showOnlyLowStock) Amber500 else (if (lowStockCount > 0) Amber100 else Slate100)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (lowStockCount > 0) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Low Stock Alert",
                                    tint = if (showOnlyLowStock) Color.White else Amber500,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                "Low Stock",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (showOnlyLowStock) Color.White else (if (lowStockCount > 0) Amber500 else Slate500),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "$lowStockCount items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (showOnlyLowStock) Color.White else (if (lowStockCount > 0) Amber500 else Slate900)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_search_input"),
                placeholder = { Text("Search by name, SKU or barcode...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate500) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate500)
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

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cat in categories) {
                    val isSelected = (selectedCategory == cat)
                    val count = if (cat == "All") allProducts.size else allProducts.count { it.category.equals(cat, ignoreCase = true) }
                    val labelText = if (count > 0) "$cat ($count)" else cat

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
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
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Active Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory == "All") "All Menu Items (${filteredProducts.size})" else "$selectedCategory • ${filteredProducts.size} Items",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
                if (selectedCategory != "All" || searchQuery.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            selectedCategory = "All"
                            searchQuery = ""
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Show All", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Product List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No inventory items found", style = MaterialTheme.typography.titleMedium, color = Slate600)
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
                    items(filteredProducts, key = { it.id }) { product ->
                        InventoryProductCard(
                            product = product,
                            onEdit = { productToEdit = product },
                            onDelete = { productToDelete = product },
                            onAdjustStock = { productForStockAdjust = product },
                            onAdjustThreshold = { productForThresholdEdit = product }
                        )
                    }
                }
            }
        }

        // FAB to add new product
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_product_fab"),
            containerColor = Slate900,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }
    }

    // Low-Stock Alert Notification Center Dialog
    if (showNotificationCenter || showLowStockSheet) {
        LowStockNotificationCenterDialog(
            lowStockProducts = lowStockProducts,
            onDismiss = {
                showNotificationCenter = false
                viewModel.closeLowStockAlertSheet()
            },
            onQuickRestock = { productId, amount ->
                viewModel.quickRestock(productId, amount)
            },
            onAdjustThreshold = { product ->
                productForThresholdEdit = product
            }
        )
    }

    // Adjust Threshold Dialog
    if (productForThresholdEdit != null) {
        AdjustThresholdDialog(
            product = productForThresholdEdit!!,
            onDismiss = { productForThresholdEdit = null },
            onSave = { newThreshold ->
                viewModel.updateProductThreshold(productForThresholdEdit!!.id, newThreshold)
                productForThresholdEdit = null
            }
        )
    }

    // Add or Edit Product Dialog
    if (showAddDialog || productToEdit != null) {
        AddEditProductDialog(
            product = productToEdit,
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },
            onSave = { id, name, category, sku, sellingPrice, costPrice, stock, reorder, gstRate, unit ->
                viewModel.saveProduct(id, name, category, sku, sellingPrice, costPrice, stock, reorder, gstRate, unit)
                showAddDialog = false
                productToEdit = null
            }
        )
    }

    // Stock Adjust Dialog
    if (productForStockAdjust != null) {
        QuickStockAdjustDialog(
            product = productForStockAdjust!!,
            onDismiss = { productForStockAdjust = null },
            onAdjust = { delta ->
                viewModel.adjustStock(productForStockAdjust!!.id, delta)
                productForStockAdjust = null
            }
        )
    }

    // Delete confirmation dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to delete '${productToDelete!!.name}'? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(productToDelete!!)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Delete")
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

@Composable
private fun InventoryProductCard(
    product: ProductItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: () -> Unit,
    onAdjustThreshold: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (product.isOutOfStock) 1.5.dp else if (product.isLowStock) 1.dp else 0.8.dp,
                if (product.isOutOfStock) Rose500 else if (product.isLowStock) Amber500 else Slate200,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (product.isIngredient) {
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFEDE9FE)
                            ) {
                                Text(
                                    text = "RAW INGREDIENT",
                                    color = Color(0xFF6D28D9),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${product.category} • Section: ${product.kitchenSection} • Stock: ${product.stockQuantity} ${product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // GST Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Sky100)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "GST ${product.gstRate.toInt()}%",
                            color = Sky600,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Stock Count Chip
                    val stockColor = when {
                        product.isOutOfStock -> Rose500
                        product.isLowStock -> Amber500
                        else -> Emerald600
                    }
                    val stockBg = when {
                        product.isOutOfStock -> Rose100
                        product.isLowStock -> Amber100
                        else -> Emerald50
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onAdjustStock),
                        color = stockBg
                    ) {
                        Text(
                            text = "${product.stockQuantity} ${product.unit} (Edit)",
                            color = stockColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Low Stock Warning Banner on Card
            if (product.isLowStock) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp)),
                    color = if (product.isOutOfStock) Rose100 else Amber100
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (product.isOutOfStock) Rose500 else Amber500,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (product.isOutOfStock) "OUT OF STOCK! Deficit: ${product.deficitQuantity} ${product.unit}"
                                else "LOW STOCK: Deficit: ${product.deficitQuantity} ${product.unit} (Min: ${product.reorderLevel})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (product.isOutOfStock) Rose500 else Amber500,
                                fontSize = 11.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = onAdjustStock,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("+ Restock", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                            }
                            TextButton(
                                onClick = onAdjustThreshold,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Threshold", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Sky600)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing and Margin Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("Selling Price", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
                        Text(
                            CurrencyFormatter.format(product.sellingPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Column {
                        Text("Cost Price", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
                        Text(
                            CurrencyFormatter.format(product.costPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Slate600
                        )
                    }

                    val margin = if (product.sellingPrice > 0) {
                        ((product.sellingPrice - product.costPrice) / product.sellingPrice) * 100.0
                    } else 0.0

                    Column {
                        Text("Margin", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
                        Text(
                            "${String.format("%.1f", margin)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (margin > 0) Emerald600 else Slate600
                        )
                    }
                }

                // Edit & Delete Action Icons
                Row {
                    IconButton(onClick = onAdjustThreshold, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = "Edit Threshold", tint = Slate500, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = Slate700, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStockAdjustDialog(
    product: ProductItem,
    onDismiss: () -> Unit,
    onAdjust: (Int) -> Unit
) {
    var deltaInput by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Adjust Stock: ${product.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Current Stock: ${product.stockQuantity} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Increment Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickStockPreset("+5 Restock") { onAdjust(5) }
                    QuickStockPreset("+10 Restock") { onAdjust(10) }
                    QuickStockPreset("+25 Restock") { onAdjust(25) }
                    QuickStockPreset("-1 Sold/Loss") { onAdjust(-1) }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Stock Entry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isAdding,
                        onClick = { isAdding = true },
                        label = { Text("+ Add") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald600,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isAdding,
                        onClick = { isAdding = false },
                        label = { Text("- Deduct") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Rose500,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deltaInput,
                    onValueChange = { deltaInput = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            val qty = deltaInput.toIntOrNull() ?: 0
                            val finalDelta = if (isAdding) qty else -qty
                            onAdjust(finalDelta)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                    ) {
                        Text("Apply Adjustment")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStockPreset(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = Slate100
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
private fun AddEditProductDialog(
    product: ProductItem?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        category: String,
        sku: String,
        sellingPrice: Double,
        costPrice: Double,
        stockQuantity: Int,
        reorderLevel: Int,
        gstRate: Double,
        unit: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Beverages") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var sellingPriceInput by remember { mutableStateOf(if (product != null) product.sellingPrice.toString() else "") }
    var costPriceInput by remember { mutableStateOf(if (product != null) product.costPrice.toString() else "") }
    var stockQuantityInput by remember { mutableStateOf(if (product != null) product.stockQuantity.toString() else "20") }
    var reorderLevelInput by remember { mutableStateOf(if (product != null) product.reorderLevel.toString() else "5") }
    var gstRate by remember { mutableStateOf(product?.gstRate ?: 5.0) }
    var unit by remember { mutableStateOf(product?.unit ?: "pcs") }

    val standardGstSlabs = listOf(0.0, 5.0, 12.0, 18.0, 28.0)
    val standardUnits = listOf("pcs", "kg", "pack", "btl", "box", "can")
    val defaultCategories = listOf(
        "Soups",
        "Appetizers & Chaap",
        "Tandoor & Kebabs",
        "Chinese",
        "Noodles",
        "Main Course (Veg & Dal)",
        "Main Course (Non-Veg)",
        "Indian Bread",
        "Rice & Biryani",
        "Special Mocktails",
        "Beverages & Shakes",
        "Desserts"
    )

    Dialog(onDismissRequest = onDismiss) {
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
                Text(
                    text = if (product == null) "Add Menu Item / Product" else "Edit Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category & SKU
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("product_category_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Barcode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_sku_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Quick category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (c in defaultCategories) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { category = c },
                            color = if (category == c) Slate900 else Slate100
                        ) {
                            Text(
                                text = c,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (category == c) Color.White else Slate700,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Prices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sellingPriceInput,
                        onValueChange = { sellingPriceInput = it },
                        label = { Text("Selling Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_selling_price_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = costPriceInput,
                        onValueChange = { costPriceInput = it },
                        label = { Text("Cost Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_cost_price_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stock & Reorder Threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockQuantityInput,
                        onValueChange = { stockQuantityInput = it },
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_stock_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = reorderLevelInput,
                        onValueChange = { reorderLevelInput = it },
                        label = { Text("Low Stock Alert At") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GST Slab Selector
                Text("GST Tax Slab Rate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (slab in standardGstSlabs) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { gstRate = slab },
                            color = if (gstRate == slab) Slate900 else Slate100
                        ) {
                            Text(
                                text = "${slab.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (gstRate == slab) Color.White else Slate800,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Unit selection
                Text("Unit of Measurement", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (u in standardUnits) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { unit = u },
                            color = if (unit == u) Slate900 else Slate100
                        ) {
                            Text(
                                text = u,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (unit == u) Color.White else Slate800,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sPrice = sellingPriceInput.toDoubleOrNull() ?: 0.0
                            val cPrice = costPriceInput.toDoubleOrNull() ?: 0.0
                            val stock = stockQuantityInput.toIntOrNull() ?: 0
                            val reorder = reorderLevelInput.toIntOrNull() ?: 5
                            if (name.isNotBlank() && sPrice > 0) {
                                onSave(
                                    product?.id ?: 0L,
                                    name,
                                    category,
                                    sku,
                                    sPrice,
                                    cPrice,
                                    stock,
                                    reorder,
                                    gstRate,
                                    unit
                                )
                            }
                        },
                        enabled = name.isNotBlank() && (sellingPriceInput.toDoubleOrNull() ?: 0.0) > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Text("Save Product")
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockNotificationCenterDialog(
    lowStockProducts: List<ProductItem>,
    onDismiss: () -> Unit,
    onQuickRestock: (Long, Int) -> Unit,
    onAdjustThreshold: (ProductItem) -> Unit
) {
    val context = LocalContext.current
    var selectedFilterTab by remember { mutableStateOf(0) } // 0: Raw Ingredients, 1: All Items

    val rawIngredients = remember(lowStockProducts) {
        lowStockProducts.filter { it.isIngredient }
    }

    val displayedItems = when (selectedFilterTab) {
        0 -> if (rawIngredients.isNotEmpty()) rawIngredients else lowStockProducts
        else -> lowStockProducts
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Alert Bell
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Rose100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = "Alerts",
                                tint = Rose500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Low-Stock Alert Center",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "70mm Lounge • Inventory Thresholds",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Tabs: Raw Ingredients vs All Low Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = (selectedFilterTab == 0),
                        onClick = { selectedFilterTab = 0 },
                        label = { Text("Raw Ingredients (${rawIngredients.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (rawIngredients.isNotEmpty()) Rose500 else Slate900,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    FilterChip(
                        selected = (selectedFilterTab == 1),
                        onClick = { selectedFilterTab = 1 },
                        label = { Text("All Items (${lowStockProducts.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate900,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (displayedItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Emerald600, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All stock levels are above threshold!", style = MaterialTheme.typography.bodyMedium, color = Slate600)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (item in displayedItems) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (item.isOutOfStock) Rose500 else Amber500,
                                        RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate900
                                                )
                                                if (item.isIngredient) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                                                        color = Color(0xFFEDE9FE)
                                                    ) {
                                                        Text(
                                                            text = "RAW",
                                                            color = Color(0xFF6D28D9),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 9.sp,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${item.category} • SKU: ${item.sku.ifBlank { "N/A" }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate500,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Surface(
                                            modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                            color = if (item.isOutOfStock) Rose100 else Amber100
                                        ) {
                                            Text(
                                                text = if (item.isOutOfStock) "0 ${item.unit} (EMPTY)" else "${item.stockQuantity} / Min ${item.reorderLevel} ${item.unit}",
                                                color = if (item.isOutOfStock) Rose500 else Amber500,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Deficit shortage banner
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Deficit: Need +${item.deficitQuantity} ${item.unit} to meet threshold",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isOutOfStock) Rose500 else Amber500,
                                            fontSize = 11.sp
                                        )

                                        TextButton(
                                            onClick = { onAdjustThreshold(item) },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("Set Threshold", fontSize = 11.sp, color = Sky600, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick Restock Shortcuts
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onQuickRestock(item.id, 5) },
                                            modifier = Modifier.weight(1f).height(30.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("+5 ${item.unit}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { onQuickRestock(item.id, 10) },
                                            modifier = Modifier.weight(1f).height(30.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("+10 ${item.unit}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { onQuickRestock(item.id, 25) },
                                            modifier = Modifier.weight(1f).height(30.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("+25 ${item.unit}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { onQuickRestock(item.id, item.deficitQuantity.coerceAtLeast(1)) },
                                            modifier = Modifier.weight(1.3f).height(30.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Fill Deficit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Send Purchase Order to Supplier via WhatsApp / Email
                Button(
                    onClick = {
                        val itemsText = displayedItems.joinToString("\n") {
                            "- ${it.name}: Need +${it.deficitQuantity} ${it.unit} (Current: ${it.stockQuantity} ${it.unit}, Min: ${it.reorderLevel} ${it.unit})"
                        }
                        val poContent = """
                            ========================================
                            70MM LOUNGE - URGENT RESTOCK ORDER
                            ========================================
                            Manager Phone : 8987477773
                            Admin Email   : 70mmlounge8bokaro@gmail.com
                            Venue         : 70mm Lounge, Bokaro
                            
                            Please supply the following urgent raw ingredients / inventory:
                            $itemsText
                            
                            Kindly confirm availability and delivery schedule as soon as possible.
                            ========================================
                        """.trimIndent()

                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "70mm Lounge - Urgent Restock Purchase Order")
                            putExtra(Intent.EXTRA_TEXT, poContent)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Send Supplier Purchase Order"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Purchase Order to Supplier")
                }
            }
        }
    }
}

@Composable
fun AdjustThresholdDialog(
    product: ProductItem,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var thresholdInput by remember { mutableStateOf(product.reorderLevel.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Set Low-Stock Alert Threshold",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "${product.name} (${if (product.isIngredient) "Raw Ingredient" else product.category})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "When stock falls at or below this threshold, the admin will be alerted immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = thresholdInput,
                    onValueChange = { thresholdInput = it },
                    label = { Text("Minimum Threshold (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(3, 5, 10, 15, 20)
                    for (p in presets) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { thresholdInput = p.toString() },
                            color = if (thresholdInput == p.toString()) Slate900 else Slate100
                        ) {
                            Text(
                                text = "$p",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (thresholdInput == p.toString()) Color.White else Slate800,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newThreshold = thresholdInput.toIntOrNull() ?: product.reorderLevel
                            onSave(newThreshold)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                    ) {
                        Text("Save Threshold")
                    }
                }
            }
        }
    }
}
