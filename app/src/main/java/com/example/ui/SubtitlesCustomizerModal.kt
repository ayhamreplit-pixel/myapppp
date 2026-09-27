package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.SubtitleTrackOption
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold

data class SubtitleStyleSettings(
  val fontSizeSp: Float = 18f,
  val textColor: Color = Color.White,
  val backgroundColor: Color = Color(0xCC000000),
  val syncOffsetMs: Long = 0L
)

@Composable
fun SubtitlesCustomizerModal(
  subtitles: List<SubtitleTrackOption>,
  selectedSubtitle: SubtitleTrackOption?,
  onSelectSubtitle: (SubtitleTrackOption?) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var fontSize by remember { mutableFloatStateOf(18f) }
  var textColorIndex by remember { mutableIntStateOf(0) }
  var bgOpacityIndex by remember { mutableIntStateOf(1) }
  var timeOffsetMs by remember { mutableStateOf(0L) }

  val textColors = listOf(Color.White, TodGold, Color(0xFF34C759), Color(0xFF00E5FF))
  val textColorNames = listOf("أبيض", "ذهبي TOD", "أخضر نيون", "سماوي")

  val bgOpacities = listOf(0.0f, 0.60f, 0.85f, 1.0f)
  val bgOpacityNames = listOf("بدون خلفية", "شبه شفاف (60%)", "داكن (85%)", "أسود كامل (100%)")

  val fontSizes = listOf(14f, 18f, 22f, 28f)
  val fontSizeNames = listOf("صغير", "متوسط", "كبير", "ضخم")

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
          onClick = {}
        )
        .padding(20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Header
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
              text = "محرك الترجمة والتزامن",
              color = Color.White,
              fontSize = 17.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.Subtitles, contentDescription = null, tint = TodGold, modifier = Modifier.size(22.dp))
          }
        }

        // Live Subtitle Preview Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF070B14))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp)),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = bgOpacities[bgOpacityIndex]))
              .padding(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Text(
              text = "معاينة: مرحباً بكم في البث الرياضي المباشر",
              color = textColors[textColorIndex],
              fontSize = fontSize.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // 1. Subtitle Track Selection
        Text("مسار الترجمة المتاح", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val isOff = selectedSubtitle == null
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isOff) TodGold else Color(0x22FFFFFF))
              .clickable { onSelectSubtitle(null) }
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text("إيقاف (Off)", color = if (isOff) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          subtitles.forEach { sub ->
            val isSelected = selectedSubtitle?.id == sub.id
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) TodGold else Color(0x22FFFFFF))
                .clickable { onSelectSubtitle(sub) }
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(sub.label, color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // 2. Subtitle Time Offset Sync (-3000ms to +3000ms)
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
            Text(
              text = "${if (timeOffsetMs > 0) "+" else ""}${timeOffsetMs} ms",
              color = TodGold,
              fontSize = 14.sp,
              fontWeight = FontWeight.Black
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("مزامنة توقيت الترجمة (Time Sync)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Icon(Icons.Default.Sync, contentDescription = null, tint = TodGold, modifier = Modifier.size(16.dp))
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .clickable { timeOffsetMs -= 250L },
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Remove, contentDescription = "تأخير", tint = Color.White, modifier = Modifier.size(18.dp))
            }

            Slider(
              value = timeOffsetMs.toFloat(),
              onValueChange = { timeOffsetMs = (it / 100).toInt() * 100L },
              valueRange = -3000f..3000f,
              colors = SliderDefaults.colors(
                thumbColor = TodGold,
                activeTrackColor = TodGold,
                inactiveTrackColor = Color(0x33FFFFFF)
              ),
              modifier = Modifier.weight(1f)
            )

            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .clickable { timeOffsetMs += 250L },
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Add, contentDescription = "تقديم", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }

        // 3. Font Size Selection
        Text("حجم خط الترجمة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          fontSizes.forEachIndexed { index, sizeVal ->
            val isSelected = fontSize == sizeVal
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) TodGold else Color(0x22FFFFFF))
                .clickable { fontSize = sizeVal }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(fontSizeNames[index], color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // 4. Color & Background
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Text Color
          Column(modifier = Modifier.weight(1f)) {
            Text("لون النص", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              textColors.forEachIndexed { idx, c ->
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(c)
                    .border(if (textColorIndex == idx) 2.dp else 0.dp, Color.White, CircleShape)
                    .clickable { textColorIndex = idx }
                )
              }
            }
          }

          // Background Opacity
          Column(modifier = Modifier.weight(1.2f)) {
            Text("خلفية النص", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              bgOpacities.forEachIndexed { idx, op ->
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (bgOpacityIndex == idx) TodGold else Color(0x22FFFFFF))
                    .clickable { bgOpacityIndex = idx }
                    .padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = if (op == 0f) "0%" else "${(op * 100).toInt()}%",
                    color = if (bgOpacityIndex == idx) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
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
