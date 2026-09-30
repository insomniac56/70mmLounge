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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import com.example.ui.components.CloudSyncDialog
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
import com.example.ui.theme.Amber500
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
        com.example.util.CloudOrderRelay.start(this, viewModel.repository)
        com.example.util.PrinterManager.init(this)
        com.example.util.PrinterDiscoveryManager.startService(this)
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
    var showCloudSyncDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val activeKots by viewModel.activeKots.collectAsStateWithLifecycle()
    val allTables by viewModel.allTables.collectAsStateWithLifecycle()
    val lastCompletedOrder by viewModel.lastCompletedOrder.collectAsStateWithLifecycle()
    val activeCustomerTable by viewModel.activeCustomerTable.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val lowStockRawIngredients by viewModel.lowStockRawIngredients.collectAsStateWithLifecycle()
    val salesSummary by viewModel.currentSalesSummary.collectAsStateWithLifecycle()

    val cartCount = cartItems.sumOf { it.quantity }
    val prepKotsCount = activeKots.count { it.status == "NEW" || it.status == "PREPARING" }
    val activeTablesCount = allTables.count { it.isOccupied || it.status == "CHECKOUT_REQUESTED" || it.currentBillAmount > 0 }
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

    // BackHandler to handle drawer or return to POS checkout if on secondary tab or inventory
    BackHandler(enabled = drawerState.isOpen || isInventoryOpen || currentScreen != PosNavDestination.POS) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (isInventoryOpen) {
            isInventoryOpen = false
        } else {
            currentScreen = PosNavDestination.POS
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(max = 330.dp)
            ) {
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_70mm),
                            contentDescription = "70mm Lounge Logo",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Emerald600, CircleShape),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.close() } }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Slider", tint = Slate400)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "70mm Lounge",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Restaurant & Club • Bokaro Steel City",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Emerald600.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Emerald600)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Multi-Terminal Active • Live Sync",
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List-wise Menu Items (The 3 icons from top bar + Cloud Real-time live monitor)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Print, contentDescription = null, tint = Emerald600) },
                    label = {
                        Column {
                            Text("Printer Settings", fontWeight = FontWeight.Bold)
                            Text("Thermal 58/80mm, Bluetooth, USB, LAN", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        showPrinterDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DesktopWindows, contentDescription = null, tint = Sky600) },
                    label = {
                        Column {
                            Text("Install on PC / Windows", fontWeight = FontWeight.Bold)
                            Text("Desktop POS App & LAN multi-terminal", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        showPcDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = Amber500) },
                    label = {
                        Column {
                            Text("Inventory & Menu Management", fontWeight = FontWeight.Bold)
                            Text("Dishes, stock levels & ingredient alerts", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        }
                    },
                    selected = isInventoryOpen,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        isInventoryOpen = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = Sky600) },
                    label = {
                        Column {
                            Text("Cloud Real-Time Sync & Live Sales", fontWeight = FontWeight.Bold)
                            Text("Travel anywhere and monitor live sales on mobile", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        }
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        showCloudSyncDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    coroutineScope.launch { drawerState.open() }
                                }
                                .padding(end = 4.dp)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "70mm Lounge",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "Open Sidebar Slider",
                                        tint = Slate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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

                        // Day / Dark Mode Toggle Button (Kept alone in top bar as requested)
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
                                PosNavDestination.TABLES -> {
                                    if (activeTablesCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = Amber500, contentColor = Color.White) {
                                                    Text("$activeTablesCount")
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

    // Cloud Real-Time Live Sales & Multi-Terminal Remote Monitor Dialog
    if (showCloudSyncDialog) {
        CloudSyncDialog(
            todaySales = salesSummary.grossSales,
            activeRunningTablesCount = activeTablesCount,
            lowStockCount = lowStockCount,
            onDismiss = { showCloudSyncDialog = false }
        )
    }
}
