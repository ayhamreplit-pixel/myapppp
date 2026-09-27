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
  val gradient: Brush,
  val bgGradientColors: List<Color>,
  val orb1Colors: List<Color>,
  val orb2Colors: List<Color>,
  val orb3Colors: List<Color>,
  val orb4Colors: List<Color>
) {
  SAPPHIRE(
    id = "sapphire",
    titleAr = "أزرق ملكي سافاير",
    subtitleAr = "النمط الملكي الأنيق مع إضاءة نيلية وسماوية",
    primaryColor = Color(0xFF007AFF),
    glowColor = Color(0xFF00F0FF),
    gradient = Brush.horizontalGradient(listOf(Color(0xFF007AFF), Color(0xFF00F0FF))),
    bgGradientColors = listOf(Color(0xFF0C142A), Color(0xFF130D2E), Color(0xFF090E20)),
    orb1Colors = listOf(Color(0xFF007AFF), Color(0xFF00F0FF)),
    orb2Colors = listOf(Color(0xFF5E5CE6), Color(0xFFA855F7)),
    orb3Colors = listOf(Color(0xFF0055D4), Color(0xFF64D2FF)),
    orb4Colors = listOf(Color(0xFF06B6D4), Color(0xFF0284C7))
  ),
  CYAN(
    id = "cyan",
    titleAr = "أزرق سماوي نيون",
    subtitleAr = "إضاءة نيون سايبربانك رياضية عصرية",
    primaryColor = Color(0xFF00F0FF),
    glowColor = Color(0xFF38EFFF),
    gradient = Brush.horizontalGradient(listOf(Color(0xFF00F0FF), Color(0xFF0284C7))),
    bgGradientColors = listOf(Color(0xFF061826), Color(0xFF0B2133), Color(0xFF04101A)),
    orb1Colors = listOf(Color(0xFF00F0FF), Color(0xFF00C7BE)),
    orb2Colors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
    orb3Colors = listOf(Color(0xFF06B6D4), Color(0xFF0891B2)),
    orb4Colors = listOf(Color(0xFF14B8A6), Color(0xFF0D9488))
  ),
  EMERALD(
    id = "emerald",
    titleAr = "زمردي رياضي فاخر",
    subtitleAr = "أجواء عشب الملاعب الخضراء الفاخرة",
    primaryColor = Color(0xFF30D158),
    glowColor = Color(0xFF34D399),
    gradient = Brush.horizontalGradient(listOf(Color(0xFF30D158), Color(0xFF059669))),
    bgGradientColors = listOf(Color(0xFF071F14), Color(0xFF0A2B1C), Color(0xFF05140D)),
    orb1Colors = listOf(Color(0xFF30D158), Color(0xFF10B981)),
    orb2Colors = listOf(Color(0xFF059669), Color(0xFF34D399)),
    orb3Colors = listOf(Color(0xFF10B981), Color(0xFF6EE7B7)),
    orb4Colors = listOf(Color(0xFF047857), Color(0xFF065F46))
  ),
  PURPLE(
    id = "purple",
    titleAr = "أرجواني ملكي إمبراطوري",
    subtitleAr = "فخامة سينمائية أرجوانية ساحرة",
    primaryColor = Color(0xFFA855F7),
    glowColor = Color(0xFFC084FC),
    gradient = Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED))),
    bgGradientColors = listOf(Color(0xFF1D0B2E), Color(0xFF280F3E), Color(0xFF12071C)),
    orb1Colors = listOf(Color(0xFFA855F7), Color(0xFFEC4899)),
    orb2Colors = listOf(Color(0xFF9333EA), Color(0xFFD946EF)),
    orb3Colors = listOf(Color(0xFF7C3AED), Color(0xFFC084FC)),
    orb4Colors = listOf(Color(0xFF6B21A8), Color(0xFF4C1D95))
  ),
  GOLD(
    id = "gold",
    titleAr = "ذهبي سائل كهرماني",
    subtitleAr = "الثيم الأصلي الملكي المتألق بالذهب والكهرمان",
    primaryColor = Color(0xFFFFB800),
    glowColor = Color(0xFFFFCE40),
    gradient = Brush.horizontalGradient(listOf(Color(0xFFFFB800), Color(0xFFFF9500))),
    bgGradientColors = listOf(Color(0xFF241604), Color(0xFF321E06), Color(0xFF160E02)),
    orb1Colors = listOf(Color(0xFFFFB800), Color(0xFFFF9500)),
    orb2Colors = listOf(Color(0xFFD97706), Color(0xFFF59E0B)),
    orb3Colors = listOf(Color(0xFFFDB913), Color(0xFFFFE082)),
    orb4Colors = listOf(Color(0xFFB45309), Color(0xFF92400E))
  ),
  RED(
    id = "red",
    titleAr = "قرمزي ياقوتي فخم",
    subtitleAr = "حماس المباريات والبطولات الكبرى بلون الياقوت",
    primaryColor = Color(0xFFFF375F),
    glowColor = Color(0xFFFF453A),
    gradient = Brush.horizontalGradient(listOf(Color(0xFFFF375F), Color(0xFFFF453A))),
    bgGradientColors = listOf(Color(0xFF280812), Color(0xFF380C1B), Color(0xFF18050B)),
    orb1Colors = listOf(Color(0xFFFF375F), Color(0xFFFF453A)),
    orb2Colors = listOf(Color(0xFFE11D48), Color(0xFFFB7185)),
    orb3Colors = listOf(Color(0xFFBE123C), Color(0xFFFDA4AF)),
    orb4Colors = listOf(Color(0xFF9F1239), Color(0xFF881337))
  ),
  AMOLED_SILVER(
    id = "silver",
    titleAr = "تيتانيوم فضي أبل",
    subtitleAr = "تباين مطلق وفخامة التيتانيوم المصقول",
    primaryColor = Color(0xFFE2E8F0),
    glowColor = Color(0xFFFFFFFF),
    gradient = Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))),
    bgGradientColors = listOf(Color(0xFF10141D), Color(0xFF181F2C), Color(0xFF0B0E14)),
    orb1Colors = listOf(Color(0xFF94A3B8), Color(0xFFE2E8F0)),
    orb2Colors = listOf(Color(0xFF64748B), Color(0xFFCBD5E1)),
    orb3Colors = listOf(Color(0xFF475569), Color(0xFF94A3B8)),
    orb4Colors = listOf(Color(0xFF334155), Color(0xFF64748B))
  ),
  NEON_LIME(
    id = "neon_lime",
    titleAr = "أخضر نيون سايبر",
    subtitleAr = "وهج نيون مستقبلي بطاقة حيوية فائقة وحماس رياضي",
    primaryColor = Color(0xFF00FF66),
    glowColor = Color(0xFF39FF14),
    gradient = Brush.horizontalGradient(listOf(Color(0xFF00FF66), Color(0xFF00E5FF))),
    bgGradientColors = listOf(Color(0xFF041A0E), Color(0xFF072A18), Color(0xFF021008)),
    orb1Colors = listOf(Color(0xFF00FF66), Color(0xFF39FF14)),
    orb2Colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4)),
    orb3Colors = listOf(Color(0xFF22C55E), Color(0xFF84CC16)),
    orb4Colors = listOf(Color(0xFF059669), Color(0xFF10B981))
  ),
  SUNSET_ORANGE(
    id = "sunset_orange",
    titleAr = "برتقالي الغروب الناري",
    subtitleAr = "حرارة المباريات ولهيب البطولات الساخنة",
    primaryColor = Color(0xFFFF5E00),
    glowColor = Color(0xFFFF9500),
    gradient = Brush.horizontalGradient(listOf(Color(0xFFFF5E00), Color(0xFFFF9500))),
    bgGradientColors = listOf(Color(0xFF260D04), Color(0xFF3A1506), Color(0xFF180702)),
    orb1Colors = listOf(Color(0xFFFF5E00), Color(0xFFFF9500)),
    orb2Colors = listOf(Color(0xFFEF4444), Color(0xFFF97316)),
    orb3Colors = listOf(Color(0xFFEA580C), Color(0xFFFBBF24)),
    orb4Colors = listOf(Color(0xFFC2410C), Color(0xFF9A3412))
  );

  companion object {
    fun fromId(id: String): AppThemePreset {
      return entries.find { it.id.equals(id, ignoreCase = true) } ?: SAPPHIRE
    }

    fun fromNameOrId(query: String): AppThemePreset {
      return entries.find {
        it.id.equals(query, ignoreCase = true) ||
        it.titleAr.equals(query, ignoreCase = true) ||
        query.contains(it.id, ignoreCase = true) ||
        it.titleAr.contains(query, ignoreCase = true)
      } ?: SAPPHIRE
    }
  }
}

