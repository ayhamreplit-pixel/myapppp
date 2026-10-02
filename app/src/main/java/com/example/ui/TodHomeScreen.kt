package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import com.example.data.SportsBackendRepository
import com.example.model.BroadcastStream
import com.example.model.SportsMatch
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold

enum class TodTopSection(val title: String) {
  HOME("الرئيسية"),
  MATCHES("المباريات"),
  NEWS("الأخبار"),
  LIVE_TV("قنوات مباشرة"),
  COMPETITIONS("المنافسات"),
  SHOWS("ملخصات وبرامج")
}

/**
 * Official TOD Sports & Live Broadcast Home Hub (Matching TOD Screenshots 1:1)
 * Purely live sports, matches, tournaments and beIN channels.
 * Fully responsive across all devices (Phones, Foldables, Tablets, TV).
 */
@Composable
fun TodHomeScreen(
  xtreamCategories: List<XtreamCategory>,
  allChannels: List<XtreamChannel>,
  isLoading: Boolean,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>, String) -> Unit,
  onOpenMatchDetail: (SportsMatch) -> Unit,
  onPlayMatchDirectly: (SportsMatch) -> Unit,
  onOpenProfile: () -> Unit,
  onOpenSearch: () -> Unit = {},
  sportsBackendRepo: SportsBackendRepository,
  initialTopSection: TodTopSection = TodTopSection.HOME,
  modifier: Modifier = Modifier
) {
  var activeTopSection by remember(initialTopSection) { mutableStateOf(initialTopSection) }

  // Channel Category selection for LIVE_TV tab
  var selectedChannelCategoryId by remember { mutableStateOf<String?>(null) }
  var isChannelListView by remember { mutableStateOf(false) }

  // Date selection for MATCHES tab
  val dateOptions = listOf("أمس", "اليوم", "غداً", "الجمعة", "السبت", "الأحد")
  var selectedDate by remember { mutableStateOf("اليوم") }
  var selectedTournamentFilter by remember { mutableStateOf<String?>(null) }
  var selectedMatchStatusFilter by remember { mutableStateOf("ALL") }
  var isGroupingByTournament by remember { mutableStateOf(true) }
  var matchForServerSelection by remember { mutableStateOf<SportsMatch?>(null) }

  val coroutineScope = rememberCoroutineScope()
  val isSyncing by sportsBackendRepo.isSyncing.collectAsState()

  // Sync matches when selectedDate changes
  LaunchedEffect(selectedDate) {
    val dateParam = when (selectedDate) {
      "أمس" -> "yesterday"
      "غداً" -> "tomorrow"
      else -> "today"
    }
    sportsBackendRepo.syncForDate(dateParam)
  }

  // Observe Sports Backend Data (AlwaysData or synced backend)
  val sportsMatches by sportsBackendRepo.matches.collectAsState()
  val competitions by sportsBackendRepo.competitions.collectAsState()
  val sportsShows by sportsBackendRepo.shows.collectAsState()
  val builtInChannels by sportsBackendRepo.sportsChannels.collectAsState()

  // Effective Channels list (combines server backend channels and playlist channels)
  val effectiveChannels = remember(allChannels, builtInChannels) {
    (builtInChannels + allChannels).distinctBy { it.streamId.ifBlank { it.name } }
  }

  // Filter live and upcoming matches
  val liveMatches = remember(sportsMatches) { sportsMatches.filter { it.isLive } }
  val upcomingMatches = remember(sportsMatches) { sportsMatches.filter { !it.isLive && !it.isEnded } }
  val featuredMatch = remember(sportsMatches) {
    sportsMatches.firstOrNull { it.id == "wales_norway_nations" }
      ?: sportsMatches.firstOrNull { it.isLive }
      ?: sportsMatches.firstOrNull()
  }

  // Handle back gesture when category/filter is open
  BackHandler(enabled = selectedChannelCategoryId != null || selectedTournamentFilter != null) {
    selectedChannelCategoryId = null
    selectedTournamentFilter = null
  }

  FluidMeshBackground(
    modifier = modifier.fillMaxSize(),
    ambientAlpha = 0.70f
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isTabletOrLandscape = screenWidth >= 700.dp
        val gridColumns = if (screenWidth >= 900.dp) 4 else if (isTabletOrLandscape) 3 else 2

        // =========================================================================
        // MAIN CONTENT (Starts at y=0 behind floating top header)
        // =========================================================================
        Box(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 1200.dp)
            .align(Alignment.TopCenter)
        ) {
          when (activeTopSection) {
            TodTopSection.HOME -> {
              // =====================================================================
              // TAB 1: TOD HOME FEED (Multi-Poster Hero Carousel + Live Rails + Fixtures + beIN Channels)
              // =====================================================================
              val heroMatches = remember(sportsMatches) {
                val withPoster = sportsMatches.filter { !it.bannerUrl.isNullOrBlank() }
                if (withPoster.isNotEmpty()) withPoster else sportsMatches.take(5)
              }

              val allList = effectiveChannels.ifEmpty { sportsBackendRepo.sportsChannels.value }

              // Dynamic Category Groups (so ANY channels added to server appear directly on HOME)
              val dynamicCategoryGroups = remember(allList) {
                val map = linkedMapOf<String, MutableList<XtreamChannel>>()
                allList.forEach { ch ->
                  val cat = ch.categoryId?.takeIf { it.isNotBlank() } ?: "قنوات البث المباشر"
                  map.getOrPut(cat) { mutableListOf() }.add(ch)
                }
                map
              }

              // Categorized Match Lists (Screenshots 1, 2, 3, 4)
              val liveMatches = remember(sportsMatches) { sportsMatches.filter { it.isLive } }
              val upcomingMatches = remember(sportsMatches) { sportsMatches.filter { !it.isLive && !it.isEnded } }
              val featuredMatches = remember(sportsMatches) { sportsMatches.filter { it.isEnded } }
              val sportsNews by sportsBackendRepo.news.collectAsState()
              val announcementConfig by sportsBackendRepo.announcement.collectAsState()

              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
              ) {
                // 0. Top Breaking Announcement Bar
                if (announcementConfig.isEnabled && announcementConfig.message.isNotBlank()) {
                  item(key = "announcement_bar") {
                    TodMarqueeAnnouncementBar(config = announcementConfig)
                    Spacer(modifier = Modifier.height(4.dp))
                  }
                }

                // 1. Dynamic Multi-Poster Hero Banner Carousel
                if (heroMatches.isNotEmpty()) {
                  item(key = "hero_carousel") {
                    TodHeroBannerCarousel(
                      matches = heroMatches,
                      onPlayMatch = onPlayMatchDirectly,
                      onOpenDetails = onOpenMatchDetail
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                  }
                }

                // 2. بطاقات مباريات كرة القدم (مطابقة 100% للصور 1 و 2 و 3 و 4)
                if (liveMatches.isNotEmpty()) {
                  item(key = "live_matches_rail") {
                    TodLiveSportsRail(
                      matches = liveMatches,
                      title = "البث المباشر - رياضات متعددة",
                      onPlayMatch = onPlayMatchDirectly,
                      onOpenDetails = onOpenMatchDetail
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                  }
                }

                // 3. آخر الأخبار الرياضية الحية (Yallakora Live News Feed)
                if (sportsNews.isNotEmpty()) {
                  item(key = "sports_news_feed_rail") {
                    TodSportsNewsRail(
                      news = sportsNews,
                      onViewAllClick = { activeTopSection = TodTopSection.NEWS }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                  }
                }

                if (upcomingMatches.isNotEmpty()) {
                  item(key = "upcoming_matches_rail") {
                    TodLiveSportsRail(
                      matches = upcomingMatches,
                      title = "الرياضة القادمة",
                      onPlayMatch = onPlayMatchDirectly,
                      onOpenDetails = onOpenMatchDetail
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                  }
                }

                if (featuredMatches.isNotEmpty()) {
                  item(key = "featured_matches_rail") {
                    TodLiveSportsRail(
                      matches = featuredMatches,
                      title = "أفضل مباريات كرة القدم مباشرةً هذا الأسبوع",
                      onPlayMatch = onPlayMatchDirectly,
                      onOpenDetails = onOpenMatchDetail
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                  }
                }

                // 3. جميع قنوات البث المباشر المضافة من السيرفر مقسمة تلقائياً حسب الفئات
                dynamicCategoryGroups.forEach { (catTitle, channelsInCat) ->
                  if (channelsInCat.isNotEmpty()) {
                    item(key = "channels_cat_$catTitle") {
                      TodChannelsGroupRail(
                        title = catTitle,
                        channels = channelsInCat,
                        onPlayChannel = { channel ->
                          onPlayChannel(channel, allList, catTitle)
                        }
                      )
                    }
                  }
                }

                // 4. Competitions Rail ("المنافسات") (Screenshots 10, 16, 18)
                item(key = "competitions_rail") {
                  TodCompetitionsRail(
                    competitions = competitions,
                    onSelectCompetition = { comp ->
                      val matchInComp = sportsMatches.firstOrNull { it.tournament.contains(comp.name.take(6), ignoreCase = true) }
                      if (matchInComp != null) {
                        onOpenMatchDetail(matchInComp)
                      } else {
                        activeTopSection = TodTopSection.MATCHES
                        selectedTournamentFilter = comp.name
                      }
                    }
                  )
                  Spacer(modifier = Modifier.height(18.dp))
                }

                // 5. Highlights & Shows Rail ("إعادات وملخصات كرة القدم") (Screenshots 15, 17, 18)
                item(key = "sports_shows_rail") {
                  TodSportsShowsRail(
                    shows = sportsShows,
                    onPlayShow = { show ->
                      val dummyChannel = XtreamChannel(
                        streamId = show.id,
                        name = show.title,
                        iconUrl = show.bannerUrl,
                        categoryId = "برامج رياضية",
                        playUrl = show.streamUrl
                      )
                      onPlayChannel(dummyChannel, emptyList(), show.title)
                    }
                  )
                  Spacer(modifier = Modifier.height(24.dp))
                }
              }
            }

        TodTopSection.MATCHES -> {
          // =====================================================================
          // TAB 2: TOD MATCHES & FIXTURES SCHEDULE (Screenshots 12, 13, 15, 18)
          // =====================================================================
          Column(modifier = Modifier.fillMaxSize()) {
            // Day selector row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              dateOptions.forEach { day ->
                val isSelected = selectedDate == day
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) TodGold else Color(0x18FFFFFF))
                    .clickable { selectedDate = day }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = day,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp,
                    fontFamily = ThmanyahFontFamily
                  )
                }
              }
            }

            // Live Status & Action Toolbar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
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
                    .background(if (liveMatches.isNotEmpty()) Color(0xFF30D158) else Color(0x60FFFFFF))
                )
                Text(
                  text = if (isSyncing) "جاري تحديث النتائج..." else "مباريات $selectedDate (${sportsMatches.size})",
                  color = if (isSyncing) TodGold else Color(0xCCFFFFFF),
                  fontSize = 12.sp,
                  fontFamily = ThmanyahFontFamily,
                  fontWeight = FontWeight.Bold
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // Grouping Toggle
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isGroupingByTournament) Color(0x350A84FF) else Color(0x18FFFFFF))
                    .clickable { isGroupingByTournament = !isGroupingByTournament }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Layers,
                      contentDescription = null,
                      tint = if (isGroupingByTournament) Color(0xFF64D2FF) else Color(0xAAFFFFFF),
                      modifier = Modifier.size(13.dp)
                    )
                    Text(
                      text = "حسب البطولة",
                      color = if (isGroupingByTournament) Color(0xFF64D2FF) else Color(0xAAFFFFFF),
                      fontSize = 11.sp,
                      fontFamily = ThmanyahFontFamily
                    )
                  }
                }

                // Instant Refresh Button
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x18FFFFFF))
                    .clickable {
                      coroutineScope.launch {
                        val dateParam = when (selectedDate) {
                          "أمس" -> "yesterday"
                          "غداً" -> "tomorrow"
                          else -> "today"
                        }
                        sportsBackendRepo.syncForDate(dateParam, forceRefresh = true)
                      }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    if (isSyncing) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(13.dp),
                        color = TodGold,
                        strokeWidth = 1.5.dp
                      )
                    } else {
                      Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = TodGold,
                        modifier = Modifier.size(13.dp)
                      )
                    }
                    Text(
                      text = "تحديث",
                      color = TodGold,
                      fontSize = 11.sp,
                      fontFamily = ThmanyahFontFamily,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }

            // Status Filter Row (الكل، مباشر، قادمة، منتهية)
            val statusOptions = listOf(
              "ALL" to "الكل (${sportsMatches.size})",
              "LIVE" to "مباشر الآن (${sportsMatches.count { it.isLive }})",
              "UPCOMING" to "القادمة (${sportsMatches.count { !it.isLive && !it.isEnded }})",
              "ENDED" to "المنتهية (${sportsMatches.count { it.isEnded }})"
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 2.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              statusOptions.forEach { (key, label) ->
                val isSelected = selectedMatchStatusFilter == key
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) TodGold else Color(0x14FFFFFF))
                    .clickable { selectedMatchStatusFilter = key }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                  Text(
                    text = label,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp,
                    fontFamily = ThmanyahFontFamily
                  )
                }
              }
            }

            // Tournament Filter Chips
            val tournaments = remember(sportsMatches) {
              listOf("الكل") + sportsMatches.map { it.tournament }.distinct()
            }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 3.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              tournaments.forEach { tour ->
                val isSelected = (selectedTournamentFilter == null && tour == "الكل") || selectedTournamentFilter == tour
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0x350A84FF) else Color(0x10FFFFFF))
                    .border(0.75.dp, if (isSelected) Color(0xFF0A84FF) else Color(0x20FFFFFF), RoundedCornerShape(12.dp))
                    .clickable {
                      selectedTournamentFilter = if (tour == "الكل") null else tour
                    }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                  Text(
                    text = tour,
                    color = if (isSelected) Color(0xFF64D2FF) else Color(0xCCFFFFFF),
                    fontSize = 11.5.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }

            val filteredMatches = remember(sportsMatches, selectedTournamentFilter, selectedMatchStatusFilter) {
              var list = if (selectedTournamentFilter == null) sportsMatches else sportsMatches.filter { it.tournament == selectedTournamentFilter }
              when (selectedMatchStatusFilter) {
                "LIVE" -> list.filter { it.isLive }
                "UPCOMING" -> list.filter { !it.isLive && !it.isEnded }
                "ENDED" -> list.filter { it.isEnded }
                else -> list
              }
            }

            val groupedMatches = remember(filteredMatches, isGroupingByTournament) {
              if (isGroupingByTournament) {
                filteredMatches.groupBy { it.tournament }
              } else {
                mapOf("all" to filteredMatches)
              }
            }

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 100.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              if (filteredMatches.isEmpty()) {
                item(key = "empty_matches_box") {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(top = 40.dp, bottom = 60.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(60.dp)
                          .clip(CircleShape)
                          .background(Color(0x200A84FF)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.SportsSoccer,
                          contentDescription = null,
                          tint = Color(0xFF64D2FF),
                          modifier = Modifier.size(30.dp)
                        )
                      }
                      Text(
                        text = "لا توجد مباريات مسجلة لـ $selectedDate",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ThmanyahFontFamily
                      )
                      Text(
                        text = "يمكنك التبديل إلى يوم آخر أو عرض مباريات اليوم",
                        color = Color(0xFF8E9BAE),
                        fontSize = 12.sp,
                        fontFamily = ThmanyahFontFamily
                      )
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(12.dp))
                          .background(TodGold)
                          .clickable {
                            selectedDate = "اليوم"
                            selectedTournamentFilter = null
                            selectedMatchStatusFilter = "ALL"
                            coroutineScope.launch { sportsBackendRepo.syncForDate("today", forceRefresh = true) }
                          }
                          .padding(horizontal = 16.dp, vertical = 8.dp)
                      ) {
                        Text(
                          text = "عرض مباريات اليوم ⚡",
                          color = Color.Black,
                          fontWeight = FontWeight.Black,
                          fontSize = 12.5.sp,
                          fontFamily = ThmanyahFontFamily
                        )
                      }
                    }
                  }
                }
              } else if (isGroupingByTournament && selectedTournamentFilter == null) {
                groupedMatches.forEach { (tourName, matchesInTour) ->
                  item(key = "hdr_$tourName") {
                    TodTournamentSectionHeader(
                      tournamentName = tourName,
                      tournamentLogo = matchesInTour.firstOrNull()?.tournamentLogo ?: "",
                      count = matchesInTour.size
                    )
                  }
                  itemsIndexed(matchesInTour, key = { idx, match -> "${match.id}_${tourName}_$idx" }) { _, match ->
                    TodMatchScheduleCard(
                      match = match,
                      onPlayMatch = { m ->
                        if (m.servers.size > 1) {
                          matchForServerSelection = m
                        } else {
                          onPlayMatchDirectly(m)
                        }
                      },
                      onOpenDetails = onOpenMatchDetail
                    )
                  }
                }
              } else {
                itemsIndexed(filteredMatches, key = { idx, match -> "${match.id}_$idx" }) { _, match ->
                  TodMatchScheduleCard(
                    match = match,
                    onPlayMatch = { m ->
                      if (m.servers.size > 1) {
                        matchForServerSelection = m
                      } else {
                        onPlayMatchDirectly(m)
                      }
                    },
                    onOpenDetails = onOpenMatchDetail
                  )
                }
              }
            }
          }
        }

        TodTopSection.NEWS -> {
          // =====================================================================
          // TAB: DEDICATED SPORTS NEWS (Yallakora, beIN & Live Sports Feeds)
          // =====================================================================
          val sportsNews by sportsBackendRepo.news.collectAsState()
          TodDedicatedNewsScreen(
            news = sportsNews,
            onRefreshNews = { sportsBackendRepo.fetchYallakoraNews() }
          )
        }

        TodTopSection.LIVE_TV -> {
          // =====================================================================
          // TAB 3: LIVE TV SECTION (All Sports & Direct Channels with Categories)
          // =====================================================================
          val displayCategories = remember(xtreamCategories, effectiveChannels) {
            val list = mutableListOf<XtreamCategory>()
            list.add(XtreamCategory("ALL", "جميع القنوات", effectiveChannels.size))
            list.add(XtreamCategory("SPORTS", "قنوات beIN SPORTS", effectiveChannels.count { it.name.contains("bein", ignoreCase = true) || it.name.contains("sport", ignoreCase = true) }))
            list.add(XtreamCategory("ALKASS", "قنوات الكأس", effectiveChannels.count { it.name.contains("kass", ignoreCase = true) }))
            list.add(XtreamCategory("CLUBS", "قنوات الأندية العالمية", effectiveChannels.count { it.name.contains("lfc", ignoreCase = true) || it.name.contains("mutv", ignoreCase = true) }))
            list.add(XtreamCategory("NEWS", "قنوات الأخبار والجزيرة", effectiveChannels.count { it.name.contains("jazeera", ignoreCase = true) || it.name.contains("news", ignoreCase = true) }))

            xtreamCategories.forEach { cat ->
              if (list.none { it.categoryId == cat.categoryId }) list.add(cat)
            }
            list
          }

          val filteredChannels = remember(selectedChannelCategoryId, effectiveChannels) {
            when (selectedChannelCategoryId) {
              null, "ALL" -> effectiveChannels
              "SPORTS" -> effectiveChannels.filter { it.name.contains("bein", ignoreCase = true) || it.name.contains("sport", ignoreCase = true) }
              "ALKASS" -> effectiveChannels.filter { it.name.contains("kass", ignoreCase = true) }
              "CLUBS" -> effectiveChannels.filter { it.name.contains("lfc", ignoreCase = true) || it.name.contains("mutv", ignoreCase = true) }
              "NEWS" -> effectiveChannels.filter { it.name.contains("jazeera", ignoreCase = true) || it.name.contains("news", ignoreCase = true) }
              else -> effectiveChannels.filter { it.categoryId?.trim() == selectedChannelCategoryId }
            }
          }

          Column(modifier = Modifier.fillMaxSize()) {
            // Category Filter Pills
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              displayCategories.forEach { cat ->
                val isSelected = (selectedChannelCategoryId == null && cat.categoryId == "ALL") || selectedChannelCategoryId == cat.categoryId
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color(0x330A84FF) else Color(0x15FFFFFF))
                    .border(1.dp, if (isSelected) Color(0xFF0A84FF) else Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                    .clickable { selectedChannelCategoryId = cat.categoryId }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                  Text(
                    text = "${cat.categoryName} (${cat.channelCount})",
                    color = if (isSelected) Color(0xFF64D2FF) else Color.White,
                    fontSize = 12.5.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Sub-header with Channels Count & Grid/List switcher
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${filteredChannels.size} قناة رياضية مباشرة",
                color = DarkTextSecondary,
                fontSize = 13.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = FontWeight.SemiBold
              )

              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0x20FFFFFF))
                  .padding(2.dp)
              ) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isChannelListView) TodGold else Color.Transparent)
                    .clickable { isChannelListView = false }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  Icon(Icons.Default.GridView, contentDescription = null, tint = if (!isChannelListView) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isChannelListView) TodGold else Color.Transparent)
                    .clickable { isChannelListView = true }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = if (isChannelListView) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                }
              }
            }

            if (isLoading) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TodGold)
              }
            } else if (isChannelListView) {
              LazyColumn(
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                itemsIndexed(filteredChannels, key = { _, ch -> ch.streamId }) { index, channel ->
                  CorporateChannelListRow(
                    channel = channel,
                    channelIndex = index + 1,
                    allChannels = filteredChannels,
                    categoryName = "قنوات مباشرة",
                    onPlayChannel = onPlayChannel
                  )
                }
              }
            } else {
              LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                items(filteredChannels, key = { it.streamId }) { channel ->
                  CorporateChannelGridCard(
                    channel = channel,
                    allChannels = filteredChannels,
                    categoryName = "بث مباشر",
                    onPlayChannel = onPlayChannel
                  )
                }
              }
            }
          }
        }

        TodTopSection.COMPETITIONS -> {
          // =====================================================================
          // TAB 4: TOD COMPETITIONS & LEAGUES (Screenshots 10, 16, 18)
          // =====================================================================
          LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTabletOrLandscape) 3 else 2),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(competitions, key = { it.id }) { comp ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(130.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .background(
                    Brush.verticalGradient(
                      listOf(
                        Color(comp.accentColorHex).copy(alpha = 0.45f),
                        Color(0xFF0C101A)
                      )
                    )
                  )
                  .border(1.dp, Color(comp.accentColorHex).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                  .clickable {
                    activeTopSection = TodTopSection.MATCHES
                    selectedTournamentFilter = comp.name
                  }
                  .padding(14.dp)
              ) {
                Column(
                  modifier = Modifier.fillMaxSize(),
                  verticalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x30FFFFFF)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                      Text(comp.season, color = Color(0xFFD0D0E0), fontSize = 10.5.sp)
                    }
                  }

                  Column {
                    Text(
                      text = comp.name,
                      color = Color.White,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = ThmanyahFontFamily,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "${comp.matchesCount} مباراة هذا الموسم",
                      color = Color(0xAAFFFFFF),
                      fontSize = 11.sp,
                      fontFamily = ThmanyahFontFamily
                    )
                  }
                }
              }
            }
          }
        }

        TodTopSection.SHOWS -> {
          // =====================================================================
          // TAB 5: TOD SHOWS & HIGHLIGHTS (Screenshots 15, 17, 18)
          // =====================================================================
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            items(sportsShows, key = { it.id }) { show ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .clip(RoundedCornerShape(22.dp))
                  .background(
                    Brush.verticalGradient(
                      listOf(Color(0xFF1E283E), Color(0xFF0D121F))
                    )
                  )
                  .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                  .clickable {
                    val dummyChannel = XtreamChannel(
                      streamId = show.id,
                      name = show.title,
                      iconUrl = show.bannerUrl,
                      categoryId = "برامج رياضية",
                      playUrl = show.streamUrl
                    )
                    onPlayChannel(dummyChannel, emptyList(), show.title)
                  }
                  .padding(16.dp)
              ) {
                Column(
                  modifier = Modifier.fillMaxSize(),
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
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                      Text("إعادة HD", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    }

                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x35FFFFFF)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                  }

                  Column {
                    Text(
                      text = show.title,
                      color = Color.White,
                      fontSize = 17.sp,
                      fontWeight = FontWeight.Black,
                      fontFamily = ThmanyahFontFamily
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                      text = show.subtitle,
                      color = Color(0xCCFFFFFF),
                      fontSize = 12.sp,
                      fontFamily = ThmanyahFontFamily,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
        }
      }

      // =========================================================================
      // TOP HEADER (Only shown on secondary tabs - Completely removed from HOME per request)
      // =========================================================================
      if (activeTopSection != TodTopSection.HOME) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0xF0071328),
                  Color(0xD0071328),
                  Color(0x80071328),
                  Color.Transparent
                )
              )
            )
            .statusBarsPadding()
            .padding(bottom = 6.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Return to Home button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x35FFFFFF))
                .clickable {
                  activeTopSection = TodTopSection.HOME
                  selectedChannelCategoryId = null
                  selectedTournamentFilter = null
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "الرجوع للرئيسية",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "الرئيسية",
                  color = Color.White,
                  fontSize = 13.sp,
                  fontFamily = ThmanyahFontFamily,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Section Title
            Text(
              text = activeTopSection.title,
              color = Color.White,
              fontSize = 17.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Black
            )
          }
        }
      }

      // Multi-Server Selection Dialog / Modal
      if (matchForServerSelection != null) {
        val selectedMatch = matchForServerSelection!!
        TodServerSelectionDialog(
          match = selectedMatch,
          onDismiss = { matchForServerSelection = null },
          onSelectServer = { srv ->
            matchForServerSelection = null
            onPlayMatchDirectly(selectedMatch.copy(streamUrl = srv.streamUrl))
          }
        )
      }
    }
  }
}
}
}

