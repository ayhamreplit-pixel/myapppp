package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.TodCyan

@Composable
fun CustomStreamDialog(
  onPlayStream: (BroadcastStream) -> Unit,
  onDismiss: () -> Unit
) {
  var url by remember { mutableStateOf("") }
  var title by remember { mutableStateOf("") }
  var selectedFormat by remember { mutableStateOf(StreamFormat.AUTO) }
  var isLive by remember { mutableStateOf(false) }

  val samplePresets = listOf(
    Triple("Mux HLS Test", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", StreamFormat.HLS),
    Triple("Akamai Sintel DASH", "https://bitdash-a.akamaihd.net/content/sintel/sintel.mpd", StreamFormat.DASH),
    Triple("Apple BipBop HLS", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8", StreamFormat.HLS),
    Triple("Big Buck Bunny MP4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", StreamFormat.PROGRESSIVE)
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurface,
    tonalElevation = 8.dp,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(
              androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(com.example.ui.theme.TodCyan, com.example.ui.theme.TodViolet)
              ),
              RoundedCornerShape(8.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Cast, contentDescription = null, tint = Color(0xFF00222B), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "تشغيل رابط بث مباشر أو فيديو",
          color = DarkTextPrimary,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "يدعم المشغّل روابط HLS (.m3u8), DASH (.mpd), MP4 مباشرة عبر محرك ExoPlayer.",
          color = DarkTextSecondary,
          fontSize = 12.sp,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        // Preset quick chips
        Text(
          text = "نماذج سريعة للتجربة:",
          color = DarkTextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.padding(bottom = 12.dp)
        ) {
          items(samplePresets) { preset ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSurfaceElevated)
                .clickable {
                  title = preset.first
                  url = preset.second
                  selectedFormat = preset.third
                  isLive = preset.first.contains("Live", ignoreCase = true)
                }
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Text(preset.first, color = TodCyan, fontSize = 11.sp)
            }
          }
        }

        OutlinedTextField(
          value = url,
          onValueChange = {
            url = it
            if (it.contains(".m3u8", ignoreCase = true)) selectedFormat = StreamFormat.HLS
            if (it.contains(".mpd", ignoreCase = true)) selectedFormat = StreamFormat.DASH
            if (it.contains(".ism", ignoreCase = true)) selectedFormat = StreamFormat.SMOOTH_STREAMING
          },
          label = { Text("Stream URL (.m3u8, .mpd, .mp4)") },
          placeholder = { Text("https://example.com/live/master.m3u8") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TodCyan,
            unfocusedBorderColor = DarkSurfaceBorder,
            focusedTextColor = DarkTextPrimary,
            unfocusedTextColor = DarkTextPrimary,
            focusedLabelColor = TodCyan,
            unfocusedLabelColor = DarkTextSecondary
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Broadcast Title (Optional)") },
          placeholder = { Text("Custom Live Feed") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TodCyan,
            unfocusedBorderColor = DarkSurfaceBorder,
            focusedTextColor = DarkTextPrimary,
            unfocusedTextColor = DarkTextPrimary,
            focusedLabelColor = TodCyan,
            unfocusedLabelColor = DarkTextSecondary
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Format Protocol:", color = DarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) TodCyan else DarkSurfaceElevated)
                .clickable { selectedFormat = fmt }
                .padding(vertical = 6.dp),
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
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (url.isNotBlank()) {
            val stream = BroadcastStream(
              id = "custom_${System.currentTimeMillis()}",
              title = if (title.isNotBlank()) title else "بث مباشر مخصص",
              subtitle = "رابط مخصص (${selectedFormat.extensionBadge})",
              category = "Custom Stream",
              streamUrl = url.trim(),
              format = selectedFormat,
              isLive = isLive || url.contains("live", ignoreCase = true) || selectedFormat == StreamFormat.HLS,
              channelNumber = "CUSTOM",
              tournamentOrLeague = "بث مخصص",
              resolutionLabel = "Direct Stream"
            )
            onPlayStream(stream)
            onDismiss()
          }
        },
        enabled = url.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = TodCyan, contentColor = Color(0xFF00222B))
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("تشغيل الآن", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء", color = DarkTextSecondary)
      }
    }
  )
}
