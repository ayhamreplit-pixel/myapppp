package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGradients

/**
 * Modern iOS 18 Search Experience for TOD IPTV
 * Features:
 * - Edge-to-edge translucent frosted glass header connected directly with the page
 * - Quick interactive search filter chips (beIN Sports, Live, 4K UHD, Movies, News)
 * - Real Xtream categories filter chips with live counts
 * - Smart empty search state with recent searches tags and trending sports marquee
 * - Ultra-smooth iOS physics spring touch bounce on all channel cards
 * - Real channel logo rendering and authentic Thmanyah typography
 */
@Composable
fun TodSearchScreen(
  xtreamCategories: List<XtreamCategory> = emptyList(),
  allChannels: List<XtreamChannel>,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryId by remember { mutableStateOf<String?>(null) }
  var quickTagFilter by remember { mutableStateOf<String?>(null) }

  // Interactive recent searches
  val recentSearches = remember {
    mutableStateListOf(
      "beIN Sports 1 HD",
      "ريال مدريد",
      "beIN 4K",
      "أبوظبي الرياضية",
      "الكأس القطرية"
    )
  }

  // Fast single-pass instant matching
  val searchResults = remember(allChannels, searchQuery, selectedCategoryId, quickTagFilter) {
    val q = searchQuery.trim()
    val tag = quickTagFilter

    allChannels.filter { channel ->
      val matchesCategory = selectedCategoryId == null || channel.categoryId == selectedCategoryId
      val matchesQuery = q.isEmpty() || channel.name.contains(q, ignoreCase = true)
      val matchesTag = when (tag) {
        "bein" -> channel.name.contains("bein", ignoreCase = true)
        "4k" -> channel.name.contains("4k", ignoreCase = true) || channel.name.contains("uhd", ignoreCase = true)
        "sports" -> channel.name.contains("sport", ignoreCase = true) || channel.name.contains("رياض", ignoreCase = true)
        "news" -> channel.name.contains("news", ignoreCase = true) || channel.name.contains("أخبار", ignoreCase = true) || channel.name.contains("الجزيرة", ignoreCase = true)
        "movies" -> channel.name.contains("movie", ignoreCase = true) || channel.name.contains("cinema", ignoreCase = true) || channel.name.contains("افلام", ignoreCase = true)
        else -> true
      }
      matchesCategory && matchesQuery && matchesTag
    }.take(80)
  }

  FluidMeshBackground(
    modifier = modifier.fillMaxSize(),
    ambientAlpha = 0.25f
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
    // ==========================================
    // 1. Apple iOS 18 Seamless Glass Header (Connected Edge-to-Edge)
    // ==========================================
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0x40FFFFFF),
              Color(0x18FFFFFF),
              Color.Transparent
            )
          )
        )
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Header Top Bar: Icon + Large Title
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Quick Voice / Mic button with iOS spring bounce
        val micInteraction = remember { MutableInteractionSource() }
        val isMicPressed by micInteraction.collectIsPressedAsState()
        val micScale by animateFloatAsState(
          targetValue = if (isMicPressed) 0.86f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
          label = "micScale"
        )

        Box(
          modifier = Modifier
            .scale(micScale)
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0x25FFFFFF))
            .border(0.75.dp, Color(0x35FFFFFF), CircleShape)
            .clickable(
              interactionSource = micInteraction,
              indication = null
            ) {
              if (searchQuery.isBlank()) {
                searchQuery = "beIN Sports"
              }
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "بحث صوتي",
            tint = TodGold,
            modifier = Modifier.size(19.dp)
          )
        }

        // Title Column with Thmanyah Typography
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "البحث والاستكشاف",
            color = Color.White,
            fontSize = 24.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.3.sp
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF34C759))
            )
            Text(
              text = "تصفح أكثر من ${allChannels.size} قناة وباقة",
              color = Color(0xFF8E8E93),
              fontSize = 11.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Apple iOS 18 Frosted Glass Search Input Field
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier.weight(1f),
          placeholder = {
            Text(
              "ابحث عن قناة، مباراة، باقة، أو تصنيف...",
              color = Color(0xFF8E8E93),
              fontSize = 13.sp,
              fontFamily = ThmanyahFontFamily
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = if (searchQuery.isNotBlank()) TodGold else Color(0xFF8E8E93),
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { searchQuery = "" }) {
                Box(
                  modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0x35FFFFFF)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "مسح",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                  )
                }
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0x40FFFFFF),
            unfocusedContainerColor = Color(0x22FFFFFF),
            focusedBorderColor = Color(0xCCFFFFFF),
            unfocusedBorderColor = Color(0x40FFFFFF),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF007AFF)
          )
        )

        // Cancel "إلغاء" button animated when typing
        AnimatedVisibility(
          visible = searchQuery.isNotBlank(),
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Text(
            text = "إلغاء",
            color = Color(0xFF64D2FF),
            fontSize = 14.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
              .iosBounceClick {
                searchQuery = ""
                selectedCategoryId = null
                quickTagFilter = null
              }
              .padding(horizontal = 4.dp, vertical = 8.dp)
          )
        }
      }
    }

    // ==========================================
    // 2. Interactive iOS Quick Filter Pills Bar
    // ==========================================
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // 1. All pill
      IosSearchPill(
        title = "الكل",
        isSelected = selectedCategoryId == null && quickTagFilter == null,
        onClick = {
          selectedCategoryId = null
          quickTagFilter = null
        }
      )

      // 2. beIN Sports
      IosSearchPill(
        title = "🏆 beIN Sports",
        isSelected = quickTagFilter == "bein",
        onClick = {
          quickTagFilter = if (quickTagFilter == "bein") null else "bein"
          selectedCategoryId = null
        }
      )

      // 3. 4K UHD
      IosSearchPill(
        title = "✨ 4K UHD",
        isSelected = quickTagFilter == "4k",
        onClick = {
          quickTagFilter = if (quickTagFilter == "4k") null else "4k"
          selectedCategoryId = null
        }
      )

      // 4. Sports & Leagues
      IosSearchPill(
        title = "⚽ القنوات الرياضية",
        isSelected = quickTagFilter == "sports",
        onClick = {
          quickTagFilter = if (quickTagFilter == "sports") null else "sports"
          selectedCategoryId = null
        }
      )

      // 5. News
      IosSearchPill(
        title = "📰 قنوات الأخبار",
        isSelected = quickTagFilter == "news",
        onClick = {
          quickTagFilter = if (quickTagFilter == "news") null else "news"
          selectedCategoryId = null
        }
      )

      // 6. Movies & Series
      IosSearchPill(
        title = "🎬 سينما وأفلام",
        isSelected = quickTagFilter == "movies",
        onClick = {
          quickTagFilter = if (quickTagFilter == "movies") null else "movies"
          selectedCategoryId = null
        }
      )

      // Real Xtream category chips
      xtreamCategories.take(15).forEach { cat ->
        val isCatSelected = selectedCategoryId == cat.categoryId
        IosSearchPill(
          title = if (cat.channelCount > 0) "${cat.categoryName} (${cat.channelCount})" else cat.categoryName,
          isSelected = isCatSelected,
          onClick = {
            selectedCategoryId = if (isCatSelected) null else cat.categoryId
            quickTagFilter = null
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // ==========================================
    // 3. Main Content: Empty State vs Results List
    // ==========================================
    if (searchQuery.isBlank() && selectedCategoryId == null && quickTagFilter == null) {
      // SMART EMPTY STATE (Recent searches & Trending cards)
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Section: Recent Searches Tags
        if (recentSearches.isNotEmpty()) {
          item {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "مسح السجل",
                  color = Color(0xFFFF453A),
                  fontSize = 12.sp,
                  fontFamily = ThmanyahFontFamily,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.iosBounceClick { recentSearches.clear() }
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "عمليات البحث الأخيرة",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Bold
                  )
                  Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = TodGold,
                    modifier = Modifier.size(17.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Chips Flow Row
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                recentSearches.forEach { tag ->
                  Box(
                    modifier = Modifier
                      .liquidGlassEffect(shape = RoundedCornerShape(14.dp), glowTint = Color(0xFF0A84FF))
                      .iosBounceClick { searchQuery = tag }
                      .padding(horizontal = 14.dp, vertical = 7.dp)
                  ) {
                    Text(
                      text = tag,
                      color = Color.White,
                      fontSize = 12.5.sp,
                      fontFamily = ThmanyahFontFamily
                    )
                  }
                }
              }
            }
          }
        }

        // Section: Popular Trending Searches (Cards)
        item {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "الأكثر بحثاً ومشاهدة اليوم",
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFFF9F0A),
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trending Cards Grid (2x2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                IosTrendingSearchCard(
                  title = "beIN Sports 1 FHD",
                  subtitle = "قمة دوري أبطال أوروبا الحصرية",
                  tag = "مباشر 🔴",
                  modifier = Modifier.weight(1f),
                  onClick = { searchQuery = "beIN Sports 1" }
                )
                IosTrendingSearchCard(
                  title = "beIN 4K HDR",
                  subtitle = "أعلى دقة نقاء سينمائي",
                  tag = "4K UHD",
                  modifier = Modifier.weight(1f),
                  onClick = { searchQuery = "4K" }
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                IosTrendingSearchCard(
                  title = "SSC Sport 1 HD",
                  subtitle = "الدوري وكأس الملك",
                  tag = "FHD",
                  modifier = Modifier.weight(1f),
                  onClick = { searchQuery = "SSC" }
                )
                IosTrendingSearchCard(
                  title = "أبوظبي الرياضية Premium",
                  subtitle = "البطولات القارية والسباقات",
                  tag = "Premium",
                  modifier = Modifier.weight(1f),
                  onClick = { searchQuery = "أبوظبي" }
                )
              }
            }
          }
        }

        // Section: Browse by Category Cards
        if (xtreamCategories.isNotEmpty()) {
          item {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "استكشاف الباقات والتصنيفات",
                  color = Color.White,
                  fontSize = 15.sp,
                  fontFamily = ThmanyahFontFamily,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.Tv,
                  contentDescription = null,
                  tint = Color(0xFF0A84FF),
                  modifier = Modifier.size(18.dp)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .liquidGlassEffect(shape = RoundedCornerShape(18.dp), isElevated = true, glowTint = Color(0xFF0A84FF))
              ) {
                xtreamCategories.take(6).forEachIndexed { index, cat ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .iosBounceClick { selectedCategoryId = cat.categoryId }
                      .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                      contentDescription = null,
                      tint = Color(0xFF8E8E93),
                      modifier = Modifier.size(18.dp)
                    )

                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      if (cat.channelCount > 0) {
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                          Text(
                            text = "${cat.channelCount} قناة",
                            color = Color(0xFF8E8E93),
                            fontSize = 11.sp,
                            fontFamily = ThmanyahFontFamily
                          )
                        }
                      }
                      Text(
                        text = cat.categoryName,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontFamily = ThmanyahFontFamily,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                  }

                  if (index < 5 && index < xtreamCategories.size - 1) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color(0x18FFFFFF))
                    )
                  }
                }
              }
            }
          }
        }
      }
    } else {
      // SEARCH RESULTS LIST
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp)
      ) {
        // Results header indicator
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0x22FFFFFF))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "${searchResults.size} قناة مطابقة",
              color = TodGold,
              fontSize = 11.5.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "نتائج البحث الفوري",
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Bold
          )
        }

        if (searchResults.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  tint = DarkTextSecondary,
                  modifier = Modifier.size(28.dp)
                )
              }
              Text(
                text = "لم يتم العثور على قنوات مطابقة",
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "جرّب البحث باسم القناة أو الباقة باللغة العربية أو الإنجليزية",
                color = Color(0xFF8E8E93),
                fontSize = 12.sp,
                fontFamily = ThmanyahFontFamily,
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
          ) {
            items(searchResults) { channel ->
              IosChannelSearchRow(
                channel = channel,
                allChannels = searchResults,
                onPlayChannel = onPlayChannel
              )
            }
          }
        }
      }
    }
  }
}
}

