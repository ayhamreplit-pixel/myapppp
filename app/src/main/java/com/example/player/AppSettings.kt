package com.example.player

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Real App Settings Manager with persistent SharedPreferences backing.
 * All toggles immediately persist and affect playback, cache, and UI behavior.
 */
class AppSettings private constructor(context: Context) {

  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

  // 1. Playback & Video Engine
  var hardwareDecoding by mutableStateOf(prefs.getBoolean("hw_decoding", true))
    private set

  var autoPlayLastChannel by mutableStateOf(prefs.getBoolean("autoplay_last", false))
    private set

  var adaptiveBitrate by mutableStateOf(prefs.getBoolean("adaptive_bitrate", true))
    private set

  var dataSaverMode by mutableStateOf(prefs.getBoolean("data_saver", false))
    private set

  var keepScreenOn by mutableStateOf(prefs.getBoolean("keep_screen_on", true))
    private set

  var defaultAspectRatio by mutableStateOf(prefs.getString("aspect_ratio", "16:9 قياسي") ?: "16:9 قياسي")
    private set

  // 2. Audio & Speech
  var audioBoostEnabled by mutableStateOf(prefs.getBoolean("audio_boost_enabled", true))
    private set

  var vocalClarity by mutableStateOf(prefs.getBoolean("vocal_clarity", true))
    private set

  var defaultAudioBoostPercent by mutableIntStateOf(prefs.getInt("audio_boost_pct", 50))
    private set

  // 3. Player UI & Gestures
  var swipeGestures by mutableStateOf(prefs.getBoolean("swipe_gestures", true))
    private set

  var showClockOverlay by mutableStateOf(prefs.getBoolean("show_clock_overlay", true))
    private set

  var fastChannelZapping by mutableStateOf(prefs.getBoolean("fast_zapping", true))
    private set

  var bufferProfile by mutableStateOf(prefs.getString("buffer_profile", "متوازن ذكي (10 ثوان)") ?: "متوازن ذكي (10 ثوان)")
    private set

  // 4. Subtitles Engine
  var subtitlesEnabled by mutableStateOf(prefs.getBoolean("subtitles_enabled", true))
    private set

  var subtitleFontSize by mutableIntStateOf(prefs.getInt("subtitle_font_size", 18))
    private set

  var subtitleEncoding by mutableStateOf(prefs.getString("subtitle_encoding", "UTF-8 تلقائي") ?: "UTF-8 تلقائي")
    private set

  var subtitleDelaySec by mutableIntStateOf(prefs.getInt("subtitle_delay_sec", 0))
    private set

  // 5. Electronic Program Guide (EPG)
  var epgAutoSync by mutableStateOf(prefs.getBoolean("epg_auto_sync", true))
    private set

  var epgTimezoneOffsetHours by mutableIntStateOf(prefs.getInt("epg_tz_offset", 3)) // Default GMT+3 for Arab region
    private set

  var epgSyncInterval by mutableStateOf(prefs.getString("epg_sync_interval", "كل 12 ساعة") ?: "كل 12 ساعة")
    private set

  // 6. Picture-in-Picture & Background Playback
  var pipAutoEnable by mutableStateOf(prefs.getBoolean("pip_auto_enable", true))
    private set

  var backgroundAudioEnabled by mutableStateOf(prefs.getBoolean("bg_audio_enabled", false))
    private set

  // 7. Parental Controls
  var parentalControlEnabled by mutableStateOf(prefs.getBoolean("parental_control_enabled", false))
    private set

  var parentalPin by mutableStateOf(prefs.getString("parental_pin", "0000") ?: "0000")
    private set

  var hideAdultContent by mutableStateOf(prefs.getBoolean("hide_adult_content", true))
    private set

  // 8. Theme Accent Glow
  var themeAccentName by mutableStateOf(prefs.getString("theme_accent", "أزرق ملكي") ?: "أزرق ملكي")
    private set

  // Setters with persistent commit
  fun setHardwareDecodingEnabled(enabled: Boolean) {
    hardwareDecoding = enabled
    prefs.edit().putBoolean("hw_decoding", enabled).apply()
  }

  fun setAutoPlayLastChannelEnabled(enabled: Boolean) {
    autoPlayLastChannel = enabled
    prefs.edit().putBoolean("autoplay_last", enabled).apply()
  }

  fun setAdaptiveBitrateEnabled(enabled: Boolean) {
    adaptiveBitrate = enabled
    prefs.edit().putBoolean("adaptive_bitrate", enabled).apply()
  }

  fun setDataSaverModeEnabled(enabled: Boolean) {
    dataSaverMode = enabled
    prefs.edit().putBoolean("data_saver", enabled).apply()
  }

  fun setKeepScreenOnEnabled(enabled: Boolean) {
    keepScreenOn = enabled
    prefs.edit().putBoolean("keep_screen_on", enabled).apply()
  }

