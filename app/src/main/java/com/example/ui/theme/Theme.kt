package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500, // Vibrant Champagne Gold
    onPrimary = Slate900, // Deep Gunmetal
    primaryContainer = Slate800, // Signature Gunmetal (#293234)
    onPrimaryContainer = Emerald100, // Video Champagne (#F5D4AF)
    secondary = Emerald600, // Rich Champagne Gold
    onSecondary = Color.White,
    background = Color(0xFF14191A), // Deep Luxury Gunmetal Noir
    surface = Color(0xFF1C2425), // Gunmetal Surface
    onSurface = Slate50,
    surfaceVariant = Slate800, // Signature Gunmetal (#293234)
    onSurfaceVariant = Slate400,
    outline = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = Slate900, // Deep Gunmetal
    onPrimary = Color.White,
    primaryContainer = Slate100, // Light Gunmetal Container
    onPrimaryContainer = Slate900,
    secondary = Emerald600, // Rich Champagne Gold
    onSecondary = Color.White,
    secondaryContainer = Emerald50, // Champagne Cream
    onSecondaryContainer = Emerald600,
    tertiary = Sky600,
    background = Slate50, // Clean Gunmetal Off-white (#F6F9F9)
    surface = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200
)

@Composable
fun RetailPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our sleek custom branded minimal POS theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
