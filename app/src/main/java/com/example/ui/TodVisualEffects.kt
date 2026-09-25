package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodLiveRed
import com.example.ui.theme.TodLiveRedLight

/**
 * Apple iOS Authentic Liquid Glass System
 * Pure translucent frosted glass with specular reflections and optical refraction borders
 */
object LiquidGlassTheme {
  // Specular Glare Colors (Subtle & Refined, eliminating harsh glaring white)
  val SpecularHighlightTop = Color(0x60FFFFFF)
  val SpecularHighlightMid = Color(0x20FFFFFF)
  val SpecularHighlightBot = Color(0x08FFFFFF)

  // Crystal Clear Obsidian-Sapphire Liquid Glass Surfaces (Deep, translucent, comfortable dark theme)
  val LiquidSurfaceRegular = listOf(
    Color(0x3519233C),
    Color(0x22121B2E),
    Color(0x140B1220)
  )

  val LiquidSurfaceElevated = listOf(
    Color(0x45223050),
    Color(0x30182440),
    Color(0x1E0F182C)
  )

  val LiquidSurfaceUltraClear = listOf(
    Color(0x25141E34),
    Color(0x180D1526),
    Color(0x0C080E1A)
  )

  val LiquidSurfaceActive = listOf(
    Color(0x550A84FF),
    Color(0x300055D4),
    Color(0x1A002D75)
  )

  // Tinted Glass Surfaces
  val LiquidSurfaceTintedBlue = listOf(
    Color(0x450A84FF),
    Color(0x250055D4),
    Color(0x14002D75)
  )

  val LiquidSurfaceTintedGold = listOf(
    Color(0x45FDB913),
    Color(0x25D97706),
    Color(0x1478350F)
  )

  val LiquidSurfaceTintedPurple = listOf(
    Color(0x45BF5AF2),
    Color(0x257A24A6),
    Color(0x144C1D95)
  )

  val LiquidSurfaceTintedEmerald = listOf(
    Color(0x4530D158),
    Color(0x2510B981),
    Color(0x14064E3B)
  )

  val LiquidSurfaceTintedRose = listOf(
    Color(0x45FF375F),
    Color(0x25E11D48),
    Color(0x14881337)
  )

  // Optical Specular Gradient Borders (Refined neon rim with soft metallic highlight)
  val LiquidSpecularBorder = Brush.verticalGradient(
    listOf(
      Color(0x65FFFFFF),
      Color(0x2564D2FF),
      Color(0x10FFFFFF),
      Color(0x250A84FF)
    )
  )

  val LiquidGoldBorder = Brush.verticalGradient(
    listOf(
      Color(0xFFFFE082),
      Color(0x99FDB913),
      Color(0x33FDB913),
      Color(0x60FFFFFF)
    )
  )

  val LiquidBlueBorder = Brush.verticalGradient(
    listOf(
      Color(0xFF90CAF9),
      Color(0x990A84FF),
      Color(0x330A84FF),
      Color(0x60FFFFFF)
    )
  )

  val LiquidPurpleBorder = Brush.verticalGradient(
    listOf(
      Color(0xFFE1BEE7),
      Color(0x99BF5AF2),
      Color(0x33BF5AF2),
      Color(0x60FFFFFF)
    )
  )

  val LiquidGreenBorder = Brush.verticalGradient(
    listOf(
      Color(0xFFA7F3D0),
      Color(0x9930D158),
      Color(0x3330D158),
      Color(0x60FFFFFF)
    )
  )
}

/**
 * Fluid Mesh Dynamic Living Gradient Background (Apple iOS 18 Mesh Aura)
 * Breathtaking luminous mesh background with animated flowing orbs
 * (Cobalt Blue, Neon Violet, Magenta, Sunset Gold, Mint Emerald)
 * that illuminate and shine through the translucent Liquid Glass cards and menus.
 */
