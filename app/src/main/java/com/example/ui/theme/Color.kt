package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Accents (Yellow Gold Identity for Jawwy IPTV)
val TodGold = Color(0xFFFDB913)
val TodGoldGlow = Color(0xFFFFCF33)
val TodGoldDark = Color(0xFFE5A50D)
val TodAmberYellow = Color(0xFFFDB913)
val TodAmberLight = Color(0xFFFFCE40)
val TodCyan = Color(0xFF00E5FF)
val TodCyanGlow = Color(0xFF38EFFF)
val TodViolet = Color(0xFF8B5CF6)
val TodPink = Color(0xFFEC4899)
val TodLiveRed = Color(0xFFE50914)
val TodLiveRedLight = Color(0xFFFF3845)
val TodGreen = Color(0xFF10B981)
val TodGreenLight = Color(0xFF34D399)

// Dark Luxury Canvas Surfaces (Pure AMOLED Black Canvas)
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF0A0A0E)
val DarkSurfaceElevated = Color(0xFF14141A)
val DarkSurfaceHigh = Color(0xFF1E1E26)
val DarkSurfaceCard = Color(0xFF131318)
val DarkSurfaceBorder = Color(0xFF262632)
val DarkBorderHighlight = Color(0xFF38384A)
val TodButtonGrey = Color(0xFF202028)
val TodBadgeGrey = Color(0xFF16161D)

// Typography Palette
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFF9E9EA7)
val DarkTextTertiary = Color(0xFF6B6B75)

// Gradients
val PremiumGoldGradStart = Color(0xFFFDB913)
val PremiumGoldGradEnd = Color(0xFFE5A50D)

val OverlayBg = Color(0xEE000000)
val OverlayControlBg = Color(0xCC0A0A0E)

// Pre-built reusable Luxury Brushes
object TodGradients {
  val ObsidianCanvas = Brush.verticalGradient(
    listOf(Color(0xFF0C0C12), Color(0xFF040407), Color(0xFF000000))
  )

  val LiquidGold = Brush.linearGradient(
    listOf(Color(0xFFFFD54F), Color(0xFFF59E0B), Color(0xFFD97706))
  )

  val GoldAccent = Brush.horizontalGradient(
    listOf(Color(0xFFFDB913), Color(0xFFFFCE40))
  )
  val GoldButton = Brush.horizontalGradient(
    listOf(Color(0xFFFFD54F), Color(0xFFFDB913), Color(0xFFE5A50D))
  )
  val CyanAccent = Brush.horizontalGradient(
    listOf(Color(0xFF00E5FF), Color(0xFF0284C7))
  )
  val LiveRed = Brush.horizontalGradient(
    listOf(Color(0xFFFF1744), Color(0xFFD50000))
  )
  val CardGlass = Brush.verticalGradient(
    listOf(Color(0x28FFFFFF), Color(0x14FFFFFF), Color(0x0C121624))
  )
  val SpecularCardBorder = Brush.verticalGradient(
    listOf(Color(0x55FFFFFF), Color(0x20FFFFFF), Color(0x0AFFFFFF))
  )
  val SpecularGlowBorder = Brush.verticalGradient(
    listOf(Color(0xAAFDB913), Color(0x33FDB913), Color(0x00000000))
  )
  val HeaderGlass = Brush.verticalGradient(
    listOf(Color(0xF508080E), Color(0xDD020204), Color(0x00000000))
  )
  val HeroScrim = Brush.verticalGradient(
    listOf(Color.Transparent, Color(0x55000000), Color(0xDD000000), Color(0xFF000000))
  )
  val PlayerTopVignette = Brush.verticalGradient(
    listOf(Color(0xCC000000), Color(0x66000000), Color.Transparent)
  )
  val PlayerBottomVignette = Brush.verticalGradient(
    listOf(Color.Transparent, Color(0x66000000), Color(0xEE000000))
  )
  val SportsPurple = Brush.verticalGradient(
    listOf(Color(0xFF3B0764), Color(0xFF18022B))
  )
  val SportsRed = Brush.verticalGradient(
    listOf(Color(0xFF4A0E17), Color(0xFF1A0307))
  )
  val SportsGreen = Brush.verticalGradient(
    listOf(Color(0xFF064E3B), Color(0xFF021B14))
  )
  val ActiveTab = Brush.horizontalGradient(
    listOf(Color(0xFFFFD54F), Color(0xFFFDB913))
  )
  val InactiveTab = Brush.horizontalGradient(
    listOf(Color(0xFF14141A), Color(0xFF0E0E12))
  )
  val BorderGold = Brush.linearGradient(
    listOf(Color(0x99FDB913), Color(0x22FDB913))
  )

