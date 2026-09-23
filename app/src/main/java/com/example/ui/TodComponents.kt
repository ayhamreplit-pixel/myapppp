package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.BroadcastStream
import com.example.model.XtreamChannel
import com.example.ui.theme.AppFontFamily
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.TodButtonGrey
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGoldGlow
import com.example.ui.theme.TodGradients
import com.example.ui.theme.TodLiveRed
import kotlinx.coroutines.delay

/**
 * Official Jawwy TV Logo with Icon Emblem and Gold Styling
 */
@Composable
fun TodLogo(
  modifier: Modifier = Modifier,
  fontSize: Int = 22,
  showSubtext: Boolean = true
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    // Elegant Logo Icon Emblem
    Box(
      modifier = Modifier
        .size((fontSize + 12).dp)
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.linearGradient(
            listOf(TodGold, Color(0xFFFF9800))
          )
        )
        .border(1.dp, TodGoldGlow.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
        .padding(2.dp),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.jawwy_icon),
        contentDescription = "Jawwy TV Logo",
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)),
        contentScale = ContentScale.Crop
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Jawwy TV Text
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Jawwy",
        color = TodGold,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp,
        fontFamily = AppFontFamily
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "TV",
        color = Color.White,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp,
        fontFamily = AppFontFamily
      )
    }
  }
}

/**
 * High-End Corporate Grade TOD Bottom Navigation Bar (Matching TOD iOS Reference)
 */
enum class TodNavTab {
  HOME,
  SEARCH,
  MORE
}

