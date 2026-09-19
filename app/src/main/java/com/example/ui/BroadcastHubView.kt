package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStream
import com.example.model.MatchEventType
import com.example.model.MatchMoment
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
import com.example.ui.theme.TodViolet

@Composable
fun BroadcastHubView(
  currentStream: BroadcastStream,
  allStreams: List<BroadcastStream>,
  onSelectStream: (BroadcastStream) -> Unit,
  onOpenCustomStreamDialog: () -> Unit,
  onSeekToMoment: (MatchMoment) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("Channels & Formats", "Match Center", "Stream Info")

  val filterCategories = listOf("All", "Live Sports", "HLS Broadcast", "DASH Broadcast", "Highlights & VOD")
  var selectedCategory by remember { mutableStateOf("All") }

  val filteredStreams = remember(allStreams, selectedCategory) {
    if (selectedCategory == "All") allStreams
    else allStreams.filter { it.category == selectedCategory }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(DarkBg)
  ) {
    // Top Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = DarkSurface,
      contentColor = TodCyan,
      edgePadding = 16.dp,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = TodCyan,
          height = 3.dp
        )
      }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              color = if (selectedTab == index) TodCyan else DarkTextSecondary,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
              fontSize = 13.sp
            )
          }
        )
      }
    }

    when (selectedTab) {
      0 -> {
        // CHANNELS & FORMATS TAB
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Spacer(modifier = Modifier.height(10.dp))
            // Quick format categories
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(filterCategories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) TodCyan else DarkSurfaceElevated)
                    .border(
                      1.dp,
                      if (isSelected) TodCyan else DarkSurfaceBorder,
                      RoundedCornerShape(20.dp)
                    )
                    .clickable { selectedCategory = cat }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                  Text(
                    text = cat,
                    color = if (isSelected) Color(0xFF00222B) else DarkTextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }

          // Custom Stream URL Input Card (Direct input field with paste and play)
          item {
            TodUrlStreamInputCard(
              onPlayStream = { customStream ->
                onSelectStream(customStream)
              }
            )
          }

          // Channel items list
          items(filteredStreams) { stream ->
            val isCurrent = stream.id == currentStream.id
            ChannelStreamCard(
              stream = stream,
              isCurrent = isCurrent,
              onClick = { onSelectStream(stream) }
            )
          }

          item {
            Spacer(modifier = Modifier.height(24.dp))
          }
        }
      }

      1 -> {
        // MATCH CENTER TAB
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Live Score Card
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = currentStream.tournamentOrLeague,
                    color = TodCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                  if (currentStream.isLive) {
                    Box(
                      modifier = Modifier
                        .background(TodLiveRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text("LIVE ${currentStream.matchTime ?: ""}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Teams & Score
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceAround,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = currentStream.title.substringBefore(" vs ").substringBefore(" - "),
                    color = DarkTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                  )
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0xFF0B0F18))
                      .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                      .padding(horizontal = 14.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = currentStream.score ?: "0 - 0",
                      color = TodCyan,
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 20.sp
                    )
                  }
                  Text(
                    text = currentStream.title.substringAfter(" vs ", "").substringBefore(" - "),
                    color = DarkTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                  )
                }
              }
            }
          }

          // Match Key Moments Timeline (Goals, Cards, VAR)
          item {
            Text(
              text = "Key Match Moments (Tap to seek)",
              color = DarkTextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }

          if (currentStream.moments.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No recorded timeline moments for this broadcast.",
                  color = DarkTextSecondary,
                  fontSize = 12.sp
                )
              }
            }
          } else {
            items(currentStream.moments) { moment ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(DarkSurfaceElevated)
                  .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                  .clickable { onSeekToMoment(moment) }
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                      when (moment.eventType) {
                        MatchEventType.GOAL -> TodCyan.copy(alpha = 0.2f)
                        MatchEventType.YELLOW_CARD -> Color(0x33FFEB3B)
                        MatchEventType.RED_CARD -> Color(0x33F44336)
                        else -> Color(0x337C3AED)
                      }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = moment.minuteText,
                    color = when (moment.eventType) {
                      MatchEventType.GOAL -> TodCyan
                      MatchEventType.YELLOW_CARD -> Color(0xFFFFEB3B)
                      MatchEventType.RED_CARD -> Color(0xFFFF5252)
                      else -> TodViolet
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = moment.description,
                    color = DarkTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                  )
                  Text(
                    text = "Tap to jump directly to this moment",
                    color = DarkTextTertiary,
                    fontSize = 11.sp
                  )
                }

                Icon(
                  imageVector = Icons.Default.PlayCircleFilled,
                  contentDescription = "Jump to moment",
                  tint = TodCyan,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }

          item {
            Spacer(modifier = Modifier.height(24.dp))
          }
        }
      }

      2 -> {
        // STREAM INFO TAB
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text("Broadcast Specifications", color = DarkTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(10.dp))

          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              SpecRow("Stream Protocol", currentStream.format.displayName, TodCyan)
              SpecRow("Resolution Target", currentStream.resolutionLabel)
              SpecRow("Broadcast Type", if (currentStream.isLive) "Live Stream (DVR Enabled)" else "Video on Demand (VOD)")
              SpecRow("Engine", "AndroidX Media3 / ExoPlayer 1.5.1")
              SpecRow("Supported Codecs", "H.264 / AVC, H.265 / HEVC, VP9, AV1, AAC, AC3")
              SpecRow("Low Latency Chunking", "Enabled (Low-Latency HLS & DASH CMAF)")
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          Text("Stream Description", color = DarkTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (currentStream.description.isNotBlank()) currentStream.description
                   else "Stream optimized for adaptive multi-rate delivery with low buffer latency.",
            color = DarkTextSecondary,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}

@Composable
private fun SpecRow(label: String, value: String, valueColor: Color = DarkTextPrimary) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, color = DarkTextSecondary, fontSize = 12.sp)
    Text(value, color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
  }
}