  fun setAspectRatio(ratio: String) {
    defaultAspectRatio = ratio
    prefs.edit().putString("aspect_ratio", ratio).apply()
  }

  fun setAudioBoost(enabled: Boolean) {
    audioBoostEnabled = enabled
    prefs.edit().putBoolean("audio_boost_enabled", enabled).apply()
  }

  fun setVocalClarityEnabled(enabled: Boolean) {
    vocalClarity = enabled
    prefs.edit().putBoolean("vocal_clarity", enabled).apply()
  }

  fun setAudioBoostPercent(pct: Int) {
    defaultAudioBoostPercent = pct
    prefs.edit().putInt("audio_boost_pct", pct).apply()
  }

  fun setSwipeGesturesEnabled(enabled: Boolean) {
    swipeGestures = enabled
    prefs.edit().putBoolean("swipe_gestures", enabled).apply()
  }

  fun setShowClock(enabled: Boolean) {
    showClockOverlay = enabled
    prefs.edit().putBoolean("show_clock_overlay", enabled).apply()
  }

  fun setFastZapping(enabled: Boolean) {
    fastChannelZapping = enabled
    prefs.edit().putBoolean("fast_zapping", enabled).apply()
  }

  fun setBuffer(profile: String) {
    bufferProfile = profile
    prefs.edit().putString("buffer_profile", profile).apply()
  }

  fun setSubtitles(enabled: Boolean) {
    subtitlesEnabled = enabled
    prefs.edit().putBoolean("subtitles_enabled", enabled).apply()
  }

  fun setSubtitleSize(size: Int) {
    subtitleFontSize = size
    prefs.edit().putInt("subtitle_font_size", size).apply()
  }

  fun setSubtitleEnc(encoding: String) {
    subtitleEncoding = encoding
    prefs.edit().putString("subtitle_encoding", encoding).apply()
  }

  fun setSubtitleDelay(seconds: Int) {
    subtitleDelaySec = seconds
    prefs.edit().putInt("subtitle_delay_sec", seconds).apply()
  }

  fun setEpgSync(enabled: Boolean) {
    epgAutoSync = enabled
    prefs.edit().putBoolean("epg_auto_sync", enabled).apply()
  }

  fun setEpgTimezone(hoursOffset: Int) {
    epgTimezoneOffsetHours = hoursOffset
    prefs.edit().putInt("epg_tz_offset", hoursOffset).apply()
  }

  fun setEpgInterval(interval: String) {
    epgSyncInterval = interval
    prefs.edit().putString("epg_sync_interval", interval).apply()
  }

  fun setPipAuto(enabled: Boolean) {
    pipAutoEnable = enabled
    prefs.edit().putBoolean("pip_auto_enable", enabled).apply()
  }

  fun setBackgroundAudio(enabled: Boolean) {
    backgroundAudioEnabled = enabled
    prefs.edit().putBoolean("bg_audio_enabled", enabled).apply()
  }

  fun setParentalEnabled(enabled: Boolean) {
    parentalControlEnabled = enabled
    prefs.edit().putBoolean("parental_control_enabled", enabled).apply()
  }

  fun setParentalCode(pin: String) {
    parentalPin = pin
    prefs.edit().putString("parental_pin", pin).apply()
  }

  fun setHideAdult(hide: Boolean) {
    hideAdultContent = hide
    prefs.edit().putBoolean("hide_adult_content", hide).apply()
  }

  fun setThemeAccent(accent: String) {
    themeAccentName = accent
    prefs.edit().putString("theme_accent", accent).apply()
  }

  fun resetToDefaults() {
    setHardwareDecodingEnabled(true)
    setAutoPlayLastChannelEnabled(false)
    setAdaptiveBitrateEnabled(true)
    setDataSaverModeEnabled(false)
    setKeepScreenOnEnabled(true)
    setAspectRatio("16:9 قياسي")
    setAudioBoost(true)
    setVocalClarityEnabled(true)
    setAudioBoostPercent(50)
    setSwipeGesturesEnabled(true)
    setShowClock(true)
    setFastZapping(true)
    setBuffer("متوازن ذكي (10 ثوان)")
    setSubtitles(true)
    setSubtitleSize(18)
    setSubtitleEnc("UTF-8 تلقائي")
    setSubtitleDelay(0)
    setEpgSync(true)
    setEpgTimezone(3)
    setEpgInterval("كل 12 ساعة")
    setPipAuto(true)
    setBackgroundAudio(false)
    setParentalEnabled(false)
    setParentalCode("0000")
    setHideAdult(true)
    setThemeAccent("أزرق ملكي")
  }

  companion object {
    @Volatile
    private var instance: AppSettings? = null

    fun getInstance(context: Context): AppSettings {
      return instance ?: synchronized(this) {
        instance ?: AppSettings(context).also { instance = it }
      }
    }
  }
}
