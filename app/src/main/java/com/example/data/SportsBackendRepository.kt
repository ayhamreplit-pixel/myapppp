package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.os.Build
import android.provider.Settings
import com.example.model.DeviceSessionInfo
import com.example.model.H2hMatch
import com.example.model.TodUserProfile
import java.util.UUID
import com.example.model.MatchLineup
import com.example.model.MatchStats
import com.example.model.SportsCompetition
import com.example.model.SportsMatch
import com.example.model.SportsShow
import com.example.model.SportsTeam
import com.example.model.StandingRow
import com.example.model.XtreamChannel
import com.example.player.SmartStreamResolver
import com.example.player.StreamSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Configuration for the AlwaysData server backend and security keys.
 */
data class AlwaysDataConfig(
  val serverUrl: String = "https://ayham.alwaysdata.net",
  val apiKey: String = "",
  val secretKey: String = StreamSecurityManager.DEFAULT_MASTER_KEY,
  val isConfigured: Boolean = true
)

/**
 * Universal Sports Backend & Repository
 * Connects to user's AlwaysData server (https://ayham.alwaysdata.net) via PHP endpoints,
 * with AES-256 decryption, local disk caching, and rich live TOD schedule.
 */
class SportsBackendRepository(private val context: Context) {

  private val prefs: SharedPreferences = context.getSharedPreferences("tod_alwaysdata_prefs", Context.MODE_PRIVATE)

  private val _matches = MutableStateFlow<List<SportsMatch>>(emptyList())
  val matches: StateFlow<List<SportsMatch>> = _matches.asStateFlow()

  private val _competitions = MutableStateFlow<List<SportsCompetition>>(emptyList())
  val competitions: StateFlow<List<SportsCompetition>> = _competitions.asStateFlow()

  private val _shows = MutableStateFlow<List<SportsShow>>(emptyList())
  val shows: StateFlow<List<SportsShow>> = _shows.asStateFlow()

  private val _sportsChannels = MutableStateFlow<List<XtreamChannel>>(emptyList())
  val sportsChannels: StateFlow<List<XtreamChannel>> = _sportsChannels.asStateFlow()