@Composable
fun TodHomeNavIcon(
  isSelected: Boolean,
  activeColor: Color = Color(0xFFFFB800),
  inactiveColor: Color = Color(0xFF8E8E93),
  modifier: Modifier = Modifier
) {
  val color = if (isSelected) activeColor else inactiveColor
  Canvas(modifier = modifier.size(26.dp)) {
    val w = size.width
    val h = size.height
    val strokeWidth = 2.4.dp.toPx()

    val path = Path().apply {
      moveTo(w * 0.5f, h * 0.08f)
      lineTo(w * 0.94f, h * 0.44f)
      lineTo(w * 0.85f, h * 0.86f)
      quadraticBezierTo(w * 0.85f, h * 0.96f, w * 0.74f, h * 0.96f)
      lineTo(w * 0.26f, h * 0.96f)
      quadraticBezierTo(w * 0.15f, h * 0.96f, w * 0.15f, h * 0.86f)
      lineTo(w * 0.06f, h * 0.44f)
      close()
    }

    drawPath(
      path = path,
      color = color,
      style = Stroke(
        width = strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )

    val dotSize = w * 0.22f
    drawRoundRect(
      color = color,
      topLeft = Offset((w - dotSize) / 2f, h * 0.52f),
      size = Size(dotSize, dotSize),
      cornerRadius = CornerRadius(3.5.dp.toPx())
    )
  }
}

@Composable
fun TodSearchNavIcon(
  isSelected: Boolean,
  activeColor: Color = Color(0xFFFFB800),
  inactiveColor: Color = Color(0xFF8E8E93),
  modifier: Modifier = Modifier
) {
  val color = if (isSelected) activeColor else inactiveColor
  Canvas(modifier = modifier.size(26.dp)) {
    val w = size.width
    val h = size.height
    val strokeWidth = 2.3.dp.toPx()
    val radius = w * 0.32f
    val center = Offset(w * 0.42f, h * 0.42f)

    drawCircle(
      color = color,
      radius = radius,
      center = center,
      style = Stroke(width = strokeWidth)
    )

    val handleStart = Offset(center.x + radius * 0.707f, center.y + radius * 0.707f)
    val handleEnd = Offset(w * 0.92f, h * 0.92f)
    drawLine(
      color = color,
      start = handleStart,
      end = handleEnd,
      strokeWidth = strokeWidth * 1.15f,
      cap = StrokeCap.Round
    )
  }
}

@Composable
fun TodMoreNavIcon(
  isSelected: Boolean,
  activeColor: Color = Color(0xFFFFB800),
  inactiveColor: Color = Color(0xFF8E8E93),
  modifier: Modifier = Modifier
) {
  if (isSelected) {
    Box(
      modifier = modifier
        .size(26.dp)
        .clip(RoundedCornerShape(7.dp))
        .background(activeColor),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(17.dp)) {
        val w = size.width
        val h = size.height
        val headRadius = w * 0.24f
        val strokeW = 1.8.dp.toPx()

        drawCircle(
          color = Color(0xFF141414),
          radius = headRadius,
          center = Offset(w * 0.5f, h * 0.32f),
          style = Stroke(width = strokeW)
        )
        val shoulderPath = Path().apply {
          moveTo(w * 0.16f, h * 0.88f)
          quadraticBezierTo(w * 0.16f, h * 0.58f, w * 0.5f, h * 0.58f)
          quadraticBezierTo(w * 0.84f, h * 0.58f, w * 0.84f, h * 0.88f)
        }
        drawPath(
          path = shoulderPath,
          color = Color(0xFF141414),
          style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
      }
    }
  } else {
    Canvas(modifier = modifier.size(26.dp)) {
      val w = size.width
      val h = size.height
      val headRadius = w * 0.24f
      val strokeW = 2.1.dp.toPx()

      drawCircle(
        color = inactiveColor,
        radius = headRadius,
        center = Offset(w * 0.5f, h * 0.30f),
        style = Stroke(width = strokeW)
      )
      val shoulderPath = Path().apply {
        moveTo(w * 0.16f, h * 0.88f)
        quadraticBezierTo(w * 0.16f, h * 0.58f, w * 0.5f, h * 0.58f)
        quadraticBezierTo(w * 0.84f, h * 0.58f, w * 0.84f, h * 0.88f)
      }
      drawPath(
        path = shoulderPath,
        color = inactiveColor,
        style = Stroke(width = strokeW, cap = StrokeCap.Round)
      )
    }
  }
}

/**
 * Modern iOS 18 Ultra-Sleek Floating Navigation Dock (Icon-Only Minimalist Dock)
 * Features:
 * - Clean floating rounded capsule geometry (RoundedCornerShape(32.dp))
 * - Translucent acrylic backdrop with subtle hairline reflection
 * - Physics-based iOS Spring bounce on press (scales to 0.88x) with indication = null (NO white flash)
 * - Luminous active capsule indicator with smooth animated scale and glow
 * - Dynamic icon morphing and zero clutter (No text labels)
 */
@Composable
fun TodBottomNavBar(
  currentTab: TodNavTab,
  onTabSelected: (TodNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .widthIn(max = 380.dp)
        .fillMaxWidth()
        .height(58.dp)
        .shadow(
          elevation = 24.dp,
          shape = RoundedCornerShape(29.dp),
          spotColor = Color(0xBB000000),
          ambientColor = Color(0x77000000)
        )
        .clip(RoundedCornerShape(29.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xF0181822),
              Color(0xFA0E0E14)
            )
          )
        )
        .border(
          width = 0.75.dp,
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0x40FFFFFF),
              Color(0x10FFFFFF)
            )
          ),
          shape = RoundedCornerShape(29.dp)
        )
        .padding(horizontal = 8.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        // Tab 1: المزيد (More / Settings)
        TodIosTabItem(
          isSelected = currentTab == TodNavTab.MORE,
          onClick = { onTabSelected(TodNavTab.MORE) },
          icon = { isSel -> TodMoreNavIcon(isSelected = isSel) }
        )

        // Tab 2: بحث (Search)
        TodIosTabItem(
          isSelected = currentTab == TodNavTab.SEARCH,
          onClick = { onTabSelected(TodNavTab.SEARCH) },
          icon = { isSel -> TodSearchNavIcon(isSelected = isSel) }
        )

        // Tab 3: الرئيسية (Home)
        TodIosTabItem(
          isSelected = currentTab == TodNavTab.HOME,
          onClick = { onTabSelected(TodNavTab.HOME) },
          icon = { isSel -> TodHomeNavIcon(isSelected = isSel) }
        )
      }
    }
  }
}

