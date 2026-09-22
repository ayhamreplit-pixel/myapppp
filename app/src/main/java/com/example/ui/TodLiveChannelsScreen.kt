package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGradients

/**
 * TOD Live Channels Screen (Screenshot 3)
 * Top bar with back arrow and "قنوات مباشرة", category groupings (قنوات الرياضة, الجزيرة, المسلسلات)
 */
@Composable
fun TodLiveChannelsScreen(
  categories: List<XtreamCategory>,
  channels: List<XtreamChannel>,
  isLoading: Boolean,
  onBack: () -> Unit,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedCategoryId by remember(categories) {
    mutableStateOf(categories.firstOrNull()?.categoryId)
  }
  var searchQuery by remember { mutableStateOf("") }

  val filteredChannels = remember(channels, selectedCategoryId, searchQuery) {
    var list = channels
    if (selectedCategoryId != null) {
      list = list.filter { it.categoryId == selectedCategoryId }
    }
    if (searchQuery.isNotBlank()) {
      list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    list
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    // 1. Top Bar with Back Arrow and "قنوات مباشرة" (Modern iOS Glass Navigation Bar)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(TodGradients.HeaderGlass)
        .statusBarsPadding()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color(0x22FFFFFF))
          .clickable { onBack() },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "رجوع",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "قنوات مباشرة",
          color = Color.White,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${filteredChannels.size} قناة متاحة",
          color = Color(0xFF8E8E93),
          fontSize = 11.5.sp
        )
      }

      Spacer(modifier = Modifier.size(36.dp))
    }

    // Search bar for live channels (Apple iOS Inset Search Bar)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0x1FFFFFFF))
        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
        .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(18.dp))
        androidx.compose.foundation.text.BasicTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          singleLine = true,
          textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 14.sp
          ),
          cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0A84FF)),
          modifier = Modifier.weight(1f),
          decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
              if (searchQuery.isEmpty()) {
                Text(
                  text = "بحث سريع في القنوات...",
                  color = Color(0xFF636366),
                  fontSize = 14.sp
                )
              }
              innerTextField()
            }
          }
        )
      }
    }

    // Category Chips Bar (Apple iOS Segmented / Pill Bar)
    if (categories.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          val isSelected = selectedCategoryId == cat.categoryId
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (isSelected) Color(0xFF0A84FF)
                else Color(0x1FFFFFFF)
              )
              .border(
                0.5.dp,
                if (isSelected) Color(0x44FFFFFF) else Color(0x22FFFFFF),
                RoundedCornerShape(16.dp)
              )
              .clickable { selectedCategoryId = cat.categoryId }
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = cat.categoryName,
              color = Color.White,
              fontSize = 12.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. Channels Grid (Apple iOS Glass Cards)
    if (isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF0A84FF))
      }
    } else if (filteredChannels.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("لا توجد قنوات تطابق البحث", color = DarkTextSecondary, fontSize = 14.sp)
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredChannels) { ch ->
          Box(
            modifier = Modifier
              .height(126.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                  listOf(Color(0xFF1E1E26).copy(alpha = 0.95f), Color(0xFF14141C).copy(alpha = 0.98f))
                )
              )
              .border(1.dp, com.example.ui.theme.TodGradients.SpecularCardBorder, RoundedCornerShape(16.dp))
              .clickable {
                val catTitle = categories.firstOrNull { it.categoryId == ch.categoryId }?.categoryName ?: "قنوات مباشرة"
                onPlayChannel(ch, filteredChannels, catTitle)
              }
              .padding(12.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              if (!ch.iconUrl.isNullOrBlank()) {
                AsyncImage(
                  model = ImageRequest.Builder(context)
                    .data(ch.iconUrl)
                    .size(96, 96)
                    .crossfade(false)
                    .build(),
                  contentDescription = ch.name,
                  modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp)),
                  contentScale = ContentScale.Fit
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x22FFFFFF)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.Tv,
                    contentDescription = null,
                    tint = Color(0xFF0A84FF),
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Text(
                text = ch.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}
