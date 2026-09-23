package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGradients

@Composable
fun TodInPlayerChannelDrawer(
  visible: Boolean,
  channels: List<BroadcastStream>,
  currentStreamId: String,
  onSelectChannel: (BroadcastStream) -> Unit,
  onClose: () -> Unit,
  onExitToHub: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredChannels = remember(channels, searchQuery) {
    if (searchQuery.isBlank()) channels
    else channels.filter { it.title.contains(searchQuery, ignoreCase = true) }
  }

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
    modifier = modifier.fillMaxSize()
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0x99000000))
        .clickable(onClick = onClose)
    ) {
        Box(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .fillMaxHeight()
          .width(360.dp)
          .background(Color(0xF412121A))
          .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
          .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
          .clickable(enabled = false) {}
          .padding(18.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // iOS Drawer Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              IosIconBadge(
                icon = Icons.Default.LiveTv,
                background = IosBadgeColors.Gold,
                tint = Color.Black,
                modifier = Modifier.size(30.dp)
              )
              Column {
                Text(
                  text = "قنوات البث المباشر",
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${channels.size} قناة متاحة",
                  color = Color(0xFF8E8E93),
                  fontSize = 11.sp
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = onExitToHub) {
                Icon(
                  imageVector = Icons.Default.ExitToApp,
                  contentDescription = "Exit to Hub",
                  tint = Color(0xFF8E8E93)
                )
              }
              IconButton(onClick = onClose) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = Color.White
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // iOS Style Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في القنوات...", fontSize = 13.sp, color = Color(0xFF8E8E93)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(18.dp)) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp)),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TodGold,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              cursorColor = TodGold
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Channel List in iOS Inset Rows
          if (filteredChannels.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (channels.isEmpty()) "لا توجد قائمة قنوات متاحة" else "لم يتم العثور على قنوات تطابق البحث",
                color = DarkTextSecondary,
                fontSize = 13.sp
              )
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(6.dp),
              contentPadding = PaddingValues(bottom = 16.dp)
            ) {
              items(filteredChannels) { stream ->
                val isPlaying = stream.id == currentStreamId
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                      if (isPlaying) Color(0x33F5A623) else Color(0x14FFFFFF)
                    )
                    .border(
                      1.dp,
                      if (isPlaying) TodGold else Color(0x12FFFFFF),
                      RoundedCornerShape(12.dp)
                    )
                    .clickable {
                      onSelectChannel(stream)
                      onClose()
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPlaying) TodGradients.LiquidGold else Brush.linearGradient(listOf(Color(0xFF2A2A38), Color(0xFF1E1E28)))),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = if (isPlaying) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                      )
                    }

                    Column {
                      Text(
                        text = stream.title,
                        color = if (isPlaying) TodGold else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      if (stream.subtitle.isNotEmpty()) {
                        Text(
                          text = stream.subtitle,
                          color = Color(0xFF8E8E93),
                          fontSize = 10.5.sp,
                          maxLines = 1
                        )
                      }
                    }
                  }

                  if (isPlaying) {
                    Box(
                      modifier = Modifier
                        .clip(CircleShape)
                        .background(TodGradients.LiquidGold)
                        .padding(5.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
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
}
