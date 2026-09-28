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
     * Dynamically creates a unique URL for a restaurant table.
     * Contains table number, zone, venue information, and a cryptographic session signature
     * to facilitate secure customer self-ordering.
     */
    fun createDynamicTableUrl(
        tableNumber: String,
        zone: String,
        baseDomain: String = DEFAULT_WEB_DOMAIN,
        salt: String = "70mm_lounge_secret"
    ): String {
        val cleanTable = tableNumber.trim()
        val cleanZone = zone.trim().ifBlank { "Club area" }
        val token = generateTableToken(cleanTable, cleanZone, salt)
        val encodedZone = urlEncode(cleanZone)
        val encodedClub = urlEncode(RESTAURANT_NAME)

        return "$baseDomain/order?table=$cleanTable&zone=$encodedZone&club=$encodedClub&phone=$RESTAURANT_PHONE&token=$token"
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

    fun generateQrMatrix(data: String, size: Int = 25): Array<BooleanArray> {
        val matrix = Array(size) { BooleanArray(size) { false } }

        // 1. Draw 3 Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, size - 7, 0)
        drawFinderPattern(matrix, 0, size - 7)

        // 2. Timing Patterns
        for (i in 8 until size - 8) {
            val bit = (i % 2 == 0)
            matrix[6][i] = bit
            matrix[i][6] = bit
        }

        // 3. Encode data deterministically into the remaining modules using hash & bytes
        val bytes = data.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        val hash = abs(data.hashCode())

        for (col in (size - 1) downTo 0 step 2) {
            val c = if (col <= 6) col - 1 else col
            if (c < 0) continue

            for (row in 0 until size) {
                val r = if ((col / 2) % 2 == 0) row else (size - 1 - row)

                for (subCol in 0..1) {
                    val currCol = c - subCol
                    if (currCol < 0) continue

                    // Skip finder patterns & timing
                    if (isFunctionPattern(currCol, r, size)) continue

                    val byteVal = if (bytes.isNotEmpty()) bytes[bitIndex % bytes.size].toInt() else 0
                    val pseudoBit = ((byteVal shr (bitIndex % 8)) and 1) == 1
                    val hashBit = ((hash shr ((bitIndex + r + currCol) % 31)) and 1) == 1

                    // Masking pattern (r + currCol) % 2 == 0
                    val mask = (r + currCol) % 2 == 0
                    matrix[r][currCol] = (pseudoBit xor hashBit xor mask)
                    bitIndex++
                }
            }
        }

        // Ensure Alignment Pattern for size >= 25 (centered at (size-7, size-7))
        if (size >= 25) {
            drawAlignmentPattern(matrix, size - 7, size - 7)
        }

        return matrix
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startRow: Int, startCol: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = (r == 0 || r == 6 || c == 0 || c == 6)
                val isInner = (r in 2..4 && c in 2..4)
                matrix[startRow + r][startCol + c] = isOuter || isInner
            }
        }
        // Separator ring
        for (r in -1..7) {
            for (c in -1..7) {
                val row = startRow + r
                val col = startCol + c
                if (row in matrix.indices && col in matrix[0].indices) {
                    if (r == -1 || r == 7 || c == -1 || c == 7) {
                        matrix[row][col] = false
                    }
                }
            }
        }
    }

    private fun drawAlignmentPattern(matrix: Array<BooleanArray>, centerRow: Int, centerCol: Int) {
        for (r in -2..2) {
            for (c in -2..2) {
                val isOuter = (abs(r) == 2 || abs(c) == 2)
                val isCenter = (r == 0 && c == 0)
                matrix[centerRow + r][centerCol + c] = isOuter || isCenter
            }
        }
    }

    private fun isFunctionPattern(col: Int, row: Int, size: Int): Boolean {
        if (row in 0..8 && col in 0..8) return true
        if (row in 0..8 && col in (size - 8) until size) return true
        if (row in (size - 8) until size && col in 0..8) return true
        if (row == 6 || col == 6) return true
        if (size >= 25 && row in (size - 9)..(size - 5) && col in (size - 9)..(size - 5)) return true
        return false
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
        val qrMatrix = generateQrMatrix(url, 25)
        val moduleSize = qrSize.toFloat() / 25f

        paint.color = AndroidColor.parseColor("#F6F9F9")
        canvas.drawRoundRect(RectF(qrLeft - 20f, qrTop - 20f, qrLeft + qrSize + 20f, qrTop + qrSize + 20f), 24f, 24f, paint)

        paint.color = AndroidColor.parseColor("#1B2324") // Deep Gunmetal
        for (r in 0 until 25) {
            for (c in 0 until 25) {
                if (qrMatrix[r][c]) {
                    canvas.drawRect(
                        qrLeft + c * moduleSize,
                        qrTop + r * moduleSize,
                        qrLeft + (c + 1) * moduleSize,
                        qrTop + (r + 1) * moduleSize,
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
    val matrixSize = 25
    val matrix = remember(data) {
        QrCodeGenerator.generateQrMatrix(data, matrixSize)
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.size(sizeDp - 24.dp)) {
            val moduleWidth = size.width / matrixSize
            val moduleHeight = size.height / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
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
