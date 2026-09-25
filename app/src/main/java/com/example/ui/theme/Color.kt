package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Accents (Pure Apple iOS Harmonic Liquid Glass Palette)
val TodGold = Color(0xFF0A84FF) // Replaced yellow with Apple Electric Blue
val TodGoldGlow = Color(0xFF64D2FF)
val TodGoldDark = Color(0xFF0055D4)
val TodAmberYellow = Color(0xFF0A84FF)
val TodAmberLight = Color(0xFF64D2FF)
val TodCyan = Color(0xFF00F0FF)
val TodCyanGlow = Color(0xFF70D7FF)
val TodViolet = Color(0xFFA855F7)
val TodPink = Color(0xFFFF375F)
val TodLiveRed = Color(0xFFFF453A)
val TodLiveRedLight = Color(0xFFFF6961)
val TodGreen = Color(0xFF30D158)
val TodGreenLight = Color(0xFF63E6E2)

// Transparent / Translucent Canvas Surfaces
val DarkBg = Color(0xFF0A1024)
val DarkSurface = Color(0x2EFFFFFF)
val DarkSurfaceElevated = Color(0x45FFFFFF)
val DarkSurfaceHigh = Color(0x5AFFFFFF)
val DarkSurfaceCard = Color(0x35FFFFFF)
val DarkSurfaceBorder = Color(0x45FFFFFF)
val DarkBorderHighlight = Color(0x88FFFFFF)
val TodButtonGrey = Color(0x30FFFFFF)
val TodBadgeGrey = Color(0x28FFFFFF)

// Typography Palette (High contrast crystal white)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xCCFFFFFF)
val DarkTextTertiary = Color(0x88FFFFFF)

// Gradients
val PremiumGoldGradStart = Color(0xFF0A84FF)
val PremiumGoldGradEnd = Color(0xFF0055D4)

val OverlayBg = Color(0x990A1224)
val OverlayControlBg = Color(0x33FFFFFF)

// Pre-built reusable Liquid Glass Brushes
object TodGradients {
  val ObsidianCanvas = Brush.verticalGradient(
    listOf(Color(0xFF0A1024), Color(0xFF120C28), Color(0xFF080C1C))
  )

  val LiquidGold = Brush.linearGradient(
    listOf(Color(0xFF64D2FF), Color(0xFF0A84FF), Color(0xFF0055D4))
  )

  val GoldAccent = Brush.horizontalGradient(
    listOf(Color(0xFF0A84FF), Color(0xFF64D2FF))
  )
  val GoldButton = Brush.horizontalGradient(
    listOf(Color(0xFF64D2FF), Color(0xFF0A84FF), Color(0xFF0055D4))
  )
  val CyanAccent = Brush.horizontalGradient(
    listOf(Color(0xFF00F0FF), Color(0xFF0A84FF))
  )
  val LiveRed = Brush.horizontalGradient(
    listOf(Color(0xFFFF453A), Color(0xFFD61A24))
  )
  val CardGlass = Brush.verticalGradient(
    listOf(Color(0x38FFFFFF), Color(0x1CFFFFFF), Color(0x10FFFFFF))
  )
  val SpecularCardBorder = Brush.verticalGradient(
    listOf(Color(0xCCFFFFFF), Color(0x40FFFFFF), Color(0x18FFFFFF), Color(0x35FFFFFF))
  )
  val SpecularGlowBorder = Brush.verticalGradient(
    listOf(Color(0xEE0A84FF), Color(0x550A84FF), Color(0x20FFFFFF))
  )
  val HeaderGlass = Brush.verticalGradient(
    listOf(Color(0x40FFFFFF), Color(0x20FFFFFF), Color(0x00FFFFFF))
  )
  val HeroScrim = Brush.verticalGradient(
    listOf(Color(0x000A1024), Color(0x200A1024), Color(0x800A1024))
  )
  val PlayerTopVignette = Brush.verticalGradient(
    listOf(Color(0x80000000), Color(0x30000000), Color(0x00000000))
  )
  val PlayerBottomVignette = Brush.verticalGradient(
    listOf(Color(0x00000000), Color(0x40000000), Color(0x90000000))
  )
  val SportsPurple = Brush.verticalGradient(
    listOf(Color(0x80A855F7), Color(0x30A855F7))
  )
  val SportsRed = Brush.verticalGradient(
    listOf(Color(0x80FF375F), Color(0x30FF375F))
  )
  val SportsGreen = Brush.verticalGradient(
    listOf(Color(0x8030D158), Color(0x3030D158))
  )
  val ActiveTab = Brush.horizontalGradient(
    listOf(Color(0xFF64D2FF), Color(0xFF0A84FF))
  )
  val InactiveTab = Brush.horizontalGradient(
    listOf(Color(0x20FFFFFF), Color(0x10FFFFFF))
  )
  val BorderGold = Brush.linearGradient(
    listOf(Color(0xCC0A84FF), Color(0x440A84FF))
  )

