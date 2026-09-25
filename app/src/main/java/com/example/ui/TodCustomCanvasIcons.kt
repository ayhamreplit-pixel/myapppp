package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 1. Custom Bespoke Icon: Xtream Server Racks with Live Signal LEDs
 */
@Composable
fun TodXtreamServerIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Top Rack Unit
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.12f, h * 0.14f),
      size = Size(w * 0.76f, h * 0.30f),
      cornerRadius = CornerRadius(w * 0.08f),
      style = Stroke(width = w * 0.08f)
    )
    // Top Rack LED & Port
    drawCircle(
      color = Color(0xFF34C759),
      radius = w * 0.05f,
      center = Offset(w * 0.30f, h * 0.29f)
    )
    drawLine(
      color = tint.copy(alpha = 0.9f),
      start = Offset(w * 0.46f, h * 0.29f),
      end = Offset(w * 0.72f, h * 0.29f),
      strokeWidth = w * 0.065f,
      cap = StrokeCap.Round
    )

    // Bottom Rack Unit
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.12f, h * 0.56f),
      size = Size(w * 0.76f, h * 0.30f),
      cornerRadius = CornerRadius(w * 0.08f),
      style = Stroke(width = w * 0.08f)
    )
    // Bottom Rack LED & Port
    drawCircle(
      color = Color(0xFF0A84FF),
      radius = w * 0.05f,
      center = Offset(w * 0.30f, h * 0.71f)
    )
    drawLine(
      color = tint.copy(alpha = 0.9f),
      start = Offset(w * 0.46f, h * 0.71f),
      end = Offset(w * 0.72f, h * 0.71f),
      strokeWidth = w * 0.065f,
      cap = StrokeCap.Round
    )
  }
}

/**
 * Large Artistic Xtream Server Rack for Explore TOD section
 */
@Composable
fun TodXtreamServerArtIcon(
  modifier: Modifier = Modifier.size(30.dp),
  primaryColor: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Top Rack Unit
    drawRoundRect(
      brush = Brush.linearGradient(listOf(Color.White, Color(0xFFFFD54F))),
      topLeft = Offset(w * 0.10f, h * 0.12f),
      size = Size(w * 0.80f, h * 0.32f),
      cornerRadius = CornerRadius(w * 0.10f),
      style = Stroke(width = w * 0.08f)
    )
    drawCircle(color = Color(0xFF34C759), radius = w * 0.06f, center = Offset(w * 0.28f, h * 0.28f))
    drawLine(Color.White, Offset(w * 0.44f, h * 0.28f), Offset(w * 0.74f, h * 0.28f), w * 0.07f, StrokeCap.Round)

    // Bottom Rack Unit
    drawRoundRect(
      brush = Brush.linearGradient(listOf(Color.White, Color(0xFFFFA000))),
      topLeft = Offset(w * 0.10f, h * 0.54f),
      size = Size(w * 0.80f, h * 0.32f),
      cornerRadius = CornerRadius(w * 0.10f),
      style = Stroke(width = w * 0.08f)
    )
    drawCircle(color = Color(0xFF00F0FF), radius = w * 0.06f, center = Offset(w * 0.28f, h * 0.70f))
    drawLine(Color.White, Offset(w * 0.44f, h * 0.70f), Offset(w * 0.74f, h * 0.70f), w * 0.07f, StrokeCap.Round)
  }
}

/**
 * 2. Custom Bespoke Icon: Direct M3U8 Stream Lightning Beam with Optical Waves
 */
@Composable
fun TodDirectStreamLinkIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Lightning Bolt Path
    val boltPath = Path().apply {
      moveTo(w * 0.54f, h * 0.08f)
      lineTo(w * 0.20f, h * 0.54f)
      lineTo(w * 0.48f, h * 0.54f)
      lineTo(w * 0.42f, h * 0.92f)
      lineTo(w * 0.80f, h * 0.44f)
      lineTo(w * 0.52f, h * 0.44f)
      close()
    }
    drawPath(path = boltPath, brush = Brush.verticalGradient(listOf(tint, Color(0xFF64D2FF))))
    drawPath(path = boltPath, color = Color.White, style = Stroke(width = w * 0.05f, join = StrokeJoin.Round))
  }
}

/**
 * Large Artistic Direct Stream Lightning for Explore TOD section
 */
@Composable
fun TodQuickLinkArtIcon(
  modifier: Modifier = Modifier.size(30.dp),
  primaryColor: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Lightning Bolt
    val boltPath = Path().apply {
      moveTo(w * 0.55f, h * 0.08f)
      lineTo(w * 0.18f, h * 0.54f)
      lineTo(w * 0.48f, h * 0.54f)
      lineTo(w * 0.40f, h * 0.94f)
      lineTo(w * 0.82f, h * 0.44f)
      lineTo(w * 0.52f, h * 0.44f)
      close()
    }
    drawPath(path = boltPath, brush = Brush.verticalGradient(listOf(Color.White, Color(0xFF00E5FF))))
    drawPath(path = boltPath, color = Color.White, style = Stroke(width = w * 0.06f, join = StrokeJoin.Round))

    // Pulse Ring
    drawArc(
      brush = Brush.radialGradient(listOf(Color(0x8000E5FF), Color(0x0000E5FF))),
      startAngle = 0f,
      sweepAngle = 360f,
      useCenter = false,
      topLeft = Offset(w * 0.05f, h * 0.05f),
      size = Size(w * 0.90f, h * 0.90f),
      style = Stroke(width = w * 0.04f)
    )
  }
}

/**
 * 3. Custom Bespoke Icon: Smart Anti-Lag Turbine & Buffer Shield
 */
@Composable
fun TodSmartAntiLagIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Speedometer Outer Gauge Arc
    drawArc(
      color = tint,
      startAngle = 140f,
      sweepAngle = 260f,
      useCenter = false,
      topLeft = Offset(w * 0.12f, h * 0.12f),
      size = Size(w * 0.76f, h * 0.76f),
      style = Stroke(width = w * 0.085f, cap = StrokeCap.Round)
    )

    // Active High-Speed Segment
    drawArc(
      brush = Brush.sweepGradient(listOf(Color(0xFF34C759), Color(0xFF00F0FF))),
      startAngle = 300f,
      sweepAngle = 100f,
      useCenter = false,
      topLeft = Offset(w * 0.12f, h * 0.12f),
      size = Size(w * 0.76f, h * 0.76f),
      style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
    )

    // Gauge Center Pivot
    drawCircle(
      color = Color(0xFF00F0FF),
      radius = w * 0.08f,
      center = Offset(w * 0.50f, h * 0.56f)
    )

    // Needle pointing fast to top-right
    drawLine(
      color = Color.White,
      start = Offset(w * 0.50f, h * 0.56f),
      end = Offset(w * 0.72f, h * 0.32f),
      strokeWidth = w * 0.08f,
      cap = StrokeCap.Round
    )
  }
}

