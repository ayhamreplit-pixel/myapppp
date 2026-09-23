package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AudioTrackOption
import com.example.model.VideoQualityTrack
import com.example.ui.theme.TodAmberYellow
import com.example.ui.theme.TodGold

enum class TodSettingsTab {
  QUALITY,
  AUDIO
}

/**
 * Robust, flicker-free Stream Settings Modal (Quality & Audio).
 * Uses a true Android Dialog with strict touch containment so clicks on tabs
 * and options never accidentally dismiss the modal or leak to the video player.
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

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false
    )
  ) {
    BackHandler { onDismiss() }

    // Fullscreen scrim: Only clicking this outer background dismisses the modal
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(Color(0xCC000000))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss
        ),
      contentAlignment = Alignment.Center
    ) {
      // Modal Card: Traps all internal clicks so user interaction never dismisses it accidentally
      Box(
        modifier = Modifier
          .widthIn(min = 320.dp, max = 500.dp)
          .fillMaxWidth(0.92f)
          .clip(RoundedCornerShape(24.dp))
          .background(Color(0xF5131622))
          .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { /* Intercept clicks inside dialog card */ }
          .padding(20.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // ==========================================
          // 1. Dialog Header: Close Button + Tabs
          // ==========================================
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Close Button
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x28FFFFFF))
                .border(0.5.dp, Color(0x33FFFFFF), CircleShape)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null,
                  onClick = onDismiss
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "إغلاق",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            // Segmented Tabs: Quality & Audio
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x22FFFFFF))
                .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Quality Tab Button
              val isQualityActive = currentTab == TodSettingsTab.QUALITY
              val qualBg by animateColorAsState(
                targetValue = if (isQualityActive) TodGold else Color.Transparent,
                label = "qualTabBg"
              )
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(qualBg)
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                  ) { currentTab = TodSettingsTab.QUALITY }
                  .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.VideoSettings,
                  contentDescription = null,
                  tint = if (isQualityActive) Color.Black else Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "الجودة",
                  color = if (isQualityActive) Color.Black else Color.White,
                  fontSize = 13.5.sp,
                  fontWeight = if (isQualityActive) FontWeight.Black else FontWeight.Medium
                )
              }

              // Audio Tab Button
              val isAudioActive = currentTab == TodSettingsTab.AUDIO
              val audioBg by animateColorAsState(
                targetValue = if (isAudioActive) TodGold else Color.Transparent,
                label = "audioTabBg"
              )
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(audioBg)
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                  ) { currentTab = TodSettingsTab.AUDIO }
                  .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.VolumeUp,
                  contentDescription = null,
                  tint = if (isAudioActive) Color.Black else Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "الصوت",
                  color = if (isAudioActive) Color.Black else Color.White,
                  fontSize = 13.5.sp,
                  fontWeight = if (isAudioActive) FontWeight.Black else FontWeight.Medium
                )
              }
            }

            // Right Balance Spacer
            Spacer(modifier = Modifier.size(40.dp))
          }

          Spacer(modifier = Modifier.height(18.dp))

          // ==========================================
          // 2. Options List Body
          // ==========================================
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (currentTab == TodSettingsTab.QUALITY) {
              // Quality Track Options
              val displayQualities = if (qualities.isNotEmpty()) {
                qualities
              } else {
                listOf(
                  VideoQualityTrack(id = "auto", label = "تلقائي (Auto) - جودة متكيفة", isAuto = true),
                  VideoQualityTrack(id = "1080", label = "1080p FHD (فائقة الوضوح)", width = 1920, height = 1080, bitrate = 6_000_000),
                  VideoQualityTrack(id = "720", label = "720p HD (عالية الدقة)", width = 1280, height = 720, bitrate = 3_000_000),
                  VideoQualityTrack(id = "480", label = "480p SD (جودة قياسية متوازنة)", width = 854, height = 480, bitrate = 1_500_000),
                  VideoQualityTrack(id = "360", label = "360p (وضع توفير البيانات)", width = 640, height = 360, bitrate = 800_000)
                )
              }

              displayQualities.forEach { quality ->
                val isSelected = (selectedQuality == null && quality.isAuto) ||
                  (selectedQuality?.id == quality.id)

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0x33FDB913) else Color(0x18FFFFFF))
                    .border(
                      width = if (isSelected) 1.5.dp else 0.5.dp,
                      color = if (isSelected) TodAmberYellow else Color(0x24FFFFFF),
                      shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                      onSelectQuality(quality)
                      onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) TodGold else Color(0x22FFFFFF)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.HighQuality,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                      )
                    }

                    Column {
                      Text(
                        text = if (quality.isAuto) "تلقائي (Auto) - جودة ذكية متكيفة" else quality.label,
                        color = if (isSelected) TodGold else Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                      )
                      val subInfo = if (quality.isAuto) "يتكيف تلقائياً مع سرعة الإنترنت لمنع التقطيع"
                      else if (quality.bitrate > 0) "${quality.bitrate / 1000} Kbps معدل البث • ${quality.height}p"
                      else "${quality.height}p"
                      Text(
                        text = subInfo,
                        color = Color(0xFF8E8E93),
                        fontSize = 11.5.sp
                      )
                    }
                  }

                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "محدد",
                      tint = TodAmberYellow,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
              }
            } else {
              // Audio Track Options
              val displayAudio = if (audioTracks.isNotEmpty()) {
                audioTracks
              } else {
                listOf(
                  AudioTrackOption(id = "ar", language = "ar", label = "العربية - تعليق عربي رئيسي", channels = 2),
                  AudioTrackOption(id = "en", language = "en", label = "English - التعليق الإنجليزي", channels = 2),
                  AudioTrackOption(id = "ambient", language = "ambient", label = "صوت الملعب والجماهير (Ambient)", channels = 2)
                )
              }

              displayAudio.forEach { track ->
                val isSelected = (selectedAudio?.id == track.id) ||
                  (selectedAudio == null && track.language == "ar")

                val arabicLabel = when {
                  track.label.contains("Arab", ignoreCase = true) || track.language == "ar" -> "العربية - المعلق الأول"
                  track.label.contains("Eng", ignoreCase = true) || track.language == "en" -> "English - التعليق الإنجليزي"
                  track.label.contains("ambient", ignoreCase = true) -> "صوت الملعب والجماهير"
                  else -> track.label
                }

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0x33FDB913) else Color(0x18FFFFFF))
                    .border(
                      width = if (isSelected) 1.5.dp else 0.5.dp,
                      color = if (isSelected) TodAmberYellow else Color(0x24FFFFFF),
                      shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                      onSelectAudio(track)
                      onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) TodGold else Color(0x22FFFFFF)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                      )
                    }

                    Column {
                      Text(
                        text = arabicLabel,
                        color = if (isSelected) TodGold else Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                      )
                      Text(
                        text = if (track.channels > 2) "Dolby Digital 5.1 محيطي" else "صوت نقي ستيريو ثنائي",
                        color = Color(0xFF8E8E93),
                        fontSize = 11.5.sp
                      )
                    }
                  }

                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "محدد",
                      tint = TodAmberYellow,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

