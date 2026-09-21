package com.example.player

import android.content.Context
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.model.AspectRatioMode
import com.example.model.AudioTrackOption
import com.example.model.BroadcastStats
import com.example.model.BroadcastStream
import com.example.model.StreamFormat
import com.example.model.SubtitleTrackOption
import com.example.model.TodPlayerState
import com.example.model.VideoQualityTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class TodExoPlayerManager(
  private val context: Context,
  private val coroutineScope: CoroutineScope
) {

  private val _playerState = MutableStateFlow(TodPlayerState())
  val playerState: StateFlow<TodPlayerState> = _playerState.asStateFlow()

  val trackSelector = DefaultTrackSelector(context).apply {
    setParameters(
      buildUponParameters()
        .setExceedRendererCapabilitiesIfNecessary(true)
        .setAllowAudioMixedMimeTypeAdaptiveness(true)
        .setAllowAudioNonSeamlessAdaptiveness(true)
        .setAllowVideoMixedMimeTypeAdaptiveness(true)
        .setAllowVideoNonSeamlessAdaptiveness(true)
    )
  }

  // Universal User-Agent rotation for IPTV bypass
  private val userAgents = listOf(
    "IPTVSmartersPro/3.1.5 (Linux; Android 12; Mobile)",
    "TiviMate/4.7.0 (Linux; Android 13; Mobile)",
    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36",
    "VLC/3.5.4 (Linux; Android 14; Mobile)",
    "ExoPlayer/2.19.1 (Linux; Android 14)"
  )
  private var currentUaIndex = 0

  // Hardware Audio Loudness Enhancer for true +200% volume boost
  private var loudnessEnhancer: LoudnessEnhancer? = null

  // Active channel context for Auto-Failover
  private var activeChannelList: List<BroadcastStream> = emptyList()

  val exoPlayer: ExoPlayer by lazy {
    // Universal RenderersFactory supporting software decoders fallback for AC-3, E-AC-3, AAC-LATM, MP2, DTS
    val renderersFactory = DefaultRenderersFactory(context)
      .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
      .setEnableDecoderFallback(true)

    // LoadControl with ultra-fast startup and buffer management
    val loadControl = DefaultLoadControl.Builder()
      .setBufferDurationsMs(
        /* minBufferMs = */ 1_500,
        /* maxBufferMs = */ 10_000,
        /* bufferForPlaybackMs = */ 200,
        /* bufferForPlaybackAfterRebufferMs = */ 400
      )
      .setBackBuffer(8_000, true)
      .setPrioritizeTimeOverSizeThresholds(true)
      .build()

    ExoPlayer.Builder(context, renderersFactory)
      .setTrackSelector(trackSelector)
      .setLoadControl(loadControl)
      .build().apply {
        playWhenReady = true
        addListener(playerListener)
        addAnalyticsListener(analyticsListener)
      }
  }

  private var tickerJob: Job? = null
  private var sleepTimerJob: Job? = null
  private var currentStream: BroadcastStream? = null
  private var retryCount = 0

  fun setChannelListContext(channels: List<BroadcastStream>) {
    activeChannelList = channels
  }

  fun playStream(stream: BroadcastStream, isRetry: Boolean = false) {
    if (!isRetry) {
      retryCount = 0
      currentUaIndex = 0
      _playerState.update { it.copy(autoFailoverMessage = null) }
    }
    currentStream = stream
    val rawUrl = stream.streamUrl.trim()

    if (rawUrl.isBlank()) {
      _playerState.update {
        it.copy(
          isBuffering = false,
          isPlaying = false,
          errorMessage = "الرجاء إدخال أو اختيار رابط بث صالح"
        )
      }
      return
    }

    val hasValidScheme = rawUrl.startsWith("http://", ignoreCase = true) ||
        rawUrl.startsWith("https://", ignoreCase = true) ||
        rawUrl.startsWith("rtsp://", ignoreCase = true) ||
        rawUrl.startsWith("rtmp://", ignoreCase = true) ||
        rawUrl.startsWith("content://", ignoreCase = true) ||
        rawUrl.startsWith("file://", ignoreCase = true)

    if (!hasValidScheme) {
      _playerState.update {
        it.copy(
          isBuffering = false,
          isPlaying = false,
          errorMessage = "رابط البث غير صالح:\n$rawUrl"
        )
      }
      return
    }

    _playerState.update {
      it.copy(
        isBuffering = true,
        errorMessage = null,
        isLive = stream.isLive
      )
    }

    try {
      val mediaSource = createMediaSource(stream)
      exoPlayer.setMediaSource(mediaSource)
      exoPlayer.prepare()
      exoPlayer.play()
      initLoudnessEnhancer()
    } catch (e: Exception) {
      Log.e("TodExoPlayerManager", "Error preparing stream", e)
      handlePlaybackRetry(e.localizedMessage ?: "Playback Exception")
    }
  }

  private fun initLoudnessEnhancer() {
    try {
      val audioSessionId = exoPlayer.audioSessionId
      if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId != 0) {
        if (loudnessEnhancer == null) {
          loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
            enabled = true
          }
        }
        applyAudioGain(_playerState.value.audioBoostPercent, _playerState.value.isVoiceEnhancerEnabled)
      }
    } catch (e: Exception) {
      Log.w("TodExoPlayerManager", "LoudnessEnhancer not supported on this device/session", e)
    }
  }

  private fun applyAudioGain(boostPercent: Int, isVoiceEnhancer: Boolean) {
    try {
      val enhancer = loudnessEnhancer
      if (enhancer != null) {
        // Boost target in milliBels (0 to 3000 mB)
        val extraVoiceGain = if (isVoiceEnhancer) 600 else 0
        val targetMb = (boostPercent * 15) + extraVoiceGain
        enhancer.setTargetGain(targetMb.coerceIn(0, 4000))
        enhancer.enabled = (boostPercent > 0 || isVoiceEnhancer)
      }
    } catch (e: Exception) {
      Log.w("TodExoPlayerManager", "Error applying audio gain", e)
    }
  }

  fun retryStream() {
    currentStream?.let { playStream(it) }
  }

  fun reloadStream() {
    currentStream?.let {
      exoPlayer.stop()
      playStream(it)
    }
  }

  /**
   * Smart Auto-Failover:
   * 1. Tries alternate container format (.ts <-> .m3u8).
   * 2. Tries alternate User-Agent.
   * 3. Automatically finds matching backup channel in the same playlist (e.g. 720p / SD / Backup).
   */
  private fun handlePlaybackRetry(errorMsg: String) {
    val stream = currentStream ?: return

    if (retryCount < 3) {
      retryCount++
      currentUaIndex = (currentUaIndex + 1) % userAgents.size
      
      val altFormat = when (stream.format) {
        StreamFormat.HLS -> StreamFormat.PROGRESSIVE
        StreamFormat.PROGRESSIVE -> StreamFormat.HLS
        else -> StreamFormat.AUTO
      }
      
      var newUrl = stream.streamUrl
      if (newUrl.endsWith(".ts", ignoreCase = true)) {
        newUrl = newUrl.substringBeforeLast(".ts") + ".m3u8"
      } else if (newUrl.endsWith(".m3u8", ignoreCase = true)) {
        newUrl = newUrl.substringBeforeLast(".m3u8") + ".ts"
      }

      val failoverMsg = "جاري التحويل التلقائي للبث البديل (#$retryCount)..."
      _playerState.update { it.copy(autoFailoverMessage = failoverMsg) }

      Log.i("TodExoPlayerManager", "AI Auto-Failover #$retryCount with UA: ${userAgents[currentUaIndex]} and format: $altFormat")
      playStream(stream.copy(streamUrl = newUrl, format = altFormat), isRetry = true)
      return
    }

    // Step 2: Search for backup stream in the channel list (e.g. 720p, Backup, SD, or same name)
    val channelTitle = stream.title.lowercase()
    val backupCandidate = activeChannelList.find { ch ->
      ch.streamUrl != stream.streamUrl &&
      (ch.title.contains(channelTitle.take(6), ignoreCase = true) ||
       channelTitle.contains(ch.title.take(6), ignoreCase = true) ||
       ch.title.contains("backup", ignoreCase = true) ||
       ch.title.contains("720", ignoreCase = true) ||
       ch.title.contains("sd", ignoreCase = true))
    }

    if (backupCandidate != null) {
      retryCount = 0
      val backupMsg = "تم التحويل تلقائياً للبث الاحتياطي: ${backupCandidate.title}"
      _playerState.update { it.copy(autoFailoverMessage = backupMsg) }
      playStream(backupCandidate, isRetry = true)
      return
    }

    // Otherwise show friendly error message
    _playerState.update {
      it.copy(
        isBuffering = false,
        isPlaying = false,
        errorMessage = "تعذر تشغيل هذا البث ($errorMsg). يرجى التأكد من استقرار السيرفر أو اختيار قناة أخرى."
      )
    }
  }

  private fun createMediaSource(stream: BroadcastStream): MediaSource {
    val uri = Uri.parse(stream.streamUrl)

    val chosenUserAgent = if (!stream.userAgent.isNullOrBlank()) stream.userAgent else userAgents[currentUaIndex]

    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
      .setUserAgent(chosenUserAgent)
      .setAllowCrossProtocolRedirects(true)
      .setKeepPostFor302Redirects(true)
      .setConnectTimeoutMs(15_000)
      .setReadTimeoutMs(20_000)
      .apply {
        val headers = mutableMapOf<String, String>()
        headers["Accept"] = "*/*"
        headers["Connection"] = "keep-alive"
        if (!stream.origin.isNullOrBlank()) headers["Origin"] = stream.origin
        if (!stream.referer.isNullOrBlank()) headers["Referer"] = stream.referer
        if (!stream.cookie.isNullOrBlank()) headers["Cookie"] = stream.cookie
        stream.extraHeaders.forEach { (k, v) ->
          headers[k] = v
        }
        setDefaultRequestProperties(headers)
      }

    val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
    val mediaItemBuilder = MediaItem.Builder().setUri(uri)

    if (!stream.drmScheme.isNullOrBlank() || !stream.drmKey.isNullOrBlank()) {
      val schemeLower = (stream.drmScheme ?: "").lowercase()
      val drmUuid = when {
        schemeLower.contains("widevine") -> C.WIDEVINE_UUID
        schemeLower.contains("playready") -> C.PLAYREADY_UUID
        else -> C.CLEARKEY_UUID
      }

      val drmConfigBuilder = MediaItem.DrmConfiguration.Builder(drmUuid)
      val licenseStr = stream.drmKey?.trim() ?: ""
      if (licenseStr.startsWith("http://", ignoreCase = true) || licenseStr.startsWith("https://", ignoreCase = true)) {
        drmConfigBuilder.setLicenseUri(licenseStr)
        val licenseHeaders = mutableMapOf<String, String>()
        if (!stream.origin.isNullOrBlank()) licenseHeaders["Origin"] = stream.origin
        if (!stream.referer.isNullOrBlank()) licenseHeaders["Referer"] = stream.referer
        drmConfigBuilder.setLicenseRequestHeaders(licenseHeaders)
      } else if (licenseStr.contains(":")) {
        val clearKeyJson = com.example.model.StreamUrlParser.buildClearKeyJson(licenseStr)
        if (clearKeyJson != null) {
          val dataUri = "data:application/json;base64," + android.util.Base64.encodeToString(
            clearKeyJson.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
          )
          drmConfigBuilder.setLicenseUri(dataUri)
        }
      }
      mediaItemBuilder.setDrmConfiguration(drmConfigBuilder.build())
    }

    val format = when (stream.format) {
      StreamFormat.AUTO -> detectFormat(stream.streamUrl)
      else -> stream.format
    }

    return when (format) {
      StreamFormat.HLS -> {
        HlsMediaSource.Factory(dataSourceFactory)
          .setAllowChunklessPreparation(true)
          .createMediaSource(mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8).build())
      }
      StreamFormat.DASH -> {
        DashMediaSource.Factory(dataSourceFactory)
          .createMediaSource(mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MPD).build())
      }
      StreamFormat.SMOOTH_STREAMING -> {
        SsMediaSource.Factory(dataSourceFactory)
          .createMediaSource(mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_SS).build())
      }
      StreamFormat.PROGRESSIVE -> {
        ProgressiveMediaSource.Factory(dataSourceFactory)
          .createMediaSource(mediaItemBuilder.build())
      }
      StreamFormat.AUTO -> {
        DefaultMediaSourceFactory(dataSourceFactory)
          .createMediaSource(mediaItemBuilder.build())
      }
    }
  }

  private fun detectFormat(url: String): StreamFormat {
    val lower = url.lowercase()
    return when {
      lower.contains(".mpd") || lower.contains("format=mpd") || lower.contains("manifest.mpd") -> StreamFormat.DASH
      lower.contains(".ism") || lower.contains("/manifest") -> StreamFormat.SMOOTH_STREAMING
      lower.contains(".mp4") || lower.contains(".mkv") || lower.contains(".flv") || lower.contains(".avi") || lower.contains(".webm") -> StreamFormat.PROGRESSIVE
      lower.contains(".ts") || lower.contains("output=ts") -> StreamFormat.PROGRESSIVE
      lower.contains(".m3u8") || lower.contains(".m3u") || lower.contains("output=m3u8") || lower.contains("/live/") || lower.contains(".php") || lower.contains(".json") || lower.contains(".css") || lower.contains(".js") || lower.contains("token=") -> StreamFormat.HLS
      else -> StreamFormat.HLS
    }
  }

  // Fast Instant Channel Zapping
  fun zapToChannel(stream: BroadcastStream) {
    currentStream = stream
    playStream(stream)
  }

  fun toggleMiniChannelBar(): Boolean {
    val newState = !_playerState.value.isMiniChannelBarVisible
    _playerState.update { it.copy(isMiniChannelBarVisible = newState) }
    return newState
  }

  fun hideMiniChannelBar() {
    _playerState.update { it.copy(isMiniChannelBarVisible = false) }
  }

  fun togglePlayPause() {
    if (exoPlayer.isPlaying) {
      exoPlayer.pause()
    } else {
      if (exoPlayer.playbackState == Player.STATE_ENDED) {
        exoPlayer.seekTo(0)
      }
      exoPlayer.play()
    }
  }

  fun pause() {
    exoPlayer.pause()
    stopPeriodicTicker()
  }

  fun stop() {
    exoPlayer.stop()
    stopPeriodicTicker()
  }

  fun seekTo(positionMs: Long) {
    exoPlayer.seekTo(positionMs.coerceIn(0, exoPlayer.duration.coerceAtLeast(0)))
  }

  fun seekForward10s() {
    val target = (exoPlayer.currentPosition + 10_000).coerceAtMost(exoPlayer.duration.coerceAtLeast(0))
    exoPlayer.seekTo(target)
  }

  fun seekBackward10s() {
    val target = (exoPlayer.currentPosition - 10_000).coerceAtLeast(0)
    exoPlayer.seekTo(target)
  }

  fun syncToLive() {
    if (exoPlayer.isCurrentMediaItemLive) {
      exoPlayer.seekToDefaultPosition()
    }
  }

  fun setPlaybackSpeed(speed: Float) {
    exoPlayer.playbackParameters = PlaybackParameters(speed)
    _playerState.update { it.copy(playbackSpeed = speed) }
  }

  fun setAspectRatioMode(mode: AspectRatioMode) {
    _playerState.update { it.copy(aspectRatioMode = mode) }
  }

  fun cycleAspectRatio(): AspectRatioMode {
    val current = _playerState.value.aspectRatioMode
    val modes = AspectRatioMode.entries
    val nextIndex = (modes.indexOf(current) + 1) % modes.size
    val nextMode = modes[nextIndex]
    setAspectRatioMode(nextMode)
    return nextMode
  }

  fun toggleMute(): Boolean {
    val currentlyMuted = _playerState.value.isMuted
    val newMuted = !currentlyMuted
    if (newMuted) {
      exoPlayer.volume = 0f
    } else {
      val normalizedVol = 1.0f + (_playerState.value.audioBoostPercent / 100f)
      exoPlayer.volume = normalizedVol.coerceIn(0f, 2.0f)
    }
    _playerState.update { it.copy(isMuted = newMuted) }
    return newMuted
  }

  fun toggleControlsLock(): Boolean {
    val newLock = !_playerState.value.isControlsLocked
    _playerState.update { it.copy(isControlsLocked = newLock) }
    return newLock
  }

  fun toggleStatsHud(): Boolean {
    val newStats = !_playerState.value.showStatsHud
    _playerState.update { it.copy(showStatsHud = newStats) }
    return newStats
  }

  fun setSleepTimer(minutes: Int) {
    sleepTimerJob?.cancel()
    if (minutes <= 0) {
      cancelSleepTimer()
      return
    }
    _playerState.update { it.copy(sleepTimerMinutes = minutes, sleepTimerRemainingSec = minutes * 60) }
    sleepTimerJob = coroutineScope.launch(Dispatchers.Main) {
      var remaining = minutes * 60
      while (remaining > 0 && isActive) {
        delay(1000)
        remaining--
        _playerState.update { it.copy(sleepTimerRemainingSec = remaining) }
      }
      if (remaining <= 0) {
        pause()
        _playerState.update { it.copy(sleepTimerMinutes = null, sleepTimerRemainingSec = 0) }
      }
    }
  }

  fun cancelSleepTimer() {
    sleepTimerJob?.cancel()
    sleepTimerJob = null
    _playerState.update { it.copy(sleepTimerMinutes = null, sleepTimerRemainingSec = 0) }
  }

  fun setAudioBoostPercent(percent: Int) {
    val clamped = percent.coerceIn(0, 200)
    _playerState.update { it.copy(audioBoostPercent = clamped) }
    if (!_playerState.value.isMuted) {
      val volumeFactor = 1.0f + (clamped / 100f)
      exoPlayer.volume = volumeFactor.coerceIn(0f, 3.0f)
    }
    applyAudioGain(clamped, _playerState.value.isVoiceEnhancerEnabled)
  }

  fun toggleVoiceEnhancer(): Boolean {
    val newState = !_playerState.value.isVoiceEnhancerEnabled
    _playerState.update { it.copy(isVoiceEnhancerEnabled = newState) }
    if (newState && _playerState.value.audioBoostPercent < 40) {
      setAudioBoostPercent(60)
    } else {
      applyAudioGain(_playerState.value.audioBoostPercent, newState)
    }
    return newState
  }

  fun toggleDataSaverMode(): Boolean {
    val newState = !_playerState.value.isDataSaverMode
    _playerState.update { it.copy(isDataSaverMode = newState) }
    if (newState) {
      // Force SD / low-bitrate to save bandwidth and prevent buffering on weak cellular
      trackSelector.setParameters(
        trackSelector.buildUponParameters()
          .setMaxVideoSizeSd()
          .setMaxVideoBitrate(1_200_000)
      )
    } else {
      // Clear limits
      trackSelector.setParameters(
        trackSelector.buildUponParameters()
          .clearVideoSizeConstraints()
          .setMaxVideoBitrate(Int.MAX_VALUE)
      )
    }
    return newState
  }

  fun selectQuality(quality: VideoQualityTrack) {
    if (quality.isAuto) {
      trackSelector.setParameters(
        trackSelector.buildUponParameters()
          .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
          .setMaxVideoSizeSd()
      )
    } else {
      val tracks = exoPlayer.currentTracks
      for (group in tracks.groups) {
        if (group.type == C.TRACK_TYPE_VIDEO) {
          for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            if (format.height == quality.height) {
              trackSelector.setParameters(
                trackSelector.buildUponParameters()
                  .setOverrideForType(
                    TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                  )
              )
              break
            }
          }
        }
      }
    }
    _playerState.update { it.copy(selectedQuality = quality) }
  }

  fun selectAudioTrack(audio: AudioTrackOption) {
    val tracks = exoPlayer.currentTracks
    for (group in tracks.groups) {
      if (group.type == C.TRACK_TYPE_AUDIO) {
        for (i in 0 until group.length) {
          val id = "${group.mediaTrackGroup.id}_$i"
          if (id == audio.id) {
            trackSelector.setParameters(
              trackSelector.buildUponParameters()
                .setOverrideForType(
                  TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                )
            )
            _playerState.update { it.copy(selectedAudioTrack = audio) }
            return
          }
        }
      }
    }
  }

  fun selectSubtitleTrack(sub: SubtitleTrackOption?) {
    if (sub == null) {
      trackSelector.setParameters(
        trackSelector.buildUponParameters()
          .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
      )
      _playerState.update { it.copy(selectedSubtitleTrack = null) }
    } else {
      val tracks = exoPlayer.currentTracks
      for (group in tracks.groups) {
        if (group.type == C.TRACK_TYPE_TEXT) {
          for (i in 0 until group.length) {
            val id = "${group.mediaTrackGroup.id}_$i"
            if (id == sub.id) {
              trackSelector.setParameters(
                trackSelector.buildUponParameters()
                  .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                  .setOverrideForType(
                    TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                  )
              )
              _playerState.update { it.copy(selectedSubtitleTrack = sub) }
              return
            }
          }
        }
      }
    }
  }

  private fun startPeriodicTicker() {
    stopPeriodicTicker()
    tickerJob = coroutineScope.launch(Dispatchers.Main) {
      while (isActive) {
        if (exoPlayer.isPlaying) {
          val pos = exoPlayer.currentPosition.coerceAtLeast(0)
          val dur = exoPlayer.duration.coerceAtLeast(0)
          val buf = exoPlayer.bufferedPosition.coerceAtLeast(0)
          val live = exoPlayer.isCurrentMediaItemLive

          _playerState.update { state ->
            state.copy(
              currentPositionMs = pos,
              durationMs = dur,
              bufferedPositionMs = buf,
              isLive = live,
              stats = state.stats.copy(
                bufferDurationSec = ((buf - pos).coerceAtLeast(0) / 1000f)
              )
            )
          }
        }
        delay(500)
      }
    }
  }

  private fun stopPeriodicTicker() {
    tickerJob?.cancel()
    tickerJob = null
  }

  private val playerListener = object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
      val isBuffering = playbackState == Player.STATE_BUFFERING
      val isEnded = playbackState == Player.STATE_ENDED
      val isReady = playbackState == Player.STATE_READY

      _playerState.update {
        it.copy(
          isBuffering = isBuffering,
          durationMs = exoPlayer.duration.coerceAtLeast(0),
          isLive = exoPlayer.isCurrentMediaItemLive
        )
      }

      if (isReady) {
        initLoudnessEnhancer()
      }

      if (isReady && exoPlayer.playWhenReady) {
        startPeriodicTicker()
      } else if (isEnded) {
        stopPeriodicTicker()
      }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
      _playerState.update {
        it.copy(
          isPlaying = isPlaying,
          isBuffering = false
        )
      }
      if (isPlaying) {
        startPeriodicTicker()
      }
    }

    override fun onAudioSessionIdChanged(audioSessionId: Int) {
      initLoudnessEnhancer()
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
      val res = "${videoSize.width}x${videoSize.height}"
      val badge = when {
        videoSize.height >= 2160 -> "4K UHD"
        videoSize.height >= 1080 -> "1080p FHD"
        videoSize.height >= 720 -> "720p HD"
        videoSize.height > 0 -> "${videoSize.height}p SD"
        else -> "HD"
      }
      _playerState.update { state ->
        state.copy(
          activeResolutionBadge = badge,
          stats = state.stats.copy(resolution = res)
        )
      }
    }

    override fun onTracksChanged(tracks: Tracks) {
      extractTracks()
    }

    override fun onPlayerError(error: PlaybackException) {
      Log.e("TodExoPlayerManager", "Player error: ${error.errorCodeName}", error)
      handlePlaybackRetry(error.errorCodeName)
    }
  }

  private val analyticsListener = object : AnalyticsListener {
    override fun onDroppedVideoFrames(
      eventTime: AnalyticsListener.EventTime,
      droppedFrames: Int,
      elapsedMs: Long
    ) {
      _playerState.update { state ->
        val total = state.stats.droppedFrames + droppedFrames
        state.copy(stats = state.stats.copy(droppedFrames = total))
      }
    }

    override fun onDownstreamFormatChanged(
      eventTime: AnalyticsListener.EventTime,
      mediaLoadData: androidx.media3.exoplayer.source.MediaLoadData
    ) {
      val format = mediaLoadData.trackFormat
      if (format != null) {
        val bitrate = format.bitrate / 1000
        val fps = format.frameRate
        val codec = format.sampleMimeType ?: format.codecs ?: "Universal Codec"
        _playerState.update { state ->
          state.copy(
            stats = state.stats.copy(
              bitrateKbps = if (bitrate > 0) bitrate else state.stats.bitrateKbps,
              frameRateFps = if (fps > 0) fps else state.stats.frameRateFps,
              videoCodec = codec,
              protocol = currentStream?.format?.extensionBadge ?: "HLS"
            )
          )
        }
      }
    }
  }

  private fun extractTracks() {
    val tracks = exoPlayer.currentTracks
    val videoQualities = mutableListOf<VideoQualityTrack>()
    val audioTracks = mutableListOf<AudioTrackOption>()
    val subtitleTracks = mutableListOf<SubtitleTrackOption>()

    videoQualities.add(
      VideoQualityTrack(
        id = "auto",
        label = "تلقائي (Auto Adaptive)",
        width = 0,
        height = 0,
        bitrate = 0,
        isAuto = true,
        isSelected = _playerState.value.selectedQuality?.isAuto ?: true
      )
    )

    for (group in tracks.groups) {
      when (group.type) {
        C.TRACK_TYPE_VIDEO -> {
          for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            if (format.height > 0) {
              val label = "${format.height}p" + (if (format.frameRate >= 50) " 60fps" else "") +
                (if (format.bitrate > 0) " (${format.bitrate / 1000}k)" else "")
              val quality = VideoQualityTrack(
                id = "${format.width}x${format.height}_${format.bitrate}",
                label = label,
                width = format.width,
                height = format.height,
                bitrate = format.bitrate,
                isSelected = group.isTrackSelected(i)
              )
              if (videoQualities.none { it.height == quality.height }) {
                videoQualities.add(quality)
              }
            }
          }
        }
        C.TRACK_TYPE_AUDIO -> {
          for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            val lang = format.language ?: "und"
            val label = when (lang.lowercase()) {
              "ar", "ara" -> "تعليق عربي (Jawwy Arabic)"
              "en", "eng" -> "تعليق إنجليزي (English)"
              "fr", "fra" -> "تعليق فرنسي (French)"
              "es", "spa" -> "تعليق إسباني (Spanish)"
              else -> if (format.label != null) format.label!! else "قناة صوتية ${audioTracks.size + 1} ($lang)"
            }
            audioTracks.add(
              AudioTrackOption(
                id = "${group.mediaTrackGroup.id}_$i",
                label = label,
                language = lang,
                channels = format.channelCount,
                isSelected = group.isTrackSelected(i)
              )
            )
          }
        }
        C.TRACK_TYPE_TEXT -> {
          for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            val lang = format.language ?: "und"
            val label = when (lang.lowercase()) {
              "ar", "ara" -> "ترجمة عربية"
              "en", "eng" -> "English"
              "fr", "fra" -> "Français"
              else -> format.label ?: "ترجمة ($lang)"
            }
            subtitleTracks.add(
              SubtitleTrackOption(
                id = "${group.mediaTrackGroup.id}_$i",
                label = label,
                language = lang,
                isSelected = group.isTrackSelected(i)
              )
            )
          }
        }
      }
    }

    _playerState.update { state ->
      state.copy(
        qualities = videoQualities.sortedByDescending { it.height },
        audioTracks = audioTracks,
        subtitleTracks = subtitleTracks
      )
    }
  }

  fun release() {
    tickerJob?.cancel()
    sleepTimerJob?.cancel()
    try {
      loudnessEnhancer?.release()
    } catch (ignored: Exception) {}
    exoPlayer.removeListener(playerListener)
    exoPlayer.removeAnalyticsListener(analyticsListener)
    exoPlayer.release()
  }
}