/**
 * Large Artistic Anti-Buffer Engine for Explore TOD section
 */
@Composable
fun TodAntiBufferEngineArtIcon(
  modifier: Modifier = Modifier.size(30.dp),
  primaryColor: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Speedometer Outer Gauge Arc
    drawArc(
      brush = Brush.sweepGradient(listOf(Color(0xFF30D158), Color(0xFF00E5FF), Color(0xFF30D158))),
      startAngle = 135f,
      sweepAngle = 270f,
      useCenter = false,
      topLeft = Offset(w * 0.10f, h * 0.10f),
      size = Size(w * 0.80f, h * 0.80f),
      style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
    )

    // Speedometer Ticks
    for (i in 0..4) {
      val angle = Math.toRadians((140 + i * 65).toDouble())
      val sx = (w * 0.50f + Math.cos(angle) * (w * 0.30f)).toFloat()
      val sy = (h * 0.50f + Math.sin(angle) * (h * 0.30f)).toFloat()
      val ex = (w * 0.50f + Math.cos(angle) * (w * 0.38f)).toFloat()
      val ey = (h * 0.50f + Math.sin(angle) * (h * 0.38f)).toFloat()
      drawLine(Color.White.copy(alpha = 0.8f), Offset(sx, sy), Offset(ex, ey), w * 0.05f, StrokeCap.Round)
    }

    // Pivot & Turbo Needle
    drawCircle(Color.White, radius = w * 0.09f, center = Offset(w * 0.50f, h * 0.50f))
    drawCircle(Color(0xFF30D158), radius = w * 0.05f, center = Offset(w * 0.50f, h * 0.50f))
    drawLine(
      color = Color(0xFFFF375F),
      start = Offset(w * 0.50f, h * 0.50f),
      end = Offset(w * 0.74f, h * 0.26f),
      strokeWidth = w * 0.09f,
      cap = StrokeCap.Round
    )
  }
}

/**
 * 4. Custom Bespoke Icon: Accounts & Multi-Server Manager Profile
 */
@Composable
fun TodAccountsManagerIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Primary User Head
    drawCircle(
      color = tint,
      radius = w * 0.18f,
      center = Offset(w * 0.44f, h * 0.30f)
    )
    // Primary User Shoulders Arc
    drawArc(
      color = tint,
      startAngle = 180f,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(w * 0.12f, h * 0.54f),
      size = Size(w * 0.64f, h * 0.42f),
      style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
    )
    // Secondary Server Node Badge
    drawCircle(
      color = Color(0xFF64D2FF),
      radius = w * 0.12f,
      center = Offset(w * 0.76f, h * 0.72f)
    )
    drawCircle(
      color = Color.White,
      radius = w * 0.05f,
      center = Offset(w * 0.76f, h * 0.72f)
    )
  }
}

/**
 * 5. Custom Bespoke Icon: M3U Playlist & Media Stack
 */
@Composable
fun TodM3uPlaylistIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // 3 Curved Playlist Rails
    drawLine(
      color = tint,
      start = Offset(w * 0.16f, h * 0.24f),
      end = Offset(w * 0.78f, h * 0.24f),
      strokeWidth = w * 0.09f,
      cap = StrokeCap.Round
    )
    drawLine(
      color = tint,
      start = Offset(w * 0.16f, h * 0.50f),
      end = Offset(w * 0.60f, h * 0.50f),
      strokeWidth = w * 0.09f,
      cap = StrokeCap.Round
    )
    drawLine(
      color = tint,
      start = Offset(w * 0.16f, h * 0.76f),
      end = Offset(w * 0.50f, h * 0.76f),
      strokeWidth = w * 0.09f,
      cap = StrokeCap.Round
    )

    // Musical Note / Broadcast Playhead
    drawCircle(
      color = Color(0xFFBF5AF2),
      radius = w * 0.14f,
      center = Offset(w * 0.76f, h * 0.64f)
    )
    val trianglePath = Path().apply {
      moveTo(w * 0.72f, h * 0.56f)
      lineTo(w * 0.82f, h * 0.64f)
      lineTo(w * 0.72f, h * 0.72f)
      close()
    }
    drawPath(path = trianglePath, color = Color.White)
  }
}

/**
 * 6. Custom Bespoke Icon: Diagnostic Network Ping Radar
 */
@Composable
fun TodPingDiagnosticIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Radar Concentric Pulse Rings
    drawArc(
      color = tint.copy(alpha = 0.4f),
      startAngle = 135f,
      sweepAngle = 270f,
      useCenter = false,
      topLeft = Offset(w * 0.08f, h * 0.08f),
      size = Size(w * 0.84f, h * 0.84f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )
    drawArc(
      color = tint.copy(alpha = 0.8f),
      startAngle = 135f,
      sweepAngle = 270f,
      useCenter = false,
      topLeft = Offset(w * 0.24f, h * 0.24f),
      size = Size(w * 0.52f, h * 0.52f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )
    // Central Pulse Beacon
    drawCircle(
      color = Color(0xFF34C759),
      radius = w * 0.12f,
      center = Offset(w * 0.50f, h * 0.50f)
    )
  }
}

/**
 * 7. Custom Bespoke Icon: Server Synchronization & Refresh
 */
@Composable
fun TodServerSyncIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Top clockwise arrow arc
    drawArc(
      color = tint,
      startAngle = 220f,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(w * 0.14f, h * 0.14f),
      size = Size(w * 0.72f, h * 0.72f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )
    // Arrowhead top
    val topArrow = Path().apply {
      moveTo(w * 0.60f, h * 0.12f)
      lineTo(w * 0.84f, h * 0.20f)
      lineTo(w * 0.72f, h * 0.42f)
      close()
    }
    drawPath(path = topArrow, color = tint)

    // Bottom counter-clockwise arrow arc
    drawArc(
      color = Color(0xFF64D2FF),
      startAngle = 40f,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(w * 0.14f, h * 0.14f),
      size = Size(w * 0.72f, h * 0.72f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )
    // Arrowhead bottom
    val bottomArrow = Path().apply {
      moveTo(w * 0.40f, h * 0.88f)
      lineTo(w * 0.16f, h * 0.80f)
      lineTo(w * 0.28f, h * 0.58f)
      close()
    }
    drawPath(path = bottomArrow, color = Color(0xFF64D2FF))
  }
}

