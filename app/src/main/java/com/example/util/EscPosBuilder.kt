package com.example.util

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * High-performance ESC/POS Command & Thermal Printing Library for Android POS hardware.
 * Compatible with Rugtek (RP-326, RP-80, RP-330), Epson, Star, TVS, NGX, POS-58, POS-80.
 */
class EscPosBuilder(private val charset: Charset = Charsets.UTF_8) {

    private val out = ByteArrayOutputStream()

    companion object {
        const val ESC: Byte = 0x1B
        const val FS: Byte = 0x1C
        const val GS: Byte = 0x1D
        const val LF: Byte = 0x0A

        // Standard alignments
        const val ALIGN_LEFT: Byte = 0x00
        const val ALIGN_CENTER: Byte = 0x01
        const val ALIGN_RIGHT: Byte = 0x02

        // Font sizes (GS ! n)
        const val SIZE_NORMAL: Byte = 0x00
        const val SIZE_DOUBLE_HEIGHT: Byte = 0x01
        const val SIZE_DOUBLE_WIDTH: Byte = 0x10
        const val SIZE_DOUBLE_BOTH: Byte = 0x11
        const val SIZE_LARGE_3X: Byte = 0x22
        const val SIZE_LARGE_4X: Byte = 0x33
    }

    init {
        initPrinter()
    }

    fun initPrinter(): EscPosBuilder {
        out.write(byteArrayOf(ESC, 0x40)) // ESC @
        return this
    }

    fun align(alignment: Byte): EscPosBuilder {
        out.write(byteArrayOf(ESC, 0x61, alignment)) // ESC a n
        return this
    }

    fun alignLeft(): EscPosBuilder = align(ALIGN_LEFT)
    fun alignCenter(): EscPosBuilder = align(ALIGN_CENTER)
    fun alignRight(): EscPosBuilder = align(ALIGN_RIGHT)

    fun bold(enable: Boolean): EscPosBuilder {
        out.write(byteArrayOf(ESC, 0x45, if (enable) 0x01 else 0x00)) // ESC E n
        return this
    }

    fun underline(enable: Boolean): EscPosBuilder {
        out.write(byteArrayOf(ESC, 0x2D, if (enable) 0x01 else 0x00)) // ESC - n
        return this
    }

    fun invert(enable: Boolean): EscPosBuilder {
        out.write(byteArrayOf(GS, 0x42, if (enable) 0x01 else 0x00)) // GS B n
        return this
    }

    fun textSize(size: Byte): EscPosBuilder {
        out.write(byteArrayOf(GS, 0x21, size)) // GS ! n
        return this
    }

    fun text(text: String): EscPosBuilder {
        out.write(text.toByteArray(charset))
        return this
    }

    fun textLine(line: String = ""): EscPosBuilder {
        if (line.isNotEmpty()) {
            out.write(line.toByteArray(charset))
        }
        out.write(LF.toInt())
        return this
    }

    fun feed(lines: Int = 1): EscPosBuilder {
        repeat(lines.coerceAtLeast(1)) {
            out.write(LF.toInt())
        }
        return this
    }

    /**
     * Prints a two-column formatted line (e.g. "Item name        Rs. 150.00")
     */
    fun twoColumn(left: String, right: String, totalCols: Int): EscPosBuilder {
        val spaces = totalCols - left.length - right.length
        val line = if (spaces > 0) {
            left + " ".repeat(spaces) + right
        } else {
            "$left $right"
        }
        return textLine(line)
    }

    /**
     * Prints a 3-column row (e.g. Item Name, Qty/Rate, Total)
     */
    fun threeColumn(col1: String, col2: String, col3: String, totalCols: Int): EscPosBuilder {
        val c1Width = if (totalCols >= 48) 26 else 16
        val c2Width = if (totalCols >= 48) 12 else 8
        val left = col1.take(c1Width).padEnd(c1Width)
        val mid = col2.padEnd(c2Width)
        val right = col3.padStart((totalCols - left.length - mid.length).coerceAtLeast(1))
        return textLine(left + mid + right)
    }

    fun divider(totalCols: Int, char: Char = '-'): EscPosBuilder {
        return textLine(char.toString().repeat(totalCols))
    }

    fun doubleDivider(totalCols: Int): EscPosBuilder {
        return divider(totalCols, '=')
    }

    /**
     * Native Hardware ESC/POS QR Code generator (GS ( k)
     * Renders directly via printer DSP without converting to bitmap.
     * Ideal for UPI payment QR codes (PhonePe / GooglePay / Paytm) on receipt.
     */
    fun qrCode(data: String, moduleSize: Int = 6): EscPosBuilder {
        val dataBytes = data.toByteArray(charset)
        val len = dataBytes.size + 3
        val pL = (len and 0xFF).toByte()
        val pH = ((len shr 8) and 0xFF).toByte()

        // 1. Select Model 2 (Function 165)
        out.write(byteArrayOf(GS, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00))

        // 2. Set Module Size (Function 167)
        val sizeClamped = moduleSize.coerceIn(1, 16).toByte()
        out.write(byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, sizeClamped))

        // 3. Set Error Correction Level M (Function 169)
        out.write(byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31))

        // 4. Store QR Data in Symbol Storage (Function 180)
        out.write(byteArrayOf(GS, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30))
        out.write(dataBytes)

        // 5. Print Symbol (Function 181)
        out.write(byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30))

        return this
    }

    /**
     * Hardware Auto-Cutter command
     */
    fun cutPaper(fullCut: Boolean = false): EscPosBuilder {
        feed(3) // Feed paper so receipt content clears cutter blade
        val cutParam: Byte = if (fullCut) 0x41 else 0x42
        out.write(byteArrayOf(GS, 0x56, cutParam, 0x03))
        return this
    }

    /**
     * Kick POS Cash Drawer (RJ11/RJ12 drawer port on Rugtek / Epson)
     */
    fun kickDrawer(pin: Int = 0): EscPosBuilder {
        val pinByte: Byte = if (pin == 1) 0x01 else 0x00
        out.write(byteArrayOf(ESC, 0x70, pinByte, 0x19, 0xFA.toByte()))
        return this
    }

    /**
     * POS Hardware Beep / Buzzer
     */
    fun beep(count: Int = 1): EscPosBuilder {
        out.write(byteArrayOf(ESC, 0x42, count.coerceIn(1, 9).toByte(), 0x02))
        return this
    }

    fun build(): ByteArray {
        return out.toByteArray()
    }
}
