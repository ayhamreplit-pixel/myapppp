package com.example.ui

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.BroadcastCatalog
import com.example.model.BroadcastStream
import com.example.player.TodExoPlayerManager
import com.example.player.XtreamRepository

enum class ScreenDestination {
  START_INPUT,
  QUICK_LINK,
  PLAYER,
  DUAL_PLAYER
}

@Composable
fun TodPlayerScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val activity = context as? Activity
  val coroutineScope = rememberCoroutineScope()

  val xtreamRepo = remember { XtreamRepository(context) }
  var favoriteIds by remember { mutableStateOf(xtreamRepo.getFavorites()) }

  val playerManager = remember { TodExoPlayerManager(context, coroutineScope) }
  val playerState by playerManager.playerState.collectAsState()

  var currentStream by remember { mutableStateOf(BroadcastCatalog.placeholderStream) }
  var activeChannelList by remember { mutableStateOf<List<BroadcastStream>>(emptyList()) }
  var screenDestination by remember { mutableStateOf(ScreenDestination.START_INPUT) }
  var returnDestination by remember { mutableStateOf(ScreenDestination.START_INPUT) }
  var isFullscreen by remember { mutableStateOf(false) }

  // Dual player streams
  var dualStream1 by remember { mutableStateOf(BroadcastCatalog.placeholderStream) }
  var dualStream2 by remember { mutableStateOf(BroadcastCatalog.placeholderStream) }

  // TOD Audio & Quality modal state (Screenshots 10 & 11)
  var showTodAudioQualityModal by remember { mutableStateOf(false) }
  var initialModalTab by remember { mutableStateOf(TodSettingsTab.QUALITY) }

  // In-Player quick channel drawer
  var showInPlayerChannelDrawer by remember { mutableStateOf(false) }

  // Other secondary sheets
  var showSubtitleSheet by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }

  val exitPlayerToHome: () -> Unit = {
    playerManager.stop()
    showInPlayerChannelDrawer = false
    isFullscreen = false
    activity?.let { act ->
      act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      val controller = WindowCompat.getInsetsController(act.window, act.window.decorView)
      controller.show(WindowInsetsCompat.Type.systemBars())
      controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }
    screenDestination = returnDestination
  }

  val launchPlayerInLandscape: (BroadcastStream, List<BroadcastStream>, ScreenDestination) -> Unit = { stream, channels, origin ->
    returnDestination = origin
    currentStream = stream
    activeChannelList = channels
    playerManager.setChannelListContext(channels)
    isFullscreen = true
    activity?.let { act ->
      val controller = WindowCompat.getInsetsController(act.window, act.window.decorView)
      controller.hide(WindowInsetsCompat.Type.ime())
      controller.hide(WindowInsetsCompat.Type.systemBars())
      controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
    playerManager.playStream(stream)
    screenDestination = ScreenDestination.PLAYER
  }

  val toggleFullscreen: () -> Unit = {
    val target = !isFullscreen
    isFullscreen = target
    activity?.let { act ->
      val controller = WindowCompat.getInsetsController(act.window, act.window.decorView)
      if (target) {
        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      } else {
        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        controller.show(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
      }
    }
  }

  // Back handler navigation: close drawer if open, otherwise exit player or quick link screen
  BackHandler(enabled = screenDestination != ScreenDestination.START_INPUT || showInPlayerChannelDrawer) {
    if (showInPlayerChannelDrawer) {
      showInPlayerChannelDrawer = false
    } else if (screenDestination == ScreenDestination.PLAYER || screenDestination == ScreenDestination.DUAL_PLAYER) {
      exitPlayerToHome()
    } else if (screenDestination == ScreenDestination.QUICK_LINK) {
      screenDestination = ScreenDestination.START_INPUT
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

    FluidMeshBackground(
      modifier = modifier.fillMaxSize()
    ) {
      when (screenDestination) {
      ScreenDestination.START_INPUT -> {
        // 1. Redesigned Hub with Xtream Codes & Direct Streams (No Presets)
        TodModernHubScreen(
          onPlayStream = { stream, channels ->
            launchPlayerInLandscape(stream, channels, ScreenDestination.START_INPUT)
          },
          onPlayDualStream = { s1, s2 ->
            dualStream1 = s1
            dualStream2 = s2
            playerManager.stop()
            returnDestination = ScreenDestination.START_INPUT
            screenDestination = ScreenDestination.DUAL_PLAYER
            activity?.let { act ->
              act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
              WindowCompat.getInsetsController(act.window, act.window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
              }
            }
          },
          onOpenQuickLinkScreen = {
            screenDestination = ScreenDestination.QUICK_LINK
          }
        )
      }

      ScreenDestination.QUICK_LINK -> {
        // Dedicated iOS Modern Full-Screen Quick Link Player Destination
        TodQuickLinkScreen(
          onBack = {
            screenDestination = ScreenDestination.START_INPUT
          },
          onPlayStream = { stream, channels ->
            launchPlayerInLandscape(stream, channels, ScreenDestination.QUICK_LINK)
          }
        )
      }

      ScreenDestination.DUAL_PLAYER -> {
        TodDualPlayerView(
          stream1 = dualStream1,
          stream2 = dualStream2,
          onClose = { exitPlayerToHome() },
          onChangeChannel1 = { exitPlayerToHome() },
          onChangeChannel2 = { exitPlayerToHome() },
          modifier = Modifier.fillMaxSize()
        )
      }

      ScreenDestination.PLAYER -> {
        // 2. TOD Video Player Screen: Starts directly in Landscape Fullscreen
        val currentIndex = activeChannelList.indexOfFirst { it.id == currentStream.id }
        val hasNext = currentIndex != -1 && currentIndex < activeChannelList.size - 1
        val hasPrev = currentIndex > 0

        val onNext: (() -> Unit)? = if (hasNext) {
          {
            val next = activeChannelList[currentIndex + 1]
            currentStream = next
            playerManager.zapToChannel(next)
          }
        } else null

        val onPrev: (() -> Unit)? = if (hasPrev) {
          {
            val prev = activeChannelList[currentIndex - 1]
            currentStream = prev
            playerManager.zapToChannel(prev)
          }
        } else null

        val isFav = favoriteIds.contains(currentStream.id)

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
          onOpenCustomStream = { exitPlayerToHome() },
          onSelectMoment = { moment ->
            playerManager.seekTo(moment.timeSeconds * 1000)
          },
          onNavigateBack = { exitPlayerToHome() },
          onOpenGrid = {
            // Open in-player channel drawer for instant channel switching!
            showInPlayerChannelDrawer = true
          },
          onNextChannel = onNext,
          onPreviousChannel = onPrev,
          onToggleFavorite = {
            xtreamRepo.toggleFavorite(currentStream.id)
            favoriteIds = xtreamRepo.getFavorites()
          },
          isFavorite = isFav,
          modifier = Modifier.fillMaxSize()
        )

        // In-Player Channel Drawer (Slides in over the landscape player)
        TodInPlayerChannelDrawer(
          visible = showInPlayerChannelDrawer,
          channels = activeChannelList,
          currentStreamId = currentStream.id,
          onSelectChannel = { newStream ->
            currentStream = newStream
            playerManager.playStream(newStream)
          },
          onClose = { showInPlayerChannelDrawer = false },
          onExitToHub = { exitPlayerToHome() }
        )
      }
    }

    // ========================================================
    // MODAL DIALOGS
    // ========================================================

    // 1. TOD Audio & Quality Modal (Exact 75% translucent backdrop & Thmanyah font)
    AnimatedVisibility(
      visible = showTodAudioQualityModal,
      enter = fadeIn(tween(200)),
      exit = fadeOut(tween(200))
    ) {
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
  }
}