/**
 * 8. Custom Bespoke Icon: Live EPG Guide Calendar & Timeline
 */
@Composable
fun TodEpgGuideIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // TV Screen frame
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.10f, h * 0.12f),
      size = Size(w * 0.80f, h * 0.62f),
      cornerRadius = CornerRadius(w * 0.10f),
      style = Stroke(width = w * 0.08f)
    )

    // Schedule Timeline bars
    drawLine(
      color = Color(0xFF64D2FF),
      start = Offset(w * 0.22f, h * 0.32f),
      end = Offset(w * 0.54f, h * 0.32f),
      strokeWidth = w * 0.065f,
      cap = StrokeCap.Round
    )
    drawLine(
      color = Color(0xFFBF5AF2),
      start = Offset(w * 0.62f, h * 0.32f),
      end = Offset(w * 0.78f, h * 0.32f),
      strokeWidth = w * 0.065f,
      cap = StrokeCap.Round
    )
    drawLine(
      color = Color(0xFF34C759),
      start = Offset(w * 0.22f, h * 0.52f),
      end = Offset(w * 0.70f, h * 0.52f),
      strokeWidth = w * 0.065f,
      cap = StrokeCap.Round
    )

    // Stand Base
    drawLine(
      color = tint,
      start = Offset(w * 0.34f, h * 0.86f),
      end = Offset(w * 0.66f, h * 0.86f),
      strokeWidth = w * 0.08f,
      cap = StrokeCap.Round
    )
    drawLine(
      color = tint,
      start = Offset(w * 0.50f, h * 0.74f),
      end = Offset(w * 0.50f, h * 0.86f),
      strokeWidth = w * 0.08f
    )
  }
}

/**
 * 9. Custom Bespoke Icon: Hardware GPU Acceleration Chip
 */
@Composable
fun TodGpuAccelerationIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Main Processor Silicon Square
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.22f, h * 0.22f),
      size = Size(w * 0.56f, h * 0.56f),
      cornerRadius = CornerRadius(w * 0.10f),
      style = Stroke(width = w * 0.08f)
    )

    // GPU Core Diamond
    val corePath = Path().apply {
      moveTo(w * 0.50f, h * 0.34f)
      lineTo(w * 0.64f, h * 0.50f)
      lineTo(w * 0.50f, h * 0.66f)
      lineTo(w * 0.36f, h * 0.50f)
      close()
    }
    drawPath(path = corePath, brush = Brush.linearGradient(listOf(Color(0xFF30D158), Color(0xFF00F0FF))))

    // Top Pins
    drawLine(tint, Offset(w * 0.36f, h * 0.08f), Offset(w * 0.36f, h * 0.22f), w * 0.06f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.64f, h * 0.08f), Offset(w * 0.64f, h * 0.22f), w * 0.06f, StrokeCap.Round)
    // Bottom Pins
    drawLine(tint, Offset(w * 0.36f, h * 0.78f), Offset(w * 0.36f, h * 0.92f), w * 0.06f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.64f, h * 0.78f), Offset(w * 0.64f, h * 0.92f), w * 0.06f, StrokeCap.Round)
    // Left Pins
    drawLine(tint, Offset(w * 0.08f, h * 0.36f), Offset(w * 0.22f, h * 0.36f), w * 0.06f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.08f, h * 0.64f), Offset(w * 0.22f, h * 0.64f), w * 0.06f, StrokeCap.Round)
    // Right Pins
    drawLine(tint, Offset(w * 0.78f, h * 0.36f), Offset(w * 0.92f, h * 0.36f), w * 0.06f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.78f, h * 0.64f), Offset(w * 0.92f, h * 0.64f), w * 0.06f, StrokeCap.Round)
  }
}

/**
 * 10. Custom Bespoke Icon: Adaptive Bitrate & Quality Balancer
 */
@Composable
fun TodAdaptiveBitrateIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // 3 Equalizer Slider Bars with Thumb Knobs
    drawLine(tint, Offset(w * 0.22f, h * 0.12f), Offset(w * 0.22f, h * 0.88f), w * 0.07f, StrokeCap.Round)
    drawCircle(Color(0xFF64D2FF), w * 0.11f, Offset(w * 0.22f, h * 0.34f))

    drawLine(tint, Offset(w * 0.50f, h * 0.12f), Offset(w * 0.50f, h * 0.88f), w * 0.07f, StrokeCap.Round)
    drawCircle(Color(0xFF0A84FF), w * 0.11f, Offset(w * 0.50f, h * 0.66f))

    drawLine(tint, Offset(w * 0.78f, h * 0.12f), Offset(w * 0.78f, h * 0.88f), w * 0.07f, StrokeCap.Round)
    drawCircle(Color(0xFF34C759), w * 0.11f, Offset(w * 0.78f, h * 0.24f))
  }
}

/**
 * 11. Custom Bespoke Icon: Aspect Ratio & Scaling
 */
@Composable
fun TodAspectRatioIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Frame Screen
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.10f, h * 0.20f),
      size = Size(w * 0.80f, h * 0.60f),
      cornerRadius = CornerRadius(w * 0.08f),
      style = Stroke(width = w * 0.08f)
    )

    // Expand Diagonal Arrows
    drawLine(Color(0xFF64D2FF), Offset(w * 0.26f, h * 0.36f), Offset(w * 0.40f, h * 0.36f), w * 0.06f, StrokeCap.Round)
    drawLine(Color(0xFF64D2FF), Offset(w * 0.26f, h * 0.36f), Offset(w * 0.26f, h * 0.50f), w * 0.06f, StrokeCap.Round)

    drawLine(Color(0xFF64D2FF), Offset(w * 0.74f, h * 0.64f), Offset(w * 0.60f, h * 0.64f), w * 0.06f, StrokeCap.Round)
    drawLine(Color(0xFF64D2FF), Offset(w * 0.74f, h * 0.64f), Offset(w * 0.74f, h * 0.50f), w * 0.06f, StrokeCap.Round)
  }
}

/**
 * 12. Custom Bespoke Icon: Video Decoder Engine (Gear & Core)
 */