  // Modern Apple iOS Ultra-Glass Materials & Gradients
  val IosCanvasBg = Brush.verticalGradient(
    listOf(Color(0xFF0A1024), Color(0xFF120C28), Color(0xFF080C1C))
  )
  val IosGlassCard = Brush.verticalGradient(
    listOf(Color(0x38FFFFFF), Color(0x1CFFFFFF), Color(0x10FFFFFF))
  )
  val IosGlassCardElevated = Brush.verticalGradient(
    listOf(Color(0x50FFFFFF), Color(0x2AFFFFFF), Color(0x18FFFFFF))
  )
  val IosGlassBorder = Brush.verticalGradient(
    listOf(Color(0xCCFFFFFF), Color(0x40FFFFFF), Color(0x18FFFFFF), Color(0x35FFFFFF))
  )
  val IosAccentGradient = Brush.horizontalGradient(
    listOf(Color(0xFF0A84FF), Color(0xFF00F0FF))
  )
  val IosGoldAccentGradient = Brush.horizontalGradient(
    listOf(Color(0xFF64D2FF), Color(0xFF0A84FF))
  )
  val IosHeroGlow = Brush.radialGradient(
    listOf(Color(0x550A84FF), Color(0x25A855F7), Color(0x00A855F7))
  )
}

// iOS System Colors (Harmonious Apple Spec)
val IosSystemBlue = Color(0xFF0A84FF)
val IosSystemBlueLight = Color(0xFF64D2FF)
val IosSystemIndigo = Color(0xFF5E5CE6)
val IosSystemPurple = Color(0xFFA855F7)
val IosSystemPink = Color(0xFFFF375F)
val IosSystemRed = Color(0xFFFF453A)
val IosSystemOrange = Color(0xFFFF9F0A)
val IosSystemYellow = Color(0xFF0A84FF) // Harmonious blue
val IosSystemGreen = Color(0xFF30D158)
val IosSystemMint = Color(0xFF63E6E2)
val IosSystemTeal = Color(0xFF64D2FF)
val IosSystemCyan = Color(0xFF70D7FF)

// iOS Glass Materials
val IosGlassUltraThin = Color(0x1EFFFFFF)
val IosGlassThin = Color(0x2EFFFFFF)
val IosGlassRegular = Color(0x3EFFFFFF)
val IosGlassThick = Color(0x55FFFFFF)
val IosGlassBorderColor = Color(0x55FFFFFF)
val IosGlassBorderSubtle = Color(0x25FFFFFF)

// iOS Labels
val IosLabelPrimary = Color(0xFFFFFFFF)
val IosLabelSecondary = Color(0xCCFFFFFF)
val IosLabelTertiary = Color(0x88FFFFFF)
val IosLabelQuaternary = Color(0x4DFFFFFF)
val IosSeparator = Color(0x33FFFFFF)
val IosOpaqueSeparator = Color(0x4DFFFFFF)
