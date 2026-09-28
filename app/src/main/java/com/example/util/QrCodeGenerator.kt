package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.model.RestaurantTable
import com.example.ui.theme.Slate900
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.security.MessageDigest
import kotlin.math.abs

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Utility for dynamically generating unique, tamper-evident customer self-ordering URLs
 * and rendering scannable QR matrices for each restaurant & club table.
 */
object TableUrlGenerator {

    const val DEFAULT_WEB_DOMAIN = "https://order.70mmlounge.club"
    const val RESTAURANT_NAME = "70mm Lounge"
    const val RESTAURANT_PHONE = "8987477773"
    const val RESTAURANT_EMAIL = "70mmlounge8bokaro@gmail.com"

    private fun urlEncode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8").replace("+", "%20")
        } catch (_: Exception) {
            value.replace(" ", "%20")
        }
    }

    /**
     * Resolves the device's local LAN or Wi-Fi / Hotspot IPv4 address.
     */
    fun getDeviceIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val ip = addr.hostAddress ?: ""
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                            return ip
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return "192.168.1.100"
    }

    /**
     * Generates a scannable HTTP URL that opens the customer web ordering menu.
     * Works across any phone on the local Wi-Fi or Hotspot network.
     */
    fun createDynamicTableUrl(
        tableNumber: String,
        zone: String,
        baseDomain: String = DEFAULT_WEB_DOMAIN,
        salt: String = "70mm_lounge_secret"
    ): String {
        val cleanTable = tableNumber.trim()
        val cleanZone = zone.trim().ifBlank { "Club area" }
        val encodedZone = urlEncode(cleanZone)
        val encodedClub = urlEncode(RESTAURANT_NAME)

        // If a specific custom web domain was requested that is not the default, use it
        return if (baseDomain.isNotBlank() && baseDomain != DEFAULT_WEB_DOMAIN && !baseDomain.contains("192.168") && !baseDomain.contains(":8080")) {
            val domain = baseDomain.trimEnd('/')
            "$domain/order?table=$cleanTable&zone=$encodedZone&club=$encodedClub"
        } else {
            val ip = getDeviceIpAddress()
            val port = CustomerHttpServer.getPort()
            "http://$ip:$port/order?table=$cleanTable&zone=$encodedZone&club=$encodedClub"
        }
    }

    /**
     * Generates a native deep-link URI that automatically opens the self-ordering screen in the app.
     */
    fun createTableDeepLink(tableNumber: String, zone: String): String {
        val cleanTable = tableNumber.trim()
        val cleanZone = zone.trim().ifBlank { "Club area" }
        val token = generateTableToken(cleanTable, cleanZone, "70mm_lounge_secret")
        val encodedZone = urlEncode(cleanZone)
        val encodedClub = urlEncode(RESTAURANT_NAME)
        return "70mm://table/$cleanTable?zone=$encodedZone&club=$encodedClub&phone=$RESTAURANT_PHONE&token=$token"
    }

    /**
     * Generates a short 8-character verification token for table authenticity.
     */
    private fun generateTableToken(tableNumber: String, zone: String, salt: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val bytes = md.digest("$tableNumber:$zone:$salt".toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }.take(8)
        } catch (_: Exception) {
            abs("$tableNumber$zone$salt".hashCode()).toString(16).take(8)
        }
    }
}

/**
 * Procedural standard QR matrix generator to visually display crisp QR codes
 * and export printable table standee graphics for every single restaurant and club table.
 */
object QrCodeGenerator {