@Composable
fun TodVideoDecoderIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Outer Cog Ring
    drawCircle(tint, w * 0.36f, Offset(w * 0.50f, h * 0.50f), style = Stroke(width = w * 0.08f))

    // 4 Cog Teeth
    drawLine(tint, Offset(w * 0.50f, h * 0.06f), Offset(w * 0.50f, h * 0.20f), w * 0.09f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.50f, h * 0.80f), Offset(w * 0.50f, h * 0.94f), w * 0.09f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.06f, h * 0.50f), Offset(w * 0.20f, h * 0.50f), w * 0.09f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.80f, h * 0.50f), Offset(w * 0.94f, h * 0.50f), w * 0.09f, StrokeCap.Round)

    // Center Play Core
    val playPath = Path().apply {
      moveTo(w * 0.44f, h * 0.36f)
      lineTo(w * 0.62f, h * 0.50f)
      lineTo(w * 0.44f, h * 0.64f)
      close()
    }
    drawPath(path = playPath, color = Color(0xFF00E5FF))
  }
}

/**
 * 13. Custom Bespoke Icon: Match Refresh Rate (60FPS / 120Hz Velocity)
 */
@Composable
fun TodMatchRefreshRateIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Double Dynamic Sine Waves
    val wavePath1 = Path().apply {
      moveTo(w * 0.12f, h * 0.36f)
      cubicTo(w * 0.32f, h * 0.16f, w * 0.68f, h * 0.56f, w * 0.88f, h * 0.36f)
    }
    drawPath(path = wavePath1, color = Color(0xFF30D158), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))

    val wavePath2 = Path().apply {
      moveTo(w * 0.12f, h * 0.64f)
      cubicTo(w * 0.32f, h * 0.44f, w * 0.68f, h * 0.84f, w * 0.88f, h * 0.64f)
    }
    drawPath(path = wavePath2, color = tint, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
  }
}

/**
 * 14. Custom Bespoke Icon: Diagnostic Telemetry HUD
 */
@Composable
fun TodDiagnosticHudIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Screen corners: Target crosshairs
    // Top-left ⌜
    drawLine(tint, Offset(w * 0.12f, h * 0.26f), Offset(w * 0.12f, h * 0.14f), w * 0.07f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.12f, h * 0.14f), Offset(w * 0.24f, h * 0.14f), w * 0.07f, StrokeCap.Round)
    // Top-right ⌝
    drawLine(tint, Offset(w * 0.88f, h * 0.26f), Offset(w * 0.88f, h * 0.14f), w * 0.07f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.88f, h * 0.14f), Offset(w * 0.76f, h * 0.14f), w * 0.07f, StrokeCap.Round)
    // Bottom-left ⌞
    drawLine(tint, Offset(w * 0.12f, h * 0.74f), Offset(w * 0.12f, h * 0.86f), w * 0.07f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.12f, h * 0.86f), Offset(w * 0.24f, h * 0.86f), w * 0.07f, StrokeCap.Round)
    // Bottom-right ⌟
    drawLine(tint, Offset(w * 0.88f, h * 0.74f), Offset(w * 0.88f, h * 0.86f), w * 0.07f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.88f, h * 0.86f), Offset(w * 0.76f, h * 0.86f), w * 0.07f, StrokeCap.Round)

    // Data pulse line
    val pulsePath = Path().apply {
      moveTo(w * 0.22f, h * 0.50f)
      lineTo(w * 0.40f, h * 0.50f)
      lineTo(w * 0.48f, h * 0.32f)
      lineTo(w * 0.56f, h * 0.68f)
      lineTo(w * 0.64f, h * 0.50f)
      lineTo(w * 0.78f, h * 0.50f)
    }
    drawPath(path = pulsePath, color = Color(0xFFFF9F0A), style = Stroke(width = w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round))
  }
}

/**
 * 15. Custom Bespoke Icon: Data Saver Network Shield
 */
@Composable
fun TodDataSaverIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Wifi / cellular signal arcs
    drawArc(tint, 220f, 100f, false, Offset(w * 0.14f, h * 0.18f), Size(w * 0.72f, h * 0.72f), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
    drawArc(tint, 230f, 80f, false, Offset(w * 0.28f, h * 0.32f), Size(w * 0.44f, h * 0.44f), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
    drawCircle(Color(0xFFFF9F0A), w * 0.08f, Offset(w * 0.50f, h * 0.68f))
  }
}

/**
 * 16. Custom Bespoke Icon: Keep Screen On (Display with Sunbeams)
 */
@Composable
fun TodKeepScreenOnIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Device Outline
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.24f, h * 0.12f),
      size = Size(w * 0.52f, h * 0.76f),
      cornerRadius = CornerRadius(w * 0.08f),
      style = Stroke(width = w * 0.08f)
    )
    // Glowing Sun in Center
    drawCircle(Color(0xFF00E5FF), w * 0.10f, Offset(w * 0.50f, h * 0.46f))
    // Home indicator
    drawLine(tint, Offset(w * 0.42f, h * 0.80f), Offset(w * 0.58f, h * 0.80f), w * 0.06f, StrokeCap.Round)
  }
}

/**
 * 17. Custom Bespoke Icon: Auto Play Last Channel
 */
@Composable
fun TodAutoPlayIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Circular loop
    drawArc(
      color = tint,
      startAngle = 0f,
      sweepAngle = 280f,
      useCenter = false,
      topLeft = Offset(w * 0.12f, h * 0.12f),
      size = Size(w * 0.76f, h * 0.76f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )

    // Center Play Triangle
    val playPath = Path().apply {
      moveTo(w * 0.44f, h * 0.36f)
      lineTo(w * 0.64f, h * 0.50f)
      lineTo(w * 0.44f, h * 0.64f)
      close()
    }
    drawPath(path = playPath, color = Color(0xFFFF2D55))
  }
}

/**
 * 18. Custom Bespoke Icon: Loudness & Audio Boost (+150%)
 */
@Composable
fun TodAudioBoostIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Speaker Cone
    val speakerPath = Path().apply {
      moveTo(w * 0.16f, h * 0.38f)
      lineTo(w * 0.32f, h * 0.38f)
      lineTo(w * 0.50f, h * 0.22f)
      lineTo(w * 0.50f, h * 0.78f)
      lineTo(w * 0.32f, h * 0.62f)
      lineTo(w * 0.16f, h * 0.62f)
      close()
    }
    drawPath(path = speakerPath, color = tint)

    // Sound waves
    drawArc(
      color = Color(0xFF30D158),
      startAngle = 300f,
      sweepAngle = 120f,
      useCenter = false,
      topLeft = Offset(w * 0.42f, h * 0.24f),
      size = Size(w * 0.36f, h * 0.52f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )
    drawArc(
      color = Color(0xFF00F0FF),
      startAngle = 300f,
      sweepAngle = 120f,
      useCenter = false,
      topLeft = Offset(w * 0.54f, h * 0.14f),
      size = Size(w * 0.38f, h * 0.72f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )
  }
}