  init {
    loadCachedOrBuiltInMatches()
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
      while (true) {
        try {
          syncFromAlwaysData()
          val activeProf = getActiveProfile()
          sendSessionHeartbeat(activeProf.name, "")
        } catch (e: Exception) {
          Log.e("SportsBackendRepo", "Realtime background sync error", e)
        }
        kotlinx.coroutines.delay(5000)
      }
    }
  }

  fun getAlwaysDataConfig(): AlwaysDataConfig {
    val url = prefs.getString("server_url", "https://ayham.alwaysdata.net/m7") ?: "https://ayham.alwaysdata.net/m7"
    val apiKey = prefs.getString("api_key", "") ?: ""
    val secretKey = prefs.getString("secret_key", StreamSecurityManager.DEFAULT_MASTER_KEY) ?: StreamSecurityManager.DEFAULT_MASTER_KEY
    val isConfigured = prefs.getBoolean("is_configured", true)
    return AlwaysDataConfig(serverUrl = url, apiKey = apiKey, secretKey = secretKey, isConfigured = isConfigured)
  }

  fun saveAlwaysDataConfig(config: AlwaysDataConfig) {
    prefs.edit()
      .putString("server_url", config.serverUrl.trim().removeSuffix("/"))
      .putString("api_key", config.apiKey.trim())
      .putString("secret_key", config.secretKey.trim())
      .putBoolean("is_configured", true)
      .apply()
  }

  /**
   * Tests connection to the AlwaysData server and measures ping in milliseconds.
   */
  suspend fun testServerConnection(): Result<Long> = withContext(Dispatchers.IO) {
    val config = getAlwaysDataConfig()
    val cleanUrl = config.serverUrl.trim().removeSuffix("/")
    val testUrl = if (cleanUrl.endsWith("/api.php")) cleanUrl else "$cleanUrl/api.php?action=server_info"

    val startTime = System.currentTimeMillis()
    try {
      val request = Request.Builder()
        .url(testUrl)
        .header("User-Agent", "TOD-Android/4.8")
        .apply {
          if (config.apiKey.isNotBlank()) header("Authorization", "Bearer ${config.apiKey}")
        }
        .build()

      SmartStreamResolver.okHttpClient.newCall(request).execute().use { response ->
        val elapsed = System.currentTimeMillis() - startTime
        if (response.isSuccessful || response.code in 200..404) {
          Result.success(elapsed.coerceAtLeast(12))
        } else {
          Result.failure(Exception("كود الاستجابة من الخادم: ${response.code}"))
        }
      }
    } catch (e: Exception) {
      // If endpoint not found, test root url
      try {
        val rootRequest = Request.Builder().url(cleanUrl).build()
        SmartStreamResolver.okHttpClient.newCall(rootRequest).execute().use {
          val elapsed = System.currentTimeMillis() - startTime
          Result.success(elapsed.coerceAtLeast(15))
        }
      } catch (rootError: Exception) {
        Result.failure(rootError)
      }
    }
  }

  /**
   * Syncs matches & channels from AlwaysData server (PHP / JSON endpoints in m7 folder)
   */
  suspend fun syncFromAlwaysData(): Result<Int> = withContext(Dispatchers.IO) {
    val config = getAlwaysDataConfig()
    val baseUrl = config.serverUrl.trim().removeSuffix("/")
    val cleanBase = baseUrl.removeSuffix("/m7")

    val candidateEndpoints = listOf(
      "$baseUrl/api.php?action=matches",
      "$baseUrl/matches.json",
      "$baseUrl/api.php",
      "$cleanBase/m7/api.php?action=matches",
      "$cleanBase/m7/matches.json",
      "$cleanBase/m7/api.php",
      "$cleanBase/api.php?action=matches",
      "$cleanBase/matches.json",
      "$cleanBase/api.php"
    )

    var syncedMatchesCount = 0

    for (endpoint in candidateEndpoints) {
      try {
        val request = Request.Builder()
          .url(endpoint)
          .header("User-Agent", "TOD-Android/4.8")
          .apply {
            if (config.apiKey.isNotBlank()) header("Authorization", "Bearer ${config.apiKey}")
          }
          .build()

        SmartStreamResolver.okHttpClient.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val bodyStr = response.body?.string() ?: ""
            if (bodyStr.isNotBlank() && (bodyStr.contains("\"matches\"") || bodyStr.startsWith("["))) {
              val parsedMatches = parseMatchesJson(bodyStr, config.secretKey)
              if (parsedMatches.isNotEmpty()) {
                _matches.value = parsedMatches
                saveMatchesToCache(bodyStr)
                syncedMatchesCount = parsedMatches.size
                Log.d("SportsBackendRepo", "Loaded ${parsedMatches.size} matches from $endpoint")
                break
              }
            }
          }
        }
      } catch (e: Exception) {
        Log.d("SportsBackendRepo", "Endpoint $endpoint skipped: ${e.message}")
      }
    }

    // Also sync channels from server
    val candidateChannelEndpoints = listOf(
      "$baseUrl/api.php?action=channels",
      "$baseUrl/channels.json",
      "$cleanBase/m7/api.php?action=channels",
      "$cleanBase/m7/channels.json",
      "$cleanBase/api.php?action=channels",
      "$cleanBase/channels.json"
    )

    for (endpoint in candidateChannelEndpoints) {
      try {
        val request = Request.Builder()
          .url(endpoint)
          .header("User-Agent", "TOD-Android/4.8")
          .build()

        SmartStreamResolver.okHttpClient.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val bodyStr = response.body?.string() ?: ""
            if (bodyStr.isNotBlank() && (bodyStr.contains("\"channels\"") || bodyStr.startsWith("["))) {
              val parsedChannels = parseChannelsJson(bodyStr)
              if (parsedChannels.isNotEmpty()) {
                _sportsChannels.value = parsedChannels
                Log.d("SportsBackendRepo", "Loaded ${parsedChannels.size} channels from $endpoint")
                break
              }
            }
          }
        }
      } catch (e: Exception) {
        Log.d("SportsBackendRepo", "Channel endpoint skipped: ${e.message}")
      }
    }

    if (syncedMatchesCount > 0) {
      Result.success(syncedMatchesCount)
    } else {
      loadCachedOrBuiltInMatches()
      Result.success(_matches.value.size)
    }
  }

  /**
   * Adds a new custom match (from Admin / Management UI)
   */
  fun addCustomMatch(match: SportsMatch) {
    val current = _matches.value.toMutableList()
    current.add(0, match)
    _matches.value = current
    saveCustomMatchLocal(match)
  }

  private fun saveCustomMatchLocal(match: SportsMatch) {
    try {
      val savedList = prefs.getString("custom_matches_json", "[]") ?: "[]"
      val array = JSONArray(savedList)
      val obj = JSONObject().apply {
        put("id", match.id)
        put("title", match.title)
        put("tournament", match.tournament)
        put("homeTeam", match.homeTeam.name)
        put("awayTeam", match.awayTeam.name)
        put("kickoffTime", match.kickoffTime)
        put("kickoffDate", match.kickoffDate)
        put("stadium", match.stadium)
        put("channelName", match.channelName)
        put("streamUrl", match.streamUrl)
        put("isLive", match.isLive)
      }
      array.put(obj)
      prefs.edit().putString("custom_matches_json", array.toString()).apply()
    } catch (ignored: Exception) {}
  }

  private fun saveMatchesToCache(json: String) {
    prefs.edit().putString("cached_matches_payload", json).apply()
  }

  private fun parseChannelsJson(json: String): List<XtreamChannel> {
    val list = mutableListOf<XtreamChannel>()
    try {
      val root = JSONObject(json)
      val array = root.optJSONArray("channels") ?: JSONArray(json)
      for (i in 0 until array.length()) {
        val item = array.getJSONObject(i)
        val streamId = item.optString("streamId", item.optString("stream_id", "ch_$i"))
        val name = item.optString("name", "قناة رياضية")
        val icon = item.optString("iconUrl", item.optString("stream_icon", ""))
        val category = item.optString("categoryId", item.optString("category_id", "قنوات beIN SPORTS"))
        val playUrl = item.optString("playUrl", item.optString("stream_url", ""))
        list.add(
          XtreamChannel(
            streamId = streamId,
            name = name,
            iconUrl = icon,
            categoryId = category,
            playUrl = playUrl
          )
        )
      }
    } catch (e: Exception) {
      Log.e("SportsBackendRepo", "Channels JSON parse error", e)
    }
    return list
  }

  private fun JSONObject.optNullableInt(key: String): Int? {
    if (!has(key) || isNull(key)) return null
    return try {
      val obj = opt(key)
      when (obj) {
        is Number -> obj.toInt()
        is String -> obj.toIntOrNull()
        else -> null
      }
    } catch (e: Exception) { null }
  }

  private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return try {
      val str = optString(key, "").trim()
      if (str.isEmpty() || str.equals("null", ignoreCase = true)) null else str
    } catch (e: Exception) { null }
  }

  private fun parseMatchesJson(json: String, secretKey: String): List<SportsMatch> {
    val list = mutableListOf<SportsMatch>()
    try {
      val root = JSONObject(json)
      val array = root.optJSONArray("matches") ?: JSONArray(json)
      for (i in 0 until array.length()) {
        val item = array.getJSONObject(i)
        val rawStream = item.optString("streamUrl", "")
        val decryptedStream = if (rawStream.startsWith("enc:") || rawStream.startsWith("aes:")) {
          StreamSecurityManager.decryptStreamUrl(rawStream, secretKey)
        } else rawStream

        val homeTeamName = item.optString("homeTeam", item.optString("home_team", "الفريق 1"))
        val homeLogo = item.optString("homeLogo", item.optString("home_logo", item.optString("homeTeamLogo", "")))
        val homeFlag = item.optString("homeFlag", item.optString("home_flag", "⚽"))

        val awayTeamName = item.optString("awayTeam", item.optString("away_team", "الفريق 2"))
        val awayLogo = item.optString("awayLogo", item.optString("away_logo", item.optString("awayTeamLogo", "")))
        val awayFlag = item.optString("awayFlag", item.optString("away_flag", "⚽"))

        val liveMin = item.optNullableString("liveMinute") ?: item.optNullableString("minute")
        val scoreH = item.optNullableInt("scoreHome") ?: item.optNullableInt("home_score")
        val scoreA = item.optNullableInt("scoreAway") ?: item.optNullableInt("away_score")
        val countdown = item.optNullableString("countdownText") ?: item.optNullableString("countdown")

        val match = SportsMatch(
          id = item.optString("id", "m_$i"),
          title = item.optString("title", "$homeTeamName ضد $awayTeamName"),
          tournament = item.optString("tournament", item.optString("competition", "دوري أبطال أوروبا")),
          tournamentLogo = item.optString("tournamentLogo", item.optString("tournament_logo", "")),
          homeTeam = SportsTeam(name = homeTeamName, logoUrl = homeLogo, flagEmoji = homeFlag),
          awayTeam = SportsTeam(name = awayTeamName, logoUrl = awayLogo, flagEmoji = awayFlag),
          kickoffTime = item.optString("kickoffTime", item.optString("time", "20:00")),
          kickoffDate = item.optString("kickoffDate", item.optString("date", "اليوم")),
          stadium = item.optString("stadium", item.optString("venue", "")),
          commentator = item.optString("commentator", "تعليق عربي"),
          channelName = item.optString("channelName", item.optString("channel", "beIN SPORTS 1 HD")),
          channelId = item.optString("channelId", "bein_1"),
          streamUrl = decryptedStream,
          isLive = item.optBoolean("isLive", item.optBoolean("is_live", false)),
          isEnded = item.optBoolean("isEnded", item.optBoolean("is_ended", false)),
          liveMinute = liveMin,
          scoreHome = scoreH,
          scoreAway = scoreA,
          countdownText = countdown,
          bannerUrl = item.optString("bannerUrl", item.optString("poster", ""))
        )
        list.add(match)
      }
    } catch (e: Exception) {
      Log.e("SportsBackendRepo", "JSON parse error safely handled", e)
    }
    return list
  }

  /**
   * Generates or retrieves stable unique device identifier for tracking on external server.
   */
  fun getDeviceId(): String {
    var deviceId = prefs.getString("stable_device_id", null)
    if (deviceId == null) {
      val androidId = try {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
      } catch (e: Exception) { null }
      deviceId = androidId ?: UUID.randomUUID().toString()
      prefs.edit().putString("stable_device_id", deviceId).apply()
    }
    return deviceId
  }

  /**
   * Retrieves clean device name for external dashboard.
   */
  fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    val model = Build.MODEL
    return "$manufacturer $model (Android ${Build.VERSION.RELEASE})"
  }

  /**
   * Sends device session heartbeat & currently watched stream telemetry to external PHP server.
   */
  suspend fun sendSessionHeartbeat(profileName: String, activeStreamTitle: String = ""): Result<Boolean> = withContext(Dispatchers.IO) {
    val config = getAlwaysDataConfig()
    val baseUrl = config.serverUrl.trim().removeSuffix("/")
    val cleanBase = baseUrl.removeSuffix("/m7")
    val candidateEndpoints = listOf(
      "$baseUrl/api.php?action=session_ping",
      "$cleanBase/m7/api.php?action=session_ping",
      "$cleanBase/api.php?action=session_ping"
    )

    var success = false
    for (endpoint in candidateEndpoints) {
      try {
        val payload = JSONObject().apply {
          put("deviceId", getDeviceId())
          put("deviceName", getDeviceName())
          put("profileName", profileName)
          put("activeStream", activeStreamTitle.ifBlank { "تصفح التطبيق" })
          put("appVersion", "TOD-v4.8.2")
          put("timestamp", System.currentTimeMillis())
        }

        val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
          .url(endpoint)
          .post(requestBody)
          .header("User-Agent", "TOD-Android/4.8")
          .header("X-Device-ID", getDeviceId())
          .apply {
            if (config.apiKey.isNotBlank()) header("Authorization", "Bearer ${config.apiKey}")
          }
          .build()

        SmartStreamResolver.okHttpClient.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            success = true
            return@withContext Result.success(true)
          }
        }
      } catch (e: Exception) {
        Log.d("SportsBackendRepo", "Heartbeat to $endpoint failed: ${e.message}")
      }
    }

    Result.success(success)
  }

  /**
   * Fetches active sessions / devices watching right now from server
   */
  suspend fun fetchActiveSessions(): List<DeviceSessionInfo> = withContext(Dispatchers.IO) {
    val config = getAlwaysDataConfig()
    val baseUrl = config.serverUrl.trim().removeSuffix("/")
    val cleanBase = baseUrl.removeSuffix("/m7")
    val candidateEndpoints = listOf(
      "$baseUrl/api.php?action=get_sessions",
      "$cleanBase/m7/api.php?action=get_sessions",
      "$cleanBase/api.php?action=get_sessions"
    )

    for (endpoint in candidateEndpoints) {
      try {
        val request = Request.Builder()
          .url(endpoint)
          .header("User-Agent", "TOD-Android/4.8")
          .build()

        SmartStreamResolver.okHttpClient.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val body = response.body?.string() ?: ""
            if (body.contains("\"sessions\"")) {
              val root = JSONObject(body)
              val array = root.optJSONArray("sessions") ?: JSONArray()
              val list = mutableListOf<DeviceSessionInfo>()
              for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                  DeviceSessionInfo(
                    deviceId = item.optString("deviceId", "dev_$i"),
                    deviceName = item.optString("deviceName", "هاتف أندرويد"),
                    profileName = item.optString("profileName", "VIP"),
                    activeStreamTitle = item.optString("activeStream", "تصفح التطبيق"),
                    ipAddress = item.optString("ip", "127.0.0.1"),
                    timestamp = item.optLong("lastPing", System.currentTimeMillis() / 1000) * 1000
                  )
                )
              }
              return@withContext list
            }
          }
        }
      } catch (e: Exception) {
        Log.d("SportsBackendRepo", "Sessions query failed: ${e.message}")
      }
    }
    emptyList()
  }

  /**
   * "من يشاهد الآن؟" User Profiles management
   */
  fun getUserProfiles(): List<TodUserProfile> {
    val json = prefs.getString("user_profiles_json", null)
    if (json != null) {
      try {
        val array = JSONArray(json)
        val list = mutableListOf<TodUserProfile>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          list.add(
            TodUserProfile(
              id = obj.getString("id"),
              name = obj.getString("name"),
              avatarEmoji = obj.optString("avatarEmoji", "⚽"),
              avatarGradientHex = obj.optLong("avatarGradientHex", 0xFFFFB800),
              isKids = obj.optBoolean("isKids", false),
              isVip = obj.optBoolean("isVip", true)
            )
          )
        }
        if (list.isNotEmpty()) return list
      } catch (ignored: Exception) {}
    }

    // Default official TOD profiles
    return listOf(
      TodUserProfile("p1", "المشترك VIP", "👑", 0xFFFFB800, isKids = false, isVip = true),
      TodUserProfile("p2", "الرياضة المباشرة", "⚽", 0xFF007AFF, isKids = false, isVip = true),
      TodUserProfile("p3", "مباريات اليوم", "🔥", 0xFFFF3B30, isKids = false, isVip = false)
    )
  }

  fun getActiveProfile(): TodUserProfile {
    val activeId = prefs.getString("active_profile_id", "p1")
    return getUserProfiles().find { it.id == activeId } ?: getUserProfiles().first()
  }

  fun setActiveProfile(profile: TodUserProfile) {
    prefs.edit().putString("active_profile_id", profile.id).apply()
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
      sendSessionHeartbeat(profile.name, "")
    }
  }

  fun addProfile(name: String, emoji: String = "⚽", isKids: Boolean = false) {
    val list = getUserProfiles().toMutableList()
    val newProfile = TodUserProfile(
      id = "p_${System.currentTimeMillis()}",
      name = name,
      avatarEmoji = emoji,
      avatarGradientHex = 0xFF64D2FF,
      isKids = isKids,
      isVip = true
    )
    list.add(newProfile)
    val array = JSONArray()
    list.forEach {
      val obj = JSONObject().apply {
        put("id", it.id)
        put("name", it.name)
        put("avatarEmoji", it.avatarEmoji)
        put("avatarGradientHex", it.avatarGradientHex)
        put("isKids", it.isKids)
        put("isVip", it.isVip)
      }
      array.put(obj)
    }
    prefs.edit().putString("user_profiles_json", array.toString()).apply()
  }

  /**
   * Initializes official TOD Schedule from cache or server
   */
  fun loadCachedOrBuiltInMatches() {
    val cachedPayload = prefs.getString("cached_matches_payload", null)
    if (!cachedPayload.isNullOrBlank()) {
      val parsed = parseMatchesJson(cachedPayload, getAlwaysDataConfig().secretKey)
      if (parsed.isNotEmpty()) {
        _matches.value = parsed
        return
      }
    }

    val defaultList = mutableListOf<SportsMatch>()

    // 0. Featured Hero Match from TOD Screenshot: روما ضد برشلونة (4 - 0)
    defaultList.add(
      SportsMatch(
        id = "roma_barca_uwcl",
        title = "روما ضد برشلونة",
        tournament = "دوري أبطال أوروبا للسيدات",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        homeTeam = SportsTeam(name = "برشلونة", flagEmoji = "🔵🔴", code = "BAR", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/512px-FC_Barcelona_%28crest%29.svg.png"),
        awayTeam = SportsTeam(name = "روما", flagEmoji = "🟡🔴", code = "ROM", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f7/AS_Roma_logo_%282017%29.svg/512px-AS_Roma_logo_%282017%29.svg.png"),
        kickoffTime = "19:45",
        kickoffDate = "٣٠ سبتمبر",
        stadium = "Stadio Tre Fontane",
        commentator = "عصام الشوالي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        bannerUrl = "android.resource://com.example/drawable/tod_hero_match_banner",
        isLive = true,
        liveMinute = "60'",
        scoreHome = 4,
        scoreAway = 0,
        stats = MatchStats(possessionHome = 65, possessionAway = 35, shotsOnTargetHome = 9, shotsOnTargetAway = 2, totalShotsHome = 18, totalShotsAway = 5, cornersHome = 8, cornersAway = 2, foulsHome = 6, foulsAway = 10, yellowCardsHome = 1, yellowCardsAway = 2)
      )
    )

    // 1. Featured Hero Match 1 (Screenshot 8, 11, 15): ويلز ضد النرويج
    defaultList.add(
      SportsMatch(
        id = "wales_norway_nations",
        title = "ويلز ضد النرويج",
        tournament = "دوري الأمم الأوروبية",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png",
        homeTeam = SportsTeam(name = "ويلز", flagEmoji = "🏴󠁧󠁢󠁷󠁬󠁳󠁿", code = "WAL", logoUrl = "https://flagcdn.com/w80/gb-wls.png"),
        awayTeam = SportsTeam(name = "النرويج", flagEmoji = "🇳🇴", code = "NOR", logoUrl = "https://flagcdn.com/w80/no.png"),
        kickoffTime = "21:45",
        kickoffDate = "1 أكتوبر 2026",
        stadium = "استاد مدينة كارديف",
        commentator = "حفيظ دراجي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        countdownText = "01 أيام : 05 ساعات : 28 دقائق",
        isLive = false,
        stats = MatchStats(possessionHome = 46, possessionAway = 54, shotsOnTargetHome = 4, shotsOnTargetAway = 7, totalShotsHome = 9, totalShotsAway = 14, cornersHome = 3, cornersAway = 6, foulsHome = 11, foulsAway = 9, yellowCardsHome = 2, yellowCardsAway = 1),
        lineups = MatchLineup(
          formationHome = "4-2-3-1", formationAway = "4-3-3",
          coachHome = "كريغ بيلامي", coachAway = "ستالي سولباكين",
          startersHome = listOf("وارد (حارس)", "روبرتس", "رودون", "ديفيز", "ويليامز", "أمبادو", "جيمس", "برينان جونسون", "ويلسون", "توماس", "كيفر مور"),
          startersAway = listOf("نيلاند (حارس)", "رايرسون", "أوستيغارد", "أيير", "مولر وولف", "بيرغ", "ثورستفيت", "أوديغارد", "نوسا", "إيرلينغ هالاند", "سورلوث")
        ),
        standings = listOf(
          StandingRow(1, "النرويج", "https://flagcdn.com/w80/no.png", 2, 4, 6),
          StandingRow(2, "النمسا", "https://flagcdn.com/w80/at.png", 2, 2, 4),
          StandingRow(3, "ويلز", "https://flagcdn.com/w80/gb-wls.png", 2, 0, 3),
          StandingRow(4, "كازاخستان", "https://flagcdn.com/w80/kz.png", 2, -6, 0)
        ),
        h2h = listOf(
          H2hMatch("14 أكتوبر 2024", "دوري الأمم الأوروبية", "النرويج", "ويلز", "2 - 1", "النرويج"),
          H2hMatch("10 سبتمبر 2024", "دوري الأمم الأوروبية", "ويلز", "النرويج", "0 - 0", null)
        )
      )
    )

    // 2. Featured Match 2 (Screenshots 17, 21): ألمانيا ضد صربيا
    defaultList.add(
      SportsMatch(
        id = "germany_serbia_nations",
        title = "ألمانيا ضد صربيا",
        tournament = "دوري الأمم الأوروبية",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png",
        homeTeam = SportsTeam(name = "ألمانيا", flagEmoji = "🇩🇪", code = "GER", logoUrl = "https://flagcdn.com/w80/de.png"),
        awayTeam = SportsTeam(name = "صربيا", flagEmoji = "🇷🇸", code = "SRB", logoUrl = "https://flagcdn.com/w80/rs.png"),
        kickoffTime = "21:45",
        kickoffDate = "1 أكتوبر 2026",
        stadium = "أليانز أرينا",
        commentator = "عصام الشوالي",
        channelName = "beIN SPORTS 2 HD",
        channelId = "bein_2",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        countdownText = "01 أيام : 05 ساعات : 27 دقائق",
        isLive = false,
        stats = MatchStats(possessionHome = 62, possessionAway = 38, shotsOnTargetHome = 8, shotsOnTargetAway = 3, totalShotsHome = 18, totalShotsAway = 7, cornersHome = 7, cornersAway = 2, foulsHome = 8, foulsAway = 14, yellowCardsHome = 1, yellowCardsAway = 3),
        lineups = MatchLineup(
          formationHome = "4-2-3-1", formationAway = "3-4-2-1",
          coachHome = "يوليان ناغلسمان", coachAway = "دراغان ستويكوفيتش",
          startersHome = listOf("باومان (حارس)", "كيميتش", "روديغر", "شلوتربيك", "ميتيلشتيت", "أندريش", "بافلوفيتش", "فلوريان فيرتز", "جمال موسيالا", "كاي هافيرتز", "أونديف"),
          startersAway = listOf("راجكوفيتش (حارس)", "إيراكوفيتش", "ميلينكوفيتش", "بافلوفيتش", "نديلكوفيتش", "ماكسيموفيتش", "غروجيتش", "بيرمانسيفيتش", "ساماردزيتش", "لوكيتش", "دوشان فلاهوفيتش")
        ),
        standings = listOf(
          StandingRow(1, "ألمانيا", "https://flagcdn.com/w80/de.png", 2, 5, 6),
          StandingRow(2, "هولندا", "https://flagcdn.com/w80/nl.png", 2, 3, 4),
          StandingRow(3, "البوسنة والهرسك", "https://flagcdn.com/w80/ba.png", 2, -4, 1),
          StandingRow(4, "صربيا", "https://flagcdn.com/w80/rs.png", 2, -4, 0)
        ),
        h2h = listOf(
          H2hMatch("20 مارس 2019", "مباراة ودية", "ألمانيا", "صربيا", "1 - 1", null),
          H2hMatch("18 يونيو 2010", "كأس العالم", "ألمانيا", "صربيا", "0 - 1", "صربيا")
        )
      )
    )

    // 3. Featured Match 3 (Screenshot 10, 18): مانشستر يونايتد ضد صباح (4 - 0)
    defaultList.add(
      SportsMatch(
        id = "man_utd_sabah_ucl",
        title = "مانشستر يونايتد ضد صباح",
        tournament = "دوري أبطال أوروبا",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        homeTeam = SportsTeam(name = "مان يونايتد", flagEmoji = "🔴", code = "MUN", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/7/7a/Manchester_United_FC_crest.svg/512px-Manchester_United_FC_crest.svg.png"),
        awayTeam = SportsTeam(name = "صباح", flagEmoji = "🟣", code = "SAB", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Sabah_FK_logo.svg/512px-Sabah_FK_logo.svg.png"),
        kickoffTime = "22:00",
        kickoffDate = "10 سبتمبر 2026",
        stadium = "أولد ترافورد",
        commentator = "خليل البلوشي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
        isLive = false,
        isEnded = true,
        scoreHome = 4,
        scoreAway = 0,
        stats = MatchStats(possessionHome = 68, possessionAway = 32, shotsOnTargetHome = 11, shotsOnTargetAway = 1, totalShotsHome = 22, totalShotsAway = 4, cornersHome = 9, cornersAway = 1, foulsHome = 7, foulsAway = 12, yellowCardsHome = 1, yellowCardsAway = 2)
      )
    )

    // 4. Live Match (Screenshot 11, 14): فرنسا ضد العراق (FIFA Live Match)
    defaultList.add(
      SportsMatch(
        id = "france_iraq_live",
        title = "فرنسا ضد العراق",
        tournament = "مباريات الفيفا الدولية",
        homeTeam = SportsTeam(name = "فرنسا", flagEmoji = "🇫🇷", code = "FRA", logoUrl = "https://flagcdn.com/w80/fr.png"),
        awayTeam = SportsTeam(name = "العراق", flagEmoji = "🇮🇶", code = "IRQ", logoUrl = "https://flagcdn.com/w80/iq.png"),
        kickoffTime = "19:00",
        kickoffDate = "اليوم",
        stadium = "استاد لوسيل",
        commentator = "علي محمد علي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = true,
        liveMinute = "مباشر 68'",
        scoreHome = 2,
        scoreAway = 1,
        stats = MatchStats(possessionHome = 58, possessionAway = 42, shotsOnTargetHome = 7, shotsOnTargetAway = 4, totalShotsHome = 14, totalShotsAway = 8, cornersHome = 6, cornersAway = 3, foulsHome = 9, foulsAway = 13, yellowCardsHome = 1, yellowCardsAway = 2)
      )
    )

    // 5. Live Tennis (Screenshot 16): نونو بورجس ضد نوفاك ديوكوفيتش
    defaultList.add(
      SportsMatch(
        id = "tennis_borges_djokovic",
        title = "نونو بورجس ضد نوفاك ديوكوفيتش",
        tournament = "بطولة الصين المفتوحة للتنس",
        homeTeam = SportsTeam(name = "ديوكوفيتش", flagEmoji = "🇷🇸", code = "SRB", logoUrl = "https://flagcdn.com/w80/rs.png"),
        awayTeam = SportsTeam(name = "نونو بورجس", flagEmoji = "🇵🇹", code = "POR", logoUrl = "https://flagcdn.com/w80/pt.png"),
        kickoffTime = "14:00",
        kickoffDate = "اليوم",
        stadium = "الملعب الرئيسي - بكين",
        channelName = "beIN SPORTS 6 HD",
        channelId = "bein_6",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = true,
        liveMinute = "المجموعة الثانية (4 - 2)"
      )
    )

    // 6. Live Padel (Screenshot 4, 11): Franco Stupaczuk vs Mario Ortega
    defaultList.add(
      SportsMatch(
        id = "padel_rotterdam_live",
        title = "Franco Stupaczuk & Jon Sanz vs Mario Ortega",
        tournament = "Rotterdam P2 Premier Padel",
        homeTeam = SportsTeam(name = "Stupaczuk / Sanz", flagEmoji = "🎾", code = "ARG"),
        awayTeam = SportsTeam(name = "Ortega / Axelsson", flagEmoji = "🎾", code = "ESP"),
        kickoffTime = "16:00",
        kickoffDate = "اليوم",
        stadium = "Rotterdam Ahoy Court 1",
        channelName = "beIN SPORTS 7 HD",
        channelId = "bein_7",
        streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
        isLive = true,
        liveMinute = "مباشر"
      )
    )

    // 7. Upcoming UCL (Screenshot 3, 15): روما ضد برشلونة
    defaultList.add(
      SportsMatch(
        id = "roma_barcelona_ucl",
        title = "روما ضد برشلونة",
        tournament = "دوري أبطال أوروبا للسيدات",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        homeTeam = SportsTeam(name = "روما", flagEmoji = "🐺", code = "ROM", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f7/AS_Roma_logo_%282017%29.svg/512px-AS_Roma_logo_%282017%29.svg.png"),
        awayTeam = SportsTeam(name = "برشلونة", flagEmoji = "🔵🔴", code = "BAR", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/512px-FC_Barcelona_%28crest%29.svg.png"),
        kickoffTime = "19:45",
        kickoffDate = "30 سبتمبر 2026",
        stadium = "Stadio Tre Fontane",
        commentator = "خالد الحدي",
        channelName = "beIN SPORTS 3 HD",
        channelId = "bein_3",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = false,
        countdownText = "03 ساعات : 28 دقائق"
      )
    )

    // 8. Upcoming UCL (Screenshot 4, 7): باريس ضد آرسنال
    defaultList.add(
      SportsMatch(
        id = "paris_arsenal_ucl",
        title = "باريس ضد آرسنال",
        tournament = "دوري أبطال أوروبا",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        homeTeam = SportsTeam(name = "باريس", flagEmoji = "🔵", code = "PSG", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/a/a7/Paris_Saint-Germain_F.C..svg/512px-Paris_Saint-Germain_F.C..svg.png"),
        awayTeam = SportsTeam(name = "آرسنال", flagEmoji = "🔴", code = "ARS", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/5/53/Arsenal_FC.svg/512px-Arsenal_FC.svg.png"),
        kickoffTime = "19:45",
        kickoffDate = "30 سبتمبر 2026",
        stadium = "حديقة الأمراء",
        commentator = "جواد بدة",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = false
      )
    )

    // 9. Friendly (Screenshot 4): ليتوانيا ضد أندورا
    defaultList.add(
      SportsMatch(
        id = "lithuania_andorra",
        title = "ليتوانيا ضد أندورا",
        tournament = "مباريات دولية ودية",
        homeTeam = SportsTeam(name = "ليتوانيا", flagEmoji = "🇱🇹", code = "LTU", logoUrl = "https://flagcdn.com/w80/lt.png"),
        awayTeam = SportsTeam(name = "أندورا", flagEmoji = "🇦🇩", code = "AND", logoUrl = "https://flagcdn.com/w80/ad.png"),
        kickoffTime = "19:00",
        kickoffDate = "30 سبتمبر 2026",
        stadium = "استاد فيلنيوس",
        commentator = "أحمد البلوشي",
        channelName = "beIN SPORTS Xtra",
        channelId = "bein_xtra",
        streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
        isLive = false
      )
    )

    // 10. La Liga (Screenshot 14): ديبورتيفو لاكو ضد ليفانتي
    defaultList.add(
      SportsMatch(
        id = "depor_levante_laliga",
        title = "ديبورتيفو لاكو ضد ليفانتي",
        tournament = "الدوري الإسباني - لا ليجا",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/LaLiga_logo_2023.svg/512px-LaLiga_logo_2023.svg.png",
        homeTeam = SportsTeam(name = "ديبورتيفو لاكو", flagEmoji = "⚪🔵", code = "DEP", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/4e/Deportivo_La_Coruna_logo.svg/512px-Deportivo_La_Coruna_logo.svg.png"),
        awayTeam = SportsTeam(name = "ليفانتي", flagEmoji = "🔴🔵", code = "LEV", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/7/7b/Levante_Uni%C3%B3n_Deportiva%2C_S.A.D._logo.svg/512px-Levante_Uni%C3%B3n_Deportiva%2C_S.A.D._logo.svg.png"),
        kickoffTime = "22:00",
        kickoffDate = "16 أكتوبر 2026",
        stadium = "ريازور",
        commentator = "حفيظ دراجي",
        channelName = "beIN SPORTS 3 HD",
        channelId = "bein_3",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = false
      )
    )

    // 11. UCL (Screenshot 3): ليون ضد تشلسي
    defaultList.add(
      SportsMatch(
        id = "lyon_chelsea_ucl",
        title = "ليون ضد تشلسي",
        tournament = "دوري أبطال أوروبا",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        homeTeam = SportsTeam(name = "ليون", flagEmoji = "🦁", code = "OL", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/c/c6/Olympique_Lyonnais.svg/512px-Olympique_Lyonnais.svg.png"),
        awayTeam = SportsTeam(name = "تشلسي", flagEmoji = "🔵", code = "CHE", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/c/cc/Chelsea_FC.svg/512px-Chelsea_FC.svg.png"),
        kickoffTime = "19:45",
        kickoffDate = "1 أكتوبر 2026",
        stadium = "بارك أولمبيك ليون",
        commentator = "محمد بركات",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = false
      )
    )

    // 12. Africa Cup / Friendly (Screenshot 10): الكونغو ضد الكاميرون
    defaultList.add(
      SportsMatch(
        id = "congo_cameroon",
        title = "الكونغو ضد الكاميرون",
        tournament = "تصفيات أمم إفريقيا",
        homeTeam = SportsTeam(name = "الكونغو", flagEmoji = "🇨🇬", code = "CGO", logoUrl = "https://flagcdn.com/w80/cg.png"),
        awayTeam = SportsTeam(name = "الكاميرون", flagEmoji = "🇨🇲", code = "CMR", logoUrl = "https://flagcdn.com/w80/cm.png"),
        kickoffTime = "22:00",
        kickoffDate = "29 سبتمبر 2026",
        stadium = "استاد أحمدو أهيدجو",
        commentator = "نوفل الباشي",
        channelName = "beIN SPORTS 4 HD",
        channelId = "bein_4",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = false,
        isEnded = true,
        scoreHome = 1,
        scoreAway = 1
      )
    )

    // 10. La Liga match
    defaultList.add(
      SportsMatch(
        id = "dep_levante",
        title = "ديبورتيفو ضد ليفانتي",
        tournament = "الدوري الإسباني - لا ليجا",
        homeTeam = SportsTeam(name = "ديبورتيفو لاكو", flagEmoji = "⚪🔵", code = "DEP"),
        awayTeam = SportsTeam(name = "ليفانتي", flagEmoji = "🔴🔵", code = "LEV"),
        kickoffTime = "22:00",
        kickoffDate = "16 أكتوبر 2026",
        stadium = "استاد ريازور",
        channelName = "beIN SPORTS 3 HD",
        channelId = "bein_3",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = false
      )
    )

    // 11. CAF (Screenshot 15): الكونغو ضد الكاميرون
    defaultList.add(
      SportsMatch(
        id = "congo_cameroon_caf",
        title = "الكونغو ضد الكاميرون",
        tournament = "تصفيات أمم أفريقيا",
        homeTeam = SportsTeam(name = "الكونغو", flagEmoji = "🇨🇬", code = "CGO"),
        awayTeam = SportsTeam(name = "الكاميرون", flagEmoji = "🇨🇲", code = "CMR"),
        kickoffTime = "22:00",
        kickoffDate = "29 سبتمبر 2026",
        stadium = "استاد ألفونس ماسيمبا",
        channelName = "beIN SPORTS 4 HD",
        channelId = "bein_4",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = false,
        isEnded = true,
        scoreHome = 1,
        scoreAway = 1
      )
    )

    // Load any user added custom matches
    val customJson = prefs.getString("custom_matches_json", "[]") ?: "[]"
    try {
      val customArray = JSONArray(customJson)
      for (i in 0 until customArray.length()) {
        val o = customArray.getJSONObject(i)
        defaultList.add(
          0,
          SportsMatch(
            id = o.optString("id", "custom_$i"),
            title = o.optString("title", "مباراة مخصصة"),
            tournament = o.optString("tournament", "دوري مخصص"),
            homeTeam = SportsTeam(name = o.optString("homeTeam", "الفريق الأول")),
            awayTeam = SportsTeam(name = o.optString("awayTeam", "الفريق الثاني")),
            kickoffTime = o.optString("kickoffTime", "21:00"),
            kickoffDate = o.optString("kickoffDate", "اليوم"),
            stadium = o.optString("stadium", "الاستاد"),
            channelName = o.optString("channelName", "البث المباشر"),
            streamUrl = o.optString("streamUrl", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
            isLive = o.optBoolean("isLive", true)
          )
        )
      }
    } catch (ignored: Exception) {}

    _matches.value = defaultList

    // Initialize Competitions (Screenshots 10, 16, 18, 20)
    _competitions.value = listOf(
      SportsCompetition("ucl", "دوري أبطال أوروبا", "2026/2027", accentColorHex = 0xFF003399, matchesCount = 16),
      SportsCompetition("unl", "دوري الأمم الأوروبية", "2026/2027", accentColorHex = 0xFF104E8B, matchesCount = 12),
      SportsCompetition("laliga", "الدوري الإسباني - لا ليجا", "2026/2027", accentColorHex = 0xFFFF0044, matchesCount = 10),
      SportsCompetition("pl", "الدوري الإنجليزي الممتاز", "2026/2027", accentColorHex = 0xFF3D195B, matchesCount = 10),
      SportsCompetition("f1", "فورمولا 1", "موسم 2026", accentColorHex = 0xFFE10600, matchesCount = 4),
      SportsCompetition("atp", "بطولات التنس العالمية (ATP)", "2026", accentColorHex = 0xFF004488, matchesCount = 8),
      SportsCompetition("caf", "تصفيات أمم أفريقيا", "2026/2027", accentColorHex = 0xFFFFB800, matchesCount = 6)
    )

    // Initialize Sports Shows (Screenshots 15, 17, 18)
    _shows.value = listOf(
      SportsShow("netbusters", "NETBUSTERS", "أروع وأقوى أهداف الجولة من زوايا حصرية", "25 دقيقة", streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
      SportsShow("highlights", "HIGHLIGHTS", "ملخصات شاملة لدوري أبطال أوروبا", "30 دقيقة", streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
      SportsShow("pl_rewind", "PL REWIND", "حصاد وإعادة مباريات الدوري الإنجليزي", "45 دقيقة", streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
      SportsShow("fifa_story", "THE STORY OF FIFA WORLD CUP", "الفيلم الوثائقي الرسمي لكأس العالم", "1س 15د", streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
    )

    // Initialize Direct Sports Channels (Screenshots 14, 16, 17)
    _sportsChannels.value = listOf(
      XtreamChannel("bein_1", "beIN SPORTS 1 HD", "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/BeIN_Sports_1_logo.svg/512px-BeIN_Sports_1_logo.svg.png", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_4k", "beIN SPORTS 4K Ultra", "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/BeIN_Sports_1_logo.svg/512px-BeIN_Sports_1_logo.svg.png", "قنوات الرياضة", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
      XtreamChannel("bein_2", "beIN SPORTS 2 HD", "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/BeIN_Sports_1_logo.svg/512px-BeIN_Sports_1_logo.svg.png", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_3", "beIN SPORTS 3 HD", "", "قنوات الرياضة", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
      XtreamChannel("bein_news", "beIN SPORTS الإخبارية", "", "قنوات الرياضة", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"),
      XtreamChannel("alkass_extra", "قناة الكأس EXTRA HD", "", "قنوات الرياضة", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
      XtreamChannel("lfctv", "LFCTV - ليفربول الرسمي", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("mutv", "MUTV - مانشستر يونايتد", "", "قنوات الرياضة", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
      XtreamChannel("premier_league", "Premier League TV", "", "قنوات الرياضة", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"),
      XtreamChannel("aljazeera_live", "الجزيرة مباشر", "", "قنوات الجزيرة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("aljazeera_doc", "الجزيرة الوثائقية", "", "قنوات الجزيرة", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
      XtreamChannel("bein_series_1", "beIN SERIES 1 HD", "", "قنوات الأخبار", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_series_2", "beIN SERIES 2 HD", "", "قنوات الأخبار", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8")
    )
  }
}
