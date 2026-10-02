package com.example.model

/**
 * Team information with name, flag or crest URL, and country/team code.
 */
data class SportsTeam(
  val name: String,
  val logoUrl: String = "",
  val flagEmoji: String = "⚽",
  val code: String = "",
  val primaryColor: Long = 0xFF0A84FF
)

/**
 * Match statistics for the Match Detail view (Possession, Shots, Fouls, etc.)
 */
data class MatchStats(
  val possessionHome: Int = 50,
  val possessionAway: Int = 50,
  val shotsOnTargetHome: Int = 0,
  val shotsOnTargetAway: Int = 0,
  val totalShotsHome: Int = 0,
  val totalShotsAway: Int = 0,
  val cornersHome: Int = 0,
  val cornersAway: Int = 0,
  val foulsHome: Int = 0,
  val foulsAway: Int = 0,
  val yellowCardsHome: Int = 0,
  val yellowCardsAway: Int = 0
)

/**
 * Lineups for both teams
 */
data class MatchLineup(
  val formationHome: String = "4-3-3",
  val formationAway: String = "4-2-3-1",
  val coachHome: String = "",
  val coachAway: String = "",
  val startersHome: List<String> = emptyList(),
  val startersAway: List<String> = emptyList(),
  val substitutesHome: List<String> = emptyList(),
  val substitutesAway: List<String> = emptyList()
)

/**
 * Tournament standing table row
 */
data class StandingRow(
  val position: Int,
  val teamName: String,
  val teamLogo: String = "",
  val played: Int,
  val goalDiff: Int,
  val points: Int
)

/**
 * Head to head past meeting
 */
data class H2hMatch(
  val date: String,
  val competition: String,
  val homeTeam: String,
  val awayTeam: String,
  val score: String,
  val winnerTeam: String? = null // null for draw
)

/**
 * Streaming server link for a match (configured from m7 PHP panel)
 */
data class MatchStreamServer(
  val id: String = "",
  val name: String,
  val streamUrl: String,
  val quality: String = "HD",
  val isWorking: Boolean = true
)

/**
 * Core Sports Match Data Model (Pixel-matched to TOD Screenshots)
 */
data class SportsMatch(
  val id: String,
  val title: String,
  val tournament: String,
  val tournamentLogo: String = "",
  val homeTeam: SportsTeam,
  val awayTeam: SportsTeam,
  val kickoffTime: String, // e.g. "19:45" or "21:45"
  val kickoffDate: String, // e.g. "30 سبتمبر 2026"
  val stadium: String = "",
  val commentator: String = "تعليق عربي",
  val channelName: String = "beIN SPORTS 1 HD",
  val channelId: String = "bein_1",
  val streamUrl: String = "",
  val servers: List<MatchStreamServer> = emptyList(),
  val isLive: Boolean = false,
  val isEnded: Boolean = false,
  val liveMinute: String? = null, // e.g. "34'" or "الشوط الثاني"
  val scoreHome: Int? = null,
  val scoreAway: Int? = null,
  val countdownText: String? = null, // e.g. "01 أيام : 05 ساعات : 28 دقائق"
  val bannerUrl: String? = null, // Hero image / player cutouts
  val stats: MatchStats? = null,
  val lineups: MatchLineup? = null,
  val standings: List<StandingRow> = emptyList(),
  val h2h: List<H2hMatch> = emptyList(),
  val isFavorite: Boolean = false
)

/**
 * Tournament / Competition Card
 */
data class SportsCompetition(
  val id: String,
  val name: String,
  val season: String = "2026/2027",
  val logoUrl: String = "",
  val accentColorHex: Long = 0xFF0A84FF,
  val matchesCount: Int = 0
)

/**
 * Sports Highlight or Replay Show (e.g. NETBUSTERS, Highlights, PL REWIND)
 */
data class SportsShow(
  val id: String,
  val title: String,
  val subtitle: String,
  val duration: String,
  val bannerUrl: String = "",
  val streamUrl: String = ""
)

/**
 * TOD User Profile ("من يشاهد الآن؟")
 */
data class TodUserProfile(
  val id: String,
  val name: String,
  val avatarEmoji: String = "⚽",
  val avatarGradientHex: Long = 0xFFFFB800,
  val isKids: Boolean = false,
  val isVip: Boolean = true
)

/**
 * Sports News Item (from Yallakora API: https://sportfeeds.gemini.media/yallakoraapi/NewsList)
 */
data class SportsNewsItem(
  val id: String,
  val title: String,
  val date: String,
  val imageUrl: String,
  val category: String = "أخبار كرة القدم",
  val source: String = "يلا كورة",
  val summary: String = "",
  val url: String = ""
)

/**
 * Top Announcement / Breaking Ticker
 */
data class AnnouncementConfig(
  val isEnabled: Boolean = true,
  val title: String = "إعلان هام",
  val message: String = "مرحباً بكم في HERO Cast • تغطية حية لجميع المباريات والبطولات العالمية وسيرفرات البث المباشر"
)

/**
 * Real-time Device Session tracking sent to server
 */
data class DeviceSessionInfo(
  val deviceId: String,
  val deviceName: String,
  val profileName: String,
  val activeStreamTitle: String = "",
  val ipAddress: String = "",
  val timestamp: Long = System.currentTimeMillis()
)

