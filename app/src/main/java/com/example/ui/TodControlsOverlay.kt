package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import com.example.model.BroadcastStream
import com.example.model.MatchMoment
import com.example.model.TodPlayerState
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodAmberYellow
import com.example.ui.theme.TodCyan

/**
 * TOD Controls Overlay: Crafted pixel-perfect to match the user's provided screenshots.
 * Top Left: Subtitles, Settings Cog with Play, 4-Grid.
 * Top Right: Title + Subtitle + Back Arrow (→).
 * Center: Replay 10 (↺ 10), Pause Bars (||) or Play Triangle (▶).
 * Right: Vertical Brightness Slider with Sun Icon & TOD Watermark.
 * Bottom: Time, Amber Yellow Seekbar, Live indicator, Fullscreen arrows.
 */
@Composable
fun TodControlsOverlay(
  stream: BroadcastStream,
  playerState: TodPlayerState,
  controlsVisible: Boolean,
  isFullscreen: Boolean,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onSeekTo: (Long) -> Unit,
  onSyncToLive: () -> Unit,
  onToggleLock: () -> Unit,
  onOpenQuality: () -> Unit,
  onOpenAudio: () -> Unit,
  onOpenSubtitles: () -> Unit,
  onOpenGrid: () -> Unit,
  onToggleFullscreen: () -> Unit,
  onNavigateBack: () -> Unit,
  onSelectMoment: (MatchMoment) -> Unit,
  onCycleAspectRatio: () -> Unit = {},
  onNextChannel: (() -> Unit)? = null,
  onPreviousChannel: (() -> Unit)? = null,
  onReloadStream: () -> Unit = {},
  onTriggerPip: () -> Unit = {},
  onToggleFavorite: () -> Unit = {},
  isFavorite: Boolean = false,
  onToggleMute: () -> Unit = {},
  brightnessLevel: Float = 0.65f,
  onBrightnessChange: (Float) -> Unit = {},
  volumeLevel: Float = 0.5f,
  onVolumeChange: (Float) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "todLiveBeacon")
  val liveDotAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(850),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dotAlpha"
  )

  Box(modifier = modifier.fillMaxSize()) {
    // If controls locked, only show minimal floating unlock button
    if (playerState.isControlsLocked) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        contentAlignment = Alignment.TopStart
      ) {
        Box(
          modifier = Modifier
            .iosBounceClick(scaleDown = 0.88f) { onToggleLock() }
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xCC101524)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Unlock Controls",
            tint = TodAmberYellow,
            modifier = Modifier.size(24.dp)
          )
        }
      }
      return@Box
    }

    // Fullscreen Controls Overlay
    AnimatedVisibility(
      visible = controlsVisible,
      enter = fadeIn(tween(220)),
      exit = fadeOut(tween(220))
    ) {
      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0xCC000000),
                  Color(0x33000000),
                  Color(0x00000000),
                  Color(0x44000000),
                  Color(0xEE000000)
                )
              )
            )
        ) {
          // ==========================================
          // 1. TOP BAR (Sleek, Balanced, Never Overflowing)
          // Left: Back button + Title & Subtitle + Resolution Badge
          // Right: Action Icons (Favorite, Subtitles, Settings, Grid, Aspect Ratio, Lock)
          // ==========================================
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.TopCenter)
              .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left Group: Back Button + Stream Title & Info
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f, fill = false)
            ) {
              // iOS Glass Back Button
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.88f) { onNavigateBack() }
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(0x441F293D)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }

              // Content Title & Category / Subtitle
              Column(modifier = Modifier.widthIn(max = 280.dp)) {
                Text(
                  text = stream.title.ifEmpty { "قناة البث المباشر" },
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                val subText = if (stream.subtitle.isNotEmpty()) stream.subtitle else stream.tournamentOrLeague
                if (subText.isNotEmpty()) {
                  Text(
                    text = subText,
                    color = Color(0xFFB0B0B0),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              // Resolution Badge (e.g. 1080p FHD, 4K UHD, 720p HD)
              val resBadge = playerState.activeResolutionBadge.ifEmpty { "FHD" }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(TodAmberYellow.copy(alpha = 0.15f))
                  .border(1.dp, TodAmberYellow.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Text(
                  text = resBadge,
                  color = TodAmberYellow,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Group: Player Action Icons
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // 1. Favorite Heart Icon
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.84f) { onToggleFavorite() }
                  .size(36.dp),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Favorite",
                  tint = if (isFavorite) Color(0xFFFF2A55) else Color.White.copy(alpha = 0.85f),
                  modifier = Modifier.size(21.dp)
                )
              }

              // 2. Subtitles icon
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.84f) { onOpenSubtitles() }
                  .size(36.dp),
                contentAlignment = Alignment.Center
              ) {
                TodSubtitles(
                  size = 22.dp,
                  tint = if (playerState.selectedSubtitleTrack != null) TodAmberYellow else Color.White
                )
              }

              // 3. Settings Cog with Play Triangle inside -> opens Audio/Quality modal
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.84f) { onOpenQuality() }
                  .size(36.dp),
                contentAlignment = Alignment.Center
              ) {
                TodSettingsCogWithPlay(size = 24.dp, tint = Color.White)
              }

              // 4. 4 Rounded Squares Grid icon -> opens channels / stream drawer
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.84f) { onOpenGrid() }
                  .size(36.dp),
                contentAlignment = Alignment.Center
              ) {
                TodGridFour(size = 22.dp, tint = Color.White)
              }

              // 5. Aspect Ratio pill button (16:9, Fit, Zoom, Stretch)
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.88f) { onCycleAspectRatio() }
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x551A2234))
                  .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 7.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Aspect Ratio",
                    tint = TodAmberYellow,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = playerState.aspectRatioMode.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              // 6. Quick Touch Lock
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.84f) { onToggleLock() }
                  .size(36.dp),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.LockOpen,
                  contentDescription = "Lock Controls",
                  tint = Color.White,
                  modifier = Modifier.size(19.dp)
                )
              }
            }
          }

          // ==========================================
          // 2. CENTER CONTROLS (Strictly Symmetrical & Mathematically Dead-Centered)
          // Play button is ALWAYS at 50% X, 50% Y.
          // Left: Previous Channel + Replay 10 (fixed width pod)
          // Right: Forward 10 + Next Channel (fixed width pod)
          // ==========================================
          val playInteractionSource = remember { MutableInteractionSource() }
          val isPlayPressed by playInteractionSource.collectIsPressedAsState()
          val playScale by animateFloatAsState(
            targetValue = if (isPlayPressed) 0.88f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "playBtnScale"
          )

          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              // Left Pod: Previous Channel (|◀) + Replay 10 (Fixed 130.dp width, right-aligned)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.width(130.dp)
              ) {
                if (onPreviousChannel != null) {
                  Box(
                    modifier = Modifier
                      .iosBounceClick(scaleDown = 0.84f) { onPreviousChannel() }
                      .size(46.dp)
                      .clip(CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.SkipPrevious,
                      contentDescription = "Previous Channel",
                      tint = Color.White.copy(alpha = 0.9f),
                      modifier = Modifier.size(32.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                }

                // Replay 10 Seconds
                Box(
                  modifier = Modifier
                    .iosBounceClick(scaleDown = 0.84f) { onSeekBackward() }
                    .size(52.dp),
                  contentAlignment = Alignment.Center
                ) {
                  TodReplay10(size = 46.dp, tint = Color.White)
                }
              }

              Spacer(modifier = Modifier.width(28.dp))

              // Dead Center: Pause Bars (||) or Play Triangle (▶)
              Box(
                modifier = Modifier
                  .scale(playScale)
                  .size(76.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.radialGradient(
                      colors = listOf(Color(0x33FDB913), Color(0x11000000), Color.Transparent)
                    )
                  )
                  .clickable(
                    interactionSource = playInteractionSource,
                    indication = null
                  ) { onTogglePlayPause() },
                contentAlignment = Alignment.Center
              ) {
                if (playerState.isBuffering) {
                  CircularProgressIndicator(
                    color = TodAmberYellow,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp)
                  )
                } else if (playerState.isPlaying) {
                  TodPauseBars(
                    width = 11.dp,
                    height = 54.dp,
                    gap = 14.dp,
                    tint = Color.White.copy(alpha = 0.92f)
                  )
                } else {
                  TodPlayTriangle(
                    size = 52.dp,
                    tint = Color.White.copy(alpha = 0.92f)
                  )
                }
              }

              Spacer(modifier = Modifier.width(28.dp))

              // Right Pod: Forward 10 + Next Channel (▶|) (Fixed 130.dp width, left-aligned)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.width(130.dp)
              ) {
                // Forward 10 Seconds
                Box(
                  modifier = Modifier
                    .iosBounceClick(scaleDown = 0.84f) { onSeekForward() }
                    .size(52.dp),
                  contentAlignment = Alignment.Center
                ) {
                  TodForward10(size = 46.dp, tint = Color.White)
                }

                if (onNextChannel != null) {
                  Spacer(modifier = Modifier.width(12.dp))
                  Box(
                    modifier = Modifier
                      .iosBounceClick(scaleDown = 0.84f) { onNextChannel() }
                      .size(46.dp)
                      .clip(CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.SkipNext,
                      contentDescription = "Next Channel",
                      tint = Color.White.copy(alpha = 0.9f),
                      modifier = Modifier.size(32.dp)
                    )
                  }
                }
              }
            }
          }

          // ==========================================
          // 3. LEFT & RIGHT VERTICAL SLIDERS (True Left & True Right)
          // ==========================================
          // Physical Left side: Volume slider
          Column(
            modifier = Modifier
              .align(Alignment.CenterStart)
              .padding(start = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .width(5.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0x66555555))
                .pointerInput(Unit) {
                  detectVerticalDragGestures { _, dragAmount ->
                    val delta = -dragAmount / 90f
                    val newLevel = (volumeLevel + delta).coerceIn(0.0f, 1.0f)
                    onVolumeChange(newLevel)
                  }
                }
            ) {
              Box(
                modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .fillMaxHeight(volumeLevel)
                  .clip(RoundedCornerShape(3.dp))
                  .background(Color(0xFFCCCCCC))
              )
            }

            Icon(
              imageVector = Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = "Volume",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          // Physical Right side: Brightness slider
          Column(
            modifier = Modifier
              .align(Alignment.CenterEnd)
              .padding(end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .width(5.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0x66555555))
                .pointerInput(Unit) {
                  detectVerticalDragGestures { _, dragAmount ->
                    val delta = -dragAmount / 90f
                    val newLevel = (brightnessLevel + delta).coerceIn(0.1f, 1.0f)
                    onBrightnessChange(newLevel)
                  }
                }
            ) {
              Box(
                modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .fillMaxHeight(brightnessLevel)
                  .clip(RoundedCornerShape(3.dp))
                  .background(Color(0xFFCCCCCC))
              )
            }

            TodSunBrightness(size = 20.dp, tint = Color.White)
          }

          // ==========================================
          // 4. BOTTOM BAR & TIMELINE (True LTR)
          // ==========================================
          val currentProgress = if (playerState.durationMs > 0) {
            (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
          } else 1.0f

          var isSeeking by remember { mutableStateOf(false) }
          var seekProgress by remember { mutableFloatStateOf(0f) }
          val displayProgress = if (isSeeking) seekProgress else currentProgress

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.BottomCenter)
              .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Left: Elapsed Time (e.g. "04:40")
            Text(
              text = formatTime(if (isSeeking && playerState.durationMs > 0) (seekProgress * playerState.durationMs).toLong() else playerState.currentPositionMs),
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Middle: iOS Liquid Seek Bar
            IosLiquidSlider(
              value = displayProgress,
              onValueChange = { frac ->
                if (playerState.durationMs > 0) {
                  onSeekTo((frac * playerState.durationMs).toLong())
                }
              },
              progressColor = TodAmberYellow,
              modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Right: Mute + Live badge + Fullscreen toggle icon
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // Mute Toggle Icon
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.84f) { onToggleMute() }
                  .size(32.dp),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (playerState.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = "Mute Toggle",
                  tint = if (playerState.isMuted) Color(0xFFFF4D4D) else Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }

              // Live Badge: Pulsing live beacon
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.90f) { onSyncToLive() }
              ) {
                PulsingLiveBadge(
                  fontSize = 12.sp,
                  paddingHorizontal = 9.dp,
                  paddingVertical = 4.dp
                )
              }

              // Fullscreen Icon
              Box(
                modifier = Modifier
                  .iosBounceClick(scaleDown = 0.84f) { onToggleFullscreen() }
                  .size(32.dp),
                contentAlignment = Alignment.Center
              ) {
                TodFullscreenArrows(size = 18.dp, tint = Color.White)
              }
            }
          }
        }
      }
    }
  }
}

private fun formatTime(millis: Long): String {
  val totalSeconds = (millis / 1000).coerceAtLeast(0)
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60
  return if (hours > 0) {
    "%02d:%02d:%02d".format(hours, minutes, seconds)
  } else {
    "%02d:%02d".format(minutes, seconds)
  }
}
