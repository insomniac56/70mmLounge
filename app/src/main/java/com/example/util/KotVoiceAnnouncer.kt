package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Manages audio chime bell sound and Hindi voice announcements for incoming Kitchen Order Tickets (KOT).
 * 
 * Announcement Rules:
 * 1. Bell Chime plays first to alert the kitchen staff.
 * 2. If it's a NEW table order:
 *    Hindi: "ध्यान दें! एरिया [Area Name], टेबल नंबर [Table] का नया ऑर्डर आया है।"
 * 3. If table is already active and items were added (ADD-ON order):
 *    Hindi: "ध्यान दें! एरिया [Area Name], टेबल नंबर [Table] में कुछ और आइटम ऐड हुआ है।"
 */
class KotVoiceAnnouncer(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var isHindiSupported = false
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("KotVoiceAnnouncer", "Failed to init TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts ?: return
            
            // Try Indian Hindi first
            val hindiLocale = Locale("hi", "IN")
            val langResult = ttsEngine.setLanguage(hindiLocale)
            
            if (langResult == TextToSpeech.LANG_AVAILABLE || 
                langResult == TextToSpeech.LANG_COUNTRY_AVAILABLE || 
                langResult == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                isHindiSupported = true
            } else {
                // Try generic Hindi
                val genericHindi = Locale("hi")
                val genericResult = ttsEngine.setLanguage(genericHindi)
                isHindiSupported = genericResult != TextToSpeech.LANG_MISSING_DATA && genericResult != TextToSpeech.LANG_NOT_SUPPORTED
            }

            // Fallback to Indian English if Hindi voice data is not installed on device
            if (!isHindiSupported) {
                val inLocale = Locale("en", "IN")
                ttsEngine.setLanguage(inLocale)
            }

            // Configure speech properties for kitchen ambient noise clarity
            ttsEngine.setPitch(1.02f)
            ttsEngine.setSpeechRate(0.92f)
            isTtsInitialized = true
            Log.d("KotVoiceAnnouncer", "TTS Initialized. Hindi supported: $isHindiSupported")
        } else {
            Log.e("KotVoiceAnnouncer", "TTS Init failed with status: $status")
        }
    }

    /**
     * Plays a two-tone kitchen bell chime followed by the Hindi voice announcement.
     *
     * @param areaName Name of the lounge/restaurant area (e.g., "Main Dining", "Lounge", "Rooftop", "Family AC")
     * @param tableNumber Table identifier (e.g., "T-04", "Table 5", "Lounge-2")
     * @param isAddon true if items were added to an already active table; false if brand new order
     */
    fun announceKot(areaName: String, tableNumber: String, isAddon: Boolean) {
        // 1. Play dual-tone bell chime
        playKitchenChime()

        // 2. Delay voice slightly (450ms) so chime finishes clearly before voice speaks
        mainHandler.postDelayed({
            speakAnnouncement(areaName, tableNumber, isAddon)
        }, 450)
    }

    /**
     * Dual-tone bell chime sound for noisy kitchen environment
     */
    fun playKitchenChime() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 220)
            mainHandler.postDelayed({
                try {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 280)
                } catch (_: Exception) {}
            }, 230)
        } catch (e: Exception) {
            Log.w("KotVoiceAnnouncer", "Could not play ToneGenerator chime", e)
        }
    }

    private fun speakAnnouncement(areaName: String, tableNumber: String, isAddon: Boolean) {
        if (!isTtsInitialized) return

        val cleanArea = sanitizeName(areaName).ifBlank { "हॉल एरिया" }
        val cleanTable = sanitizeTable(tableNumber)

        val hindiSpeechText = if (!isAddon) {
            "ध्यान दें! $cleanArea, टेबल नंबर $cleanTable का नया ऑर्डर आया है।"
        } else {
            "ध्यान दें! $cleanArea, टेबल नंबर $cleanTable में कुछ और आइटम ऐड हुआ है।"
        }

        // Phonetic Hindi/Hinglish fallback for engines without Devanagari font script support
        val speechTextToUse = if (isHindiSupported) {
            hindiSpeechText
        } else {
            if (!isAddon) {
                "Dhyan dein! $cleanArea, Table number $cleanTable ka naya order aaya hai."
            } else {
                "Dhyan dein! $cleanArea, Table number $cleanTable mein kuch aur item add hua hai."
            }
        }

        try {
            tts?.speak(
                speechTextToUse,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "kot_voice_${System.currentTimeMillis()}"
            )
        } catch (e: Exception) {
            Log.e("KotVoiceAnnouncer", "TTS speak failed", e)
        }
    }

    private fun sanitizeName(name: String): String {
        return name.replace("Dining", "डाइनिंग")
            .replace("Lounge", "लाउंज")
            .replace("Rooftop", "रूफटॉप")
            .replace("Bar", "बार")
            .replace("Family", "फैमिली")
            .replace("Club area", "क्लब एरिया")
            .replace("Club", "क्लब")
            .replace("Zone", "एरिया")
            .trim()
    }

    private fun sanitizeTable(table: String): String {
        val t = table.replace("Table", "", ignoreCase = true)
            .replace("T-", "")
            .replace("T", "")
            .trim()
        return if (t.isNotBlank()) t else table
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isTtsInitialized = false
    }
}
