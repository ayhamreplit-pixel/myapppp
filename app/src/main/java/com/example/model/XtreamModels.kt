package com.example.model

data class XtreamCategory(
  val categoryId: String,
  val categoryName: String,
  val channelCount: Int = 0
)

data class XtreamChannel(
  val streamId: String,
  val name: String,
  val iconUrl: String? = null,
  val categoryId: String? = null,
  val playUrl: String
)

data class XtreamAccountInfo(
  val username: String,
  val status: String,
  val expDate: String?,
  val serverUrl: String
)

data class XtreamPlaylistConfig(
  val playlistName: String = "Xtream Codes",
  val username: String = "",
  val password: String = "",
  val serverUrl: String = "",
  val isM3u: Boolean = false,
  val m3uUrl: String = "",
  val useDefaultUserAgent: Boolean = true,
  val customUserAgent: String = "IPTVSmartersPro/3.1.5 (Linux; Android 12)",
  val isEnabled: Boolean = true,
  val updateInterval: String = "عند بدء التطبيق", // "عند بدء التطبيق", "كل ساعة", "كل 4 ساعات", "كل 6 ساعات", "كل 12 ساعة", "كل 24 ساعة", "يدوياً فقط"
  val lastUpdatedTimestamp: Long = 0L,
  val totalChannelCount: Int = 0,
  val enableChannels: Boolean = true,
  val enableMovies: Boolean = true,
  val enableSeries: Boolean = true,
  val streamFormat: String = "MPEG-TS (.ts)", // or "HLS (.m3u8)"
  val archivePeriod: String = "تلقائي"
)
