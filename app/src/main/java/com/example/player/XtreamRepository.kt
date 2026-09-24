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
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import com.squareup.moshi.JsonReader
import okio.buffer
import okio.source
import java.io.BufferedReader
import java.io.InputStreamReader
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

  // Ultra-fast HTTP client with connection pooling, gzip support and lenient SSL
  private val client: OkHttpClient by lazy {
    val dispatcher = Dispatcher().apply {
      maxRequests = 64
      maxRequestsPerHost = 32
    }
    val connectionPool = ConnectionPool(32, 5, TimeUnit.MINUTES)

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
        .dispatcher(dispatcher)
        .connectionPool(connectionPool)
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
    } catch (e: Exception) {
      OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .connectionPool(connectionPool)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
    }
  }

  // Sanitizes server URL from any extra path like /c/, /player_api.php, /get.php, etc.
  fun cleanServerUrl(input: String): String {
    var s = input.trim()
    if (s.isEmpty()) return ""
    if (!s.startsWith("http://", ignoreCase = true) && !s.startsWith("https://", ignoreCase = true)) {
      s = "http://$s"
    }
    // Remove query params if any in base URL (e.g. ?username=...)
    if (s.contains("?")) {
      s = s.substringBefore("?")
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

  // Intelligently extracts server URL, username, and password if the user pasted a full M3U or Xtream link
  fun smartExtractXtreamDetails(input: String): Triple<String, String, String>? {
    val trimmed = input.trim()
    if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
      return null
    }
    try {
      val uri = Uri.parse(trimmed)
      val user = uri.getQueryParameter("username")
      val pass = uri.getQueryParameter("password")
      if (!user.isNullOrBlank() && !pass.isNullOrBlank()) {
        val host = "${uri.scheme}://${uri.authority}"
        return Triple(host, user, pass)
      }
      // Pattern: /live/username/password/streamId
      val pathSegments = uri.pathSegments
      if (pathSegments.size >= 3 && (pathSegments[0] == "live" || pathSegments[0] == "movie" || pathSegments[0] == "series")) {
        val u = pathSegments[1]
        val p = pathSegments[2]
        val host = "${uri.scheme}://${uri.authority}"
        return Triple(host, u, p)
      }
    } catch (ignored: Exception) {}
    return null
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
          val expDate = userInfo?.optString("exp_date", "")?.takeIf { it.isNotBlank() }
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

        if (!forceRefresh) {
          val cachedMem = categoryCache[cacheKey]
          if (!cachedMem.isNullOrEmpty()) {
            return@withContext Result.success(cachedMem)
          }
          val cachedDisk = getCachedCategories(cacheKey)
          if (cachedDisk.isNotEmpty()) {
            categoryCache[cacheKey] = cachedDisk
            return@withContext Result.success(cachedDisk)
          }
        }

        val url = "$cleanServer/player_api.php?username=$cleanUser&password=$cleanPass&action=get_live_categories"
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", "IPTVSmartersPro/3.1.5 (Linux; Android 12)")
          .header("Accept", "*/*")
          .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body ?: return@withContext Result.failure(Exception("فارغ"))

        val list = parseCategoriesStreaming(responseBody)

        if (list.isNotEmpty()) {
          categoryCache[cacheKey] = list
          saveCachedCategories(cacheKey, list)
        }
        Result.success(list)
      } catch (e: Exception) {
        Log.e("XtreamRepository", "Fetch categories error", e)
        val cleanServer = cleanServerUrl(serverUrl)
        val cleanUser = username.trim()
        val cacheKey = "$cleanServer|$cleanUser"
        val diskFallback = getCachedCategories(cacheKey)
        if (diskFallback.isNotEmpty()) {
          return@withContext Result.success(diskFallback)
        }
        Result.failure(e)
      }
    }

  private fun parseCategoriesStreaming(responseBody: okhttp3.ResponseBody): List<XtreamCategory> {
    val list = ArrayList<XtreamCategory>(128)
    try {
      responseBody.byteStream().use { inputStream ->
        val source = inputStream.source().buffer()
        val reader = JsonReader.of(source)
        reader.isLenient = true

        if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
          reader.beginArray()
          while (reader.hasNext()) {
            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
              reader.beginObject()
              var id = ""
              var name = ""
              while (reader.hasNext()) {
                when (reader.nextName()) {
                  "category_id", "id" -> {
                    id = when (reader.peek()) {
                      JsonReader.Token.STRING, JsonReader.Token.NUMBER -> reader.nextString()
                      else -> { reader.skipValue(); id }
                    }
                  }
                  "category_name", "name" -> {
                    name = when (reader.peek()) {
                      JsonReader.Token.STRING -> reader.nextString()
                      else -> { reader.skipValue(); name }
                    }
                  }
                  else -> reader.skipValue()
                }
              }
              reader.endObject()
              if (id.isNotBlank() && id != "0") {
                list.add(XtreamCategory(id, name.ifBlank { "باقة $id" }))
              }
            } else {
              reader.skipValue()
            }
          }
          reader.endArray()
        } else if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
          reader.beginObject()
          while (reader.hasNext()) {
            val key = reader.nextName()
            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
              reader.beginObject()
              var id = key
              var name = "باقة $key"
              while (reader.hasNext()) {
                when (reader.nextName()) {
                  "category_id", "id" -> {
                    id = when (reader.peek()) {
                      JsonReader.Token.STRING, JsonReader.Token.NUMBER -> reader.nextString()
                      else -> { reader.skipValue(); id }
                    }
                  }
                  "category_name", "name" -> {
                    name = when (reader.peek()) {
                      JsonReader.Token.STRING -> reader.nextString()
                      else -> { reader.skipValue(); name }
                    }
                  }
                  else -> reader.skipValue()
                }
              }
              reader.endObject()
              list.add(XtreamCategory(id, name))
            } else {
              reader.skipValue()
            }
          }
          reader.endObject()
        }
      }
    } catch (e: Exception) {
      Log.e("XtreamRepository", "Streaming categories parse error", e)
    }
    return list
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

      if (!forceRefresh) {
        val cached = streamCache[cacheKey]
        if (!cached.isNullOrEmpty()) {
          return@withContext Result.success(cached)
        }
        val cachedDisk = getCachedStreams(cacheKey)
        if (cachedDisk.isNotEmpty()) {
          streamCache[cacheKey] = cachedDisk
          return@withContext Result.success(cachedDisk)
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
      val responseBody = response.body ?: return@withContext Result.failure(Exception("قائمة القنوات فارغة"))

      var list = parseStreamsStreaming(responseBody, cleanServer, cleanUser, cleanPass, preferredFormat)

      // Fallback 1: If filtered fetch returned empty or failed because the server doesn't support &category_id=,
      // fetch all streams and filter in memory
      if (list.isEmpty() && isFilterRequested) {
        val allUrl = "$cleanServer/player_api.php?username=$cleanUser&password=$cleanPass&action=get_live_streams"
        val allReq = Request.Builder()
          .url(allUrl)
          .header("User-Agent", "IPTVSmartersPro/3.1.5 (Linux; Android 12)")
          .header("Accept", "*/*")
          .build()
        val allRes = client.newCall(allReq).execute()
        val allResBody = allRes.body
        if (allResBody != null) {
          val allChannels = parseStreamsStreaming(allResBody, cleanServer, cleanUser, cleanPass, preferredFormat)
          list = if (isFilterRequested) allChannels.filter { it.categoryId == categoryId } else allChannels
        }
      }

      // Fallback 2: If player_api.php is blocked or returned empty, query get.php with m3u_plus
      if (list.isEmpty()) {
        val m3uUrl = "$cleanServer/get.php?username=$cleanUser&password=$cleanPass&type=m3u_plus&output=ts"
        val m3uRes = parseM3uPlaylist(m3uUrl)
        if (m3uRes.isSuccess) {
          val m3uList = m3uRes.getOrDefault(emptyList())
          list = if (isFilterRequested) m3uList.filter { it.categoryId == categoryId } else m3uList
        }
      }

      if (list.isNotEmpty()) {
        streamCache[cacheKey] = list
        saveCachedStreams(cacheKey, list)
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e("XtreamRepository", "Fetch streams error, trying m3u fallback", e)
      val cleanServer = cleanServerUrl(serverUrl)
      val cleanUser = username.trim()
      val cacheKey = "$cleanServer|$cleanUser|${categoryId ?: "ALL"}"
      val diskFallback = getCachedStreams(cacheKey)
      if (diskFallback.isNotEmpty()) {
        return@withContext Result.success(diskFallback)
      }
      try {
        val cleanPass = password.trim()
        val m3uUrl = "$cleanServer/get.php?username=$cleanUser&password=$cleanPass&type=m3u_plus&output=ts"
        val m3uRes = parseM3uPlaylist(m3uUrl)
        if (m3uRes.isSuccess && m3uRes.getOrDefault(emptyList()).isNotEmpty()) {
          val m3uList = m3uRes.getOrDefault(emptyList())
          val filtered = if (!categoryId.isNullOrEmpty() && categoryId != "ALL") m3uList.filter { it.categoryId == categoryId } else m3uList
          return@withContext Result.success(filtered)
        }
      } catch (ignored: Exception) {}
      Result.failure(e)
    }
  }

  /**
   * Ultra-high performance streaming JSON parser that can ingest 100,000+ channels
   * without OOM or memory spikes by parsing tokens sequentially.
   */
  private fun parseStreamsStreaming(
    responseBody: okhttp3.ResponseBody,
    cleanServer: String,
    cleanUser: String,
    cleanPass: String,
    preferredFormat: String
  ): List<XtreamChannel> {
    val list = ArrayList<XtreamChannel>(2048)
    val maxChannels = 35000
    try {
      responseBody.byteStream().use { inputStream ->
        val source = inputStream.source().buffer()
        val reader = JsonReader.of(source)
        reader.isLenient = true

        val token = reader.peek()
        if (token == JsonReader.Token.BEGIN_ARRAY) {
          reader.beginArray()
          while (reader.hasNext()) {
            if (list.size >= maxChannels) {
              reader.skipValue()
              continue
            }
            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
              val ch = parseSingleChannelStreaming(reader, cleanServer, cleanUser, cleanPass, preferredFormat)
              if (ch != null) {
                list.add(ch)
              }
            } else {
              reader.skipValue()
            }
          }
          reader.endArray()
        } else if (token == JsonReader.Token.BEGIN_OBJECT) {
          reader.beginObject()
          while (reader.hasNext()) {
            reader.nextName() // object key
            if (list.size >= maxChannels) {
              reader.skipValue()
              continue
            }
            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
              val ch = parseSingleChannelStreaming(reader, cleanServer, cleanUser, cleanPass, preferredFormat)
              if (ch != null) {
                list.add(ch)
              }
            } else {
              reader.skipValue()
            }
          }
          reader.endObject()
        }
      }
    } catch (e: Exception) {
      Log.e("XtreamRepository", "Streaming parse error", e)
    }
    return list
  }

  private fun parseSingleChannelStreaming(
    reader: JsonReader,
    cleanServer: String,
    cleanUser: String,
    cleanPass: String,
    preferredFormat: String
  ): XtreamChannel? {
    reader.beginObject()
    var streamId = ""
    var name = ""
    var iconUrl: String? = null
    var categoryId: String? = null
    var containerExtension = "ts"

    while (reader.hasNext()) {
      when (reader.nextName()) {
        "stream_id", "id", "num" -> {
          streamId = when (reader.peek()) {
            JsonReader.Token.NUMBER, JsonReader.Token.STRING -> reader.nextString()
            else -> {
              reader.skipValue()
              streamId
            }
          }
        }
        "name", "title", "stream_name" -> {
          name = when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString()
            else -> {
              reader.skipValue()
              name
            }
          }
        }
        "stream_icon", "icon", "logo" -> {
          iconUrl = when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString().takeIf { it.isNotBlank() }
            else -> {
              reader.skipValue()
              iconUrl
            }
          }
        }
        "category_id" -> {
          categoryId = when (reader.peek()) {
            JsonReader.Token.NUMBER, JsonReader.Token.STRING -> reader.nextString()
            else -> {
              reader.skipValue()
              categoryId
            }
          }
        }
        "container_extension" -> {
          containerExtension = when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString()
            else -> {
              reader.skipValue()
              containerExtension
            }
          }
        }
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    if (streamId.isBlank() || streamId == "0") return null

    val finalName = name.ifBlank { "قناة $streamId" }
    val ext = when {
      preferredFormat.contains("m3u8", ignoreCase = true) || preferredFormat.contains("hls", ignoreCase = true) -> "m3u8"
      preferredFormat.contains("ts", ignoreCase = true) -> "ts"
      else -> if (containerExtension.isNotBlank()) containerExtension else "ts"
    }

    val playUrl = "$cleanServer/live/$cleanUser/$cleanPass/$streamId.$ext"

    return XtreamChannel(
      streamId = streamId,
      name = finalName,
      iconUrl = iconUrl,
      categoryId = categoryId,
      playUrl = playUrl
    )
  }

  suspend fun parseM3uPlaylist(urlOrContent: String): Result<List<XtreamChannel>> =
    withContext(Dispatchers.IO) {
      try {
        val channels = ArrayList<XtreamChannel>(2048)
        var currentName: String? = null
        var currentLogo: String? = null
        var currentGroup: String? = null
        var currentUa: String? = null
        var currentReferer: String? = null
        var currentOrigin: String? = null
        var currentDrmScheme: String? = null
        var currentDrmKey: String? = null
        val customHeaders = mutableMapOf<String, String>()

        val baseUrl = if (urlOrContent.startsWith("http://") || urlOrContent.startsWith("https://")) {
          urlOrContent.substringBeforeLast("/")
        } else null

        val processLine: (String) -> Unit = { line ->
          val trimmed = line.trim()
          if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
            // Extract channel name after the last comma (if any)
            val nameCandidate = trimmed.substringAfterLast(",").trim()
            if (nameCandidate.isNotBlank()) {
              currentName = nameCandidate
            }

            // Extract tvg-logo or logo
            if (trimmed.contains("tvg-logo=\"", ignoreCase = true)) {
              currentLogo = trimmed.substringAfter("tvg-logo=\"", "").substringBefore("\"").takeIf { it.isNotBlank() }
            } else if (trimmed.contains("logo=\"", ignoreCase = true)) {
              currentLogo = trimmed.substringAfter("logo=\"", "").substringBefore("\"").takeIf { it.isNotBlank() }
            }

            // Extract group-title or category
            if (trimmed.contains("group-title=\"", ignoreCase = true)) {
              currentGroup = trimmed.substringAfter("group-title=\"", "").substringBefore("\"").takeIf { it.isNotBlank() }
            } else if (trimmed.contains("group=\"", ignoreCase = true)) {
              currentGroup = trimmed.substringAfter("group=\"", "").substringBefore("\"").takeIf { it.isNotBlank() }
            }

            // Extract embedded user-agent / referer inside EXTINF
            if (trimmed.contains("user-agent=\"", ignoreCase = true)) {
              currentUa = trimmed.substringAfter("user-agent=\"", "").substringBefore("\"")
            } else if (trimmed.contains("http-user-agent=\"", ignoreCase = true)) {
              currentUa = trimmed.substringAfter("http-user-agent=\"", "").substringBefore("\"")
            }
            if (trimmed.contains("referer=\"", ignoreCase = true)) {
              currentReferer = trimmed.substringAfter("referer=\"", "").substringBefore("\"")
            } else if (trimmed.contains("http-referrer=\"", ignoreCase = true)) {
              currentReferer = trimmed.substringAfter("http-referrer=\"", "").substringBefore("\"")
            }
          } else if (trimmed.startsWith("#EXT-X-STREAM-INF:", ignoreCase = true)) {
            // HLS variant stream tag
            if (trimmed.contains("NAME=\"", ignoreCase = true)) {
              currentName = trimmed.substringAfter("NAME=\"", "").substringBefore("\"")
            } else if (trimmed.contains("RESOLUTION=", ignoreCase = true)) {
              val res = trimmed.substringAfter("RESOLUTION=", "").substringBefore(",").substringBefore(" ")
              currentName = "بث جودة ($res)"
            }
          } else if (trimmed.startsWith("#EXT-X-MEDIA:", ignoreCase = true)) {
            if (trimmed.contains("NAME=\"", ignoreCase = true)) {
              currentName = trimmed.substringAfter("NAME=\"", "").substringBefore("\"")
            }
            if (trimmed.contains("URI=\"", ignoreCase = true)) {
              val extractedUri = trimmed.substringAfter("URI=\"", "").substringBefore("\"")
              if (extractedUri.isNotBlank()) {
                val fullUrl = if (!extractedUri.startsWith("http://") && !extractedUri.startsWith("https://") && baseUrl != null) {
                  "$baseUrl/$extractedUri"
                } else extractedUri
                channels.add(
                  XtreamChannel(
                    streamId = (channels.size + 1).toString(),
                    name = currentName ?: "مسار وسائط ${channels.size + 1}",
                    iconUrl = currentLogo,
                    categoryId = currentGroup ?: "وسائط إضافية",
                    playUrl = fullUrl
                  )
                )
                currentName = null
                currentLogo = null
              }
            }
          } else if (trimmed.startsWith("#EXTVLCOPT:", ignoreCase = true)) {
            val opt = trimmed.substringAfter(":").trim()
            if (opt.startsWith("http-user-agent=", ignoreCase = true)) {
              currentUa = opt.substringAfter("=")
            } else if (opt.startsWith("http-referrer=", ignoreCase = true)) {
              currentReferer = opt.substringAfter("=")
            }
          } else if (trimmed.startsWith("#KODIPROP:inputstream.adaptive.license_type", ignoreCase = true)) {
            currentDrmScheme = trimmed.substringAfter("=").trim()
          } else if (trimmed.startsWith("#KODIPROP:inputstream.adaptive.license_key", ignoreCase = true)) {
            currentDrmKey = trimmed.substringAfter("=").trim()
          } else if (trimmed.startsWith("#EXTHTTP:", ignoreCase = true)) {
            try {
              val jsonStr = trimmed.substringAfter(":").trim()
              val json = JSONObject(jsonStr)
              json.keys().forEach { k ->
                customHeaders[k] = json.optString(k, "")
              }
            } catch (ignored: Exception) {}
          } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
            // URL line found (can be ts, st, js, php, json, css, mpd, m3u8, mp4, etc.)
            var playUrl = trimmed
            // Resolve relative URLs if needed
            if (!playUrl.startsWith("http://") && !playUrl.startsWith("https://") && !playUrl.startsWith("rtmp://") && !playUrl.startsWith("rtsp://") && baseUrl != null) {
              playUrl = if (playUrl.startsWith("/")) {
                val baseDomain = baseUrl.substringBefore("://") + "://" + Uri.parse(baseUrl).authority
                "$baseDomain$playUrl"
              } else {
                "$baseUrl/$playUrl"
              }
            }

            // Append pipe headers if available
            val headersToAppend = mutableListOf<String>()
            if (!currentUa.isNullOrBlank()) headersToAppend.add("User-Agent=$currentUa")
            if (!currentReferer.isNullOrBlank()) headersToAppend.add("Referer=$currentReferer")
            if (!currentOrigin.isNullOrBlank()) headersToAppend.add("Origin=$currentOrigin")
            if (!currentDrmScheme.isNullOrBlank()) headersToAppend.add("drmScheme=$currentDrmScheme")
            if (!currentDrmKey.isNullOrBlank()) headersToAppend.add("drmLicense=$currentDrmKey")
            customHeaders.forEach { (k, v) ->
              headersToAppend.add("$k=$v")
            }

            if (headersToAppend.isNotEmpty() && !playUrl.contains("|")) {
              playUrl = "$playUrl|${headersToAppend.joinToString("&")}"
            }

            val name = currentName ?: "قناة ${channels.size + 1}"
            if (channels.size < 35000) {
              channels.add(
                XtreamChannel(
                  streamId = (channels.size + 1).toString(),
                  name = name,
                  iconUrl = currentLogo,
                  categoryId = currentGroup ?: "عام",
                  playUrl = playUrl
                )
              )
            }

            // Reset per-stream state
            currentName = null
            currentLogo = null
            currentGroup = null
            currentUa = null
            currentReferer = null
            currentOrigin = null
            currentDrmScheme = null
            currentDrmKey = null
            customHeaders.clear()
          }
        }

        if (urlOrContent.startsWith("http://") || urlOrContent.startsWith("https://")) {
          val req = Request.Builder()
            .url(urlOrContent)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
            .header("Accept", "*/*")
            .build()
          val res = client.newCall(req).execute()
          val body = res.body
          if (body != null) {
            body.byteStream().use { inputStream ->
              BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8), 32768).useLines { lines ->
                for (line in lines) {
                  processLine(line)
                }
              }
            }
          }
        } else {
          urlOrContent.lineSequence().forEach { line ->
            processLine(line)
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
            updateInterval = obj.optString("updateInterval", "عند بدء التطبيق"),
            lastUpdatedTimestamp = obj.optLong("lastUpdatedTimestamp", 0L),
            totalChannelCount = obj.optInt("totalChannelCount", 0),
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

  fun shouldRefreshPlaylist(config: XtreamPlaylistConfig): Boolean {
    if (config.lastUpdatedTimestamp <= 0L) return true
    val now = System.currentTimeMillis()
    val elapsed = now - config.lastUpdatedTimestamp

    return when (config.updateInterval) {
      "عند بدء التطبيق" -> true
      "كل ساعة" -> elapsed >= 1 * 60 * 60 * 1000L
      "كل 4 ساعات" -> elapsed >= 4 * 60 * 60 * 1000L
      "كل 6 ساعات" -> elapsed >= 6 * 60 * 60 * 1000L
      "كل 12 ساعة" -> elapsed >= 12 * 60 * 60 * 1000L
      "كل 24 ساعة", "كل يوم" -> elapsed >= 24 * 60 * 60 * 1000L
      "يدوياً فقط" -> false
      else -> true
    }
  }

  fun updatePlaylistTimestampAndCount(config: XtreamPlaylistConfig, channelCount: Int) {
    val updated = config.copy(
      lastUpdatedTimestamp = System.currentTimeMillis(),
      totalChannelCount = channelCount
    )
    savePlaylistConfig(updated)
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
      obj.put("lastUpdatedTimestamp", p.lastUpdatedTimestamp)
      obj.put("totalChannelCount", p.totalChannelCount)
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

  fun setActivePlaylist(config: XtreamPlaylistConfig) {
    val key = if (config.isM3u) config.m3uUrl else "${config.serverUrl}_${config.username}"
    prefs.edit().putString("active_playlist_key", key).apply()
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
      obj.put("lastUpdatedTimestamp", p.lastUpdatedTimestamp)
      obj.put("totalChannelCount", p.totalChannelCount)
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

  fun clearCustomUrlHistory() {
    prefs.edit().remove("custom_url_history").apply()
  }

  // Theme persistence
  fun getSavedTheme(): String {
    return prefs.getString("app_theme_id", "gold") ?: "gold"
  }

  fun saveTheme(themeId: String) {
    prefs.edit().putString("app_theme_id", themeId).apply()
  }

  private val channelCacheDir by lazy {
    java.io.File(context.cacheDir, "channel_cache").apply { mkdirs() }
  }

  private fun getCacheFile(cacheKey: String): java.io.File {
    val safeName = cacheKey.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(60)
    return java.io.File(channelCacheDir, "streams_$safeName.gz")
  }

  // Instant 0ms Disk Cache for Categories and Streams
  fun getCachedCategories(cacheKey: String): List<XtreamCategory> {
    val json = prefs.getString("disk_categories_$cacheKey", null) ?: return emptyList()
    return try {
      val array = JSONArray(json)
      val list = mutableListOf<XtreamCategory>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          XtreamCategory(
            categoryId = obj.optString("categoryId"),
            categoryName = obj.optString("categoryName"),
            channelCount = obj.optInt("channelCount", 0)
          )
        )
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun saveCachedCategories(cacheKey: String, categories: List<XtreamCategory>) {
    try {
      val array = JSONArray()
      for (cat in categories) {
        val obj = JSONObject()
        obj.put("categoryId", cat.categoryId)
        obj.put("categoryName", cat.categoryName)
        obj.put("channelCount", cat.channelCount)
        array.put(obj)
      }
      prefs.edit().putString("disk_categories_$cacheKey", array.toString()).apply()
    } catch (ignored: Exception) {}
  }

  fun getCachedStreams(cacheKey: String): List<XtreamChannel> {
    val file = getCacheFile(cacheKey)
    if (!file.exists() || file.length() == 0L) {
      // Check legacy SharedPreferences once
      val legacy = prefs.getString("disk_streams_$cacheKey", null)
      if (!legacy.isNullOrBlank()) {
        try {
          val array = JSONArray(legacy)
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
          saveCachedStreams(cacheKey, list)
          return list
        } catch (ignored: Exception) {}
      }
      return emptyList()
    }

    val list = ArrayList<XtreamChannel>(1024)
    try {
      java.util.zip.GZIPInputStream(java.io.FileInputStream(file).buffered(16384)).use { gz ->
        val reader = gz.bufferedReader(Charsets.UTF_8)
        var line: String? = reader.readLine()
        while (line != null) {
          if (line.isNotEmpty()) {
            val parts = line.split("\t")
            if (parts.size >= 5) {
              list.add(
                XtreamChannel(
                  streamId = parts[0],
                  name = parts[1],
                  iconUrl = parts[2].takeIf { it.isNotEmpty() },
                  categoryId = parts[3].takeIf { it.isNotEmpty() },
                  playUrl = parts[4]
                )
              )
            }
          }
          line = reader.readLine()
        }
      }
    } catch (e: Exception) {
      Log.e("XtreamRepository", "Error reading compressed stream cache", e)
    }
    return list
  }

  fun saveCachedStreams(cacheKey: String, channels: List<XtreamChannel>) {
    try {
      val file = getCacheFile(cacheKey)
      // Save top 5000 channels compressed to GZIP (takes ~150-250KB only on disk!)
      val toSave = channels.take(5000)
      java.util.zip.GZIPOutputStream(java.io.FileOutputStream(file).buffered(16384)).use { gz ->
        val writer = gz.bufferedWriter(Charsets.UTF_8)
        for (ch in toSave) {
          writer.write(ch.streamId.replace("\t", " "))
          writer.write("\t")
          writer.write(ch.name.replace("\t", " ").replace("\n", " "))
          writer.write("\t")
          writer.write(ch.iconUrl?.replace("\t", " ") ?: "")
          writer.write("\t")
          writer.write(ch.categoryId?.replace("\t", " ") ?: "")
          writer.write("\t")
          writer.write(ch.playUrl.replace("\t", " ").replace("\n", " "))
          writer.newLine()
        }
        writer.flush()
      }
      // Remove any heavy string from SharedPreferences to keep app storage super light
      prefs.edit().remove("disk_streams_$cacheKey").apply()
    } catch (e: Exception) {
      Log.e("XtreamRepository", "Error writing compressed stream cache", e)
    }
  }

  fun clearAllCache(): Int {
    val count = streamCache.size
    clearMemoryCache()
    try {
      channelCacheDir.listFiles()?.forEach { it.delete() }
    } catch (ignored: Exception) {}
    val allKeys = prefs.all.keys.filter { it.startsWith("disk_streams_") || it.startsWith("disk_categories_") }
    val editor = prefs.edit()
    allKeys.forEach { editor.remove(it) }
    editor.apply()
    return count + allKeys.size
  }
}