/**
 * Modern iOS 18 Capsule Filter Pill
 */
@Composable
private fun IosSearchPill(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val interaction = remember { MutableInteractionSource() }
  val isPressed by interaction.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.90f else if (isSelected) 1.03f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "pillScale"
  )

  Box(
    modifier = Modifier
      .scale(scale)
      .liquidGlassEffect(
        shape = RoundedCornerShape(20.dp),
        glowTint = Color(0xFF0A84FF),
        isElevated = isSelected
      )
      .clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 14.dp, vertical = 7.dp)
  ) {
    Text(
      text = title,
      color = Color.White,
      fontSize = 12.sp,
      fontFamily = ThmanyahFontFamily,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
    )
  }
}

/**
 * Trending Marquee Card in Search Empty State
 */
@Composable
private fun IosTrendingSearchCard(
  title: String,
  subtitle: String,
  tag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val interaction = remember { MutableInteractionSource() }
  val isPressed by interaction.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.93f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "trendScale"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(14.dp))
      .background(Color(0x1CFFFFFF))
      .border(0.75.dp, Color(0x30FFFFFF), RoundedCornerShape(14.dp))
      .clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
      )
      .padding(12.dp)
  ) {
    Column(horizontalAlignment = Alignment.End) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0x33FFAE00))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = tag,
          color = TodGold,
          fontSize = 10.sp,
          fontFamily = ThmanyahFontFamily,
          fontWeight = FontWeight.Bold
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        color = Color.White,
        fontSize = 13.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        color = Color(0xFF8E8E93),
        fontSize = 10.5.sp,
        fontFamily = ThmanyahFontFamily,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * Apple iOS Channel Search Result Row with Spring Physics
 */