@Composable
fun FluidMeshBackground(
  modifier: Modifier = Modifier,
  ambientAlpha: Float = 0.65f,
  content: @Composable BoxScope.() -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "fluidMesh")
  val orb1Offset by infiniteTransition.animateFloat(
    initialValue = -50f,
    targetValue = 70f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 8500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb1Offset"
  )
  val orb2Offset by infiniteTransition.animateFloat(
    initialValue = 60f,
    targetValue = -60f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb2Offset"
  )
  val orb3Scale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb3Scale"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF0C142A), // Deep vibrant sapphire indigo
            Color(0xFF130D2E), // Deep luxury violet
            Color(0xFF090E20)  // Midnight blue
          )
        )
      )
      .drawBehind {
        val w = size.width
        val h = size.height

        // 1. Glowing Fluid Orb 1: Electric Blue & Vivid Cyan (Top-Left / Header region)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF007AFF).copy(alpha = (ambientAlpha * 0.85f).coerceIn(0f, 1f)),
              Color(0xFF00F0FF).copy(alpha = (ambientAlpha * 0.45f).coerceIn(0f, 1f)),
              Color.Transparent
            ),
            center = Offset(w * 0.20f + orb1Offset, h * 0.15f + orb2Offset * 0.5f),
            radius = w * 0.85f
          ),
          center = Offset(w * 0.20f + orb1Offset, h * 0.15f + orb2Offset * 0.5f),
          radius = w * 0.85f
        )

        // 2. Glowing Fluid Orb 2: Royal Violet & Vivid Magenta (Middle-Right)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFA855F7).copy(alpha = (ambientAlpha * 0.75f).coerceIn(0f, 1f)),
              Color(0xFFEC4899).copy(alpha = (ambientAlpha * 0.40f).coerceIn(0f, 1f)),
              Color.Transparent
            ),
            center = Offset(w * 0.85f + orb2Offset, h * 0.42f + orb1Offset),
            radius = w * 0.80f * orb3Scale
          ),
          center = Offset(w * 0.85f + orb2Offset, h * 0.42f + orb1Offset),
          radius = w * 0.80f * orb3Scale
        )

        // 3. Glowing Fluid Orb 3: Radiant Amber & Golden Sunset (Bottom-Left)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFB800).copy(alpha = (ambientAlpha * 0.55f).coerceIn(0f, 1f)),
              Color(0xFFFF6B6B).copy(alpha = (ambientAlpha * 0.30f).coerceIn(0f, 1f)),
              Color.Transparent
            ),
            center = Offset(w * 0.15f - orb1Offset * 0.6f, h * 0.75f + orb2Offset * 0.4f),
            radius = w * 0.75f
          ),
          center = Offset(w * 0.15f - orb1Offset * 0.6f, h * 0.75f + orb2Offset * 0.4f),
          radius = w * 0.75f
        )

        // 4. Glowing Fluid Orb 4: Emerald Mint & Cyan Glow (Bottom-Right / Nav region)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF10B981).copy(alpha = (ambientAlpha * 0.50f).coerceIn(0f, 1f)),
              Color(0xFF06B6D4).copy(alpha = (ambientAlpha * 0.25f).coerceIn(0f, 1f)),
              Color.Transparent
            ),
            center = Offset(w * 0.80f - orb2Offset * 0.5f, h * 0.88f),
            radius = w * 0.70f
          ),
          center = Offset(w * 0.80f - orb2Offset * 0.5f, h * 0.88f),
          radius = w * 0.70f
        )
      }
  ) {
    content()
  }
}

/**
 * Liquid Glass Modifier with Specular Light Reflection Sheen and Optical Refraction Rim
 */
fun Modifier.liquidGlassEffect(
  shape: Shape = RoundedCornerShape(22.dp),
  isElevated: Boolean = false,
  glowTint: Color? = Color(0xFF0A84FF),
  glassColor: Color? = null,
  borderBrush: Brush? = null,
  showTopGlare: Boolean = true
): Modifier = composed {
  val effectiveGlowTint = glowTint ?: Color(0xFF0A84FF)
  val surfaceColors = when {
    glassColor != null -> listOf(
      glassColor,
      glassColor.copy(alpha = (glassColor.alpha * 0.65f).coerceAtLeast(0.08f)),
      glassColor.copy(alpha = (glassColor.alpha * 0.35f).coerceAtLeast(0.04f))
    )
    else -> listOf(
      effectiveGlowTint.copy(alpha = 0.28f),
      effectiveGlowTint.copy(alpha = 0.14f),
      effectiveGlowTint.copy(alpha = 0.05f)
    )
  }

  val finalBorderBrush = borderBrush ?: when (effectiveGlowTint) {
    TodGold -> LiquidGlassTheme.LiquidGoldBorder
    Color(0xFFBF5AF2) -> LiquidGlassTheme.LiquidPurpleBorder
    Color(0xFF30D158) -> LiquidGlassTheme.LiquidGreenBorder
    else -> LiquidGlassTheme.LiquidBlueBorder
  }

  this
    .clip(shape)
    .background(Brush.verticalGradient(surfaceColors))
    .drawBehind {
      if (showTopGlare) {
        // Curved optical refraction specular highlight arc across the top rim (soft, elegant)
        val glareWidth = size.width
        val glareHeight = (size.height * 0.34f).coerceAtMost(26.dp.toPx())

        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0x40FFFFFF),
              Color(0x12FFFFFF),
              Color.Transparent
            ),
            startY = 0f,
            endY = glareHeight
          ),
          topLeft = Offset(0f, 0f),
          size = Size(glareWidth, glareHeight),
          cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
        )
      }
    }
    .border(width = 1.15.dp, brush = finalBorderBrush, shape = shape)
}

