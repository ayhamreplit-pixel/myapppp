package com.example.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

enum class AppThemePreset(
  val id: String,
  val titleAr: String,
  val subtitleAr: String,
  val primaryColor: Color,
  val glowColor: Color,
  val gradient: Brush
) {
  GOLD(
    id = "gold",
    titleAr = "الذهبي الأصلي (TOD Gold)",
    subtitleAr = "الثيم الافتراضي الرسمي لـ TOD & beIN",
    primaryColor = Color(0xFFFDB913),
    glowColor = Color(0xFFFFCF33),
    gradient = Brush.horizontalGradient(listOf(Color(0xFFFDB913), Color(0xFFFFCE40)))
  ),
  CYAN(
    id = "cyan",
    titleAr = "أزرق سيان نيون (Cyan Glow)",
    subtitleAr = "إضاءة نيون رياضية عصرية",
    primaryColor = Color(0xFF00E5FF),
    glowColor = Color(0xFF38EFFF),
    gradient = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF0284C7)))
  ),
  PURPLE(
    id = "purple",
    titleAr = "أرجواني ملكي (Royal Purple)",
    subtitleAr = "فخامة وأناقة سينمائية",
    primaryColor = Color(0xFF9333EA),
    glowColor = Color(0xFFA855F7),
    gradient = Brush.horizontalGradient(listOf(Color(0xFF9333EA), Color(0xFFC084FC)))
  ),
  EMERALD(
    id = "emerald",
    titleAr = "أخضر الملاعب (Pitch Emerald)",
    subtitleAr = "أجواء عشب الملاعب الحماسية",
    primaryColor = Color(0xFF10B981),
    glowColor = Color(0xFF34D399),
    gradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
  ),
  RED(
    id = "red",
    titleAr = "أحمر رياضي لايف (Live Red)",
    subtitleAr = "حماس المباريات والبطولات الكبرى",
    primaryColor = Color(0xFFE50914),
    glowColor = Color(0xFFFF3845),
    gradient = Brush.horizontalGradient(listOf(Color(0xFFE50914), Color(0xFFFF5252)))
  ),
  AMOLED_SILVER(
    id = "silver",
    titleAr = "أسود وفضي ماسي (AMOLED Silver)",
    subtitleAr = "تباين مطلق لشاشات الأوليد",
    primaryColor = Color(0xFFE2E8F0),
    glowColor = Color(0xFFFFFFFF),
    gradient = Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)))
  );

  companion object {
    fun fromId(id: String): AppThemePreset {
      return entries.find { it.id.equals(id, ignoreCase = true) } ?: GOLD
    }
  }
}

object ThemeStateHolder {
  var currentTheme by mutableStateOf(AppThemePreset.GOLD)
}

val LocalAppTheme = compositionLocalOf { AppThemePreset.GOLD }

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  appTheme: AppThemePreset = ThemeStateHolder.currentTheme,
  content: @Composable () -> Unit,
) {
  val currentScheme = darkColorScheme(
    primary = appTheme.primaryColor,
    onPrimary = Color(0xFF000000),
    primaryContainer = appTheme.primaryColor.copy(alpha = 0.2f),
    onPrimaryContainer = appTheme.primaryColor,
    secondary = appTheme.glowColor,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E1E28),
    onSecondaryContainer = Color(0xFFE0E0E0),
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

  MaterialTheme(
    colorScheme = currentScheme,
    typography = Typography,
  ) {
    CompositionLocalProvider(
      LocalAppTheme provides appTheme,
      LocalTextStyle provides TextStyle(
        fontFamily = AppFontFamily,
        color = DarkTextPrimary
      ),
      content = content
    )
  }
}


