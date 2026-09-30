package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.CartItem
import com.example.data.model.ProductItem
import com.example.data.repository.PosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Embedded Lightweight Web Server running inside the 70mm Lounge Android App.
 * Allows any customer on the local Wi-Fi / Hotspot to scan the table QR code with their
 * standard phone camera and instantly access the full Customer Self-Ordering Menu in Chrome/Safari.
 */
object CustomerHttpServer {

    private const val TAG = "CustomerHttpServer"
    private const val DEFAULT_PORT = 8080
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var repository: PosRepository? = null
    private var appContext: Context? = null

    private val activeBills = java.util.concurrent.ConcurrentHashMap<String, JSONObject>()

    fun setTableBill(tableNumber: String, bill: JSONObject) {
        activeBills[tableNumber.trim().uppercase()] = bill
    }

    fun clearTableBill(tableNumber: String) {
        activeBills.remove(tableNumber.trim().uppercase())
    }

    fun getTableBill(tableNumber: String): JSONObject? {
        return activeBills[tableNumber.trim().uppercase()]
    }

    fun start(context: Context, repo: PosRepository, port: Int = DEFAULT_PORT) {
        if (isRunning) return
        repository = repo
        appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var currentPort = port
                while (currentPort < port + 10) {
                    try {
                        serverSocket = ServerSocket(currentPort)
                        break
                    } catch (e: Exception) {
                        currentPort++
                    }
                }
                val boundPort = serverSocket?.localPort ?: return@launch
                isRunning = true
                Log.d(TAG, "Customer Web Server started successfully on port $boundPort")

                while (isRunning && !serverSocket!!.isClosed) {
                    try {
                        val client = serverSocket!!.accept()
                        launch(Dispatchers.IO) {
                            handleClient(client)
                        }
                    } catch (e: Exception) {
                        if (!isRunning) break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize embedded web server", e)
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
    }

    fun getPort(): Int {
        return serverSocket?.localPort ?: DEFAULT_PORT
    }

    fun getLocalIpAddress(): String {
        return TableUrlGenerator.getDeviceIpAddress()
    }

    private suspend fun handleClient(socket: Socket) {
        socket.use { s ->
            try {
                val input = BufferedReader(InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))
                val output = s.getOutputStream()

                val requestLine = input.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return

                val method = parts[0].uppercase()
                val fullPath = parts[1]

                // Read headers
                val headers = mutableMapOf<String, String>()
                var contentLength = 0
                while (true) {
                    val line = input.readLine() ?: break
                    if (line.isEmpty()) break
                    val headerParts = line.split(":", limit = 2)
                    if (headerParts.size == 2) {
                        val name = headerParts[0].trim().lowercase()
                        val value = headerParts[1].trim()
                        headers[name] = value
                        if (name == "content-length") {
                            contentLength = value.toIntOrNull() ?: 0
                        }
                    }
                }

                // Read body if POST
                val body = if (contentLength > 0) {
                    val buffer = CharArray(contentLength)
                    var readTotal = 0
                    while (readTotal < contentLength) {
                        val r = input.read(buffer, readTotal, contentLength - readTotal)
                        if (r == -1) break
                        readTotal += r
                    }
                    String(buffer, 0, readTotal)
                } else ""

                // Route handling
                val uriParts = fullPath.split("?", limit = 2)
                val path = uriParts[0]
                val queryParams = parseQueryParams(if (uriParts.size > 1) uriParts[1] else "")

                when {
                    method == "OPTIONS" -> {
                        sendResponse(output, 200, "text/plain", "OK", allowCors = true)
                    }
                    path == "/" || path == "/order" || path == "/pos-menu.html" || path == "/menu.html" -> {
                        val table = queryParams["table"] ?: "C-1"
                        val zone = queryParams["zone"] ?: "Club area"
                        val html = generateCustomerMenuHtml(table, zone)
                        sendResponse(output, 200, "text/html; charset=UTF-8", html, allowCors = true)
                    }
                    path == "/api/menu" -> {
                        val products = repository?.allProducts?.firstOrNull() ?: emptyList()
                        val json = JSONArray()
                        for (p in products) {
                            if (p.stockQuantity > 0 && !p.isIngredient) {
                                val obj = JSONObject()
                                obj.put("id", p.id)
                                obj.put("name", p.name)
                                obj.put("category", p.category)
                                obj.put("price", p.sellingPrice)
                                obj.put("stock", p.stockQuantity)
                                obj.put("section", p.kitchenSection)
                                json.put(obj)
                            }
                        }
                        sendResponse(output, 200, "application/json", json.toString(), allowCors = true)
                    }
                    path == "/api/table-status" -> {
                        val tableNum = queryParams["table"] ?: ""
                        val table = repository?.allTables?.firstOrNull()?.find { it.tableNumber.equals(tableNum, ignoreCase = true) }
                        val kots = repository?.allKots?.firstOrNull() ?: emptyList()
                        val latestKot = kots.firstOrNull { it.tableNumber.equals(tableNum, ignoreCase = true) }
                        val activeBill = getTableBill(tableNum)

                        val obj = JSONObject()
                        obj.put("tableNumber", tableNum)
                        obj.put("status", table?.status ?: "AVAILABLE")
                        obj.put("guestName", table?.currentGuestName ?: "")
                        obj.put("billAmount", table?.currentBillAmount ?: 0.0)
                        obj.put("isCheckoutRequested", table?.isCheckoutRequested ?: false)
                        obj.put("kotStatus", latestKot?.status ?: "NONE")
                        if (activeBill != null) {
                            obj.put("bill", activeBill)
                        }
                        sendResponse(output, 200, "application/json", obj.toString(), allowCors = true)
                    }
                    path == "/api/bill" -> {
                        val tableNum = queryParams["table"] ?: ""
                        val activeBill = getTableBill(tableNum)
                        if (activeBill != null) {
                            sendResponse(output, 200, "application/json", activeBill.toString(), allowCors = true)
                        } else {
                            sendResponse(output, 404, "application/json", "{\"error\":\"No active bill\"}", allowCors = true)
                        }
                    }
                    path == "/api/confirm-payment" && method == "POST" -> {
                        try {
                            val json = if (body.isNotBlank() && body.trim().startsWith("{")) JSONObject(body) else JSONObject()
                            val tableNum = json.optString("tableNumber", queryParams["table"] ?: "")
                            val amount = json.optDouble("amount", 0.0)
                            if (tableNum.isNotBlank()) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    CloudOrderRelay.publishBillToCustomer(tableNum, JSONObject().apply {
                                        put("type", "PAYMENT_CONFIRMED")
                                        put("tableNumber", tableNum)
                                        put("amount", amount)
                                    })
                                }
                            }
                            sendResponse(output, 200, "application/json", "{\"success\":true,\"message\":\"Payment registered\"}", allowCors = true)
                        } catch (e: Exception) {
                            sendResponse(output, 400, "application/json", "{\"error\":\"Invalid request\"}", allowCors = true)
                        }
                    }
                    path == "/api/order" && method == "POST" -> {
                        handleOrderPost(body, output)
                    }
                    path == "/api/request-checkout" && method == "POST" -> {
                        handleCheckoutRequestPost(body, queryParams, output)
                    }
                    path == "/api/feedback" && method == "POST" -> {
                        sendResponse(output, 200, "application/json", "{\"success\":true}", allowCors = true)
                    }
                    else -> {
                        sendResponse(output, 404, "text/plain", "Not Found", allowCors = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling client request", e)
            }
        }
    }

    private suspend fun handleOrderPost(body: String, output: OutputStream) {
        val repo = repository
        if (repo == null) {
            sendResponse(output, 500, "application/json", "{\"error\":\"Server unready\"}", allowCors = true)
            return
        }
        try {
            val json = JSONObject(body)
            val tableNum = json.optString("tableNumber", "T1").trim()
            val zone = json.optString("zone", "Club area").trim()
            val customerName = json.optString("customerName", "Guest").trim()
            val customerPhone = json.optString("customerPhone", "").trim()
            val notes = json.optString("notes", "").trim()
            val itemsArray = json.optJSONArray("items") ?: JSONArray()

            val products = repo.allProducts.firstOrNull() ?: emptyList()
            val cartItems = mutableListOf<CartItem>()

            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                val prodId = itemObj.optLong("id", 0L)
                val qty = itemObj.optInt("quantity", 1)
                val name = itemObj.optString("name", "")
                val price = itemObj.optDouble("price", 0.0)

                val matchedProd = products.find { it.id == prodId }
                    ?: ProductItem(
                        id = prodId,
                        name = name.ifBlank { "Custom Dish" },
                        category = "Special",
                        sku = "WEB-$prodId",
                        sellingPrice = price,
                        costPrice = price * 0.4,
                        stockQuantity = 999
                    )

                cartItems.add(CartItem(product = matchedProd, quantity = qty))
            }

            if (cartItems.isNotEmpty()) {
                repo.submitCustomerTableOrder(
                    tableNumber = tableNum,
                    zone = zone,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    cartItems = cartItems,
                    specialNotes = notes
                )
                sendResponse(output, 200, "application/json", "{\"success\":true,\"message\":\"Order placed successfully\"}", allowCors = true)
            } else {
                sendResponse(output, 400, "application/json", "{\"error\":\"No items ordered\"}", allowCors = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse order JSON", e)
            sendResponse(output, 400, "application/json", "{\"error\":\"Invalid order request\"}", allowCors = true)
        }
    }

    private suspend fun handleCheckoutRequestPost(body: String, queryParams: Map<String, String>, output: OutputStream) {
        val repo = repository
        val tableNum = try {
            if (body.isNotBlank() && body.trim().startsWith("{")) {
                JSONObject(body).optString("tableNumber", "")
            } else {
                queryParams["table"] ?: ""
            }
        } catch (_: Exception) {
            queryParams["table"] ?: ""
        }

        if (tableNum.isNotBlank() && repo != null) {
            repo.requestTableCheckout(tableNum)
            sendResponse(output, 200, "application/json", "{\"success\":true,\"message\":\"Checkout requested\"}", allowCors = true)
        } else {
            sendResponse(output, 400, "application/json", "{\"error\":\"Missing table number\"}", allowCors = true)
        }
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (query.isBlank()) return map
        val pairs = query.split("&")
        for (p in pairs) {
            val kv = p.split("=", limit = 2)
            if (kv.isNotEmpty()) {
                val key = URLDecoder.decode(kv[0], "UTF-8")
                val value = if (kv.size > 1) URLDecoder.decode(kv[1], "UTF-8") else ""
                map[key] = value
            }
        }
        return map
    }

    private fun sendResponse(
        output: OutputStream,
        statusCode: Int,
        contentType: String,
        body: String,
        allowCors: Boolean = true
    ) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        val statusText = when (statusCode) {
            200 -> "OK"
            400 -> "Bad Request"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            else -> "OK"
        }

        val headerBuilder = StringBuilder()
        headerBuilder.append("HTTP/1.1 $statusCode $statusText\r\n")
        headerBuilder.append("Content-Type: $contentType\r\n")
        headerBuilder.append("Content-Length: ${bytes.size}\r\n")
        headerBuilder.append("Connection: close\r\n")
        if (allowCors) {
            headerBuilder.append("Access-Control-Allow-Origin: *\r\n")
            headerBuilder.append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
            headerBuilder.append("Access-Control-Allow-Headers: Content-Type\r\n")
        }
        headerBuilder.append("\r\n")

        output.write(headerBuilder.toString().toByteArray(StandardCharsets.UTF_8))
        output.write(bytes)
        output.flush()
    }

    /**
     * Generates a sleek, modern, mobile-optimized HTML5/CSS3/JavaScript customer self-ordering menu.
     */
    private suspend fun generateCustomerMenuHtml(tableNumber: String, zone: String): String {
        try {
            appContext?.assets?.open("pos-menu.html")?.use { inputStream ->
                val html = inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
                val port = getPort()
                val ip = getLocalIpAddress()
                return html
                    .replace("localStorage.getItem('70mm_table') || 'C-1'", "localStorage.getItem('70mm_table') || '$tableNumber'")
                    .replace("localStorage.getItem('70mm_zone') || 'Club area'", "localStorage.getItem('70mm_zone') || '$zone'")
                    .replace("localStorage.getItem('70mm_pos_host') || ''", "localStorage.getItem('70mm_pos_host') || '$ip:$port'")
                    .replace("TABLE C-1", "TABLE $tableNumber")
                    .replace("CLUB AREA", zone.uppercase())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading pos-menu.html from assets, falling back to embedded template", e)
        }

        val products = repository?.allProducts?.firstOrNull() ?: emptyList()
        val productsJson = JSONArray()
        for (p in products) {
            if (p.stockQuantity > 0 && !p.isIngredient) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("name", p.name)
                obj.put("category", p.category)
                obj.put("price", p.sellingPrice)
                obj.put("stock", p.stockQuantity)
                obj.put("section", p.kitchenSection)
                productsJson.put(obj)
            }
        }

        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>70mm Lounge • Table $tableNumber Menu</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif; -webkit-tap-highlight-color: transparent; }
    body { background-color: #0b1120; color: #f8fafc; padding-bottom: 100px; min-height: 100vh; }
    
    /* Header */
    header { background: linear-gradient(180deg, #020617 0%, #0f172a 100%); border-bottom: 1px solid #1e293b; padding: 16px; position: sticky; top: 0; z-index: 40; }
    .brand-row { display: flex; align-items: center; justify-content: space-between; }
    .brand-info { display: flex; align-items: center; gap: 12px; }
    .logo-badge { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #059669 0%, #047857 100%); display: flex; align-items: center; justify-content: center; font-weight: 900; font-size: 16px; color: #fff; box-shadow: 0 4px 12px rgba(5, 150, 105, 0.4); border: 1.5px solid #34d399; }
    .brand-title { font-size: 18px; font-weight: 900; color: #ffffff; letter-spacing: 0.5px; }
    .brand-subtitle { font-size: 11px; color: #94a3b8; font-weight: 500; }
    .table-badge { background: #1e293b; border: 1px solid #334155; padding: 6px 12px; border-radius: 10px; text-align: right; }
    .table-badge .t-num { font-size: 13px; font-weight: 800; color: #34d399; }
    .table-badge .t-zone { font-size: 10px; color: #94a3b8; text-transform: uppercase; }

    /* Live Status Banner */
    #liveStatusCard { margin: 12px 16px; padding: 14px; border-radius: 14px; background: #0f172a; border: 1.5px solid #059669; display: none; }
    #liveStatusCard.active { display: block; animation: slideDown 0.3s ease; }
    .status-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
    .status-pill { padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 800; text-transform: uppercase; }
    .status-cooking { background: rgba(245, 158, 11, 0.15); color: #fbbf24; border: 1px solid #d97706; }
    .status-ready { background: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid #059669; }
    .status-billed { background: rgba(56, 189, 248, 0.15); color: #38bdf8; border: 1px solid #0284c7; }
    .status-body { font-size: 12px; color: #cbd5e1; line-height: 1.5; }
    .checkout-btn { width: 100%; margin-top: 10px; padding: 10px; border-radius: 10px; background: linear-gradient(135deg, #d97706 0%, #b45309 100%); color: #fff; font-size: 13px; font-weight: 800; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 0 4px 14px rgba(217, 119, 6, 0.35); }
    .checkout-btn.requested { background: #334155; color: #94a3b8; cursor: not-allowed; box-shadow: none; }

    /* Search & Category Tabs */
    .controls { padding: 8px 16px 12px; }
    .search-input { width: 100%; padding: 12px 16px; background: #1e293b; border: 1px solid #334155; border-radius: 12px; color: #fff; font-size: 14px; outline: none; margin-bottom: 12px; transition: border-color 0.2s; }
    .search-input:focus { border-color: #059669; }
    .cat-chips { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; scrollbar-width: none; }
    .cat-chips::-webkit-scrollbar { display: none; }
    .chip { padding: 7px 14px; border-radius: 20px; background: #1e293b; border: 1px solid #334155; color: #94a3b8; font-size: 12px; font-weight: 700; white-space: nowrap; cursor: pointer; transition: all 0.2s; }
    .chip.active { background: #059669; border-color: #10b981; color: #fff; box-shadow: 0 2px 8px rgba(5, 150, 105, 0.4); }

    /* Menu Grid */
    .menu-container { padding: 0 16px; display: flex; flex-direction: column; gap: 12px; }
    .menu-item-card { background: #0f172a; border: 1px solid #1e293b; border-radius: 14px; padding: 14px; display: flex; justify-content: space-between; align-items: center; gap: 12px; }
    .item-info { flex: 1; }
    .item-cat { font-size: 10px; color: #10b981; text-transform: uppercase; font-weight: 800; letter-spacing: 0.5px; margin-bottom: 2px; }
    .item-name { font-size: 15px; font-weight: 800; color: #f1f5f9; line-height: 1.3; }
    .item-price { font-size: 14px; font-weight: 900; color: #fbbf24; margin-top: 4px; }
    .stepper { display: flex; align-items: center; gap: 8px; }
    .btn-step { width: 32px; height: 32px; border-radius: 8px; background: #1e293b; border: 1px solid #334155; color: #fff; font-size: 16px; font-weight: 800; cursor: pointer; display: flex; align-items: center; justify-content: center; }
    .btn-step:hover { background: #334155; }
    .step-val { font-size: 14px; font-weight: 800; min-width: 20px; text-align: center; }
    .btn-add { background: #059669; border: 1px solid #10b981; color: #fff; padding: 8px 16px; border-radius: 10px; font-size: 12px; font-weight: 800; cursor: pointer; }

    /* Bottom Cart Float */
    #cartBar { position: fixed; bottom: 0; left: 0; right: 0; background: #020617; border-top: 1px solid #1e293b; padding: 12px 16px; display: none; align-items: center; justify-content: space-between; z-index: 50; box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.6); }
    #cartBar.show { display: flex; animation: slideUp 0.25s ease; }
    .cart-summary { display: flex; flex-direction: column; }
    .cart-count { font-size: 12px; color: #94a3b8; font-weight: 600; }
    .cart-total { font-size: 18px; font-weight: 900; color: #fbbf24; }
    .btn-view-cart { background: linear-gradient(135deg, #059669 0%, #047857 100%); color: #fff; padding: 10px 20px; border-radius: 12px; font-size: 13px; font-weight: 800; border: none; cursor: pointer; box-shadow: 0 4px 12px rgba(5, 150, 105, 0.4); }

    /* Modal Sheet */
    .modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.75); display: none; justify-content: center; align-items: flex-end; z-index: 100; backdrop-filter: blur(4px); }
    .modal-overlay.open { display: flex; }
    .modal-sheet { background: #0f172a; border-top-left-radius: 24px; border-top-right-radius: 24px; width: 100%; max-width: 540px; max-height: 85vh; overflow-y: auto; padding: 20px 16px 30px; border-top: 1px solid #334155; animation: slideUp 0.3s ease; }
    .modal-title { font-size: 18px; font-weight: 900; color: #fff; margin-bottom: 14px; display: flex; justify-content: space-between; align-items: center; }
    .close-btn { background: none; border: none; color: #94a3b8; font-size: 20px; cursor: pointer; }
    
    .form-group { margin-bottom: 12px; }
    .form-group label { display: block; font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; margin-bottom: 4px; }
    .form-input { width: 100%; padding: 10px 14px; background: #1e293b; border: 1px solid #334155; border-radius: 10px; color: #fff; font-size: 14px; outline: none; }
    .form-input:focus { border-color: #059669; }

    .order-items-list { margin: 12px 0; border: 1px solid #1e293b; border-radius: 12px; padding: 8px 12px; background: #020617; }
    .order-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; font-size: 13px; border-bottom: 1px solid #1e293b; }
    .order-row:last-child { border-bottom: none; }
    .btn-submit-order { width: 100%; padding: 14px; border-radius: 12px; background: linear-gradient(135deg, #059669 0%, #047857 100%); color: #fff; font-size: 15px; font-weight: 800; border: none; cursor: pointer; margin-top: 14px; box-shadow: 0 4px 14px rgba(5, 150, 105, 0.4); }
    .btn-submit-order:disabled { opacity: 0.5; cursor: not-allowed; }

    /* Greetings Overlay */
    #greetingsOverlay { position: fixed; inset: 0; background: #0b1120; z-index: 200; display: none; flex-direction: column; align-items: center; justify-content: center; padding: 24px; text-align: center; }
    #greetingsOverlay.active { display: flex; animation: fadeIn 0.4s ease; }
    .stars { font-size: 32px; color: #fbbf24; margin: 16px 0; cursor: pointer; }
    .btn-done { background: #059669; color: #fff; padding: 12px 30px; border-radius: 12px; font-size: 14px; font-weight: 800; border: none; cursor: pointer; margin-top: 20px; }

    @keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
    @keyframes slideDown { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
  </style>
</head>
<body>

  <header>
    <div class="brand-row">
      <div class="brand-info">
        <div class="logo-badge">70</div>
        <div>
          <div class="brand-title">70mm Lounge</div>
          <div class="brand-subtitle">Restaurant & Club Lounge • Bokaro</div>
        </div>
      </div>
      <div class="table-badge">
        <div class="t-num">TABLE $tableNumber</div>
        <div class="t-zone">$zone</div>
      </div>
    </div>
  </header>

  <!-- Live Status Card -->
  <div id="liveStatusCard">
    <div class="status-header">
      <span style="font-size: 13px; font-weight: 800; color: #f8fafc;" id="statusTableLabel">Table $tableNumber • $zone</span>
      <span class="status-pill status-cooking" id="statusPill">COOKING 👨‍🍳</span>
    </div>
    <div class="status-body" id="statusMsg">Your order was sent to the kitchen and is being freshly prepared.</div>
    <div style="font-size: 11px; color: #fbbf24; font-weight: 800; margin-top: 6px;" id="statusBillAmount"></div>
    <button class="checkout-btn" id="btnRequestCheckout" onclick="requestCheckout()">
      🔔 Checkout / Pay Bill
    </button>
  </div>

  <div class="controls">
    <input type="text" class="search-input" id="searchInput" placeholder="Search dishes, drinks, appetizers..." oninput="filterMenu()">
    <div class="cat-chips" id="catChips">
      <div class="chip active" onclick="selectCategory('All', this)">All</div>
    </div>
  </div>

  <div class="menu-container" id="menuContainer">
    <!-- Items rendered via JS -->
  </div>

  <!-- Sticky Bottom Cart Float -->
  <div id="cartBar">
    <div class="cart-summary">
      <span class="cart-count" id="cartCountLabel">0 items</span>
      <span class="cart-total" id="cartTotalLabel">₹0.00</span>
    </div>
    <button class="btn-view-cart" onclick="openCartModal()">View Order & Checkout ➔</button>
  </div>

  <!-- Cart & Checkout Modal -->
  <div class="modal-overlay" id="cartModal">
    <div class="modal-sheet">
      <div class="modal-title">
        <span>Table $tableNumber Order</span>
        <button class="close-btn" onclick="closeCartModal()">✕</button>
      </div>

      <div class="form-group">
        <label>Your Name *</label>
        <input type="text" class="form-input" id="custName" placeholder="Enter your full name" required>
      </div>

      <div class="form-group">
        <label>10-Digit Mobile Number *</label>
        <input type="tel" class="form-input" id="custPhone" placeholder="Enter 10-digit mobile number" maxlength="10" required>
      </div>

      <div class="form-group">
        <label>Cooking Instructions / Notes (Optional)</label>
        <input type="text" class="form-input" id="custNotes" placeholder="e.g. Less spicy, extra ice, etc.">
      </div>

      <div style="font-size: 12px; font-weight: 800; color: #94a3b8; text-transform: uppercase; margin-top: 14px;">Selected Items:</div>
      <div class="order-items-list" id="modalItemsList"></div>

      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px;">
        <span style="font-size: 14px; font-weight: 700; color: #94a3b8;">Total Bill:</span>
        <span style="font-size: 20px; font-weight: 900; color: #fbbf24;" id="modalTotalLabel">₹0.00</span>
      </div>

      <button class="btn-submit-order" id="btnPlaceOrder" onclick="submitOrder()">
        Send Order to Kitchen 🚀
      </button>
    </div>
  </div>

  <!-- Greetings / Thank You Screen -->
  <div id="greetingsOverlay">
    <div style="font-size: 48px; margin-bottom: 12px;">🎉</div>
    <h2 style="font-size: 22px; font-weight: 900; color: #ffffff; margin-bottom: 6px;">Thank You for Dining!</h2>
    <p style="font-size: 14px; color: #34d399; font-weight: 700;">Table $tableNumber Bill Settled Successfully</p>
    <p style="font-size: 12px; color: #94a3b8; margin-top: 6px; max-width: 320px;">We hope you had a fantastic experience at 70mm Lounge. Please visit us again!</p>
    
    <div class="stars">★★★★★</div>
    <button class="btn-done" onclick="location.reload()">Start New Session</button>
  </div>

  <script>
    const tableNumber = "$tableNumber";
    const zone = "$zone";
    const rawProducts = ${productsJson.toString()};
    
    let cart = {}; // productId -> quantity
    let activeCategory = 'All';
    let hasPlacedOrder = false;
    let pollInterval = null;

    function init() {
      // Build categories
      const cats = ['All', ...new Set(rawProducts.map(function(p) { return p.category; }))];
      const chipsContainer = document.getElementById('catChips');
      chipsContainer.innerHTML = cats.map(function(c) {
        return '<div class="chip ' + (c === 'All' ? 'active' : '') + '" onclick="selectCategory(\'' + c + '\', this)">' + c + '</div>';
      }).join('');

      renderMenu();
      startStatusPolling();
    }

    function selectCategory(cat, el) {
      activeCategory = cat;
      document.querySelectorAll('.chip').forEach(function(c) { c.classList.remove('active'); });
      el.classList.add('active');
      renderMenu();
    }

    function filterMenu() {
      renderMenu();
    }

    function renderMenu() {
      const q = document.getElementById('searchInput').value.toLowerCase().trim();
      const container = document.getElementById('menuContainer');
      
      const filtered = rawProducts.filter(function(p) {
        const matchesCat = (activeCategory === 'All' || p.category === activeCategory);
        const matchesSearch = (!q || p.name.toLowerCase().includes(q));
        return matchesCat && matchesSearch;
      });

      if (filtered.length === 0) {
        container.innerHTML = '<div style="text-align: center; color: #64748b; padding: 40px;">No dishes found matching your search</div>';
        return;
      }

      container.innerHTML = filtered.map(function(p) {
        const qty = cart[p.id] || 0;
        const actBtn = qty === 0 ?
          '<button class="btn-add" onclick="updateQty(' + p.id + ', 1)">+ Add</button>' :
          '<div class="stepper">' +
            '<button class="btn-step" onclick="updateQty(' + p.id + ', -1)">-</button>' +
            '<span class="step-val">' + qty + '</span>' +
            '<button class="btn-step" onclick="updateQty(' + p.id + ', 1)">+</button>' +
          '</div>';

        return '<div class="menu-item-card">' +
          '<div class="item-info">' +
            '<div class="item-cat">' + p.category + '</div>' +
            '<div class="item-name">' + p.name + '</div>' +
            '<div class="item-price">₹' + p.price.toFixed(2) + '</div>' +
          '</div>' +
          '<div>' + actBtn + '</div>' +
        '</div>';
      }).join('');

      updateCartBar();
    }

    function updateQty(id, delta) {
      const current = cart[id] || 0;
      const next = current + delta;
      if (next <= 0) {
        delete cart[id];
      } else {
        cart[id] = next;
      }
      renderMenu();
    }

    function updateCartBar() {
      const items = Object.entries(cart);
      const totalCount = items.reduce(function(sum, pair) { return sum + pair[1]; }, 0);
      let totalAmount = 0;
      items.forEach(function(pair) {
        const prod = rawProducts.find(function(p) { return p.id == pair[0]; });
        if (prod) totalAmount += (prod.price * pair[1]);
      });

      const bar = document.getElementById('cartBar');
      if (totalCount > 0) {
        bar.classList.add('show');
        document.getElementById('cartCountLabel').innerText = totalCount + (totalCount > 1 ? ' items' : ' item');
        document.getElementById('cartTotalLabel').innerText = '₹' + totalAmount.toFixed(2);
      } else {
        bar.classList.remove('show');
      }
    }

    function openCartModal() {
      const list = document.getElementById('modalItemsList');
      const items = Object.entries(cart);
      let totalAmount = 0;
      
      list.innerHTML = items.map(function(pair) {
        const id = pair[0];
        const q = pair[1];
        const prod = rawProducts.find(function(p) { return p.id == id; });
        if (!prod) return '';
        const lineTotal = prod.price * q;
        totalAmount += lineTotal;
        return '<div class="order-row">' +
          '<div><strong>' + q + 'x</strong> ' + prod.name + '</div>' +
          '<div style="font-weight: 800; color: #fbbf24;">₹' + lineTotal.toFixed(2) + '</div>' +
        '</div>';
      }).join('');

      document.getElementById('modalTotalLabel').innerText = '₹' + totalAmount.toFixed(2);
      document.getElementById('cartModal').classList.add('open');
    }

    function closeCartModal() {
      document.getElementById('cartModal').classList.remove('open');
    }

    async function submitOrder() {
      const name = document.getElementById('custName').value.trim();
      const phone = document.getElementById('custPhone').value.trim();
      const notes = document.getElementById('custNotes').value.trim();

      if (!name) {
        alert("Please enter your name");
        return;
      }
      if (!phone || phone.length < 10) {
        alert("Please enter a valid 10-digit mobile number");
        return;
      }

      const items = Object.entries(cart).map(function(pair) {
        const id = pair[0];
        const q = pair[1];
        const prod = rawProducts.find(function(p) { return p.id == id; });
        return {
          id: parseInt(id),
          name: prod ? prod.name : 'Dish',
          price: prod ? prod.price : 0,
          quantity: q
        };
      });

      if (items.length === 0) return;

      const btn = document.getElementById('btnPlaceOrder');
      btn.disabled = true;
      btn.innerText = "Placing Order...";

      try {
        const res = await fetch('/api/order', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            tableNumber: tableNumber,
            zone: zone,
            customerName: name,
            customerPhone: phone,
            notes: notes,
            items: items
          })
        });
        const data = await res.json();
        if (data.success) {
          hasPlacedOrder = true;
          cart = {};
          closeCartModal();
          renderMenu();

          // Show status card
          const card = document.getElementById('liveStatusCard');
          card.classList.add('active');
          document.getElementById('statusPill').className = 'status-pill status-cooking';
          document.getElementById('statusPill').innerText = 'COOKING 👨‍🍳';
          document.getElementById('statusMsg').innerText = 'Order placed for ' + name + ' (' + phone + '). Food is being freshly prepared!';
        } else {
          alert(data.error || "Failed to place order");
        }
      } catch (err) {
        alert("Network error placing order: " + err.message);
      } finally {
        btn.disabled = false;
        btn.innerText = "Send Order to Kitchen 🚀";
      }
    }

    async function requestCheckout() {
      const btn = document.getElementById('btnRequestCheckout');
      btn.disabled = true;
      btn.innerText = "Requesting Checkout...";

      try {
        const res = await fetch('/api/request-checkout', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ tableNumber: tableNumber })
        });
        const data = await res.json();
        if (data.success) {
          btn.className = 'checkout-btn requested';
          btn.innerText = '🔔 Checkout Requested ⏳ Cashier Notified';
        }
      } catch (e) {
        alert("Failed to request checkout");
        btn.disabled = false;
        btn.innerText = "🔔 Checkout / Pay Bill";
      }
    }

    function startStatusPolling() {
      if (pollInterval) clearInterval(pollInterval);
      pollInterval = setInterval(async function() {
        try {
          const res = await fetch('/api/table-status?table=' + encodeURIComponent(tableNumber));
          const data = await res.json();
          
          const card = document.getElementById('liveStatusCard');
          const pill = document.getElementById('statusPill');
          const msg = document.getElementById('statusMsg');
          const billEl = document.getElementById('statusBillAmount');
          const checkoutBtn = document.getElementById('btnRequestCheckout');

          if (data.status === 'OCCUPIED' || data.status === 'CHECKOUT_REQUESTED' || hasPlacedOrder) {
            card.classList.add('active');
            if (data.billAmount > 0) {
              billEl.innerText = 'Current Running Bill: ₹' + data.billAmount.toFixed(2);
            }

            if (data.kotStatus === 'READY' || data.kotStatus === 'SERVED') {
              pill.className = 'status-pill status-ready';
              pill.innerText = 'SERVED / READY 🍽️';
              msg.innerText = 'Your food & drinks are served to your table. Enjoy your meal!';
            } else {
              pill.className = 'status-pill status-cooking';
              pill.innerText = 'COOKING 👨‍🍳';
              msg.innerText = 'Your order is being prepared in the kitchen.';
            }

            if (data.isCheckoutRequested) {
              checkoutBtn.className = 'checkout-btn requested';
              checkoutBtn.innerText = '🔔 Checkout Requested ⏳ Cashier Notified';
            }
          }

          // If order was placed, and table status is vacated/available -> Order Completed!
          if (hasPlacedOrder && (data.status === 'AVAILABLE' || data.status === 'BILLED')) {
            clearInterval(pollInterval);
            document.getElementById('greetingsOverlay').classList.add('active');
          }
        } catch (_e) {}
      }, 3000);
    }

    init();
  </script>
</body>
</html>
        """.trimIndent()
    }
}
