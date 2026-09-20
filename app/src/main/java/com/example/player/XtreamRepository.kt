package com.example.player

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.model.XtreamAccountInfo
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.model.XtreamPlaylistConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class XtreamRepository(context: Context) {
  private val prefs = context.getSharedPreferences("xtream_prefs", Context.MODE_PRIVATE)

  companion object {
    // In-memory cache for ultra-fast instant 0ms category & channel access
    private val categoryCache = mutableMapOf<String, List<XtreamCategory>>()
    private val streamCache = mutableMapOf<String, List<XtreamChannel>>()
  }

  fun clearMemoryCache() {
    categoryCache.clear()
    streamCache.clear()
  }

  suspend fun pingServer(serverUrl: String): Long = withContext(Dispatchers.IO) {
    try {
      val clean = cleanServerUrl(serverUrl)
      val startTime = System.currentTimeMillis()
      val req = Request.Builder()
        .url("$clean/player_api.php")
        .head()
        .header("User-Agent", "IPTVSmartersPro/3.1.5 (Linux; Android 12)")
        .build()
      val response = client.newCall(req).execute()
      response.close()
      val duration = System.currentTimeMillis() - startTime
      if (duration > 0) duration else 15L
    } catch (e: Exception) {
      -1L
    }
  }

  // Tolerant OkHttpClient with universal SSL trust and redirect handling for IPTV servers
  private val client: OkHttpClient by lazy {
    try {
      val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
      })

      val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, SecureRandom())
      }

      OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
    } catch (e: Exception) {
      OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
    }
  }

  // Sanitizes server URL from any extra path like /c/, /player_api.php, /get.php, etc.
  fun cleanServerUrl(input: String): String {
    var s = input.trim()
    if (!s.startsWith("http://", ignoreCase = true) && !s.startsWith("https://", ignoreCase = true)) {
      s = "http://$s"
    }
    // Remove trailing slashes and common web-player suffixes
    s = s.trimEnd('/')
    val suffixesToRemove = listOf(
      "/player_api.php",
      "/get.php",
      "/xmltv.php",
      "/c",
      "/live",
      "/en",
      "/panel",
      "/stalker_portal"
    )
    for (suffix in suffixesToRemove) {
      if (s.endsWith(suffix, ignoreCase = true)) {
        s = s.substring(0, s.length - suffix.length).trimEnd('/')
      }
    }
    return s
  }

  fun getSavedCredentials(): Triple<String, String, String>? {
    val server = prefs.getString("server_url", null) ?: return null
    val user = prefs.getString("username", null) ?: return null
    val pass = prefs.getString("password", null) ?: return null
    return Triple(cleanServerUrl(server), user, pass)
  }

  fun saveCredentials(server: String, user: String, pass: String) {
    prefs.edit()
      .putString("server_url", cleanServerUrl(server))
      .putString("username", user.trim())
      .putString("password", pass.trim())
      .apply()
  }

  fun clearCredentials() {
    prefs.edit().clear().apply()
  }

  suspend fun login(serverUrl: String, username: String, password: String): Result<XtreamAccountInfo> =
    withContext(Dispatchers.IO) {
      try {
        val cleanServer = cleanServerUrl(serverUrl)
        val cleanUser = username.trim()
        val cleanPass = password.trim()

        if (cleanUser.isEmpty() || cleanPass.isEmpty()) {
          return@withContext Result.failure(Exception("يرجى إدخال اسم المستخدم وكلمة المرور"))
        }

        val url = "$cleanServer/player_api.php?username=$cleanUser&password=$cleanPass"
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", "IPTVSmartersPro/3.1.5 (Linux; Android 12)")
          .header("Accept", "*/*")
          .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
          return@withContext Result.failure(Exception("فشل الاتصال: السيرفر أعاد رمز (${response.code})"))
        }

        val body = response.body?.string() ?: return@withContext Result.failure(Exception("استجابة السيرفر فارغة"))
        val json = JSONObject(body)

        val userInfo = json.optJSONObject("user_info")
        val auth = userInfo?.optInt("auth", 0) ?: 0
        val status = userInfo?.optString("status", "Unknown") ?: "Unknown"

        // Some servers return auth as string or don't set auth == 1 but status is Active
        val isAuthOk = auth == 1 ||
            userInfo?.optString("auth", "") == "1" ||
            status.equals("Active", ignoreCase = true) ||
            json.has("server_info")

        if (isAuthOk) {
          saveCredentials(cleanServer, cleanUser, cleanPass)
          val expDate = userInfo?.optString("exp_date", null)
          Result.success(
            XtreamAccountInfo(
              username = cleanUser,
              status = if (status.equals("Active", ignoreCase = true)) "Active" else "نشط",
              expDate = expDate,
              serverUrl = cleanServer
            )
          )
        } else {
          val msg = json.optString("message", "فشل التحقق: اسم المستخدم أو كلمة المرور غير صحيحة")
          Result.failure(Exception(msg))
        }
      } catch (e: Exception) {
        Log.e("XtreamRepository", "Login error", e)
        Result.failure(Exception("تعذر الاتصال بالسيرفر: ${e.localizedMessage ?: "تأكد من عنوان السيرفر والإنترنت"}"))
      }
    }

  suspend fun fetchCategories(
    serverUrl: String,
    username: String,
    password: String,
    forceRefresh: Boolean = false
  ): Result<List<XtreamCategory>> =
    withContext(Dispatchers.IO) {
      try {
        val cleanServer = cleanServerUrl(serverUrl)
        val cleanUser = username.trim()
        val cleanPass = password.trim()
        val cacheKey = "$cleanServer|$cleanUser"

        if (!forceRefresh && categoryCache.containsKey(cacheKey)) {
          val cached = categoryCache[cacheKey]
          if (!cached.isNullOrEmpty()) {
            return@withContext Result.success(cached)
          }
        }

        val url = "$cleanServer/player_api.php?username=$cleanUser&password=$cleanPass&action=get_live_categories"
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", "IPTVSmartersPro/3.1.5 (Linux; Android 12)")
          .header("Accept", "*/*")
          .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()?.trim() ?: return@withContext Result.failure(Exception("فارغ"))

        val list = mutableListOf<XtreamCategory>()

        if (body.startsWith("[")) {
          // Parse JSONArray
          val array = JSONArray(body)
          for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val id = if (obj.has("category_id")) {
              obj.optString("category_id", "").ifEmpty { obj.optInt("category_id", 0).toString() }
            } else {
              obj.optString("id", "")
            }
            val name = obj.optString("category_name", "").ifEmpty { obj.optString("name", "باقة بدون اسم") }
            if (id.isNotEmpty() && id != "0") {
              list.add(XtreamCategory(id, name))
            }
          }
        } else if (body.startsWith("{")) {
          // Parse JSONObject (map format)
          val jsonObj = JSONObject(body)
          val keys = jsonObj.keys()
          while (keys.hasNext()) {
            val key = keys.next()
            val child = jsonObj.optJSONObject(key)
            if (child != null) {
              val id = child.optString("category_id", key).ifEmpty { key }
              val name = child.optString("category_name", child.optString("name", "باقة $key"))
              list.add(XtreamCategory(id, name))
            }
          }
        }

        if (list.isNotEmpty()) {
          categoryCache[cacheKey] = list
        }
        Result.success(list)
      } catch (e: Exception) {
        Log.e("XtreamRepository", "Fetch categories error", e)
        Result.failure(e)
      }
    }

  suspend fun fetchStreams(
    serverUrl: String,
    username: String,
    password: String,
    categoryId: String? = null,
    preferredFormat: String = "MPEG-TS (.ts)",
    forceRefresh: Boolean = false
  ): Result<List<XtreamChannel>> = withContext(Dispatchers.IO) {
    try {
      val cleanServer = cleanServerUrl(serverUrl)
      val cleanUser = username.trim()
      val cleanPass = password.trim()
      val cacheKey = "$cleanServer|$cleanUser|${categoryId ?: "ALL"}"

      if (!forceRefresh && streamCache.containsKey(cacheKey)) {
        val cached = streamCache[cacheKey]
        if (!cached.isNullOrEmpty()) {
          return@withContext Result.success(cached)
        }
      }

      val isFilterRequested = !categoryId.isNullOrEmpty() && categoryId != "ALL"

      val url = buildString {
        append("$cleanServer/player_api.php?username=$cleanUser&password=$cleanPass&action=get_live_streams")
        if (isFilterRequested) {
          append("&category_id=$categoryId")
        }
      }

      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "IPTVSmartersPro/3.1.5 (Linux; Android 12)")
        .header("Accept", "*/*")
        .build()

      val response = client.newCall(request).execute()
      val body = response.body?.string()?.trim() ?: return@withContext Result.failure(Exception("قائمة القنوات فارغة"))

      var list = parseStreamsJson(body, cleanServer, cleanUser, cleanPass, preferredFormat)

      // Fallback: If filtered fetch returned empty or failed because the server doesn't support &category_id=,
      // fetch all streams and filter in memory
      if (list.isEmpty() && isFilterRequested) {
        val allUrl = "$cleanServer/player_api.php?username=$cleanUser&password=$cleanPass&action=get_live_streams"
        val allReq = Request.Builder()
          .url(allUrl)
          .header("User-Agent", "IPTVSmartersPro/3.1.5 (Linux; Android 12)")
          .header("Accept", "*/*")
          .build()
        val allRes = client.newCall(allReq).execute()
        val allBody = allRes.body?.string()?.trim()
        if (!allBody.isNullOrEmpty()) {
          val allChannels = parseStreamsJson(allBody, cleanServer, cleanUser, cleanPass, preferredFormat)
          list = allChannels.filter { it.categoryId == categoryId }
        }
      }

      if (list.isNotEmpty()) {
        streamCache[cacheKey] = list
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e("XtreamRepository", "Fetch streams error", e)
      Result.failure(e)
    }
  }

  private fun parseStreamsJson(
    body: String,
    cleanServer: String,
    cleanUser: String,
    cleanPass: String,
    preferredFormat: String
  ): List<XtreamChannel> {
    val list = mutableListOf<XtreamChannel>()

    if (body.startsWith("[")) {
      val array = JSONArray(body)
      for (i in 0 until array.length()) {
        val obj = array.optJSONObject(i) ?: continue
        val channel = parseSingleChannel(obj, cleanServer, cleanUser, cleanPass, preferredFormat)
        if (channel != null) {
          list.add(channel)
        }
      }
    } else if (body.startsWith("{")) {
      val jsonObj = JSONObject(body)
      val keys = jsonObj.keys()
      while (keys.hasNext()) {
        val key = keys.next()
        val obj = jsonObj.optJSONObject(key)
        if (obj != null) {
          val channel = parseSingleChannel(obj, cleanServer, cleanUser, cleanPass, preferredFormat)
          if (channel != null) {
            list.add(channel)
          }
        }
      }
    }
    return list
  }

  private fun parseSingleChannel(
    obj: JSONObject,
    cleanServer: String,
    cleanUser: String,
    cleanPass: String,
    preferredFormat: String
  ): XtreamChannel? {
    val streamId = when {
      obj.has("stream_id") -> obj.optString("stream_id", "").ifEmpty { obj.optInt("stream_id", 0).toString() }
      obj.has("id") -> obj.optString("id", "")
      obj.has("num") -> obj.optString("num", "")
      else -> ""
    }

    if (streamId.isEmpty() || streamId == "0") return null

    val name = obj.optString("name", "").ifEmpty {
      obj.optString("title", "").ifEmpty {
        obj.optString("stream_name", "قناة $streamId")
      }
    }

    val icon = obj.optString("stream_icon", null)?.takeIf { it.isNotBlank() }
      ?: obj.optString("icon", null)?.takeIf { it.isNotBlank() }
      ?: obj.optString("logo", null)?.takeIf { it.isNotBlank() }

    val catId = when {
      obj.has("category_id") -> obj.optString("category_id", "").ifEmpty { obj.optInt("category_id", 0).toString() }
      else -> null
    }

    val ext = when {
      preferredFormat.contains("m3u8", ignoreCase = true) || preferredFormat.contains("hls", ignoreCase = true) -> "m3u8"
      preferredFormat.contains("ts", ignoreCase = true) -> "ts"
      else -> {
        val containerExtension = obj.optString("container_extension", "ts")
        if (containerExtension.isNotBlank()) containerExtension else "ts"
      }
    }

    val playUrl = "$cleanServer/live/$cleanUser/$cleanPass/$streamId.$ext"

    return XtreamChannel(
      streamId = streamId,
      name = name,
      iconUrl = icon,
      categoryId = catId,
      playUrl = playUrl
    )
  }

  suspend fun parseM3uPlaylist(urlOrContent: String): Result<List<XtreamChannel>> =
    withContext(Dispatchers.IO) {
      try {
        val content = if (urlOrContent.startsWith("http://") || urlOrContent.startsWith("https://")) {
          val req = Request.Builder()
            .url(urlOrContent)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
            .build()
          client.newCall(req).execute().body?.string() ?: ""
        } else {
          urlOrContent
        }

        val lines = content.lines()
        val channels = mutableListOf<XtreamChannel>()
        var currentName: String? = null
        var currentLogo: String? = null
        var currentGroup: String? = null

        for (line in lines) {
          val trimmed = line.trim()
          if (trimmed.startsWith("#EXTINF:")) {
            currentName = trimmed.substringAfterLast(",").trim()
            currentLogo = if (trimmed.contains("tvg-logo=\"")) {
              trimmed.substringAfter("tvg-logo=\"").substringBefore("\"")
            } else null
            currentGroup = if (trimmed.contains("group-title=\"")) {
              trimmed.substringAfter("group-title=\"").substringBefore("\"")
            } else null
          } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
            val playUrl = trimmed
            val name = currentName ?: "Channel ${channels.size + 1}"
            channels.add(
              XtreamChannel(
                streamId = (channels.size + 1).toString(),
                name = name,
                iconUrl = currentLogo,
                categoryId = currentGroup,
                playUrl = playUrl
              )
            )
            currentName = null
            currentLogo = null
            currentGroup = null
          }
        }
        Result.success(channels)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  // Multiple Playlists persistence
  fun getAllPlaylists(): List<XtreamPlaylistConfig> {
    val jsonString = prefs.getString("saved_playlists_json", null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<XtreamPlaylistConfig>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          XtreamPlaylistConfig(
            playlistName = obj.optString("playlistName", "سيرفر"),
            username = obj.optString("username", ""),
            password = obj.optString("password", ""),
            serverUrl = obj.optString("serverUrl", ""),
            isM3u = obj.optBoolean("isM3u", false),
            m3uUrl = obj.optString("m3uUrl", ""),
            useDefaultUserAgent = obj.optBoolean("useDefaultUserAgent", true),
            customUserAgent = obj.optString("customUserAgent", "IPTVSmartersPro/3.1.5 (Linux; Android 12)"),
            isEnabled = obj.optBoolean("isEnabled", true),
            updateInterval = obj.optString("updateInterval", "كل يوم"),
            enableChannels = obj.optBoolean("enableChannels", true),
            enableMovies = obj.optBoolean("enableMovies", true),
            enableSeries = obj.optBoolean("enableSeries", true),
            streamFormat = obj.optString("streamFormat", "MPEG-TS (.ts)"),
            archivePeriod = obj.optString("archivePeriod", "تلقائي")
          )
        )
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun savePlaylistConfig(config: XtreamPlaylistConfig) {
    val current = getAllPlaylists().filter {
      if (config.isM3u) it.m3uUrl != config.m3uUrl
      else (it.serverUrl != config.serverUrl || it.username != config.username)
    }.toMutableList()
    current.add(0, config)

    val array = JSONArray()
    for (p in current) {
      val obj = JSONObject()
      obj.put("playlistName", p.playlistName)
      obj.put("username", p.username)
      obj.put("password", p.password)
      obj.put("serverUrl", p.serverUrl)
      obj.put("isM3u", p.isM3u)
      obj.put("m3uUrl", p.m3uUrl)
      obj.put("useDefaultUserAgent", p.useDefaultUserAgent)
      obj.put("customUserAgent", p.customUserAgent)
      obj.put("isEnabled", p.isEnabled)
      obj.put("updateInterval", p.updateInterval)
      obj.put("enableChannels", p.enableChannels)
      obj.put("enableMovies", p.enableMovies)
      obj.put("enableSeries", p.enableSeries)
      obj.put("streamFormat", p.streamFormat)
      obj.put("archivePeriod", p.archivePeriod)
      array.put(obj)
    }
    prefs.edit()
      .putString("saved_playlists_json", array.toString())
      .putString("active_playlist_key", if (config.isM3u) config.m3uUrl else "${config.serverUrl}_${config.username}")
      .apply()

    if (!config.isM3u) {
      saveCredentials(config.serverUrl, config.username, config.password)
    }
  }

  fun getActivePlaylistConfig(): XtreamPlaylistConfig? {
    val all = getAllPlaylists()
    if (all.isEmpty()) {
      // Check legacy single credentials
      val legacy = getSavedCredentials()
      if (legacy != null && legacy.first.isNotBlank() && legacy.second.isNotBlank()) {
        return XtreamPlaylistConfig(
          playlistName = legacy.second,
          username = legacy.second,
          password = legacy.third,
          serverUrl = legacy.first
        )
      }
      return null
    }
    val activeKey = prefs.getString("active_playlist_key", null)
    return all.find {
      val key = if (it.isM3u) it.m3uUrl else "${it.serverUrl}_${it.username}"
      key == activeKey
    } ?: all.first()
  }

  fun deletePlaylistConfig(config: XtreamPlaylistConfig) {
    val current = getAllPlaylists().filter {
      if (config.isM3u) it.m3uUrl != config.m3uUrl
      else (it.serverUrl != config.serverUrl || it.username != config.username)
    }
    val array = JSONArray()
    for (p in current) {
      val obj = JSONObject()
      obj.put("playlistName", p.playlistName)
      obj.put("username", p.username)
      obj.put("password", p.password)
      obj.put("serverUrl", p.serverUrl)
      obj.put("isM3u", p.isM3u)
      obj.put("m3uUrl", p.m3uUrl)
      obj.put("useDefaultUserAgent", p.useDefaultUserAgent)
      obj.put("customUserAgent", p.customUserAgent)
      obj.put("isEnabled", p.isEnabled)
      obj.put("updateInterval", p.updateInterval)
      obj.put("enableChannels", p.enableChannels)
      obj.put("enableMovies", p.enableMovies)
      obj.put("enableSeries", p.enableSeries)
      obj.put("streamFormat", p.streamFormat)
      obj.put("archivePeriod", p.archivePeriod)
      array.put(obj)
    }
    prefs.edit().putString("saved_playlists_json", array.toString()).apply()
  }
  fun getFavorites(): Set<String> {
    return prefs.getStringSet("favorite_channels", emptySet()) ?: emptySet()
  }

  fun toggleFavorite(channelId: String): Boolean {
    val current = getFavorites().toMutableSet()
    val isNowFav = if (current.contains(channelId)) {
      current.remove(channelId)
      false
    } else {
      current.add(channelId)
      true
    }
    prefs.edit().putStringSet("favorite_channels", current).apply()
    return isNowFav
  }

  // Recents management
  fun getRecentChannels(): List<XtreamChannel> {
    val jsonString = prefs.getString("recent_channels_json", null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<XtreamChannel>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          XtreamChannel(
            streamId = obj.optString("streamId"),
            name = obj.optString("name"),
            iconUrl = obj.optString("iconUrl").takeIf { it.isNotEmpty() },
            categoryId = obj.optString("categoryId").takeIf { it.isNotEmpty() },
            playUrl = obj.optString("playUrl")
          )
        )
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun addRecentChannel(channel: XtreamChannel) {
    try {
      val current = getRecentChannels().filter { it.streamId != channel.streamId && it.playUrl != channel.playUrl }.toMutableList()
      current.add(0, channel)
      val trimmed = current.take(20)
      val array = JSONArray()
      for (c in trimmed) {
        val obj = JSONObject()
        obj.put("streamId", c.streamId)
        obj.put("name", c.name)
        obj.put("iconUrl", c.iconUrl ?: "")
        obj.put("categoryId", c.categoryId ?: "")
        obj.put("playUrl", c.playUrl)
        array.put(obj)
      }
      prefs.edit().putString("recent_channels_json", array.toString()).apply()
    } catch (ignored: Exception) {}
  }

  // Custom Stream URL History
  fun getCustomUrlHistory(): List<Pair<String, String>> {
    val jsonString = prefs.getString("custom_url_history", null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<Pair<String, String>>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val title = obj.optString("title", "بث مباشر")
        val url = obj.optString("url", "")
        if (url.isNotEmpty()) {
          list.add(Pair(title, url))
        }
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun addCustomUrlToHistory(title: String, url: String) {
    try {
      val current = getCustomUrlHistory().filter { it.second != url }.toMutableList()
      current.add(0, Pair(title, url))
      val trimmed = current.take(15)
      val array = JSONArray()
      for ((t, u) in trimmed) {
        val obj = JSONObject()
        obj.put("title", t)
        obj.put("url", u)
        array.put(obj)
      }
      prefs.edit().putString("custom_url_history", array.toString()).apply()
    } catch (ignored: Exception) {}
  }

  fun removeCustomUrlFromHistory(url: String) {
    val current = getCustomUrlHistory().filter { it.second != url }
    val array = JSONArray()
    for ((t, u) in current) {
      val obj = JSONObject()
      obj.put("title", t)
      obj.put("url", u)
      array.put(obj)
    }
    prefs.edit().putString("custom_url_history", array.toString()).apply()
  }
}
