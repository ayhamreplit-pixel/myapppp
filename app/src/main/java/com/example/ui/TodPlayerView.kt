package com.example.ui

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.AspectRatioMode
import com.example.model.BroadcastStream
import com.example.model.MatchMoment
import com.example.model.TodPlayerState
import com.example.player.TodExoPlayerManager
import com.example.ui.theme.TodAmberYellow
import com.example.ui.theme.TodCyan
import com.example.ui.theme.TodViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun TodPlayerView(
  playerManager: TodExoPlayerManager,
  playerState: TodPlayerState,
  stream: BroadcastStream,
  isFullscreen: Boolean,
  onToggleFullscreen: () -> Unit,
  onTriggerPip: () -> Unit,
  onOpenQuality: () -> Unit,
  onOpenAudio: () -> Unit,
  onOpenSubtitles: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenCustomStream: () -> Unit,
  onSelectMoment: (MatchMoment) -> Unit,
  onNavigateBack: () -> Unit = {},
  onOpenGrid: () -> Unit = {},
  onNextChannel: (() -> Unit)? = null,
  onPreviousChannel: (() -> Unit)? = null,
  onToggleFavorite: () -> Unit = {},
  isFavorite: Boolean = false,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

  var controlsVisible by remember { mutableStateOf(true) }
  var lastUserInteraction by remember { mutableFloatStateOf(0f) }

  // Gesture HUD states
  var showVolumeHud by remember { mutableStateOf(false) }
  var volumePercent by remember { mutableFloatStateOf(0.5f) }

  var showBrightnessHud by remember { mutableStateOf(false) }
  var brightnessPercent by remember { mutableFloatStateOf(0.5f) }

  var showDoubleTapFeedback by remember { mutableStateOf<String?>(null) }

  val coroutineScope = rememberCoroutineScope()

  // Auto-hide controls timer
  LaunchedEffect(controlsVisible, playerState.isPlaying, lastUserInteraction) {
    if (controlsVisible && playerState.isPlaying && !playerState.isControlsLocked) {
      delay(4500)
      controlsVisible = false
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      try {
        activity?.let { act ->
          val lp = act.window.attributes
          lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
          act.window.attributes = lp
        }
      } catch (ignored: Exception) {}
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .pointerInput(playerState.isControlsLocked) {
        detectTapGestures(
          onTap = {
            controlsVisible = !controlsVisible
            lastUserInteraction = System.currentTimeMillis().toFloat()
          },
          onDoubleTap = { offset ->
            if (playerState.isControlsLocked) return@detectTapGestures
            val width = size.width
            if (offset.x < width * 0.4f) {
              // Double tap left: Rewind 10s
              playerManager.seekBackward10s()
              showDoubleTapFeedback = "-10s"
              coroutineScope.launch {
                delay(600)
                if (showDoubleTapFeedback == "-10s") showDoubleTapFeedback = null
              }
            } else if (offset.x > width * 0.6f) {
              // Double tap right: Forward 10s
              playerManager.seekForward10s()
              showDoubleTapFeedback = "+10s"
              coroutineScope.launch {
                delay(600)
                if (showDoubleTapFeedback == "+10s") showDoubleTapFeedback = null
              }
            } else {
              // Center double tap: toggle play/pause
              playerManager.togglePlayPause()
            }
          }
        )
      }
      .pointerInput(playerState.isControlsLocked) {
        if (playerState.isControlsLocked) return@pointerInput
        detectDragGestures(
          onDragStart = { offset ->
            lastUserInteraction = System.currentTimeMillis().toFloat()
            val width = size.width
            if (offset.x < width * 0.45f) {
              // Left half: Brightness
              val currentBrightness = activity?.window?.attributes?.screenBrightness ?: 0.5f
              brightnessPercent = if (currentBrightness < 0f) 0.5f else currentBrightness
              showBrightnessHud = true
            } else if (offset.x > width * 0.55f) {
              // Right half: Volume
              try {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                val currVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                volumePercent = (currVol.toFloat() / maxVol.toFloat()).coerceIn(0f, 1f)
                showVolumeHud = true
              } catch (e: Exception) {
                // Ignore emulator audio service quirks
              }
            }
          },
          onDragEnd = {
            coroutineScope.launch {
              delay(800)
              showBrightnessHud = false
              showVolumeHud = false
            }
          },
          onDragCancel = {
            showBrightnessHud = false
            showVolumeHud = false
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val delta = -dragAmount.y / 400f
            if (showBrightnessHud && activity != null) {
              brightnessPercent = (brightnessPercent + delta).coerceIn(0.05f, 1.0f)
              try {
                val lp = activity.window.attributes
                lp.screenBrightness = brightnessPercent
                activity.window.attributes = lp
              } catch (e: Exception) {
                // Ignore in headless/emulator window manager
              }
            } else if (showVolumeHud) {
              try {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                volumePercent = (volumePercent + delta).coerceIn(0f, 1.0f)
                val targetVol = (volumePercent * maxVol).toInt().coerceIn(0, maxVol)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
              } catch (e: Exception) {
                // Ignore audio volume exceptions
              }
            }
          }
        )
      }
  ) {
    // Media3 PlayerView Native Surface
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          useController = false
          player = playerManager.exoPlayer
          keepScreenOn = true
          resizeMode = when (playerState.aspectRatioMode) {
            AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
          }
        }
      },
      update = { view ->
        view.player = playerManager.exoPlayer
        view.resizeMode = when (playerState.aspectRatioMode) {
          AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
          AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
          AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
          AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
          AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // Double-tap Feedback HUD
    showDoubleTapFeedback?.let { feedback ->
      Box(
        modifier = Modifier
          .align(if (feedback.startsWith("-")) Alignment.CenterStart else Alignment.CenterEnd)
          .padding(horizontal = 40.dp)
          .clip(CircleShape)
          .background(Color(0x9900E5FF))
          .padding(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (feedback.startsWith("-")) Icons.Default.FastRewind else Icons.Default.FastForward,
            contentDescription = null,
            tint = Color(0xFF00222B),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(feedback, color = Color(0xFF00222B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }
    }

    // Brightness HUD
    AnimatedVisibility(
      visible = showBrightnessHud,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xCC080B12))
          .padding(horizontal = 14.dp, vertical = 18.dp)
      ) {
        Icon(
          Icons.Default.BrightnessMedium,
          contentDescription = "Brightness",
          tint = TodCyan,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "${(brightnessPercent * 100).toInt()}%",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .width(6.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF263248))
        ) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxSize()
              .background(TodCyan)
          )
        }
      }
    }

    // Volume HUD
    AnimatedVisibility(
      visible = showVolumeHud,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xCC080B12))
          .padding(horizontal = 14.dp, vertical = 18.dp)
      ) {
        Icon(
          Icons.AutoMirrored.Filled.VolumeUp,
          contentDescription = "Volume",
          tint = TodCyan,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "${(volumePercent * 100).toInt()}%",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .width(6.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF263248))
        ) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxSize()
              .background(TodCyan)
          )
        }
      }
    }

    // Broadcast Controls Overlay
    TodControlsOverlay(
      stream = stream,
      playerState = playerState,
      controlsVisible = controlsVisible,
      isFullscreen = isFullscreen,
      onTogglePlayPause = {
        playerManager.togglePlayPause()
        lastUserInteraction = System.currentTimeMillis().toFloat()
      },
      onSeekForward = {
        playerManager.seekForward10s()
        lastUserInteraction = System.currentTimeMillis().toFloat()
      },
      onSeekBackward = {
        playerManager.seekBackward10s()
        lastUserInteraction = System.currentTimeMillis().toFloat()
      },
      onSeekTo = { pos ->
        playerManager.seekTo(pos)
        lastUserInteraction = System.currentTimeMillis().toFloat()
      },
      onSyncToLive = {
        playerManager.syncToLive()
        lastUserInteraction = System.currentTimeMillis().toFloat()
      },
      onToggleLock = {
        playerManager.toggleControlsLock()
        lastUserInteraction = System.currentTimeMillis().toFloat()
      },
      onOpenQuality = onOpenQuality,
      onOpenAudio = onOpenAudio,
      onOpenSubtitles = onOpenSubtitles,
      onOpenGrid = onOpenGrid,
      onToggleFullscreen = onToggleFullscreen,
      onNavigateBack = onNavigateBack,
      onSelectMoment = { moment ->
        playerManager.seekTo(moment.timeSeconds * 1000)
        lastUserInteraction = System.currentTimeMillis().toFloat()
      },
      onCycleAspectRatio = { playerManager.cycleAspectRatio() },
      onNextChannel = onNextChannel,
      onPreviousChannel = onPreviousChannel,
      onReloadStream = { playerManager.reloadStream() },
      onTriggerPip = onTriggerPip,
      onToggleFavorite = onToggleFavorite,
      isFavorite = isFavorite,
      onToggleMute = { playerManager.toggleMute() },
      brightnessLevel = brightnessPercent,
      onBrightnessChange = { newB ->
        brightnessPercent = newB
        activity?.let { act ->
          val lp = act.window.attributes
          lp.screenBrightness = newB
          act.window.attributes = lp
        }
      },
      volumeLevel = volumePercent,
      onVolumeChange = { newV ->
        volumePercent = newV
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVol = (newV * maxVol).toInt().coerceIn(0, maxVol)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
      }
    )

    // Stats for Nerds Telemetry HUD
    if (playerState.showStatsHud) {
      TodStatsHud(
        stats = playerState.stats,
        isLive = playerState.isLive,
        onClose = { playerManager.toggleStatsHud() },
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 50.dp, end = 16.dp)
      )
    }

    // Stream Error Banner (with retry button & back to channels)
    playerState.errorMessage?.let { error ->
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .padding(24.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xF0181216))
          .border(1.dp, Color(0xFFE50914).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
          .padding(20.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.widthIn(max = 380.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = Color(0xFFFF5252),
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "خطأ في البث / Playback Error",
            color = Color(0xFFFF5252),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = error,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2B3245))
                .clickable { onNavigateBack() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = "رجوع للقنوات",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(TodAmberYellow)
                .clickable { playerManager.retryStream() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                TodReloadIcon(size = 18.dp, tint = Color.Black)
                Text(
                  text = "إعادة المحاولة",
                  color = Color.Black,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
