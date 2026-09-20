package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom TOD Icon: Settings Cog with a centered Play Triangle inside.
 * As seen in TOD player screenshots.
 */
@Composable
fun TodSettingsCogWithPlay(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 26.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
    val outerRadius = size.toPx() * 0.46f
    val innerRadius = size.toPx() * 0.36f
    val numTeeth = 8
    val strokeWidth = size.toPx() * 0.085f

    // Draw scalloped gear outline path
    val path = Path()
    val totalPoints = numTeeth * 2
    for (i in 0 until totalPoints) {
      val angle = (i * 2 * PI / totalPoints).toFloat() - (PI / 2).toFloat()
      val r = if (i % 2 == 0) outerRadius else innerRadius
      val x = center.x + r * cos(angle)
      val y = center.y + r * sin(angle)
      if (i == 0) {
        path.moveTo(x, y)
      } else {
        path.lineTo(x, y)
      }
    }
    path.close()

    drawPath(
      path = path,
      color = tint,
      style = Stroke(
        width = strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )

    // Draw centered Play Triangle pointing right
    val triangleSize = size.toPx() * 0.22f
    val triPath = Path().apply {
      moveTo(center.x - triangleSize * 0.45f, center.y - triangleSize * 0.65f)
      lineTo(center.x + triangleSize * 0.65f, center.y)
      lineTo(center.x - triangleSize * 0.45f, center.y + triangleSize * 0.65f)
      close()
    }
    drawPath(path = triPath, color = tint, style = Fill)
  }
}

/**
 * Custom TOD Icon: 2x2 Rounded Squares Grid (Channel / Quick Switch).
 * As seen in TOD player screenshots.
 */
@Composable
fun TodGridFour(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 24.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val total = size.toPx()
    val gap = total * 0.16f
    val sqSize = (total - gap) / 2f
    val cornerRadius = CornerRadius(sqSize * 0.32f, sqSize * 0.32f)
    val strokeWidth = total * 0.085f

    // Top-left
    drawRoundRect(
      color = tint,
      topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
      size = Size(sqSize - strokeWidth, sqSize - strokeWidth),
      cornerRadius = cornerRadius,
      style = Stroke(width = strokeWidth)
    )
    // Top-right
    drawRoundRect(
      color = tint,
      topLeft = Offset(sqSize + gap + strokeWidth / 2, strokeWidth / 2),
      size = Size(sqSize - strokeWidth, sqSize - strokeWidth),
      cornerRadius = cornerRadius,
      style = Stroke(width = strokeWidth)
    )
    // Bottom-left
    drawRoundRect(
      color = tint,
      topLeft = Offset(strokeWidth / 2, sqSize + gap + strokeWidth / 2),
      size = Size(sqSize - strokeWidth, sqSize - strokeWidth),
      cornerRadius = cornerRadius,
      style = Stroke(width = strokeWidth)
    )
    // Bottom-right
    drawRoundRect(
      color = tint,
      topLeft = Offset(sqSize + gap + strokeWidth / 2, sqSize + gap + strokeWidth / 2),
      size = Size(sqSize - strokeWidth, sqSize - strokeWidth),
      cornerRadius = cornerRadius,
      style = Stroke(width = strokeWidth)
    )
  }
}

/**
 * Custom TOD Icon: Subtitles / Captions (Rounded square with 2 horizontal lines).
 * As seen in TOD player screenshots.
 */
@Composable
fun TodSubtitles(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 24.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val total = size.toPx()
    val strokeWidth = total * 0.085f
    val cornerRadius = CornerRadius(total * 0.22f, total * 0.22f)

    // Outer rounded rect
    drawRoundRect(
      color = tint,
      topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
      size = Size(total - strokeWidth, total - strokeWidth),
      cornerRadius = cornerRadius,
      style = Stroke(width = strokeWidth)
    )

    // Top horizontal line
    val lineInsetX = total * 0.26f
    val lineStroke = total * 0.08f
    val line1Y = total * 0.40f
    drawLine(
      color = tint,
      start = Offset(lineInsetX, line1Y),
      end = Offset(total - lineInsetX, line1Y),
      strokeWidth = lineStroke,
      cap = StrokeCap.Round
    )

    // Bottom horizontal line (slightly shorter or equal)
    val line2Y = total * 0.60f
    drawLine(
      color = tint,
      start = Offset(lineInsetX, line2Y),
      end = Offset(total * 0.62f, line2Y),
      strokeWidth = lineStroke,
      cap = StrokeCap.Round
    )
  }
}

