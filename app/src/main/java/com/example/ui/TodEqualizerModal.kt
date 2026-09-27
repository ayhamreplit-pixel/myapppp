package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodAmberYellow
import com.example.ui.theme.TodGold

enum class EqualizerPreset(val label: String, val bands: List<Float>, val bassBoost: Float, val icon: String) {
  SPORTS_COMMENTARY("⚽ تعليق رياضي", listOf(-2f, 1f, 6f, 4f, 2f), 20f, "⚽"),
  STADIUM("🏟️ أجواء الملعب", listOf(6f, 3f, 0f, 4f, 7f), 65f, "🏟️"),
  BASS_BOOST("🚀 تضخيم الباس", listOf(8f, 6f, 2f, 0f, -1f), 85f, "🚀"),
  VOCAL_CLARITY("🎵 صوت نقي", listOf(-4f, 2f, 7f, 5f, 1f), 10f, "🎵"),
  FLAT("🎚️ عادي / متوازن", listOf(0f, 0f, 0f, 0f, 0f), 0f, "🎚️"),
  CUSTOM("⚡ مخصص", listOf(0f, 0f, 0f, 0f, 0f), 30f, "⚡")
}

@Composable
fun TodEqualizerModal(
  onDismiss: () -> Unit,
  onApplyPreset: (EqualizerPreset, List<Float>, Float) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier
) {
  var selectedPreset by remember { mutableStateOf(EqualizerPreset.SPORTS_COMMENTARY) }
  var isEqEnabled by remember { mutableStateOf(true) }
  var bassBoostValue by remember { mutableFloatStateOf(selectedPreset.bassBoost) }

  // 5 Frequency bands in dB (-10dB to +10dB): 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
  val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
  val bandValues = remember {
    mutableStateListOf<Float>().apply {
      addAll(selectedPreset.bands)
    }
  }

  BackHandler { onDismiss() }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.75f))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onDismiss
      ),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(26.dp))
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFF1B2338), Color(0xFF101524), Color(0xFF090D18))
          )
        )
        .border(1.2.dp, Brush.verticalGradient(listOf(TodGold.copy(alpha = 0.6f), Color(0x33FFFFFF))), RoundedCornerShape(26.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {} // Consume inner click
        )
        .padding(20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color(0x33FFFFFF))
              .clickable { onDismiss() },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(20.dp))
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "معادل الصوت وBass Boost",
              color = Color.White,
              fontSize = 17.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = TodGold, modifier = Modifier.size(22.dp))
          }
        }

        // Enable Switch
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x18FFFFFF))
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Switch(
            checked = isEqEnabled,
            onCheckedChange = { isEqEnabled = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = TodGold,
              checkedTrackColor = TodGold.copy(alpha = 0.4f)
            )
          )
          Column(horizontalAlignment = Alignment.End) {
            Text("تفعيل تحسين الصوت الرياضي", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            Text("معالجة الصوت لتعزيز المعلق وأجواء الملعب", color = Color(0xAAFFFFFF), fontSize = 11.sp)
          }
        }

        // Presets Chips Row
        Text("الأنماط الرياضية الجاهزة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          EqualizerPreset.entries.forEach { preset ->
            val isSelected = selectedPreset == preset
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) TodGold else Color(0x22FFFFFF))
                .clickable {
                  selectedPreset = preset
                  if (preset != EqualizerPreset.CUSTOM) {
                    bandValues.clear()
                    bandValues.addAll(preset.bands)
                    bassBoostValue = preset.bassBoost
                  }
                  onApplyPreset(preset, bandValues.toList(), bassBoostValue)
                }
                .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
              Text(
                text = preset.label,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            }
          }
        }

        // Bass Boost Section
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x18FFFFFF))
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "${bassBoostValue.toInt()}%", color = TodGold, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(text = "مضخم الترددات المنخفضة (Bass Boost)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TodGold, modifier = Modifier.size(16.dp))
            }
          }
          Slider(
            value = bassBoostValue,
            onValueChange = {
              bassBoostValue = it
              selectedPreset = EqualizerPreset.CUSTOM
              onApplyPreset(EqualizerPreset.CUSTOM, bandValues.toList(), it)
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
              thumbColor = TodGold,
              activeTrackColor = TodGold,
              inactiveTrackColor = Color(0x33FFFFFF)
            )
          )
        }

        // 5-Band Equalizer Sliders
        Text("التحكم بترددات الصوت (5-Band Graphic EQ)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x18FFFFFF))
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          bandFrequencies.forEachIndexed { idx, freqLabel ->
            val value = bandValues.getOrElse(idx) { 0f }
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "${if (value > 0) "+" else ""}${value.toInt()}dB",
                color = if (value != 0f) TodGold else Color(0xAAFFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
              Box(
                modifier = Modifier
                  .height(110.dp)
                  .width(36.dp),
                contentAlignment = Alignment.Center
              ) {
                // Vertical-like control simulation
                Slider(
                  value = value,
                  onValueChange = { newVal ->
                    bandValues[idx] = newVal
                    selectedPreset = EqualizerPreset.CUSTOM
                    onApplyPreset(EqualizerPreset.CUSTOM, bandValues.toList(), bassBoostValue)
                  },
                  valueRange = -10f..10f,
                  colors = SliderDefaults.colors(
                    thumbColor = TodGold,
                    activeTrackColor = TodGold,
                    inactiveTrackColor = Color(0x33FFFFFF)
                  ),
                  modifier = Modifier.fillMaxSize()
                )
              }
              Text(text = freqLabel, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}
