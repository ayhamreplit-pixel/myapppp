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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

  // Observe Sports Backend Data (AlwaysData or synced backend)
  val sportsMatches by sportsBackendRepo.matches.collectAsState()
  val competitions by sportsBackendRepo.competitions.collectAsState()
  val sportsShows by sportsBackendRepo.shows.collectAsState()
  val builtInChannels by sportsBackendRepo.sportsChannels.collectAsState()

  // Effective Channels list (connected server channels or high-fidelity TOD beIN channels)
  val effectiveChannels = remember(allChannels, builtInChannels) {
    if (allChannels.isNotEmpty()) allChannels else builtInChannels
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

  BoxWithConstraints(modifier = modifier.fillMaxSize().background(DarkBg)) {
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
          // TAB 1: TOD HOME FEED (Hero Banner + Live Rails + Fixtures + beIN Channels)
          // =====================================================================
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
          ) {
            // 1. Featured Match Countdown Hero Banner (Screenshots 11, 15)
            if (featuredMatch != null) {
              item(key = "hero_featured_match") {
                TodMatchCountdownHero(
                  match = featuredMatch,
                  onPlayMatch = onPlayMatchDirectly,
                  onOpenDetails = onOpenMatchDetail
                )
                Spacer(modifier = Modifier.height(14.dp))
              }
            }

            // 2. Live Multi-Sports Horizontal Rail ("البث المباشر - رياضات متعددة") (Screenshots 4, 11, 14)
            if (liveMatches.isNotEmpty()) {
              item(key = "live_sports_rail") {
                TodLiveSportsRail(
                  matches = liveMatches,
                  onPlayMatch = onPlayMatchDirectly,
                  onOpenDetails = onOpenMatchDetail
                )
                Spacer(modifier = Modifier.height(18.dp))
              }
            }

            // 3. Upcoming Football Fixtures Rail ("أفضل مباريات كرة القدم مباشرةً هذا الأسبوع") (Screenshots 12, 13, 15)
            if (upcomingMatches.isNotEmpty()) {
              item(key = "upcoming_matches_rail") {
                Column(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    text = "أفضل مباريات كرة القدم مباشرةً هذا الأسبوع",
                    color = Color.White,
                    fontSize = 17.5.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                  )
                  Spacer(modifier = Modifier.height(6.dp))

                  LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    items(upcomingMatches, key = { it.id }) { match ->
                      TodMatchFixtureCard(
                        match = match,
                        onClick = { onOpenMatchDetail(match) }
                      )
                    }
                  }
                }
                Spacer(modifier = Modifier.height(18.dp))
              }
            }

            // 4. Direct Sports Channels Rail ("قنوات الرياضة والبث المباشر") (Screenshots 14, 16, 17)
            item(key = "sports_channels_rail") {
              val sportsOnlyChannels = effectiveChannels.filter {
                it.name.contains("bein", ignoreCase = true) ||
                it.name.contains("kass", ignoreCase = true) ||
                it.name.contains("sport", ignoreCase = true) ||
                it.name.contains("lfc", ignoreCase = true) ||
                it.name.contains("mutv", ignoreCase = true) ||
                it.name.contains("league", ignoreCase = true) ||
                it.categoryId?.contains("رياض", ignoreCase = true) == true
              }.ifEmpty { effectiveChannels.take(12) }

              TodSportsChannelsRail(
                channels = sportsOnlyChannels,
                onPlayChannel = { channel ->
                  onPlayChannel(channel, effectiveChannels, "قنوات الرياضة")
                }
              )
              Spacer(modifier = Modifier.height(18.dp))
            }

            // 5. Competitions Rail ("المنافسات") (Screenshots 10, 16, 18)
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

            // 6. Highlights & Shows Rail ("إعادات وملخصات كرة القدم") (Screenshots 15, 17, 18)
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
              Spacer(modifier = Modifier.height(18.dp))
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

            // Tournament Filter Chips
            val tournaments = remember(sportsMatches) {
              listOf("الكل") + sportsMatches.map { it.tournament }.distinct()
            }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 4.dp),
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

            val filteredMatches = remember(sportsMatches, selectedTournamentFilter) {
              if (selectedTournamentFilter == null) sportsMatches
              else sportsMatches.filter { it.tournament == selectedTournamentFilter }
            }

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 100.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(filteredMatches, key = { it.id }) { match ->
                TodMatchScheduleCard(
                  match = match,
                  onPlayMatch = onPlayMatchDirectly,
                  onOpenDetails = onOpenMatchDetail
                )
              }
            }
          }
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
      // FLOATING OVERLAY HEADER (100% Matching TOD App Screenshot - Directly over Hero Banner)
      // =========================================================================
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0xDC000000),
                Color(0x95000000),
                Color(0x30000000),
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left: Profile Avatar & Search
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val avatarInteraction = remember { MutableInteractionSource() }
            val isAvatarPressed by avatarInteraction.collectIsPressedAsState()
            val avatarScale by animateFloatAsState(
              targetValue = if (isAvatarPressed) 0.88f else 1.0f,
              animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
              label = "avatarScale"
            )

            Box(
              modifier = Modifier
                .scale(avatarScale)
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(TodGold, Color(0xFFFF9500))))
                .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
                .clickable(
                  interactionSource = avatarInteraction,
                  indication = null,
                  onClick = onOpenProfile
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(sportsBackendRepo.getActiveProfile().avatarEmoji, fontSize = 18.sp)
            }

            IconButton(
              onClick = onOpenSearch,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x35000000))
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "بحث",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          // Right: Official TOD by beIN Logo
          TodLogo(fontSize = 24, showSubtext = true)
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Floating Categories Text Row (100% Identical to TOD Screenshot)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(22.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          TodTopSection.entries.forEach { section ->
            val isSelected = activeTopSection == section
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.clickable {
                activeTopSection = section
                selectedChannelCategoryId = null
                selectedTournamentFilter = null
              }
            ) {
              Text(
                text = section.title,
                color = if (isSelected) Color.White else Color(0xB8FFFFFF),
                fontSize = if (isSelected) 15.5.sp else 14.5.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
              )
              if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                  modifier = Modifier
                    .width(18.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(TodGold)
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
 * TOD Match Schedule Fixture Card for the "المباريات" Section
 */
@Composable
fun TodMatchScheduleCard(
  match: SportsMatch,
  onPlayMatch: (SportsMatch) -> Unit,
  onOpenDetails: (SportsMatch) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(22.dp))
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFF1B1E32),
            Color(0xFF111322),
            Color(0xFF090A14)
          )
        )
      )
      .border(
        1.dp,
        Brush.linearGradient(
          listOf(
            Color(0x4564D2FF),
            Color(0x35BF5AF2),
            Color(0x20FFFFFF)
          )
        ),
        RoundedCornerShape(22.dp)
      )
      .clickable { onOpenDetails(match) }
      .padding(16.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header: Tournament + Live/Time Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color(0xFF64D2FF), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = match.tournament,
            color = Color(0xFFCCCCCC),
            fontSize = 12.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (match.isLive) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFFE50914))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(6.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(match.liveMinute ?: "مباشر الآن", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
            }
          }
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = match.kickoffTime,
              color = TodGold,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = match.kickoffDate,
              color = Color(0x99FFFFFF),
              fontSize = 11.sp,
              fontFamily = ThmanyahFontFamily
            )
          }
        }
      }

      // Teams Rows
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Teams
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF))
                .border(1.dp, Color(0x35FFFFFF), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(match.homeTeam.flagEmoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(match.homeTeam.name, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, fontFamily = ThmanyahFontFamily)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF))
                .border(1.dp, Color(0x35FFFFFF), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(match.awayTeam.flagEmoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(match.awayTeam.name, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, fontFamily = ThmanyahFontFamily)
          }
        }

        // Scores or Action Button
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (match.scoreHome != null && match.scoreAway != null) {
            Text(
              text = "${match.scoreHome} - ${match.scoreAway}",
              color = if (match.isLive) Color(0xFF64D2FF) else Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Black
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (match.isLive) TodGold else Color(0x25FFFFFF))
              .border(0.75.dp, if (match.isLive) TodGold else Color(0x35FFFFFF), RoundedCornerShape(12.dp))
              .clickable { onPlayMatch(match) }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (match.isLive) Color.Black else Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (match.isLive) "شاهد الآن" else "البث المباشر",
                color = if (match.isLive) Color.Black else Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ThmanyahFontFamily
              )
            }
          }
        }
      }

      // Footer: Stadium & Channel
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0x15FFFFFF))
          .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Tv, contentDescription = null, tint = Color(0xFF64D2FF), modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = match.channelName,
            color = Color(0xFF64D2FF),
            fontSize = 11.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Bold
          )
        }
        Text(
          text = "🎙️ ${match.commentator} • 🏟️ ${match.stadium.ifBlank { "الملعب الرئيسي" }}",
          color = Color(0xFFB0B0C0),
          fontSize = 10.5.sp,
          fontFamily = ThmanyahFontFamily
        )
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
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

        PulsingLiveBadge()
      }

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
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "listRowScale"
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
      .liquidGlassEffect(shape = RoundedCornerShape(16.dp), glowTint = Color(0xFF0A84FF))
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) { onPlayChannel(channel, allChannels, categoryName) }
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PulsingLiveBadge()
        VideoQualityBadge(qualityText = qualityLabel)
        Icon(
          imageVector = Icons.Default.KeyboardArrowLeft,
          contentDescription = null,
          tint = Color(0x66FFFFFF),
          modifier = Modifier.size(16.dp)
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = cleanChannelName(channel.name),
            color = Color.White,
            fontSize = 13.5.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = categoryName,
            color = DarkTextTertiary,
            fontSize = 11.sp,
            fontFamily = ThmanyahFontFamily
          )
        }

        Box(
          modifier = Modifier
            .size(38.dp)
            .liquidGlassEffect(shape = RoundedCornerShape(10.dp), glowTint = Color(0xFF0A84FF)),
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

        Text(
          text = "$channelIndex",
          color = Color(0x55FFFFFF),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.width(22.dp),
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

/**
 * Intelligent Channel Name Formatter:
 * Cleans technical tags and language prefixes while keeping the full channel name intact.
 */
fun cleanChannelName(raw: String): String {
  var text = raw.trim()
  if (text.isEmpty()) return "قناة"

  val prefixRegex = Regex("^(?:[A-Z]{2,4}\\s*[-:|/]\\s*)", RegexOption.IGNORE_CASE)
  text = text.replace(prefixRegex, "").trim()

  text = text.replace(Regex("\\[(?:HEVC|H\\.?265|H\\.?264|VIP|4K|FHD|HD|SD|RAW|LOW|50FPS|60FPS)\\]", RegexOption.IGNORE_CASE), "")
    .replace(Regex("\\((?:HEVC|H\\.?265|H\\.?264|VIP|4K|FHD|HD|SD|RAW|LOW|50FPS|60FPS)\\)", RegexOption.IGNORE_CASE), "")
    .replace(Regex("\\s+"), " ")
    .trim()

  return if (text.isBlank()) raw.trim() else text
}
