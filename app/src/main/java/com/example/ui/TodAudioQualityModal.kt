package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrackOption
import com.example.model.VideoQualityTrack

val TodAmberYellow = Color(0xFFFFB800)

enum class TodSettingsTab {
  QUALITY,
  AUDIO
}

/**
 * TOD Audio & Quality Selector Modal.
 * Exactly replicates Screenshots 10 & 11 from the user's upload.
 */
@Composable
fun TodAudioQualityModal(
  initialTab: TodSettingsTab = TodSettingsTab.QUALITY,
  qualities: List<VideoQualityTrack>,
  selectedQuality: VideoQualityTrack?,
  audioTracks: List<AudioTrackOption>,
  selectedAudio: AudioTrackOption?,
  onSelectQuality: (VideoQualityTrack) -> Unit,
  onSelectAudio: (AudioTrackOption) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentTab by remember { mutableStateOf(initialTab) }

  // Dimmed translucent backdrop covering the video
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xD9000000))
      .clickable(onClick = onDismiss)
  ) {
    // Top Bar: Close 'X' on Left, Tabs in Center
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
      // Top Left: Close 'X' icon (Screenshot 8 / 10 / 11)
      Box(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .size(44.dp)
          .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
      ) {
        TodCloseX(tint = Color.White, size = 22.dp)
      }

      // Top Center: "الجودة" and "الصوت" Tabs (Screenshot 10 & 11)
      Row(
        modifier = Modifier.align(Alignment.Center),
        horizontalArrangement = Arrangement.spacedBy(36.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tab 1: الجودة (Quality)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable { currentTab = TodSettingsTab.QUALITY }
        ) {
          Text(
            text = "الجودة",
            color = if (currentTab == TodSettingsTab.QUALITY) Color.White else Color(0xFF888888),
            fontSize = 17.sp,
            fontWeight = if (currentTab == TodSettingsTab.QUALITY) FontWeight.Bold else FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(6.dp))
          // Amber active underline
          Box(
            modifier = Modifier
              .width(36.dp)
              .height(3.dp)
              .background(
                if (currentTab == TodSettingsTab.QUALITY) TodAmberYellow else Color.Transparent,
                RoundedCornerShape(2.dp)
              )
          )
        }

        // Tab 2: الصوت (Audio)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable { currentTab = TodSettingsTab.AUDIO }
        ) {
          Text(
            text = "الصوت",
            color = if (currentTab == TodSettingsTab.AUDIO) Color.White else Color(0xFF888888),
            fontSize = 17.sp,
            fontWeight = if (currentTab == TodSettingsTab.AUDIO) FontWeight.Bold else FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(6.dp))
          // Amber active underline
          Box(
            modifier = Modifier
              .width(36.dp)
              .height(3.dp)
              .background(
                if (currentTab == TodSettingsTab.AUDIO) TodAmberYellow else Color.Transparent,
                RoundedCornerShape(2.dp)
              )
          )
        }
      }

      // Right: Watermark badge
      Box(modifier = Modifier.align(Alignment.CenterEnd)) {
        TodWatermarkBadge(size = 36.dp)
      }
    }

    // Center Content: Selection List
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 80.dp),
      contentAlignment = Alignment.Center
    ) {
      if (currentTab == TodSettingsTab.QUALITY) {
        // Quality List (Screenshot 10: "قياسي", "1080p FHD", etc.)
        val displayQualities = if (qualities.isNotEmpty()) {
          qualities
        } else {
          listOf(
            VideoQualityTrack(id = "auto", label = "قياسي (Standard)", isAuto = true),
            VideoQualityTrack(id = "1080", label = "1080p FHD", width = 1920, height = 1080, bitrate = 6_000_000),
            VideoQualityTrack(id = "720", label = "720p HD", width = 1280, height = 720, bitrate = 3_000_000),
            VideoQualityTrack(id = "480", label = "480p SD", width = 854, height = 480, bitrate = 1_500_000)
          )
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
          displayQualities.forEach { quality ->
            val isSelected = (selectedQuality == null && quality.isAuto) ||
              (selectedQuality?.id == quality.id)

            Text(
              text = if (quality.isAuto) "قياسي" else quality.label,
              color = if (isSelected) TodAmberYellow else Color.White,
              fontSize = 20.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier
                .clickable {
                  onSelectQuality(quality)
                  onDismiss()
                }
                .padding(horizontal = 24.dp, vertical = 6.dp)
            )
          }
        }
      } else {
        // Audio List (Screenshot 11: "العربية", "الإنجليزية")
        val displayAudio = if (audioTracks.isNotEmpty()) {
          audioTracks
        } else {
          listOf(
            AudioTrackOption(id = "ar", language = "ar", label = "العربية", channels = 2),
            AudioTrackOption(id = "en", language = "en", label = "الإنجليزية", channels = 2)
          )
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
          displayAudio.forEach { track ->
            val isSelected = (selectedAudio?.id == track.id) ||
              (selectedAudio == null && track.language == "ar")

            val arabicLabel = when {
              track.label.contains("Arab", ignoreCase = true) || track.language == "ar" -> "العربية"
              track.label.contains("Eng", ignoreCase = true) || track.language == "en" -> "الإنجليزية"
              else -> track.label
            }

            Text(
              text = arabicLabel,
              color = if (isSelected) TodAmberYellow else Color.White,
              fontSize = 20.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier
                .clickable {
                  onSelectAudio(track)
                  onDismiss()
                }
                .padding(horizontal = 24.dp, vertical = 6.dp)
            )
          }
        }
      }
    }
  }
}
