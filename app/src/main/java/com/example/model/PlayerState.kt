package com.example.model

data class VideoQualityTrack(
  val id: String,
  val label: String,
  val width: Int = 0,
  val height: Int = 0,
  val bitrate: Int = 0,
  val isAuto: Boolean = false,
  val isSelected: Boolean = false
)

data class AudioTrackOption(
  val id: String,
  val label: String,
  val language: String,
  val channels: Int = 2,
  val isSelected: Boolean = false
)

data class SubtitleTrackOption(
  val id: String,
  val label: String,
  val language: String,
  val isSelected: Boolean = false
)

enum class AspectRatioMode(val label: String) {
  FIT("Fit"),
  ZOOM("Zoom (Fill)"),
  STRETCH("Stretch"),
  RATIO_16_9("16:9"),
  RATIO_4_3("4:3")
}

data class BroadcastStats(
  val protocol: String = "HLS",
  val resolution: String = "1920x1080",
  val bitrateKbps: Int = 0,
  val frameRateFps: Float = 0f,
  val droppedFrames: Int = 0,
  val bufferDurationSec: Float = 0f,
  val liveLatencySec: Float = 0f,
  val videoCodec: String = "H.264 (avc1)",
  val audioCodec: String = "AAC (Stereo)",
  val decoderName: String = "c2.android.avc.decoder"
)

data class TodPlayerState(
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val isLive: Boolean = false,
  val currentPositionMs: Long = 0L,
  val durationMs: Long = 0L,
  val bufferedPositionMs: Long = 0L,
  val liveOffsetMs: Long = 0L,
  val playbackSpeed: Float = 1.0f,
  val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
  val audioBoostPercent: Int = 0,
  val isVoiceEnhancerEnabled: Boolean = false,
  val isDataSaverMode: Boolean = false,
  val isControlsLocked: Boolean = false,
  val showStatsHud: Boolean = false,
  val stats: BroadcastStats = BroadcastStats(),
  val qualities: List<VideoQualityTrack> = emptyList(),
  val selectedQuality: VideoQualityTrack? = null,
  val audioTracks: List<AudioTrackOption> = emptyList(),
  val selectedAudioTrack: AudioTrackOption? = null,
  val subtitleTracks: List<SubtitleTrackOption> = emptyList(),
  val selectedSubtitleTrack: SubtitleTrackOption? = null,
  val sleepTimerMinutes: Int? = null,
  val sleepTimerRemainingSec: Int = 0,
  val activeResolutionBadge: String = "HD",
  val isMuted: Boolean = false,
  val isMiniChannelBarVisible: Boolean = false,
  val errorMessage: String? = null
)

