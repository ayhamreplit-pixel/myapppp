package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.BroadcastStream
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold

/**
 * Android TV & Large Screen Leanback Split Screen (List-Detail Pane)
 * Side Channel List with active selection + Main Interactive Player pane.
 */
@Composable
fun TodLeanbackView(
  categories: List<XtreamCategory>,
  channels: List<XtreamChannel>,
  currentStream: BroadcastStream,
  onSelectChannel: (XtreamChannel) -> Unit,
  onToggleFullscreen: () -> Unit,
  playerContent: @Composable () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.categoryId ?: "ALL") }

  val filteredChannels = remember(channels, selectedCategory) {
    if (selectedCategory == "ALL") channels
    else channels.filter { it.categoryId == selectedCategory }
  }

  Row(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF070B14))
  ) {
    // Left / Main Player Pane (65% width)
    Box(
      modifier = Modifier
        .weight(0.65f)
        .fillMaxHeight()
    ) {
      playerContent()

      // Expand to full screen button on top-right
      IconButton(
        onClick = onToggleFullscreen,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(16.dp)
          .size(44.dp)
          .clip(CircleShape)
          .background(Color(0x88000000))
          .tvFocusable(shape = CircleShape, onEnterClick = onToggleFullscreen)
      ) {
        Icon(Icons.Default.Fullscreen, contentDescription = "ملء الشاشة", tint = Color.White)
      }
    }

    // Right / Side Channel Drawer Pane (35% width)
    Column(
      modifier = Modifier
        .weight(0.35f)
        .fillMaxHeight()
        .background(
          Brush.horizontalGradient(
            listOf(Color(0xFF0F1526), Color(0xFF141B30))
          )
        )
        .border(width = 1.dp, color = Color(0x22FFFFFF))
        .padding(14.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TodGold)
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(text = "واجهة التلفاز", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "قائمة القنوات",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Bold
          )
          Icon(Icons.Default.Tv, contentDescription = null, tint = TodGold, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Channel List with D-Pad focusability
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filteredChannels, key = { it.streamId }) { channel ->
          val isCurrent = channel.streamId == currentStream.id
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(if (isCurrent) TodGold.copy(alpha = 0.25f) else Color(0x18FFFFFF))
              .border(
                width = if (isCurrent) 1.5.dp else 0.5.dp,
                color = if (isCurrent) TodGold else Color(0x22FFFFFF),
                shape = RoundedCornerShape(12.dp)
              )
              .tvFocusable(
                shape = RoundedCornerShape(12.dp),
                onEnterClick = { onSelectChannel(channel) }
              )
              .clickable { onSelectChannel(channel) }
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            if (isCurrent) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF34C759))
              )
            } else {
              Spacer(modifier = Modifier.size(8.dp))
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              if (!channel.iconUrl.isNullOrBlank()) {
                AsyncImage(
                  model = channel.iconUrl,
                  contentDescription = channel.name,
                  modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp)),
                  contentScale = ContentScale.Fit
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x33FFFFFF)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
              }

              Text(
                text = channel.name,
                color = if (isCurrent) TodGold else Color.White,
                fontSize = 13.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}