/**
 * 19. Custom Bespoke Icon: Vocal Commentary Clarity
 */
@Composable
fun TodVocalClarityIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Mic capsule
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.34f, h * 0.14f),
      size = Size(w * 0.32f, h * 0.46f),
      cornerRadius = CornerRadius(w * 0.16f),
      style = Stroke(width = w * 0.08f)
    )
    // Mic U-cradle
    drawArc(
      color = Color(0xFFFF375F),
      startAngle = 0f,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(w * 0.22f, h * 0.32f),
      size = Size(w * 0.56f, h * 0.42f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )
    // Stem
    drawLine(tint, Offset(w * 0.50f, h * 0.74f), Offset(w * 0.50f, h * 0.88f), w * 0.08f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.34f, h * 0.88f), Offset(w * 0.66f, h * 0.88f), w * 0.08f, StrokeCap.Round)
  }
}

/**
 * 20. Custom Bespoke Icon: Audio Equalizer DSP Sound Waves
 */
@Composable
fun TodAudioEqualizerIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Sound Bar 1
    drawLine(Color(0xFFFF9F0A), Offset(w * 0.16f, h * 0.40f), Offset(w * 0.16f, h * 0.80f), w * 0.09f, StrokeCap.Round)
    // Sound Bar 2 (High)
    drawLine(Color(0xFFFF375F), Offset(w * 0.38f, h * 0.18f), Offset(w * 0.38f, h * 0.86f), w * 0.09f, StrokeCap.Round)
    // Sound Bar 3 (Peak)
    drawLine(Color(0xFF00F0FF), Offset(w * 0.62f, h * 0.10f), Offset(w * 0.62f, h * 0.88f), w * 0.09f, StrokeCap.Round)
    // Sound Bar 4
    drawLine(Color(0xFF30D158), Offset(w * 0.84f, h * 0.30f), Offset(w * 0.84f, h * 0.82f), w * 0.09f, StrokeCap.Round)
  }
}

/**
 * 21. Custom Bespoke Icon: Subtitles & Closed Captions (CC)
 */
@Composable
fun TodSubtitlesCcIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Dialogue Bubble Frame
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.10f, h * 0.16f),
      size = Size(w * 0.80f, h * 0.58f),
      cornerRadius = CornerRadius(w * 0.10f),
      style = Stroke(width = w * 0.08f)
    )

    // CC Lettering Lines
    // C 1
    drawArc(Color(0xFFFFD54F), 60f, 240f, false, Offset(w * 0.22f, h * 0.28f), Size(w * 0.22f, h * 0.34f), style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))
    // C 2
    drawArc(Color(0xFFFFD54F), 60f, 240f, false, Offset(w * 0.54f, h * 0.28f), Size(w * 0.22f, h * 0.34f), style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))

    // Bubble Tail
    val tailPath = Path().apply {
      moveTo(w * 0.30f, h * 0.74f)
      lineTo(w * 0.22f, h * 0.88f)
      lineTo(w * 0.44f, h * 0.74f)
      close()
    }
    drawPath(path = tailPath, color = tint)
  }
}

/**
 * 22. Custom Bespoke Icon: Arabic Text Encoding
 */
@Composable
fun TodTextEncodingIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Stylized Arabic "ض" / Latin "T" glyph combination
    // Main baseline curve
    val baseline = Path().apply {
      moveTo(w * 0.16f, h * 0.56f)
      cubicTo(w * 0.30f, h * 0.76f, w * 0.60f, h * 0.76f, w * 0.80f, h * 0.56f)
    }
    drawPath(baseline, color = tint, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))

    // Diacritic Arabic Point
    drawCircle(Color(0xFF00E5FF), w * 0.07f, Offset(w * 0.50f, h * 0.28f))

    // Vertical stem
    drawLine(Color(0xFFBF5AF2), Offset(w * 0.50f, h * 0.40f), Offset(w * 0.50f, h * 0.66f), w * 0.08f, StrokeCap.Round)
  }
}

/**
 * 23. Custom Bespoke Icon: Fluid Touch Swipe Gestures
 */
@Composable
fun TodSwipeGesturesIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Upward swipe trail
    drawLine(Color(0xFF0A84FF), Offset(w * 0.28f, h * 0.78f), Offset(w * 0.28f, h * 0.26f), w * 0.08f, StrokeCap.Round)
    // Upward Arrowhead
    val upArrow = Path().apply {
      moveTo(w * 0.16f, h * 0.36f)
      lineTo(w * 0.28f, h * 0.18f)
      lineTo(w * 0.40f, h * 0.36f)
    }
    drawPath(upArrow, Color(0xFF0A84FF), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Downward swipe trail
    drawLine(Color(0xFF64D2FF), Offset(w * 0.72f, h * 0.22f), Offset(w * 0.72f, h * 0.74f), w * 0.08f, StrokeCap.Round)
    // Downward Arrowhead
    val downArrow = Path().apply {
      moveTo(w * 0.60f, h * 0.64f)
      lineTo(w * 0.72f, h * 0.82f)
      lineTo(w * 0.84f, h * 0.64f)
    }
    drawPath(downArrow, Color(0xFF64D2FF), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))
  }
}

/**
 * 24. Custom Bespoke Icon: Picture-in-Picture (PiP) Floating Screen
 */
@Composable
fun TodPictureInPictureIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Master Screen frame
    drawRoundRect(
      color = tint,
      topLeft = Offset(w * 0.10f, h * 0.14f),
      size = Size(w * 0.80f, h * 0.72f),
      cornerRadius = CornerRadius(w * 0.10f),
      style = Stroke(width = w * 0.08f)
    )

    // Floating PiP Sub-window (Bottom-Right)
    drawRoundRect(
      brush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF007AFF))),
      topLeft = Offset(w * 0.48f, h * 0.46f),
      size = Size(w * 0.36f, h * 0.34f),
      cornerRadius = CornerRadius(w * 0.06f)
    )
    drawRoundRect(
      color = Color.White,
      topLeft = Offset(w * 0.48f, h * 0.46f),
      size = Size(w * 0.36f, h * 0.34f),
      cornerRadius = CornerRadius(w * 0.06f),
      style = Stroke(width = w * 0.05f)
    )
  }
}

/**
 * 25. Custom Bespoke Icon: Live Clock Chronometer
 */
