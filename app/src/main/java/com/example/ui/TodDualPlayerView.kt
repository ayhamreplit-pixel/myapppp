package com.example.ui

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.BroadcastStream
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.TodAmberYellow
import com.example.ui.theme.TodCyan
import com.example.ui.theme.TodLiveRed

/**
 * Modern High-Performance Dual-Stream (Multi-View) Player View.
 * Displays two live broadcast streams side-by-side (or top/bottom in portrait)
 * with independent audio routing, live beacons, active stream highlight, and channel swap.
 */
@OptIn(UnstableApi::class)
@Composable
fun TodDualPlayerView(
  stream1: BroadcastStream,
  stream2: BroadcastStream,
  onClose: () -> Unit,
  onChangeChannel1: () -> Unit,
  onChangeChannel2: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Player 1 state
  var isBuffering1 by remember { mutableStateOf(true) }
  var isPlaying1 by remember { mutableStateOf(false) }

  // Player 2 state
  var isBuffering2 by remember { mutableStateOf(true) }
  var isPlaying2 by remember { mutableStateOf(false) }

  // Audio focus: which player has active audio (1 or 2)
  var activeAudioPlayer by remember { mutableIntStateOf(1) }

  // Stream swap state
  var currentStream1 by remember(stream1) { mutableStateOf(stream1) }
  var currentStream2 by remember(stream2) { mutableStateOf(stream2) }

  val exoPlayer1 = remember {
    ExoPlayer.Builder(context).build().apply {
      repeatMode = Player.REPEAT_MODE_OFF
      playWhenReady = true
      volume = 1.0f
      addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
          isBuffering1 = (state == Player.STATE_BUFFERING)
          isPlaying1 = (state == Player.STATE_READY && playWhenReady)
        }
        override fun onIsPlayingChanged(isPlaying: Boolean) {
          isPlaying1 = isPlaying
        }
      })
    }
  }

  val exoPlayer2 = remember {
    ExoPlayer.Builder(context).build().apply {
      repeatMode = Player.REPEAT_MODE_OFF
      playWhenReady = true
      volume = 0.0f
      addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
          isBuffering2 = (state == Player.STATE_BUFFERING)
          isPlaying2 = (state == Player.STATE_READY && playWhenReady)
        }
        override fun onIsPlayingChanged(isPlaying: Boolean) {
          isPlaying2 = isPlaying
        }
      })
    }
  }

  // Load stream 1
  DisposableEffect(currentStream1) {
    if (currentStream1.streamUrl.isNotBlank()) {
      exoPlayer1.setMediaItem(MediaItem.fromUri(currentStream1.streamUrl))
      exoPlayer1.prepare()
      exoPlayer1.play()
    }
    onDispose {}
  }

  // Load stream 2
  DisposableEffect(currentStream2) {
    if (currentStream2.streamUrl.isNotBlank()) {
      exoPlayer2.setMediaItem(MediaItem.fromUri(currentStream2.streamUrl))
      exoPlayer2.prepare()
      exoPlayer2.play()
    }
    onDispose {}
  }

  // Handle audio focus switching
  DisposableEffect(activeAudioPlayer) {
    if (activeAudioPlayer == 1) {
      exoPlayer1.volume = 1.0f
      exoPlayer2.volume = 0.0f
    } else {
      exoPlayer1.volume = 0.0f
      exoPlayer2.volume = 1.0f
    }
    onDispose {}
  }

  // Cleanup on exit
  DisposableEffect(Unit) {
    onDispose {
      exoPlayer1.release()
      exoPlayer2.release()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Control Bar for Dual-Stream
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              listOf(Color(0xEE080B11), Color(0x99080B11), Color.Transparent)
            )
          )
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(
                Brush.linearGradient(listOf(TodCyan, Color(0xFF0077FF)))
              )
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "DUAL STREAM",
              color = Color.Black,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black
            )
          }

          Text(
            text = "بث مباشر ثنائي - قناتين في نفس الوقت",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Swap streams button
          IconButton(
            onClick = {
              val temp = currentStream1
              currentStream1 = currentStream2
              currentStream2 = temp
            },
            modifier = Modifier
              .clip(CircleShape)
              .background(Color(0x33FFFFFF))
              .size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "Swap Streams",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          // Close button
          IconButton(
            onClick = onClose,
            modifier = Modifier
              .clip(CircleShape)
              .background(Color(0x33FF2A55))
              .size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Dual Stream",
              tint = Color(0xFFFF5252),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      // Main Split-Screen View: Side-by-Side dual players
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Stream 1 Container
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(
              width = if (activeAudioPlayer == 1) 2.dp else 1.dp,
              color = if (activeAudioPlayer == 1) TodAmberYellow else DarkSurfaceBorder,
              shape = RoundedCornerShape(14.dp)
            )
            .clickable { activeAudioPlayer = 1 }
        ) {
          AndroidView(
            factory = { ctx ->
              PlayerView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = false
                player = exoPlayer1
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                keepScreenOn = true
              }
            },
            modifier = Modifier.fillMaxSize()
          )

          // Stream 1 Info & Controls Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.TopCenter)
              .background(
                Brush.verticalGradient(
                  listOf(Color(0xCC000000), Color.Transparent)
                )
              )
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f, fill = false)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(TodLiveRed)
              )
              Text(
                text = currentStream1.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Sound status badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (activeAudioPlayer == 1) TodAmberYellow.copy(alpha = 0.25f) else Color(0x33000000))
                  .clickable { activeAudioPlayer = 1 }
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (activeAudioPlayer == 1) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Audio Stream 1",
                    tint = if (activeAudioPlayer == 1) TodAmberYellow else Color(0xFF888888),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = if (activeAudioPlayer == 1) "الصوت نشط" else "مكتوم",
                    color = if (activeAudioPlayer == 1) TodAmberYellow else Color(0xFF888888),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Change channel button
              IconButton(
                onClick = onChangeChannel1,
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.GridView,
                  contentDescription = "Change Channel 1",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          // Buffering Indicator 1
          if (isBuffering1) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(
                color = TodAmberYellow,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
              )
            }
          }
        }

        // Stream 2 Container
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(
              width = if (activeAudioPlayer == 2) 2.dp else 1.dp,
              color = if (activeAudioPlayer == 2) TodAmberYellow else DarkSurfaceBorder,
              shape = RoundedCornerShape(14.dp)
            )
            .clickable { activeAudioPlayer = 2 }
        ) {
          AndroidView(
            factory = { ctx ->
              PlayerView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = false
                player = exoPlayer2
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                keepScreenOn = true
              }
            },
            modifier = Modifier.fillMaxSize()
          )

          // Stream 2 Info & Controls Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.TopCenter)
              .background(
                Brush.verticalGradient(
                  listOf(Color(0xCC000000), Color.Transparent)
                )
              )
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f, fill = false)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(TodLiveRed)
              )
              Text(
                text = currentStream2.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Sound status badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (activeAudioPlayer == 2) TodAmberYellow.copy(alpha = 0.25f) else Color(0x33000000))
                  .clickable { activeAudioPlayer = 2 }
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (activeAudioPlayer == 2) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Audio Stream 2",
                    tint = if (activeAudioPlayer == 2) TodAmberYellow else Color(0xFF888888),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = if (activeAudioPlayer == 2) "الصوت نشط" else "مكتوم",
                    color = if (activeAudioPlayer == 2) TodAmberYellow else Color(0xFF888888),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Change channel button
              IconButton(
                onClick = onChangeChannel2,
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.GridView,
                  contentDescription = "Change Channel 2",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          // Buffering Indicator 2
          if (isBuffering2) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(
                color = TodAmberYellow,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
              )
            }
          }
        }
      }
    }
  }
}
