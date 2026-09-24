package com.example.player

import android.content.Context
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
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
import androidx.media3.exoplayer.hls.DefaultHlsExtractorFactory
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
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

  // Active channel context
  private var activeChannelList: List<BroadcastStream> = emptyList()

  // High-performance Universal Extractors for MP2, AAC-LATM, AC3, EAC3, DTS, Opus, FLAC
  private val universalExtractorsFactory = DefaultExtractorsFactory()
    .setConstantBitrateSeekingEnabled(true)
    .setTsExtractorFlags(
      DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES or
      DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS or
      DefaultTsPayloadReaderFactory.FLAG_IGNORE_SPLICE_INFO_STREAM or
      DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS
    )
    .setTsExtractorTimestampSearchBytes(1500 * 188)

  // Dedicated HLS Extractor factory to parse all audio formats from TS chunks
  private val universalHlsExtractorFactory = DefaultHlsExtractorFactory(
    DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES or
    DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS or
    DefaultTsPayloadReaderFactory.FLAG_IGNORE_SPLICE_INFO_STREAM or
    DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS,
    /* exposeCea608WhenMissingDeclarations = */ true
  )

  val exoPlayer: ExoPlayer by lazy {
    // Universal RenderersFactory with software decoder fallback and safe component querying
    val renderersFactory = DefaultRenderersFactory(context)
      .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
      .setMediaCodecSelector(MediaCodecSelector.DEFAULT)
      .setEnableDecoderFallback(true)
      .setAllowedVideoJoiningTimeMs(5000)

    // High-capacity LoadControl tuned for zero-stutter playback on 1080p FHD / 4K UHD
    val loadControl = DefaultLoadControl.Builder()
      .setBufferDurationsMs(
        /* minBufferMs = */ 25_000,
        /* maxBufferMs = */ 65_000,
        /* bufferForPlaybackMs = */ 2_000,
        /* bufferForPlaybackAfterRebufferMs = */ 3_500
      )
      .setTargetBufferBytes(64 * 1024 * 1024) // 64MB buffer allocation prevents 4K starvation
      .setBackBuffer(20_000, true)
      .setPrioritizeTimeOverSizeThresholds(false)
      .build()

    val audioAttributes = AudioAttributes.Builder()
      .setUsage(C.USAGE_MEDIA)
      .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
      .build()

    ExoPlayer.Builder(context, renderersFactory)
      .setTrackSelector(trackSelector)
      .setLoadControl(loadControl)
      .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
      .setWakeMode(C.WAKE_MODE_NETWORK)
      .setHandleAudioBecomingNoisy(true)
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
  private var retryJob: Job? = null

  fun setChannelListContext(channels: List<BroadcastStream>) {
    activeChannelList = channels
  }

  fun playStream(stream: BroadcastStream, isRetry: Boolean = false) {
    retryJob?.cancel()
    if (!isRetry) {
      retryCount = 0
      currentUaIndex = 0
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

    // Check if link might be an iframe embed, web player page, or dynamic script needing extraction
    val cleanUrl = rawUrl.substringBefore('?').lowercase()
    val isWebOrEmbed = !cleanUrl.endsWith(".m3u8") && !cleanUrl.endsWith(".mpd") &&
        !cleanUrl.endsWith(".ts") && !cleanUrl.endsWith(".mp4") &&
        !cleanUrl.endsWith(".mkv") && !cleanUrl.endsWith(".ism") &&
        (rawUrl.contains("<iframe", ignoreCase = true) || rawUrl.contains("embed", ignoreCase = true) ||
         rawUrl.contains("player", ignoreCase = true) || rawUrl.contains(".html", ignoreCase = true) ||
         rawUrl.contains(".php", ignoreCase = true) || rawUrl.contains("watch", ignoreCase = true) ||
         rawUrl.contains("live", ignoreCase = true) && !cleanUrl.endsWith(".ts"))

    if (isWebOrEmbed && !isRetry) {
      _playerState.update {
        it.copy(
          isBuffering = true,
          errorMessage = null,
          isLive = stream.isLive
        )
      }
      coroutineScope.launch(Dispatchers.Main) {
        val resolved = SmartStreamResolver.resolveAsync(rawUrl, stream.title)
        executePlay(resolved, isRetry = false)
      }
      return
    }

    executePlay(stream, isRetry)
  }

  private fun executePlay(stream: BroadcastStream, isRetry: Boolean) {
    currentStream = stream
    val rawUrl = stream.streamUrl.trim()

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
      applyAppSettings()
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

  private var currentAudioSessionId: Int = C.AUDIO_SESSION_ID_UNSET

  private fun initLoudnessEnhancer() {
    try {
      val audioSessionId = exoPlayer.audioSessionId
      if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId == 0) return

      // If audio session changed, safely dispose of old enhancer instance
      if (loudnessEnhancer != null && currentAudioSessionId != audioSessionId) {
        try {
          loudnessEnhancer?.release()
        } catch (ignored: Exception) {}
        loudnessEnhancer = null
      }
      currentAudioSessionId = audioSessionId

      val boost = _playerState.value.audioBoostPercent
      val isVoice = _playerState.value.isVoiceEnhancerEnabled

      // Only allocate system effect if boost or voice enhancement is active
      if (boost > 0 || isVoice) {
        if (loudnessEnhancer == null) {
          loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
            enabled = true
          }
        }
        applyAudioGain(boost, isVoice)
      } else {
        loudnessEnhancer?.enabled = false
      }
    } catch (e: Throwable) {
      Log.w("TodExoPlayerManager", "LoudnessEnhancer not supported on this device/session: ${e.message}")
      loudnessEnhancer = null
    }
  }

  private fun applyAudioGain(boostPercent: Int, isVoiceEnhancer: Boolean) {
    try {
      if (boostPercent == 0 && !isVoiceEnhancer) {
        loudnessEnhancer?.enabled = false
        return
      }
      val audioSessionId = exoPlayer.audioSessionId
      if (loudnessEnhancer == null && audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId != 0) {
        loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
          enabled = true
        }
      }
      val enhancer = loudnessEnhancer
      if (enhancer != null) {
        val extraVoiceGain = if (isVoiceEnhancer) 600 else 0
        val targetMb = (boostPercent * 15) + extraVoiceGain
        enhancer.setTargetGain(targetMb.coerceIn(0, 4000))
        enhancer.enabled = true
      }
    } catch (e: Throwable) {
      Log.w("TodExoPlayerManager", "Error applying audio gain: ${e.message}")
    }
  }

  fun applyAppSettings() {
    try {
      val appSettings = AppSettings.getInstance(context)
      val builder = trackSelector.buildUponParameters()

      // 1. Data Saver mode: cap video resolution to 720p and bitrate to 1.5Mbps
      if (appSettings.dataSaverMode) {
        builder.setMaxVideoSize(1280, 720)
               .setMaxVideoBitrate(1_500_000)
      } else {
        builder.clearVideoSizeConstraints()
               .setMaxVideoBitrate(Int.MAX_VALUE)
      }

      // 2. Adaptive Bitrate
      if (!appSettings.adaptiveBitrate) {
        builder.setAllowVideoNonSeamlessAdaptiveness(false)
      } else {
        builder.setAllowVideoNonSeamlessAdaptiveness(true)
      }

      trackSelector.setParameters(builder)

      // 3. Audio Boost & Vocal Clarity
      if (appSettings.audioBoostEnabled) {
        val boost = appSettings.defaultAudioBoostPercent
        val isVoice = appSettings.vocalClarity
        applyAudioGain(boost, isVoice)
      } else {
        applyAudioGain(0, false)
      }
    } catch (e: Throwable) {
      Log.w("TodExoPlayerManager", "Failed to apply real AppSettings: ${e.message}")
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
   * Silent playback retry with alternate format / user agent if needed, without unsolicited jumping.
   */
  private fun handlePlaybackRetry(errorMsg: String) {
    val stream = currentStream ?: return

    if (retryCount < 2) {
      retryCount++
      currentUaIndex = (currentUaIndex + 1) % userAgents.size
      
      val altFormat = when (stream.format) {
        StreamFormat.HLS -> StreamFormat.PROGRESSIVE
        StreamFormat.PROGRESSIVE -> StreamFormat.HLS
        else -> StreamFormat.AUTO
      }

      Log.i("TodExoPlayerManager", "Silent retry #$retryCount with format: $altFormat and UA: ${userAgents[currentUaIndex]}")
      retryJob?.cancel()
      retryJob = coroutineScope.launch {
        delay(1000)
        playStream(stream.copy(format = altFormat), isRetry = true)
      }
      return
    }

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
      .setConnectTimeoutMs(25_000)
      .setReadTimeoutMs(30_000)
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
          .setExtractorFactory(universalHlsExtractorFactory)
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
        ProgressiveMediaSource.Factory(dataSourceFactory, universalExtractorsFactory)
          .createMediaSource(mediaItemBuilder.build())
      }
      StreamFormat.AUTO -> {
        DefaultMediaSourceFactory(dataSourceFactory, universalExtractorsFactory)
          .createMediaSource(mediaItemBuilder.build())
      }
    }
  }

  private fun detectFormat(url: String): StreamFormat {
    val cleanUrl = url.trim()
    val pathWithoutQuery = cleanUrl.substringBefore('?').lowercase()
    val lower = cleanUrl.lowercase()
    return when {
      pathWithoutQuery.endsWith(".mpd") || lower.contains("format=mpd") || lower.contains("manifest.mpd") -> StreamFormat.DASH
      pathWithoutQuery.endsWith(".ism") || lower.contains("/manifest") -> StreamFormat.SMOOTH_STREAMING
      pathWithoutQuery.endsWith(".mp4") || pathWithoutQuery.endsWith(".mkv") || pathWithoutQuery.endsWith(".flv") ||
      pathWithoutQuery.endsWith(".avi") || pathWithoutQuery.endsWith(".webm") || pathWithoutQuery.endsWith(".mov") ||
      pathWithoutQuery.endsWith(".mp3") || pathWithoutQuery.endsWith(".aac") || pathWithoutQuery.endsWith(".ogg") -> StreamFormat.PROGRESSIVE
      pathWithoutQuery.endsWith(".ts") || lower.contains("output=ts") -> StreamFormat.PROGRESSIVE
      pathWithoutQuery.endsWith(".m3u8") || pathWithoutQuery.endsWith(".m3u") || lower.contains("output=m3u8") ||
      pathWithoutQuery.endsWith(".php") || pathWithoutQuery.endsWith(".json") || pathWithoutQuery.endsWith(".css") ||
      pathWithoutQuery.endsWith(".js") || pathWithoutQuery.endsWith(".html") || pathWithoutQuery.endsWith(".htm") ||
      lower.contains("/live/") || lower.contains("/play/") || lower.contains("/stream/") || lower.contains("token=") -> StreamFormat.HLS
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
          isBuffering = if (isPlaying) false else (exoPlayer.playbackState == Player.STATE_BUFFERING)
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
    val rawVideoTracks = mutableListOf<VideoQualityTrack>()
    val audioTracks = mutableListOf<AudioTrackOption>()
    val subtitleTracks = mutableListOf<SubtitleTrackOption>()

    for (group in tracks.groups) {
      when (group.type) {
        C.TRACK_TYPE_VIDEO -> {
          for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            if (format.height > 0) {
              val track = VideoQualityTrack(
                id = "${format.width}x${format.height}_${format.bitrate}",
                label = "",
                width = format.width,
                height = format.height,
                bitrate = format.bitrate,
                isSelected = group.isTrackSelected(i)
              )
              if (rawVideoTracks.none { it.height == track.height }) {
                rawVideoTracks.add(track)
              }
            }
          }
        }
        C.TRACK_TYPE_AUDIO -> {
          for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            val lang = format.language?.lowercase() ?: "und"
            val label = when {
              lang == "ar" || lang == "ara" || format.label?.contains("ar", ignoreCase = true) == true -> "العربية"
              lang == "en" || lang == "eng" || format.label?.contains("en", ignoreCase = true) == true -> "الإنجليزية"
              lang == "fr" || lang == "fra" -> "الفرنسية"
              lang == "es" || lang == "spa" -> "الإسبانية"
              lang == "de" || lang == "deu" -> "الألمانية"
              lang == "it" || lang == "ita" -> "الإيطالية"
              lang == "tr" || lang == "tur" -> "التركية"
              lang == "fa" || lang == "fas" -> "الفارسية"
              lang == "ku" || lang == "kur" -> "الكردية"
              !format.label.isNullOrBlank() -> format.label!!
              else -> "العربية"
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
            val lang = format.language?.lowercase() ?: "und"
            val label = when {
              lang == "ar" || lang == "ara" -> "ترجمة عربية"
              lang == "en" || lang == "eng" -> "English"
              lang == "fr" || lang == "fra" -> "Français"
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

    // Process Video Qualities to match exact TOD Arabic design (Screenshots 1 & 2)
    val sortedVideoTracks = rawVideoTracks.sortedByDescending { it.height }
    val finalQualities = mutableListOf<VideoQualityTrack>()

    if (sortedVideoTracks.size <= 1) {
      // Single-stream quality (Screenshot 2): Only "قياسي"
      val single = sortedVideoTracks.firstOrNull()
      finalQualities.add(
        VideoQualityTrack(
          id = single?.id ?: "standard",
          label = "قياسي",
          width = single?.width ?: 1920,
          height = single?.height ?: 1080,
          bitrate = single?.bitrate ?: 0,
          isAuto = false,
          isSelected = true
        )
      )
    } else {
      // Multi-stream / Adaptive quality (Screenshot 1):
      // 1. تلقائي (Auto)
      // 2. قياسي (Standard / Mid)
      // 3. الأفضل (Best / High)
      // (and 4. منخفض if extra low bitrate exists)
      finalQualities.add(
        VideoQualityTrack(
          id = "auto",
          label = "تلقائي",
          width = 0,
          height = 0,
          bitrate = 0,
          isAuto = true,
          isSelected = _playerState.value.selectedQuality?.isAuto ?: true
        )
      )

      val bestTrack = sortedVideoTracks.first()
      val standardTrack = if (sortedVideoTracks.size >= 2) sortedVideoTracks[1] else sortedVideoTracks.last()

      finalQualities.add(
        VideoQualityTrack(
          id = standardTrack.id,
          label = "قياسي",
          width = standardTrack.width,
          height = standardTrack.height,
          bitrate = standardTrack.bitrate,
          isAuto = false,
          isSelected = _playerState.value.selectedQuality?.id == standardTrack.id
        )
      )

      finalQualities.add(
        VideoQualityTrack(
          id = bestTrack.id,
          label = "الأفضل",
          width = bestTrack.width,
          height = bestTrack.height,
          bitrate = bestTrack.bitrate,
          isAuto = false,
          isSelected = _playerState.value.selectedQuality?.id == bestTrack.id
        )
      )

      if (sortedVideoTracks.size >= 4) {
        val lowTrack = sortedVideoTracks.last()
        finalQualities.add(
          VideoQualityTrack(
            id = lowTrack.id,
            label = "منخفض",
            width = lowTrack.width,
            height = lowTrack.height,
            bitrate = lowTrack.bitrate,
            isAuto = false,
            isSelected = _playerState.value.selectedQuality?.id == lowTrack.id
          )
        )
      }
    }

    // Process Audio Tracks (Screenshot 3): Clean list with fallback to "العربية"
    val finalAudioTracks = if (audioTracks.isNotEmpty()) {
      // Remove duplicate labels if any
      val distinctAudio = mutableListOf<AudioTrackOption>()
      var arabicCount = 0
      audioTracks.forEach { track ->
        val finalLabel = if (track.label == "العربية") {
          arabicCount++
          if (arabicCount > 1) "العربية ($arabicCount)" else "العربية"
        } else {
          track.label
        }
        distinctAudio.add(track.copy(label = finalLabel))
      }
      distinctAudio
    } else {
      listOf(
        AudioTrackOption(
          id = "default_ar",
          label = "العربية",
          language = "ar",
          channels = 2,
          isSelected = true
        )
      )
    }

    _playerState.update { state ->
      val defaultSelectedQuality = state.selectedQuality ?: finalQualities.firstOrNull()
      val defaultSelectedAudio = state.selectedAudioTrack ?: finalAudioTracks.firstOrNull()
      state.copy(
        qualities = finalQualities,
        selectedQuality = defaultSelectedQuality,
        audioTracks = finalAudioTracks,
        selectedAudioTrack = defaultSelectedAudio,
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

