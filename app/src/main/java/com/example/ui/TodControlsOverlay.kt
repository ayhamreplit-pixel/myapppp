package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
            .clip(CircleShape)
            .background(Color(0xCC101524))
            .clickable { onToggleLock() }
            .padding(12.dp)
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
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0xCC000000),
                Color(0x22000000),
                Color(0x22000000),
                Color(0xE6000000)
              )
            )
          )
      ) {
        // ==========================================
        // 1. TOP BAR (Screenshots 2, 4, 7, 9, 12, 13)
        // Strictly LTR: Left has tools (Subtitles, Settings, Grid, Lock); Right has Title & Back Arrow (→)
        // ==========================================
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.TopCenter)
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.horizontalGradient(
                  colors = listOf(
                    Color(0xCC0B101E),
                    Color(0x99111827),
                    Color(0xCC0B101E)
                  )
                )
              )
              .border(
                1.dp,
                Brush.horizontalGradient(
                  listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.20f)
                  )
                ),
                RoundedCornerShape(16.dp)
              )
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left side icons: Subtitles, Settings Cog, 4-Grid, Aspect Ratio, Sleep Timer, PiP, Reload, Camera, Lock
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // 1. Subtitles icon (Screenshot 7)
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clickable { onOpenSubtitles() },
                contentAlignment = Alignment.Center
              ) {
                TodSubtitles(
                  size = 24.dp,
                  tint = if (playerState.selectedSubtitleTrack != null) TodAmberYellow else Color.White
                )
              }

              // 2. Settings Cog with Play Triangle inside (Screenshot 2 & 7) -> opens Audio/Quality modal
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clickable { onOpenQuality() },
                contentAlignment = Alignment.Center
              ) {
                TodSettingsCogWithPlay(size = 26.dp, tint = Color.White)
              }

              // 3. 4 Rounded Squares Grid icon (Screenshot 2 & 12) -> opens channels / stream hub
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clickable { onOpenGrid() },
                contentAlignment = Alignment.Center
              ) {
                TodGridFour(size = 24.dp, tint = Color.White)
              }

              // 4. Aspect Ratio pill button (16:9, Fit, Zoom, Stretch)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x551A2234))
                  .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                  .clickable { onCycleAspectRatio() }
                  .padding(horizontal = 8.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Aspect Ratio",
                    tint = TodAmberYellow,
                    modifier = Modifier.size(15.dp)
                  )
                  Text(
                    text = playerState.aspectRatioMode.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              // 5. Quick Touch Lock (locks gestures for safe viewing)
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clickable { onToggleLock() },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.LockOpen,
                  contentDescription = "Lock Controls",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            // Right side: Favorite + Quality Badge + Match / Content Title + Subtitle + Back Arrow (→)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Favorite Heart Icon
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clickable { onToggleFavorite() },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Favorite",
                  tint = if (isFavorite) Color(0xFFFF2A55) else Color.White.copy(alpha = 0.85f),
                  modifier = Modifier.size(22.dp)
                )
              }

              // Resolution Badge (e.g. 1080p FHD, 4K UHD, 720p HD)
              val resBadge = playerState.activeResolutionBadge.ifEmpty { "HD" }
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
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable { onNavigateBack() }
              ) {
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = stream.title.ifEmpty { "Premier League" },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  if (stream.subtitle.isNotEmpty() || stream.tournamentOrLeague.isNotEmpty()) {
                    Text(
                      text = if (stream.subtitle.isNotEmpty()) stream.subtitle else stream.tournamentOrLeague,
                      color = Color(0xFFB0B0B0),
                      fontSize = 11.sp,
                      maxLines = 1
                    )
                  }
                }

                // Thin white back arrow → (Screenshot 4)
                TodArrowBackRtl(size = 24.dp, tint = Color.White)
              }
            }
          }
        }

        // ==========================================
        // 2. CENTER CONTROLS (Screenshots 3, 9, 12, 13)
        // Includes: Previous Channel (|◀), Replay 10, Play/Pause, Forward 10, Next Channel (▶|)
        // ==========================================
        Row(
          modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.68f),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Previous Channel (|◀)
          if (onPreviousChannel != null) {
            Box(
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .clickable { onPreviousChannel() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous Channel",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(34.dp)
              )
            }
          }

          // Replay 10 Seconds: Circular arrow with "10" inside (Screenshot 3)
          Box(
            modifier = Modifier
              .size(54.dp)
              .clickable { onSeekBackward() },
            contentAlignment = Alignment.Center
          ) {
            TodReplay10(size = 46.dp, tint = Color.White)
          }

          // Center Pause Bars (||) or Play Triangle (▶)
          Box(
            modifier = Modifier
              .size(72.dp)
              .clickable { onTogglePlayPause() },
            contentAlignment = Alignment.Center
          ) {
            if (playerState.isBuffering) {
              CircularProgressIndicator(
                color = TodAmberYellow,
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
              )
            } else if (playerState.isPlaying) {
              // Two tall rounded vertical pill bars (Screenshot 3 & 9)
              TodPauseBars(
                width = 11.dp,
                height = 54.dp,
                gap = 14.dp,
                tint = Color.White.copy(alpha = 0.88f)
              )
            } else {
              // Sleek play triangle
              TodPlayTriangle(
                size = 52.dp,
                tint = Color.White.copy(alpha = 0.88f)
              )
            }
          }

          // Forward 10 Seconds: Circular arrow with "10" inside
          Box(
            modifier = Modifier
              .size(54.dp)
              .clickable { onSeekForward() },
            contentAlignment = Alignment.Center
          ) {
            TodForward10(size = 46.dp, tint = Color.White)
          }

          // Next Channel (▶|)
          if (onNextChannel != null) {
            Box(
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .clickable { onNextChannel() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next Channel",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(34.dp)
              )
            }
          }
        }

        // ==========================================
        // 3. LEFT & RIGHT VERTICAL SLIDERS & WATERMARK (Screenshot 5 & 9)
        // ==========================================
        // Left side vertical volume slider
        Column(
          modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 16.dp),
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

        // Right side vertical brightness slider capsule (Screenshot 5)
        Column(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
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
            // Filled vertical level
            Box(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(brightnessLevel)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFCCCCCC))
            )
          }

          // Sun / Brightness Icon underneath slider (Screenshot 5)
          TodSunBrightness(size = 20.dp, tint = Color.White)
        }

        // ==========================================
        // 4. BOTTOM BAR & TIMELINE (Screenshot 100% Match)
        // ==========================================
        val currentProgress = if (playerState.durationMs > 0) {
          (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 1.0f // Live default full line

        var isSeeking by remember { mutableStateOf(false) }
        var seekProgress by remember { mutableFloatStateOf(0f) }
        val displayProgress = if (isSeeking) seekProgress else currentProgress

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.BottomCenter)
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.horizontalGradient(
                  colors = listOf(
                    Color(0xCC0B101E),
                    Color(0x99111827),
                    Color(0xCC0B101E)
                  )
                )
              )
              .border(
                1.dp,
                Brush.horizontalGradient(
                  listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.20f)
                  )
                ),
                RoundedCornerShape(16.dp)
              )
              .padding(horizontal = 16.dp, vertical = 10.dp),
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

            Spacer(modifier = Modifier.width(12.dp))

            // Middle: Horizontal Solid Yellow Seek Bar
            Box(
              modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .pointerInput(playerState.durationMs) {
                  detectTapGestures { offset ->
                    val w = size.width.toFloat()
                    if (w > 0 && playerState.durationMs > 0) {
                      val frac = (offset.x / w).coerceIn(0f, 1f)
                      onSeekTo((frac * playerState.durationMs).toLong())
                    }
                  }
                }
                .pointerInput(playerState.durationMs) {
                  detectHorizontalDragGestures(
                    onDragStart = { offset ->
                      val w = size.width.toFloat()
                      if (w > 0) {
                        isSeeking = true
                        seekProgress = (offset.x / w).coerceIn(0f, 1f)
                      }
                    },
                    onDragEnd = {
                      if (isSeeking && playerState.durationMs > 0) {
                        onSeekTo((seekProgress * playerState.durationMs).toLong())
                      }
                      isSeeking = false
                    },
                    onDragCancel = {
                      isSeeking = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                      val w = size.width.toFloat()
                      if (w > 0) {
                        seekProgress = (seekProgress + dragAmount / w).coerceIn(0f, 1f)
                      }
                    }
                  )
                },
              contentAlignment = Alignment.CenterStart
            ) {
              // Background track: subtle translucent bar
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(Color(0x55555555))
              )

              // Active track: Solid TOD Amber Yellow bar
              Box(
                modifier = Modifier
                  .fillMaxWidth(displayProgress)
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(TodAmberYellow)
              )

              // Smooth circular glow thumb at the end of progress
              Box(
                modifier = Modifier
                  .fillMaxWidth(displayProgress)
              ) {
                Box(
                  modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(TodAmberYellow)
                    .border(1.5.dp, Color.White, CircleShape)
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Mute + Live badge + Fullscreen toggle icon
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // Mute Toggle Icon
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clickable { onToggleMute() },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (playerState.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = "Mute Toggle",
                  tint = if (playerState.isMuted) Color(0xFFFF4D4D) else Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }

              // Live Badge: Red circle with "مباشر" Arabic text
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clickable { onSyncToLive() }
                  .padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                // Red circle dot
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .background(Color(0xFFFF2A55).copy(alpha = liveDotAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "مباشر",
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              // Fullscreen Icon
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clickable { onToggleFullscreen() },
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