@Composable
private fun TodIosTabItem(
  isSelected: Boolean,
  onClick: () -> Unit,
  icon: @Composable (Boolean) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Spring physical feedback on touch (No white flash!)
  val pressScale by animateFloatAsState(
    targetValue = if (isPressed) 0.86f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "tabPressScale"
  )

  // Icon bounce when active
  val iconScale by animateFloatAsState(
    targetValue = if (isSelected) 1.15f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "tabIconScale"
  )

  // Active glowing capsule background
  val activeBgAlpha by animateFloatAsState(
    targetValue = if (isSelected) 1.0f else 0.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "activeTabBg"
  )

  Box(
    modifier = Modifier
      .scale(pressScale)
      .height(46.dp)
      .width(72.dp)
      .clip(RoundedCornerShape(23.dp))
      .then(
        if (activeBgAlpha > 0.01f) {
          Modifier
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0x35FDB913).copy(alpha = 0.28f * activeBgAlpha),
                  Color(0x15FDB913).copy(alpha = 0.12f * activeBgAlpha)
                )
              )
            )
            .border(
              0.75.dp,
              Color(0x44FDB913).copy(alpha = 0.40f * activeBgAlpha),
              RoundedCornerShape(23.dp)
            )
        } else {
          Modifier
        }
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null, // Strictly NO white/gray material ripple!
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .scale(iconScale),
      contentAlignment = Alignment.Center
    ) {
      icon(isSelected)
    }
  }
}

/**
 * iOS Floating Mini-Player Pill (Top pill in images (5).jpeg)
 * Floats directly above the bottom navigation pill dock with track artwork, title/subtitle, and play/pause/30s controls.
 */
@Composable
fun TodIosFloatingMiniPlayer(
  stream: BroadcastStream,
  isPlaying: Boolean,
  onTogglePlayPause: () -> Unit,
  onSeekForward30: () -> Unit,
  onClick: () -> Unit,
  onClose: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .height(60.dp)
      .shadow(12.dp, shape = RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.5f))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(26.dp),
    color = Color(0xEC22222E),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      Brush.verticalGradient(listOf(Color(0x45FFFFFF), Color(0x12FFFFFF)))
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Thumbnail Artwork
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2C2C38)),
          contentAlignment = Alignment.Center
        ) {
          if (!stream.logoUrl.isNullOrBlank()) {
            AsyncImage(
              model = stream.logoUrl,
              contentDescription = stream.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Icon(
              imageVector = Icons.Default.Tv,
              contentDescription = null,
              tint = TodGold,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Center: Title + Subtitle
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = stream.title.ifBlank { "بث مباشر نشط" },
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = stream.subtitle.ifBlank { if (stream.isLive) "بث مباشر الآن" else "جاهز للتشغيل" },
            color = Color(0xFFAAAAAA),
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Right: Play/Pause & Skip 30s Buttons (from images (5).jpeg)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Box(
          modifier = Modifier
            .iosBounceClick(scaleDown = 0.85f) { onTogglePlayPause() }
            .size(36.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Box(
          modifier = Modifier
            .iosBounceClick(scaleDown = 0.85f) { onSeekForward30() }
            .size(36.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "تقديم 30 ثانية",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Text(
              "30",
              color = Color.White,
              fontSize = 7.5.sp,
              fontWeight = FontWeight.Black
            )
          }
        }
      }
    }
  }
}

/**
 * iOS Liquid Slider (Bottom of images (5).jpeg)
 * Features an authentic liquid smooth pill thumb and dynamic glowing track.
 */
@Composable
fun IosLiquidSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  trackColor: Color = Color(0x35FFFFFF),
  progressColor: Color = Color(0xFF0A84FF)
) {
  var isDragging by remember { mutableStateOf(false) }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(28.dp)
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragStart = { offset ->
            isDragging = true
            val widthPx = size.width.toFloat()
            if (widthPx > 0f) {
              val newProgress = (offset.x / widthPx).coerceIn(0f, 1f)
              onValueChange(newProgress)
            }
          },
          onDragEnd = { isDragging = false },
          onDragCancel = { isDragging = false },
          onHorizontalDrag = { change, _ ->
            change.consume()
            val widthPx = size.width.toFloat()
            if (widthPx > 0f) {
              val newProgress = (change.position.x / widthPx).coerceIn(0f, 1f)
              onValueChange(newProgress)
            }
          }
        )
      },
    contentAlignment = Alignment.CenterStart
  ) {
    val totalWidth = maxWidth
    val currentX = totalWidth * value.coerceIn(0f, 1f)

    // Base Track
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(4.5.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(trackColor)
    )

    // Active Filled Track
    Box(
      modifier = Modifier
        .width(currentX)
        .height(4.5.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(
          Brush.horizontalGradient(
            listOf(progressColor.copy(alpha = 0.85f), progressColor)
          )
        )
    )

    // Smooth Liquid Pill Thumb (from images (5).jpeg)
    Box(
      modifier = Modifier
        .padding(start = (currentX - 18.dp).coerceAtLeast(0.dp))
        .width(36.dp)
        .height(18.dp)
        .shadow(8.dp, shape = RoundedCornerShape(9.dp), spotColor = progressColor.copy(alpha = 0.5f))
        .clip(RoundedCornerShape(9.dp))
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xFFFFFFFF),
              Color(0xFFE0E5F0)
            )
          )
        )
        .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(9.dp))
    )
  }
}

