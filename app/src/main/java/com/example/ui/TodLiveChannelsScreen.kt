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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
  var selectedCategoryId by remember { mutableStateOf<String?>("ALL") }
  var searchQuery by remember { mutableStateOf("") }

  val filteredChannels = remember(channels, selectedCategoryId, searchQuery) {
    var list = channels
    if (selectedCategoryId != null && selectedCategoryId != "ALL") {
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
    // 1. Top Bar with Back Arrow and "قنوات مباشرة" (Screenshot 3)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF09090C))
        .padding(horizontal = 8.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "رجوع",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      Text(
        text = "قنوات مباشرة",
        color = Color.White,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(end = 12.dp)
      )
    }

    // Search bar for live channels
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      placeholder = { Text("بحث في القنوات المباشرة...", color = DarkTextSecondary, fontSize = 13.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TodGold) },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TodGold,
        unfocusedBorderColor = Color(0xFF26262E),
        focusedContainerColor = Color(0xFF121216),
        unfocusedContainerColor = Color(0xFF121216),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White
      )
    )

    // Category Chips Bar (قنوات الرياضة, قنوات الجزيرة, قنوات المسلسلات, إلخ)
    if (categories.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // "الكل" Chip
        val isAllSelected = selectedCategoryId == "ALL"
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isAllSelected) TodGold else Color(0xFF1C1C22))
            .clickable { selectedCategoryId = "ALL" }
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Text(
            text = "جميع القنوات (${channels.size})",
            color = if (isAllSelected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
          )
        }

        categories.filter { it.categoryId != "ALL" }.forEach { cat ->
          val isSelected = selectedCategoryId == cat.categoryId
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(if (isSelected) TodGold else Color(0xFF1C1C22))
              .clickable { selectedCategoryId = cat.categoryId }
              .padding(horizontal = 16.dp, vertical = 6.dp)
          ) {
            Text(
              text = cat.categoryName,
              color = if (isSelected) Color.Black else Color.White,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. Channels Grid (Screenshot 3 style: Sleek dark cards with logos)
    if (isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = TodGold)
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
              .height(130.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF111116))
              .border(1.dp, Color(0xFF22222A), RoundedCornerShape(14.dp))
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
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp)),
                  contentScale = ContentScale.Fit
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E26)),
                  contentAlignment = Alignment.Center
                ) {
                  Text("📺", fontSize = 24.sp)
                }
              }

              Text(
                text = ch.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
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
