package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CartItem
import com.example.data.model.ProductItem
import com.example.data.model.RestaurantTable
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderScreen(
    viewModel: PosViewModel,
    table: RestaurantTable,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.activeProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.customerCartItems.collectAsStateWithLifecycle()
    val guestName by viewModel.customerGuestName.collectAsStateWithLifecycle()
    val guestPhone by viewModel.customerGuestPhone.collectAsStateWithLifecycle()
    val specialNotes by viewModel.customerSpecialNotes.collectAsStateWithLifecycle()
    val isOrderPlaced by viewModel.customerOrderPlacedSuccess.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isViewCartOpen by remember { mutableStateOf(false) }

    val cleanPhone = guestPhone.filter { it.isDigit() }
    val isNameValid = guestName.trim().isNotBlank()
    val isPhoneValid = cleanPhone.length == 10
    val canSubmitOrder = isNameValid && isPhoneValid && cartItems.isNotEmpty()

    val categories = listOf("All") + products.map { it.category }.distinct()

    val filteredProducts = products.filter { p ->
        val matchesCategory = (selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val totalCartCount = cartItems.sumOf { it.quantity }
    val cartGrandTotal = cartItems.sumOf { it.totalAmount }

    val allTables by viewModel.allTables.collectAsStateWithLifecycle()
    val allKots by viewModel.allKots.collectAsStateWithLifecycle()
    val liveTable = allTables.find { it.tableNumber.equals(table.tableNumber, ignoreCase = true) } ?: table
    val latestKot = allKots.firstOrNull { it.tableNumber.equals(table.tableNumber, ignoreCase = true) && it.status != "CANCELLED" }

    var hasPlacedOrderThisSession by remember { mutableStateOf(false) }
    var initialWasOccupied by remember { mutableStateOf(table.isOccupied) }

    LaunchedEffect(isOrderPlaced) {
        if (isOrderPlaced) {
            hasPlacedOrderThisSession = true
        }
    }

    val isBillingCompleted = (hasPlacedOrderThisSession || initialWasOccupied) && liveTable.isAvailable

    if (isBillingCompleted) {
        val context = LocalContext.current
        var rating by remember { mutableStateOf(5) }
        var selectedFeedbackTags by remember { mutableStateOf(setOf("Delicious Food 🍲", "Great Drinks 🍸")) }
        var feedbackComment by remember { mutableStateOf("") }
        var isFeedbackSubmitted by remember { mutableStateOf(false) }

        val feedbackOptions = listOf(
            "Delicious Food 🍲",
            "Great Drinks 🍸",
            "Awesome Vibe 🎶",
            "Fast Service ⚡",
            "Polite Staff 🌟",
            "Clean & Safe ✨"
        )

        // Confirmation & Feedback / Review Screen after Customer Order & Dining
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .border(1.2.dp, Emerald100, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Emerald50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Emerald600,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Thank You for Dining at 70MM Lounge! 🎉",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Table ${liveTable.tableNumber} • ${liveTable.zone} Bill Completed & Settled",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald600,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Your table bill has been processed at POS. We hope you had a fantastic time!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dining & Payment Cleared Status Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        color = Color(0xFFF0FDF4)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Guest: ${guestName.ifBlank { "Guest" }} ($guestPhone)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                    Text(
                                        text = "Payment Status: Settled & Cleared ✅",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "BILL PAID",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(18.dp))

                    // --- THOUGHTFUL THANK YOU MESSAGE NOTE ---
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.2.dp, Color(0xFFF5D4AF), RoundedCornerShape(16.dp)),
                        color = Color(0xFFFDF6ED) // Champagne Cream Tint
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💖 A Heartfelt Note from 70MM Lounge",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFC58A3E),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Dear ${guestName.ifBlank { "Valued Guest" }},",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E2628),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Thank you so much for dining with us today! We hope you loved our food, drinks, and ambiance. Your smiles and happiness are the reason our kitchen cooks and our bar creates.\n\n" +
                                        "Aapka dining experience kaisa raha? Hamare chef aur service team ke liye aapka ek chhota sa review behad keemti hai!",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF56676A),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // --- SERVICE QUALITY RATING (1 to 5 Stars) ---
                    Text(
                        text = "Rate Your Experience",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Interactive Star Rating
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (star in 1..5) {
                            IconButton(
                                onClick = { rating = star },
                                modifier = Modifier.size(46.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Rate $star star",
                                    tint = if (star <= rating) Color(0xFFF59E0B) else Slate400,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    val ratingText = when (rating) {
                        5 -> "⭐⭐⭐⭐⭐ Outstanding! Best lounge experience!"
                        4 -> "⭐⭐⭐⭐ Great food, drinks & wonderful vibe!"
                        3 -> "⭐⭐⭐ Good, we'll keep improving!"
                        2 -> "⭐⭐ Needs improvement, thanks for your feedback"
                        else -> "⭐ We apologize, we will do better next time!"
                    }

                    Text(
                        text = ratingText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC58A3E),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Service quality feedback tags
                    Text(
                        text = "What did you enjoy the most?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        feedbackOptions.forEach { tag ->
                            val isSelected = selectedFeedbackTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedFeedbackTags = if (isSelected) {
                                        selectedFeedbackTags - tag
                                    } else {
                                        selectedFeedbackTags + tag
                                    }
                                },
                                label = { Text(tag, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E2628),
                                    selectedLabelColor = Color(0xFFF5D4AF),
                                    containerColor = Slate100,
                                    labelColor = Slate700
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optional Feedback text box
                    OutlinedTextField(
                        value = feedbackComment,
                        onValueChange = { feedbackComment = it },
                        label = { Text("Any message for the Chef or Bartender? (Optional)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC58A3E),
                            unfocusedBorderColor = Slate200
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isFeedbackSubmitted) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)),
                            color = Color(0xFFF0FDF4)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Feedback saved! Thank you for helping 70MM Lounge shine! ❤️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        OutlinedButton(
                            onClick = {
                                isFeedbackSubmitted = true
                                Toast.makeText(context, "Thank you for your rating!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate400)
                        ) {
                            Text("Save Service Rating", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // --- PROMINENT "WRITE A REVIEW" GOOGLE BUTTON ---
                    // Directly opens https://g.page/r/CY2JZyrR9B6ZEAE/review
                    Button(
                        onClick = {
                            val reviewUrl = "https://g.page/r/CY2JZyrR9B6ZEAE/review"
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reviewUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening Google Review link...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("write_google_review_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B2324), // Signature Deep Gunmetal
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF5D4AF)) // Champagne Gold Border
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Review",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Write a Review on Google ⭐",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF5D4AF)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open Link",
                                tint = Color(0xFFF5D4AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Done / Return to POS / Menu
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate100),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("back_to_pos_button")
                    ) {
                        Text("Close / Back to Portal", color = Slate800, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    } else {
        val context = LocalContext.current
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header with back button and table name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "70MM LOUNGE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                                color = Emerald50
                            ) {
                                Text(
                                    text = "TABLE ${table.tableNumber}",
                                    color = Emerald600,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${table.zone} • Guest Self-Ordering Portal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }

                    // Quick Google Review button
                    IconButton(
                        onClick = {
                            val reviewUrl = "https://g.page/r/CY2JZyrR9B6ZEAE/review"
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reviewUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening Google Review...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("customer_quick_review_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Write a Review",
                            tint = Color(0xFFF59E0B)
                        )
                    }

                    // Cart Icon button
                    BadgedBox(
                        badge = {
                            if (totalCartCount > 0) {
                                Badge(
                                    containerColor = Emerald600,
                                    contentColor = Color.White
                                ) {
                                    Text("$totalCartCount")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { if (totalCartCount > 0) isViewCartOpen = true },
                            modifier = Modifier.testTag("customer_cart_button")
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = "Cart")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Live Table Status & Order Status Banner (Visible when order placed or table is occupied)
                if (liveTable.isOccupied || hasPlacedOrderThisSession) {
                    val isReady = latestKot?.status == "READY" || latestKot?.status == "SERVED"
                    val isReq = liveTable.isCheckoutRequested
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.5.dp,
                                if (isReq) Color(0xFFF59E0B) else if (isReady) Emerald600 else Color(0xFF0284C7),
                                RoundedCornerShape(12.dp)
                            ),
                        color = if (isReq) Color(0xFFFEF3C7).copy(alpha = 0.6f)
                                else if (isReady) Emerald50
                                else Color(0xFFE0F2FE).copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isReq) Color(0xFFF59E0B) else if (isReady) Emerald600 else Color(0xFF0284C7))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Table ${liveTable.tableNumber} • ${liveTable.zone}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Slate900
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isReq) Color(0xFFF59E0B) else if (isReady) Emerald600 else Color(0xFF0284C7)
                                ) {
                                    Text(
                                        text = if (isReq) "BILLING REQUESTED ⏳"
                                               else if (isReady) "ORDER READY / SERVED 🍽️"
                                               else "COOKING IN KITCHEN 👨‍🍳",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            val displayGuest = liveTable.currentGuestName.ifBlank { guestName.ifBlank { "Table Guest" } }
                            Text(
                                text = "Guest: $displayGuest • Running Bill: ₹${String.format("%.2f", liveTable.currentBillAmount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // "Checkout" Button as requested by user
                            if (!isReq) {
                                Button(
                                    onClick = {
                                        viewModel.requestTableCheckout(liveTable.tableNumber)
                                        Toast.makeText(context, "Checkout requested! POS cashier will bill Table ${liveTable.tableNumber}.", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("customer_request_checkout_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD97706),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Checkout", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🔔 Checkout Requested • Complete Order in POS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Guest Name & Mobile Number (Mandatory to send order)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { viewModel.setCustomerGuestName(it) },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("guest_name_input"),
                        label = { Text("Customer Name *") },
                        placeholder = { Text("Your Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Slate900,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    OutlinedTextField(
                        value = guestPhone,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(10)
                            viewModel.setCustomerGuestPhone(digits)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("guest_phone_input"),
                        label = { Text("Mobile No. *") },
                        placeholder = { Text("10 digits") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Slate900,
                            unfocusedBorderColor = Slate200
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("customer_search_input"),
                    placeholder = { Text("Search food, drinks or cocktails...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate900,
                        unfocusedBorderColor = Slate200
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
                        val count = if (cat == "All") products.size else products.count { it.category.equals(cat, ignoreCase = true) }
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
                        text = if (selectedCategory == "All") "All Dishes (${filteredProducts.size})" else "$selectedCategory • ${filteredProducts.size} Dishes",
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
                            Text("All", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Menu items list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val inCart = cartItems.find { it.product.id == product.id }
                        CustomerMenuItemCard(
                            product = product,
                            quantityInCart = inCart?.quantity ?: 0,
                            onAdd = { viewModel.addCustomerCartItem(product) },
                            onIncrement = { viewModel.updateCustomerCartQuantity(product.id, 1) },
                            onDecrement = { viewModel.updateCustomerCartQuantity(product.id, -1) }
                        )
                    }
                }
            }

            // Bottom Order Bar
            AnimatedVisibility(
                visible = totalCartCount > 0,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isViewCartOpen = true }
                        .testTag("customer_bottom_order_bar"),
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
                        Column {
                            Text(
                                text = "$totalCartCount items in order",
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

                        Button(
                            onClick = { isViewCartOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("review_and_place_order_button")
                        ) {
                            Text("Review & Send Order", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet to Review Order & Add Cooking Notes
    if (isViewCartOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { isViewCartOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Review Order for Table ${table.tableNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "This will immediately notify both the Kitchen and Admin POS.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Cart item list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (item in cartItems) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${CurrencyFormatter.format(item.product.sellingPrice)} each",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate100)
                            ) {
                                IconButton(
                                    onClick = { viewModel.updateCustomerCartQuantity(item.product.id, -1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.updateCustomerCartQuantity(item.product.id, 1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = CurrencyFormatter.format(item.totalAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(10.dp))

                // Special Instructions for Chef / Bartender
                OutlinedTextField(
                    value = specialNotes,
                    onValueChange = { viewModel.setCustomerSpecialNotes(it) },
                    modifier = Modifier.fillMaxWidth().testTag("cooking_instructions_input"),
                    label = { Text("Special Cooking / Bar Instructions") },
                    placeholder = { Text("e.g. Extra spicy, less ice, medium rare steak...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total with GST", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(CurrencyFormatter.format(cartGrandTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Emerald600)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name & Phone verification banner
                if (!canSubmitOrder) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFFFEF2F2)
                    ) {
                        Text(
                            text = if (!isNameValid && !isPhoneValid) "⚠️ Order send karne ke liye apna Name aur 10-digit Mobile Number enter karein."
                            else if (!isNameValid) "⚠️ Kripya apna Name enter karein."
                            else "⚠️ Kripya valid 10-digit Mobile Number enter karein.",
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Place Order Button
                Button(
                    onClick = {
                        if (canSubmitOrder) {
                            viewModel.submitCustomerOrder {
                                isViewCartOpen = false
                            }
                        }
                    },
                    enabled = canSubmitOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_customer_order_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald600,
                        disabledContainerColor = Slate200
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canSubmitOrder) "Send Order to Kitchen & Admin" else "Name & Mobile Required",
                        fontWeight = FontWeight.Bold,
                        color = if (canSubmitOrder) Color.White else Slate500
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerMenuItemCard(
    product: ProductItem,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Slate200, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500,
                        fontSize = 11.sp
                    )

                    // Kitchen/Bar section icon
                    val isBar = product.kitchenSection == "BAR"
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                        color = if (isBar) Sky100 else Slate100
                    ) {
                        Text(
                            text = product.kitchenSection,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBar) Sky600 else Slate700,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = "• GST ${product.gstRate.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(product.sellingPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Emerald600
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (quantityInCart > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald50)
                            .border(1.dp, Emerald100, RoundedCornerShape(8.dp))
                    ) {
                        IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus", tint = Emerald600, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "$quantityInCart",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Plus", tint = Emerald600, modifier = Modifier.size(14.dp))
                        }
                    }
                } else {
                    Button(
                        onClick = onAdd,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("+ Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
