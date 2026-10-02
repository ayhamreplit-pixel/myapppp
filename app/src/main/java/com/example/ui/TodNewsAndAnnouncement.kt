package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.model.AnnouncementConfig
import com.example.model.SportsNewsItem
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold
import kotlinx.coroutines.launch

/**
 * Top Marquee Announcement Bar (Ticker with Gold "إعلان هام" Badge)
 */
@Composable
fun TodMarqueeAnnouncementBar(
  config: AnnouncementConfig,
  modifier: Modifier = Modifier
) {
  if (!config.isEnabled || config.message.isBlank()) return

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(
        Brush.horizontalGradient(
          listOf(
            Color(0xFF1E1708),
            Color(0xFF141926),
            Color(0xFF0F1118)
          )
        )
      )
      .border(0.8.dp, Color(0x60FFB800), RoundedCornerShape(12.dp))
      .padding(horizontal = 10.dp, vertical = 7.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Golden Badge
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFFFFB800))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          Icons.Default.Campaign,
          contentDescription = null,
          tint = Color.Black,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = config.title,
          color = Color.Black,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Black,
          fontFamily = ThmanyahFontFamily
        )
      }

      // Scrolling / Static message
      Text(
        text = config.message,
        color = Color.White,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = ThmanyahFontFamily,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

/**
 * Horizontal Sports News Rail (Home tab preview)
 */
@Composable
fun TodSportsNewsRail(
  news: List<SportsNewsItem>,
  onViewAllClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  if (news.isEmpty()) return

  var selectedNews by remember { mutableStateOf<SportsNewsItem?>(null) }

  Column(modifier = modifier.fillMaxWidth()) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          Icons.Default.Newspaper,
          contentDescription = null,
          tint = TodGold,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = "آخر الأخبار الرياضية الحية",
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          fontFamily = ThmanyahFontFamily
        )
      }

      Text(
        text = "عرض الكل",
        color = TodGold,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = ThmanyahFontFamily,
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .clickable(onClick = onViewAllClick)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }

    // News Horizontal Cards
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(news, key = { it.id }) { item ->
        TodNewsCard(
          item = item,
          onClick = { selectedNews = item }
        )
      }
    }
  }

  // News Detail Modal Dialog
  selectedNews?.let { item ->
    TodNewsDetailDialog(item = item, onDismiss = { selectedNews = null })
  }
}

/**
 * Dedicated Full News Screen (قسم الأخبار المستقل والمطور)
 */
