package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FashionGold,
    onPrimary = EbonyBackground,
    primaryContainer = FashionNavy,
    onPrimaryContainer = FashionGoldLight,
    secondary = FashionRose,
    onSecondary = EbonyBackground,
    tertiary = FashionSage,
    background = EbonyBackground,
    surface = EbonySurface,
    surfaceVariant = EbonySurfaceVariant,
    onBackground = EbonyTextPrimary,
    onSurface = EbonyTextPrimary,
    onSurfaceVariant = EbonyTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = FashionTerracotta,
    onPrimary = PearlBackground,
    primaryContainer = PearlSurfaceVariant,
    onPrimaryContainer = PearlTextPrimary,
    secondary = FashionNavy,
    onSecondary = PearlBackground,
    tertiary = FashionSage,
    background = PearlBackground,
    surface = PearlSurface,
    surfaceVariant = PearlSurfaceVariant,
    onBackground = PearlTextPrimary,
    onSurface = PearlTextPrimary,
    onSurfaceVariant = PearlTextSecondary
)

@Composable
fun OutfitStylistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted luxury palette by default
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