/**
 * Authentic Liquid Glass Container Card with bouncy spring physics
 */
@Composable
fun LiquidGlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(24.dp),
  isElevated: Boolean = false,
  glowTint: Color? = Color(0xFF0A84FF),
  glassColor: Color? = null,
  borderBrush: Brush? = null,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed && onClick != null) 0.96f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "liquidCardScale"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .shadow(
        elevation = if (isElevated) 14.dp else 8.dp,
        shape = shape,
        spotColor = (glowTint ?: Color(0xFF0A84FF)).copy(alpha = 0.40f),
        ambientColor = Color.Transparent
      )
      .liquidGlassEffect(
        shape = shape,
        isElevated = isElevated,
        glowTint = glowTint ?: Color(0xFF0A84FF),
        glassColor = glassColor,
        borderBrush = borderBrush
      )
      .then(
        if (onClick != null) {
          Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
          )
        } else Modifier
      ),
    content = content
  )
}

/**
 * Authentic Liquid Glass Capsule / Action Pill Button
 */
@Composable
fun LiquidGlassPill(
  modifier: Modifier = Modifier,
  isActive: Boolean = false,
  activeTint: Color = TodGold,
  onClick: () -> Unit,
  content: @Composable RowScopeWrapper.() -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.92f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "liquidPillScale"
  )

  val pillShape = RoundedCornerShape(32.dp)

  Box(
    modifier = modifier
      .scale(scale)
      .shadow(
        elevation = if (isActive) 12.dp else 6.dp,
        shape = pillShape,
        spotColor = if (isActive) activeTint.copy(alpha = 0.5f) else Color(0xFF0055D4).copy(alpha = 0.3f)
      )
      .clip(pillShape)
      .background(
        if (isActive) {
          Brush.verticalGradient(
            listOf(
              activeTint.copy(alpha = 0.90f),
              activeTint.copy(alpha = 0.70f),
              activeTint.copy(alpha = 0.50f)
            )
          )
        } else {
          Brush.verticalGradient(LiquidGlassTheme.LiquidSurfaceRegular)
        }
      )
      .drawBehind {
        // Specular top highlight sheen
        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0x99FFFFFF),
              Color(0x25FFFFFF),
              Color.Transparent
            ),
            startY = 0f,
            endY = size.height * 0.45f
          ),
          topLeft = Offset(0f, 0f),
          size = Size(size.width, size.height * 0.45f),
          cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx())
        )
      }
      .border(
        width = 1.15.dp,
        brush = if (isActive) LiquidGlassTheme.LiquidGoldBorder else LiquidGlassTheme.LiquidSpecularBorder,
        shape = pillShape
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 16.dp, vertical = 9.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      RowScopeWrapper(this).content()
    }
  }
}

class RowScopeWrapper(private val rowScope: RowScope) : RowScope by rowScope

/**
 * Custom Bespoke Identity Canvas Vector: Home / Live Cinema Hub
 */
