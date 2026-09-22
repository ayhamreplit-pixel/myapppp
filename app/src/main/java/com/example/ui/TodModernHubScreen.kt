package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStream
import com.example.model.StreamFormat
import com.example.model.XtreamAccountInfo
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.model.XtreamPlaylistConfig
import com.example.player.XtreamRepository
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodButtonGrey
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGradients
import com.example.ui.theme.TodGreen
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

enum class HubViewMode {
  CATEGORIES,
  CHANNELS,
  XTREAM_FORM,
  M3U_FORM,
  ONBOARDING
}

/**
 * High-Performance Official TOD Hub Screen
 * 100% matched to TOD Design Language (Gold #FDB913, AMOLED Black #000000, 3-tab Bottom Nav)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodModernHubScreen(
  onPlayStream: (BroadcastStream, List<BroadcastStream>) -> Unit,
  onPlayDualStream: (BroadcastStream, BroadcastStream) -> Unit = { _, _ -> },
  onOpenLivePlayback: () -> Unit = {},
  onOpenQuickLinkScreen: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()
  val xtreamRepo = remember { XtreamRepository(context) }

  // App Navigation View Mode
  var viewMode by remember { mutableStateOf(HubViewMode.CATEGORIES) }
  var activeNavTab by remember { mutableStateOf(TodNavTab.HOME) }

  // Match Detail Modal Sheet
  var activeMatchDetail by remember { mutableStateOf<TodMatchDetail?>(null) }

  // Saved Playlists State
  var playlistConfig by remember { mutableStateOf<XtreamPlaylistConfig?>(null) }
  var savedPlaylists by remember { mutableStateOf(xtreamRepo.getAllPlaylists()) }

  // Active Connection State
  var isXtreamLoading by remember { mutableStateOf(false) }
  var xtreamError by remember { mutableStateOf<String?>(null) }
  var xtreamAccount by remember { mutableStateOf<XtreamAccountInfo?>(null) }

  // Categories & Channels
  val xtreamCategories = remember { mutableStateListOf<XtreamCategory>() }
  val allChannels = remember { mutableStateListOf<XtreamChannel>() }

  // Modals & Dialogs
  var showPlaylistsManagerModal by remember { mutableStateOf(false) }
  var showAccountInfoModal by remember { mutableStateOf(false) }

  var serverPingMs by remember { mutableStateOf<Long?>(null) }

  // Helper: Reload playlist data with instant cache restore + high-speed background sync
  val loadPlaylistData: (XtreamPlaylistConfig, Boolean) -> Unit = { config, forceRefresh ->
    scope.launch {
      val cleanServer = xtreamRepo.cleanServerUrl(config.serverUrl)
      val cleanUser = config.username.trim()
      val cacheKey = "$cleanServer|$cleanUser"

      // 1. Instant 0ms local cache display
      if (!config.isM3u) {
        val cachedDiskChannels = xtreamRepo.getCachedStreams(cacheKey)
        val cachedDiskCategories = xtreamRepo.getCachedCategories(cacheKey)
        if (cachedDiskChannels.isNotEmpty()) {
          allChannels.clear()
          allChannels.addAll(cachedDiskChannels)

          xtreamCategories.clear()
          xtreamCategories.add(XtreamCategory("ALL", "جميع القنوات", cachedDiskChannels.size))
          if (cachedDiskCategories.isNotEmpty()) {
            xtreamCategories.addAll(cachedDiskCategories)
          } else {
            val groupMap = cachedDiskChannels.groupBy { it.categoryId ?: "عام" }
            groupMap.forEach { (groupId, list) ->
              xtreamCategories.add(XtreamCategory(groupId, groupId, list.size))
            }
          }
          isXtreamLoading = false
        } else {
          isXtreamLoading = true
        }
      } else {
        isXtreamLoading = true
      }

      xtreamError = null

      if (config.isM3u) {
        val res = xtreamRepo.parseM3uPlaylist(config.m3uUrl)
        res.onSuccess { streams ->
          allChannels.clear()
          allChannels.addAll(streams)

          val groupMap = streams.groupBy { it.categoryId ?: "عام" }
          xtreamCategories.clear()
          xtreamCategories.add(XtreamCategory("ALL", "جميع القنوات", streams.size))
          groupMap.forEach { (group, list) ->
            xtreamCategories.add(XtreamCategory(group, group, list.size))
          }
          xtreamAccount = XtreamAccountInfo(
            username = config.playlistName.ifBlank { "قائمة M3U" },
            status = "نشط",
            expDate = "غير محدد",
            serverUrl = config.m3uUrl
          )
          xtreamRepo.updatePlaylistTimestampAndCount(config, streams.size)
        }.onFailure {
          xtreamError = "فشل تحميل قائمة M3U: ${it.localizedMessage ?: "تحقق من الرابط"}"
        }
      } else {
        // Parallelize network requests for maximum speed and resiliency
        val pingDeferred = async { xtreamRepo.pingServer(config.serverUrl) }
        val loginDeferred = async { xtreamRepo.login(config.serverUrl, config.username, config.password) }
        val catsDeferred = async { xtreamRepo.fetchCategories(config.serverUrl, config.username, config.password, forceRefresh = forceRefresh) }
        val streamsDeferred = async {
          xtreamRepo.fetchStreams(
            serverUrl = config.serverUrl,
            username = config.username,
            password = config.password,
            categoryId = null,
            preferredFormat = config.streamFormat,
            forceRefresh = forceRefresh
          )
        }

        val streamsRes = streamsDeferred.await()
        val catsRes = catsDeferred.await()
        val loginRes = loginDeferred.await()
        val ping = pingDeferred.await()

        if (ping > 0) {
          serverPingMs = ping
        }

        if (streamsRes.isSuccess && streamsRes.getOrDefault(emptyList()).isNotEmpty()) {
          val streams = streamsRes.getOrDefault(emptyList())
          allChannels.clear()
          allChannels.addAll(streams)

          // Set Account Info from login or create dynamic active profile
          xtreamAccount = loginRes.getOrNull() ?: XtreamAccountInfo(
            username = config.username.ifBlank { config.playlistName },
            status = "نشط",
            expDate = "متصل",
            serverUrl = config.serverUrl
          )

          // Build categories
          val cats = catsRes.getOrDefault(emptyList())
          xtreamCategories.clear()
          xtreamCategories.add(XtreamCategory("ALL", "جميع القنوات", streams.size))

          if (cats.isNotEmpty()) {
            cats.forEach { cat ->
              val count = streams.count { it.categoryId == cat.categoryId }
              if (count > 0) {
                xtreamCategories.add(XtreamCategory(cat.categoryId, cat.categoryName, count))
              }
            }
          } else {
            // Auto extract categories dynamically from stream tags
            val groupMap = streams.groupBy { it.categoryId ?: "عام" }
            groupMap.forEach { (groupId, list) ->
              xtreamCategories.add(XtreamCategory(groupId, groupId, list.size))
            }
          }
          xtreamRepo.updatePlaylistTimestampAndCount(config, streams.size)
        } else if (allChannels.isEmpty()) {
          // If streams failed and no cache was loaded, report specific reason or guidance
          val reason = streamsRes.exceptionOrNull()?.localizedMessage
            ?: loginRes.exceptionOrNull()?.localizedMessage
            ?: "تعذر جلب قنوات السيرفر. يرجى التحقق من صحة الرابط واسم المستخدم وكلمة المرور"
          xtreamError = reason
        }
      }
      isXtreamLoading = false
    }
  }

  // Initial Startup Logic with configurable update interval check
  LaunchedEffect(Unit) {
    val activeConfig = xtreamRepo.getActivePlaylistConfig()
    if (activeConfig != null) {
      playlistConfig = activeConfig
      viewMode = HubViewMode.CATEGORIES
      val forceRefresh = xtreamRepo.shouldRefreshPlaylist(activeConfig)
      loadPlaylistData(activeConfig, forceRefresh)
    } else {
      viewMode = HubViewMode.ONBOARDING
    }
  }

  // Build high-performance playback list
  val buildOptimizedPlaybackList: (XtreamChannel, List<XtreamChannel>, String) -> Pair<BroadcastStream, List<BroadcastStream>> =
    { clickedChannel, channelList, categoryName ->
      val targetStream = BroadcastStream(
        id = clickedChannel.streamId,
        title = clickedChannel.name,
        subtitle = categoryName,
        category = categoryName,
        streamUrl = clickedChannel.playUrl,
        isLive = true
      )

      val fullStreams = channelList.map { ch ->
        BroadcastStream(
          id = ch.streamId,
          title = ch.name,
          subtitle = categoryName,
          category = categoryName,
          streamUrl = ch.playUrl,
          isLive = true
        )
      }

      Pair(targetStream, fullStreams)
    }

  Surface(
    modifier = modifier.fillMaxSize(),
    color = DarkBg
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
    ) {
      // ========================================================
      // MAIN SCREEN SWITCHER
      // ========================================================
      when (viewMode) {
        HubViewMode.ONBOARDING -> {
          // Apple iOS Ultra-Modern Glassmorphic Welcome Hub
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(TodGradients.IosCanvasBg)
          ) {
            // Ambient Radial Glows
            Box(
              modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .background(
                  Brush.radialGradient(
                    colors = listOf(Color(0x350A84FF), Color(0x105E5CE6), Color.Transparent)
                  )
                )
            )

            Column(
              modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              // Glass Logo Badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(26.dp))
                  .background(
                    Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x10FFFFFF)))
                  )
                  .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(26.dp))
                  .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
              ) {
                TodLogo(fontSize = 34, showSubtext = true)
              }

              Spacer(modifier = Modifier.height(28.dp))

              Text(
                text = "ابدأ تجربة البث المباشر الآن",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "اربط حساب Xtream Codes أو أضف قائمة M3U أو شغل أي رابط فيديو مباشر بدقة عالية وفك تشفير تلقائي.",
                color = Color(0x99EBEBF5),
                fontSize = 13.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(34.dp))

              // Inset Grouped iOS Card with Modern Options
              IosListGroup {
                // Option 1: Quick Link (Direct Stream Player) - Highlighted
                IosListRow(
                  title = "تشغيل رابط سريع ومباشر",
                  subtitle = "روابط M3U8, TS, DASH مع دعم الحماية و DRM",
                  iconBadge = {
                    IosIconBadge(
                      icon = Icons.Default.Bolt,
                      background = Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF0055D4)))
                    )
                  },
                  onClick = { onOpenQuickLinkScreen() }
                )

                // Option 2: Add Xtream Codes Server
                IosListRow(
                  title = "إضافة سيرفر Xtream Codes",
                  subtitle = "تسجيل الدخول عبر الرابط والمستخدم وكلمة السر",
                  iconBadge = {
                    IosIconBadge(
                      icon = Icons.Default.Dns,
                      background = Brush.linearGradient(listOf(Color(0xFFFF9F0A), Color(0xFFD66000)))
                    )
                  },
                  onClick = {
                    playlistConfig = XtreamPlaylistConfig(playlistName = "سيرفر Xtream 1")
                    viewMode = HubViewMode.XTREAM_FORM
                  }
                )

                // Option 3: Add M3U Playlist
                IosListRow(
                  title = "إضافة قائمة M3U جديدة",
                  subtitle = "تحميل ملف M3U محلي أو رابط ويب مباشر",
                  iconBadge = {
                    IosIconBadge(
                      icon = Icons.Default.Add,
                      background = Brush.linearGradient(listOf(Color(0xFFBF5AF2), Color(0xFF7A24A6)))
                    )
                  },
                  showDivider = false,
                  onClick = {
                    playlistConfig = XtreamPlaylistConfig(isM3u = true, playlistName = "قائمة M3U")
                    viewMode = HubViewMode.M3U_FORM
                  }
                )
              }
            }
          }
        }

        HubViewMode.CATEGORIES -> {
          // TOD MAIN 3-TAB APP INTERFACE
          Column(modifier = Modifier.fillMaxSize()) {
            // Main Tab View
            Box(modifier = Modifier.weight(1f)) {
              when (activeNavTab) {
                TodNavTab.HOME -> {
                  TodHomeScreen(
                    xtreamCategories = xtreamCategories,
                    allChannels = allChannels,
                    isLoading = isXtreamLoading,
                    onPlayChannel = { ch, list, cat ->
                      val (stream, streams) = buildOptimizedPlaybackList(ch, list, cat)
                      onPlayStream(stream, streams)
                    },
                    onOpenMatchDetail = { match ->
                      activeMatchDetail = match
                    },
                    onOpenLiveChannels = {
                      viewMode = HubViewMode.CHANNELS
                    },
                    onOpenProfile = {
                      activeNavTab = TodNavTab.MORE
                    },
                    onOpenQuickLink = {
                      onOpenQuickLinkScreen()
                    }
                  )
                }
                TodNavTab.SEARCH -> {
                  TodSearchScreen(
                    xtreamCategories = xtreamCategories,
                    allChannels = allChannels,
                    onPlayChannel = { ch, list, cat ->
                      val (stream, streams) = buildOptimizedPlaybackList(ch, list, cat)
                      onPlayStream(stream, streams)
                    }
                  )
                }
                TodNavTab.MORE -> {
                  TodMoreScreen(
                    activeConfig = playlistConfig,
                    savedPlaylists = savedPlaylists,
                    accountInfo = xtreamAccount,
                    serverPingMs = serverPingMs,
                    onSelectPlaylist = { config ->
                      playlistConfig = config
                      xtreamRepo.savePlaylistConfig(config)
                      val force = xtreamRepo.shouldRefreshPlaylist(config)
                      loadPlaylistData(config, force)
                    },
                    onSwitchPlaylist = {
                      savedPlaylists = xtreamRepo.getAllPlaylists()
                      showPlaylistsManagerModal = true
                    },
                    onOpenXtreamForm = {
                      playlistConfig = XtreamPlaylistConfig(playlistName = "سيرفر Xtream ${savedPlaylists.size + 1}")
                      viewMode = HubViewMode.XTREAM_FORM
                    },
                    onOpenM3uForm = {
                      playlistConfig = XtreamPlaylistConfig(playlistName = "قائمة M3U ${savedPlaylists.size + 1}", isM3u = true)
                      viewMode = HubViewMode.M3U_FORM
                    },
                    onOpenDirectLink = {
                      onOpenQuickLinkScreen()
                    },
                    onRefreshPlaylist = { config ->
                      loadPlaylistData(config, true)
                    },
                    onDeletePlaylist = { config ->
                      xtreamRepo.deletePlaylistConfig(config)
                      savedPlaylists = xtreamRepo.getAllPlaylists()
                    }
                  )
                }
              }
            }

            // TOD High-End Corporate Bottom Navigation Bar
            TodBottomNavBar(
              currentTab = activeNavTab,
              onTabSelected = { activeNavTab = it },
              modifier = Modifier.navigationBarsPadding()
            )
          }
        }

        HubViewMode.CHANNELS -> {
          // TOD Live Channels Screen (Screenshot 3 style)
          TodLiveChannelsScreen(
            categories = xtreamCategories,
            channels = allChannels,
            isLoading = isXtreamLoading,
            onBack = { viewMode = HubViewMode.CATEGORIES },
            onPlayChannel = { ch, list, cat ->
              val (stream, streams) = buildOptimizedPlaybackList(ch, list, cat)
              onPlayStream(stream, streams)
            }
          )
        }

        HubViewMode.XTREAM_FORM -> {
          // Apple iOS Modern Glass Xtream Settings Form
          val activeConfig = playlistConfig ?: XtreamPlaylistConfig()

          var nameInput by remember(activeConfig) { mutableStateOf(activeConfig.playlistName.ifBlank { "سيرفر Xtream 1" }) }
          var userInput by remember(activeConfig) { mutableStateOf(activeConfig.username) }
          var passInput by remember(activeConfig) { mutableStateOf(activeConfig.password) }
          var serverInput by remember(activeConfig) { mutableStateOf(activeConfig.serverUrl) }
          var streamFormat by remember(activeConfig) { mutableStateOf(activeConfig.streamFormat) }
          var updateInterval by remember(activeConfig) { mutableStateOf(activeConfig.updateInterval.ifBlank { "عند بدء التطبيق" }) }

          val intervalOptions = listOf(
            "عند بدء التطبيق",
            "كل ساعة",
            "كل 4 ساعات",
            "كل 6 ساعات",
            "كل 12 ساعة",
            "كل 24 ساعة",
            "يدوياً فقط"
          )

          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(TodGradients.IosCanvasBg)
          ) {
            // Modern iOS Glass Navigation Bar
            IosNavigationBar(
              title = "إعدادات سيرفر Xtream",
              subtitle = "ربط ومزامنة القنوات المباشرة",
              onBack = {
                if (playlistConfig != null) viewMode = HubViewMode.CATEGORIES
                else viewMode = HubViewMode.ONBOARDING
              },
              trailing = {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A84FF))
                    .clickable {
                      val updated = activeConfig.copy(
                        playlistName = nameInput.ifBlank { userInput.ifBlank { "سيرفر Xtream" } },
                        username = userInput,
                        password = passInput,
                        serverUrl = serverInput,
                        streamFormat = streamFormat,
                        updateInterval = updateInterval,
                        isM3u = false
                      )
                      xtreamRepo.savePlaylistConfig(updated)
                      savedPlaylists = xtreamRepo.getAllPlaylists()
                      playlistConfig = updated
                      loadPlaylistData(updated, true)
                      viewMode = HubViewMode.CATEGORIES
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                  Text("حفظ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
              }
            )

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              item {
                IosSectionHeader(title = "بيانات الاتصال بالسيرفر")
                IosListGroup {
                  IosTextFieldRow(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = "اسم مخصص للسيرفر...",
                    label = "الاسم",
                    iconBadge = {
                      IosIconBadge(Icons.Default.Dns, background = Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF0055D4))))
                    }
                  )
                  IosTextFieldRow(
                    value = serverInput,
                    onValueChange = { input ->
                      serverInput = input
                      val extracted = xtreamRepo.smartExtractXtreamDetails(input)
                      if (extracted != null) {
                        serverInput = extracted.first
                        userInput = extracted.second
                        passInput = extracted.third
                      }
                    },
                    placeholder = "http://domain.com:8080",
                    label = "الرابط",
                    iconBadge = {
                      IosIconBadge(Icons.Default.Bolt, background = Brush.linearGradient(listOf(Color(0xFFFF9F0A), Color(0xFFD66000))))
                    }
                  )
                  IosTextFieldRow(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = "اسم المستخدم",
                    label = "المستخدم",
                    iconBadge = {
                      IosIconBadge(Icons.Default.Settings, background = Brush.linearGradient(listOf(Color(0xFF30D158), Color(0xFF1B8A38))))
                    }
                  )
                  IosTextFieldRow(
                    value = passInput,
                    onValueChange = { passInput = it },
                    placeholder = "كلمة المرور",
                    label = "كلمة السر",
                    showDivider = false,
                    iconBadge = {
                      IosIconBadge(Icons.Default.Add, background = Brush.linearGradient(listOf(Color(0xFFBF5AF2), Color(0xFF7A24A6))))
                    }
                  )
                }

                Spacer(modifier = Modifier.height(16.dp))

                IosSectionHeader(title = "صيغة البث المفضلة")
                IosSegmentedControl(
                  items = listOf("MPEG-TS (.ts)", "HLS (.m3u8)"),
                  selectedIndex = if (streamFormat == "HLS (.m3u8)") 1 else 0,
                  onSelect = { streamFormat = if (it == 1) "HLS (.m3u8)" else "MPEG-TS (.ts)" }
                )

                Spacer(modifier = Modifier.height(16.dp))

                IosSectionHeader(title = "مزامنة القنوات تلقائياً")
                IosListGroup {
                  intervalOptions.forEachIndexed { idx, opt ->
                    IosListRow(
                      title = opt,
                      value = if (updateInterval == opt) "محدد" else "",
                      valueColor = Color(0xFF0A84FF),
                      showChevron = false,
                      showDivider = idx != intervalOptions.size - 1,
                      onClick = { updateInterval = opt }
                    )
                  }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Connect Button
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A84FF))
                    .clickable {
                      val updated = activeConfig.copy(
                        playlistName = nameInput.ifBlank { userInput.ifBlank { "سيرفر Xtream" } },
                        username = userInput.trim(),
                        password = passInput.trim(),
                        serverUrl = serverInput.trim(),
                        streamFormat = streamFormat,
                        updateInterval = updateInterval,
                        isM3u = false
                      )
                      xtreamRepo.savePlaylistConfig(updated)
                      savedPlaylists = xtreamRepo.getAllPlaylists()
                      playlistConfig = updated
                      loadPlaylistData(updated, true)
                      viewMode = HubViewMode.CATEGORIES
                    }
                    .padding(vertical = 14.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Text("اتصال ومزامنة القنوات الآن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  }
                }
              }
            }
          }
        }

        HubViewMode.M3U_FORM -> {
          // Apple iOS Modern Glass M3U Settings Form
          val activeConfig = playlistConfig ?: XtreamPlaylistConfig(isM3u = true)
          var m3uName by remember { mutableStateOf(activeConfig.playlistName.ifBlank { "قائمة M3U" }) }
          var m3uUrlInput by remember { mutableStateOf(activeConfig.m3uUrl) }
          var m3uUpdateInterval by remember { mutableStateOf(activeConfig.updateInterval.ifBlank { "عند بدء التطبيق" }) }

          val intervalOptions = listOf(
            "عند بدء التطبيق",
            "كل ساعة",
            "كل 4 ساعات",
            "كل 6 ساعات",
            "كل 12 ساعة",
            "كل 24 ساعة",
            "يدوياً فقط"
          )

          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(TodGradients.IosCanvasBg)
          ) {
            // Modern iOS Glass Navigation Bar
            IosNavigationBar(
              title = "إعدادات قائمة M3U",
              subtitle = "تحميل وتحديث روابط البث",
              onBack = {
                if (playlistConfig != null) viewMode = HubViewMode.CATEGORIES
                else viewMode = HubViewMode.ONBOARDING
              },
              trailing = {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A84FF))
                    .clickable {
                      val updated = activeConfig.copy(
                        playlistName = m3uName.ifBlank { "قائمة M3U" },
                        m3uUrl = m3uUrlInput.trim(),
                        updateInterval = m3uUpdateInterval,
                        isM3u = true
                      )
                      xtreamRepo.savePlaylistConfig(updated)
                      savedPlaylists = xtreamRepo.getAllPlaylists()
                      playlistConfig = updated
                      loadPlaylistData(updated, true)
                      viewMode = HubViewMode.CATEGORIES
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                  Text("حفظ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
              }
            )

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              item {
                IosSectionHeader(title = "بيانات قائمة التشغيل")
                IosListGroup {
                  IosTextFieldRow(
                    value = m3uName,
                    onValueChange = { m3uName = it },
                    placeholder = "اسم القائمة...",
                    label = "الاسم",
                    iconBadge = {
                      IosIconBadge(Icons.Default.Dns, background = Brush.linearGradient(listOf(Color(0xFFBF5AF2), Color(0xFF7A24A6))))
                    }
                  )
                  IosTextFieldRow(
                    value = m3uUrlInput,
                    onValueChange = { m3uUrlInput = it },
                    placeholder = "http://example.com/playlist.m3u",
                    label = "رابط M3U",
                    showDivider = false,
                    iconBadge = {
                      IosIconBadge(Icons.Default.Bolt, background = Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF0055D4))))
                    }
                  )
                }

                Spacer(modifier = Modifier.height(16.dp))

                IosSectionHeader(title = "مزامنة القنوات تلقائياً")
                IosListGroup {
                  intervalOptions.forEachIndexed { idx, opt ->
                    IosListRow(
                      title = opt,
                      value = if (m3uUpdateInterval == opt) "محدد" else "",
                      valueColor = Color(0xFF0A84FF),
                      showChevron = false,
                      showDivider = idx != intervalOptions.size - 1,
                      onClick = { m3uUpdateInterval = opt }
                    )
                  }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Connect Button
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A84FF))
                    .clickable {
                      val updated = activeConfig.copy(
                        playlistName = m3uName.ifBlank { "قائمة M3U" },
                        m3uUrl = m3uUrlInput.trim(),
                        updateInterval = m3uUpdateInterval,
                        isM3u = true
                      )
                      xtreamRepo.savePlaylistConfig(updated)
                      savedPlaylists = xtreamRepo.getAllPlaylists()
                      playlistConfig = updated
                      loadPlaylistData(updated, true)
                      viewMode = HubViewMode.CATEGORIES
                    }
                    .padding(vertical = 14.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Text("حفظ وتحميل القنوات الآن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  }
                }
              }
            }
          }
        }
      }

      // ========================================================
      // MATCH DETAIL SHEET OVERLAY (Screenshots 1 & 2)
      // ========================================================
      activeMatchDetail?.let { match ->
        TodMatchDetailSheet(
          match = match,
          onClose = { activeMatchDetail = null },
          onPlayNow = {
            val ch = allChannels.firstOrNull { it.name.contains("beIN", ignoreCase = true) }
              ?: allChannels.firstOrNull()
            if (ch != null) {
              val (stream, streams) = buildOptimizedPlaybackList(ch, allChannels, "Jawwy Match")
              onPlayStream(stream, streams)
            }
            activeMatchDetail = null
          },
          onPlayCatchup = {
            val ch = allChannels.firstOrNull()
            if (ch != null) {
              val (stream, streams) = buildOptimizedPlaybackList(ch, allChannels, "Jawwy Catchup")
              onPlayStream(stream, streams)
            }
            activeMatchDetail = null
          },
          onPlayMultiView = {
            if (allChannels.size >= 2) {
              val s1 = BroadcastStream(
                id = allChannels[0].streamId,
                title = allChannels[0].name,
                subtitle = "Jawwy Multi 1",
                category = "Live",
                streamUrl = allChannels[0].playUrl,
                isLive = true
              )
              val s2 = BroadcastStream(
                id = allChannels[1].streamId,
                title = allChannels[1].name,
                subtitle = "Jawwy Multi 2",
                category = "Live",
                streamUrl = allChannels[1].playUrl,
                isLive = true
              )
              onPlayDualStream(s1, s2)
            }
            activeMatchDetail = null
          }
        )
      }

      // ========================================================
      // MODAL 1: Playlists & Servers Manager (Apple iOS Modern Glass Sheet)
      // ========================================================
      if (showPlaylistsManagerModal) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { showPlaylistsManagerModal = false },
          contentAlignment = Alignment.BottomCenter
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
              .background(Color(0xFF16161E).copy(alpha = 0.96f))
              .border(1.dp, TodGradients.SpecularCardBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
              .clickable(enabled = false) {}
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // iOS Sheet Grabber Pill
            IosGrabber()

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(0x22FFFFFF))
                  .clickable { showPlaylistsManagerModal = false },
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Clear, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(16.dp))
              }

              Text(
                text = "إدارة السيرفرات وقوائم التشغيل",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.size(32.dp))
            }

            // Saved Playlists List in iOS Grouped Style
            if (savedPlaylists.isNotEmpty()) {
              IosListGroup {
                savedPlaylists.forEachIndexed { idx, pl ->
                  val isCurrent = if (pl.isM3u) playlistConfig?.m3uUrl == pl.m3uUrl
                  else (playlistConfig?.serverUrl == pl.serverUrl && playlistConfig?.username == pl.username)

                  IosListRow(
                    title = pl.playlistName,
                    subtitle = if (pl.isM3u) "قائمة M3U" else pl.serverUrl,
                    value = if (isCurrent) "نشط" else "",
                    valueColor = Color(0xFF0A84FF),
                    showChevron = false,
                    showDivider = idx != savedPlaylists.size - 1,
                    iconBadge = {
                      IosIconBadge(
                        icon = if (pl.isM3u) Icons.Default.Bolt else Icons.Default.Dns,
                        background = if (isCurrent) Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF0055D4)))
                                     else Brush.linearGradient(listOf(Color(0xFF3A3A3C), Color(0xFF2C2C2E)))
                      )
                    },
                    trailing = {
                      IconButton(
                        onClick = {
                          xtreamRepo.deletePlaylistConfig(pl)
                          savedPlaylists = xtreamRepo.getAllPlaylists()
                          if (savedPlaylists.isEmpty()) {
                            playlistConfig = null
                            showPlaylistsManagerModal = false
                            viewMode = HubViewMode.ONBOARDING
                          } else if (isCurrent) {
                            val nextPl = savedPlaylists.first()
                            playlistConfig = nextPl
                            loadPlaylistData(nextPl, xtreamRepo.shouldRefreshPlaylist(nextPl))
                          }
                        },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Delete,
                          contentDescription = "حذف",
                          tint = Color(0xFFFF453A),
                          modifier = Modifier.size(18.dp)
                        )
                      }
                    },
                    onClick = {
                      playlistConfig = pl
                      xtreamRepo.setActivePlaylist(pl)
                      val force = xtreamRepo.shouldRefreshPlaylist(pl)
                      loadPlaylistData(pl, force)
                      showPlaylistsManagerModal = false
                    }
                  )
                }
              }
            }

            // iOS Add Actions
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0x22FFFFFF))
                  .clickable {
                    showPlaylistsManagerModal = false
                    playlistConfig = XtreamPlaylistConfig(isM3u = true)
                    viewMode = HubViewMode.M3U_FORM
                  }
                  .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
              ) {
                Text("+ إضافة M3U", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF0A84FF))
                  .clickable {
                    showPlaylistsManagerModal = false
                    playlistConfig = XtreamPlaylistConfig()
                    viewMode = HubViewMode.XTREAM_FORM
                  }
                  .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
              ) {
                Text("+ سيرفر Xtream", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
