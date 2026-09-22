package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodButtonGrey
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGoldGlow
import com.example.ui.theme.TodGradients
import com.example.ui.theme.TodLiveRed
import kotlinx.coroutines.delay

/**
 * Dynamic Hero Item for TOD Carousel
 */
data class DynamicTodHeroItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val categoryName: String,
  val channel: XtreamChannel?,
  val isLive: Boolean = true,
  val backdropGradient: Brush,
  val tags: List<String> = emptyList(),
  val primaryButtonLabel: String = "تابع الآن"
)

/**
 * High-End Corporate TOD Home Screen
 * Features 100% stable layouts, instant responsive category loading, and intelligent channel organization
 */
@Composable
fun TodHomeScreen(
  xtreamCategories: List<XtreamCategory>,
  allChannels: List<XtreamChannel>,
  isLoading: Boolean,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit,
  onOpenMatchDetail: (TodMatchDetail) -> Unit,
  onOpenLiveChannels: () -> Unit,
  onOpenProfile: () -> Unit,
  onOpenQuickLink: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedCategoryId by remember { mutableStateOf<String?>("ALL") }
  var searchQuery by remember { mutableStateOf("") }
  var isMuted by remember { mutableStateOf(false) }

  // Map channels grouped by Xtream category dynamically with complete O(N) coverage
  val channelsByCategory: Map<XtreamCategory, List<XtreamChannel>> = remember(allChannels, xtreamCategories) {
    if (allChannels.isEmpty()) {
      emptyMap()
    } else {
      val map = mutableMapOf<XtreamCategory, List<XtreamChannel>>()
      
      // Fast O(N) grouping by categoryId
      val channelsByCatId = allChannels.groupBy { it.categoryId ?: "" }

      // Map standard categories
      xtreamCategories.forEach { category ->
        if (category.categoryId != "ALL") {
          val list = channelsByCatId[category.categoryId]
            ?: channelsByCatId[category.categoryName]
            ?: emptyList()
          if (list.isNotEmpty()) {
            map[category] = list
          }
        }
      }

      // If no categories matched or categories empty, group dynamically
      if (map.isEmpty()) {
        channelsByCatId.forEach { (catKey, list) ->
          val label = catKey.ifBlank { "القنوات الرئيسية" }
          map[XtreamCategory(label, label, list.size)] = list
        }
      }

      map
    }
  }

  // Channels for current selected view with efficient slicing for huge channel lists
  val currentFilteredChannels = remember(allChannels, selectedCategoryId, searchQuery) {
    val baseList = if (selectedCategoryId == null || selectedCategoryId == "ALL") {
      allChannels
    } else {
      allChannels.filter { it.categoryId == selectedCategoryId }
    }

    if (searchQuery.isNotBlank()) {
      val query = searchQuery.trim()
      baseList.filter { it.name.contains(query, ignoreCase = true) }
    } else {
      baseList
    }
  }

  // Dynamic Hero Carousel items
  val dynamicHeroItems = remember(allChannels) {
    if (allChannels.isNotEmpty()) {
      val topCandidates = allChannels.filter {
        it.name.contains("bein", ignoreCase = true) ||
        it.name.contains("sports", ignoreCase = true) ||
        it.name.contains("4k", ignoreCase = true) ||
        it.name.contains("vs", ignoreCase = true) ||
        it.name.contains("live", ignoreCase = true)
      }.ifEmpty { allChannels.take(5) }.take(6)

      val gradients = listOf(
        TodGradients.SportsPurple,
        TodGradients.SportsGreen,
        TodGradients.SportsRed,
        Brush.verticalGradient(listOf(Color(0xFF161F30), Color(0xFF131722), Color(0xFF09090C))),
        Brush.verticalGradient(listOf(Color(0xFF2E1C0A), Color(0xFF1C130D), Color(0xFF09090C)))
      )

      topCandidates.mapIndexed { index, channel ->
        val isMatch = channel.name.contains(" vs ", ignoreCase = true) || channel.name.contains(" - ")
        val title = cleanChannelName(channel.name)
        val subtitle = if (isMatch) "مباراة حية ومباشرة • جودة فائقة FHD" else "بث مباشر وحصري عبر سيرفرك"
        val tags = if (channel.name.contains("4K", ignoreCase = true)) listOf("4K UHD", "مباشر", "حصري")
                   else if (channel.name.contains("FHD", ignoreCase = true)) listOf("1080p FHD", "مباشر")
                   else listOf("HD", "مباشر")

        DynamicTodHeroItem(
          id = channel.streamId,
          title = title,
          subtitle = subtitle,
          categoryName = "TOD Live",
          channel = channel,
          isLive = true,
          backdropGradient = gradients[index % gradients.size],
          tags = tags,
          primaryButtonLabel = "تابع الآن"
        )
      }
    } else {
      listOf(
        DynamicTodHeroItem(
          id = "welcome_hero",
          title = "مرحباً بك في TOD • البث المباشر",
          subtitle = "قم بربط اشتراك Xtream Codes أو M3U لبدء البث المباشر الفوري لكافة القنوات",
          categoryName = "TOD by beIN",
          channel = null,
          isLive = false,
          backdropGradient = TodGradients.SportsPurple,
          tags = listOf("Xtream Codes", "M3U8", "4K / FHD"),
          primaryButtonLabel = "إضافة سيرفر جديد"
        )
      )
    }
  }

  var currentHeroIndex by remember { mutableIntStateOf(0) }

  // Auto-scroll hero banner gently
  LaunchedEffect(dynamicHeroItems.size) {
    if (dynamicHeroItems.size > 1) {
      while (true) {
        delay(7000)
        currentHeroIndex = (currentHeroIndex + 1) % dynamicHeroItems.size
      }
    }
  }

  val activeHero = dynamicHeroItems.getOrElse(currentHeroIndex.coerceIn(0, dynamicHeroItems.size - 1)) {
    dynamicHeroItems.first()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(TodGradients.ObsidianCanvas)
  ) {
    // 1. Top Header: TOD Logo + User Xtream Categories
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xFF101015), Color(0xFF0A0A0D))
          )
        )
        .padding(top = 4.dp, bottom = 6.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left Profile Avatar
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.linearGradient(listOf(TodGold, Color(0xFFFF9800)))
            )
            .clickable { onOpenProfile() },
          contentAlignment = Alignment.Center
        ) {
          Text("M", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        // Official TOD by beIN Logo
        TodLogo(fontSize = 24, showSubtext = true)

        // Apple iOS Glass Quick Link Action Button
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x1FFFFFFF))
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            .clickable { onOpenQuickLink() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = "رابط سريع",
              tint = Color(0xFF0A84FF),
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "رابط سريع",
              color = Color.White,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Top Category Chips Row with Active Gold Pill (Real Xtream Categories)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Real Xtream Categories Chips
        xtreamCategories.filter { it.categoryId != "ALL" }.forEach { category ->
          val isSelected = selectedCategoryId == category.categoryId
          val chipScale by animateFloatAsState(
            targetValue = if (isSelected) 1.05f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "chipScale_${category.categoryId}"
          )

          Box(
            modifier = Modifier
              .scale(chipScale)
              .clip(RoundedCornerShape(20.dp))
              .background(
                if (isSelected) {
                  TodGradients.LiquidGold
                } else {
                  Brush.horizontalGradient(listOf(Color(0xFF161622), Color(0xFF101018)))
                }
              )
              .border(
                1.dp,
                if (isSelected) Color(0xFFFFD54F) else Color(0xFF28283A),
                RoundedCornerShape(20.dp)
              )
              .clickable { 
                selectedCategoryId = if (isSelected) null else category.categoryId
              }
              .padding(horizontal = 15.dp, vertical = 7.dp)
          ) {
            Text(
              text = if (category.channelCount > 0) "${category.categoryName} (${category.channelCount})" else category.categoryName,
              color = if (isSelected) Color.Black else Color.White,
              fontSize = 12.5.sp,
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
            )
          }
        }
      }
    }

    // 2. Main Content Body
    if (selectedCategoryId != null && selectedCategoryId != "ALL") {
      // SPECIFIC CATEGORY VIEW: Displays high-density Grid of channels for that category
      val activeCatName = xtreamCategories.find { it.categoryId == selectedCategoryId }?.categoryName ?: "القنوات"

      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(DarkBg)
      ) {
        // Category Header with count and search
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1A1A24))
              .clickable { selectedCategoryId = null }
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text("العودة للرئيسية", color = TodGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Text(
            text = "$activeCatName (${currentFilteredChannels.size})",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
          )
        }

        // Quick In-Category Search
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("بحث في $activeCatName...", color = DarkTextSecondary, fontSize = 12.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TodGold, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TodGold,
            unfocusedBorderColor = Color(0xFF262632),
            focusedContainerColor = Color(0xFF121218),
            unfocusedContainerColor = Color(0xFF121218),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (isLoading) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TodGold)
          }
        } else if (currentFilteredChannels.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد قنوات مطابقة في هذا القسم", color = DarkTextSecondary, fontSize = 14.sp)
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(currentFilteredChannels, key = { it.streamId }) { channel ->
              CorporateChannelGridCard(
                channel = channel,
                allChannels = currentFilteredChannels,
                categoryName = activeCatName,
                onPlayChannel = onPlayChannel
              )
            }
          }
        }
      }
    } else {
      // ALL CATEGORIES MAIN FEED
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
      ) {
        // 3. Hero Cinematic Carousel Banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
        ) {
          // Gradient backdrop
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(activeHero.backdropGradient)
          )

          // Scrim overlay
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(TodGradients.HeroScrim)
          )

          // Sound mute toggle in hero
          IconButton(
            onClick = { isMuted = !isMuted },
            modifier = Modifier
              .padding(top = 12.dp, start = 12.dp)
              .size(34.dp)
              .clip(CircleShape)
              .background(Color(0x77000000))
              .align(Alignment.TopStart)
          ) {
            Icon(
              imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = "الصوت",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }

          // Hero Bottom Overlay Controls
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.BottomCenter)
              .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.End
          ) {
            // Animated Hero Content with Cross-fade
            AnimatedContent(
              targetState = activeHero,
              transitionSpec = {
                fadeIn(animationSpec = tween(450)) togetherWith fadeOut(animationSpec = tween(350))
              },
              label = "heroCrossfade"
            ) { hero ->
              Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
              ) {
                // Badges / Tags row
                if (hero.tags.isNotEmpty()) {
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    hero.tags.forEach { tag ->
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(Color(0xFF222228))
                          .border(1.dp, Color(0xFF383842), RoundedCornerShape(4.dp))
                          .padding(horizontal = 8.dp, vertical = 3.dp)
                      ) {
                        Text(tag, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                      }
                    }
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                }

                // Pulsing Live Beacon Pill
                if (hero.isLive) {
                  PulsingLiveBadge()
                  Spacer(modifier = Modifier.height(6.dp))
                }

                // Hero Title + Channel Logo Row
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.End
                ) {
                  Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                  ) {
                    Text(
                      text = hero.title,
                      color = Color.White,
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Black,
                      textAlign = TextAlign.End,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = hero.subtitle,
                      color = DarkTextSecondary,
                      fontSize = 11.5.sp,
                      textAlign = TextAlign.End
                    )
                  }

                  if (!hero.channel?.iconUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                      modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x99181824))
                        .border(1.dp, TodGold.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      AsyncImage(
                        model = hero.channel?.iconUrl,
                        contentDescription = hero.title,
                        modifier = Modifier.size(38.dp),
                        contentScale = ContentScale.Fit
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Buttons Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Replay Button (↺)
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(TodButtonGrey)
                  .clickable {
                    activeHero.channel?.let { onPlayChannel(it, allChannels, "Hero Replay") }
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }

              // Add Button (+)
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(TodButtonGrey)
                  .clickable { /* Watchlist */ },
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }

              // Big Wide Liquid Gold Button: "تابع الآن ▶"
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .clip(RoundedCornerShape(11.dp))
                  .background(TodGradients.LiquidGold)
                  .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(11.dp))
                  .clickable {
                    if (activeHero.channel != null) {
                      onPlayChannel(activeHero.channel, allChannels, "TOD Hero")
                    } else {
                      onOpenProfile()
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = activeHero.primaryButtonLabel,
                    color = Color.Black,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Black
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Carousel Indicator Dots
            if (dynamicHeroItems.size > 1) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                dynamicHeroItems.indices.forEach { idx ->
                  val isActive = idx == currentHeroIndex
                  Box(
                    modifier = Modifier
                      .padding(horizontal = 3.dp)
                      .height(3.5.dp)
                      .width(if (isActive) 16.dp else 5.dp)
                      .clip(RoundedCornerShape(2.dp))
                      .background(if (isActive) Color.White else Color(0xFF4A4A52))
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // If no channels yet, show clean connection card
        if (allChannels.isEmpty() && !isLoading) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF131318))
              .border(1.dp, TodGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
              .padding(20.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.Dns, contentDescription = null, tint = TodGold, modifier = Modifier.size(38.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                "لم يتم توصيل سيرفر بعد",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "أضف بيانات سيرفر Xtream Codes أو رابط M3U لعرض مجموعات وقنوات اشتراكك مباشرةً بهوية TOD الذكية.",
                color = DarkTextSecondary,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
              )
              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = onOpenProfile,
                colors = ButtonDefaults.buttonColors(containerColor = TodGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
              ) {
                Text("إضافة السيرفر الآن", color = Color.Black, fontWeight = FontWeight.Black)
              }
            }
          }
        }

        // 4. "تابع الآن على الهواء" Quick Live Channels Rail
        if (allChannels.isNotEmpty()) {
          DynamicChannelRail(
            sectionTitle = "تابع الآن على الهواء",
            actionLabel = "عرض الكل",
            channels = allChannels.take(15),
            allChannels = allChannels,
            onActionClick = onOpenLiveChannels,
            onPlayChannel = onPlayChannel
          )
          Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. Dynamic Xtream Category Rails (Show top 15 rails on home feed for buttery-smooth scrolling)
        val homeRails = channelsByCategory.entries.take(15)
        homeRails.forEach { (category, catChannels) ->
          if (catChannels.isNotEmpty()) {
            DynamicChannelRail(
              sectionTitle = category.categoryName,
              actionLabel = "عرض الكل (${catChannels.size})",
              channels = catChannels.take(20),
              allChannels = allChannels,
              onActionClick = {
                selectedCategoryId = category.categoryId
              },
              onPlayChannel = onPlayChannel
            )
            Spacer(modifier = Modifier.height(18.dp))
          }
        }

        // Fallback: If no category groupings exist but channels exist
        if (channelsByCategory.isEmpty() && allChannels.isNotEmpty()) {
          DynamicChannelRail(
            sectionTitle = "جميع القنوات المتاحة",
            actionLabel = "تصفح الكل (${allChannels.size})",
            channels = allChannels.take(30),
            allChannels = allChannels,
            onActionClick = onOpenLiveChannels,
            onPlayChannel = onPlayChannel
          )
          Spacer(modifier = Modifier.height(18.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

/**
 * Modern Corporate Card for Grid View with Spring Bounce & Specular Glow Border
 */
@Composable
fun CorporateChannelGridCard(
  channel: XtreamChannel,
  allChannels: List<XtreamChannel>,
  categoryName: String,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.94f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "gridCardScale"
  )

  val qualityLabel = if (channel.name.contains("4K", ignoreCase = true)) "4K UHD" 
                     else if (channel.name.contains("FHD", ignoreCase = true)) "1080p FHD" 
                     else "HD"

  Box(
    modifier = Modifier
      .scale(scale)
      .fillMaxWidth()
      .height(128.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(TodGradients.CardGlass)
      .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(14.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) { onPlayChannel(channel, allChannels, categoryName) }
      .padding(10.dp)
  ) {
    // Channel Icon / Placeholder
    if (!channel.iconUrl.isNullOrBlank()) {
      AsyncImage(
        model = channel.iconUrl,
        contentDescription = null,
        modifier = Modifier
          .size(38.dp)
          .align(Alignment.TopStart)
          .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Fit
      )
    } else {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF1E1E28))
          .align(Alignment.TopStart),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TodGold, modifier = Modifier.size(16.dp))
      }
    }

    // Live Pulsing Beacon
    PulsingLiveBadge(
      modifier = Modifier.align(Alignment.TopEnd)
    )

    // Channel Name & Info at bottom
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomEnd),
      horizontalAlignment = Alignment.End
    ) {
      Text(
        text = cleanChannelName(channel.name),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.End
      )
      Spacer(modifier = Modifier.height(3.dp))
      VideoQualityBadge(qualityText = qualityLabel)
    }
  }
}

