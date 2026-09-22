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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioMode
import com.example.model.AudioTrackOption
import com.example.model.SubtitleTrackOption
import com.example.model.VideoQualityTrack
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodAmberYellow
import com.example.ui.theme.TodCyan
import com.example.ui.theme.TodCyanGlow
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQualitySheet(
  qualities: List<VideoQualityTrack>,
  selectedQuality: VideoQualityTrack?,
  onSelect: (VideoQualityTrack) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF14141E),
    dragHandle = { IosGrabber() },
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(bottom = 34.dp)
    ) {
      Text(
        text = "جودة ودقة الفيديو (Video Quality)",
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "اختر الدقة المفضلة أو دع البث التكيفي يحدد الجودة المثلى لشبكتك",
        color = Color(0xFF8E8E93),
        fontSize = 12.5.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
      )

      IosListGroup {
        qualities.forEachIndexed { index, quality ->
          val isSelected = (selectedQuality == null && quality.isAuto) ||
            (selectedQuality?.id == quality.id)

          IosListRow(
            title = quality.label,
            subtitle = if (quality.isAuto) "تعديل ديناميكي تلقائي يمنع التقطيع"
            else if (quality.bitrate > 0) "${quality.bitrate / 1000} Kbps معدل البث"
            else null,
            iconBadge = {
              IosIconBadge(
                icon = Icons.Default.VideoSettings,
                background = if (isSelected) IosBadgeColors.Gold else IosBadgeColors.Slate,
                tint = if (isSelected) Color.Black else Color.White
              )
            },
            trailing = if (isSelected) {
              {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = TodGold,
                  modifier = Modifier.size(20.dp)
                )
              }
            } else null,
            showChevron = false,
            showDivider = index < qualities.size - 1,
            onClick = {
              onSelect(quality)
              onDismiss()
            }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTrackSheet(
  audioTracks: List<AudioTrackOption>,
  selectedTrack: AudioTrackOption?,
  onSelect: (AudioTrackOption) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF14141E),
    dragHandle = { IosGrabber() },
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(bottom = 34.dp)
    ) {
      Text(
        text = "المسار الصوتي والتعليق (Audio Tracks)",
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "اختر القناة الصوتية المفضلة (صوت المعلق الأول / الثاني أو صوت الملعب)",
        color = Color(0xFF8E8E93),
        fontSize = 12.5.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
      )

      if (audioTracks.isEmpty()) {
        IosListGroup {
          IosListRow(
            title = "المسار الافتراضي (Standard Stereo)",
            subtitle = "القناة الصوتية المضمنة للبث",
            iconBadge = {
              IosIconBadge(icon = Icons.AutoMirrored.Filled.VolumeUp, background = IosBadgeColors.Blue)
            },
            trailing = {
              Icon(Icons.Default.Check, contentDescription = null, tint = TodGold, modifier = Modifier.size(20.dp))
            },
            showChevron = false,
            showDivider = false
          )
        }
      } else {
        IosListGroup {
          audioTracks.forEachIndexed { index, track ->
            val isSelected = selectedTrack?.id == track.id || (selectedTrack == null && track.isSelected)
            IosListRow(
              title = track.label,
              subtitle = "${if (track.channels > 2) "Dolby 5.1 المحيطي" else "Stereo"} • ${track.language.uppercase()}",
              iconBadge = {
                IosIconBadge(
                  icon = Icons.AutoMirrored.Filled.VolumeUp,
                  background = if (isSelected) IosBadgeColors.Indigo else IosBadgeColors.Slate
                )
              },
              trailing = if (isSelected) {
                {
                  Icon(Icons.Default.Check, contentDescription = "Selected", tint = TodGold, modifier = Modifier.size(20.dp))
                }
              } else null,
              showChevron = false,
              showDivider = index < audioTracks.size - 1,
              onClick = {
                onSelect(track)
                onDismiss()
              }
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleTrackSheet(
  subtitles: List<SubtitleTrackOption>,
  selectedSubtitle: SubtitleTrackOption?,
  onSelect: (SubtitleTrackOption?) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF14141E),
    dragHandle = { IosGrabber() },
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(bottom = 34.dp)
    ) {
      Text(
        text = "الترجمة والنصوص (Subtitles)",
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "نصوص الترجمة المصاحبة والتعليق النصي",
        color = Color(0xFF8E8E93),
        fontSize = 12.5.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
      )

      val isOff = selectedSubtitle == null
      IosListGroup {
        // Option: Off
        IosListRow(
          title = "إيقاف الترجمة (Off)",
          subtitle = "عدم عرض أي نصوص على الشاشة",
          iconBadge = {
            IosIconBadge(icon = Icons.Default.Subtitles, background = if (isOff) IosBadgeColors.Red else IosBadgeColors.Slate)
          },
          trailing = if (isOff) {
            { Icon(Icons.Default.Check, contentDescription = "Selected", tint = TodGold, modifier = Modifier.size(20.dp)) }
          } else null,
          showChevron = false,
          showDivider = subtitles.isNotEmpty(),
          onClick = {
            onSelect(null)
            onDismiss()
          }
        )

        subtitles.forEachIndexed { index, sub ->
          val isSelected = selectedSubtitle?.id == sub.id
          IosListRow(
            title = sub.label,
            subtitle = sub.language.uppercase(),
            iconBadge = {
              IosIconBadge(icon = Icons.Default.Subtitles, background = if (isSelected) IosBadgeColors.Gold else IosBadgeColors.Slate)
            },
            trailing = if (isSelected) {
              { Icon(Icons.Default.Check, contentDescription = "Selected", tint = TodGold, modifier = Modifier.size(20.dp)) }
            } else null,
            showChevron = false,
            showDivider = index < subtitles.size - 1,
            onClick = {
              onSelect(sub)
              onDismiss()
            }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsSheet(
  currentSpeed: Float,
  currentAspect: AspectRatioMode,
  audioBoostPercent: Int,
  onSpeedChange: (Float) -> Unit,
  onAspectChange: (AspectRatioMode) -> Unit,
  onAudioBoostChange: (Int) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()
  val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
  val speedLabels = listOf("0.5x", "0.75x", "عادي 1x", "1.25x", "1.5x", "2.0x")
  val currentSpeedIndex = speeds.indexOf(currentSpeed).takeIf { it >= 0 } ?: 2

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF14141E),
    dragHandle = { IosGrabber() },
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(bottom = 36.dp)
    ) {
      Text(
        text = "إعدادات المشغل (Player Controls)",
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "تخصيص سرعة التشغيل، أبعاد الشاشة ومضخم الصوت",
        color = Color(0xFF8E8E93),
        fontSize = 12.5.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
      )

      // 1. Playback Speed Section with iOS Segmented Control
      IosSectionHeader(title = "سرعة التشغيل (Playback Speed)")
      IosSegmentedControl(
        items = speedLabels,
        selectedIndex = currentSpeedIndex,
        onSelect = { index -> onSpeedChange(speeds[index]) }
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 2. Aspect Ratio Section with iOS Group
      val aspectModes = AspectRatioMode.values()
      val aspectLabels = aspectModes.map { it.label }
      val currentAspectIndex = aspectModes.indexOf(currentAspect).takeIf { it >= 0 } ?: 0

      IosSectionHeader(title = "أبعاد الشاشة (Aspect Ratio Mode)")
      IosSegmentedControl(
        items = aspectLabels,
        selectedIndex = currentAspectIndex,
        onSelect = { index -> onAspectChange(aspectModes[index]) }
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 3. Audio Boost with iOS Group
      IosSectionHeader(title = "مضخم الصوت وموازنة التعليق")
      IosListGroup {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              IosIconBadge(
                icon = Icons.Default.GraphicEq,
                background = IosBadgeColors.Teal,
                modifier = Modifier.size(28.dp)
              )
              Text("مضخم الصوت (Loudness Boost)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
              text = "+${audioBoostPercent}%",
              color = TodGold,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Slider(
            value = audioBoostPercent.toFloat(),
            onValueChange = { onAudioBoostChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
              thumbColor = TodGold,
              activeTrackColor = TodGold,
              inactiveTrackColor = Color(0x33FFFFFF)
            )
          )
        }
      }
    }
  }
}