@Composable
fun ChannelStreamCard(
  stream: BroadcastStream,
  isCurrent: Boolean,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (isCurrent) TodCyan.copy(alpha = 0.12f) else DarkSurfaceElevated)
      .border(
        width = 1.dp,
        color = if (isCurrent) TodCyan else DarkSurfaceBorder,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Channel Icon / Badge
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(
          Brush.linearGradient(
            colors = listOf(
              if (isCurrent) TodCyan else Color(0xFF1E283C),
              if (isCurrent) Color(0xFF00758A) else Color(0xFF131B29)
            )
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      if (isCurrent) {
        Icon(
          imageVector = Icons.Default.Equalizer,
          contentDescription = "Playing",
          tint = Color(0xFF00222B),
          modifier = Modifier.size(24.dp)
        )
      } else {
        Icon(
          imageVector = Icons.Default.LiveTv,
          contentDescription = null,
          tint = DarkTextSecondary,
          modifier = Modifier.size(22.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Stream Details
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (stream.channelNumber != null) {
          Text(
            text = stream.channelNumber,
            color = TodCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
        }

        if (stream.isLive) {
          Box(
            modifier = Modifier
              .background(TodLiveRed, RoundedCornerShape(3.dp))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.width(6.dp))
        }

        // Format pill (HLS, DASH, MP4)
        Box(
          modifier = Modifier
            .background(Color(0xFF202A3C), RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(
            text = stream.format.extensionBadge,
            color = TodViolet,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = stream.title,
        color = DarkTextPrimary,
        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = stream.subtitle,
        color = DarkTextSecondary,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Resolution badge or Score
    Column(horizontalAlignment = Alignment.End) {
      if (stream.score != null) {
        Text(
          text = stream.score,
          color = TodCyan,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
      Text(
        text = stream.resolutionLabel,
        color = DarkTextTertiary,
        fontSize = 10.sp
      )
    }
  }
}
