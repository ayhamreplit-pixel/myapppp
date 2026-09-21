package com.example.ui

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.BroadcastStream
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.TodLiveRed

/**
 * Modern High-Performance Multi-Stream Player (Dual & Quad 4-Screen).
 * Displays up to 4 simultaneous live broadcast streams in a clean, dark grid
 * with independent audio focus routing, channel switching, and single-screen expansion.
 */
@OptIn(UnstableApi::class)
@Composable
fun TodDualPlayerView(
  stream1: BroadcastStream,
  stream2: BroadcastStream,
  stream3: BroadcastStream? = null,
  stream4: BroadcastStream? = null,
  onClose: () -> Unit,
  onChangeChannel1: () -> Unit,
  onChangeChannel2: () -> Unit,
  onChangeChannel3: (() -> Unit)? = null,
  onChangeChannel4: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val themePrimary = MaterialTheme.colorScheme.primary

  // Multi-screen mode: 2 (Dual) or 4 (Quad)
  var screenCount by remember { mutableIntStateOf(if (stream3 != null || stream4 != null) 4 else 2) }

  // Audio focus: 1, 2, 3, or 4
  var activeAudioPlayer by remember { mutableIntStateOf(1) }

  // Expanded single player index (0 = grid view, 1..4 = full view)
  var expandedPlayerIndex by remember { mutableIntStateOf(0) }

  // Channels state
  var s1 by remember(stream1) { mutableStateOf(stream1) }
  var s2 by remember(stream2) { mutableStateOf(stream2) }
  var s3 by remember(stream3) { mutableStateOf(stream3 ?: stream1) }
  var s4 by remember(stream4) { mutableStateOf(stream4 ?: stream2) }

  // Buffering flags
  var isBuf1 by remember { mutableStateOf(true) }
  var isBuf2 by remember { mutableStateOf(true) }
  var isBuf3 by remember { mutableStateOf(true) }
  var isBuf4 by remember { mutableStateOf(true) }

  // Helper to create high-compatibility ExoPlayer instances
  fun createUniversalPlayer(): ExoPlayer {
    val renderers = DefaultRenderersFactory(context)
      .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
      .setEnableDecoderFallback(true)
      .setAllowedVideoJoiningTimeMs(5000)
    val selector = DefaultTrackSelector(context).apply {
      setParameters(
        buildUponParameters()
          .setExceedRendererCapabilitiesIfNecessary(true)
          .setAllowAudioMixedMimeTypeAdaptiveness(true)
          .setAllowAudioNonSeamlessAdaptiveness(true)
      )
    }
    return ExoPlayer.Builder(context, renderers)
      .setTrackSelector(selector)
      .build().apply {
        repeatMode = Player.REPEAT_MODE_OFF
        playWhenReady = true
      }
  }

  // Player instances
  val exo1 = remember {
    createUniversalPlayer().apply {
      volume = 1.0f
      addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(st: Int) {
          isBuf1 = (st == Player.STATE_BUFFERING)
        }
      })
    }
  }

  val exo2 = remember {
    createUniversalPlayer().apply {
      volume = 0.0f
      addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(st: Int) {
          isBuf2 = (st == Player.STATE_BUFFERING)
        }
      })
    }
  }

  val exo3 = remember {
    createUniversalPlayer().apply {
      volume = 0.0f
      addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(st: Int) {
          isBuf3 = (st == Player.STATE_BUFFERING)
        }
      })
    }
  }

  val exo4 = remember {
    createUniversalPlayer().apply {
      volume = 0.0f
      addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(st: Int) {
          isBuf4 = (st == Player.STATE_BUFFERING)
        }
      })
    }
  }

  // Load streams
  DisposableEffect(s1) {
    if (s1.streamUrl.isNotBlank()) {
      exo1.setMediaItem(MediaItem.fromUri(s1.streamUrl))
      exo1.prepare()
      exo1.play()
    }
    onDispose {}
  }

  DisposableEffect(s2) {
    if (s2.streamUrl.isNotBlank()) {
      exo2.setMediaItem(MediaItem.fromUri(s2.streamUrl))
      exo2.prepare()
      exo2.play()
    }
    onDispose {}
  }

  DisposableEffect(s3, screenCount) {
    if (screenCount == 4 && s3.streamUrl.isNotBlank()) {
      exo3.setMediaItem(MediaItem.fromUri(s3.streamUrl))
      exo3.prepare()
      exo3.play()
    } else {
      exo3.stop()
    }
    onDispose {}
  }

  DisposableEffect(s4, screenCount) {
    if (screenCount == 4 && s4.streamUrl.isNotBlank()) {
      exo4.setMediaItem(MediaItem.fromUri(s4.streamUrl))
      exo4.prepare()
      exo4.play()
    } else {
      exo4.stop()
    }
    onDispose {}
  }

  // Audio volume routing
  DisposableEffect(activeAudioPlayer) {
    exo1.volume = if (activeAudioPlayer == 1) 1.0f else 0.0f
    exo2.volume = if (activeAudioPlayer == 2) 1.0f else 0.0f
    exo3.volume = if (activeAudioPlayer == 3) 1.0f else 0.0f
    exo4.volume = if (activeAudioPlayer == 4) 1.0f else 0.0f
    onDispose {}
  }

  // Release on disposal
  DisposableEffect(Unit) {
    onDispose {
      exo1.release()
      exo2.release()
      exo3.release()
      exo4.release()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Control Bar (Clean Dark Theme)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF0F141C))
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(themePrimary)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = if (screenCount == 4) "QUAD 4X" else "DUAL 2X",
              color = Color.Black,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black
            )
          }

          Text(
            text = if (screenCount == 4) "عرض متعدد: 4 شاشات متزامنة" else "عرض مزدوج: شاشتان معاً",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Toggle Dual / Quad mode
          IconButton(
            onClick = {
              screenCount = if (screenCount == 2) 4 else 2
              expandedPlayerIndex = 0
            },
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1E2638))
              .size(36.dp)
          ) {
            Icon(
              imageVector = if (screenCount == 2) Icons.Default.Window else Icons.Default.ViewAgenda,
              contentDescription = "Toggle Grid Mode",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }

          // Swap Stream 1 & 2
          IconButton(
            onClick = {
              val temp = s1
              s1 = s2
              s2 = temp
            },
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1E2638))
              .size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "Swap Streams",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }

          // Close Multi-view
          IconButton(
            onClick = onClose,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF33141E))
              .size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Multi View",
              tint = Color(0xFFFF5252),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Main Content Area (Dual or Quad Grid)
      if (expandedPlayerIndex != 0) {
        // Single Expanded Stream View
        val (currentExo, currentStream, playerIdx) = when (expandedPlayerIndex) {
          1 -> Triple(exo1, s1, 1)
          2 -> Triple(exo2, s2, 2)
          3 -> Triple(exo3, s3, 3)
          else -> Triple(exo4, s4, 4)
        }
        val isBuf = when (expandedPlayerIndex) {
          1 -> isBuf1
          2 -> isBuf2
          3 -> isBuf3
          else -> isBuf4
        }

        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
        ) {
          SingleStreamSlot(
            exo = currentExo,
            stream = currentStream,
            isBuffering = isBuf,
            isAudioActive = activeAudioPlayer == playerIdx,
            onSelectAudio = { activeAudioPlayer = playerIdx },
            onChangeChannel = {
              when (playerIdx) {
                1 -> onChangeChannel1()
                2 -> onChangeChannel2()
                3 -> onChangeChannel3?.invoke()
                4 -> onChangeChannel4?.invoke()
              }
            },
            isExpanded = true,
            onToggleExpand = { expandedPlayerIndex = 0 },
            themePrimary = themePrimary,
            modifier = Modifier.fillMaxSize()
          )
        }
      } else if (screenCount == 2) {
        // Dual Screen Layout (Side by side)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          SingleStreamSlot(
            exo = exo1,
            stream = s1,
            isBuffering = isBuf1,
            isAudioActive = activeAudioPlayer == 1,
            onSelectAudio = { activeAudioPlayer = 1 },
            onChangeChannel = onChangeChannel1,
            isExpanded = false,
            onToggleExpand = { expandedPlayerIndex = 1 },
            themePrimary = themePrimary,
            modifier = Modifier.weight(1f).fillMaxHeight()
          )

          SingleStreamSlot(
            exo = exo2,
            stream = s2,
            isBuffering = isBuf2,
            isAudioActive = activeAudioPlayer == 2,
            onSelectAudio = { activeAudioPlayer = 2 },
            onChangeChannel = onChangeChannel2,
            isExpanded = false,
            onToggleExpand = { expandedPlayerIndex = 2 },
            themePrimary = themePrimary,
            modifier = Modifier.weight(1f).fillMaxHeight()
          )
        }
      } else {
        // Quad Screen Layout (2x2 Grid)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Top Row: Quad 1 & 2
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            SingleStreamSlot(
              exo = exo1,
              stream = s1,
              isBuffering = isBuf1,
              isAudioActive = activeAudioPlayer == 1,
              onSelectAudio = { activeAudioPlayer = 1 },
              onChangeChannel = onChangeChannel1,
              isExpanded = false,
              onToggleExpand = { expandedPlayerIndex = 1 },
              themePrimary = themePrimary,
              modifier = Modifier.weight(1f).fillMaxHeight()
            )

            SingleStreamSlot(
              exo = exo2,
              stream = s2,
              isBuffering = isBuf2,
              isAudioActive = activeAudioPlayer == 2,
              onSelectAudio = { activeAudioPlayer = 2 },
              onChangeChannel = onChangeChannel2,
              isExpanded = false,
              onToggleExpand = { expandedPlayerIndex = 2 },
              themePrimary = themePrimary,
              modifier = Modifier.weight(1f).fillMaxHeight()
            )
          }

          // Bottom Row: Quad 3 & 4
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            SingleStreamSlot(
              exo = exo3,
              stream = s3,
              isBuffering = isBuf3,
              isAudioActive = activeAudioPlayer == 3,
              onSelectAudio = { activeAudioPlayer = 3 },
              onChangeChannel = { onChangeChannel3?.invoke() ?: onChangeChannel1() },
              isExpanded = false,
              onToggleExpand = { expandedPlayerIndex = 3 },
              themePrimary = themePrimary,
              modifier = Modifier.weight(1f).fillMaxHeight()
            )

            SingleStreamSlot(
              exo = exo4,
              stream = s4,
              isBuffering = isBuf4,
              isAudioActive = activeAudioPlayer == 4,
              onSelectAudio = { activeAudioPlayer = 4 },
              onChangeChannel = { onChangeChannel4?.invoke() ?: onChangeChannel2() },
              isExpanded = false,
              onToggleExpand = { expandedPlayerIndex = 4 },
              themePrimary = themePrimary,
              modifier = Modifier.weight(1f).fillMaxHeight()
            )
          }
        }
      }
    }
  }
}