@Composable
fun TodLiveClockIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Dial Ring
    drawCircle(tint, w * 0.38f, Offset(w * 0.50f, h * 0.50f), style = Stroke(width = w * 0.08f))

    // Precision Hands
    drawCircle(Color(0xFFFF9F0A), w * 0.06f, Offset(w * 0.50f, h * 0.50f))
    // Hour hand (pointing to 3)
    drawLine(Color.White, Offset(w * 0.50f, h * 0.50f), Offset(w * 0.70f, h * 0.50f), w * 0.075f, StrokeCap.Round)
    // Minute hand (pointing to 12)
    drawLine(Color.White, Offset(w * 0.50f, h * 0.50f), Offset(w * 0.50f, h * 0.24f), w * 0.075f, StrokeCap.Round)
  }
}

/**
 * 26. Custom Bespoke Icon: Fast Channel Zapping
 */
@Composable
fun TodFastZappingIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Top Right Arrow
    drawLine(Color(0xFFBF5AF2), Offset(w * 0.20f, h * 0.36f), Offset(w * 0.74f, h * 0.36f), w * 0.08f, StrokeCap.Round)
    val rightHead = Path().apply {
      moveTo(w * 0.62f, h * 0.24f)
      lineTo(w * 0.78f, h * 0.36f)
      lineTo(w * 0.62f, h * 0.48f)
    }
    drawPath(rightHead, Color(0xFFBF5AF2), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Bottom Left Arrow
    drawLine(tint, Offset(w * 0.80f, h * 0.64f), Offset(w * 0.26f, h * 0.64f), w * 0.08f, StrokeCap.Round)
    val leftHead = Path().apply {
      moveTo(w * 0.38f, h * 0.52f)
      lineTo(w * 0.22f, h * 0.64f)
      lineTo(w * 0.38f, h * 0.76f)
    }
    drawPath(leftHead, tint, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))
  }
}

/**
 * 27. Custom Bespoke Icon: Smart Sleep Timer Moon & Clock
 */
@Composable
fun TodSleepTimerIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Crescent Moon
    val moonPath = Path().apply {
      moveTo(w * 0.50f, h * 0.10f)
      cubicTo(w * 0.85f, h * 0.18f, w * 0.85f, h * 0.82f, w * 0.50f, h * 0.90f)
      cubicTo(w * 0.70f, h * 0.75f, w * 0.70f, h * 0.25f, w * 0.50f, h * 0.10f)
      close()
    }
    drawPath(
      path = moonPath,
      brush = Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF9F0A)))
    )

    // Clock Hands on Moon Body
    drawCircle(tint, w * 0.06f, Offset(w * 0.38f, h * 0.50f))
    drawLine(tint, Offset(w * 0.38f, h * 0.50f), Offset(w * 0.38f, h * 0.32f), w * 0.06f, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.38f, h * 0.50f), Offset(w * 0.52f, h * 0.50f), w * 0.06f, StrokeCap.Round)
  }
}

/**
 * 28. Custom Bespoke Icon: Theme Ambient Aura Palette
 */
@Composable
fun TodThemePaletteIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Painter's Palette Shape
    val palette = Path().apply {
      moveTo(w * 0.50f, h * 0.12f)
      cubicTo(w * 0.88f, h * 0.12f, w * 0.92f, h * 0.60f, w * 0.74f, h * 0.82f)
      cubicTo(w * 0.62f, h * 0.94f, w * 0.40f, h * 0.84f, w * 0.30f, h * 0.84f)
      cubicTo(w * 0.16f, h * 0.84f, w * 0.08f, h * 0.68f, w * 0.08f, h * 0.50f)
      cubicTo(w * 0.08f, h * 0.28f, w * 0.28f, h * 0.12f, w * 0.50f, h * 0.12f)
      close()
    }
    drawPath(palette, color = tint, style = Stroke(width = w * 0.08f))

    // Color Droplets
    drawCircle(Color(0xFF00E5FF), w * 0.07f, Offset(w * 0.34f, h * 0.34f))
    drawCircle(Color(0xFFFF2D55), w * 0.07f, Offset(w * 0.60f, h * 0.30f))
    drawCircle(Color(0xFF30D158), w * 0.07f, Offset(w * 0.72f, h * 0.52f))
  }
}

/**
 * 29. Custom Bespoke Icon: Parental Lock Shield
 */
@Composable
fun TodParentalLockIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Shield outline
    val shield = Path().apply {
      moveTo(w * 0.50f, h * 0.12f)
      lineTo(w * 0.82f, h * 0.26f)
      cubicTo(w * 0.82f, h * 0.64f, w * 0.50f, h * 0.88f, w * 0.50f, h * 0.88f)
      cubicTo(w * 0.50f, h * 0.88f, w * 0.18f, h * 0.64f, w * 0.18f, h * 0.26f)
      close()
    }
    drawPath(shield, color = tint, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))

    // Keyhole in Center
    drawCircle(Color(0xFFFF375F), w * 0.08f, Offset(w * 0.50f, h * 0.46f))
    val keySlot = Path().apply {
      moveTo(w * 0.46f, h * 0.48f)
      lineTo(w * 0.54f, h * 0.48f)
      lineTo(w * 0.56f, h * 0.64f)
      lineTo(w * 0.44f, h * 0.64f)
      close()
    }
    drawPath(keySlot, Color(0xFFFF375F))
  }
}

/**
 * 30. Custom Bespoke Icon: Parental Security PIN Keypad
 */
@Composable
fun TodParentalPinIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // 4 Keypad Numeric Dots
    drawCircle(Color(0xFF64D2FF), w * 0.09f, Offset(w * 0.32f, h * 0.32f))
    drawCircle(Color(0xFF64D2FF), w * 0.09f, Offset(w * 0.68f, h * 0.32f))
    drawCircle(Color(0xFF64D2FF), w * 0.09f, Offset(w * 0.32f, h * 0.68f))
    drawCircle(Color(0xFF64D2FF), w * 0.09f, Offset(w * 0.68f, h * 0.68f))

    // Surrounding Keypad Boundary
    drawRoundRect(tint, Offset(w * 0.14f, h * 0.14f), Size(w * 0.72f, h * 0.72f), CornerRadius(w * 0.12f), style = Stroke(width = w * 0.07f))
  }
}

/**
 * 31. Custom Bespoke Icon: Adult Content Eye Filter Guard
 */