    /**
     * Generates a 100% ISO-standard scannable QR Code boolean matrix using ZXing.
     * Can be scanned by any smartphone camera (Google Lens, Apple Camera, Samsung, Xiaomi, etc.)
     */
    fun generateQrMatrix(data: String, minSize: Int = 29): Array<BooleanArray> {
        return try {
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            hints[EncodeHintType.MARGIN] = 1
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

            val bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 0, 0, hints)
            val w = bitMatrix.width
            val h = bitMatrix.height
            val matrix = Array(h) { BooleanArray(w) }
            for (y in 0 until h) {
                for (x in 0 until w) {
                    matrix[y][x] = bitMatrix.get(x, y)
                }
            }
            matrix
        } catch (e: Exception) {
            // Fallback safe minimum matrix
            Array(25) { BooleanArray(25) }
        }
    }

    /**
     * Creates a high-resolution printable Bitmap standee graphic for a table.
     */
    fun createTableStandeeBitmap(
        table: RestaurantTable,
        url: String,
        width: Int = 800,
        height: Int = 1100
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)

        // Background
        canvas.drawColor(AndroidColor.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer border
        paint.color = AndroidColor.parseColor("#D8E1E2")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawRoundRect(RectF(20f, 20f, width - 20f, height - 20f), 32f, 32f, paint)

        // Top decorative bar
        paint.style = Paint.Style.FILL
        paint.color = AndroidColor.parseColor("#293234") // Signature Gunmetal (#293234)
        canvas.drawRoundRect(RectF(20f, 20f, width - 20f, 130f), 32f, 32f, paint)
        canvas.drawRect(RectF(20f, 70f, width - 20f, 130f), paint)

        // Top Title
        paint.color = AndroidColor.WHITE
        paint.textSize = 44f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("70MM LOUNGE", width / 2f, 85f, paint)

        // Subtitle
        paint.color = AndroidColor.parseColor("#56676A") // Gunmetal medium
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Club Lounge • Restaurant • Bar  |  Bokaro", width / 2f, 175f, paint)

        // Table Badge Pill
        val badgeRect = RectF(width / 2f - 220f, 205f, width / 2f + 220f, 275f)
        paint.color = AndroidColor.parseColor("#C58A3E") // Champagne Gold
        canvas.drawRoundRect(badgeRect, 20f, 20f, paint)

        paint.color = AndroidColor.WHITE
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TABLE ${table.tableNumber} • ${table.zone.uppercase()}", width / 2f, 250f, paint)

        // QR Code Box
        val qrSize = 460
        val qrLeft = (width - qrSize) / 2
        val qrTop = 310
        val qrMatrix = generateQrMatrix(url)
        val matrixRows = qrMatrix.size
        val matrixCols = if (matrixRows > 0) qrMatrix[0].size else 1
        val moduleWidth = qrSize.toFloat() / matrixCols.toFloat()
        val moduleHeight = qrSize.toFloat() / matrixRows.toFloat()

        paint.color = AndroidColor.parseColor("#F6F9F9")
        canvas.drawRoundRect(RectF(qrLeft - 20f, qrTop - 20f, qrLeft + qrSize + 20f, qrTop + qrSize + 20f), 24f, 24f, paint)

        paint.color = AndroidColor.parseColor("#1B2324") // Deep Gunmetal
        for (r in 0 until matrixRows) {
            for (c in 0 until matrixCols) {
                if (qrMatrix[r][c]) {
                    canvas.drawRect(
                        qrLeft + c * moduleWidth,
                        qrTop + r * moduleHeight,
                        qrLeft + (c + 1) * moduleWidth,
                        qrTop + (r + 1) * moduleHeight,
                        paint
                    )
                }
            }
        }

        // Instructions
        paint.color = AndroidColor.parseColor("#1B2324")
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SCAN TO ORDER FOOD & DRINKS", width / 2f, 850f, paint)

        paint.color = AndroidColor.parseColor("#75878A")
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Place orders from your phone directly to the Kitchen & Bar", width / 2f, 890f, paint)

        // Footer URL / Details
        paint.color = AndroidColor.parseColor("#C58A3E") // Champagne Gold
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val displayUrl = if (url.length > 55) url.take(55) + "..." else url
        canvas.drawText(displayUrl, width / 2f, 960f, paint)

        paint.color = AndroidColor.parseColor("#9EAEB0")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Powered by 70MM Lounge POS • Support: 8987477773", width / 2f, 1020f, paint)

        return bitmap
    }

    /**
     * Downloads/saves the Table QR Standee image to device storage and returns Uri.
     */
    fun downloadTableQrStandee(
        context: Context,
        table: RestaurantTable,
        url: String,
        showToast: Boolean = true
    ): Uri? {
        return try {
            val bitmap = createTableStandeeBitmap(table, url)
            val fileName = "70MM_Lounge_Table_${table.tableNumber.replace(" ", "_")}_QR.png"

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/70MM_Lounge_QR_Standees")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val insertedUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (insertedUri != null) {
                    context.contentResolver.openOutputStream(insertedUri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(insertedUri, contentValues, null, null)
                }
                insertedUri
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "70MM_Lounge_QR_Standees").apply { mkdirs() }
                val file = File(appDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }

            if (showToast) {
                Toast.makeText(
                    context,
                    "✅ Table ${table.tableNumber} QR Standee downloaded to Pictures!",
                    Toast.LENGTH_LONG
                ).show()
            }
            uri
        } catch (e: Exception) {
            // Fallback: save to app internal cache and provide URI
            try {
                val qrDir = File(context.cacheDir, "qr").apply { mkdirs() }
                val file = File(qrDir, "Table_${table.tableNumber}_QR.png")
                val bitmap = createTableStandeeBitmap(table, url)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val fallbackUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                if (showToast) {
                    Toast.makeText(context, "✅ Table ${table.tableNumber} QR Standee ready to print!", Toast.LENGTH_SHORT).show()
                }
                fallbackUri
            } catch (ex: Exception) {
                Toast.makeText(context, "Error saving QR: ${ex.message}", Toast.LENGTH_SHORT).show()
                null
            }
        }
    }
}

@Composable
fun TableQrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 180.dp,
    foregroundColor: Color = Slate900,
    backgroundColor: Color = Color.White
) {
    val matrix = remember(data) {
        QrCodeGenerator.generateQrMatrix(data)
    }
    val rows = matrix.size
    val cols = if (rows > 0) matrix[0].size else 1

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.size(sizeDp - 24.dp)) {
            val moduleWidth = size.width / cols.toFloat()
            val moduleHeight = size.height / rows.toFloat()

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = foregroundColor,
                            topLeft = Offset(c * moduleWidth, r * moduleHeight),
                            size = Size(moduleWidth + 0.5f, moduleHeight + 0.5f)
                        )
                    }
                }
            }
        }
    }
}