/**
 * Custom TOD Icon: Replay 10 Seconds (Counter-clockwise circular arrow with "10" inside).
 * Exactly as seen in TOD player screenshots.
 */
@Composable
fun TodReplay10(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 44.dp
) {
  Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.size(size)) {
      val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
      val radius = size.toPx() * 0.42f
      val strokeWidth = size.toPx() * 0.075f

      // Draw counter-clockwise circular arc (sweeping ~280 degrees)
      val path = Path().apply {
        // Start from top-left, sweep around bottom and right back towards top
        arcTo(
          rect = androidx.compose.ui.geometry.Rect(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
          ),
          startAngleDegrees = -60f,
          sweepAngleDegrees = 280f,
          forceMoveTo = false
        )
      }
      drawPath(
        path = path,
        color = tint,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )

      // Draw Arrow Head at the top-left (-60 deg) pointing counter-clockwise
      val arrowAngle = -60f * (PI / 180f).toFloat()
      val arrowTip = Offset(center.x + radius * cos(arrowAngle), center.y + radius * sin(arrowAngle))
      val arrowPath = Path().apply {
        moveTo(arrowTip.x, arrowTip.y - size.toPx() * 0.08f)
        lineTo(arrowTip.x - size.toPx() * 0.16f, arrowTip.y + size.toPx() * 0.02f)
        lineTo(arrowTip.x + size.toPx() * 0.02f, arrowTip.y + size.toPx() * 0.12f)
        close()
      }
      drawPath(path = arrowPath, color = tint, style = Fill)
    }

    // Centered "10" text
    Text(
      text = "10",
      color = tint,
      fontSize = (size.value * 0.38f).sp,
      fontWeight = FontWeight.Bold
    )
  }
}

/**
 * Custom TOD Icon: Forward 10 Seconds (Clockwise circular arrow with "10" inside).
 */
@Composable
fun TodForward10(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 44.dp
) {
  Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.size(size)) {
      val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
      val radius = size.toPx() * 0.42f
      val strokeWidth = size.toPx() * 0.075f

      // Draw clockwise circular arc (sweeping ~280 degrees)
      val path = Path().apply {
        arcTo(
          rect = androidx.compose.ui.geometry.Rect(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
          ),
          startAngleDegrees = -120f,
          sweepAngleDegrees = 280f,
          forceMoveTo = false
        )
      }
      drawPath(
        path = path,
        color = tint,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )

      // Draw Arrow Head at the top-right (-120+280 = 160 deg -> pointing clockwise)
      val arrowAngle = 160f * (PI / 180f).toFloat()
      val arrowTip = Offset(center.x + radius * cos(arrowAngle), center.y + radius * sin(arrowAngle))
      val arrowPath = Path().apply {
        moveTo(arrowTip.x, arrowTip.y - size.toPx() * 0.08f)
        lineTo(arrowTip.x + size.toPx() * 0.16f, arrowTip.y + size.toPx() * 0.02f)
        lineTo(arrowTip.x - size.toPx() * 0.02f, arrowTip.y + size.toPx() * 0.12f)
        close()
      }
      drawPath(path = arrowPath, color = tint, style = Fill)
    }

    Text(
      text = "10",
      color = tint,
      fontSize = (size.value * 0.38f).sp,
      fontWeight = FontWeight.Bold
    )
  }
}

/**
 * Custom TOD Icon: Pause Bars (Two tall rounded vertical pill bars).
 * Exactly as seen in TOD player screenshots.
 */
