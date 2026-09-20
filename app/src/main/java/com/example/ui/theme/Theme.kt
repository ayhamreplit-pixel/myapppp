package com.example.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private val TodColorScheme =
  darkColorScheme(
    primary = TodCyan,
    onPrimary = Color(0xFF00222B),
    primaryContainer = Color(0xFF004958),
    onPrimaryContainer = Color(0xFFB8F6FF),
    secondary = TodViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3B1F70),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = TodLiveRed,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF1E283C),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = TodColorScheme,
    typography = Typography,
  ) {
    CompositionLocalProvider(
      LocalTextStyle provides TextStyle(
        fontFamily = AppFontFamily,
        color = DarkTextPrimary
      ),
      content = content
    )
  }
}

