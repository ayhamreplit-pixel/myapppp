package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodLiveRed
import com.example.ui.theme.TodLiveRedLight

/**
 * Shimmer Brush for skeleton loading effects
 */
@Composable
fun rememberShimmerBrush(
  targetValue: Float = 1000f,
  durationMillis: Int = 1200
): Brush {
  val shimmerColors = listOf(
    Color(0xFF14141E),
    Color(0xFF28283C),
    Color(0xFF14141E)
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
      .clip(RoundedCornerShape(6.dp))
      .background(
        Brush.horizontalGradient(
          listOf(TodLiveRed, Color(0xFFB71C1C))
        )
      )
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
 * High-definition Video Format Tag (4K UHD, 1080p FHD, HD 60FPS) with Metallic Specular styling
 */
@Composable
fun VideoQualityBadge(
  qualityText: String,
  modifier: Modifier = Modifier
) {
  val is4k = qualityText.contains("4K", ignoreCase = true)
  val isFhd = qualityText.contains("FHD", ignoreCase = true) || qualityText.contains("1080", ignoreCase = true)

  val tagGradient = when {
    is4k -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000)))
    isFhd -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF0284C7)))
    else -> Brush.linearGradient(listOf(Color(0xFF9E9EA7), Color(0xFF5E5E68)))
  }

  val textColor = when {
    is4k -> Color.Black
    isFhd -> Color.Black
    else -> Color.White
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(tagGradient)
      .padding(horizontal = 5.dp, vertical = 1.5.dp),
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
