package com.example.ui

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.BroadcastCatalog
import com.example.model.BroadcastStream
import com.example.player.TodExoPlayerManager

enum class ScreenDestination {
  START_INPUT,
  PLAYER,
  CATALOG
}

@Composable
fun TodPlayerScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val activity = context as? Activity
  val coroutineScope = rememberCoroutineScope()

  val playerManager = remember { TodExoPlayerManager(context, coroutineScope) }
  val playerState by playerManager.playerState.collectAsState()

  val streamsList = remember {
    mutableStateListOf<BroadcastStream>().apply {
      addAll(BroadcastCatalog.defaultStreams)
    }
  }

  var currentStream by remember { mutableStateOf(streamsList.first()) }
  var screenDestination by remember { mutableStateOf(ScreenDestination.START_INPUT) }
  var isFullscreen by remember { mutableStateOf(false) }

  // TOD Audio & Quality modal state (Screenshots 10 & 11)
  var showTodAudioQualityModal by remember { mutableStateOf(false) }
  var initialModalTab by remember { mutableStateOf(TodSettingsTab.QUALITY) }

  // Other secondary sheets
  var showSubtitleSheet by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var showCustomStreamDialog by remember { mutableStateOf(false) }

  // Back handler navigation
  BackHandler(enabled = isFullscreen || screenDestination != ScreenDestination.START_INPUT) {
    if (isFullscreen) {
      isFullscreen = false
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      activity?.window?.let { window ->
        WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
      }
    } else if (screenDestination == ScreenDestination.CATALOG) {
      screenDestination = ScreenDestination.START_INPUT
    } else if (screenDestination == ScreenDestination.PLAYER) {
      screenDestination = ScreenDestination.START_INPUT
    }
  }

  // Handle Fullscreen orientation changes & system bars
  val toggleFullscreen: () -> Unit = {
    val target = !isFullscreen
    isFullscreen = target
    activity?.let { act ->
      if (target) {
        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        WindowCompat.getInsetsController(act.window, act.window.decorView).apply {
          hide(WindowInsetsCompat.Type.systemBars())
          systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
      } else {
        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        WindowCompat.getInsetsController(act.window, act.window.decorView).show(WindowInsetsCompat.Type.systemBars())
      }
    }
  }

  // PiP mode trigger
  val triggerPip: () -> Unit = {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
      val params = PictureInPictureParams.Builder()
        .setAspectRatio(Rational(16, 9))
        .build()
      activity.enterPictureInPictureMode(params)
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      playerManager.release()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    when (screenDestination) {
      ScreenDestination.START_INPUT -> {
        // 1. Media3 Simple Start & Input Screen (Screenshot 1)
        TodStartInputScreen(
          onPlayStream = { stream ->
            if (!streamsList.any { it.streamUrl == stream.streamUrl }) {
              streamsList.add(0, stream)
            }
            currentStream = stream
            playerManager.playStream(stream)
            screenDestination = ScreenDestination.PLAYER
          },
          onOpenPresetCatalog = {
            screenDestination = ScreenDestination.CATALOG
          }
        )
      }

      ScreenDestination.CATALOG -> {
        // 2. Channels & Broadcast Catalog Hub
        Column(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
        ) {
          // Top bar with back to start input
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0F141E))
              .padding(horizontal = 8.dp, vertical = 8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              IconButton(onClick = { screenDestination = ScreenDestination.START_INPUT }) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back to Input",
                  tint = Color.White
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "قنوات ومباريات TOD",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          BroadcastHubView(
            currentStream = currentStream,
            allStreams = streamsList,
            onSelectStream = { stream ->
              currentStream = stream
              playerManager.playStream(stream)
              screenDestination = ScreenDestination.PLAYER
            },
            onOpenCustomStreamDialog = { screenDestination = ScreenDestination.START_INPUT },
            onSeekToMoment = { moment ->
              playerManager.seekTo(moment.timeSeconds * 1000)
            },
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          )
        }
      }

      ScreenDestination.PLAYER -> {
        // 2. TOD Video Player Screen: Dedicated clean player with no bottom channels/match cards
        TodPlayerView(
          playerManager = playerManager,
          playerState = playerState,
          stream = currentStream,
          isFullscreen = isFullscreen,
          onToggleFullscreen = toggleFullscreen,
          onTriggerPip = triggerPip,
          onOpenQuality = {
            initialModalTab = TodSettingsTab.QUALITY
            showTodAudioQualityModal = true
          },
          onOpenAudio = {
            initialModalTab = TodSettingsTab.AUDIO
            showTodAudioQualityModal = true
          },
          onOpenSubtitles = { showSubtitleSheet = true },
          onOpenSettings = { showSettingsSheet = true },
          onOpenCustomStream = { screenDestination = ScreenDestination.START_INPUT },
          onSelectMoment = { moment ->
            playerManager.seekTo(moment.timeSeconds * 1000)
          },
          onNavigateBack = {
            if (isFullscreen) toggleFullscreen()
            screenDestination = ScreenDestination.START_INPUT
          },
          onOpenGrid = {
            // Return to input to load any other dynamic stream
            if (isFullscreen) toggleFullscreen()
            screenDestination = ScreenDestination.START_INPUT
          },
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    // ========================================================
    // MODAL DIALOGS
    // ========================================================

    // 1. TOD Audio & Quality Modal (Screenshots 10 & 11)
    if (showTodAudioQualityModal) {
      TodAudioQualityModal(
        initialTab = initialModalTab,
        qualities = playerState.qualities,
        selectedQuality = playerState.selectedQuality,
        audioTracks = playerState.audioTracks,
        selectedAudio = playerState.selectedAudioTrack,
        onSelectQuality = { q -> playerManager.selectQuality(q) },
        onSelectAudio = { a -> playerManager.selectAudioTrack(a) },
        onDismiss = { showTodAudioQualityModal = false }
      )
    }

    // 2. Subtitles Sheet
    if (showSubtitleSheet) {
      SubtitleTrackSheet(
        subtitles = playerState.subtitleTracks,
        selectedSubtitle = playerState.selectedSubtitleTrack,
        onSelect = { sub -> playerManager.selectSubtitleTrack(sub) },
        onDismiss = { showSubtitleSheet = false }
      )
    }

    // 3. Playback Settings Sheet
    if (showSettingsSheet) {
      PlaybackSettingsSheet(
        currentSpeed = playerState.playbackSpeed,
        currentAspect = playerState.aspectRatioMode,
        audioBoostPercent = playerState.audioBoostPercent,
        onSpeedChange = { speed -> playerManager.setPlaybackSpeed(speed) },
        onAspectChange = { mode -> playerManager.setAspectRatioMode(mode) },
        onAudioBoostChange = { boost -> playerManager.setAudioBoostPercent(boost) },
        onDismiss = { showSettingsSheet = false }
      )
    }

    // 4. Custom Stream Dialog (if triggered)
    if (showCustomStreamDialog) {
      CustomStreamDialog(
        onPlayStream = { customStream ->
          streamsList.add(0, customStream)
          currentStream = customStream
          playerManager.playStream(customStream)
          screenDestination = ScreenDestination.PLAYER
        },
        onDismiss = { showCustomStreamDialog = false }
      )
    }
  }
}
