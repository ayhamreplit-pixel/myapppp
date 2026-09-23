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

  fun resetToDefaults() {
    setHardwareDecodingEnabled(true)
    setAutoPlayLastChannelEnabled(false)
    setAdaptiveBitrateEnabled(true)
    setDataSaverModeEnabled(false)
    setKeepScreenOnEnabled(true)
    setAudioBoost(true)
    setVocalClarityEnabled(true)
    setAudioBoostPercent(50)
    setSwipeGesturesEnabled(true)
    setShowClock(true)
    setFastZapping(true)
    setBuffer("متوازن ذكي (10 ثوان)")
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