@Composable
fun TodPauseBars(
  modifier: Modifier = Modifier,
  tint: Color = Color.White.copy(alpha = 0.85f),
  width: Dp = 11.dp,
  height: Dp = 52.dp,
  gap: Dp = 13.dp
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(width, height)
        .clip(RoundedCornerShape(width / 2))
        .background(tint)
    )
    Spacer(modifier = Modifier.width(gap))
    Box(
      modifier = Modifier
        .size(width, height)
        .clip(RoundedCornerShape(width / 2))
        .background(tint)
    )
  }
}

/**
 * Custom TOD Icon: Play Triangle (Smooth rounded right-facing triangle).
 */
@Composable
fun TodPlayTriangle(
  modifier: Modifier = Modifier,
  tint: Color = Color.White.copy(alpha = 0.85f),
  size: Dp = 50.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val w = size.toPx()
    val h = size.toPx()
    val path = Path().apply {
      moveTo(w * 0.22f, h * 0.14f)
      lineTo(w * 0.86f, h * 0.50f)
      lineTo(w * 0.22f, h * 0.86f)
      close()
    }
    drawPath(
      path = path,
      color = tint,
      style = Fill
    )
  }
}

/**
 * Custom TOD Icon: Sun / Brightness Indicator (Center circle + 8 radiant pill dashes).
 * Exactly as seen under the vertical slider in TOD screenshots.
 */
@Composable
fun TodSunBrightness(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 20.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
    val coreRadius = size.toPx() * 0.16f

    // Center filled circle
    drawCircle(color = tint, radius = coreRadius, center = center)

    // 8 radial dashed pill rays
    val rayInner = size.toPx() * 0.27f
    val rayOuter = size.toPx() * 0.46f
    val rayStroke = size.toPx() * 0.11f

    for (i in 0 until 8) {
      val angle = (i * 2 * PI / 8).toFloat()
      val start = Offset(center.x + rayInner * cos(angle), center.y + rayInner * sin(angle))
      val end = Offset(center.x + rayOuter * cos(angle), center.y + rayOuter * sin(angle))
      drawLine(
        color = tint,
        start = start,
        end = end,
        strokeWidth = rayStroke,
        cap = StrokeCap.Round
      )
    }
  }
}

/**
 * Custom TOD Icon: Close 'X' (Thin diagonal cross).
 * As seen in TOD modal and player screenshots.
 */
@Composable
fun TodCloseX(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 24.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val total = size.toPx()
    val strokeWidth = total * 0.10f
    val inset = total * 0.20f

    // First diagonal \
    drawLine(
      color = tint,
      start = Offset(inset, inset),
      end = Offset(total - inset, total - inset),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )
    // Second diagonal /
    drawLine(
      color = tint,
      start = Offset(total - inset, inset),
      end = Offset(inset, total - inset),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )
  }
}

/**
 * Custom TOD Icon: Fullscreen Expand (4 diagonal outward arrows).
 * As seen in bottom right corner of TOD screenshots.
 */
