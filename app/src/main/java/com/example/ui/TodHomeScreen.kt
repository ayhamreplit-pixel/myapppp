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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import com.example.ui.theme.DarkTextTertiary
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

  // Map channels grouped by Xtream category dynamically with complete O(N) coverage and robust matching
  val channelsByCategory: Map<XtreamCategory, List<XtreamChannel>> = remember(allChannels, xtreamCategories) {
    if (allChannels.isEmpty()) {
      emptyMap()
    } else {
      val map = mutableMapOf<XtreamCategory, List<XtreamChannel>>()

      // Clean category matching
      xtreamCategories.forEach { category ->
        if (category.categoryId != "ALL") {
          val catTrim = category.categoryId.trim()
          val matched = allChannels.filter { ch ->
            val chCat = ch.categoryId?.trim() ?: ""
            chCat.equals(catTrim, ignoreCase = true) ||
            (chCat.toIntOrNull() != null && catTrim.toIntOrNull() != null && chCat.toInt() == catTrim.toInt()) ||
            (chCat.isEmpty() && category.categoryName.trim().equals("عام", ignoreCase = true))
          }
          if (matched.isNotEmpty()) {
            map[category.copy(channelCount = matched.size)] = matched
          }
        }
      }

      // If no categories matched or categories empty, group dynamically by categoryId or default
      if (map.isEmpty()) {
        val channelsByCatId = allChannels.groupBy { it.categoryId?.trim()?.ifBlank { "القنوات العامة" } ?: "القنوات العامة" }
        channelsByCatId.forEach { (catKey, list) ->
          map[XtreamCategory(catKey, catKey, list.size)] = list
        }
      }

      map
    }
  }

  // Robust category channels for selected category view
  val currentCategoryChannels = remember(allChannels, selectedCategoryId) {
    if (selectedCategoryId == null || selectedCategoryId == "ALL") {
      allChannels
    } else {
      val targetCat = selectedCategoryId?.trim() ?: ""
      val matched = allChannels.filter { ch ->
        val chCat = ch.categoryId?.trim() ?: ""
        chCat.equals(targetCat, ignoreCase = true) ||
        (chCat.toIntOrNull() != null && targetCat.toIntOrNull() != null && chCat.toInt() == targetCat.toInt())
      }
      if (matched.isNotEmpty()) matched
      else {
        // Fallback by category name
        val catName = xtreamCategories.find { it.categoryId.trim() == targetCat }?.categoryName?.trim()
        if (!catName.isNullOrBlank()) {
          allChannels.filter { (it.categoryId?.trim() ?: "").equals(catName, ignoreCase = true) }
        } else emptyList()
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
    // 1. Ultra-Clean Modern iOS Seamless Navigation Header (Continuous Edge-to-Edge)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xEE12131C),
              Color(0x880D0E15),
              Color.Transparent
            )
          )
        )
        .padding(top = 4.dp, bottom = 6.dp)
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
            .size(36.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(listOf(TodGold, Color(0xFFFF9500)))
            )
            .border(1.5.dp, Color(0x55FFFFFF), CircleShape)
            .clickable(
              interactionSource = avatarInteraction,
              indication = null,
              onClick = onOpenProfile
            ),
          contentAlignment = Alignment.Center
        ) {
          Text("M", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
        }

        // Clean Official TOD by beIN Logo
        Box(
          contentAlignment = Alignment.Center
        ) {
          TodLogo(fontSize = 22, showSubtext = true)
        }

        // Modern Apple iOS Glass Pill Button: Quick Link Player
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
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x220A84FF))
            .border(1.dp, Color(0x450A84FF), RoundedCornerShape(18.dp))
            .clickable(
              interactionSource = quickInteraction,
              indication = null,
              onClick = onOpenQuickLink
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = "رابط سريع",
              tint = Color(0xFF0A84FF),
              modifier = Modifier.size(15.dp)
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
            .clip(RoundedCornerShape(20.dp))
            .background(
              if (isAllSelected) TodGradients.LiquidGold
              else Brush.horizontalGradient(listOf(Color(0x18FFFFFF), Color(0x10FFFFFF)))
            )
            .border(
              width = 1.dp,
              color = if (isAllSelected) Color(0xFFFFD54F) else Color(0x22FFFFFF),
              shape = RoundedCornerShape(20.dp)
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
            color = if (isAllSelected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isAllSelected) FontWeight.Black else FontWeight.SemiBold
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
              .clip(RoundedCornerShape(20.dp))
              .background(
                if (isSelected) TodGradients.LiquidGold
                else Brush.horizontalGradient(listOf(Color(0x18FFFFFF), Color(0x10FFFFFF)))
              )
              .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFFFFD54F) else Color(0x22FFFFFF),
                shape = RoundedCornerShape(20.dp)
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
              color = if (isSelected) Color.Black else Color.White,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
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
          .background(DarkBg)
      ) {
        // Apple iOS Seamless Category Navigation Header (Continuous Edge-to-Edge)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0xEE12131C),
                  Color(0x880D0E15),
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
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x28FFFFFF))
                .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(16.dp))
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
                tint = TodGold,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("الرئيسية", color = TodGold, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
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
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x24FFFFFF))
                .border(0.75.dp, Color(0x28FFFFFF), RoundedCornerShape(10.dp))
                .padding(3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (!isListView) TodGold else Color.Transparent)
                  .clickable { isListView = false }
                  .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.GridView, contentDescription = null, tint = if (!isListView) Color.Black else Color.White, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("شبكة", color = if (!isListView) Color.Black else Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isListView) TodGold else Color.Transparent)
                  .clickable { isListView = true }
                  .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = if (isListView) Color.Black else Color.White, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("قائمة", color = if (isListView) Color.Black else Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
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
        contentPadding = PaddingValues(bottom = 32.dp)
      ) {
        // 3. Apple TV Style Floating Cinematic Glass Carousel Banner
        item(key = "hero_banner") {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 6.dp)
              .height(310.dp)
              .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xCC000000),
                ambientColor = Color(0x66000000)
              )
              .clip(RoundedCornerShape(24.dp))
              .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                  colors = listOf(Color(0x45FFFFFF), Color(0x10FFFFFF))
                ),
                shape = RoundedCornerShape(24.dp)
              )
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
                .padding(top = 14.dp, start = 14.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x88000000))
                .border(0.75.dp, Color(0x33FFFFFF), CircleShape)
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
              horizontalAlignment = Alignment.End
            ) {
              // Animated Hero Content with Cross-fade
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
                  // Badges / Tags row
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
                        color = Color(0xFFD0D0D8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End
                      )
                    }

                    if (!hero.channel?.iconUrl.isNullOrBlank()) {
                      Spacer(modifier = Modifier.width(12.dp))
                      Box(
                        modifier = Modifier
                          .size(48.dp)
                          .clip(RoundedCornerShape(12.dp))
                          .background(Color(0x99181824))
                          .border(1.dp, TodGold.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
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

              // Hero Action Buttons with Physics-Based Touch Feedback
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Replay Button (↺)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x28FFFFFF))
                    .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(
                      interactionSource = replayInteraction,
                      indication = null
                    ) {
                      activeHero.channel?.let { onPlayChannel(it, allChannels, "Hero Replay") }
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                // Add Button (+)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x28FFFFFF))
                    .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(
                      interactionSource = addInteraction,
                      indication = null
                    ) { /* Watchlist */ },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                // Apple-Style Primary Action Button: "تابع الآن ▶"
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
                    .background(TodGradients.LiquidGold)
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                    .clickable(
                      interactionSource = playInteraction,
                      indication = null
                    ) {
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

              Spacer(modifier = Modifier.height(10.dp))

              // Carousel Indicator Pills
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
          Spacer(modifier = Modifier.height(12.dp))
        }

        // If no channels yet, show clean connection card
        if (allChannels.isEmpty() && !isLoading) {
          item(key = "empty_conn_card") {
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
        }

        // 4. "تابع الآن على الهواء" Quick Live Channels Rail
        if (allChannels.isNotEmpty()) {
          item(key = "live_now_rail") {
            DynamicChannelRail(
              sectionTitle = "تابع الآن على الهواء",
              actionLabel = "عرض الكل",
              channels = allChannels.take(15),
              allChannels = allChannels,
              onActionClick = { selectedCategoryId = "ALL" },
              onPlayChannel = onPlayChannel
            )
            Spacer(modifier = Modifier.height(18.dp))
          }
        }

        // 5. Dynamic Xtream Category Rails (Show top 15 rails on home feed with lazy composition)
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
      .height(138.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(TodGradients.CardGlass)
      .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(16.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) { onPlayChannel(channel, allChannels, categoryName) }
      .padding(12.dp)
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
        // Channel Icon Container (Always 38dp x 38dp)
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E28))
            .border(0.75.dp, Color(0xFF383848), RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          if (!channel.iconUrl.isNullOrBlank()) {
            AsyncImage(
              model = channel.iconUrl,
              contentDescription = null,
              modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
              contentScale = ContentScale.Fit
            )
          } else {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = TodGold,
              modifier = Modifier.size(18.dp)
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
        fontSize = 12.5.sp,
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
          fontSize = 10.sp,
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
      .height(68.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(TodGradients.CardGlass)
      .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(14.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) { onPlayChannel(channel, allChannels, categoryName) }
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = String.format("%02d", channelIndex),
        color = DarkTextTertiary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(26.dp)
      )

      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF1E1E28))
          .border(0.75.dp, Color(0xFF383848), RoundedCornerShape(10.dp)),
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
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TodGold, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = cleanChannelName(channel.name),
          color = Color.White,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = categoryName,
          color = DarkTextSecondary,
          fontSize = 11.sp,
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
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0x18FFFFFF))
          .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
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
          tint = TodGold,
          modifier = Modifier.size(15.dp)
        )
        Text(
          text = actionLabel,
          color = TodGold,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Text(
        text = sectionTitle,
        color = Color.White,
        fontSize = 18.sp,
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
            .width(176.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TodGradients.CardGlass)
            .border(
              width = 1.dp,
              brush = Brush.verticalGradient(
                colors = listOf(Color(0x35FFFFFF), Color(0x10FFFFFF))
              ),
              shape = RoundedCornerShape(16.dp)
            )
            .clickable(
              interactionSource = interactionSource,
              indication = null
            ) { onPlayChannel(channel, allChannels, sectionTitle) }
            .padding(11.dp)
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
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF1E1E28))
                  .border(0.75.dp, Color(0xFF383848), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                if (!channel.iconUrl.isNullOrBlank()) {
                  AsyncImage(
                    model = channel.iconUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(2.5.dp),
                    contentScale = ContentScale.Fit
                  )
                } else {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TodGold, modifier = Modifier.size(16.dp))
                }
              }

              PulsingLiveBadge()
            }

            // Middle: Channel Name
            Text(
              text = cleanChannelName(channel.name),
              color = Color.White,
              fontSize = 12.sp,
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

