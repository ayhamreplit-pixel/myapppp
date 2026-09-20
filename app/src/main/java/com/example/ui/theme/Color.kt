package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Accents (Unified with Player)
val TodCyan = Color(0xFF00E5FF)
val TodCyanGlow = Color(0xFF38EFFF)
val TodGold = Color(0xFFF5A623)
val TodGoldGlow = Color(0xFFFFB800)
val TodAmberYellow = Color(0xFFF5A623)
val TodAmberLight = Color(0xFFFFC72C)
val TodViolet = Color(0xFF8B5CF6)
val TodPink = Color(0xFFEC4899)
val TodLiveRed = Color(0xFFFF2A55)
val TodLiveRedLight = Color(0xFFFF5376)
val TodGreen = Color(0xFF10B981)
val TodGreenLight = Color(0xFF34D399)

// Dark Luxury Canvas Surfaces
val DarkBg = Color(0xFF080B11)
val DarkSurface = Color(0xFF0E131E)
val DarkSurfaceElevated = Color(0xFF141A29)
val DarkSurfaceHigh = Color(0xFF1B2337)
val DarkSurfaceCard = Color(0xFF121724)
val DarkSurfaceBorder = Color(0xFF222C42)
val DarkBorderHighlight = Color(0xFF313F5E)

// Typography Palette
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextTertiary = Color(0xFF64748B)

// Gradients
val CyberNeonBlue = Color(0xFF00E5FF)
val CyberElectricIndigo = Color(0xFF6366F1)
val PremiumGoldGradStart = Color(0xFFFFC72C)
val PremiumGoldGradEnd = Color(0xFFE08A00)

val OverlayBg = Color(0xDD080B11)
val OverlayControlBg = Color(0x990E131E)

// Pre-built reusable Brushes
object TodGradients {
  val GoldAccent = Brush.horizontalGradient(
    listOf(Color(0xFFFFC72C), Color(0xFFF5A623))
  )
  val CyanAccent = Brush.horizontalGradient(
    listOf(Color(0xFF00E5FF), Color(0xFF0284C7))
  )
  val LiveRed = Brush.horizontalGradient(
    listOf(Color(0xFFFF2A55), Color(0xFFFF5376))
  )
  val CardGlass = Brush.verticalGradient(
    listOf(Color(0xFF182032), Color(0xFF0F1522))
  )
  val HeaderGlass = Brush.verticalGradient(
    listOf(Color(0xFF141B2B), Color(0xFF0A0E17))
  )
  val ActiveTab = Brush.horizontalGradient(
    listOf(Color(0xFFFFC72C), Color(0xFFF5A623))
  )
  val InactiveTab = Brush.horizontalGradient(
    listOf(Color(0xFF141A29), Color(0xFF101522))
  )
  val BorderGold = Brush.linearGradient(
    listOf(Color(0x80FFC72C), Color(0x20F5A623))
  )
}


