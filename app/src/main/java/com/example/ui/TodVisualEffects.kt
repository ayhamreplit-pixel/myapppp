package com.example.ui

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
  // Specular Glare Colors
  val SpecularHighlightTop = Color(0xB3FFFFFF)
  val SpecularHighlightMid = Color(0x38FFFFFF)
  val SpecularHighlightBot = Color(0x10FFFFFF)

  // Crystal Clear Liquid Glass Surfaces (Translucent without dark black mud)
  val LiquidSurfaceRegular = listOf(
    Color(0x38FFFFFF),
    Color(0x1CFFFFFF),
    Color(0x10FFFFFF)
  )

  val LiquidSurfaceElevated = listOf(
    Color(0x4EFFFFFF),
    Color(0x28FFFFFF),
    Color(0x18FFFFFF)
  )

  val LiquidSurfaceUltraClear = listOf(
    Color(0x30FFFFFF),
    Color(0x16FFFFFF),
    Color(0x0CFFFFFF)
  )

  val LiquidSurfaceActive = listOf(
    Color(0x60FFFFFF),
    Color(0x35FFFFFF),
    Color(0x20FFFFFF)
  )

  // Tinted Glass Surfaces
  val LiquidSurfaceTintedBlue = listOf(
    Color(0x550A84FF),
    Color(0x2A0055D4),
    Color(0x18002D75)
  )

  val LiquidSurfaceTintedGold = listOf(
    Color(0x55FDB913),
    Color(0x2AD97706),
    Color(0x1878350F)
  )

  val LiquidSurfaceTintedPurple = listOf(
    Color(0x55BF5AF2),
    Color(0x2A7A24A6),
    Color(0x184C1D95)
  )

  val LiquidSurfaceTintedEmerald = listOf(
    Color(0x5530D158),
    Color(0x2A10B981),
    Color(0x18064E3B)
  )

  val LiquidSurfaceTintedRose = listOf(
    Color(0x55FF375F),
    Color(0x2AE11D48),
    Color(0x18881337)
  )

  // Optical Specular Gradient Borders (Bright top highlight -> soft rim)
  val LiquidSpecularBorder = Brush.verticalGradient(
    listOf(
      Color(0xCCFFFFFF),
      Color(0x40FFFFFF),
      Color(0x18FFFFFF),
      Color(0x35FFFFFF)
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
  glowTint: Color? = null,
  glassColor: Color? = null,
  borderBrush: Brush? = null,
  showTopGlare: Boolean = true
): Modifier = composed {
  val surfaceColors = when {
    glassColor != null -> listOf(
      glassColor,
      glassColor.copy(alpha = (glassColor.alpha * 0.65f).coerceAtLeast(0.08f)),
      glassColor.copy(alpha = (glassColor.alpha * 0.35f).coerceAtLeast(0.04f))
    )
    glowTint != null -> listOf(
      glowTint.copy(alpha = 0.42f),
      glowTint.copy(alpha = 0.20f),
      glowTint.copy(alpha = 0.08f)
    )
    isElevated -> LiquidGlassTheme.LiquidSurfaceElevated
    else -> LiquidGlassTheme.LiquidSurfaceRegular
  }

  val finalBorderBrush = borderBrush ?: when {
    glowTint == TodGold -> LiquidGlassTheme.LiquidGoldBorder
    glowTint == Color(0xFF0A84FF) -> LiquidGlassTheme.LiquidBlueBorder
    glowTint == Color(0xFFBF5AF2) -> LiquidGlassTheme.LiquidPurpleBorder
    glowTint == Color(0xFF30D158) -> LiquidGlassTheme.LiquidGreenBorder
    else -> LiquidGlassTheme.LiquidSpecularBorder
  }

  this
    .clip(shape)
    .background(Brush.verticalGradient(surfaceColors))
    .drawBehind {
      if (showTopGlare) {
        // Curved optical refraction specular highlight arc across the top rim
        val glareWidth = size.width
        val glareHeight = (size.height * 0.38f).coerceAtMost(32.dp.toPx())

        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0x7AFFFFFF),
              Color(0x22FFFFFF),
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
  glowTint: Color? = null,
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
        elevation = if (isElevated) 18.dp else 10.dp,
        shape = shape,
        spotColor = (glowTint ?: Color(0xFF0055D4)).copy(alpha = 0.35f),
        ambientColor = Color.Black.copy(alpha = 0.25f)
      )
      .liquidGlassEffect(
        shape = shape,
        isElevated = isElevated,
        glowTint = glowTint,
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
 * Apple iOS Floating Liquid Glass Bottom Navigation Dock
 * Symmetrical, perfectly centered icons, harmonious active indicators, flush to bottom.
 */
@Composable
fun LiquidGlassBottomBar(
  currentTab: TodNavTab,
  onTabSelected: (TodNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val tabs = listOf(
    Triple(TodNavTab.HOME, "الرئيسية", Icons.Default.Home),
    Triple(TodNavTab.SEARCH, "بحث", Icons.Default.Search),
    Triple(TodNavTab.MORE, "المزيد", Icons.Default.MoreHoriz)
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, bottom = 4.dp, top = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFF007AFF).copy(alpha = 0.35f))
        .liquidGlassEffect(
          shape = RoundedCornerShape(26.dp),
          isElevated = true,
          glassColor = Color(0x35FFFFFF)
        )
        .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        tabs.forEach { (tab, title, icon) ->
          val isSelected = currentTab == tab
          val interactionSource = remember { MutableInteractionSource() }
          val isPressed by interactionSource.collectIsPressedAsState()

          val tabScale by animateFloatAsState(
            targetValue = if (isPressed) 0.92f else if (isSelected) 1.0f else 0.96f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "tabScale"
          )

          Box(
            modifier = Modifier
              .weight(1f)
              .scale(tabScale)
              .clip(RoundedCornerShape(20.dp))
              .then(
                if (isSelected) {
                  Modifier
                    .background(
                      Brush.verticalGradient(
                        listOf(
                          Color(0x600A84FF),
                          Color(0x350055D4),
                          Color(0x200A84FF)
                        )
                      )
                    )
                    .border(1.dp, Color(0x9964D2FF), RoundedCornerShape(20.dp))
                } else {
                  Modifier
                }
              )
              .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onTabSelected(tab) }
              )
              .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else Color(0x99FFFFFF),
                modifier = Modifier.size(23.dp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = title,
                color = if (isSelected) Color.White else Color(0x99FFFFFF),
                fontSize = 11.5.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
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


