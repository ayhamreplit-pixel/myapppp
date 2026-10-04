package com.example.model

/**
 * Ensures team logo image URLs from YSScores or other CDNs are complete, valid HTTPS links.
 */
fun formatYsscoresTeamLogo(urlOrName: String?): String {
  if (urlOrName.isNullOrBlank() || urlOrName.equals("null", ignoreCase = true)) return ""
  val clean = urlOrName.trim()
  return when {
    clean.startsWith("http://img.ysscores.com/teams/") -> clean.replace("http://img.ysscores.com/teams/", "https://imgs.ysscores.com/teams/128/")
    clean.startsWith("https://img.ysscores.com/teams/") -> clean.replace("https://img.ysscores.com/teams/", "https://imgs.ysscores.com/teams/128/")
    clean.contains("imgs.ysscores.com/teams/") && !clean.contains("/128/") && !clean.contains("/64/") -> clean.replace("imgs.ysscores.com/teams/", "imgs.ysscores.com/teams/128/")
    clean.startsWith("http://") -> clean.replace("http://", "https://")
    clean.startsWith("https://") -> clean
    clean.endsWith(".png") || clean.endsWith(".jpg") || clean.endsWith(".webp") || clean.endsWith(".svg") -> "https://imgs.ysscores.com/teams/128/$clean"
    clean.matches(Regex("^[0-9]+$")) -> "https://imgs.ysscores.com/teams/128/$clean.png"
    else -> clean
  }
}

/**
 * Ensures championship logo image URLs from YSScores CDN are complete, valid HTTPS links.
 */
fun formatYsscoresChampionshipLogo(urlOrName: String?): String {
  if (urlOrName.isNullOrBlank() || urlOrName.equals("null", ignoreCase = true)) return ""
  val clean = urlOrName.trim()
  return when {
    clean.startsWith("http://img.ysscores.com/championship/") -> clean.replace("http://img.ysscores.com/championship/", "https://imgs.ysscores.com/championship/128/")
    clean.startsWith("https://img.ysscores.com/championship/") -> clean.replace("https://img.ysscores.com/championship/", "https://imgs.ysscores.com/championship/128/")
    clean.contains("imgs.ysscores.com/championship/") && !clean.contains("/128/") && !clean.contains("/48/") -> clean.replace("imgs.ysscores.com/championship/", "imgs.ysscores.com/championship/128/")
    clean.startsWith("http://") -> clean.replace("http://", "https://")
    clean.startsWith("https://") -> clean
    clean.endsWith(".png") || clean.endsWith(".jpg") || clean.endsWith(".webp") || clean.endsWith(".svg") -> "https://imgs.ysscores.com/championship/128/$clean"
    clean.matches(Regex("^[0-9]+$")) -> "https://imgs.ysscores.com/championship/128/$clean.png"
    else -> clean
  }
}

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

data class PlayerInfo(
  val name: String,
  val number: Int,
  val position: String,
  val imageUrl: String
)

/**
 * Lineups for both teams
 */
data class MatchLineup(
  val formationHome: String = "4-4-2",
  val formationAway: String = "4-3-3",
  val coachHome: String = "",
  val coachHomeImage: String = "",
  val coachAway: String = "",
  val coachAwayImage: String = "",
  val startersHome: List<PlayerInfo> = emptyList(),
  val startersAway: List<PlayerInfo> = emptyList(),
  val injuredHome: List<PlayerInfo> = emptyList()
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
  val isStreamActive: Boolean = false,
  val liveMinute: String? = null, // e.g. "34'" or "الشوط الثاني"
  val scoreHome: Int? = null,
  val scoreAway: Int? = null,
  val homeGoalDetails: String? = null, // e.g. "فينيسيوس 24' • بيلينغهام 72'"
  val awayGoalDetails: String? = null, // e.g. "دي بروين 45'"
  val countdownText: String? = null, // e.g. "01 أيام : 05 ساعات : 28 دقائق"
  val bannerUrl: String? = null, // Hero image / player cutouts
  val stats: MatchStats? = null,
  val lineups: MatchLineup? = null,
  val standings: List<StandingRow> = emptyList(),
  val h2h: List<H2hMatch> = emptyList(),
  val isFavorite: Boolean = false
) {
  val hasPlayableStream: Boolean get() = isStreamActive && (streamUrl.isNotBlank() || servers.any { it.streamUrl.isNotBlank() })
}

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
 * Server Live Notification & Push Announcement
 */
data class AnnouncementConfig(
  val isEnabled: Boolean = false,
  val id: String = "",
  val title: String = "",
  val message: String = "",
  val targetType: String = "none", // "match", "channel", "news", "url", "none"
  val targetId: String = "",
  val actionUrl: String = ""
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

