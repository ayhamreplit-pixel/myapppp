package com.example.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
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

  val trackSelector = DefaultTrackSelector(context)

  val exoPlayer: ExoPlayer by lazy {
    val loadControl = DefaultLoadControl.Builder()
      .setBufferDurationsMs(
        /* minBufferMs = */ 15_000,
        /* maxBufferMs = */ 50_000,
        /* bufferForPlaybackMs = */ 1_500,
        /* bufferForPlaybackAfterRebufferMs = */ 3_000
      )
      .setPrioritizeTimeOverSizeThresholds(true)
      .build()

    ExoPlayer.Builder(context)
      .setTrackSelector(trackSelector)
      .setLoadControl(loadControl)
      .build().apply {
        playWhenReady = true
        addListener(playerListener)
        addAnalyticsListener(analyticsListener)
      }
  }

  private var tickerJob: Job? = null
  private var currentStream: BroadcastStream? = null

  init {
    startPeriodicTicker()
  }

  fun playStream(stream: BroadcastStream) {
    currentStream = stream
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
    } catch (e: Exception) {
      Log.e("TodExoPlayerManager", "Error preparing stream", e)
      _playerState.update { it.copy(errorMessage = "Error loading stream: ${e.localizedMessage}") }
    }
  }

  private fun createMediaSource(stream: BroadcastStream): MediaSource {
    val uri = Uri.parse(stream.streamUrl)

    // Build HTTP data source with custom headers if provided
    val defaultUa = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 ExoPlayer/2.19.1"
    val chosenUserAgent = if (!stream.userAgent.isNullOrBlank()) stream.userAgent else defaultUa

    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
      .setUserAgent(chosenUserAgent)
      .setAllowCrossProtocolRedirects(true)
      .setKeepPostFor302Redirects(true)
      .setConnectTimeoutMs(15_000)
      .setReadTimeoutMs(20_000)
      .apply {
        val headers = mutableMapOf<String, String>()
        if (!stream.origin.isNullOrBlank()) headers["Origin"] = stream.origin
        if (!stream.referer.isNullOrBlank()) headers["Referer"] = stream.referer
        if (!stream.cookie.isNullOrBlank()) headers["Cookie"] = stream.cookie
        if (headers.isNotEmpty()) {
          setDefaultRequestProperties(headers)
        }
      }

    val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

    // Build MediaItem with optional DRM configuration
    val mediaItemBuilder = MediaItem.Builder().setUri(uri)

    if (!stream.drmScheme.isNullOrBlank() || !stream.drmKey.isNullOrBlank()) {
      val schemeLower = (stream.drmScheme ?: "").lowercase()
      val drmUuid = when {
        schemeLower.contains("widevine") -> C.WIDEVINE_UUID
        schemeLower.contains("playready") -> C.PLAYREADY_UUID
        else -> C.CLEARKEY_UUID
      }

      val drmConfigBuilder = MediaItem.DrmConfiguration.Builder(drmUuid)
      if (!stream.drmKey.isNullOrBlank() && stream.drmKey.startsWith("http")) {
        drmConfigBuilder.setLicenseUri(stream.drmKey)
      }
      mediaItemBuilder.setDrmConfiguration(drmConfigBuilder.build())
    }

    // Determine format either by explicit enum or URI inspection
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
      StreamFormat.PROGRESSIVE, StreamFormat.AUTO -> {
        ProgressiveMediaSource.Factory(dataSourceFactory)
          .createMediaSource(mediaItemBuilder.build())
      }
    }
  }

  private fun detectFormat(url: String): StreamFormat {
    val lower = url.lowercase()
    return when {
      lower.contains(".m3u8") -> StreamFormat.HLS
      lower.contains(".mpd") -> StreamFormat.DASH
      lower.contains(".ism") -> StreamFormat.SMOOTH_STREAMING
      else -> StreamFormat.PROGRESSIVE
    }
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

  fun setAudioBoostPercent(percent: Int) {
    _playerState.update { it.copy(audioBoostPercent = percent) }
    // Standard volume adjustment on ExoPlayer (1.0 = normal, up to 2.0 boost)
    val normalizedVol = 1.0f + (percent / 100f)
    exoPlayer.volume = normalizedVol.coerceIn(0f, 2.0f)
  }

  fun toggleControlsLock() {
    _playerState.update { it.copy(isControlsLocked = !it.isControlsLocked) }
  }

  fun toggleStatsHud() {
    _playerState.update { it.copy(showStatsHud = !it.showStatsHud) }
  }

  fun selectQuality(quality: VideoQualityTrack) {
    val parameters = trackSelector.buildUponParameters()
    if (quality.isAuto) {
      parameters.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
      parameters.setMaxVideoSizeSd()
      parameters.clearVideoSizeConstraints()
      parameters.setMaxVideoBitrate(Int.MAX_VALUE)
    } else {
      parameters.setMaxVideoSize(quality.width, quality.height)
      parameters.setMaxVideoBitrate(if (quality.bitrate > 0) (quality.bitrate * 1.2).toInt() else Int.MAX_VALUE)
    }
    trackSelector.setParameters(parameters)
    _playerState.update { state ->
      state.copy(
        selectedQuality = quality,
        qualities = state.qualities.map { it.copy(isSelected = it.id == quality.id) }
      )
    }
  }

  fun selectAudioTrack(option: AudioTrackOption) {
    val parameters = trackSelector.buildUponParameters()
    parameters.setPreferredAudioLanguage(option.language)
    trackSelector.setParameters(parameters)
    _playerState.update { state ->
      state.copy(
        selectedAudioTrack = option,
        audioTracks = state.audioTracks.map { it.copy(isSelected = it.id == option.id) }
      )
    }
  }

  fun selectSubtitleTrack(option: SubtitleTrackOption?) {
    val parameters = trackSelector.buildUponParameters()
    if (option == null) {
      parameters.setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT)
      parameters.setPreferredTextLanguage(null)
      parameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
    } else {
      parameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
      parameters.setPreferredTextLanguage(option.language)
    }
    trackSelector.setParameters(parameters)
    _playerState.update { state ->
      state.copy(
        selectedSubtitleTrack = option,
        subtitleTracks = state.subtitleTracks.map { it.copy(isSelected = it.id == option?.id) }
      )
    }
  }

  fun retryStream() {
    currentStream?.let { playStream(it) }
  }

  private fun startPeriodicTicker() {
    tickerJob?.cancel()
    tickerJob = coroutineScope.launch(Dispatchers.Main) {
      while (isActive) {
        updateProgressAndStats()
        delay(400)
      }
    }
  }

  private fun updateProgressAndStats() {
    val position = exoPlayer.currentPosition
    val duration = exoPlayer.duration.coerceAtLeast(0)
    val buffered = exoPlayer.bufferedPosition
    val isLive = exoPlayer.isCurrentMediaItemLive
    val liveOffset = if (isLive) exoPlayer.currentLiveOffset else 0L

    val bufferDurationSec = ((buffered - position).coerceAtLeast(0L) / 1000f)
    val liveLatencySec = (liveOffset.coerceAtLeast(0L) / 1000f)

    val currentStats = _playerState.value.stats.copy(
      bufferDurationSec = bufferDurationSec,
      liveLatencySec = liveLatencySec
    )

    _playerState.update {
      it.copy(
        isPlaying = exoPlayer.isPlaying,
        currentPositionMs = position,
        durationMs = duration,
        bufferedPositionMs = buffered,
        isLive = isLive,
        liveOffsetMs = liveOffset,
        stats = currentStats
      )
    }
  }

  private val playerListener = object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
      val isBuffering = playbackState == Player.STATE_BUFFERING
      _playerState.update {
        it.copy(
          isBuffering = isBuffering,
          isLive = exoPlayer.isCurrentMediaItemLive,
          durationMs = exoPlayer.duration.coerceAtLeast(0)
        )
      }
      if (playbackState == Player.STATE_READY) {
        extractTracks()
      }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
      _playerState.update { it.copy(isPlaying = isPlaying) }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
      val res = "${videoSize.width}x${videoSize.height}"
      _playerState.update { state ->
        state.copy(stats = state.stats.copy(resolution = res))
      }
    }

    override fun onTracksChanged(tracks: Tracks) {
      extractTracks()
    }

    override fun onPlayerError(error: PlaybackException) {
      Log.e("TodExoPlayerManager", "Player error: ${error.errorCodeName}", error)
      val friendlyMessage = when {
        error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
          "Stream access denied or expired (HTTP Error). Please choose another channel or check the stream URL."
        error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
          "Network connection failed. Please check your internet connection."
        error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ||
        error.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
          "Unsupported or corrupted stream format."
        else ->
          error.message ?: "Playback encountered an error (${error.errorCodeName})"
      }
      _playerState.update {
        it.copy(
          isBuffering = false,
          isPlaying = false,
          errorMessage = friendlyMessage
        )
      }
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
        val codec = format.sampleMimeType ?: format.codecs ?: "Unknown Codec"
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
        label = "Auto (Adaptive)",
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
              "en", "eng" -> "English Commentary"
              "ar", "ara" -> "Arabic Commentary (TOD)"
              "es", "spa" -> "Spanish Commentary"
              "fr", "fra" -> "French Commentary"
              "de", "deu" -> "German Commentary"
              else -> if (format.label != null) format.label!! else "Audio Channel ${audioTracks.size + 1} ($lang)"
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
              "en", "eng" -> "English"
              "ar", "ara" -> "Arabic"
              "es", "spa" -> "Spanish"
              "fr", "fra" -> "French"
              else -> format.label ?: "Subtitles ($lang)"
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
    exoPlayer.removeListener(playerListener)
    exoPlayer.removeAnalyticsListener(analyticsListener)
    exoPlayer.release()
  }
}
