package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
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
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.ThmanyahFontFamily
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
  onOpenProfile: () -> Unit,
  onOpenQuickLink: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  // Start on Home view (null = Main Feed with Hero & Rails)
  var selectedCategoryId by remember { mutableStateOf<String?>(null) }
  var isMuted by remember { mutableStateOf(false) }

  // Handle hardware & system back gesture to return from category view to home feed
  BackHandler(enabled = selectedCategoryId != null) {
    selectedCategoryId = null
  }

  // High-performance single-pass category grouping to prevent UI freezing and ANR on 30k+ channels
  val channelsByCategory: Map<XtreamCategory, List<XtreamChannel>> = remember(allChannels, xtreamCategories) {
    if (allChannels.isEmpty()) {
      emptyMap()
    } else {
      val catGroupMap = HashMap<String, MutableList<XtreamChannel>>()
      for (ch in allChannels) {
        val key = ch.categoryId?.trim()?.lowercase() ?: "general"
        catGroupMap.getOrPut(key) { mutableListOf() }.add(ch)
      }
      val result = LinkedHashMap<XtreamCategory, List<XtreamChannel>>()
      for (cat in xtreamCategories) {
        if (cat.categoryId == "ALL") continue
        val key = cat.categoryId.trim().lowercase()
        val list = catGroupMap[key]
        if (!list.isNullOrEmpty()) {
          result[cat.copy(channelCount = list.size)] = list
        }
      }
      if (result.isEmpty()) {
        catGroupMap.entries.take(20).forEach { (k, list) ->
          result[XtreamCategory(k, k, list.size)] = list
        }
      }
      result
    }
  }

  // Robust category channels for selected category view using instant map lookup
  val currentCategoryChannels = remember(allChannels, selectedCategoryId, channelsByCategory) {
    if (selectedCategoryId == null || selectedCategoryId == "ALL") {
      allChannels
    } else {
      val target = selectedCategoryId?.trim() ?: ""
      val matchedFromMap = channelsByCategory.entries.firstOrNull {
        it.key.categoryId.trim().equals(target, ignoreCase = true)
      }?.value
      if (!matchedFromMap.isNullOrEmpty()) {
        matchedFromMap
      } else {
        allChannels.filter { (it.categoryId?.trim() ?: "").equals(target, ignoreCase = true) }
      }
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
          primaryButtonLabel = "مشاهدة الآن"
        )
      }
    } else {
      listOf(
        DynamicTodHeroItem(
          id = "welcome_hero_pro",
          title = "مشغل IPTV الذكي 4K",
          subtitle = "بث فوري مباشر لكافة القنوات العالمية والرياضية بأعلى جودة وبدون تقطيع",
          categoryName = "البث المباشر الذكي",
          channel = null,
          isLive = true,
          backdropGradient = Brush.verticalGradient(
            listOf(Color(0xFF0A84FF), Color(0xFF0055D4), Color(0xFF080D20))
          ),
          tags = listOf("🌟 IPTV 4K", "⚡ فائق السرعة", "🏆 مانع التقطيع", "🔥 مباشر"),
          primaryButtonLabel = "تسجيل الدخول إلى سيرفرك"
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

  val activeHero = if (dynamicHeroItems.isNotEmpty()) {
    dynamicHeroItems.getOrNull(currentHeroIndex.coerceIn(0, dynamicHeroItems.size - 1))
  } else null

  FluidMeshBackground(
    modifier = modifier.fillMaxSize(),
    ambientAlpha = 0.70f
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // 1. Ultra-Clean Modern iOS Liquid Glass Seamless Navigation Header
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0x40FFFFFF),
                Color(0x18FFFFFF),
                Color.Transparent
              )
            )
          )
          .statusBarsPadding()
          .padding(top = 2.dp, bottom = 6.dp)
      ) {
        // Top Row: Avatar + Brand + Quick Link Action
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // User Avatar with iOS Press Feedback
          val avatarInteraction = remember { MutableInteractionSource() }
          val isAvatarPressed by avatarInteraction.collectIsPressedAsState()
          val avatarScale by animateFloatAsState(
            targetValue = if (isAvatarPressed) 0.88f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "avatarPressScale"
          )

          Box(
            modifier = Modifier
              .scale(avatarScale)
              .size(38.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(listOf(TodGold, Color(0xFFFF9500)))
              )
              .drawBehind {
                drawRoundRect(
                  brush = Brush.verticalGradient(listOf(Color(0x80FFFFFF), Color(0x00FFFFFF))),
                  cornerRadius = CornerRadius(19.dp.toPx(), 19.dp.toPx())
                )
              }
              .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
              .clickable(
                interactionSource = avatarInteraction,
                indication = null,
                onClick = onOpenProfile
              ),
            contentAlignment = Alignment.Center
          ) {
            Text("M", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
          }

          // Clean Official Jawwy / TOD Logo
          Box(
            contentAlignment = Alignment.Center
          ) {
            TodLogo(fontSize = 22, showSubtext = true)
          }

          // Modern Apple iOS Liquid Glass Pill Button: Quick Link Player
          val quickInteraction = remember { MutableInteractionSource() }
          val isQuickPressed by quickInteraction.collectIsPressedAsState()
          val quickScale by animateFloatAsState(
            targetValue = if (isQuickPressed) 0.90f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "quickPressScale"
          )

          Box(
            modifier = Modifier
              .scale(quickScale)
              .liquidGlassEffect(
                shape = RoundedCornerShape(20.dp),
                glowTint = Color(0xFF0A84FF)
              )
              .clickable(
                interactionSource = quickInteraction,
                indication = null,
                onClick = onOpenQuickLink
              )
              .padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              TodQuickLinkArtIcon(modifier = Modifier.size(16.dp))
              Text(
                text = "رابط سريع",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

      Spacer(modifier = Modifier.height(6.dp))

      // Apple iOS Horizontal Category Filter Pill Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // "الكل" Pill
        val isAllSelected = selectedCategoryId == null || selectedCategoryId == "ALL"
        val allInteraction = remember { MutableInteractionSource() }
        val isAllPressed by allInteraction.collectIsPressedAsState()
        val allScale by animateFloatAsState(
          targetValue = if (isAllPressed) 0.92f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
          label = "allPillScale"
        )

        Box(
          modifier = Modifier
            .scale(allScale)
            .liquidGlassEffect(
              shape = RoundedCornerShape(20.dp),
              glowTint = Color(0xFF0A84FF),
              isElevated = isAllSelected
            )
            .clickable(
              interactionSource = allInteraction,
              indication = null
            ) { selectedCategoryId = null }
            .padding(horizontal = 15.dp, vertical = 7.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "الكل (${allChannels.size})",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
          )
        }

        // Real Xtream Categories Filter Pills
        xtreamCategories.filter { it.categoryId != "ALL" }.forEach { category ->
          val isSelected = selectedCategoryId == category.categoryId
          val catInteraction = remember { MutableInteractionSource() }
          val isCatPressed by catInteraction.collectIsPressedAsState()
          val catScale by animateFloatAsState(
            targetValue = if (isCatPressed) 0.92f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "catPillScale_${category.categoryId}"
          )

          Box(
            modifier = Modifier
              .scale(catScale)
            .liquidGlassEffect(
              shape = RoundedCornerShape(20.dp),
              glowTint = Color(0xFF0A84FF),
              isElevated = isSelected
            )
            .clickable(
              interactionSource = catInteraction,
              indication = null
            ) {
              selectedCategoryId = category.categoryId
            }
            .padding(horizontal = 15.dp, vertical = 7.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (category.channelCount > 0) "${category.categoryName} (${category.channelCount})" else category.categoryName,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
      }
    }

    // 2. Main Content Body
    if (selectedCategoryId != null) {
      val isAll = selectedCategoryId == "ALL"
      val activeCatName = if (isAll) "جميع القنوات"
        else xtreamCategories.find { it.categoryId.trim() == selectedCategoryId?.trim() }?.categoryName
          ?: selectedCategoryId ?: "القنوات"
      val baseCategoryChannels = currentCategoryChannels
      var isListView by remember { mutableStateOf(false) }

      Column(
        modifier = Modifier
          .fillMaxSize()
      ) {
        // Apple iOS Seamless Category Navigation Header (Continuous Edge-to-Edge)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0x40FFFFFF),
                  Color(0x18FFFFFF),
                  Color.Transparent
                )
              )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val catBackInteraction = remember { MutableInteractionSource() }
            val isCatBackPressed by catBackInteraction.collectIsPressedAsState()
            val catBackScale by animateFloatAsState(
              targetValue = if (isCatBackPressed) 0.90f else 1.0f,
              animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
              label = "catBackScale"
            )

            Row(
              modifier = Modifier
                .scale(catBackScale)
                .liquidGlassEffect(
                  shape = RoundedCornerShape(16.dp),
                  glowTint = Color(0xFF0A84FF)
                )
                .clickable(
                  interactionSource = catBackInteraction,
                  indication = null
                ) { selectedCategoryId = null }
                .padding(horizontal = 12.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "رجوع للرئيسية",
                tint = Color(0xFF0A84FF),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("الرئيسية", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = activeCatName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = "${baseCategoryChannels.size} قناة متاحة",
                color = Color(0xFF8E8E93),
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // View Mode Switcher (Grid vs List)
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
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF34C759))
              )
              Text(
                text = "بثوث نشطة ومباشرة",
                color = Color(0xFF34C759),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Row(
              modifier = Modifier
                .liquidGlassEffect(
                  shape = RoundedCornerShape(14.dp),
                  glowTint = Color(0xFF0A84FF)
                )
                .padding(3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(11.dp))
                  .background(if (!isListView) Color(0xFF0A84FF) else Color.Transparent)
                  .iosBounceClick { isListView = false }
                  .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.GridView, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("شبكة", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(11.dp))
                  .background(if (isListView) Color(0xFF0A84FF) else Color.Transparent)
                  .iosBounceClick { isListView = true }
                  .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("قائمة", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isLoading) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TodGold)
          }
        } else if (baseCategoryChannels.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد قنوات في هذا القسم", color = DarkTextSecondary, fontSize = 14.sp)
          }
        } else {
          if (isListView) {
            LazyColumn(
              contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 24.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              itemsIndexed(baseCategoryChannels, key = { _, ch -> ch.streamId }) { index, channel ->
                CorporateChannelListRow(
                  channel = channel,
                  channelIndex = index + 1,
                  allChannels = baseCategoryChannels,
                  categoryName = activeCatName,
                  onPlayChannel = onPlayChannel
                )
              }
            }
          } else {
            LazyVerticalGrid(
              columns = GridCells.Fixed(2),
              contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(baseCategoryChannels, key = { it.streamId }) { channel ->
                CorporateChannelGridCard(
                  channel = channel,
                  allChannels = baseCategoryChannels,
                  categoryName = activeCatName,
                  onPlayChannel = onPlayChannel
                )
              }
            }
          }
        }
      }
    } else {
      // ALL CATEGORIES MAIN FEED - High-Performance Recycled LazyColumn
      val homeRails = remember(channelsByCategory) { channelsByCategory.entries.take(15) }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
      ) {
        // 3. Apple TV Style Floating Cinematic Glass Carousel Banner
        if (activeHero != null) {
          item(key = "hero_banner") {
            if (activeHero.channel == null) {
              // ULTRA-MODERN FUTURISTIC 4K IPTV PLAYER SHOWCASE HERO (Brand New Redesigned Layout)
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 6.dp)
                  .clip(RoundedCornerShape(26.dp))
                  .liquidGlassEffect(
                    shape = RoundedCornerShape(26.dp),
                    glowTint = Color(0xFF0A84FF),
                    borderBrush = Brush.linearGradient(
                      listOf(Color(0xFF64D2FF), Color(0xFF0A84FF), Color(0x30FFFFFF))
                    )
                  )
                  .padding(18.dp)
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.End
                ) {
                  // Top Row: 4K UHD Masterpiece Emblem + Live Badges
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    TodIptv4kMasterpieceIcon(modifier = Modifier.size(50.dp))

                    Row(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .background(
                            Brush.horizontalGradient(
                              listOf(Color(0xFF0A84FF).copy(alpha = 0.4f), Color(0xFF00E5FF).copy(alpha = 0.25f))
                            )
                          )
                          .border(1.dp, Color(0xFF64D2FF).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                          .padding(horizontal = 10.dp, vertical = 4.dp)
                      ) {
                        Text("🌟 4K UHD 60FPS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                      }
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .background(Color(0x3334C759))
                          .border(0.75.dp, Color(0x8834C759), RoundedCornerShape(8.dp))
                          .padding(horizontal = 8.dp, vertical = 4.dp)
                      ) {
                        Text("⚡ مباشر", color = Color(0xFF34C759), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  // Main Title & Subtitle
                  Text(
                    text = "مشغل IPTV الذكي 4K Ultra",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.End
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = "بث فوري مباشر لكافة القنوات الرياضية والعالمية بأعلى دقة وتقنية مانع التقطيع التلقائي الذكي",
                    color = Color(0xFFD8D8E0),
                    fontSize = 12.sp,
                    fontFamily = ThmanyahFontFamily,
                    textAlign = TextAlign.End,
                    lineHeight = 17.sp
                  )

                  Spacer(modifier = Modifier.height(12.dp))

                  // Feature Spec Badges Row
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    val featurePills = listOf("🛡️ مانع تقطيع", "🔊 Dolby Audio", "⚡ زمن 0ms", "🌐 Xtream & M3U")
                    featurePills.forEach { pill ->
                      Box(
                        modifier = Modifier
                          .padding(start = 6.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .background(Color(0x18FFFFFF))
                          .border(0.5.dp, Color(0x30FFFFFF), RoundedCornerShape(8.dp))
                          .padding(horizontal = 8.dp, vertical = 3.dp)
                      ) {
                        Text(pill, color = Color(0xFFE0E0EA), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  // Action Buttons
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    // Quick Link Button
                    val quickInteraction = remember { MutableInteractionSource() }
                    val isQuickPressed by quickInteraction.collectIsPressedAsState()
                    val quickScale by animateFloatAsState(
                      targetValue = if (isQuickPressed) 0.92f else 1.0f,
                      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                      label = "quickHeroScale"
                    )

                    Box(
                      modifier = Modifier
                        .scale(quickScale)
                        .height(46.dp)
                        .liquidGlassEffect(
                          shape = RoundedCornerShape(14.dp),
                          glowTint = Color(0xFF00E5FF)
                        )
                        .clickable(
                          interactionSource = quickInteraction,
                          indication = null,
                          onClick = onOpenQuickLink
                        )
                        .padding(horizontal = 14.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        TodDirectStreamLinkIcon(modifier = Modifier.size(18.dp), tint = Color(0xFF00E5FF))
                        Text(
                          text = "رابط سريع",
                          color = Color.White,
                          fontSize = 13.sp,
                          fontFamily = ThmanyahFontFamily,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }

                    // Primary Connect Xtream Button
                    val playInteraction = remember { MutableInteractionSource() }
                    val isPlayPressed by playInteraction.collectIsPressedAsState()
                    val playScale by animateFloatAsState(
                      targetValue = if (isPlayPressed) 0.94f else 1.0f,
                      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                      label = "heroPlayScale"
                    )

                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .scale(playScale)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                          Brush.horizontalGradient(
                            listOf(Color(0xFF0A84FF), Color(0xFF0055D4), Color(0xFF003CB3))
                          )
                        )
                        .border(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF64D2FF), Color(0x40FFFFFF))), RoundedCornerShape(14.dp))
                        .clickable(
                          interactionSource = playInteraction,
                          indication = null,
                          onClick = onOpenProfile
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        TodXtreamServerIcon(modifier = Modifier.size(20.dp), tint = Color.White)
                        Text(
                          text = "تسجيل الدخول إلى سيرفرك",
                          color = Color.White,
                          fontSize = 13.5.sp,
                          fontFamily = ThmanyahFontFamily,
                          fontWeight = FontWeight.Black
                        )
                      }
                    }
                  }
                }
              }
            } else {
              // Live Channel Hero Mode
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 6.dp)
                  .height(310.dp)
                .clip(RoundedCornerShape(24.dp))
                .liquidGlassEffect(shape = RoundedCornerShape(24.dp), glowTint = Color(0xFF0A84FF))
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(activeHero.backdropGradient)
                )

                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(TodGradients.HeroScrim)
                )

                IconButton(
                  onClick = { isMuted = !isMuted },
                  modifier = Modifier
                    .padding(top = 14.dp, start = 14.dp)
                    .size(36.dp)
                    .liquidGlassEffect(shape = CircleShape, glowTint = Color(0xFF0A84FF))
                    .align(Alignment.TopStart)
                ) {
                  Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "الصوت",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                }

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                  horizontalAlignment = Alignment.End
                ) {
                  AnimatedContent(
                    targetState = activeHero,
                    transitionSpec = {
                      fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "heroCrossfade"
                  ) { hero ->
                    Column(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalAlignment = Alignment.End
                    ) {
                      if (hero.tags.isNotEmpty()) {
                        Row(
                          horizontalArrangement = Arrangement.spacedBy(6.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          hero.tags.forEach { tag ->
                            Box(
                              modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33FFFFFF))
                                .border(0.5.dp, Color(0x44FFFFFF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                              Text(tag, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                          }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                      }

                      if (hero.isLive) {
                        PulsingLiveBadge()
                        Spacer(modifier = Modifier.height(6.dp))
                      }

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
                            fontFamily = ThmanyahFontFamily,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.End,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                          )
                          Text(
                            text = hero.subtitle,
                            color = Color(0xFFD0D0D8),
                            fontSize = 12.sp,
                            fontFamily = ThmanyahFontFamily,
                            textAlign = TextAlign.End
                          )
                        }

                        if (!hero.channel?.iconUrl.isNullOrBlank()) {
                          Spacer(modifier = Modifier.width(12.dp))
                          Box(
                            modifier = Modifier
                              .size(48.dp)
                              .liquidGlassEffect(shape = RoundedCornerShape(14.dp), glowTint = Color(0xFF0A84FF))
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

                  Spacer(modifier = Modifier.height(12.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    val replayInteraction = remember { MutableInteractionSource() }
                    val isReplayPressed by replayInteraction.collectIsPressedAsState()
                    val replayScale by animateFloatAsState(
                      targetValue = if (isReplayPressed) 0.88f else 1.0f,
                      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                      label = "replayScale"
                    )

                    Box(
                      modifier = Modifier
                        .scale(replayScale)
                        .size(42.dp)
                        .liquidGlassEffect(shape = RoundedCornerShape(14.dp), glowTint = Color(0xFF0A84FF))
                        .clickable(
                          interactionSource = replayInteraction,
                          indication = null
                        ) {
                          activeHero.channel?.let { onPlayChannel(it, allChannels, "Hero Replay") }
                        },
                      contentAlignment = Alignment.Center
                    ) {
                      TodServerSyncIcon(modifier = Modifier.size(20.dp), tint = Color.White)
                    }

                    val addInteraction = remember { MutableInteractionSource() }
                    val isAddPressed by addInteraction.collectIsPressedAsState()
                    val addScale by animateFloatAsState(
                      targetValue = if (isAddPressed) 0.88f else 1.0f,
                      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                      label = "addHeroScale"
                    )

                    Box(
                      modifier = Modifier
                        .scale(addScale)
                        .size(42.dp)
                        .liquidGlassEffect(shape = RoundedCornerShape(14.dp), glowTint = Color(0xFF0A84FF))
                        .clickable(
                          interactionSource = addInteraction,
                          indication = null
                        ) { /* Watchlist */ },
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    val playInteraction = remember { MutableInteractionSource() }
                    val isPlayPressed by playInteraction.collectIsPressedAsState()
                    val playScale by animateFloatAsState(
                      targetValue = if (isPlayPressed) 0.94f else 1.0f,
                      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                      label = "heroPlayScale"
                    )

                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .scale(playScale)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF0055D4))))
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                        .clickable(
                          interactionSource = playInteraction,
                          indication = null
                        ) {
                          activeHero.channel?.let {
                            onPlayChannel(it, allChannels, "TOD Hero")
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
                          color = Color.White,
                          fontSize = 14.sp,
                          fontFamily = ThmanyahFontFamily,
                          fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                          imageVector = Icons.Default.PlayArrow,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(20.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  if (dynamicHeroItems.size > 1) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.Center,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      dynamicHeroItems.indices.forEach { idx ->
                        val isActive = idx == currentHeroIndex
                        val pillWidth by animateDpAsState(
                          targetValue = if (isActive) 20.dp else 6.dp,
                          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                          label = "heroDotWidth_$idx"
                        )

                        Box(
                          modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(4.dp)
                            .width(pillWidth)
                            .clip(CircleShape)
                            .background(if (isActive) Color.White else Color(0x55FFFFFF))
                        )
                      }
                    }
                  }
                }
              }
            }
            Spacer(modifier = Modifier.height(12.dp))
          }
        }

      // Real Live Channels Rail directly from Xtream ("تابع الآن على الهواء")
      if (allChannels.isNotEmpty()) {
        item(key = "live_now_rail") {
          DynamicChannelRail(
            sectionTitle = "تابع الآن على الهواء",
            actionLabel = "عرض الكل",
            channels = allChannels.take(20),
            allChannels = allChannels,
            onActionClick = { selectedCategoryId = "ALL" },
            onPlayChannel = onPlayChannel
          )
          Spacer(modifier = Modifier.height(18.dp))
        }
      }

      // Dynamic Categorized Xtream Category Rails (Organized & Ordered)
        items(
          items = homeRails,
          key = { (category, _) -> "rail_${category.categoryId}" }
        ) { (category, catChannels) ->
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
          item(key = "fallback_rail") {
            DynamicChannelRail(
              sectionTitle = "جميع القنوات المتاحة",
              actionLabel = "تصفح الكل (${allChannels.size})",
              channels = allChannels.take(30),
              allChannels = allChannels,
              onActionClick = { selectedCategoryId = "ALL" },
              onPlayChannel = onPlayChannel
            )
            Spacer(modifier = Modifier.height(18.dp))
          }
        }

        // iOS 18 Translucent Glass Showcase when no channels are loaded yet
        if (allChannels.isEmpty() && !isLoading) {
          item(key = "empty_channels_state") {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = "استكشف إمكانيات TOD Pro",
                color = Color.White,
                fontSize = 16.5.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
              )

              // Card 1: Xtream Server
              val card1Interaction = remember { MutableInteractionSource() }
              val isCard1Pressed by card1Interaction.collectIsPressedAsState()
              val card1Scale by animateFloatAsState(
                targetValue = if (isCard1Pressed) 0.97f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "card1Scale"
              )

              Box(
                modifier = Modifier
                  .scale(card1Scale)
                  .fillMaxWidth()
                  .liquidGlassEffect(
                    shape = RoundedCornerShape(20.dp),
                    glowTint = Color(0xFF0A84FF)
                  )
                  .clickable(
                    interactionSource = card1Interaction,
                    indication = null,
                    onClick = onOpenProfile
                  )
                  .padding(16.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFF59E0B))))
                      .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    TodXtreamServerArtIcon(modifier = Modifier.size(28.dp))
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "ربط اشتراك الاكستريم (Xtream)",
                      color = Color.White,
                      fontSize = 14.5.sp,
                      fontFamily = ThmanyahFontFamily,
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "أدخل بيانات خادمك للوصول إلى آلاف القنوات الرياضية والعالمية",
                      color = DarkTextSecondary,
                      fontSize = 11.5.sp,
                      fontFamily = ThmanyahFontFamily,
                      lineHeight = 16.sp
                    )
                  }

                  Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color(0xFF0A84FF),
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              // Card 2: Quick Direct Link
              val card2Interaction = remember { MutableInteractionSource() }
              val isCard2Pressed by card2Interaction.collectIsPressedAsState()
              val card2Scale by animateFloatAsState(
                targetValue = if (isCard2Pressed) 0.97f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "card2Scale"
              )

              Box(
                modifier = Modifier
                  .scale(card2Scale)
                  .fillMaxWidth()
                  .liquidGlassEffect(
                    shape = RoundedCornerShape(20.dp),
                    glowTint = Color(0xFF0A84FF)
                  )
                  .clickable(
                    interactionSource = card2Interaction,
                    indication = null,
                    onClick = onOpenQuickLink
                  )
                  .padding(16.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF0284C7))))
                      .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    TodQuickLinkArtIcon(modifier = Modifier.size(28.dp))
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "تشغيل رابط بث سريع (M3U8)",
                      color = Color.White,
                      fontSize = 14.5.sp,
                      fontFamily = ThmanyahFontFamily,
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "شاهد أي بث مباشر فوري بدون تسجيل وبجودة فائقة",
                      color = DarkTextSecondary,
                      fontSize = 11.5.sp,
                      fontFamily = ThmanyahFontFamily,
                      lineHeight = 16.sp
                    )
                  }

                  Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              // Card 3: Performance Engine
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .liquidGlassEffect(
                    shape = RoundedCornerShape(20.dp),
                    glowTint = Color(0xFF0A84FF)
                  )
                  .padding(16.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(Brush.linearGradient(listOf(Color(0xFF30D158), Color(0xFF0E8A38))))
                      .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    TodAntiBufferEngineArtIcon(modifier = Modifier.size(28.dp))
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "محرك مانع التقطيع الذكي",
                      color = Color.White,
                      fontSize = 14.5.sp,
                      fontFamily = ThmanyahFontFamily,
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "معالجة فورية وتخزين مؤقت سلس لتشغيل خالٍ من التوقفات",
                      color = DarkTextSecondary,
                      fontSize = 11.5.sp,
                      fontFamily = ThmanyahFontFamily,
                      lineHeight = 16.sp
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

/**
 * iOS-Fidelity Corporate Channel Card for Grid View with Rock-Solid Fixed Alignment
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
    targetValue = if (isPressed) 0.95f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "gridCardScale"
  )

  val qualityLabel = when {
    channel.name.contains("4K", ignoreCase = true) -> "4K UHD"
    channel.name.contains("FHD", ignoreCase = true) || channel.name.contains("1080", ignoreCase = true) -> "1080p FHD"
    channel.name.contains("HD", ignoreCase = true) || channel.name.contains("720", ignoreCase = true) -> "720p HD"
    else -> "HD"
  }

  Box(
    modifier = Modifier
      .scale(scale)
      .fillMaxWidth()
      .height(148.dp)
      .liquidGlassEffect(shape = RoundedCornerShape(22.dp), glowTint = Color(0xFF0A84FF), isElevated = true)
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) { onPlayChannel(channel, allChannels, categoryName) }
      .padding(13.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Row: Channel Icon + Live Badge (Stationary, NEVER shifts!)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Channel Icon Container in Glass frame
        Box(
          modifier = Modifier
            .size(42.dp)
            .liquidGlassEffect(shape = RoundedCornerShape(12.dp), glowTint = Color(0xFF0A84FF)),
          contentAlignment = Alignment.Center
        ) {
          if (!channel.iconUrl.isNullOrBlank()) {
            AsyncImage(
              model = channel.iconUrl,
              contentDescription = null,
              modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
              contentScale = ContentScale.Fit
            )
          } else {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = Color(0xFF0A84FF),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Live Pulsing Beacon
        PulsingLiveBadge()
      }

      // Middle: Clean readable channel name
      Text(
        text = cleanChannelName(channel.name),
        color = Color.White,
        fontSize = 13.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 17.sp,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      )

      // Bottom Row: Quality Badge + Category Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        VideoQualityBadge(qualityText = qualityLabel)
        Text(
          text = categoryName.take(16),
          color = DarkTextTertiary,
          fontSize = 10.5.sp,
          fontFamily = ThmanyahFontFamily,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

/**
 * iOS Inset Grouped Table Row for long channel names
 */
@Composable
fun CorporateChannelListRow(
  channel: XtreamChannel,
  channelIndex: Int,
  allChannels: List<XtreamChannel>,
  categoryName: String,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.98f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "listRowScale"
  )

  val qualityLabel = when {
    channel.name.contains("4K", ignoreCase = true) -> "4K"
    channel.name.contains("FHD", ignoreCase = true) || channel.name.contains("1080", ignoreCase = true) -> "FHD"
    else -> "HD"
  }

  Row(
    modifier = Modifier
      .scale(scale)
      .fillMaxWidth()
      .height(74.dp)
      .liquidGlassEffect(shape = RoundedCornerShape(20.dp), glowTint = Color(0xFF0A84FF), isElevated = true)
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) { onPlayChannel(channel, allChannels, categoryName) }
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = String.format("%02d", channelIndex),
        color = Color(0xFF0A84FF),
        fontSize = 12.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(26.dp)
      )

      Box(
        modifier = Modifier
          .size(44.dp)
          .liquidGlassEffect(shape = RoundedCornerShape(12.dp), glowTint = Color(0xFF0A84FF)),
        contentAlignment = Alignment.Center
      ) {
        if (!channel.iconUrl.isNullOrBlank()) {
          AsyncImage(
            model = channel.iconUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(4.dp),
            contentScale = ContentScale.Fit
          )
        } else {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0A84FF), modifier = Modifier.size(20.dp))
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = cleanChannelName(channel.name),
          color = Color.White,
          fontSize = 13.5.sp,
          fontFamily = ThmanyahFontFamily,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = categoryName,
          color = DarkTextSecondary,
          fontSize = 11.sp,
          fontFamily = ThmanyahFontFamily,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      VideoQualityBadge(qualityText = qualityLabel)
      PulsingLiveBadge()
      Icon(
        Icons.Default.KeyboardArrowLeft,
        contentDescription = null,
        tint = Color(0x66FFFFFF),
        modifier = Modifier.size(16.dp)
      )
    }
  }
}