/**
 * Tournament Header Badge in Matches Schedule Tab
 */
@Composable
fun TodTournamentSectionHeader(
  tournamentName: String,
  tournamentLogo: String,
  count: Int,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (tournamentLogo.isNotBlank()) {
        SubcomposeAsyncImage(
          model = tournamentLogo,
          contentDescription = null,
          modifier = Modifier.size(24.dp),
          contentScale = ContentScale.Fit,
          error = {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0x250A84FF)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color(0xFF64D2FF), modifier = Modifier.size(14.dp))
            }
          }
        )
      } else {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0x250A84FF)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color(0xFF64D2FF), modifier = Modifier.size(14.dp))
        }
      }
      Text(
        text = tournamentName,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 13.5.sp,
        fontFamily = ThmanyahFontFamily
      )
    }

    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0x200A84FF))
        .border(0.75.dp, Color(0x400A84FF), RoundedCornerShape(8.dp))
        .padding(horizontal = 8.dp, vertical = 2.5.dp)
    ) {
      Text(
        text = "$count مباريات",
        color = Color(0xFF64D2FF),
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = ThmanyahFontFamily
      )
    }
  }
}

/**
 * Modern Server Selection Dialog for matches with multiple streaming feeds
 */
@Composable
fun TodServerSelectionDialog(
  match: SportsMatch,
  onDismiss: () -> Unit,
  onSelectServer: (com.example.model.MatchStreamServer) -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xFF141C2E), Color(0xFF0C101A))
          )
        )
        .border(1.2.dp, TodGold.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
        .padding(22.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header with close button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0x20FFFFFF))
          ) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(16.dp))
          }

          Text(
            text = "اختر سيرفر المشاهدة",
            color = TodGold,
            fontSize = 16.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Black
          )

          Spacer(modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Match Teams subtitle
        Text(
          text = "${match.homeTeam.name} ضد ${match.awayTeam.name}",
          color = Color.White,
          fontSize = 15.sp,
          fontFamily = ThmanyahFontFamily,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )
        Text(
          text = "${match.tournament} • ${match.channelName}",
          color = Color(0xFF8E9BAE),
          fontSize = 11.5.sp,
          fontFamily = ThmanyahFontFamily,
          modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // List of Servers
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          match.servers.forEachIndexed { idx, srv ->
            val isFirst = idx == 0
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                  if (isFirst) {
                    Brush.horizontalGradient(listOf(TodGold, Color(0xFFFF9500)))
                  } else {
                    Brush.horizontalGradient(listOf(Color(0x250A84FF), Color(0x150A84FF)))
                  }
                )
                .border(
                  1.dp,
                  if (isFirst) TodGold else Color(0x400A84FF),
                  RoundedCornerShape(14.dp)
                )
                .clickable { onSelectServer(srv) }
                .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isFirst) Color.Black else Color(0xFF64D2FF),
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = srv.name.ifBlank { "سيرفر ${idx + 1}" },
                    color = if (isFirst) Color.Black else Color.White,
                    fontSize = 13.5.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Bold
                  )
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFirst) Color(0x30000000) else Color(0x200A84FF))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = srv.quality.ifBlank { "HD" },
                    color = if (isFirst) Color.Black else Color(0xFF64D2FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
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


