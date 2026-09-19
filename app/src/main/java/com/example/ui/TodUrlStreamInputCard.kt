package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStream
import com.example.model.StreamFormat
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.TodCyan
import com.example.ui.theme.TodCyanGlow
import com.example.ui.theme.TodLiveRed
import com.example.ui.theme.TodPink
import com.example.ui.theme.TodViolet

/**
 * Premium TOD-themed URL Input Box with neon gradients, quick paste, preset streams,
 * format detection (HLS/DASH/MP4), and live DVR toggle.
 */
@Composable
fun TodUrlStreamInputCard(
  onPlayStream: (BroadcastStream) -> Unit,
  modifier: Modifier = Modifier
) {
  var urlText by remember { mutableStateOf("") }
  var streamTitle by remember { mutableStateOf("") }
  var selectedFormat by remember { mutableStateOf(StreamFormat.AUTO) }
  var isLiveStream by remember { mutableStateOf(true) }
  var isExpanded by remember { mutableStateOf(false) }

  val clipboardManager = LocalClipboardManager.current
  val focusManager = LocalFocusManager.current

  val infiniteTransition = rememberInfiniteTransition(label = "todGlow")
  val pulseLiveScale by infiniteTransition.animateFloat(
    initialValue = 0.92f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseLive"
  )

  // Popular test streams for 1-tap testing
  val quickPresets = remember {
    listOf(
      Preset("Mux HLS 1080p", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", StreamFormat.HLS, true),
      Preset("Akamai Live HLS", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8", StreamFormat.HLS, true),
      Preset("Sintel DASH", "https://bitdash-a.akamaihd.net/content/sintel/sintel.mpd", StreamFormat.DASH, false),
      Preset("Apple BipBop HLS", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8", StreamFormat.HLS, false),
      Preset("Big Buck MP4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", StreamFormat.PROGRESSIVE, false)
    )
  }

  fun submitPlay() {
    if (urlText.isBlank()) return
    focusManager.clearFocus()
    val finalTitle = if (streamTitle.isNotBlank()) streamTitle.trim() else "Custom Broadcast"
    val stream = BroadcastStream(
      id = "custom_${System.currentTimeMillis()}",
      title = finalTitle,
      subtitle = if (isLiveStream) "Live Broadcast Feed (${selectedFormat.extensionBadge})" else "VOD Playback (${selectedFormat.extensionBadge})",
      category = "Custom Stream",
      streamUrl = urlText.trim(),
      format = selectedFormat,
      isLive = isLiveStream,
      channelNumber = "CUSTOM",
      tournamentOrLeague = "Custom Stream",
      resolutionLabel = "Direct Stream",
      description = "Playing user-provided URL stream directly with ExoPlayer hardware decoder."
    )
    onPlayStream(stream)
  }

  // Futuristic TOD Glowing Card
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
          colors = listOf(TodCyan, TodViolet, Color(0xFF00B4D8)),
          start = Offset(0f, 0f),
          end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = RoundedCornerShape(16.dp)
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF131B2C),
              DarkSurface,
              Color(0xFF0B0F19)
            )
          )
        )
        .padding(16.dp)
    ) {
      // Header with TOD Badge & title
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(TodCyan, TodViolet)
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Link,
              contentDescription = "Stream URL",
              tint = Color(0xFF00222B),
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "تشغيل رابط مخصص",
                color = DarkTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              // TOD Neon Live / Custom Pill
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFF002F3A))
                  .border(0.8.dp, TodCyan, RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "TOD DIRECT",
                  color = TodCyan,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
            Text(
              text = "HLS (.m3u8), DASH (.mpd), MP4, WebM",
              color = DarkTextSecondary,
              fontSize = 11.sp
            )
          }
        }

        // Expand/Collapse Advanced Options button
        IconButton(
          onClick = { isExpanded = !isExpanded },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = "Expand options",
            tint = TodCyan,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // URL Input Field with Paste & Clear actions
      OutlinedTextField(
        value = urlText,
        onValueChange = { newUrl ->
          urlText = newUrl
          // Auto detect format & live state
          if (newUrl.contains(".m3u8", ignoreCase = true)) {
            selectedFormat = StreamFormat.HLS
            isLiveStream = true
          } else if (newUrl.contains(".mpd", ignoreCase = true)) {
            selectedFormat = StreamFormat.DASH
          } else if (newUrl.contains(".mp4", ignoreCase = true) || newUrl.contains(".webm", ignoreCase = true)) {
            selectedFormat = StreamFormat.PROGRESSIVE
            isLiveStream = false
          }
        },
        placeholder = {
          Text(
            text = "ضع رابط البث هنا (مثال: https://.../stream.m3u8)",
            color = DarkTextTertiary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Cast,
            contentDescription = null,
            tint = if (urlText.isNotBlank()) TodCyan else DarkTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (urlText.isNotBlank()) {
              IconButton(onClick = { urlText = "" }, modifier = Modifier.size(32.dp)) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear text",
                  tint = DarkTextSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            // Quick Paste button
            Box(
              modifier = Modifier
                .padding(end = 6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSurfaceElevated)
                .clickable {
                  val clip = clipboardManager.getText()?.text
                  if (!clip.isNullOrBlank()) {
                    urlText = clip.trim()
                    if (urlText.contains(".m3u8", ignoreCase = true)) selectedFormat = StreamFormat.HLS
                    if (urlText.contains(".mpd", ignoreCase = true)) selectedFormat = StreamFormat.DASH
                  }
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = "Paste",
                  tint = TodCyan,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "لصق",
                  color = TodCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Uri,
          imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(
          onGo = { submitPlay() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF0C111C),
          unfocusedContainerColor = Color(0xFF0C111C),
          focusedBorderColor = TodCyan,
          unfocusedBorderColor = DarkSurfaceBorder,
          focusedTextColor = DarkTextPrimary,
          unfocusedTextColor = DarkTextPrimary,
          cursorColor = TodCyan
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Main PLAY Button with Animated Cyan/Violet Gradient & Arrow
      Button(
        onClick = { submitPlay() },
        enabled = urlText.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Transparent,
          disabledContainerColor = Color(0xFF192233)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .then(
            if (urlText.isNotBlank()) {
              Modifier.background(
                brush = Brush.horizontalGradient(
                  colors = listOf(TodCyan, Color(0xFF00B4D8), TodViolet)
                ),
                shape = RoundedCornerShape(12.dp)
              )
            } else {
              Modifier.background(Color(0xFF1E283A), RoundedCornerShape(12.dp))
            }
          )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = if (urlText.isNotBlank()) Color(0xFF00222B) else DarkTextTertiary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "تشغيل البث الآن",
            color = if (urlText.isNotBlank()) Color(0xFF00222B) else DarkTextTertiary,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      // Quick Test Stream Chips (Row)
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "روابط تجريبية جاهزة للاختبار بنقرة واحدة:",
        color = DarkTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(quickPresets) { preset ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF141D2D))
              .border(0.8.dp, Color(0xFF233047), RoundedCornerShape(8.dp))
              .clickable {
                urlText = preset.url
                streamTitle = preset.name
                selectedFormat = preset.format
                isLiveStream = preset.isLive
              }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (preset.isLive) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(TodLiveRed)
                )
                Spacer(modifier = Modifier.width(5.dp))
              }
              Text(
                text = preset.name,
                color = TodCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // Expandable Advanced Options (Title, Format Protocol, Live Mode)
      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(tween(250)) + fadeIn(),
        exit = shrinkVertically(tween(200)) + fadeOut()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
        ) {
          // Custom Stream Title
          OutlinedTextField(
            value = streamTitle,
            onValueChange = { streamTitle = it },
            placeholder = { Text("عنوان مخصص للبث (اختياري)", color = DarkTextTertiary, fontSize = 12.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF0C111C),
              unfocusedContainerColor = Color(0xFF0C111C),
              focusedBorderColor = TodCyan,
              unfocusedBorderColor = DarkSurfaceBorder,
              focusedTextColor = DarkTextPrimary,
              unfocusedTextColor = DarkTextPrimary
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Protocol selection pills
          Text(
            text = "نوع البروتوكول (Format):",
            color = DarkTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(StreamFormat.AUTO, StreamFormat.HLS, StreamFormat.DASH, StreamFormat.PROGRESSIVE).forEach { fmt ->
              val isSelected = selectedFormat == fmt
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) TodCyan else Color(0xFF141D2D))
                  .border(
                    1.dp,
                    if (isSelected) TodCyan else DarkSurfaceBorder,
                    RoundedCornerShape(8.dp)
                  )
                  .clickable { selectedFormat = fmt }
                  .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = fmt.extensionBadge,
                  color = if (isSelected) Color(0xFF00222B) else DarkTextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Live DVR Toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF141D2D))
              .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .scale(if (isLiveStream) pulseLiveScale else 1f)
                  .clip(CircleShape)
                  .background(if (isLiveStream) TodLiveRed else Color.Gray)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = if (isLiveStream) "بث مباشر (Live DVR مفعّل)" else "فيديو عند الطلب (VOD)",
                  color = DarkTextPrimary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = if (isLiveStream) "إمكانية إرجاع البث المباشر وتتبع أحدث نقطة" else "شريط تقديم وتأخير عادي",
                  color = DarkTextSecondary,
                  fontSize = 10.sp
                )
              }
            }

            Switch(
              checked = isLiveStream,
              onCheckedChange = { isLiveStream = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TodLiveRed,
                uncheckedThumbColor = DarkTextSecondary,
                uncheckedTrackColor = Color(0xFF222C42)
              )
            )
          }
        }
      }
    }
  }
}

private data class Preset(
  val name: String,
  val url: String,
  val format: StreamFormat,
  val isLive: Boolean
)