@Composable
fun TodNavHomeIcon(
  isSelected: Boolean,
  primaryColor: Color,
  modifier: Modifier = Modifier.size(24.dp)
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // 1. OLED Cinema Display Bezel
    val screenTop = h * 0.12f
    val screenHeight = h * 0.58f
    val screenWidth = w * 0.76f
    val screenLeft = w * 0.12f

    drawRoundRect(
      color = primaryColor,
      topLeft = Offset(screenLeft, screenTop),
      size = Size(screenWidth, screenHeight),
      cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
      style = Stroke(width = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx())
    )

    // 2. Central Glowing Diamond Crystal Core
    val diamondPath = Path().apply {
      moveTo(w * 0.50f, screenTop + screenHeight * 0.22f)
      lineTo(w * 0.62f, screenTop + screenHeight * 0.50f)
      lineTo(w * 0.50f, screenTop + screenHeight * 0.78f)
      lineTo(w * 0.38f, screenTop + screenHeight * 0.50f)
      close()
    }

    if (isSelected) {
      drawPath(
        path = diamondPath,
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFF64D2FF), Color(0xFF0A84FF)),
          center = Offset(w * 0.50f, screenTop + screenHeight * 0.50f),
          radius = screenWidth * 0.35f
        )
      )
    } else {
      drawPath(
        path = diamondPath,
        color = primaryColor.copy(alpha = 0.65f),
        style = Stroke(width = 1.3.dp.toPx())
      )
    }

    // 3. Curved TV Base Pedestal
    val standY = screenTop + screenHeight
    drawLine(
      color = primaryColor,
      start = Offset(w * 0.50f, standY),
      end = Offset(w * 0.50f, h * 0.86f),
      strokeWidth = 1.8.dp.toPx(),
      cap = StrokeCap.Round
    )
    drawLine(
      color = primaryColor,
      start = Offset(w * 0.34f, h * 0.86f),
      end = Offset(w * 0.66f, h * 0.86f),
      strokeWidth = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx(),
      cap = StrokeCap.Round
    )
  }
}

/**
 * Custom Bespoke Identity Canvas Vector: Discovery & Search Radar
 */
@Composable
fun TodNavSearchIcon(
  isSelected: Boolean,
  primaryColor: Color,
  modifier: Modifier = Modifier.size(24.dp)
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val center = Offset(w * 0.44f, h * 0.44f)
    val outerRadius = w * 0.32f

    // 1. Concentric Optical Lens
    drawCircle(
      color = primaryColor,
      center = center,
      radius = outerRadius,
      style = Stroke(width = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx())
    )

    // 2. High-Tech Internal Reticle Target / Glowing Beacon
    if (isSelected) {
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFF00F0FF), Color(0xFF007AFF)),
          center = center,
          radius = outerRadius * 0.65f
        ),
        center = center,
        radius = outerRadius * 0.42f
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        center = center,
        radius = 1.8.dp.toPx()
      )
    } else {
      drawCircle(
        color = primaryColor.copy(alpha = 0.55f),
        center = center,
        radius = outerRadius * 0.30f
      )
    }

    // 3. 45-Degree Laser Grip / Handle with Rounded Ends
    val handleStart = Offset(w * 0.66f, h * 0.66f)
    val handleEnd = Offset(w * 0.88f, h * 0.88f)
    drawLine(
      color = primaryColor,
      start = handleStart,
      end = handleEnd,
      strokeWidth = if (isSelected) 2.8.dp.toPx() else 2.2.dp.toPx(),
      cap = StrokeCap.Round
    )
  }
}

/**
 * Custom Bespoke Identity Canvas Vector: Apple-Style Control Hub & Settings
 */
@Composable
fun TodNavMoreIcon(
  isSelected: Boolean,
  primaryColor: Color,
  modifier: Modifier = Modifier.size(24.dp)
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // 3 Vertical Equalizer Control Channels with Staggered Nodes
    val channelsX = listOf(w * 0.28f, w * 0.50f, w * 0.72f)
    val knobsY = listOf(h * 0.34f, h * 0.66f, h * 0.44f)

    channelsX.forEachIndexed { i, x ->
      // Channel guide line
      drawLine(
        color = primaryColor.copy(alpha = if (isSelected) 0.55f else 0.35f),
        start = Offset(x, h * 0.18f),
        end = Offset(x, h * 0.82f),
        strokeWidth = 1.8.dp.toPx(),
        cap = StrokeCap.Round
      )

      // Control Knob / Slider Pill
      val y = knobsY[i]
      if (isSelected) {
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFBF5AF2), Color(0xFF7A24A6)),
            center = Offset(x, y),
            radius = 5.dp.toPx()
          ),
          center = Offset(x, y),
          radius = 4.2.dp.toPx()
        )
        drawCircle(
          color = Color.White.copy(alpha = 0.85f),
          center = Offset(x, y),
          radius = 1.6.dp.toPx()
        )
      } else {
        drawCircle(
          color = primaryColor,
          center = Offset(x, y),
          radius = 3.5.dp.toPx()
        )
      }
    }
  }
}

