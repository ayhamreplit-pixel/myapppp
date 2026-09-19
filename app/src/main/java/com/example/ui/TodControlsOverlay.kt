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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
 * Bottom: Time, Amber Yellow Seekbar, "مباشر 🔴", Fullscreen arrows.
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
  brightnessLevel: Float = 0.65f,
  onBrightnessChange: (Float) -> Unit = {},
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
        // ==========================================
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .padding(horizontal = 20.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left side icons: Subtitles, Settings Cog with Play inside, 4-Grid
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
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
          }

          // Right side: Match / Content Title + Subtitle + Back Arrow (→) (Screenshot 4, 9, 12, 13)
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

        // ==========================================
        // 2. CENTER CONTROLS (Screenshots 3, 9, 12, 13)
        // ==========================================
        Row(
          modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.55f),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
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

          // Forward placeholder or spacer
          Spacer(modifier = Modifier.size(54.dp))
        }

        // ==========================================
        // 3. RIGHT SIDE VERTICAL SLIDER & WATERMARK (Screenshot 5 & 9)
        // ==========================================
        Column(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Vertical Brightness slider capsule (Screenshot 5)
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

          Spacer(modifier = Modifier.height(10.dp))

          // TOD Dolphin crest watermark badge (Screenshots 9, 12, 13)
          TodWatermarkBadge(size = 38.dp)
        }

        // ==========================================
        // 4. BOTTOM BAR & TIMELINE (Screenshots 6, 9, 12, 13)
        // ==========================================
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
          // Match moment tags (if any)
          if (stream.moments.isNotEmpty() && playerState.durationMs > 0) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              stream.moments.take(4).forEach { moment ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x991A1A1A))
                    .clickable { onSelectMoment(moment) }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = "${moment.minuteText} ${moment.description.take(14)}...",
                    color = TodAmberYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }

          // Sleek Solid Amber Yellow Seekbar (Screenshot 6, 9, 12, 13)
          Slider(
            value = if (playerState.durationMs > 0) {
              (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f,
            onValueChange = { frac ->
              val target = (frac * playerState.durationMs).toLong()
              onSeekTo(target)
            },
            colors = SliderDefaults.colors(
              thumbColor = TodAmberYellow,
              activeTrackColor = TodAmberYellow,
              inactiveTrackColor = Color(0x66444444)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(18.dp)
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Bottom Info Row: Elapsed Time on Left, Live indicator & Fullscreen on Right
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left: Elapsed Time (Screenshot 6 & 9: e.g. "04:37" or "55:44")
            Text(
              text = formatTime(playerState.currentPositionMs),
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )

            // Right: "مباشر 🔴" Live badge + Fullscreen toggle icon (Screenshot 6, 12, 13)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              // Live Badge: Red circle with "مباشر" Arabic text
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clickable { onSyncToLive() }
                  .padding(4.dp)
              ) {
                // Outer red ring with filled center
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFFE50914), CircleShape)
                    .background(Color(0xFFE50914).copy(alpha = liveDotAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "مباشر",
                  color = Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              // Fullscreen Icon (Screenshot 6, 12, 13: 4 outward corner arrows)
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clickable { onToggleFullscreen() },
                contentAlignment = Alignment.Center
              ) {
                TodFullscreenArrows(size = 20.dp, tint = Color.White)
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
