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
import androidx.compose.material.icons.filled.Speed
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
    containerColor = DarkSurface,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 32.dp)
    ) {
      Text(
        text = "Broadcast Video Quality",
        color = DarkTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Select preferred resolution or let TOD adaptive stream optimize",
        color = DarkTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
      )

      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(qualities) { quality ->
          val isSelected = (selectedQuality == null && quality.isAuto) ||
            (selectedQuality?.id == quality.id)

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) TodAmberYellow.copy(alpha = 0.18f) else DarkSurfaceElevated)
              .clickable {
                onSelect(quality)
                onDismiss()
              }
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = quality.label,
                color = if (isSelected) TodAmberYellow else DarkTextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp
              )
              if (quality.isAuto) {
                Text(
                  text = "Dynamically adjusts to your network speed",
                  color = DarkTextSecondary,
                  fontSize = 12.sp
                )
              } else if (quality.bitrate > 0) {
                Text(
                  text = "${quality.bitrate / 1000} Kbps",
                  color = DarkTextSecondary,
                  fontSize = 12.sp
                )
              }
            }
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = TodAmberYellow,
                modifier = Modifier.size(20.dp)
              )
            }
          }
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
    containerColor = DarkSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 32.dp)
    ) {
      Text(
        text = "Commentary & Audio Language",
        color = DarkTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Select commentary broadcast or stadium sound channel",
        color = DarkTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
      )

      if (audioTracks.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("Standard Stereo Commentary (Default)", color = DarkTextSecondary)
        }
      } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(audioTracks) { track ->
            val isSelected = selectedTrack?.id == track.id || (selectedTrack == null && track.isSelected)

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) TodAmberYellow.copy(alpha = 0.18f) else DarkSurfaceElevated)
                .clickable {
                  onSelect(track)
                  onDismiss()
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(if (isSelected) TodAmberYellow.copy(alpha = 0.25f) else Color(0xFF1E2638), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (isSelected) TodAmberYellow else DarkTextSecondary,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = track.label,
                    color = if (isSelected) TodAmberYellow else DarkTextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "${if (track.channels > 2) "Dolby 5.1 / Surround" else "Stereo"} • ${track.language.uppercase()}",
                    color = DarkTextSecondary,
                    fontSize = 12.sp
                  )
                }
              }
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = TodAmberYellow,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
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
    containerColor = DarkSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 32.dp)
    ) {
      Text(
        text = "Subtitles & Captions",
        color = DarkTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Closed captions for live commentary and dialogues",
        color = DarkTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
      )

      // Option: Off
      val isOff = selectedSubtitle == null
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(if (isOff) TodAmberYellow.copy(alpha = 0.18f) else DarkSurfaceElevated)
          .clickable {
            onSelect(null)
            onDismiss()
          }
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Off",
          color = if (isOff) TodAmberYellow else DarkTextPrimary,
          fontWeight = if (isOff) FontWeight.Bold else FontWeight.Medium,
          fontSize = 15.sp
        )
        if (isOff) {
          Icon(Icons.Default.Check, "Selected", tint = TodAmberYellow, modifier = Modifier.size(20.dp))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(subtitles) { sub ->
          val isSelected = selectedSubtitle?.id == sub.id

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) TodAmberYellow.copy(alpha = 0.18f) else DarkSurfaceElevated)
              .clickable {
                onSelect(sub)
                onDismiss()
              }
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = sub.label,
              color = if (isSelected) TodAmberYellow else DarkTextPrimary,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 15.sp
            )
            if (isSelected) {
              Icon(Icons.Default.Check, "Selected", tint = TodAmberYellow, modifier = Modifier.size(20.dp))
            }
          }
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

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = DarkSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 36.dp)
    ) {
      Text(
        text = "Player Settings",
        color = DarkTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Playback speed
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Speed, contentDescription = null, tint = TodAmberYellow, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Playback Speed", color = DarkTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        speeds.forEach { speed ->
          val isSelected = currentSpeed == speed
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) TodAmberYellow else DarkSurfaceElevated)
              .clickable { onSpeedChange(speed) }
              .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (speed == 1.0f) "Normal" else "${speed}x",
              color = if (isSelected) Color.Black else DarkTextPrimary,
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
              fontSize = 12.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Aspect Ratio Mode
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AspectRatio, contentDescription = null, tint = TodAmberYellow, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Aspect Ratio / Resize Mode", color = DarkTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AspectRatioMode.values().forEach { mode ->
          val isSelected = currentAspect == mode
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) TodAmberYellow else DarkSurfaceElevated)
              .clickable { onAspectChange(mode) }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = mode.label,
              color = if (isSelected) Color.Black else DarkTextPrimary,
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
              fontSize = 11.sp,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Audio Boost
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = TodCyan, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Audio Boost (Loudness Enhancer)", color = DarkTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        Text(
          text = "+${audioBoostPercent}%",
          color = TodCyan,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
      Slider(
        value = audioBoostPercent.toFloat(),
        onValueChange = { onAudioBoostChange(it.toInt()) },
        valueRange = 0f..100f,
        colors = SliderDefaults.colors(
          thumbColor = TodCyan,
          activeTrackColor = TodCyan,
          inactiveTrackColor = DarkSurfaceElevated
        )
      )
    }
  }
}