@Composable
fun TodDedicatedNewsScreen(
  news: List<SportsNewsItem>,
  onRefreshNews: suspend () -> Unit,
  modifier: Modifier = Modifier
) {
  val coroutineScope = rememberCoroutineScope()
  var isRefreshing by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("الكل") }
  var selectedNewsDetail by remember { mutableStateOf<SportsNewsItem?>(null) }

  val categories = listOf("الكل", "دوري أبطال أوروبا", "الدوري الإنجليزي", "الدوري الإسباني", "الكرة العربية", "انتقالات")

  val filteredNews = remember(news, selectedCategory, searchQuery) {
    var list = news
    if (selectedCategory != "الكل") {
      list = list.filter {
        it.category.contains(selectedCategory, ignoreCase = true) ||
        it.title.contains(selectedCategory, ignoreCase = true)
      }
    }
    if (searchQuery.isNotBlank()) {
      list = list.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.summary.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true)
      }
    }
    list
  }

  val heroNews = remember(filteredNews) { filteredNews.firstOrNull() }
  val remainingNews = remember(filteredNews, heroNews) {
    if (heroNews != null) filteredNews.filter { it.id != heroNews.id } else filteredNews
  }

  Column(modifier = modifier.fillMaxSize()) {
    // Top Bar with Search & Instant Refresh
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            "ابحث في آخر الأخبار الرياضية...",
            color = Color(0x80FFFFFF),
            fontSize = 12.sp,
            fontFamily = ThmanyahFontFamily
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "بحث", tint = TodGold, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.White, modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0x18FFFFFF),
          unfocusedContainerColor = Color(0x10FFFFFF),
          focusedBorderColor = TodGold,
          unfocusedBorderColor = Color(0x20FFFFFF),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
      )

      // Refresh Button
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0x18FFFFFF))
          .border(0.75.dp, TodGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        .clickable(enabled = !isRefreshing) {
          coroutineScope.launch {
            isRefreshing = true
            try {
              onRefreshNews()
            } finally {
              isRefreshing = false
            }
          }
        }
        .padding(horizontal = 12.dp, vertical = 12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (isRefreshing) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = TodGold, strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.Refresh, contentDescription = "تحديث الأخبار", tint = TodGold, modifier = Modifier.size(16.dp))
          }
          Text(
            text = "تحديث",
            color = TodGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ThmanyahFontFamily
          )
        }
      }
    }

    // Category Tabs Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 14.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { cat ->
        val isSelected = selectedCategory == cat
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) TodGold else Color(0x15FFFFFF))
            .clickable { selectedCategory = cat }
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text(
            text = cat,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            fontFamily = ThmanyahFontFamily
          )
        }
      }
    }

    // Content Grid / List
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Big Breaking News Hero Card
      if (heroNews != null && searchQuery.isBlank() && selectedCategory == "الكل") {
        item(key = "hero_news_banner") {
          TodHeroNewsCard(
            item = heroNews,
            onClick = { selectedNewsDetail = heroNews }
          )
        }
      }

      if (filteredNews.isEmpty()) {
        item(key = "empty_news") {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 50.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Icon(Icons.Default.Newspaper, contentDescription = null, tint = Color(0x60FFFFFF), modifier = Modifier.size(48.dp))
              Text("لا توجد أخبار مطابقة لخيارات البحث", color = Color.White, fontSize = 14.sp, fontFamily = ThmanyahFontFamily)
            }
          }
        }
      } else {
        val displayList = if (searchQuery.isBlank() && selectedCategory == "الكل") remainingNews else filteredNews
        items(displayList, key = { it.id }) { item ->
          TodNewsFeedRowCard(
            item = item,
            onClick = { selectedNewsDetail = item }
          )
        }
      }
    }
  }

  selectedNewsDetail?.let { item ->
    TodNewsDetailDialog(item = item, onDismiss = { selectedNewsDetail = null })
  }
}

/**
 * Big Featured Hero News Banner
 */
@Composable
fun TodHeroNewsCard(
  item: SportsNewsItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(210.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(Color(0xFF141824))
      .border(1.dp, TodGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
      .clickable(onClick = onClick)
  ) {
    SubcomposeAsyncImage(
      model = item.imageUrl,
      contentDescription = item.title,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
      loading = {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1C2237)), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = TodGold, modifier = Modifier.size(24.dp))
        }
      },
      error = {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1C2237)), contentAlignment = Alignment.Center) {
          Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = TodGold, modifier = Modifier.size(40.dp))
        }
      }
    )

    // Gradient Shade
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color.Transparent,
              Color(0x70000000),
              Color(0xEE090C14),
              Color(0xFF07090E)
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TodGold)
            .padding(horizontal = 9.dp, vertical = 4.dp)
        ) {
          Text(
            text = "⚡ خبر رئيسي عاجل",
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = ThmanyahFontFamily
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x90000000))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = item.source,
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ThmanyahFontFamily
          )
        }
      }

      Column {
        Text(
          text = item.title,
          color = Color.White,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.Black,
          fontFamily = ThmanyahFontFamily,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Schedule, contentDescription = null, tint = TodGold, modifier = Modifier.size(13.dp))
          Text(
            text = item.date,
            color = Color(0xCCFFFFFF),
            fontSize = 11.sp,
            fontFamily = ThmanyahFontFamily
          )
          Text(
            text = "• ${item.category}",
            color = TodGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ThmanyahFontFamily
          )
        }
      }
    }
  }
}

/**
 * List Row Card in News Screen
 */