@Composable
private fun IosChannelSearchRow(
  channel: XtreamChannel,
  allChannels: List<XtreamChannel>,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit
) {
  val interaction = remember { MutableInteractionSource() }
  val isPressed by interaction.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "rowScale_${channel.streamId}"
  )

  val qualityLabel = if (channel.name.contains("4K", ignoreCase = true)) "4K"
                     else if (channel.name.contains("FHD", ignoreCase = true)) "1080p"
                     else "HD"

  Box(
    modifier = Modifier
      .scale(scale)
      .fillMaxWidth()
      .liquidGlassEffect(
        shape = RoundedCornerShape(16.dp),
        borderBrush = Brush.verticalGradient(
          listOf(Color(0x55FFFFFF), Color(0x1AFFFFFF))
        )
      )
      .clickable(
        interactionSource = interaction,
        indication = null
      ) {
        onPlayChannel(channel, allChannels, "Search Results")
      }
      .padding(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Play Action Squircle
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0x33FFAE00))
          .border(0.5.dp, Color(0x66FFAE00), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "تشغيل",
          tint = TodGold,
          modifier = Modifier.size(19.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      // Center: Channel Info
      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.End
      ) {
        Text(
          text = cleanChannelName(channel.name),
          color = Color.White,
          fontSize = 13.5.sp,
          fontFamily = ThmanyahFontFamily,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Quality badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (qualityLabel == "4K") Color(0x33FFAE00) else Color(0x22FFFFFF))
              .padding(horizontal = 5.dp, vertical = 1.5.dp)
          ) {
            Text(
              text = qualityLabel,
              color = if (qualityLabel == "4K") TodGold else Color.White,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Live indicator
          PulsingLiveBadge()
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      // Right: Channel Logo
      if (!channel.iconUrl.isNullOrBlank()) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .liquidGlassEffect(shape = RoundedCornerShape(12.dp), glowTint = Color(0xFF0A84FF))
            .padding(3.dp),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = channel.iconUrl,
            contentDescription = channel.name,
            modifier = Modifier.size(38.dp),
            contentScale = ContentScale.Fit
          )
        }
      } else {
        Box(
          modifier = Modifier
            .size(44.dp)
            .liquidGlassEffect(shape = RoundedCornerShape(12.dp), glowTint = Color(0xFF0A84FF)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = channel.name.take(2).uppercase(),
            color = Color(0xFF64D2FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
          )
        }
      }
    }
  }
}