/**
 * TOD Match Countdown Timer (Screenshot 2)
 * [10 ساعات] : [23 دقائق] : [42 ثواني] with bold yellow numbers
 */
@Composable
fun TodCountdownTimer(
  initialHours: Int = 10,
  initialMinutes: Int = 23,
  initialSeconds: Int = 42,
  modifier: Modifier = Modifier
) {
  var remainingSec by remember {
    mutableIntStateOf(initialHours * 3600 + initialMinutes * 60 + initialSeconds)
  }

  LaunchedEffect(Unit) {
    while (remainingSec > 0) {
      delay(1000)
      remainingSec--
    }
  }

  val hours = remainingSec / 3600
  val minutes = (remainingSec % 3600) / 60
  val seconds = remainingSec % 60

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // Seconds Box
    CountdownBox(value = "%02d".format(seconds), label = "ثواني")
    Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    // Minutes Box
    CountdownBox(value = "%02d".format(minutes), label = "دقائق")
    Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    // Hours Box
    CountdownBox(value = "%d".format(hours), label = "ساعات")
  }
}

@Composable
private fun CountdownBox(value: String, label: String) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(TodGradients.CardGlass)
      .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = value,
      color = TodGold,
      fontSize = 15.sp,
      fontWeight = FontWeight.Black
    )
    Text(
      text = label,
      color = Color(0xFFDDDDDF),
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

/**
 * Match Detail & Multi-View Modal (Screenshots 1 & 2)
 * Stadium hero, tournament logo, teams, countdown, big yellow button, replay, multi-view.
 */
data class TodMatchDetail(
  val id: String,
  val title: String,
  val team1Name: String,
  val team1LogoUrl: String? = null,
  val team2Name: String,
  val team2LogoUrl: String? = null,
  val tournament: String,
  val tournamentLogoUrl: String? = null,
  val matchDateTime: String,
  val stadium: String = "",
  val isLive: Boolean = true,
  val streamUrl: String = "",
  val hoursRemaining: Int = 10,
  val minutesRemaining: Int = 23,
  val secondsRemaining: Int = 42
)

@Composable
fun TodMatchDetailSheet(
  match: TodMatchDetail,
  onClose: () -> Unit,
  onPlayNow: () -> Unit,
  onPlayCatchup: () -> Unit,
  onPlayMultiView: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isSavedToMyTod by remember { androidx.compose.runtime.mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      // 1. Hero Image / Stadium Banner with Close Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(230.dp)
      ) {
        // Gradient fallback or stadium backdrop
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color(0xFF0F2648), Color(0xFF1E0E32), Color(0xFF0A0A0C))
              )
            )
        )
        // Scrim
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color(0x55000000), Color.Transparent, Color(0xFF000000))
              )
            )
        )

        // Close 'X' Button on top-left (Screenshot 1 & 2)
        IconButton(
          onClick = onClose,
          modifier = Modifier
            .padding(top = 40.dp, start = 16.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .align(Alignment.TopStart)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "إغلاق",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        // Tournament badge on top-right (Screenshot 1)
        if (!match.tournamentLogoUrl.isNullOrBlank()) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(match.tournamentLogoUrl)
              .crossfade(false)
              .build(),
            contentDescription = null,
            modifier = Modifier
              .padding(top = 44.dp, end = 20.dp)
              .size(54.dp)
              .align(Alignment.TopEnd)
          )
        }
      }

      // 2. Teams and Match Header Info
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Teams Row (Aston Villa vs Tottenham)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          // Team 1
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1B1B22))
                .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              if (!match.team1LogoUrl.isNullOrBlank()) {
                AsyncImage(
                  model = match.team1LogoUrl,
                  contentDescription = match.team1Name,
                  modifier = Modifier.size(48.dp)
                )
              } else {
                Icon(
                  Icons.Default.Tv,
                  contentDescription = null,
                  tint = TodGold,
                  modifier = Modifier.size(28.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = match.team1Name,
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
          }

          // VS separator
          Text(
            text = "—",
            color = TodGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
          )

          // Team 2
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1B1B22))
                .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              if (!match.team2LogoUrl.isNullOrBlank()) {
                AsyncImage(
                  model = match.team2LogoUrl,
                  contentDescription = match.team2Name,
                  modifier = Modifier.size(48.dp)
                )
              } else {
                Icon(
                  Icons.Default.Tv,
                  contentDescription = null,
                  tint = TodGold,
                  modifier = Modifier.size(28.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = match.team2Name,
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Match Title & Info
        Text(
          text = match.title,
          color = Color.White,
          fontSize = 19.sp,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Countdown Timer if upcoming, or LIVE badge if active
        if (match.isLive) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(TodLiveRed)
              .padding(horizontal = 14.dp, vertical = 4.dp)
          ) {
            Text(
              text = "مباشر",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          TodCountdownTimer(
            initialHours = match.hoursRemaining,
            initialMinutes = match.minutesRemaining,
            initialSeconds = match.secondsRemaining
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Match metadata (date, time, stadium, tournament + HDR badge)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "${match.matchDateTime} • ${match.stadium} • ${match.tournament}",
            color = DarkTextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF202026))
              .border(1.dp, Color(0xFF33333E), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text("HDR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Primary Button: Big Wide Yellow Button with Liquid Gold Gradient & Spring Physics
        val matchPrimaryInteraction = remember { MutableInteractionSource() }
        val isMatchPrimaryPressed by matchPrimaryInteraction.collectIsPressedAsState()
        val matchPrimaryScale by animateFloatAsState(
          targetValue = if (isMatchPrimaryPressed) 0.95f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
          label = "matchPrimaryScale"
        )

        Box(
          modifier = Modifier
            .scale(matchPrimaryScale)
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TodGradients.LiquidGold)
            .clickable(
              interactionSource = matchPrimaryInteraction,
              indication = null
            ) {
              if (match.isLive) onPlayNow() else isSavedToMyTod = !isSavedToMyTod
            },
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            if (match.isLive) {
              Text(
                text = "تابع الآن",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
              )
            } else {
              Text(
                text = if (isSavedToMyTod) "تمت الإضافة إلى My TOD" else "+ My TOD",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Secondary Action Buttons (Screenshot 1)
        if (match.isLive) {
          // Replay Button (تابع من البداية ↺)
          Button(
            onClick = onPlayCatchup,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B26)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "تابع من البداية",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Multi-View Button (شاهد العرض المتعدد 㗊)
          Button(
            onClick = onPlayMultiView,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B26)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "شاهد العرض المتعدد",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // My TOD Add Action (Divider + "+ My TOD")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isSavedToMyTod = !isSavedToMyTod }
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isSavedToMyTod) "محفوظ في My TOD" else "My TOD",
            color = TodGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = TodGold,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Match Statistics Section (إحصائيات المباراة - Screenshot 2)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          horizontalAlignment = Alignment.End
        ) {
          Text(
            text = "إحصائيات المباراة",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF0F0F12))
              .border(1.dp, Color(0xFF1E1E24), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = TodGold,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "لا توجد بيانات متاحة حتى الآن",
                color = DarkTextSecondary,
                fontSize = 13.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

/**
 * TOD Profiles Screen: "من يشاهد الآن؟" (Screenshot 6)
 */
@Composable
fun TodProfileSelectionSheet(
  activeProfileName: String = "Main",
  onSelectProfile: (String) -> Unit,
  onManageProfiles: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "من يشاهد الآن؟",
      color = Color.White,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(48.dp))

    // Profiles Row
    Row(
      horizontalArrangement = Arrangement.spacedBy(32.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Add Profile Box
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            .clickable { /* Add profile */ },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "إضافة ملف شخصي",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "إضافة ملف شخصي",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // 2. Main Profile Box (Iconic TOD Golden Yellow Avatar)
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TodGold)
            .clickable { onSelectProfile("Main") },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Main",
            tint = Color.Black,
            modifier = Modifier.size(54.dp)
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = activeProfileName,
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Black
        )
      }
    }

    Spacer(modifier = Modifier.height(72.dp))

    // Manage Profiles Button
    Button(
      onClick = onManageProfiles,
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .height(50.dp),
      colors = ButtonDefaults.buttonColors(containerColor = TodButtonGrey),
      shape = RoundedCornerShape(10.dp)
    ) {
      Text(
        text = "إدارة الملفات الشخصية",
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