/**
 * Apple iOS Floating Liquid Glass Bottom Navigation Dock
 * Truly floating island dock: unselected shows icon only; selected expands smoothly to reveal icon + Arabic label.
 * Absolutely NO solid bar/header behind it! Pure floating translucent liquid glass.
 */
@Composable
fun LiquidGlassBottomBar(
  currentTab: TodNavTab,
  onTabSelected: (TodNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val tabs = listOf(
    Pair(TodNavTab.HOME, "الرئيسية"),
    Pair(TodNavTab.SEARCH, "بحث"),
    Pair(TodNavTab.MORE, "المزيد")
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    // Floating Dynamic Island Dock Container
    Box(
      modifier = Modifier
        .shadow(
          elevation = 18.dp,
          shape = RoundedCornerShape(34.dp),
          spotColor = Color(0xFF007AFF).copy(alpha = 0.30f),
          ambientColor = Color.Black.copy(alpha = 0.40f)
        )
        .liquidGlassEffect(
          shape = RoundedCornerShape(34.dp),
          isElevated = true,
          glassColor = Color(0x35141D34),
          borderBrush = Brush.verticalGradient(
            listOf(
              Color(0x60FFFFFF),
              Color(0x2864D2FF),
              Color(0x10FFFFFF),
              Color(0x250A84FF)
            )
          )
        )
        .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        tabs.forEach { (tab, title) ->
          val isSelected = currentTab == tab
          val interactionSource = remember { MutableInteractionSource() }
          val isPressed by interactionSource.collectIsPressedAsState()

          val tabScale by animateFloatAsState(
            targetValue = if (isPressed) 0.90f else 1.0f,
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMedium
            ),
            label = "tabScale_${tab.name}"
          )

          Box(
            modifier = Modifier
              .scale(tabScale)
              .shadow(
                elevation = if (isSelected) 8.dp else 0.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0xFF0A84FF).copy(alpha = 0.45f)
              )
              .clip(RoundedCornerShape(26.dp))
              .then(
                if (isSelected) {
                  Modifier
                    .background(
                      Brush.horizontalGradient(
                        listOf(
                          Color(0xFF0A84FF).copy(alpha = 0.35f),
                          Color(0xFF0055D4).copy(alpha = 0.22f),
                          Color(0xFF002B7A).copy(alpha = 0.15f)
                        )
                      )
                    )
                    .border(
                      width = 1.15.dp,
                      brush = Brush.horizontalGradient(
                        listOf(Color(0xFF64D2FF), Color(0xFF0A84FF), Color(0x40FFFFFF))
                      ),
                      shape = RoundedCornerShape(26.dp)
                    )
                } else {
                  Modifier
                    .background(Color(0x0AFFFFFF))
                    .border(
                      width = 0.75.dp,
                      color = Color(0x15FFFFFF),
                      shape = RoundedCornerShape(26.dp)
                    )
                }
              )
              .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onTabSelected(tab) }
              )
              .padding(
                horizontal = if (isSelected) 16.dp else 12.dp,
                vertical = 9.dp
              ),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              // Custom Handcrafted Vector Identity Icon
              when (tab) {
                TodNavTab.HOME -> TodNavHomeIcon(
                  isSelected = isSelected,
                  primaryColor = if (isSelected) Color(0xFF64D2FF) else Color(0xFF8E8E93)
                )
                TodNavTab.SEARCH -> TodNavSearchIcon(
                  isSelected = isSelected,
                  primaryColor = if (isSelected) Color(0xFF00F0FF) else Color(0xFF8E8E93)
                )
                TodNavTab.MORE -> TodNavMoreIcon(
                  isSelected = isSelected,
                  primaryColor = if (isSelected) Color(0xFFBF5AF2) else Color(0xFF8E8E93)
                )
              }

              // Animated Label: reveals smoothly ONLY when selected!
              AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    expandHorizontally(
                      animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                      )
                    ),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    shrinkHorizontally(
                      animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                      )
                    )
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Spacer(modifier = Modifier.width(7.dp))
                  Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Shimmer Brush for skeleton loading effects
 */