@Composable
fun TodFullscreenArrows(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 22.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val total = size.toPx()
    val strokeWidth = total * 0.11f
    val armLength = total * 0.28f

    // Top-Left corner: ⌜
    drawLine(color = tint, start = Offset(0f, armLength), end = Offset(0f, 0f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(0f, 0f), end = Offset(armLength, 0f), strokeWidth = strokeWidth, cap = StrokeCap.Round)

    // Top-Right corner: ⌝
    drawLine(color = tint, start = Offset(total - armLength, 0f), end = Offset(total, 0f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(total, 0f), end = Offset(total, armLength), strokeWidth = strokeWidth, cap = StrokeCap.Round)

    // Bottom-Left corner: ⌞
    drawLine(color = tint, start = Offset(0f, total - armLength), end = Offset(0f, total), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(0f, total), end = Offset(armLength, total), strokeWidth = strokeWidth, cap = StrokeCap.Round)

    // Bottom-Right corner: ⌟
    drawLine(color = tint, start = Offset(total - armLength, total), end = Offset(total, total), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(total, total), end = Offset(total, total - armLength), strokeWidth = strokeWidth, cap = StrokeCap.Round)
  }
}

/**
 * Custom TOD Icon: Sleek RTL Back Arrow '→'.
 * As seen next to match title in TOD screenshots.
 */
@Composable
fun TodArrowBackRtl(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 24.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val total = size.toPx()
    val strokeWidth = total * 0.09f
    val cy = total / 2f

    // Horizontal shaft
    drawLine(
      color = tint,
      start = Offset(total * 0.15f, cy),
      end = Offset(total * 0.85f, cy),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )

    // Arrowhead pointing right: >
    val barbSize = total * 0.28f
    drawLine(
      color = tint,
      start = Offset(total * 0.85f - barbSize, cy - barbSize),
      end = Offset(total * 0.85f, cy),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )
    drawLine(
      color = tint,
      start = Offset(total * 0.85f - barbSize, cy + barbSize),
      end = Offset(total * 0.85f, cy),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )
  }
}

/**
 * Custom TOD Dolphin Crest Watermark Badge.
 * As seen on the right edge in TOD player screenshots.
 */
@Composable
fun TodWatermarkBadge(
  modifier: Modifier = Modifier,
  size: Dp = 40.dp
) {
  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(Color(0x33001824)),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size * 0.75f)) {
      val center = Offset(size.toPx() * 0.375f, size.toPx() * 0.375f)
      val r = size.toPx() * 0.34f
      // Draw outer subtle blue-tinted ring
      drawCircle(
        color = Color(0x6600E5FF),
        radius = r,
        center = center,
        style = Stroke(width = size.toPx() * 0.04f)
      )
      // Dolphin-like curved silhouette
      val path = Path().apply {
        moveTo(center.x - r * 0.6f, center.y + r * 0.2f)
        cubicTo(
          center.x - r * 0.3f, center.y - r * 0.7f,
          center.x + r * 0.4f, center.y - r * 0.6f,
          center.x + r * 0.7f, center.y + r * 0.1f
        )
        cubicTo(
          center.x + r * 0.3f, center.y + r * 0.6f,
          center.x - r * 0.2f, center.y + r * 0.5f,
          center.x - r * 0.6f, center.y + r * 0.2f
        )
        close()
      }
      drawPath(path = path, color = Color(0x8840C4FF), style = Fill)
    }
  }
}

/**
 * Custom TOD Icon: Stream Reload / Replay curved circular arrow.
 * Sleek and pixel-perfect matching TOD.
 */
@Composable
fun TodReloadIcon(
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 22.dp
) {
  Canvas(modifier = modifier.size(size)) {
    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
    val radius = size.toPx() * 0.40f
    val strokeWidth = size.toPx() * 0.085f

    // Draw circular arc (~300 degrees)
    val path = Path().apply {
      arcTo(
        rect = androidx.compose.ui.geometry.Rect(
          center.x - radius,
          center.y - radius,
          center.x + radius,
          center.y + radius
        ),
        startAngleDegrees = -70f,
        sweepAngleDegrees = 300f,
        forceMoveTo = false
      )
    }
    drawPath(
      path = path,
      color = tint,
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Draw Arrow tip at the top
    val arrowAngle = -70f * (PI / 180f).toFloat()
    val arrowTip = Offset(center.x + radius * cos(arrowAngle), center.y + radius * sin(arrowAngle))
    val arrowPath = Path().apply {
      moveTo(arrowTip.x + size.toPx() * 0.04f, arrowTip.y - size.toPx() * 0.12f)
      lineTo(arrowTip.x - size.toPx() * 0.14f, arrowTip.y - size.toPx() * 0.02f)
      lineTo(arrowTip.x - size.toPx() * 0.02f, arrowTip.y + size.toPx() * 0.12f)
      close()
    }
    drawPath(path = arrowPath, color = tint, style = Fill)
  }
}

