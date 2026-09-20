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

private val DarkColorScheme =
  darkColorScheme(
    primary = ZeroSobraGreenLight,
    onPrimary = ZeroSobraTextPrimary,
    primaryContainer = ZeroSobraGreenDark,
    onPrimaryContainer = ZeroSobraGreenContainer,
    secondary = ZeroSobraWarmAmber,
    background = Color(0xFF121A13),
    surface = Color(0xFF182319),
    onBackground = Color(0xFFE8EFE8),
    onSurface = Color(0xFFE8EFE8),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ZeroSobraGreenPrimary,
    onPrimary = ZeroSobraOnPrimary,
    primaryContainer = ZeroSobraGreenContainer,
    onPrimaryContainer = ZeroSobraGreenDark,
    secondary = ZeroSobraWarmAmber,
    onSecondary = Color.White,
    secondaryContainer = ZeroSobraWarmContainer,
    onSecondaryContainer = ZeroSobraWarmOrange,
    background = ZeroSobraBackground,
    surface = ZeroSobraSurface,
    surfaceVariant = ZeroSobraSurfaceVariant,
    onBackground = ZeroSobraTextPrimary,
    onSurface = ZeroSobraTextPrimary,
    outline = ZeroSobraOutline,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep branded colors by default for consistent sustainability identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
