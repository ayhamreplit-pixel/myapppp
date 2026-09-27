package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.BroadcastStream
import com.example.model.ChannelTitleFormatter
import com.example.ui.theme.ThmanyahFontFamily

@Composable
fun DualChannelPickerModal(
  channels: List<BroadcastStream>,
  currentStreamId: String,
  onSelectSecondChannel: (BroadcastStream) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  
  val filteredChannels = remember(channels, searchQuery, currentStreamId) {
    channels.filter { ch ->
      ch.id != currentStreamId && (searchQuery.isBlank() || ch.title.contains(searchQuery, ignoreCase = true) || ch.category.contains(searchQuery, ignoreCase = true))
    }
  }

  BackHandler { onDismiss() }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.72f))
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
            listOf(Color(0xFF161E30), Color(0xFF0F1522), Color(0xFF0A0E18))
          )
        )
        .border(
          1.2.dp,
          Brush.verticalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color(0x33FFFFFF))),
          RoundedCornerShape(26.dp)
        )
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {} // Consume click inside
        )
        .padding(20.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Close button
          Box(
            modifier = Modifier
              .iosBounceClick(scaleDown = 0.86f) { onDismiss() }
              .size(36.dp)
              .clip(CircleShape)
              .background(Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(20.dp))
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "اختيار القناة الثانية",
                color = Color.White,
                fontSize = 17.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "تشغيل قناتين معاً في نفس الوقت",
                color = Color(0xFF00E5FF),
                fontSize = 11.5.sp,
                fontFamily = ThmanyahFontFamily
              )
            }
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0x3300E5FF)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
            }
          }
        }

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              text = "ابحث عن القناة الثانية...",
              color = Color(0x88FFFFFF),
              fontFamily = ThmanyahFontFamily,
              fontSize = 13.sp
            )
          },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0x22FFFFFF),
            unfocusedContainerColor = Color(0x15FFFFFF),
            focusedBorderColor = Color(0xFF00E5FF),
            unfocusedBorderColor = Color(0x33FFFFFF),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        )

        // Channels List
        if (filteredChannels.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (channels.isEmpty()) "لا توجد قنوات متوفرة في القائمة" else "لم يتم العثور على قنوات تطابق البحث",
              color = Color(0xAAFFFFFF),
              fontFamily = ThmanyahFontFamily,
              fontSize = 13.5.sp,
              textAlign = TextAlign.Center
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
          ) {
            items(filteredChannels, key = { it.id }) { ch ->
              val cleanTitle = ChannelTitleFormatter.formatTitle(ch.title)
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .iosBounceClick(scaleDown = 0.96f) {
                    onSelectSecondChannel(ch)
                  }
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color(0x1EFFFFFF))
                  .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                  .padding(horizontal = 14.dp, vertical = 10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  // Play Icon
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(Color(0x3300E5FF)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "تشغيل", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                  }

                  // Channel Name & Category
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = cleanTitle,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = ThmanyahFontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      if (ch.category.isNotBlank()) {
                        Text(
                          text = ch.category,
                          color = Color(0xFF90CAF9),
                          fontSize = 11.sp,
                          fontFamily = ThmanyahFontFamily
                        )
                      }
                    }

                    // Logo / Icon
                    if (!ch.logoUrl.isNullOrBlank()) {
                      AsyncImage(
                        model = ch.logoUrl,
                        contentDescription = cleanTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                          .size(36.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .background(Color(0x33FFFFFF))
                      )
                    } else {
                      Box(
                        modifier = Modifier
                          .size(36.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .background(Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
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
}
