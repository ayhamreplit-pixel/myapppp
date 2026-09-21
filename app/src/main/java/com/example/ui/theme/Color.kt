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

// Pre-built reusable Brushes
object TodGradients {
  val GoldAccent = Brush.horizontalGradient(
    listOf(Color(0xFFFDB913), Color(0xFFFFCE40))
  )
  val GoldButton = Brush.horizontalGradient(
    listOf(Color(0xFFFDB913), Color(0xFFFFCF33))
  )
  val CyanAccent = Brush.horizontalGradient(
    listOf(Color(0xFF00E5FF), Color(0xFF0284C7))
  )
  val LiveRed = Brush.horizontalGradient(
    listOf(Color(0xFFE50914), Color(0xFFFF3845))
  )
  val CardGlass = Brush.verticalGradient(
    listOf(Color(0xFF1A1A22), Color(0xFF101015))
  )
  val HeaderGlass = Brush.verticalGradient(
    listOf(Color(0xF00A0A0E), Color(0xDD000000))
  )
  val HeroScrim = Brush.verticalGradient(
    listOf(Color.Transparent, Color(0x77000000), Color(0xFF000000))
  )
  val SportsPurple = Brush.verticalGradient(
    listOf(Color(0xFF2D0A4E), Color(0xFF11031F))
  )
  val SportsRed = Brush.verticalGradient(
    listOf(Color(0xFF4A0E17), Color(0xFF1A0307))
  )
  val SportsGreen = Brush.verticalGradient(
    listOf(Color(0xFF063A26), Color(0xFF02160E))
  )
  val ActiveTab = Brush.horizontalGradient(
    listOf(Color(0xFFFDB913), Color(0xFFFFCE40))
  )
  val InactiveTab = Brush.horizontalGradient(
    listOf(Color(0xFF14141A), Color(0xFF0E0E12))
  )
  val BorderGold = Brush.linearGradient(
    listOf(Color(0x99FDB913), Color(0x22FDB913))
  )
}


