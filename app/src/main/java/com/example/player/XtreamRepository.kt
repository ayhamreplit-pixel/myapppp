package com.example.player

import android.content.Context
import com.example.model.XtreamAccountInfo
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class XtreamRepository(context: Context) {
  private val prefs = context.getSharedPreferences("xtream_prefs", Context.MODE_PRIVATE)

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  fun getSavedCredentials(): Triple<String, String, String>? {
    val server = prefs.getString("server_url", null) ?: return null
    val user = prefs.getString("username", null) ?: return null
    val pass = prefs.getString("password", null) ?: return null
    return Triple(server, user, pass)
  }

  fun saveCredentials(server: String, user: String, pass: String) {
    prefs.edit()
      .putString("server_url", server.trimEnd('/'))
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
        val cleanServer = serverUrl.trim().trimEnd('/')
        val url = "$cleanServer/player_api.php?username=$username&password=$password"
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
          return@withContext Result.failure(Exception("HTTP error code: ${response.code}"))
        }

        val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
        val json = JSONObject(body)

        val userInfo = json.optJSONObject("user_info")
        val auth = userInfo?.optInt("auth", 0) ?: 0
        val status = userInfo?.optString("status", "Unknown") ?: "Unknown"

        if (auth == 1 || status.equals("Active", ignoreCase = true)) {
          saveCredentials(cleanServer, username, password)
          val expDate = userInfo?.optString("exp_date", null)
          Result.success(
            XtreamAccountInfo(
              username = username,
              status = status,
              expDate = expDate,
              serverUrl = cleanServer
            )
          )
        } else {
          Result.failure(Exception("Authentication failed: Invalid credentials or expired account"))
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun fetchCategories(serverUrl: String, username: String, password: String): Result<List<XtreamCategory>> =
    withContext(Dispatchers.IO) {
      try {
        val cleanServer = serverUrl.trim().trimEnd('/')
        val url = "$cleanServer/player_api.php?username=$username&password=$password&action=get_live_categories"
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty categories response"))

        val array = JSONArray(body)
        val list = mutableListOf<XtreamCategory>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val id = obj.optString("category_id", "")
          val name = obj.optString("category_name", "Unknown Category")
          if (id.isNotEmpty()) {
            list.add(XtreamCategory(id, name))
          }
        }
        Result.success(list)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun fetchStreams(
    serverUrl: String,
    username: String,
    password: String,
    categoryId: String? = null
  ): Result<List<XtreamChannel>> = withContext(Dispatchers.IO) {
    try {
      val cleanServer = serverUrl.trim().trimEnd('/')
      val url = buildString {
        append("$cleanServer/player_api.php?username=$username&password=$password&action=get_live_streams")
        if (!categoryId.isNullOrEmpty() && categoryId != "ALL") {
          append("&category_id=$categoryId")
        }
      }
      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty streams response"))

      val array = JSONArray(body)
      val list = mutableListOf<XtreamChannel>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val streamId = obj.optString("stream_id", "")
        val name = obj.optString("name", "Channel")
        val icon = obj.optString("stream_icon", null).takeIf { !it.isNullOrBlank() }
        val catId = obj.optString("category_id", null)

        val playUrl = "$cleanServer/live/$username/$password/$streamId.m3u8"
        list.add(
          XtreamChannel(
            streamId = streamId,
            name = name,
            iconUrl = icon,
            categoryId = catId,
            playUrl = playUrl
          )
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun parseM3uPlaylist(urlOrContent: String): Result<List<XtreamChannel>> =
    withContext(Dispatchers.IO) {
      try {
        val content = if (urlOrContent.startsWith("http://") || urlOrContent.startsWith("https://")) {
          val req = Request.Builder().url(urlOrContent).build()
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
            // Parse name
            currentName = trimmed.substringAfterLast(",").trim()
            // Parse logo
            currentLogo = if (trimmed.contains("tvg-logo=\"")) {
              trimmed.substringAfter("tvg-logo=\"").substringBefore("\"")
            } else null
            // Parse group
            currentGroup = if (trimmed.contains("group-title=\"")) {
              trimmed.substringAfter("group-title=\"").substringBefore("\"")
            } else null
          } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
            val playUrl = trimmed
            val name = currentName ?: "Channel ${channels.size + 1}"
            channels.add(
              XtreamChannel(
                streamId = channels.size.toString(),
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

  // Favorites management
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
