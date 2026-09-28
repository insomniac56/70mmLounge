package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RestaurantTable
import com.example.ui.components.PcSoftwareDialog
import com.example.ui.components.PrinterSettingsDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.CustomerOrderScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.KitchenScreen
import com.example.ui.screens.OrdersHistoryScreen
import com.example.ui.screens.PosCheckoutScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.TablesScreen
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.RetailPosTheme
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.PosViewModel
import com.example.util.CustomerHttpServer

enum class PosNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    POS("POS", Icons.Default.PointOfSale, "nav_pos"),
    TABLES("Tables", Icons.Default.TableBar, "nav_tables"),
    KITCHEN("Kitchen", Icons.Default.SoupKitchen, "nav_kitchen"),
    REPORTS("Reports", Icons.Default.Assessment, "nav_reports"),
    INVOICES("Invoices", Icons.AutoMirrored.Filled.ReceiptLong, "nav_invoices")
}

class MainActivity : ComponentActivity() {

    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)
        CustomerHttpServer.start(this, viewModel.repository)
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            RetailPosTheme(darkTheme = isDarkMode) {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        val tableParam = uri.getQueryParameter("table")
            ?: uri.lastPathSegment?.takeIf { it != "order" && it != "table" }
        val zoneParam = uri.getQueryParameter("zone") ?: "Club area"
        if (!tableParam.isNullOrBlank()) {
            viewModel.openCustomerOrderingForTableNumber(tableParam, zoneParam)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: PosViewModel) {
    var currentScreen by remember { mutableStateOf(PosNavDestination.POS) }
    var isInventoryOpen by remember { mutableStateOf(false) }
    var showPcDialog by remember { mutableStateOf(false) }
    var showPrinterDialog by remember { mutableStateOf(false) }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val activeKots by viewModel.activeKots.collectAsStateWithLifecycle()
    val lastCompletedOrder by viewModel.lastCompletedOrder.collectAsStateWithLifecycle()
    val activeCustomerTable by viewModel.activeCustomerTable.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val lowStockRawIngredients by viewModel.lowStockRawIngredients.collectAsStateWithLifecycle()

    val cartCount = cartItems.sumOf { it.quantity }
    val prepKotsCount = activeKots.count { it.status == "NEW" || it.status == "PREPARING" }
    val lowStockCount = lowStockProducts.size

    // If customer order mode is open for a table, display CustomerOrderScreen
    val currentCustomerTable = activeCustomerTable
    if (currentCustomerTable != null) {
        BackHandler {
            viewModel.exitCustomerOrdering()
        }
        CustomerOrderScreen(
            viewModel = viewModel,
            table = currentCustomerTable,
            onBack = { viewModel.exitCustomerOrdering() }
        )
        return
    }

    // BackHandler to return to POS checkout if on secondary tab or inventory
    BackHandler(enabled = isInventoryOpen || currentScreen != PosNavDestination.POS) {
        if (isInventoryOpen) {
            isInventoryOpen = false
        } else {
            currentScreen = PosNavDestination.POS
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_70mm),
                            contentDescription = "70mm Lounge Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, Emerald600, CircleShape),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "70mm Lounge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Restaurant & Club • Bokaro",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Low-Stock Notification Bell Alert
                    if (lowStockCount > 0) {
                        IconButton(
                            onClick = {
                                isInventoryOpen = true
                                viewModel.openLowStockAlertSheet()
                            },
                            modifier = Modifier.testTag("low_stock_notification_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = if (lowStockRawIngredients.isNotEmpty()) Color(0xFFEF4444) else Color(0xFFF59E0B),
                                        contentColor = Color.White
                                    ) {
                                        Text("$lowStockCount")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Low Stock Alerts",
                                    tint = if (lowStockRawIngredients.isNotEmpty()) Color(0xFFEF4444) else Color(0xFFF59E0B)
                                )
                            }
                        }
                    }

                    // Day / Dark Mode Toggle Button
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("toggle_dark_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to Day Mode" else "Switch to Dark Mode",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else Slate800
                        )
                    }

                    // Printer Configuration (Thermal, BT, WiFi, USB, LAN)
                    IconButton(
                        onClick = { showPrinterDialog = true },
                        modifier = Modifier.testTag("printer_settings_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Printer Settings",
                            tint = Emerald600
                        )
                    }

                    // PC / Desktop Software Download button
                    IconButton(
                        onClick = { showPcDialog = true },
                        modifier = Modifier.testTag("pc_software_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DesktopWindows,
                            contentDescription = "Install on PC",
                            tint = Sky600
                        )
                    }

                    // Inventory & Menu management button
                    IconButton(
                        onClick = { isInventoryOpen = !isInventoryOpen },
                        modifier = Modifier.testTag("inventory_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Inventory",
                            tint = if (isInventoryOpen) Emerald600 else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                PosNavDestination.values().forEach { destination ->
                    val isSelected = (!isInventoryOpen && currentScreen == destination)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            isInventoryOpen = false
                            currentScreen = destination
                        },
                        icon = {
                            when (destination) {
                                PosNavDestination.POS -> {
                                    if (cartCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = Emerald600, contentColor = Color.White) {
                                                    Text("$cartCount")
                                                }
                                            }
                                        ) {
                                            Icon(destination.icon, contentDescription = destination.title, modifier = Modifier.size(24.dp))
                                        }
                                    } else {
                                        Icon(destination.icon, contentDescription = destination.title, modifier = Modifier.size(24.dp))
                                    }
                                }
                                PosNavDestination.KITCHEN -> {
                                    if (prepKotsCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = Sky600, contentColor = Color.White) {
                                                    Text("$prepKotsCount")
                                                }
                                            }
                                        ) {
                                            Icon(destination.icon, contentDescription = destination.title, modifier = Modifier.size(24.dp))
                                        }
                                    } else {
                                        Icon(destination.icon, contentDescription = destination.title, modifier = Modifier.size(24.dp))
                                    }
                                }
                                else -> {
                                    Icon(destination.icon, contentDescription = destination.title, modifier = Modifier.size(24.dp))
                                }
                            }
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = Slate900,
                            unselectedIconColor = Slate500,
                            unselectedTextColor = Slate500
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isInventoryOpen) {
                InventoryScreen(viewModel = viewModel)
            } else {
                when (currentScreen) {
                    PosNavDestination.POS -> {
                        PosCheckoutScreen(viewModel = viewModel)
                    }
                    PosNavDestination.TABLES -> {
                        TablesScreen(
                            viewModel = viewModel,
                            onNavigateToCustomerOrder = { table ->
                                viewModel.startCustomerTableOrder(table)
                            },
                            onNavigateToCheckout = { table ->
                                currentScreen = PosNavDestination.POS
                            }
                        )
                    }
                    PosNavDestination.KITCHEN -> {
                        KitchenScreen(viewModel = viewModel)
                    }
                    PosNavDestination.REPORTS -> {
                        ReportsScreen(viewModel = viewModel)
                    }
                    PosNavDestination.INVOICES -> {
                        OrdersHistoryScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Modal Thermal Receipt Dialog (when an order is completed or inspected)
    lastCompletedOrder?.let { orderWithItems ->
        ReceiptDialog(
            orderWithItems = orderWithItems,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // PC Software / Admin Desktop Installation Instructions Dialog
    if (showPcDialog) {
        PcSoftwareDialog(onDismiss = { showPcDialog = false })
    }

    // Printer Configuration Dialog (Thermal, Bluetooth, WiFi, USB, LAN)
    if (showPrinterDialog) {
        PrinterSettingsDialog(onDismiss = { showPrinterDialog = false })
    }
}