enum class AppFontPreset(
  val id: String,
  val titleAr: String,
  val descriptionAr: String,
  val fontFamily: androidx.compose.ui.text.font.FontFamily
) {
  THMANYAH(
    id = "thmanyah",
    titleAr = "خط ثمانية الرسمي",
    descriptionAr = "الخط العربي الأصلي المعتمد لجميع صفحات ونصوص التطبيق",
    fontFamily = ThmanyahFontFamily
  );

  companion object {
    fun fromId(id: String): AppFontPreset {
      return THMANYAH
    }
  }
}

object ThemeStateHolder {
  var currentTheme by mutableStateOf(AppThemePreset.SAPPHIRE)
}

object FontStateHolder {
  var currentFont by mutableStateOf(AppFontPreset.THMANYAH)
}

val LocalAppTheme = compositionLocalOf { AppThemePreset.SAPPHIRE }
val LocalAppFont = compositionLocalOf { AppFontPreset.THMANYAH }

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  appTheme: AppThemePreset = ThemeStateHolder.currentTheme,
  appFont: AppFontPreset = FontStateHolder.currentFont,
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
    typography = getTypography(appFont.fontFamily),
  ) {
    CompositionLocalProvider(
      LocalAppTheme provides appTheme,
      LocalAppFont provides appFont,
      LocalTextStyle provides TextStyle(
        fontFamily = appFont.fontFamily,
        color = DarkTextPrimary
      ),
      content = content
    )
  }
}


