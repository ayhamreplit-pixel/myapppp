package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGradients
import com.example.ui.theme.TodLiveRed

/**
 * Official TOD Search Screen - Fully Integrated with Real Xtream Categories & Channels
 */
@Composable
fun TodSearchScreen(
  xtreamCategories: List<XtreamCategory> = emptyList(),
  allChannels: List<XtreamChannel>,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryId by remember { mutableStateOf<String?>(null) }

  val searchResults = remember(allChannels, searchQuery, selectedCategoryId) {
    var list = allChannels
    if (selectedCategoryId != null) {
      list = list.filter { it.categoryId == selectedCategoryId }
    }
    if (searchQuery.isNotBlank()) {
      list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    list.take(100)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(TodGradients.ObsidianCanvas)
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    // 1. Search Text Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("ابحث عن مباراة، قناة، رياضة أو باقة...", color = DarkTextSecondary, fontSize = 14.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TodGold) },
      trailingIcon = {
        if (searchQuery.isNotBlank()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.White)
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFF16161D),
        unfocusedContainerColor = Color(0xFF121217),
        focusedBorderColor = TodGold,
        unfocusedBorderColor = Color(0xFF262633),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = TodGold
      )
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Real Xtream Categories Quick Filter Chips (Horizontal)
    if (xtreamCategories.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        xtreamCategories.forEach { category ->
          val isSelected = selectedCategoryId == category.categoryId
          val chipScale by animateFloatAsState(
            targetValue = if (isSelected) 1.05f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "searchChipScale"
          )

          Box(
            modifier = Modifier
              .scale(chipScale)
              .clip(RoundedCornerShape(18.dp))
              .background(if (isSelected) TodGradients.LiquidGold else Brush.horizontalGradient(listOf(Color(0xFF1C1C28), Color(0xFF14141E))))
              .border(
                1.dp,
                if (isSelected) Color(0xFFFFD54F) else Color(0xFF28283A),
                RoundedCornerShape(18.dp)
              )
              .clickable { 
                selectedCategoryId = if (isSelected) null else category.categoryId
              }
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = if (category.channelCount > 0) "${category.categoryName} (${category.channelCount})" else category.categoryName,
              color = if (isSelected) Color.Black else Color.White,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 3. Results count indicator
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${searchResults.size} نتيجة",
        color = DarkTextSecondary,
        fontSize = 12.sp
      )
      Text(
        text = if (searchQuery.isNotBlank()) "نتائج البحث" else "قنوات اشتراكك",
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 4. Search Results Lazy List
    if (searchResults.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Search, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(48.dp))
          Spacer(modifier = Modifier.height(10.dp))
          Text("لم يتم العثور على قنوات تطابق بحثك", color = DarkTextSecondary, fontSize = 14.sp)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
      ) {
        items(searchResults) { channel ->
          val interactionSource = remember { MutableInteractionSource() }
          val isPressed by interactionSource.collectIsPressedAsState()
          val cardScale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "searchCardScale"
          )

          val qualityLabel = if (channel.name.contains("4K", ignoreCase = true)) "4K" 
                             else if (channel.name.contains("FHD", ignoreCase = true)) "FHD" 
                             else "HD"

          Box(
            modifier = Modifier
              .scale(cardScale)
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(TodGradients.CardGlass)
              .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(14.dp))
              .clickable(
                interactionSource = interactionSource,
                indication = null
              ) { onPlayChannel(channel, searchResults, "Search Results") }
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Left Play Icon
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF1E1E28)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TodGold, modifier = Modifier.size(20.dp))
              }

              Spacer(modifier = Modifier.width(12.dp))

              // Center Details: Name + Badges
              Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
              ) {
                Text(
                  text = cleanChannelName(channel.name),
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Quality badge
                  VideoQualityBadge(qualityText = qualityLabel)

                  // Category tag if found
                  val catName = xtreamCategories.firstOrNull { it.categoryId == channel.categoryId }?.categoryName
                  if (!catName.isNullOrBlank()) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E1E28))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(catName, color = DarkTextSecondary, fontSize = 10.sp)
                    }
                  }

                  // Live pulsing tag
                  PulsingLiveBadge()
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              // Right Channel Logo
              if (!channel.iconUrl.isNullOrBlank()) {
                AsyncImage(
                  model = channel.iconUrl,
                  contentDescription = channel.name,
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp)),
                  contentScale = ContentScale.Fit
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E28)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = channel.name.take(2).uppercase(),
                    color = TodGold,
                    fontSize = 13.sp,
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
