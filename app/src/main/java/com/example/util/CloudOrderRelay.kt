package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.CartItem
import com.example.data.model.ProductItem
import com.example.data.repository.PosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Cloud Real-Time Bridge for 70mm Lounge.
 * Enables customers to place orders, request checkout, view dynamic UPI bills,
 * and complete payments seamlessly from their phone browsers WITHOUT needing to be
 * connected to the same local Wi-Fi network (works over cellular 4G/5G, hotspots, or any network).
 */
object CloudOrderRelay {

    private const val TAG = "CloudOrderRelay"
    const val CLOUD_ORDERS_TOPIC = "lounge70mm_bokaro_orders"
    private const val RELAY_BASE_URL = "https://ntfy.sh"

    private var isRunning = false
    private val processedIds = mutableSetOf<String>()
    private var repository: PosRepository? = null

    init {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true")
            System.setProperty("java.net.preferIPv6Addresses", "false")
        } catch (_: Exception) {}
    }

    /**
     * Resilient OkHttpClient configured with IPv4 priority to prevent IPv6 routing failures
     * in container / mobile environments without IPv6 routes.
     */
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    return try {
                        val addresses = Dns.SYSTEM.lookup(hostname)
                        val ipv4 = addresses.filterIsInstance<Inet4Address>()
                        if (ipv4.isNotEmpty()) ipv4 else addresses
                    } catch (_: Exception) {
                        Dns.SYSTEM.lookup(hostname)
                    }
                }
            })
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    // Bill requested alert event for POS Screen
    data class BillRequestAlert(
        val tableNumber: String,
        val customerName: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _billRequestAlertFlow = MutableSharedFlow<BillRequestAlert>(extraBufferCapacity = 10)
    val billRequestAlertFlow: SharedFlow<BillRequestAlert> = _billRequestAlertFlow.asSharedFlow()

    // Payment confirmation event for POS Screen (when customer pays via UPI QR on mobile)
    data class CustomerPaymentEvent(
        val tableNumber: String,
        val amount: Double,
        val paymentMethod: String = "UPI",
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _paymentConfirmedFlow = MutableSharedFlow<CustomerPaymentEvent>(extraBufferCapacity = 10)
    val paymentConfirmedFlow: SharedFlow<CustomerPaymentEvent> = _paymentConfirmedFlow.asSharedFlow()

    fun start(context: Context, repo: PosRepository) {
        if (isRunning) return
        isRunning = true
        repository = repo

        Log.d(TAG, "Starting Cloud Order Relay for topic: $CLOUD_ORDERS_TOPIC")
        CoroutineScope(Dispatchers.IO).launch {
            var lastSinceTimestamp = (System.currentTimeMillis() / 1000) - 30 // Listen from 30s ago
            var backoffDelayMs = 2000L

            while (isActive && isRunning) {
                try {
                    pollCloudOrders(lastSinceTimestamp) { newTimestamp ->
                        if (newTimestamp > lastSinceTimestamp) {
                            lastSinceTimestamp = newTimestamp
                        }
                    }
                    backoffDelayMs = 2000L // Reset backoff on successful response
                } catch (e: Exception) {
                    // Log at debug level to avoid noisy fatal alerts during offline / no-internet periods
                    Log.d(TAG, "Cloud relay temporary network status: ${e.message}")
                    backoffDelayMs = (backoffDelayMs * 2).coerceAtMost(30000L)
                }
                delay(backoffDelayMs)
            }
        }
    }

    fun stop() {
        isRunning = false
    }

    private suspend fun pollCloudOrders(sinceSeconds: Long, onTimestampUpdate: (Long) -> Unit) {
        val request = Request.Builder()
            .url("$RELAY_BASE_URL/$CLOUD_ORDERS_TOPIC/json?poll=1&since=${sinceSeconds}s")
            .header("Accept", "application/x-ndjson, application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.source()?.let { source ->
                    val reader = BufferedReader(InputStreamReader(source.inputStream(), StandardCharsets.UTF_8))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line?.trim() ?: continue
                        if (currentLine.isEmpty() || !currentLine.startsWith("{")) continue
                        try {
                            val msgObj = JSONObject(currentLine)
                            val eventType = msgObj.optString("event", "message")
                            if (eventType != "message") continue

                            val msgId = msgObj.optString("id", "")
                            val msgTime = msgObj.optLong("time", 0L)
                            if (msgTime > 0) onTimestampUpdate(msgTime)

                            if (msgId.isNotBlank()) {
                                if (processedIds.contains(msgId)) continue
                                processedIds.add(msgId)
                                if (processedIds.size > 500) {
                                    processedIds.clear()
                                    processedIds.add(msgId)
                                }
                            }

                            val rawMessage = msgObj.optString("message", "")
                            if (rawMessage.startsWith("{")) {
                                processIncomingCloudMessage(JSONObject(rawMessage))
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse ndjson line: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private suspend fun processIncomingCloudMessage(json: JSONObject) {
        val repo = repository ?: return
        val type = json.optString("type", "")

        when (type) {
            "CUSTOMER_ORDER" -> {
                val tableNum = json.optString("tableNumber", "").trim()
                val zone = json.optString("zone", "Dine-in").trim()
                val customerName = json.optString("customerName", "Guest").trim()
                val customerPhone = json.optString("customerPhone", "").trim()
                val notes = json.optString("specialNotes", "").trim()
                val itemsArray = json.optJSONArray("items") ?: JSONArray()

                val allProducts = repo.allProducts.firstOrNull() ?: emptyList()
                val productMap = allProducts.associateBy { it.id }

                val cartItems = mutableListOf<CartItem>()
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    val prodId = itemObj.optLong("productId", -1L)
                    val qty = itemObj.optInt("quantity", 1)
                    val prod = productMap[prodId]
                    if (prod != null && qty > 0) {
                        cartItems.add(CartItem(product = prod, quantity = qty))
                    }
                }

                if (tableNum.isNotBlank() && cartItems.isNotEmpty()) {
                    repo.submitCustomerTableOrder(
                        tableNumber = tableNum,
                        zone = zone,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        cartItems = cartItems,
                        specialNotes = notes
                    )
                    Log.d(TAG, "Cloud order submitted successfully for table $tableNum with ${cartItems.size} items")
                }
            }

            "CHECKOUT_REQUEST" -> {
                val tableNum = json.optString("tableNumber", "").trim()
                val customerName = json.optString("customerName", "Guest").trim()
                if (tableNum.isNotBlank()) {
                    repo.requestTableCheckout(tableNum)
                    _billRequestAlertFlow.emit(BillRequestAlert(tableNumber = tableNum, customerName = customerName))
                    Log.d(TAG, "Checkout request received for table $tableNum")
                }
            }

            "PAYMENT_CONFIRMED" -> {
                val tableNum = json.optString("tableNumber", "").trim()
                val amount = json.optDouble("amount", 0.0)
                val method = json.optString("paymentMethod", "UPI").trim()
                if (tableNum.isNotBlank()) {
                    _paymentConfirmedFlow.emit(CustomerPaymentEvent(tableNumber = tableNum, amount = amount, paymentMethod = method))
                    Log.d(TAG, "Customer payment confirmed for table $tableNum (₹$amount)")
                }
            }
        }
    }

    /**
     * Publishes official bill details to customer's mobile phone via cloud relay.
     * Table customer's mobile browser instantly catches this and shows the Bill & UPI QR.
     */
    fun publishBillToCustomer(
        tableNumber: String,
        billPayload: JSONObject
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val topic = "lounge70mm_table_${tableNumber.trim().lowercase()}_bill"
                val request = Request.Builder()
                    .url("$RELAY_BASE_URL/$topic")
                    .post(billPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.d(TAG, "Published bill to topic $topic, response: ${response.code}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Failed to publish bill to cloud: ${e.message}")
            }
        }
    }

    /**
     * Publishes settlement / table cleared event to customer's mobile phone.
     * Shows greetings & rating dialog.
     */
    fun publishSettlementToCustomer(tableNumber: String) {
        val payload = JSONObject().apply {
            put("type", "BILL_SETTLED")
            put("tableNumber", tableNumber)
            put("timestamp", System.currentTimeMillis())
        }
        publishBillToCustomer(tableNumber, payload)
    }
}
