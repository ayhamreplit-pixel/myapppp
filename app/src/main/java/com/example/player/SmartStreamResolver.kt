package com.example.player

import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.model.BroadcastStream
import com.example.model.StreamFormat
import com.example.model.StreamUrlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Universal Smart Stream & Embed Resolver
 * Resolves direct streams, dynamic IPTV scripts, embed/player pages, iframes, Base64 obfuscated streams,
 * and extracts required security headers (Referer, Origin, User-Agent, ClearKey DRM) automatically.
 */
object SmartStreamResolver {

  private const val TAG = "SmartStreamResolver"

  const val DEFAULT_DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
  const val DEFAULT_IPHONE_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1"

  // Permissive OkHttpClient with lenient SSL & redirect tracking
  val okHttpClient: OkHttpClient by lazy {
    val trustAllCerts = arrayOf<TrustManager>(
      object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
      }
    )

    val sslContext = SSLContext.getInstance("SSL").apply {
      init(null, trustAllCerts, SecureRandom())
    }

    OkHttpClient.Builder()
      .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
      .hostnameVerifier { _, _ -> true }
      .followRedirects(true)
      .followSslRedirects(true)
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .build()
  }

  /**
   * Fast synchronous parse using syntax parsing.
   */
  fun parseFast(input: String, title: String = "بث مباشر"): BroadcastStream {
    val cleanInput = extractUrlFromEmbedTag(input.trim())
    val parsed = StreamUrlParser.parse(cleanInput, title)
    return BroadcastStream(
      id = "quick_${System.currentTimeMillis()}",
      title = title,
      subtitle = if (parsed.format != StreamFormat.AUTO) parsed.format.extensionBadge else "بث مباشر",
      category = "بث مخصص",
      streamUrl = parsed.cleanUrl,
      format = parsed.format,
      isLive = true,
      userAgent = parsed.userAgent,
      referer = parsed.referer,
      origin = parsed.origin,
      cookie = parsed.cookie,
      drmScheme = parsed.drmScheme,
      drmKey = parsed.drmLicense,
      extraHeaders = parsed.extraHeaders
    )
  }

  /**
   * Deep async resolution: follows redirects, extracts video streams from web pages / iframes / embeds,
   * detects ClearKey DRM, and applies necessary headers.
   */
  suspend fun resolveAsync(input: String, title: String = "بث مباشر"): BroadcastStream = withContext(Dispatchers.IO) {
    val cleanInput = extractUrlFromEmbedTag(input.trim())
    val initial = parseFast(cleanInput, title)

    val url = initial.streamUrl.trim()
    if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
      return@withContext initial
    }

    // If it's already an obvious direct media stream without needing web extraction
    if (isDirectStreamUrl(url) && !url.contains("embed", ignoreCase = true) && !url.contains("player", ignoreCase = true)) {
      return@withContext initial
    }

    try {
      Log.i(TAG, "Resolving web page/embed for: $url")
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", initial.userAgent ?: DEFAULT_IPHONE_UA)
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .apply {
          if (!initial.referer.isNullOrBlank()) header("Referer", initial.referer)
          if (!initial.origin.isNullOrBlank()) header("Origin", initial.origin)
        }
        .build()

      okHttpClient.newCall(request).execute().use { response ->
        val finalUrl = response.request.url.toString()
        val contentType = (response.header("Content-Type") ?: "").lowercase()

        // 1. If redirected directly to a video/playlist stream
        if (isDirectStreamUrl(finalUrl) || contentType.contains("mpegurl") || contentType.contains("dash") || contentType.contains("video/")) {
          Log.i(TAG, "Redirected directly to stream: $finalUrl")
          return@withContext initial.copy(
            streamUrl = finalUrl,
            format = detectFormat(finalUrl, contentType),
            referer = initial.referer ?: url,
            origin = initial.origin ?: getOriginFromUrl(url)
          )
        }

        // 2. Response is HTML - inspect embed content
        val body = response.body?.string() ?: ""
        val extractedStream = extractStreamFromHtml(body, finalUrl)

        if (extractedStream != null) {
          Log.i(TAG, "Successfully extracted stream: ${extractedStream.streamUrl}")
          return@withContext initial.copy(
            streamUrl = extractedStream.streamUrl,
            format = extractedStream.format,
            userAgent = extractedStream.userAgent ?: initial.userAgent ?: DEFAULT_IPHONE_UA,
            referer = extractedStream.referer ?: finalUrl,
            origin = extractedStream.origin ?: getOriginFromUrl(finalUrl),
            drmScheme = extractedStream.drmScheme ?: initial.drmScheme,
            drmKey = extractedStream.drmKey ?: initial.drmKey,
            extraHeaders = initial.extraHeaders + extractedStream.extraHeaders
          )
        }

        // 3. Check for nested iframe inside body
        val nestedIframeUrl = extractNestedIframeUrl(body, finalUrl)
        if (!nestedIframeUrl.isNullOrBlank() && nestedIframeUrl != finalUrl) {
          Log.i(TAG, "Found nested iframe: $nestedIframeUrl - fetching...")
          val nestedRequest = Request.Builder()
            .url(nestedIframeUrl)
            .header("User-Agent", DEFAULT_IPHONE_UA)
            .header("Referer", finalUrl)
            .build()

          okHttpClient.newCall(nestedRequest).execute().use { nestedResponse ->
            val nestedBody = nestedResponse.body?.string() ?: ""
            val nestedStream = extractStreamFromHtml(nestedBody, nestedIframeUrl)
            if (nestedStream != null) {
              return@withContext initial.copy(
                streamUrl = nestedStream.streamUrl,
                format = nestedStream.format,
                userAgent = DEFAULT_IPHONE_UA,
                referer = nestedIframeUrl,
                origin = getOriginFromUrl(nestedIframeUrl),
                drmScheme = nestedStream.drmScheme ?: initial.drmScheme,
                drmKey = nestedStream.drmKey ?: initial.drmKey
              )
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Embed resolution encountered an issue, falling back to direct URL: ${e.message}")
    }

    return@withContext initial
  }

  private fun extractUrlFromEmbedTag(input: String): String {
    if (input.contains("<iframe", ignoreCase = true)) {
      val matcher = Pattern.compile("src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(input)
      if (matcher.find()) {
        return matcher.group(1)?.trim() ?: input
      }
    }
    if (input.contains("<video", ignoreCase = true) || input.contains("<source", ignoreCase = true)) {
      val matcher = Pattern.compile("src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(input)
      if (matcher.find()) {
        return matcher.group(1)?.trim() ?: input
      }
    }
    return input
  }

  private fun isDirectStreamUrl(url: String): Boolean {
    val clean = url.substringBefore('?').lowercase()
    return clean.endsWith(".m3u8") || clean.endsWith(".m3u") || clean.endsWith(".mpd") ||
           clean.endsWith(".ts") || clean.endsWith(".mp4") || clean.endsWith(".mkv") ||
           clean.endsWith(".ism") || clean.endsWith(".flv")
  }

  private fun detectFormat(url: String, contentType: String = ""): StreamFormat {
    val lower = url.lowercase()
    val ct = contentType.lowercase()
    return when {
      ct.contains("dash") || lower.contains(".mpd") || lower.contains("format=mpd") -> StreamFormat.DASH
      ct.contains("mpegurl") || lower.contains(".m3u8") || lower.contains(".m3u") -> StreamFormat.HLS
      ct.contains("smoothstreaming") || lower.contains(".ism") -> StreamFormat.SMOOTH_STREAMING
      lower.contains(".mp4") || lower.contains(".mkv") || lower.contains(".ts") || lower.contains(".flv") -> StreamFormat.PROGRESSIVE
      else -> StreamFormat.AUTO
    }
  }

  private data class ExtractedStreamData(
    val streamUrl: String,
    val format: StreamFormat = StreamFormat.AUTO,
    val userAgent: String? = null,
    val referer: String? = null,
    val origin: String? = null,
    val drmScheme: String? = null,
    val drmKey: String? = null,
    val extraHeaders: Map<String, String> = emptyMap()
  )

  private fun extractStreamFromHtml(html: String, pageUrl: String): ExtractedStreamData? {
    // 1. Look for explicit .m3u8 or .mpd URLs inside quotes
    val m3u8Regex = Pattern.compile("[\"'](https?://[^\"'\\s<>]+\\.(?:m3u8|m3u)[^\"'\\s<>]*)[\"']", Pattern.CASE_INSENSITIVE)
    val m3u8Matcher = m3u8Regex.matcher(html)
    if (m3u8Matcher.find()) {
      val foundUrl = m3u8Matcher.group(1)!!.replace("\\/", "/")
      return ExtractedStreamData(
        streamUrl = resolveRelativeUrl(foundUrl, pageUrl),
        format = StreamFormat.HLS,
        referer = pageUrl,
        origin = getOriginFromUrl(pageUrl)
      )
    }

    val mpdRegex = Pattern.compile("[\"'](https?://[^\"'\\s<>]+\\.mpd[^\"'\\s<>]*)[\"']", Pattern.CASE_INSENSITIVE)
    val mpdMatcher = mpdRegex.matcher(html)
    if (mpdMatcher.find()) {
      val foundUrl = mpdMatcher.group(1)!!.replace("\\/", "/")
      val drmData = extractDrmFromHtml(html)
      return ExtractedStreamData(
        streamUrl = resolveRelativeUrl(foundUrl, pageUrl),
        format = StreamFormat.DASH,
        referer = pageUrl,
        origin = getOriginFromUrl(pageUrl),
        drmScheme = drmData?.first,
        drmKey = drmData?.second
      )
    }

    // 2. Look for player config properties: source: "...", file: "...", src: "..."
    val configRegex = Pattern.compile("(?:source|file|src|streamUrl|hls|videoUrl)\\s*:\\s*[\"'](https?://[^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    val configMatcher = configRegex.matcher(html)
    if (configMatcher.find()) {
      val foundUrl = configMatcher.group(1)!!.replace("\\/", "/")
      return ExtractedStreamData(
        streamUrl = resolveRelativeUrl(foundUrl, pageUrl),
        format = detectFormat(foundUrl),
        referer = pageUrl,
        origin = getOriginFromUrl(pageUrl)
      )
    }

    // 3. Look for Base64 encoded stream URLs (atob("..."))
    val atobRegex = Pattern.compile("atob\\s*\\(\\s*[\"']([A-Za-z0-9+/=]{16,})[\"']\\s*\\)")
    val atobMatcher = atobRegex.matcher(html)
    while (atobMatcher.find()) {
      val b64 = atobMatcher.group(1)!!
      try {
        val decoded = String(Base64.decode(b64, Base64.DEFAULT), Charsets.UTF_8).trim()
        if (decoded.startsWith("http://", ignoreCase = true) || decoded.startsWith("https://", ignoreCase = true)) {
          if (isDirectStreamUrl(decoded) || decoded.contains("m3u8", ignoreCase = true) || decoded.contains("mpd", ignoreCase = true)) {
            return ExtractedStreamData(
              streamUrl = decoded,
              format = detectFormat(decoded),
              referer = pageUrl,
              origin = getOriginFromUrl(pageUrl)
            )
          }
        }
      } catch (_: Exception) {}
    }

    // 4. Look for raw progressive video tags: <video src="..."><source src="..."
    val videoSrcRegex = Pattern.compile("<(?:video|source)[^>]+src=[\"'](https?://[^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    val videoMatcher = videoSrcRegex.matcher(html)
    if (videoMatcher.find()) {
      val foundUrl = videoMatcher.group(1)!!.replace("\\/", "/")
      return ExtractedStreamData(
        streamUrl = resolveRelativeUrl(foundUrl, pageUrl),
        format = detectFormat(foundUrl),
        referer = pageUrl,
        origin = getOriginFromUrl(pageUrl)
      )
    }

    return null
  }

  private fun extractNestedIframeUrl(html: String, pageUrl: String): String? {
    val iframeRegex = Pattern.compile("<iframe[^>]+src=[\"'](https?://[^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    val matcher = iframeRegex.matcher(html)
    if (matcher.find()) {
      val url = matcher.group(1)!!.replace("\\/", "/")
      return resolveRelativeUrl(url, pageUrl)
    }
    return null
  }

  private fun extractDrmFromHtml(html: String): Pair<String, String>? {
    // Check for clearkey hex pair "keyId:key"
    val clearKeyPattern = Pattern.compile("([a-fA-F0-9]{32}):([a-fA-F0-9]{32})")
    val ckMatcher = clearKeyPattern.matcher(html)
    if (ckMatcher.find()) {
      return Pair("clearkey", "${ckMatcher.group(1)}:${ckMatcher.group(2)}")
    }

    // Check for clearkey json / kid / k properties
    val kidKPattern = Pattern.compile("[\"']?kid[\"']?\\s*:\\s*[\"']([a-zA-Z0-9_-]+)[\"'].*?[\"']?k[\"']?\\s*:\\s*[\"']([a-zA-Z0-9_-]+)[\"']", Pattern.DOTALL)
    val kidMatcher = kidKPattern.matcher(html)
    if (kidMatcher.find()) {
      return Pair("clearkey", "${kidMatcher.group(1)}:${kidMatcher.group(2)}")
    }

    return null
  }

  private fun resolveRelativeUrl(url: String, baseUrl: String): String {
    if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
      return url
    }
    return try {
      val baseUri = Uri.parse(baseUrl)
      if (url.startsWith("//")) {
        "${baseUri.scheme}:$url"
      } else if (url.startsWith("/")) {
        "${baseUri.scheme}://${baseUri.authority}$url"
      } else {
        val path = baseUri.path ?: "/"
        val lastSlash = path.lastIndexOf('/')
        val dir = if (lastSlash >= 0) path.substring(0, lastSlash + 1) else "/"
        "${baseUri.scheme}://${baseUri.authority}$dir$url"
      }
    } catch (_: Exception) {
      url
    }
  }

  private fun getOriginFromUrl(url: String): String {
    return try {
      val uri = Uri.parse(url)
      "${uri.scheme}://${uri.authority}"
    } catch (_: Exception) {
      url
    }
  }
}
