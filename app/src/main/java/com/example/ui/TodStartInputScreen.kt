package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStream
import com.example.model.StreamFormat
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodCyan
import com.example.ui.theme.TodViolet

val DefaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"

/**
 * Start & Input Interface: Media3 Simple / TOD Launcher.
 * Designed exactly after the user's uploaded Screenshot 1, but with dark modern luxury styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodStartInputScreen(
  onPlayStream: (BroadcastStream) -> Unit,
  onOpenPresetCatalog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val scrollState = rememberScrollState()

  var title by remember { mutableStateOf("Premier League") }
  var url by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }
  var origin by remember { mutableStateOf("") }
  var referer by remember { mutableStateOf("") }
  var cookie by remember { mutableStateOf("") }
  var drmKey by remember { mutableStateOf("") }
  var userAgent by remember { mutableStateOf(DefaultUserAgent) }
  var scheme by remember { mutableStateOf("") }

  val infiniteTransition = rememberInfiniteTransition(label = "pulsePlay")
  val playPulseScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "playScale"
  )

  val handlePlay = {
    focusManager.clearFocus()
    val cleanUrl = url.trim()
    if (cleanUrl.isNotEmpty()) {
      val stream = BroadcastStream(
        id = "custom_${System.currentTimeMillis()}",
        title = if (title.isNotBlank()) title.trim() else "Custom Stream",
        subtitle = when {
          cleanUrl.contains(".m3u8") -> "HLS Live Broadcast"
          cleanUrl.contains(".mpd") -> "DASH Stream"
          else -> "Media Playback"
        },
        category = "Custom",
        streamUrl = cleanUrl,
        format = when {
          cleanUrl.contains(".m3u8") -> StreamFormat.HLS
          cleanUrl.contains(".mpd") -> StreamFormat.DASH
          else -> StreamFormat.PROGRESSIVE
        },
        isLive = cleanUrl.contains(".m3u8") || cleanUrl.contains("live"),
        origin = origin.ifBlank { null },
        referer = referer.ifBlank { null },
        cookie = cookie.ifBlank { null },
        userAgent = userAgent.ifBlank { null },
        drmKey = drmKey.ifBlank { null },
        drmScheme = scheme.ifBlank { null }
      )
      onPlayStream(stream)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
    ) {
      // Top App Bar (Styled after Screenshot 1: terracotta/warm header with back button)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.horizontalGradient(
              colors = listOf(Color(0xFF9E4738), Color(0xFFB85949), Color(0xFF8D3E32))
            )
          )
          .padding(horizontal = 8.dp, vertical = 10.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          IconButton(onClick = { onOpenPresetCatalog() }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Catalog",
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Media3 Simple",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.weight(1f))

          // Quick Preset Catalog Button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0x33000000))
              .clickable { onOpenPresetCatalog() }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Tv,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "قنوات TOD",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Scrollable form fields matching Screenshot 1 exactly
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(scrollState)
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. TITLE
        FormInputField(
          label = "TITLE",
          value = title,
          onValueChange = { title = it },
          placeholder = "e.g. Premier League or جوهور دار التعظيم"
        )

        // 2. URL with Paste button
        FormInputField(
          label = "URL",
          value = url,
          onValueChange = { url = it },
          placeholder = "https://... (m3u8, mpd, mp4)",
          keyboardType = KeyboardType.Uri,
          trailingAction = {
            IconButton(
              onClick = {
                val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipManager.primaryClip
                if (clip != null && clip.itemCount > 0) {
                  val text = clip.getItemAt(0).text?.toString()
                  if (!text.isNullOrBlank()) {
                    url = text.trim()
                  }
                }
              }
            ) {
              Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = TodCyan, modifier = Modifier.size(20.dp))
            }
          }
        )

        // 3. Origin
        FormInputField(
          label = "Origin",
          value = origin,
          onValueChange = { origin = it },
          placeholder = "e.g. https://domain.com"
        )

        // 4. Referer
        FormInputField(
          label = "Referer",
          value = referer,
          onValueChange = { referer = it },
          placeholder = "e.g. https://domain.com/"
        )

        // 5. Cookie
        FormInputField(
          label = "Cookie",
          value = cookie,
          onValueChange = { cookie = it },
          placeholder = "Session or auth cookie"
        )

        // 6. DRM Key
        FormInputField(
          label = "DRM Key",
          value = drmKey,
          onValueChange = { drmKey = it },
          placeholder = "ClearKey hex or Widevine license URL"
        )

        // 7. User-Agent
        FormInputField(
          label = "User-Agent",
          value = userAgent,
          onValueChange = { userAgent = it },
          placeholder = "Custom User-Agent header",
          singleLine = false,
          maxLines = 3
        )

        // 8. Enter scheme
        Column {
          FormInputField(
            label = "Enter scheme",
            value = scheme,
            onValueChange = { scheme = it },
            placeholder = "widevine, playready, or clearkey"
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Make sure Scheme only contains (widevine, playready, clearkey)",
            color = Color(0xFF9E9E9E),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // PRIMARY PLAY BUTTON (Prominent terracotta/amber button matching Screenshot 1)
        Button(
          onClick = handlePlay,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(playPulseScale),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFA55B4B)
          )
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "PLAY",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sample Presets matching Screenshot 1:
        // 1. ENTER CLEARKEY SAMPLE
        SamplePresetButton(
          label = "ENTER CLEARKEY SAMPLE",
          onClick = {
            title = "ClearKey DRM Test Stream"
            url = "https://storage.googleapis.com/shaka-demo-assets/angel-one-clearkey/dash.mpd"
            scheme = "clearkey"
            drmKey = "https://cwip-shaka-drm.appspot.com/clearkey?drmkey=clearkey"
          }
        )

        // 2. ENTER WIDEVINE SAMPLE
        SamplePresetButton(
          label = "ENTER WIDEVINE SAMPLE",
          onClick = {
            title = "Widevine Modular DRM Sample"
            url = "https://storage.googleapis.com/shaka-demo-assets/angel-one-widevine/dash.mpd"
            scheme = "widevine"
            drmKey = "https://cwip-shaka-drm.appspot.com/no_auth"
          }
        )

        // 3. ENTER MP4 SAMPLE
        SamplePresetButton(
          label = "ENTER MP4 SAMPLE",
          onClick = {
            title = "Big Buck Bunny MP4"
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            scheme = ""
            drmKey = ""
          }
        )

        // 4. ENTER HLS LIVE SAMPLE (bonus for live sport testing)
        SamplePresetButton(
          label = "ENTER HLS LIVE (TOD SPORTS)",
          onClick = {
            title = "جوهور دار التعظيم ضد بوريراك يونايتد"
            url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            scheme = ""
            drmKey = ""
          }
        )

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormInputField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  keyboardType: KeyboardType = KeyboardType.Text,
  singleLine: Boolean = true,
  maxLines: Int = 1,
  trailingAction: (@Composable () -> Unit)? = null
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = label,
      color = Color(0xFF6B8299),
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 0.5.sp,
      modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
    )

    TextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(6.dp)),
      placeholder = {
        Text(placeholder, color = Color(0xFF5A6E82), fontSize = 13.sp)
      },
      trailingIcon = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (value.isNotEmpty()) {
            IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF8B9BAE), modifier = Modifier.size(16.dp))
            }
          }
          trailingAction?.invoke()
        }
      },
      singleLine = singleLine,
      maxLines = maxLines,
      keyboardOptions = KeyboardOptions(
        keyboardType = keyboardType,
        imeAction = ImeAction.Next
      ),
      colors = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFF131B2A),
        unfocusedContainerColor = Color(0xFF111724),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color(0xFFE2E8F0),
        focusedIndicatorColor = Color(0xFFA55B4B),
        unfocusedIndicatorColor = Color(0xFF2C3E50),
        cursorColor = Color(0xFFA55B4B)
      )
    )
  }
}

@Composable
private fun SamplePresetButton(
  label: String,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp),
    shape = RoundedCornerShape(6.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = Color(0xFFC7CDD3)
    )
  ) {
    Text(
      text = label,
      color = Color(0xFF232A32),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp
    )
  }
}