  // Modern Apple iOS Ultra-Glass Materials & Gradients
  val IosCanvasBg = Brush.verticalGradient(
    listOf(Color(0xFF0D0E15), Color(0xFF07080D), Color(0xFF000000))
  )
  val IosGlassCard = Brush.verticalGradient(
    listOf(Color(0x2EFFFFFF), Color(0x14FFFFFF))
  )
  val IosGlassCardElevated = Brush.verticalGradient(
    listOf(Color(0x3DFFFFFF), Color(0x1EFFFFFF))
  )
  val IosGlassBorder = Brush.verticalGradient(
    listOf(Color(0x55FFFFFF), Color(0x1AFFFFFF), Color(0x0AFFFFFF))
  )
  val IosAccentGradient = Brush.horizontalGradient(
    listOf(Color(0xFF0A84FF), Color(0xFF2997FF))
  )
  val IosGoldAccentGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFFD60A), Color(0xFFFF9F0A))
  )
  val IosHeroGlow = Brush.radialGradient(
    listOf(Color(0x350A84FF), Color(0x155E5CE6), Color.Transparent)
  )
}

// iOS System Colors (Dark Mode Spec)
val IosSystemBlue = Color(0xFF0A84FF)
val IosSystemBlueLight = Color(0xFF2997FF)
val IosSystemIndigo = Color(0xFF5E5CE6)
val IosSystemPurple = Color(0xFFBF5AF2)
val IosSystemPink = Color(0xFFFF375F)
val IosSystemRed = Color(0xFFFF453A)
val IosSystemOrange = Color(0xFFFF9F0A)
val IosSystemYellow = Color(0xFFFFD60A)
val IosSystemGreen = Color(0xFF30D158)
val IosSystemMint = Color(0xFF63E6E2)
val IosSystemTeal = Color(0xFF64D2FF)
val IosSystemCyan = Color(0xFF70D7FF)

// iOS Dark Mode Materials & Backgrounds
val IosDarkBackground = Color(0xFF000000)
val IosDarkSecondaryBackground = Color(0xFF1C1C1E)
val IosDarkTertiaryBackground = Color(0xFF2C2C2E)
val IosDarkGroupedBackground = Color(0xFF000000)
val IosDarkSecondaryGroupedBackground = Color(0xFF1C1C1E)
val IosDarkTertiaryGroupedBackground = Color(0xFF2C2C2E)

// iOS Glass Materials
val IosGlassUltraThin = Color(0x14FFFFFF)
val IosGlassThin = Color(0x24FFFFFF)
val IosGlassRegular = Color(0x33FFFFFF)
val IosGlassThick = Color(0x45FFFFFF)
val IosGlassBorderColor = Color(0x33FFFFFF)
val IosGlassBorderSubtle = Color(0x1AFFFFFF)

// iOS Labels
val IosLabelPrimary = Color(0xFFFFFFFF)
val IosLabelSecondary = Color(0x99EBEBF5)
val IosLabelTertiary = Color(0x4DEBEBF5)
val IosLabelQuaternary = Color(0x29EBEBF5)
val IosSeparator = Color(0x33545458)
val IosOpaqueSeparator = Color(0xFF38383A)