/**
 * Dynamic Channel Rail for each Xtream Category Group
 */
@Composable
fun DynamicChannelRail(
  sectionTitle: String,
  actionLabel: String,
  channels: List<XtreamChannel>,
  allChannels: List<XtreamChannel>,
  onActionClick: () -> Unit,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = actionLabel,
        color = TodGold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable { onActionClick() }
      )
      Text(
        text = sectionTitle,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(channels, key = { it.streamId }) { channel ->
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
          targetValue = if (isPressed) 0.94f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
          label = "railCardScale"
        )

        val qualityLabel = if (channel.name.contains("4K", ignoreCase = true)) "4K UHD" 
                           else if (channel.name.contains("FHD", ignoreCase = true)) "1080p FHD" 
                           else "HD"

        Box(
          modifier = Modifier
            .scale(scale)
            .width(168.dp)
            .height(118.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TodGradients.CardGlass)
            .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(12.dp))
            .clickable(
              interactionSource = interactionSource,
              indication = null
            ) { onPlayChannel(channel, allChannels, sectionTitle) }
            .padding(10.dp)
        ) {
          // Channel Icon if available
          if (!channel.iconUrl.isNullOrBlank()) {
            AsyncImage(
              model = channel.iconUrl,
              contentDescription = null,
              modifier = Modifier
                .size(34.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(6.dp)),
              contentScale = ContentScale.Fit
            )
          } else {
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E1E28))
                .align(Alignment.TopStart),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TodGold, modifier = Modifier.size(16.dp))
            }
          }

          // Live Red Pulsing Badge
          PulsingLiveBadge(
            modifier = Modifier.align(Alignment.TopEnd)
          )

          // Channel Name & Info at bottom
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = cleanChannelName(channel.name),
              color = Color.White,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(2.dp))
            VideoQualityBadge(qualityText = qualityLabel)
          }
        }
      }
    }
  }
}

/**
 * Cleans ugly IPTV prefixes e.g. "AR | beIN SPORTS 1 FHD [HEVC]" -> "beIN SPORTS 1 FHD"
 */
fun cleanChannelName(raw: String): String {
  var name = raw
  if (name.contains("|")) {
    val parts = name.split("|")
    name = parts.last().trim()
  }
  if (name.contains(":")) {
    val parts = name.split(":")
    name = parts.last().trim()
  }
  return name.replace("[HEVC]", "", ignoreCase = true)
    .replace("[H.265]", "", ignoreCase = true)
    .replace("(VIP)", "", ignoreCase = true)
    .replace("[VIP]", "", ignoreCase = true)
    .trim()
}