/**
 * Dynamic Channel Rail for each Xtream Category Group with Modern iOS Styling
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
    // Apple iOS Style Section Header Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val actionInteraction = remember { MutableInteractionSource() }
      val isActionPressed by actionInteraction.collectIsPressedAsState()
      val actionScale by animateFloatAsState(
        targetValue = if (isActionPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "railActionScale"
      )

      Row(
        modifier = Modifier
          .scale(actionScale)
          .liquidGlassEffect(
            shape = RoundedCornerShape(14.dp),
            glowTint = Color(0xFF0A84FF)
          )
          .clickable(
            interactionSource = actionInteraction,
            indication = null,
            onClick = onActionClick
          )
          .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowLeft,
          contentDescription = null,
          tint = Color(0xFF0A84FF),
          modifier = Modifier.size(15.dp)
        )
        Text(
          text = actionLabel,
          color = Color.White,
          fontSize = 11.5.sp,
          fontFamily = ThmanyahFontFamily,
          fontWeight = FontWeight.Bold
        )
      }

      Text(
        text = sectionTitle,
        color = Color.White,
        fontSize = 18.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.2.sp
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(channels, key = { it.streamId }) { channel ->
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
          targetValue = if (isPressed) 0.94f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
          label = "railCardScale"
        )

        val qualityLabel = when {
          channel.name.contains("4K", ignoreCase = true) -> "4K UHD"
          channel.name.contains("FHD", ignoreCase = true) || channel.name.contains("1080", ignoreCase = true) -> "1080p FHD"
          channel.name.contains("HD", ignoreCase = true) || channel.name.contains("720", ignoreCase = true) -> "720p HD"
          else -> "HD"
        }

        Box(
          modifier = Modifier
            .scale(scale)
            .width(182.dp)
            .height(134.dp)
            .liquidGlassEffect(shape = RoundedCornerShape(20.dp), glowTint = Color(0xFF0A84FF))
            .clickable(
              interactionSource = interactionSource,
              indication = null
            ) { onPlayChannel(channel, allChannels, sectionTitle) }
            .padding(12.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            // Top Row: Fixed Dimension Channel Icon + Live Beacon
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .liquidGlassEffect(shape = RoundedCornerShape(11.dp), glowTint = Color(0xFF0A84FF)),
                contentAlignment = Alignment.Center
              ) {
                if (!channel.iconUrl.isNullOrBlank()) {
                  AsyncImage(
                    model = channel.iconUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(3.dp),
                    contentScale = ContentScale.Fit
                  )
                } else {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0A84FF), modifier = Modifier.size(18.dp))
                }
              }

              PulsingLiveBadge()
            }

            // Middle: Channel Name
            Text(
              text = cleanChannelName(channel.name),
              color = Color.White,
              fontSize = 12.5.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Bold,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              lineHeight = 16.sp,
              modifier = Modifier.fillMaxWidth()
            )

            // Bottom: Quality
            VideoQualityBadge(qualityText = qualityLabel)
          }
        }
      }
    }
  }
}

/**
 * Intelligent Channel Name Formatter:
 * Cleans ugly technical tags and language prefixes while keeping the full channel name intact.
 */
fun cleanChannelName(raw: String): String {
  var text = raw.trim()
  if (text.isEmpty()) return "قناة"

  // Remove common prefix markers like "AR | ", "AR: ", "EN | ", "FR | ", "OSN - "
  val prefixRegex = Regex("^(?:[A-Z]{2,4}\\s*[-:|/]\\s*)", RegexOption.IGNORE_CASE)
  text = text.replace(prefixRegex, "").trim()

  // Remove technical codec/quality tags inside square or round brackets
  text = text.replace(Regex("\\[(?:HEVC|H\\.?265|H\\.?264|VIP|4K|FHD|HD|SD|RAW|LOW|50FPS|60FPS)\\]", RegexOption.IGNORE_CASE), "")
    .replace(Regex("\\((?:HEVC|H\\.?265|H\\.?264|VIP|4K|FHD|HD|SD|RAW|LOW|50FPS|60FPS)\\)", RegexOption.IGNORE_CASE), "")
    .replace(Regex("\\s+"), " ")
    .trim()

  return if (text.isBlank()) raw.trim() else text
}