@Composable
fun TodAdultFilterIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Eye Outline
    val eyePath = Path().apply {
      moveTo(w * 0.12f, h * 0.50f)
      cubicTo(w * 0.28f, h * 0.22f, w * 0.72f, h * 0.22f, w * 0.88f, h * 0.50f)
      cubicTo(w * 0.72f, h * 0.78f, w * 0.28f, h * 0.78f, w * 0.12f, h * 0.50f)
      close()
    }
    drawPath(eyePath, color = tint, style = Stroke(width = w * 0.08f))

    // Pupil
    drawCircle(Color(0xFF64D2FF), w * 0.11f, Offset(w * 0.50f, h * 0.50f))

    // Slash Guard Line
    drawLine(Color(0xFFFF375F), Offset(w * 0.20f, h * 0.80f), Offset(w * 0.80f, h * 0.20f), w * 0.08f, StrokeCap.Round)
  }
}

/**
 * 32. Custom Bespoke Icon: Global DNS & Network Acceleration
 */
@Composable
fun TodDnsNetworkIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Globe outer circle
    drawCircle(tint, w * 0.38f, Offset(w * 0.50f, h * 0.50f), style = Stroke(width = w * 0.08f))

    // Latitudes & Longitudes
    drawLine(tint, Offset(w * 0.12f, h * 0.50f), Offset(w * 0.88f, h * 0.50f), w * 0.06f)
    drawLine(tint, Offset(w * 0.50f, h * 0.12f), Offset(w * 0.50f, h * 0.88f), w * 0.06f)

    drawOval(Color(0xFF00E5FF), Offset(w * 0.28f, h * 0.12f), Size(w * 0.44f, h * 0.76f), style = Stroke(width = w * 0.06f))
  }
}

/**
 * 33. Custom Bespoke Icon: Backup & Export Clipboard Tray
 */
@Composable
fun TodBackupExportIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Tray box
    val tray = Path().apply {
      moveTo(w * 0.16f, h * 0.48f)
      lineTo(w * 0.16f, h * 0.82f)
      lineTo(w * 0.84f, h * 0.82f)
      lineTo(w * 0.84f, h * 0.48f)
    }
    drawPath(tray, color = tint, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Upward Quantum Arrow
    drawLine(Color(0xFF34C759), Offset(w * 0.50f, h * 0.64f), Offset(w * 0.50f, h * 0.18f), w * 0.08f, StrokeCap.Round)
    val upHead = Path().apply {
      moveTo(w * 0.34f, h * 0.32f)
      lineTo(w * 0.50f, h * 0.14f)
      lineTo(w * 0.66f, h * 0.32f)
    }
    drawPath(upHead, Color(0xFF34C759), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))
  }
}

/**
 * 34. Custom Bespoke Icon: Clear Cache & Sparkle Booster
 */
@Composable
fun TodClearCacheIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Broom wand handle
    drawLine(tint, Offset(w * 0.78f, h * 0.16f), Offset(w * 0.44f, h * 0.50f), w * 0.08f, StrokeCap.Round)

    // Sparkle 1 (Large Cyan)
    val sparklePath = Path().apply {
      moveTo(w * 0.30f, h * 0.40f)
      cubicTo(w * 0.30f, h * 0.55f, w * 0.15f, h * 0.55f, w * 0.15f, h * 0.55f)
      cubicTo(w * 0.30f, h * 0.55f, w * 0.30f, h * 0.70f, w * 0.30f, h * 0.70f)
      cubicTo(w * 0.30f, h * 0.55f, w * 0.45f, h * 0.55f, w * 0.45f, h * 0.55f)
      cubicTo(w * 0.30f, h * 0.55f, w * 0.30f, h * 0.40f, w * 0.30f, h * 0.40f)
      close()
    }
    drawPath(sparklePath, brush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF30D158))))

    // Sparkle 2 (Small Gold)
    drawCircle(Color(0xFFFFD54F), w * 0.06f, Offset(w * 0.68f, h * 0.68f))
    drawCircle(Color.White, w * 0.04f, Offset(w * 0.54f, h * 0.76f))
  }
}

/**
 * 35. Custom Bespoke Icon: Reset Settings to Default
 */
@Composable
fun TodResetDefaultsIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Circular revert arc
    drawArc(
      color = Color(0xFFFF453A),
      startAngle = 60f,
      sweepAngle = 270f,
      useCenter = false,
      topLeft = Offset(w * 0.14f, h * 0.14f),
      size = Size(w * 0.72f, h * 0.72f),
      style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )

    // Revert Arrowhead
    val arr = Path().apply {
      moveTo(w * 0.44f, h * 0.84f)
      lineTo(w * 0.62f, h * 0.94f)
      lineTo(w * 0.60f, h * 0.72f)
      close()
    }
    drawPath(arr, Color(0xFFFF453A))

    // Central Revert Node
    drawCircle(Color.White, w * 0.08f, Offset(w * 0.50f, h * 0.50f))
  }
}

/**
 * 36. Custom Bespoke Icon: App Info Diamond Crest
 */
@Composable
fun TodAppInfoIcon(
  modifier: Modifier = Modifier.size(22.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Diamond Crest
    val diamond = Path().apply {
      moveTo(w * 0.50f, h * 0.10f)
      lineTo(w * 0.88f, h * 0.50f)
      lineTo(w * 0.50f, h * 0.90f)
      lineTo(w * 0.12f, h * 0.50f)
      close()
    }
    drawPath(diamond, brush = Brush.linearGradient(listOf(Color(0xFF64D2FF), Color(0xFF0A84FF))), style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))

    // Letter "i"
    drawCircle(Color.White, w * 0.06f, Offset(w * 0.50f, h * 0.36f))
    drawLine(Color.White, Offset(w * 0.50f, h * 0.48f), Offset(w * 0.50f, h * 0.68f), w * 0.08f, StrokeCap.Round)
  }
}

/**
 * 37. Ultra Futuristic 4K IPTV Engine Emblem for the Home Screen Player Card
 */
