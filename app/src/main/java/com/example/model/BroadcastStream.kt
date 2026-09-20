package com.example.model

enum class StreamFormat(val displayName: String, val extensionBadge: String) {
  AUTO("Auto Detect", "AUTO"),
  HLS("HLS Broadcast (m3u8)", "HLS"),
  DASH("DASH Streaming (mpd)", "DASH"),
  SMOOTH_STREAMING("SmoothStreaming (ism)", "SS"),
  PROGRESSIVE("MP4 / Video File", "MP4")
}

enum class MatchEventType {
  GOAL,
  YELLOW_CARD,
  RED_CARD,
  PENALTY,
  VAR_CHECK,
  SUBSTITUTION,
  HALF_TIME
}

data class MatchMoment(
  val timeSeconds: Long,
  val minuteText: String,
  val description: String,
  val eventType: MatchEventType,
  val teamBadge: String? = null
)

data class BroadcastStream(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: String,
  val streamUrl: String,
  val format: StreamFormat = StreamFormat.HLS,
  val isLive: Boolean = false,
  val channelNumber: String? = null,
  val thumbnailColor: Long = 0xFF10141E,
  val tournamentOrLeague: String = "TOD Broadcast",
  val score: String? = null,
  val matchTime: String? = null,
  val moments: List<MatchMoment> = emptyList(),
  val resolutionLabel: String = "1080p FHD",
  val isDvrEnabled: Boolean = true,
  val description: String = "",
  val origin: String? = null,
  val referer: String? = null,
  val cookie: String? = null,
  val userAgent: String? = null,
  val drmKey: String? = null,
  val drmScheme: String? = null
)
