package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStats
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.TodCyan
import com.example.ui.theme.TodGreen
import com.example.ui.theme.TodLiveRed

@Composable
fun TodStatsHud(
  stats: BroadcastStats,
  isLive: Boolean,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(Color(0xEE090C14), RoundedCornerShape(12.dp))
      .border(1.dp, Color(0xFF1E2A42), RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column(modifier = Modifier.width(280.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(TodCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "BROADCAST TELEMETRY",
              color = TodCyan,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
          }
        }
        IconButton(onClick = onClose, modifier = Modifier.height(24.dp).width(24.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Stats",
            tint = DarkTextSecondary,
            modifier = Modifier.height(16.dp).width(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      StatRow(label = "Protocol / Stream", value = stats.protocol, valueColor = TodCyan)
      StatRow(label = "Resolution", value = stats.resolution)
      StatRow(
        label = "Bitrate",
        value = if (stats.bitrateKbps > 0) "${stats.bitrateKbps} kbps" else "Adaptive"
      )
      StatRow(
        label = "Frame Rate",
        value = if (stats.frameRateFps > 0) "%.1f fps".format(stats.frameRateFps) else "60.0 fps"
      )
      StatRow(
        label = "Dropped Frames",
        value = stats.droppedFrames.toString(),
        valueColor = if (stats.droppedFrames > 10) TodLiveRed else TodGreen
      )
      StatRow(
        label = "Buffer Health",
        value = "%.1f s".format(stats.bufferDurationSec),
        valueColor = if (stats.bufferDurationSec > 5f) TodGreen else TodCyan
      )

      if (isLive) {
        StatRow(
          label = "Live Latency (DVR)",
          value = "%.1f s".format(stats.liveLatencySec),
          valueColor = if (stats.liveLatencySec < 3f) TodGreen else TodLiveRed
        )
      }

      StatRow(label = "Video Codec", value = stats.videoCodec.substringAfterLast('/'))
      StatRow(label = "Hardware Decoder", value = stats.decoderName.takeLast(20))
    }
  }
}

@Composable
private fun StatRow(
  label: String,
  value: String,
  valueColor: Color = DarkTextPrimary
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = DarkTextTertiary,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )
    Text(
      text = value,
      color = valueColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      fontFamily = FontFamily.Monospace
    )
  }
}
