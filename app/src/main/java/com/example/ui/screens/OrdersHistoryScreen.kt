package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderWithItems
import com.example.ui.theme.Emerald600
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
fun OrdersHistoryScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val ordersWithItems by viewModel.allOrdersWithItems.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val filteredOrders = ordersWithItems.filter { item ->
        val query = searchQuery.trim()
        if (query.isBlank()) true
        else {
            item.order.invoiceNumber.contains(query, ignoreCase = true) ||
                    item.order.customerName.contains(query, ignoreCase = true) ||
                    item.order.customerPhone.contains(query, ignoreCase = true) ||
                    item.items.any { it.productName.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Invoices & Receipts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Text(
            text = "${ordersWithItems.size} total completed transactions",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("invoices_search_input"),
            placeholder = { Text("Search invoice #, customer or product...", style = MaterialTheme.typography.bodyMedium) },
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

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No transactions found", style = MaterialTheme.typography.titleMedium, color = Slate600)
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
                items(filteredOrders, key = { it.order.orderId }) { orderWithItems ->
                    OrderHistoryCard(
                        orderWithItems = orderWithItems,
                        onClick = { viewModel.viewOrderReceipt(orderWithItems) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(
    orderWithItems: OrderWithItems,
    onClick: () -> Unit
) {
    val order = orderWithItems.order
    val items = orderWithItems.items

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Slate200, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("invoice_card_${order.invoiceNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Invoice No + Date + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.invoiceNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                        color = Slate100
                    ) {
                        Text(
                            text = order.paymentMethod,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate700,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = CurrencyFormatter.format(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Emerald600
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Customer & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.customerName + if (order.customerPhone.isNotBlank()) " (${order.customerPhone})" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )
                Text(
                    text = CurrencyFormatter.formatDateTime(order.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Items Summary snippet
            val itemsSnippet = items.joinToString(", ") { "${it.quantity}x ${it.productName}" }
            Text(
                text = itemsSnippet,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // GST and Discount tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GST Tax: ${CurrencyFormatter.format(order.taxAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sky600,
                        fontWeight = FontWeight.Medium
                    )
                    if (order.discountAmount > 0) {
                        Text(
                            text = "Disc: -${CurrencyFormatter.format(order.discountAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "Tap to view receipt >",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        }
    }
}