@Composable
fun rememberShimmerBrush(
  targetValue: Float = 1000f,
  durationMillis: Int = 1200
): Brush {
  val shimmerColors = listOf(
    Color(0x18FFFFFF),
    Color(0x40FFFFFF),
    Color(0x18FFFFFF)
  )

  val transition = rememberInfiniteTransition(label = "shimmerTransition")
  val translateAnimation by transition.animateFloat(
    initialValue = 0f,
    targetValue = targetValue,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmerTranslate"
  )

  return Brush.linearGradient(
    colors = shimmerColors,
    start = Offset.Zero,
    end = Offset(x = translateAnimation, y = translateAnimation)
  )
}

/**
 * Pulsing glowing live beacon indicator
 */
@Composable
fun PulsingLiveBadge(
  modifier: Modifier = Modifier,
  text: String = "مباشر",
  fontSize: TextUnit = 9.sp,
  paddingHorizontal: Dp = 7.dp,
  paddingVertical: Dp = 2.dp
) {
  val infiniteTransition = rememberInfiniteTransition(label = "livePulseTransition")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "livePulseScale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 0.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "livePulseAlpha"
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(
        Brush.horizontalGradient(
          listOf(TodLiveRed, Color(0xFFB71C1C))
        )
      )
      .border(0.75.dp, Color(0x66FFFFFF), RoundedCornerShape(8.dp))
      .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier.size(10.dp),
        contentAlignment = Alignment.Center
      ) {
        // Outer halo
        Box(
          modifier = Modifier
            .size(8.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(TodLiveRedLight.copy(alpha = pulseAlpha))
        )
        // Solid core
        Box(
          modifier = Modifier
            .size(4.5.dp)
            .clip(CircleShape)
            .background(Color.White)
        )
      }
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = text,
        color = Color.White,
        fontSize = fontSize,
        fontWeight = FontWeight.Black
      )
    }
  }
}

/**
 * Apple iOS 18 Control Center Circular Glowing Action Badge
 */
@Composable
fun IosCircularControlBadge(
  icon: ImageVector,
  background: Brush,
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 52.dp,
  iconSize: Dp = 26.dp
) {
  Box(
    modifier = modifier
      .size(size)
      .shadow(10.dp, CircleShape, spotColor = Color(0xFF007AFF).copy(alpha = 0.45f))
      .clip(CircleShape)
      .background(background)
      .drawBehind {
        // Specular top highlight curve
        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0x88FFFFFF), Color.Transparent),
            startY = 0f,
            endY = drawContext.size.height * 0.45f
          ),
          cornerRadius = CornerRadius(size.toPx() / 2f, size.toPx() / 2f)
        )
      }
      .border(1.dp, Color(0x66FFFFFF), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(iconSize)
    )
  }
}

/**
 * Apple iOS 18 Control Center Frosted Circle Button (like Flashlight/Timer/Calculator)
 */
@Composable
fun IosCircleControlButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 56.dp,
  iconSize: Dp = 24.dp
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.88f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "circleBtnScale"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .scale(scale)
        .size(size)
        .shadow(10.dp, CircleShape, spotColor = Color(0xFF007AFF).copy(alpha = 0.35f))
        .liquidGlassEffect(shape = CircleShape, isElevated = true)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = tint,
        modifier = Modifier.size(iconSize)
      )
    }
    Text(
      text = label,
      color = Color(0xCCFFFFFF),
      fontSize = 11.sp,
      fontFamily = ThmanyahFontFamily,
      fontWeight = FontWeight.Medium
    )
  }
}

/**
 * High-definition Video Format Tag (4K UHD, 1080p FHD, HD 60FPS) with Specular styling
 */
@Composable
fun VideoQualityBadge(
  qualityText: String,
  modifier: Modifier = Modifier
) {
  val is4k = qualityText.contains("4K", ignoreCase = true)
  val isFhd = qualityText.contains("FHD", ignoreCase = true) || qualityText.contains("1080", ignoreCase = true)

  val tagGradient = when {
    is4k -> Brush.linearGradient(listOf(Color(0xFF64D2FF), Color(0xFF0A84FF)))
    isFhd -> Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF007AFF)))
    else -> Brush.linearGradient(listOf(Color(0x50FFFFFF), Color(0x25FFFFFF)))
  }

  val textColor = Color.White

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(tagGradient)
      .border(0.5.dp, Color(0x55FFFFFF), RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = qualityText,
      color = textColor,
      fontSize = 9.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 0.4.sp
    )
  }
}