@Composable
fun TodNewsFeedRowCard(
  item: SportsNewsItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFF131826))
      .border(0.75.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // News Image
      Box(
        modifier = Modifier
          .size(width = 110.dp, height = 85.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF1B2236))
      ) {
        SubcomposeAsyncImage(
          model = item.imageUrl,
          contentDescription = item.title,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
          error = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = TodGold, modifier = Modifier.size(24.dp))
            }
          }
        )
      }

      // Title & Metadata
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0x30FFB800))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.category.take(18),
              color = TodGold,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = ThmanyahFontFamily
            )
          }

          Text(
            text = item.source,
            color = Color(0x99FFFFFF),
            fontSize = 10.sp,
            fontFamily = ThmanyahFontFamily
          )
        }

        Text(
          text = item.title,
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = ThmanyahFontFamily,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 18.sp
        )

        Text(
          text = item.date.take(19),
          color = Color(0x70FFFFFF),
          fontSize = 10.5.sp,
          fontFamily = ThmanyahFontFamily
        )
      }
    }
  }
}

/**
 * Single News Card Item (Rail / Horizontal format)
 */
@Composable
fun TodNewsCard(
  item: SportsNewsItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .width(220.dp)
      .height(160.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFF121624))
      .border(0.75.dp, Color(0x25FFFFFF), RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
  ) {
    if (item.imageUrl.isNotBlank()) {
      SubcomposeAsyncImage(
        model = item.imageUrl,
        contentDescription = item.title,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        error = {
          Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A2136)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = TodGold, modifier = Modifier.size(32.dp))
          }
        }
      )
      // Dark Gradient Overlay for perfect readability
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Transparent,
                Color(0xCC000000),
                Color(0xF007090E)
              )
            )
          )
      )
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Category Pill
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0x80000000))
          .padding(horizontal = 7.dp, vertical = 3.dp)
      ) {
        Text(
          text = item.source.ifBlank { "يلا كورة" },
          color = TodGold,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = ThmanyahFontFamily
        )
      }

      // Title & Date
      Column {
        Text(
          text = item.title,
          color = Color.White,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = ThmanyahFontFamily,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 17.sp
        )
        if (item.date.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = item.date.take(16),
            color = Color(0x99FFFFFF),
            fontSize = 10.sp,
            fontFamily = ThmanyahFontFamily
          )
        }
      }
    }
  }
}

/**
 * News Detail Modal Dialog
 */
@Composable
fun TodNewsDetailDialog(
  item: SportsNewsItem,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xFF121624))
        .border(1.dp, TodGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        .padding(18.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(TodGold)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = item.category,
                color = Color.Black,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = ThmanyahFontFamily
              )
            }
            Text(
              text = item.source,
              color = Color(0x80FFFFFF),
              fontSize = 11.sp,
              fontFamily = ThmanyahFontFamily
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(Color(0x20FFFFFF))
          ) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(16.dp))
          }
        }

        // Image
        if (item.imageUrl.isNotBlank()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(170.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF1C2237))
          ) {
            SubcomposeAsyncImage(
              model = item.imageUrl,
              contentDescription = item.title,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }
        }

        // Title
        Text(
          text = item.title,
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          fontFamily = ThmanyahFontFamily,
          lineHeight = 22.sp
        )

        // Summary / Content
        if (item.summary.isNotBlank()) {
          Text(
            text = item.summary,
            color = Color(0xDDFFFFFF),
            fontSize = 13.sp,
            fontFamily = ThmanyahFontFamily,
            lineHeight = 20.sp
          )
        }

        // Date
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.Schedule, contentDescription = null, tint = TodGold, modifier = Modifier.size(13.dp))
          Text(
            text = item.date,
            color = Color(0x80FFFFFF),
            fontSize = 11.sp,
            fontFamily = ThmanyahFontFamily
          )
        }

        // Close Button
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = TodGold, contentColor = Color.Black),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("تم القراءة", fontWeight = FontWeight.Black, fontFamily = ThmanyahFontFamily)
        }
      }
    }
  }
}
