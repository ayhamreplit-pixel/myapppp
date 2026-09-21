package com.example.model

import android.net.Uri
import android.util.Base64
import java.nio.charset.StandardCharsets

/**
 * Utility to parse URLs with embedded parameters (headers, DRM, cookies, user-agent, etc.)
 * Supports standard syntax such as:
 *   https://example.com/live.mpd?|drmScheme=clearkey&drmLicense=keyId:key
 *   https://example.com/stream.m3u8|User-Agent=CustomUA&Referer=https://ref.com&Origin=https://orig.com
 *   https://example.com/stream.mpd#drmScheme=widevine&drmLicense=https://lic.server.com
 *   https://example.com/stream.m3u8?headers={"User-Agent":"..."}
 */
object StreamUrlParser {

  data class ParsedStreamInfo(
    val cleanUrl: String,
    val userAgent: String? = null,
    val referer: String? = null,
    val origin: String? = null,
    val cookie: String? = null,
    val drmScheme: String? = null, // "clearkey", "widevine", "playready"
    val drmLicense: String? = null, // URL or keyId:key for clearkey
    val extraHeaders: Map<String, String> = emptyMap(),
    val format: StreamFormat = StreamFormat.AUTO
  )

  fun parse(rawInput: String, defaultTitle: String = "بث مباشر"): ParsedStreamInfo {
    val trimmed = rawInput.trim()
    if (trimmed.isEmpty()) {
      return ParsedStreamInfo(cleanUrl = "")
    }

    var cleanUrl = trimmed
    val paramsMap = mutableMapOf<String, String>()

    // Check for delimiter patterns:
    // 1) Pipe delimiter: "?|" or "|" (e.g. url?|key=val or url|key=val)
    if (cleanUrl.contains("|")) {
      val pipeIndex = cleanUrl.indexOf("|")
      var basePart = cleanUrl.substring(0, pipeIndex)
      if (basePart.endsWith("?")) {
        basePart = basePart.substring(0, basePart.length - 1)
      }
      val paramsPart = cleanUrl.substring(pipeIndex + 1)
      cleanUrl = basePart
      parseQueryStringToMap(paramsPart, paramsMap)
    }

    // 2) Hash delimiter: "#" (e.g. url#drmScheme=clearkey&...)
    if (cleanUrl.contains("#")) {
      val hashIndex = cleanUrl.indexOf("#")
      val basePart = cleanUrl.substring(0, hashIndex)
      val hashParams = cleanUrl.substring(hashIndex + 1)
      cleanUrl = basePart
      parseQueryStringToMap(hashParams, paramsMap)
    }

    // 3) Standard query string parameter scanning (e.g. ?drmScheme=... or &drmScheme=...)
    try {
      val uri = Uri.parse(cleanUrl)
      val queryParamNames = uri.queryParameterNames
      val drmKeys = listOf(
        "drmscheme", "drm_scheme", "drmlicense", "drm_license", "drmkey", "drm_key",
        "user-agent", "useragent", "ua", "referer", "referrer", "origin", "cookie", "cookies"
      )
      
      var hasDrmOrHeaderInQuery = false
      for (name in queryParamNames) {
        val lower = name.lowercase()
        if (drmKeys.contains(lower)) {
          hasDrmOrHeaderInQuery = true
          val value = uri.getQueryParameter(name)
          if (value != null && !paramsMap.containsKey(lower)) {
            paramsMap[lower] = value
          }
        }
      }
      
      // If drm or header params were inside regular query string, strip them from cleanUrl so ExoPlayer request is clean
      if (hasDrmOrHeaderInQuery) {
        val builder = uri.buildUpon().clearQuery()
        for (name in queryParamNames) {
          if (!drmKeys.contains(name.lowercase())) {
            uri.getQueryParameters(name).forEach { v ->
              builder.appendQueryParameter(name, v)
            }
          }
        }
        cleanUrl = builder.build().toString()
      }
    } catch (_: Exception) {
      // Ignore URI parse failures for malformed strings
    }

    // Extract recognized values case-insensitively
    var userAgent: String? = null
    var referer: String? = null
    var origin: String? = null
    var cookie: String? = null
    var drmScheme: String? = null
    var drmLicense: String? = null
    val extraHeaders = mutableMapOf<String, String>()

    paramsMap.forEach { (k, v) ->
      val keyLower = k.lowercase().replace("_", "").replace("-", "")
      when (keyLower) {
        "useragent", "ua" -> userAgent = v
        "referer", "referrer", "ref" -> referer = v
        "origin", "orig" -> origin = v
        "cookie", "cookies" -> cookie = v
        "drmscheme", "scheme" -> drmScheme = v
        "drmlicense", "drmkey", "key", "license", "lic" -> drmLicense = v
        else -> {
          // Additional custom headers (e.g. Authorization, X-Forwarded-For, etc.)
          extraHeaders[k] = v
        }
      }
    }

    // Auto-detect format from cleanUrl
    val lower = cleanUrl.lowercase()
    val format = when {
      lower.contains(".mpd") || lower.contains("format=mpd") || lower.contains("manifest.mpd") -> StreamFormat.DASH
      lower.contains(".ism") || lower.contains("/manifest") -> StreamFormat.SMOOTH_STREAMING
      lower.contains(".m3u8") || lower.contains(".m3u") -> StreamFormat.HLS
      lower.contains(".mp4") || lower.contains(".mkv") || lower.contains(".ts") -> StreamFormat.PROGRESSIVE
      else -> StreamFormat.AUTO
    }

    return ParsedStreamInfo(
      cleanUrl = cleanUrl,
      userAgent = userAgent,
      referer = referer,
      origin = origin,
      cookie = cookie,
      drmScheme = drmScheme,
      drmLicense = drmLicense,
      extraHeaders = extraHeaders,
      format = format
    )
  }

  private fun parseQueryStringToMap(query: String, outMap: MutableMap<String, String>) {
    val pairs = query.split("&", ";")
    for (pair in pairs) {
      val trimmed = pair.trim()
      if (trimmed.isEmpty()) continue
      val eqIndex = trimmed.indexOf("=")
      if (eqIndex > 0) {
        val key = trimmed.substring(0, eqIndex).trim()
        val value = trimmed.substring(eqIndex + 1).trim()
        outMap[key] = value
      } else {
        outMap[trimmed] = ""
      }
    }
  }

  /**
   * Builds ClearKey JSON string from "keyId:key" hex strings for ExoPlayer.
   * Format: {"keys":[{"kty":"oct","k":"<b64key>","kid":"<b64keyId>"}],"type":"temporary"}
   */
  fun buildClearKeyJson(keyIdColonKey: String): String? {
    try {
      val trimmed = keyIdColonKey.trim()
      if (!trimmed.contains(":")) return null
      val parts = trimmed.split(":")
      if (parts.size != 2) return null
      val hexKeyId = parts[0].trim()
      val hexKey = parts[1].trim()

      val b64KeyId = hexToBase64Url(hexKeyId)
      val b64Key = hexToBase64Url(hexKey)

      return """{"keys":[{"kty":"oct","k":"$b64Key","kid":"$b64KeyId"}],"type":"temporary"}"""
    } catch (_: Exception) {
      return null
    }
  }

  private fun hexToBase64Url(hex: String): String {
    val clean = hex.replace("-", "").replace(" ", "").trim()
    val len = clean.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
      data[i / 2] = ((Character.digit(clean[i], 16) shl 4) + Character.digit(clean[i + 1], 16)).toByte()
      i += 2
    }
    return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
  }
}
