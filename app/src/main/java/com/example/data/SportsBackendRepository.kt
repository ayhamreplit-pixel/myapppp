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
import com.example.model.MatchStreamServer
import com.example.model.SportsCompetition
import com.example.model.SportsMatch
import com.example.model.SportsNewsItem
import com.example.model.AnnouncementConfig
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

  private val _news = MutableStateFlow<List<SportsNewsItem>>(emptyList())
  val news: StateFlow<List<SportsNewsItem>> = _news.asStateFlow()

  private val _sliderBanners = MutableStateFlow<List<String>>(emptyList())
  val sliderBanners: StateFlow<List<String>> = _sliderBanners.asStateFlow()

  private val _announcement = MutableStateFlow(
    AnnouncementConfig(
      isEnabled = false,
      id = "",
      title = "",
      message = "",
      targetType = "none",
      targetId = ""
    )
  )
  val announcement: StateFlow<AnnouncementConfig> = _announcement.asStateFlow()

  private val _competitions = MutableStateFlow<List<SportsCompetition>>(emptyList())
  val competitions: StateFlow<List<SportsCompetition>> = _competitions.asStateFlow()

  private val _shows = MutableStateFlow<List<SportsShow>>(emptyList())
  val shows: StateFlow<List<SportsShow>> = _shows.asStateFlow()

  private val _sportsChannels = MutableStateFlow<List<XtreamChannel>>(emptyList())
  val sportsChannels: StateFlow<List<XtreamChannel>> = _sportsChannels.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val _currentDateParam = MutableStateFlow("today")
  val currentDateParam: StateFlow<String> = _currentDateParam.asStateFlow()

  private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
  val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

  init {
    loadCachedOrBuiltInMatches()
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
      // Immediate fetch on launch
      try {
        fetchAnnouncement()
        fetchYallakoraNews()
        fetchSliderBanners()
        syncFromAlwaysData("today")
      } catch (e: Exception) {
        Log.e("SportsBackendRepo", "Startup sync error", e)
      }

      while (true) {
        kotlinx.coroutines.delay(20000)
        try {
          syncFromAlwaysData(_currentDateParam.value)
          fetchAnnouncement()
          fetchYallakoraNews()
          val activeProf = getActiveProfile()
          sendSessionHeartbeat(activeProf.name, "")
        } catch (e: Exception) {
          Log.e("SportsBackendRepo", "Realtime background sync error", e)
        }
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
   * Public function to switch and fetch matches for a specific date (today, yesterday, tomorrow, or YYYY-MM-DD)
   */
  suspend fun syncForDate(dateParam: String, forceRefresh: Boolean = false): Result<Int> {
    _currentDateParam.value = dateParam
    return syncFromAlwaysData(dateParam, forceRefresh)
  }

  /**
   * Syncs matches & channels from AlwaysData server (PHP / JSON endpoints in m7 folder)
   */
  suspend fun syncFromAlwaysData(dateParam: String? = null, forceRefresh: Boolean = false): Result<Int> = withContext(Dispatchers.IO) {
    _isSyncing.value = true
    val effectiveDateParam = dateParam ?: _currentDateParam.value
    val config = getAlwaysDataConfig()
    val baseUrl = config.serverUrl.trim().removeSuffix("/")
    val cleanBase = baseUrl.removeSuffix("/m7")

    val dateQuery = "&date=$effectiveDateParam${if (forceRefresh) "&refresh=1" else ""}"
    val candidateEndpoints = listOf(
      "$baseUrl/api.php?action=matches$dateQuery",
      "$baseUrl/matches.json",
      "$baseUrl/api.php?date=$effectiveDateParam",
      "$cleanBase/m7/api.php?action=matches$dateQuery",
      "$cleanBase/m7/matches.json",
      "$cleanBase/m7/api.php?date=$effectiveDateParam",
      "$cleanBase/api.php?action=matches$dateQuery",
      "$cleanBase/matches.json",
      "$cleanBase/api.php?date=$effectiveDateParam"
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
            if (bodyStr.isNotBlank() && (bodyStr.contains("\"data\"") || bodyStr.contains("\"matches\"") || bodyStr.startsWith("["))) {
              val parsedMatches = parseMatchesJson(bodyStr, config.secretKey)
              if (parsedMatches.isNotEmpty()) {
                val combined = mutableListOf<SportsMatch>()
                if (effectiveDateParam == "today" || effectiveDateParam == "") {
                  val featuredGermany = _matches.value.firstOrNull { it.id == "germany_serbia_nations" }
                  if (featuredGermany != null && parsedMatches.none { it.id == "germany_serbia_nations" }) {
                    combined.add(featuredGermany)
                  }
                }
                combined.addAll(parsedMatches.filter { it.id != "germany_serbia_nations" })
                // Sort: Live matches first, then matches with streaming servers, then by tournament
                val sorted = combined.sortedWith(
                  compareByDescending<SportsMatch> { it.isLive }
                    .thenByDescending { it.servers.isNotEmpty() || it.streamUrl.isNotBlank() }
                    .thenBy { it.isEnded }
                )
                _matches.value = sorted
                saveMatchesToCache(bodyStr)
                syncedMatchesCount = sorted.size
                _lastSyncTime.value = System.currentTimeMillis()
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

    // Direct YSScores live API fallback if server endpoint not configured or offline
    if (syncedMatchesCount == 0) {
      try {
        val targetDateStr = when (effectiveDateParam) {
          "yesterday" -> {
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Riyadh"))
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
              timeZone = java.util.TimeZone.getTimeZone("Asia/Riyadh")
            }.format(cal.time)
          }
          "tomorrow" -> {
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Riyadh"))
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
              timeZone = java.util.TimeZone.getTimeZone("Asia/Riyadh")
            }.format(cal.time)
          }
          "today", "" -> {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
              timeZone = java.util.TimeZone.getTimeZone("Asia/Riyadh")
            }.format(java.util.Date())
          }
          else -> effectiveDateParam
        }
        val directYsscoresUrl = "https://api-ar.ysscores.com/api/matches/matches_date_get/$targetDateStr/%5B%5D/%5B%5D/%5B%5D/D/180"

        val directRequest = Request.Builder()
          .url(directYsscoresUrl)
          .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
          .header("Accept", "application/json, text/plain, */*")
          .header("Referer", "https://ysscores.com/")
          .header("Origin", "https://ysscores.com")
          .build()

        SmartStreamResolver.okHttpClient.newCall(directRequest).execute().use { resp ->
          if (resp.isSuccessful) {
            val body = resp.body?.string() ?: ""
            if (body.isNotBlank() && body.contains("\"data\"")) {
              val parsed = parseMatchesJson(body, config.secretKey)
              if (parsed.isNotEmpty()) {
                val combined = mutableListOf<SportsMatch>()
                if (effectiveDateParam == "today" || effectiveDateParam == "") {
                  val featuredGermany = _matches.value.firstOrNull { it.id == "germany_serbia_nations" }
                  if (featuredGermany != null && parsed.none { it.id == "germany_serbia_nations" }) {
                    combined.add(featuredGermany)
                  }
                }
                combined.addAll(parsed.filter { it.id != "germany_serbia_nations" })
                val sorted = combined.sortedWith(
                  compareByDescending<SportsMatch> { it.isLive }
                    .thenByDescending { it.servers.isNotEmpty() || it.streamUrl.isNotBlank() }
                    .thenBy { it.isEnded }
                )
                _matches.value = sorted
                saveMatchesToCache(body)
                syncedMatchesCount = sorted.size
                _lastSyncTime.value = System.currentTimeMillis()
                Log.d("SportsBackendRepo", "Loaded ${parsed.size} live matches directly from YSScores API")
              }
            }
          }
        }
      } catch (e: Exception) {
        Log.d("SportsBackendRepo", "Direct YSScores API fallback error: ${e.message}")
      }
    }
    _isSyncing.value = false

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
              val parsedChannels = parseChannelsJson(bodyStr, config.secretKey)
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
   * Fetches latest sports news from Yallakora API and server endpoint
   */
  suspend fun fetchYallakoraNews(): List<SportsNewsItem> = withContext(Dispatchers.IO) {
    val config = getAlwaysDataConfig()
    val baseUrl = config.serverUrl.trim().removeSuffix("/")
    val candidateUrls = listOf(
      "$baseUrl/api.php?action=news",
      "https://sportfeeds.gemini.media/yallakoraapi/NewsList?pageIndex=1&pageSize=24&otherSportsNews=false"
    )

    for (url in candidateUrls) {
      try {
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
          .build()
        SmartStreamResolver.okHttpClient.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val body = response.body?.string() ?: ""
            val list = mutableListOf<SportsNewsItem>()
            val array = when {
              body.trim().startsWith("[") -> JSONArray(body)
              body.trim().startsWith("{") -> {
                val root = JSONObject(body)
                root.optJSONArray("news") ?: root.optJSONArray("data")
              }
              else -> null
            }
            if (array != null && array.length() > 0) {
              for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val title = item.optString("Title", item.optString("title", "خبر رياضي عاجل"))
                val date = item.optString("Date", item.optString("date", "اليوم")).replace("T", " ")
                val summary = item.optString("Brief", item.optString("summary", item.optString("description", "")))
                val category = item.optString("CategoryName", item.optString("category", "أخبار كرة القدم"))
                val source = item.optString("Source", item.optString("source", "يلا كورة"))
                val articleUrl = item.optString("Url", item.optString("url", ""))

                val picObj = item.optJSONObject("Picture")
                var imgUrl = picObj?.optString("MeduimPath", "")?.ifBlank { null }
                  ?: picObj?.optString("MediumPath", "")?.ifBlank { null }
                  ?: picObj?.optString("OriginalPath", "")?.ifBlank { null }
                  ?: picObj?.optString("SmallPath", "")?.ifBlank { null }
                  ?: item.optString("imageUrl", item.optString("image", item.optString("picture", "")))

                if (imgUrl.isNotBlank()) {
                  if (imgUrl.startsWith("//")) {
                    imgUrl = "https:$imgUrl"
                  } else if (imgUrl.startsWith("/")) {
                    imgUrl = "https://media.gemini.media$imgUrl"
                  } else if (!imgUrl.startsWith("http")) {
                    imgUrl = "https://media.gemini.media/$imgUrl"
                  }
                } else {
                  // Fallback vibrant sports photos
                  imgUrl = when (i % 5) {
                    0 -> "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800&auto=format&fit=crop"
                    1 -> "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&auto=format&fit=crop"
                    2 -> "https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=800&auto=format&fit=crop"
                    3 -> "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=800&auto=format&fit=crop"
                    else -> "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=800&auto=format&fit=crop"
                  }
                }

                list.add(
                  SportsNewsItem(
                    id = "news_${item.optString("NewsID", i.toString())}",
                    title = title,
                    date = date,
                    imageUrl = imgUrl,
                    category = category,
                    source = source,
                    summary = summary,
                    url = articleUrl
                  )
                )
              }
              if (list.isNotEmpty()) {
                _news.value = list
                return@withContext list
              }
            }
          }
        }
      } catch (e: Exception) {
        Log.d("SportsBackendRepo", "News fetch error: ${e.message}")
      }
    }

    // Default high quality news if offline
    if (_news.value.isEmpty()) {
      val fallbackNews = listOf(
        SportsNewsItem(
          id = "news_fb_1",
          title = "قمة دوري أبطال أوروبا: ريال مدريد يستعد لمواجهة مانشستر سيتي في ملحمة كروية كبرى",
          date = "اليوم • منذ ساعة",
          imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800&auto=format&fit=crop",
          category = "دوري أبطال أوروبا",
          source = "TOD الأخبارية",
          summary = "استعدادات مكثفة وتصريحات نارية قبل القمة الأوروبية المرتقبة على ملعب سانتياغو برنابيو وسط ترقب جماهيري عالمي."
        ),
        SportsNewsItem(
          id = "news_fb_2",
          title = "صراع صدارة الدوري الإنجليزي: أرسنال وليفربول في سباق ناري نحو اللقب الأغلى",
          date = "اليوم • منذ ساعتين",
          imageUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&auto=format&fit=crop",
          category = "الدوري الإنجليزي",
          source = "يلا كورة",
          summary = "جولة حاسمة في البريميرليغ وسط تقارب النقاط وصراع تكتيكي مشتعل في الأمتار الأخيرة من الموسم."
        ),
        SportsNewsItem(
          id = "news_fb_3",
          title = "برشلونة يواصل تألقه في الليغا ويحقق فوزاً عريضاً يعزز موقعه في جدول الترتيب",
          date = "اليوم • منذ 3 ساعات",
          imageUrl = "https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=800&auto=format&fit=crop",
          category = "الدوري الإسباني",
          source = "beIN SPORTS",
          summary = "أداء استثنائي وتناغم هجومي مبهر يقود البلوغرانا لحصد النقاط الثلاث ومواصلة الضغط في الصدارة."
        ),
        SportsNewsItem(
          id = "news_fb_4",
          title = "دوري أبطال آسيا للنخبة: الهلال والنصر في مواجهات قوية نحو التأهل إلى الأدوار النهائية",
          date = "اليوم • منذ 4 ساعات",
          imageUrl = "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=800&auto=format&fit=crop",
          category = "الكرة العربية",
          source = "TOD الأخبارية",
          summary = "الأندية السعودية تواصل هيمنتها الآسيوية وسط حضور جماهيري غفير ومستويات فنية متميزة."
        )
      )
      _news.value = fallbackNews
      return@withContext fallbackNews
    }
    _news.value
  }

  /**
   * Fetches real-time push announcement configured from the /m7 control panel
   */
  suspend fun fetchAnnouncement(): AnnouncementConfig = withContext(Dispatchers.IO) {
    val config = getAlwaysDataConfig()
    val baseUrl = config.serverUrl.trim().removeSuffix("/")
    val candidateUrls = listOf(
      "$baseUrl/api.php?action=announcement",
      "$baseUrl/announcement.json"
    )

    for (url in candidateUrls) {
      try {
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", "TOD-Android/4.8")
          .build()
        SmartStreamResolver.okHttpClient.newCall(request).execute().use { resp ->
          if (resp.isSuccessful) {
            val body = resp.body?.string() ?: ""
            if (body.isNotBlank()) {
              val root = JSONObject(body)
              val announceObj = root.optJSONObject("announcement") ?: root
              val isEnabled = announceObj.optBoolean("enabled", false)
              val title = announceObj.optString("title", "")
              val message = announceObj.optString("message", "")
              val targetType = announceObj.optString("target_type", "none")
              val targetId = announceObj.optString("target_id", "")
              val notif = AnnouncementConfig(
                isEnabled = isEnabled,
                id = announceObj.optString("id", "notif_live"),
                title = title,
                message = message,
                targetType = targetType,
                targetId = targetId
              )
              _announcement.value = notif
              return@withContext notif
            }
          }
        }
      } catch (e: Exception) {
        Log.d("SportsBackendRepo", "Announcement fetch error: ${e.message}")
      }
    }
    _announcement.value
  }

  /**
   * Fetches promotional slider banners from API / CDN / Server
   */
  suspend fun fetchSliderBanners(): List<String> = withContext(Dispatchers.IO) {
    val config = getAlwaysDataConfig()
    val baseUrl = config.serverUrl.trim().removeSuffix("/")
    val candidateUrls = listOf(
      "$baseUrl/api.php?action=slider",
      "https://iptv-subscription-api.tvkora56.workers.dev/v1/config",
      "https://raw.githubusercontent.com/mahmoudhwhwhwh/live-stream-premium/main/app_Slider.json"
    )

    for (url in candidateUrls) {
      try {
        val request = Request.Builder().url(url).build()
        SmartStreamResolver.okHttpClient.newCall(request).execute().use { resp ->
          if (resp.isSuccessful) {
            val body = resp.body?.string() ?: ""
            val list = mutableListOf<String>()
            if (body.trim().startsWith("{")) {
              val root = JSONObject(body)
              val sliderArr = root.optJSONArray("slider") ?: root.optJSONArray("banners")
              if (sliderArr != null) {
                for (i in 0 until sliderArr.length()) {
                  val item = sliderArr.opt(i)
                  if (item is JSONObject) {
                    val u = item.optString("image_url", item.optString("image", item.optString("url", "")))
                    if (u.isNotBlank()) list.add(u.trim())
                  } else if (item is String && item.isNotBlank()) {
                    list.add(item.trim())
                  }
                }
              }
            } else if (body.trim().startsWith("[")) {
              val arr = JSONArray(body)
              for (i in 0 until arr.length()) {
                val s = arr.optString(i, "")
                if (s.isNotBlank()) list.add(s.trim())
              }
            }
            if (list.isNotEmpty()) {
              _sliderBanners.value = list
              return@withContext list
            }
          }
        }
      } catch (e: Exception) {
        Log.d("SportsBackendRepo", "Slider fetch error: ${e.message}")
      }
    }
    emptyList()
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

  private fun parseChannelsJson(json: String, secretKey: String = getAlwaysDataConfig().secretKey): List<XtreamChannel> {
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
        val rawPlayUrl = item.optString("playUrl", item.optString("stream_url", item.optString("url", "")))
        val decryptedPlayUrl = if (rawPlayUrl.startsWith("enc:") || rawPlayUrl.startsWith("aes:") || rawPlayUrl.startsWith("sec:") || rawPlayUrl.startsWith("m7:") || rawPlayUrl.startsWith("m7enc:")) {
          StreamSecurityManager.decryptStreamUrl(rawPlayUrl, secretKey)
        } else rawPlayUrl

        list.add(
          XtreamChannel(
            streamId = streamId,
            name = name,
            iconUrl = icon,
            categoryId = category,
            playUrl = decryptedPlayUrl
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
      val trimmed = json.trim()
      if (trimmed.startsWith("{")) {
        val root = JSONObject(trimmed)
        // 1. Check if response is YSScores structure (data: [...])
        val dataArray = root.optJSONArray("data")
        if (dataArray != null && dataArray.length() > 0) {
          return parseYsscoresMatches(dataArray, secretKey)
        }
        val array = root.optJSONArray("matches")
        if (array != null) {
          return parseStandardMatchesArray(array, secretKey)
        }
      } else if (trimmed.startsWith("[")) {
        val array = JSONArray(trimmed)
        if (array.length() > 0) {
          val firstItem = array.optJSONObject(0)
          if (firstItem != null && (firstItem.has("match_id") || firstItem.has("championship") || firstItem.has("home_team"))) {
            return parseYsscoresMatches(array, secretKey)
          }
          return parseStandardMatchesArray(array, secretKey)
        }
      }
    } catch (e: Exception) {
      Log.e("SportsBackendRepo", "JSON parse error safely handled", e)
    }
    return list
  }

  private fun parseYsscoresMatches(array: JSONArray, secretKey: String): List<SportsMatch> {
    val list = mutableListOf<SportsMatch>()
    for (i in 0 until array.length()) {
      try {
        val item = array.getJSONObject(i)
        val matchId = item.optString("match_id", "ys_$i")

        val champObj = item.optJSONObject("championship")
        val champTitle = champObj?.optString("title", "مباراة اليوم") ?: "مباراة اليوم"
        val rawChampImg = champObj?.optString("image", "") ?: ""
        val champLogo = when {
          rawChampImg.startsWith("http") -> rawChampImg
          rawChampImg.isNotBlank() -> "https://imgs.ysscores.com/championship/64/$rawChampImg"
          else -> ""
        }

        val homeObj = item.optJSONObject("home_team")
        val homeTitle = homeObj?.optString("title", "الفريق المضيف") ?: "الفريق المضيف"
        val rawHomeImg = homeObj?.optString("image", "") ?: ""
        val homeLogo = when {
          rawHomeImg.startsWith("http") -> rawHomeImg
          rawHomeImg.isNotBlank() -> "https://imgs.ysscores.com/teams/128/$rawHomeImg"
          else -> ""
        }

        val awayObj = item.optJSONObject("away_team")
        val awayTitle = awayObj?.optString("title", "الفريق الضيف") ?: "الفريق الضيف"
        val rawAwayImg = awayObj?.optString("image", "") ?: ""
        val awayLogo = when {
          rawAwayImg.startsWith("http") -> rawAwayImg
          rawAwayImg.isNotBlank() -> "https://imgs.ysscores.com/teams/128/$rawAwayImg"
          else -> ""
        }

        val liveVal = item.optInt("live", 0)
        val statusVal = item.optInt("status", 1) // 1 = not started, 2 = 1st half, 3 = 2nd half, 4 = ended
        val isLive = liveVal == 1 || statusVal == 2 || statusVal == 3
        val isEnded = statusVal == 4

        val homeScores = item.optNullableInt("home_scores")
        val awayScores = item.optNullableInt("away_scores")
        val scoreTime = item.optNullableString("score_time")

        val liveMinute = when {
          isLive && !scoreTime.isNullOrBlank() -> if (scoreTime.startsWith("'")) scoreTime else "'$scoreTime"
          isLive && statusVal == 2 -> "'الشوط 1"
          isLive && statusVal == 3 -> "'الشوط 2"
          isLive -> "'مباشر"
          else -> null
        }

        val matchDate = item.optString("match_date", "اليوم")
        val matchTimeRaw = item.optString("match_time", "20:00:00")
        val matchTime = if (matchTimeRaw.length >= 5) matchTimeRaw.substring(0, 5) else matchTimeRaw

        // Channel & Commentator if available
        var channelName = item.optString("channel_name", "beIN SPORTS 1 HD")
        var commentator = item.optString("commentator", "تعليق عربي")
        val channelArr = item.optJSONArray("channel_commm")
        if (channelArr != null && channelArr.length() > 0) {
          val chObj = channelArr.optJSONObject(0)
          if (chObj != null) {
            if (!item.has("channel_name")) channelName = chObj.optString("channel_name", channelName)
            if (!item.has("commentator")) commentator = chObj.optString("commentator", commentator)
          }
        }

        // Servers attached from m7 control panel (if provided)
        val serversList = mutableListOf<MatchStreamServer>()
        val rawServers = item.optJSONArray("servers")
        if (rawServers != null) {
          for (s in 0 until rawServers.length()) {
            val srv = rawServers.getJSONObject(s)
            val srvUrlRaw = srv.optString("url", srv.optString("streamUrl", ""))
            val srvDecrypted = if (srvUrlRaw.startsWith("enc:") || srvUrlRaw.startsWith("aes:") || srvUrlRaw.startsWith("sec:") || srvUrlRaw.startsWith("m7:")) {
              StreamSecurityManager.decryptStreamUrl(srvUrlRaw, secretKey)
            } else srvUrlRaw
            if (srvDecrypted.isNotBlank()) {
              serversList.add(
                MatchStreamServer(
                  id = srv.optString("id", "srv_$s"),
                  name = srv.optString("name", "سيرفر ${s + 1}"),
                  streamUrl = srvDecrypted,
                  quality = srv.optString("quality", "HD")
                )
              )
            }
          }
        }

        val directStreamRaw = item.optString("streamUrl", item.optString("stream_url", ""))
        val directStream = if (directStreamRaw.isNotBlank()) {
          if (directStreamRaw.startsWith("enc:") || directStreamRaw.startsWith("aes:") || directStreamRaw.startsWith("sec:") || directStreamRaw.startsWith("m7:")) {
            StreamSecurityManager.decryptStreamUrl(directStreamRaw, secretKey)
          } else directStreamRaw
        } else serversList.firstOrNull()?.streamUrl ?: ""

        val isStreamActive = item.optBoolean("is_stream_active", item.optInt("stream_active", 0) == 1) || serversList.isNotEmpty() || directStream.isNotBlank()

        val finalScoreHome = if (isLive || isEnded || homeScores != null) homeScores else null
        val finalScoreAway = if (isLive || isEnded || awayScores != null) awayScores else null

        // Goal scorers & minute details extraction
        var hGoals = item.optString("home_goals", item.optString("home_scorers", "")).trim()
        var aGoals = item.optString("away_goals", item.optString("away_scorers", "")).trim()

        if (hGoals.isBlank() && aGoals.isBlank()) {
          val eventsArr = item.optJSONArray("events") ?: item.optJSONArray("goals")
          if (eventsArr != null && eventsArr.length() > 0) {
            val hList = mutableListOf<String>()
            val aList = mutableListOf<String>()
            for (evIdx in 0 until eventsArr.length()) {
              val ev = eventsArr.optJSONObject(evIdx) ?: continue
              val evType = ev.optString("type", "").lowercase()
              if (evType.contains("goal") || evType.contains("score") || ev.has("minute")) {
                val min = ev.optString("minute", ev.optString("time", "")).replace("'", "")
                val player = ev.optString("player", ev.optString("player_name", "")).trim()
                val isHome = ev.optInt("team", 1) == 1 || ev.optString("team", "").contains("home", ignoreCase = true)
                val entry = if (player.isNotBlank()) "$player '$min" else "'$min"
                if (isHome) hList.add(entry) else aList.add(entry)
              }
            }
            if (hList.isNotEmpty()) hGoals = hList.joinToString(" • ")
            if (aList.isNotEmpty()) aGoals = aList.joinToString(" • ")
          }
        }

        // Real goal minutes parsed from YSScores score_time JSON array: e.g. [{"1":26},{"1":57},{"4":84}]
        if (hGoals.isBlank() && aGoals.isBlank() && !scoreTime.isNullOrBlank()) {
          try {
            val trimmedScoreTime = scoreTime.trim()
            if (trimmedScoreTime.startsWith("[")) {
              val scoreArr = JSONArray(trimmedScoreTime)
              val parsedMinuteEvents = mutableListOf<Pair<Int, String>>()
              for (sIdx in 0 until scoreArr.length()) {
                val goalObj = scoreArr.optJSONObject(sIdx) ?: continue
                val itKeys = goalObj.keys()
                while (itKeys.hasNext()) {
                  val k = itKeys.next()
                  val min = goalObj.optInt(k, 0)
                  if (min > 0) {
                    val suffix = when (k) {
                      "4" -> " (ر.ج)" // ضربة جزاء
                      "5" -> " (هـ.ذ)" // هدف ذاتي
                      else -> ""
                    }
                    parsedMinuteEvents.add(Pair(min, suffix))
                  }
                }
              }
              // Sort chronologically
              parsedMinuteEvents.sortBy { it.first }

              val hScore = finalScoreHome ?: 0
              val aScore = finalScoreAway ?: 0
              if (parsedMinuteEvents.isNotEmpty()) {
                if (hScore > 0 && aScore == 0) {
                  hGoals = parsedMinuteEvents.joinToString(" • ") { "'${it.first}${it.second}" }
                } else if (aScore > 0 && hScore == 0) {
                  aGoals = parsedMinuteEvents.joinToString(" • ") { "'${it.first}${it.second}" }
                } else if (hScore > 0 && aScore > 0) {
                  // Distribute chronologically to home and away based on scores
                  val homePortion = parsedMinuteEvents.take(hScore)
                  val awayPortion = parsedMinuteEvents.drop(hScore).take(aScore)
                  if (homePortion.isNotEmpty()) {
                    hGoals = homePortion.joinToString(" • ") { "'${it.first}${it.second}" }
                  }
                  if (awayPortion.isNotEmpty()) {
                    aGoals = awayPortion.joinToString(" • ") { "'${it.first}${it.second}" }
                  }
                }
              }
            }
          } catch (e: Exception) {
            Log.d("SportsBackendRepo", "Failed to parse score_time: ${e.message}")
          }
        }

        val match = SportsMatch(
          id = "ys_$matchId",
          title = "$homeTitle ضد $awayTitle",
          tournament = champTitle,
          tournamentLogo = champLogo,
          homeTeam = SportsTeam(name = homeTitle, logoUrl = homeLogo),
          awayTeam = SportsTeam(name = awayTitle, logoUrl = awayLogo),
          kickoffTime = matchTime,
          kickoffDate = matchDate,
          stadium = item.optString("stadium", ""),
          commentator = commentator,
          channelName = channelName,
          channelId = "bein_1",
          streamUrl = directStream,
          servers = serversList,
          isLive = isLive,
          isEnded = isEnded,
          isStreamActive = isStreamActive,
          liveMinute = liveMinute,
          scoreHome = finalScoreHome,
          scoreAway = finalScoreAway,
          homeGoalDetails = hGoals.ifBlank { null },
          awayGoalDetails = aGoals.ifBlank { null },
          bannerUrl = item.optString("bannerUrl", "")
        )
        list.add(match)
      } catch (e: Exception) {
        Log.e("SportsBackendRepo", "Single YSScores match parse error", e)
      }
    }
    return list
  }

  private fun parseStandardMatchesArray(array: JSONArray, secretKey: String): List<SportsMatch> {
    val list = mutableListOf<SportsMatch>()
    for (i in 0 until array.length()) {
      try {
        val item = array.getJSONObject(i)
        val rawStream = item.optString("streamUrl", item.optString("stream_url", item.optString("url", "")))
        val decryptedStream = if (rawStream.startsWith("enc:") || rawStream.startsWith("aes:") || rawStream.startsWith("sec:") || rawStream.startsWith("m7:") || rawStream.startsWith("m7enc:")) {
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

        // Parse custom servers if present
        val serversList = mutableListOf<MatchStreamServer>()
        val rawServers = item.optJSONArray("servers")
        if (rawServers != null) {
          for (s in 0 until rawServers.length()) {
            val srv = rawServers.getJSONObject(s)
            val srvUrl = srv.optString("url", srv.optString("streamUrl", ""))
            val dec = if (srvUrl.startsWith("enc:") || srvUrl.startsWith("aes:") || srvUrl.startsWith("sec:") || srvUrl.startsWith("m7:")) {
              StreamSecurityManager.decryptStreamUrl(srvUrl, secretKey)
            } else srvUrl
            if (dec.isNotBlank()) {
              serversList.add(
                MatchStreamServer(
                  id = srv.optString("id", "srv_$s"),
                  name = srv.optString("name", "سيرفر ${s + 1}"),
                  streamUrl = dec,
                  quality = srv.optString("quality", "HD")
                )
              )
            }
          }
        }

        val isStreamActive = item.optBoolean("is_stream_active", item.optInt("stream_active", 0) == 1) || decryptedStream.isNotBlank() || serversList.isNotEmpty()

        val hGoals = item.optString("homeGoalDetails", item.optString("home_goals", "")).ifBlank { null }
        val aGoals = item.optString("awayGoalDetails", item.optString("away_goals", "")).ifBlank { null }

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
          streamUrl = decryptedStream.ifBlank { serversList.firstOrNull()?.streamUrl ?: "" },
          servers = serversList,
          isLive = item.optBoolean("isLive", item.optBoolean("is_live", false)),
          isEnded = item.optBoolean("isEnded", item.optBoolean("is_ended", false)),
          isStreamActive = isStreamActive,
          liveMinute = liveMin,
          scoreHome = scoreH,
          scoreAway = scoreA,
          homeGoalDetails = hGoals,
          awayGoalDetails = aGoals,
          countdownText = countdown,
          bannerUrl = item.optString("bannerUrl", item.optString("poster", ""))
        )
        list.add(match)
      } catch (e: Exception) {
        Log.e("SportsBackendRepo", "JSON parse error safely handled", e)
      }
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
    val germanyPosterUrl = "android.resource://com.example/drawable/tod_germany_serbia_poster"
    val germanyMatch = SportsMatch(
      id = "germany_serbia_nations",
      title = "ألمانيا ضد صربيا",
      tournament = "دوري الأمم الأوروبية",
      tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png",
      homeTeam = SportsTeam(
        name = "ألمانيا",
        flagEmoji = "🇩🇪",
        code = "GER",
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/e/e3/DFB-Logo_2014.svg/512px-DFB-Logo_2014.svg.png"
      ),
      awayTeam = SportsTeam(
        name = "صربيا",
        flagEmoji = "🇷🇸",
        code = "SRB",
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/0/07/Football_Association_of_Serbia_logo.svg/512px-Football_Association_of_Serbia_logo.svg.png"
      ),
      kickoffTime = "21:45",
      kickoffDate = "1 أكتوبر 2026",
      stadium = "أليانز أرينا",
      commentator = "عصام الشوالي",
      channelName = "beIN SPORTS 2 HD",
      channelId = "bein_2",
      streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
      bannerUrl = germanyPosterUrl,
      isLive = true,
      liveMinute = "'72",
      scoreHome = 0,
      scoreAway = 0,
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

    val defaultList = mutableListOf<SportsMatch>()

    // 0. Primary Featured Hero Match: ألمانيا ضد صربيا (User Requested TOD Poster)
    defaultList.add(germanyMatch)

    // 1. Match from Screenshot 165536: مانشستر سيتي ضد ريال مدريد (22:00 - 1 أكتوبر 2026) - دوري أبطال أوروبا
    defaultList.add(
      SportsMatch(
        id = "mancity_real_ucl",
        title = "مانشستر سيتي ضد ريال مدريد",
        tournament = "دوري أبطال أوروبا",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        homeTeam = SportsTeam(name = "مانشستر سيتي", flagEmoji = "🔵", code = "MCI", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/e/eb/Manchester_City_FC_badge.svg/512px-Manchester_City_FC_badge.svg.png"),
        awayTeam = SportsTeam(name = "ريال مدريد", flagEmoji = "⚪", code = "RMA", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/5/56/Real_Madrid_CF.svg/512px-Real_Madrid_CF.svg.png"),
        kickoffTime = "22:00",
        kickoffDate = "1 أكتوبر 2026",
        stadium = "استاد الاتحاد",
        commentator = "حفيظ دراجي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        bannerUrl = "android.resource://com.example/drawable/tod_hero_match_banner",
        isLive = true,
        liveMinute = "'84",
        scoreHome = 3,
        scoreAway = 2
      )
    )

    // 2. Match from Screenshot 165527: آرسنال ضد تشيلسي - الدوري الإنجليزي الممتاز
    defaultList.add(
      SportsMatch(
        id = "arsenal_chelsea_pl",
        title = "آرسنال ضد تشيلسي",
        tournament = "الدوري الإنجليزي الممتاز",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Premier_League_Logo.svg/512px-Premier_League_Logo.svg.png",
        homeTeam = SportsTeam(name = "آرسنال", flagEmoji = "🔴", code = "ARS", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/5/53/Arsenal_FC.svg/512px-Arsenal_FC.svg.png"),
        awayTeam = SportsTeam(name = "تشيلسي", flagEmoji = "🔵", code = "CHE", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/c/cc/Chelsea_FC.svg/512px-Chelsea_FC.svg.png"),
        kickoffTime = "18:30",
        kickoffDate = "1 أكتوبر 2026",
        stadium = "استاد الإمارات",
        commentator = "خليل البلوشي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = true,
        liveMinute = "'58",
        scoreHome = 2,
        scoreAway = 2
      )
    )

    // 3. Match from Screenshot 165601: غينيا ضد كينيا (19:00 - 1 أكتوبر 2026) - تصفيات أمم أفريقيا
    defaultList.add(
      SportsMatch(
        id = "guinea_kenya_caf",
        title = "غينيا ضد كينيا",
        tournament = "تصفيات أمم أفريقيا",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/0/07/Confederation_of_African_Football_logo.svg/512px-Confederation_of_African_Football_logo.svg.png",
        homeTeam = SportsTeam(name = "غينيا", flagEmoji = "🇬🇳", code = "GUI", logoUrl = "https://flagcdn.com/w80/gn.png"),
        awayTeam = SportsTeam(name = "كينيا", flagEmoji = "🇰🇪", code = "KEN", logoUrl = "https://flagcdn.com/w80/ke.png"),
        kickoffTime = "19:00",
        kickoffDate = "1 أكتوبر 2026",
        stadium = "استاد لانسانا كونتي",
        commentator = "محمد علي",
        channelName = "beIN SPORTS 3 HD",
        channelId = "bein_3",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = true,
        liveMinute = "'63",
        scoreHome = 1,
        scoreAway = 0
      )
    )

    // 4. Featured Hero Match from TOD Screenshot: روما ضد برشلونة (Screenshot 161604)
    defaultList.add(
      SportsMatch(
        id = "roma_barca_uwcl",
        title = "روما ضد برشلونة",
        tournament = "دوري أبطال أوروبا",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        homeTeam = SportsTeam(name = "روما", flagEmoji = "🟡🔴", code = "ROM", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f7/AS_Roma_logo_%282017%29.svg/512px-AS_Roma_logo_%282017%29.svg.png"),
        awayTeam = SportsTeam(name = "برشلونة", flagEmoji = "🔵🔴", code = "BAR", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/512px-FC_Barcelona_%28crest%29.svg.png"),
        kickoffTime = "19:45",
        kickoffDate = "30 سبتمبر 2026",
        stadium = "Stadio Tre Fontane",
        commentator = "عصام الشوالي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        bannerUrl = "android.resource://com.example/drawable/tod_hero_match_banner",
        isLive = false,
        scoreHome = null,
        scoreAway = null,
        stats = MatchStats(possessionHome = 65, possessionAway = 35, shotsOnTargetHome = 9, shotsOnTargetAway = 2, totalShotsHome = 18, totalShotsAway = 5, cornersHome = 8, cornersAway = 2, foulsHome = 6, foulsAway = 10, yellowCardsHome = 1, yellowCardsAway = 2)
      )
    )

    // 5. Featured Hero Match: ويلز ضد النرويج
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
        bannerUrl = "android.resource://com.example/drawable/tod_hero_match_banner",
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

    // 6. Featured Match 3 (Screenshot 10, 18): مانشستر يونايتد ضد صباح (4 - 0)
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

    // 13. Saudi Pro League: الهلال ضد النصر (Live)
    defaultList.add(
      SportsMatch(
        id = "hilal_nassr_spl",
        title = "الهلال ضد النصر",
        tournament = "دوري روشن السعودي",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Saudi_Pro_League_logo.svg/512px-Saudi_Pro_League_logo.svg.png",
        homeTeam = SportsTeam(name = "الهلال", flagEmoji = "🔵", code = "HIL", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/8/87/Al_Hilal_SFC_logo.svg/512px-Al_Hilal_SFC_logo.svg.png"),
        awayTeam = SportsTeam(name = "النصر", flagEmoji = "🟡", code = "NAS", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/c/c5/Al-Nassr_FC_logo.svg/512px-Al-Nassr_FC_logo.svg.png"),
        kickoffTime = "21:00",
        kickoffDate = "اليوم",
        stadium = "المملكة أرينا",
        commentator = "فهد العتيبي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = true,
        liveMinute = "'82",
        scoreHome = 2,
        scoreAway = 1
      )
    )

    // 14. Saudi Pro League: الاتحاد ضد الأهلي (Upcoming)
    defaultList.add(
      SportsMatch(
        id = "ittihad_ahli_spl",
        title = "الاتحاد ضد الأهلي",
        tournament = "دوري روشن السعودي",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Saudi_Pro_League_logo.svg/512px-Saudi_Pro_League_logo.svg.png",
        homeTeam = SportsTeam(name = "الاتحاد", flagEmoji = "🟡⚫", code = "ITT", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/5/53/Al-Ittihad_Club_logo.svg/512px-Al-Ittihad_Club_logo.svg.png"),
        awayTeam = SportsTeam(name = "الأهلي", flagEmoji = "🟢", code = "AHL", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/e/e0/Al-Ahli_Saudi_FC_logo.svg/512px-Al-Ahli_Saudi_FC_logo.svg.png"),
        kickoffTime = "21:00",
        kickoffDate = "غداً",
        stadium = "مدينة الملك عبدالله الرياضية",
        commentator = "فارس عوض",
        channelName = "beIN SPORTS 2 HD",
        channelId = "bein_2",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = false
      )
    )

    // 15. AFC Champions League: العين ضد الهلال (Live)
    defaultList.add(
      SportsMatch(
        id = "ain_hilal_afc",
        title = "العين ضد الهلال",
        tournament = "دوري أبطال آسيا",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/en/thumb/d/d4/AFC_Champions_League_Elite_logo.svg/512px-AFC_Champions_League_Elite_logo.svg.png",
        homeTeam = SportsTeam(name = "العين", flagEmoji = "🟣", code = "AIN", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/3/3f/Al_Ain_FC_logo.svg/512px-Al_Ain_FC_logo.svg.png"),
        awayTeam = SportsTeam(name = "الهلال", flagEmoji = "🔵", code = "HIL", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/8/87/Al_Hilal_SFC_logo.svg/512px-Al_Hilal_SFC_logo.svg.png"),
        kickoffTime = "19:00",
        kickoffDate = "اليوم",
        stadium = "استاد هزاع بن زايد",
        commentator = "عامر عبد الله",
        channelName = "beIN SPORTS AFC HD",
        channelId = "bein_afc",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = true,
        liveMinute = "'79",
        scoreHome = 2,
        scoreAway = 3
      )
    )

    // 16. Serie A: يوفنتوس ضد روما (Live)
    defaultList.add(
      SportsMatch(
        id = "juve_roma_seriea",
        title = "يوفنتوس ضد روما",
        tournament = "الدوري الإيطالي",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Serie_A_logo_2019.svg/512px-Serie_A_logo_2019.svg.png",
        homeTeam = SportsTeam(name = "يوفنتوس", flagEmoji = "⚪⚫", code = "JUV", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Juventus_FC_2017_logo.svg/512px-Juventus_FC_2017_logo.svg.png"),
        awayTeam = SportsTeam(name = "روما", flagEmoji = "🟡🔴", code = "ROM", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f7/AS_Roma_logo_%282017%29.svg/512px-AS_Roma_logo_%282017%29.svg.png"),
        kickoffTime = "21:45",
        kickoffDate = "اليوم",
        stadium = "أليانز ستاديوم - تورينو",
        commentator = "علي محمد علي",
        channelName = "beIN SPORTS 2 HD",
        channelId = "bein_2",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = true,
        liveMinute = "'74",
        scoreHome = 1,
        scoreAway = 1
      )
    )

    // 17. Serie A: إنتر ميلان ضد ميلان (Upcoming)
    defaultList.add(
      SportsMatch(
        id = "inter_milan_seriea",
        title = "إنتر ميلان ضد ميلان",
        tournament = "الدوري الإيطالي",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Serie_A_logo_2019.svg/512px-Serie_A_logo_2019.svg.png",
        homeTeam = SportsTeam(name = "إنتر ميلان", flagEmoji = "🔵⚫", code = "INT", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/FC_Internazionale_Milano_2021.svg/512px-FC_Internazionale_Milano_2021.svg.png"),
        awayTeam = SportsTeam(name = "ميلان", flagEmoji = "🔴⚫", code = "MIL", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Logo_of_AC_Milan.svg/512px-Logo_of_AC_Milan.svg.png"),
        kickoffTime = "21:45",
        kickoffDate = "الأحد 5 أكتوبر 2026",
        stadium = "سان سيرو",
        commentator = "حفيظ دراجي",
        channelName = "beIN SPORTS 2 HD",
        channelId = "bein_2",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = false
      )
    )

    // 18. La Liga: ريال مدريد ضد برشلونة (Upcoming الكلاسيكو)
    defaultList.add(
      SportsMatch(
        id = "clasico_real_barca",
        title = "ريال مدريد ضد برشلونة",
        tournament = "الدوري الإسباني",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/LaLiga_logo_2023.svg/512px-LaLiga_logo_2023.svg.png",
        homeTeam = SportsTeam(name = "ريال مدريد", flagEmoji = "⚪", code = "RMA", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/5/56/Real_Madrid_CF.svg/512px-Real_Madrid_CF.svg.png"),
        awayTeam = SportsTeam(name = "برشلونة", flagEmoji = "🔵🔴", code = "BAR", logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/512px-FC_Barcelona_%28crest%29.svg.png"),
        kickoffTime = "22:00",
        kickoffDate = "السبت 4 أكتوبر 2026",
        stadium = "سانتياغو برنابيو",
        commentator = "عصام الشوالي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = false
      )
    )

    // 19. Friendlies: البرتغال ضد الدنمارك (Ended)
    defaultList.add(
      SportsMatch(
        id = "portugal_denmark_friendly",
        title = "البرتغال ضد الدنمارك",
        tournament = "مباريات دولية ودية",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/FIFA_logo_without_slogan.svg/512px-FIFA_logo_without_slogan.svg.png",
        homeTeam = SportsTeam(name = "البرتغال", flagEmoji = "🇵🇹", code = "POR", logoUrl = "https://flagcdn.com/w80/pt.png"),
        awayTeam = SportsTeam(name = "الدنمارك", flagEmoji = "🇩🇰", code = "DEN", logoUrl = "https://flagcdn.com/w80/dk.png"),
        kickoffTime = "21:45",
        kickoffDate = "أمس",
        stadium = "استاد خوسيه ألفالادي",
        commentator = "خليل البلوشي",
        channelName = "beIN SPORTS 1 HD",
        channelId = "bein_1",
        streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        isLive = false,
        isEnded = true,
        scoreHome = 3,
        scoreAway = 2
      )
    )

    // 20. Tennis: كارلوس ألكاراز ضد يانيك سينر (Upcoming)
    defaultList.add(
      SportsMatch(
        id = "tennis_alcaraz_sinner",
        title = "كارلوس ألكاراز ضد يانيك سينر",
        tournament = "بث مباشر - رياضات متنوعة",
        tournamentLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/ATP_Tour_logo.svg/512px-ATP_Tour_logo.svg.png",
        homeTeam = SportsTeam(name = "كارلوس ألكاراز", flagEmoji = "🇪🇸", code = "ESP", logoUrl = "https://flagcdn.com/w80/es.png"),
        awayTeam = SportsTeam(name = "يانيك سينر", flagEmoji = "🇮🇹", code = "ITA", logoUrl = "https://flagcdn.com/w80/it.png"),
        kickoffTime = "16:30",
        kickoffDate = "غداً",
        stadium = "الملعب الرئيسي - بكين",
        commentator = "أحمد عبده",
        channelName = "beIN SPORTS 6 HD",
        channelId = "bein_6",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isLive = false
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

    // Initialize Direct Sports & Entertainment Channels (Matching Screenshots 1:1)
    _sportsChannels.value = listOf(
      // 1. قنوات الرياضة (beIN SPORTS & Major Networks)
      XtreamChannel("bein_1", "beIN SPORTS 1 HD", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_2", "beIN SPORTS 2 HD", "", "قنوات الرياضة", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
      XtreamChannel("bein_3", "beIN SPORTS 3 HD", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_4", "beIN SPORTS 4 HD", "", "قنوات الرياضة", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
      XtreamChannel("bein_5", "beIN SPORTS 5 HD", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_6", "beIN SPORTS 6 HD", "", "قنوات الرياضة", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
      XtreamChannel("bein_news", "beIN SPORTS الإخبارية", "", "قنوات الرياضة", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"),
      XtreamChannel("bein_xtra", "beIN SPORTS XTRA 1", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_afc", "beIN SPORTS AFC", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("premier_league", "Premier League TV", "", "قنوات الرياضة", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"),
      XtreamChannel("lfctv", "LFCTV", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("alkass_1", "قناة الكأس 1 HD", "", "قنوات الرياضة", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
      XtreamChannel("alkass_extra", "قناة الكأس EXTRA HD", "", "قنوات الرياضة", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
      XtreamChannel("ssc_1", "SSC 1 HD", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("ad_sports", "أبوظبي الرياضية 1 HD", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("ontime_1", "ON Time Sports HD", "", "قنوات الرياضة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),

      // 2. قنوات الجزيرة
      XtreamChannel("aljazeera_main", "الجزيرة", "", "قنوات الجزيرة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("aljazeera_doc", "الجزيرة الوثائقية", "", "قنوات الجزيرة", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
      XtreamChannel("aljazeera_live", "الجزيرة مباشر", "", "قنوات الجزيرة", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),

      // 3. قنوات المسلسلات
      XtreamChannel("bein_series_1", "beIN SERIES 1", "", "قنوات المسلسلات", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_series_2", "beIN SERIES 2", "", "قنوات المسلسلات", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),

      // 4. قنوات الأفلام
      XtreamChannel("bein_movies_1", "beIN MOVIES 1", "", "قنوات الأفلام", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
      XtreamChannel("bein_movies_2", "beIN MOVIES 2", "", "قنوات الأفلام", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8")
    )
  }
}