@OptIn(UnstableApi::class)
@Composable
private fun SingleStreamSlot(
  exo: ExoPlayer,
  stream: BroadcastStream,
  isBuffering: Boolean,
  isAudioActive: Boolean,
  onSelectAudio: () -> Unit,
  onChangeChannel: () -> Unit,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  themePrimary: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(DarkSurface)
      .border(
        width = if (isAudioActive) 2.dp else 1.dp,
        color = if (isAudioActive) themePrimary else DarkSurfaceBorder,
        shape = RoundedCornerShape(10.dp)
      )
      .clickable { onSelectAudio() }
  ) {
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          useController = false
          player = exo
          resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
          keepScreenOn = true
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // Header Overlay (Clean dark bar)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .background(Color(0xD90D111A))
        .padding(horizontal = 8.dp, vertical = 6.dp),
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
            .size(7.dp)
            .clip(CircleShape)
            .background(TodLiveRed)
        )
        Text(
          text = stream.title,
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Sound status indicator badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isAudioActive) themePrimary.copy(alpha = 0.2f) else Color(0xFF1E2638))
            .clickable { onSelectAudio() }
            .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isAudioActive) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeMute,
              contentDescription = "Audio status",
              tint = if (isAudioActive) themePrimary else Color(0xFF7A8B9E),
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (isAudioActive) "الصوت" else "مكتوم",
              color = if (isAudioActive) themePrimary else Color(0xFF7A8B9E),
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Expand / Contract full view
        IconButton(
          onClick = onToggleExpand,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
            contentDescription = "Toggle Fullscreen",
            tint = Color.White,
            modifier = Modifier.size(14.dp)
          )
        }

        // Change channel
        IconButton(
          onClick = onChangeChannel,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "Change Channel",
            tint = Color.White,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }

    // Buffering indicator
    if (isBuffering) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          color = themePrimary,
          strokeWidth = 2.5.dp,
          modifier = Modifier.size(30.dp)
        )
      }
    }
  }
}