@Composable
fun TodIptv4kMasterpieceIcon(
  modifier: Modifier = Modifier.size(52.dp)
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Orbital Radiant Aura Ring
    drawCircle(
      brush = Brush.radialGradient(
        listOf(Color(0x6000E5FF), Color(0x200A84FF), Color(0x000A84FF))
      ),
      radius = w * 0.48f,
      center = Offset(w * 0.50f, h * 0.50f)
    )

    // Holographic Octagonal Shield Frame
    val octPath = Path().apply {
      moveTo(w * 0.30f, h * 0.08f)
      lineTo(w * 0.70f, h * 0.08f)
      lineTo(w * 0.92f, h * 0.30f)
      lineTo(w * 0.92f, h * 0.70f)
      lineTo(w * 0.70f, h * 0.92f)
      lineTo(w * 0.30f, h * 0.92f)
      lineTo(w * 0.08f, h * 0.70f)
      lineTo(w * 0.08f, h * 0.30f)
      close()
    }
    drawPath(
      path = octPath,
      brush = Brush.linearGradient(
        listOf(Color(0xFF00E5FF), Color(0xFF0A84FF), Color(0xFFBF5AF2))
      ),
      style = Stroke(width = w * 0.06f, join = StrokeJoin.Round)
    )

    // Glowing Laser Play Triangle with Specular Polish
    val playPath = Path().apply {
      moveTo(w * 0.40f, h * 0.30f)
      lineTo(w * 0.70f, h * 0.50f)
      lineTo(w * 0.40f, h * 0.70f)
      close()
    }
    drawPath(
      path = playPath,
      brush = Brush.horizontalGradient(
        listOf(Color.White, Color(0xFF64D2FF), Color(0xFF00E5FF))
      )
    )
    drawPath(
      path = playPath,
      color = Color.White,
      style = Stroke(width = w * 0.03f, join = StrokeJoin.Round)
    )

    // Top Right 4K Live Spark
    drawCircle(Color(0xFFFFD54F), radius = w * 0.06f, center = Offset(w * 0.78f, h * 0.22f))
  }
}

/**
 * 38. Custom Bespoke Icon: iOS Frosted Settings Gear with Sapphire Core
 */
@Composable
fun TodSettingsWheelIcon(
  modifier: Modifier = Modifier.size(24.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    // Outer Gear Teeth Ring
    for (i in 0 until 6) {
      val angle = (i * 60.0) * (Math.PI / 180.0)
      val x = center.x + (w * 0.38f) * Math.cos(angle).toFloat()
      val y = center.y + (h * 0.38f) * Math.sin(angle).toFloat()
      drawCircle(
        color = Color(0xFF64D2FF),
        radius = w * 0.10f,
        center = Offset(x, y)
      )
    }

    // Outer Ring
    drawCircle(
      brush = Brush.linearGradient(listOf(Color(0xFF64D2FF), Color(0xFF0A84FF))),
      radius = w * 0.36f,
      center = center,
      style = Stroke(width = w * 0.08f)
    )

    // Inner Sapphire Core
    drawCircle(
      brush = Brush.radialGradient(listOf(Color(0xFF00F0FF), Color(0xFF0055D4))),
      radius = w * 0.18f,
      center = center
    )
    drawCircle(
      color = Color.White,
      radius = w * 0.06f,
      center = center
    )
  }
}

/**
 * 39. Custom Bespoke Icon: Direct Link Clipboard Paste with Neon Beam
 */
@Composable
fun TodDirectPasteIcon(
  modifier: Modifier = Modifier.size(24.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Clipboard body
    drawRoundRect(
      brush = Brush.linearGradient(listOf(Color(0xFF30D158), Color(0xFF10B981))),
      topLeft = Offset(w * 0.18f, h * 0.22f),
      size = Size(w * 0.64f, h * 0.68f),
      cornerRadius = CornerRadius(w * 0.10f),
      style = Stroke(width = w * 0.08f)
    )

    // Clipboard clip on top
    drawRoundRect(
      color = Color(0xFF34C759),
      topLeft = Offset(w * 0.34f, h * 0.10f),
      size = Size(w * 0.32f, h * 0.18f),
      cornerRadius = CornerRadius(w * 0.05f)
    )

    // Lightning paste checkmark/beam
    val beam = Path().apply {
      moveTo(w * 0.32f, h * 0.54f)
      lineTo(w * 0.46f, h * 0.68f)
      lineTo(w * 0.70f, h * 0.42f)
    }
    drawPath(beam, color = Color.White, style = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
  }
}

/**
 * 40. Custom Bespoke Icon: High-Precision Speedometer Ping Test Dial
 */
@Composable
fun TodSpeedTestDialIcon(
  modifier: Modifier = Modifier.size(24.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    // Speedometer Arc Gauge (240 degrees)
    drawArc(
      brush = Brush.sweepGradient(
        listOf(Color(0xFF00E5FF), Color(0xFF30D158), Color(0xFFFF9F0A), Color(0xFFFF3B30)),
        center = center
      ),
      startAngle = 150f,
      sweepAngle = 240f,
      useCenter = false,
      topLeft = Offset(w * 0.12f, h * 0.12f),
      size = Size(w * 0.76f, h * 0.76f),
      style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
    )

    // Center pivot
    drawCircle(
      color = Color.White,
      radius = w * 0.10f,
      center = center
    )

    // Dial Needle pointing to high speed
    val needle = Path().apply {
      moveTo(center.x - w * 0.04f, center.y)
      lineTo(center.x + w * 0.04f, center.y)
      lineTo(center.x + w * 0.28f, center.y - h * 0.28f)
      close()
    }
    drawPath(needle, brush = Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF0A84FF))))
  }
}

/**
 * 41. Custom Bespoke Icon: Live Broadcast Television Station Emblem
 */
@Composable
fun TodBroadcastStationIcon(
  modifier: Modifier = Modifier.size(24.dp),
  tint: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // TV Screen outline
    drawRoundRect(
      brush = Brush.linearGradient(listOf(Color(0xFF34C759), Color(0xFF00E5FF))),
      topLeft = Offset(w * 0.12f, h * 0.20f),
      size = Size(w * 0.76f, h * 0.58f),
      cornerRadius = CornerRadius(w * 0.12f),
      style = Stroke(width = w * 0.08f)
    )

    // Live signal transmission waves from top antenna
    drawArc(
      color = Color(0xFF34C759),
      startAngle = 220f,
      sweepAngle = 100f,
      useCenter = false,
      topLeft = Offset(w * 0.30f, h * 0.04f),
      size = Size(w * 0.40f, h * 0.26f),
      style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
    )

    // TV Stand base
    drawLine(Color.White, Offset(w * 0.35f, h * 0.88f), Offset(w * 0.65f, h * 0.88f), w * 0.08f, StrokeCap.Round)
    drawLine(Color.White, Offset(w * 0.50f, h * 0.78f), Offset(w * 0.50f, h * 0.88f), w * 0.08f, StrokeCap.Round)

    // Glowing Live Signal Beacon dot inside
    drawCircle(Color(0xFF34C759), radius = w * 0.09f, center = Offset(w * 0.50f, h * 0.49f))
  }
}
