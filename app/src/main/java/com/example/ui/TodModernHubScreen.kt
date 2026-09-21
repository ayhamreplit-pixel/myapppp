package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
  var showDirectLinkModal by remember { mutableStateOf(false) }
  var showAccountInfoModal by remember { mutableStateOf(false) }

  // Direct Stream Quick Player Inputs
  var directUrlInput by remember { mutableStateOf("") }
  var directTitleInput by remember { mutableStateOf("") }

  var serverPingMs by remember { mutableStateOf<Long?>(null) }

  // Helper: Reload playlist data with high-speed parallel fetching and ping
  val loadPlaylistData: (XtreamPlaylistConfig) -> Unit = { config ->
    scope.launch {
      isXtreamLoading = true
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
        }.onFailure {
          xtreamError = "فشل تحميل قائمة M3U: ${it.localizedMessage ?: "تحقق من الرابط"}"
        }
      } else {
        // Parallelize network requests for maximum speed and resiliency
        val pingDeferred = async { xtreamRepo.pingServer(config.serverUrl) }
        val loginDeferred = async { xtreamRepo.login(config.serverUrl, config.username, config.password) }
        val catsDeferred = async { xtreamRepo.fetchCategories(config.serverUrl, config.username, config.password) }
        val streamsDeferred = async {
          xtreamRepo.fetchStreams(
            serverUrl = config.serverUrl,
            username = config.username,
            password = config.password,
            categoryId = null,
            preferredFormat = config.streamFormat
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
        } else {
          // If streams failed, report specific reason or guidance
          val reason = streamsRes.exceptionOrNull()?.localizedMessage
            ?: loginRes.exceptionOrNull()?.localizedMessage
            ?: "تعذر جلب قنوات السيرفر. يرجى التحقق من صحة الرابط واسم المستخدم وكلمة المرور"
          xtreamError = reason
        }
      }
      isXtreamLoading = false
    }
  }

  // Initial Startup Logic
  LaunchedEffect(Unit) {
    val activeConfig = xtreamRepo.getActivePlaylistConfig()
    if (activeConfig != null) {
      playlistConfig = activeConfig
      viewMode = HubViewMode.CATEGORIES
      loadPlaylistData(activeConfig)
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
          // Official TOD Style Welcome Setup Screen
          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(DarkBg)
              .navigationBarsPadding()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            TodLogo(fontSize = 36, showSubtext = true)

            Spacer(modifier = Modifier.height(28.dp))

            Text(
              text = "ابدأ تجربة البث المباشر الآن",
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "قم بربط حساب Xtream Codes الخاص بك أو أضف قائمة M3U للاستمتاع بالقنوات والمباريات",
              color = DarkTextSecondary,
              fontSize = 13.sp,
              modifier = Modifier.padding(horizontal = 16.dp),
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Button 1: Add Xtream Codes
            Button(
              onClick = {
                playlistConfig = XtreamPlaylistConfig(playlistName = "سيرفر Xtream 1")
                viewMode = HubViewMode.XTREAM_FORM
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TodGold, contentColor = Color.Black)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("إضافة سيرفر Xtream Codes", fontSize = 14.sp, fontWeight = FontWeight.Black)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Button 2: Direct Link Quick Player
            Button(
              onClick = { showDirectLinkModal = true },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TodButtonGrey, contentColor = Color.White)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = TodGold, modifier = Modifier.size(20.dp))
                Text("مشغل رابط مباشر سريع (M3U8 / TS)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Button 3: Add M3U Playlist
            Button(
              onClick = {
                playlistConfig = XtreamPlaylistConfig(isM3u = true, playlistName = "قائمة M3U")
                viewMode = HubViewMode.M3U_FORM
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141418), contentColor = Color.White)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("إضافة قائمة M3U جديدة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                      loadPlaylistData(config)
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
                      showDirectLinkModal = true
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
          // Xtream Codes Settings Screen (TOD Dark & Gold Styled)
          val activeConfig = playlistConfig ?: XtreamPlaylistConfig()

          var nameInput by remember(activeConfig) { mutableStateOf(activeConfig.playlistName.ifBlank { "سيرفر Xtream 1" }) }
          var userInput by remember(activeConfig) { mutableStateOf(activeConfig.username) }
          var passInput by remember(activeConfig) { mutableStateOf(activeConfig.password) }
          var serverInput by remember(activeConfig) { mutableStateOf(activeConfig.serverUrl) }
          var streamFormat by remember(activeConfig) { mutableStateOf(activeConfig.streamFormat) }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(DarkBg)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C0C10))
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              IconButton(
                onClick = {
                  val updated = activeConfig.copy(
                    playlistName = nameInput.ifBlank { userInput.ifBlank { "سيرفر Xtream" } },
                    username = userInput,
                    password = passInput,
                    serverUrl = serverInput,
                    streamFormat = streamFormat,
                    isM3u = false
                  )
                  xtreamRepo.savePlaylistConfig(updated)
                  savedPlaylists = xtreamRepo.getAllPlaylists()
                  playlistConfig = updated
                  loadPlaylistData(updated)
                  viewMode = HubViewMode.CATEGORIES
                }
              ) {
                Icon(Icons.Default.Check, contentDescription = "حفظ", tint = TodGold, modifier = Modifier.size(28.dp))
              }

              Text(
                text = "إعدادات سيرفر Xtream Codes",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )

              IconButton(onClick = {
                if (playlistConfig != null) viewMode = HubViewMode.CATEGORIES
                else viewMode = HubViewMode.ONBOARDING
              }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White, modifier = Modifier.size(24.dp))
              }
            }

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                  Text("اسم السيرفر", color = TodGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = TodGold,
                      unfocusedBorderColor = Color(0xFF2B2B36),
                      focusedContainerColor = Color(0xFF141418),
                      unfocusedContainerColor = Color(0xFF141418),
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White
                    )
                  )

                  Text("رابط السيرفر (Host & Port)", color = TodGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  OutlinedTextField(
                    value = serverInput,
                    onValueChange = { input ->
                      serverInput = input
                      // Smart auto extract if full link pasted
                      val extracted = xtreamRepo.smartExtractXtreamDetails(input)
                      if (extracted != null) {
                        serverInput = extracted.first
                        userInput = extracted.second
                        passInput = extracted.third
                      }
                    },
                    placeholder = { Text("http://example.com:8080", color = DarkTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = TodGold,
                      unfocusedBorderColor = Color(0xFF2B2B36),
                      focusedContainerColor = Color(0xFF141418),
                      unfocusedContainerColor = Color(0xFF141418),
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White
                    )
                  )

                  Text("اسم المستخدم (Username)", color = TodGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = TodGold,
                      unfocusedBorderColor = Color(0xFF2B2B36),
                      focusedContainerColor = Color(0xFF141418),
                      unfocusedContainerColor = Color(0xFF141418),
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White
                    )
                  )

                  Text("كلمة المرور (Password)", color = TodGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  OutlinedTextField(
                    value = passInput,
                    onValueChange = { passInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = TodGold,
                      unfocusedBorderColor = Color(0xFF2B2B36),
                      focusedContainerColor = Color(0xFF141418),
                      unfocusedContainerColor = Color(0xFF141418),
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White
                    )
                  )

                  Text("صيغة البث المفضلة", color = TodGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("MPEG-TS (.ts)", "HLS (.m3u8)").forEach { fmt ->
                      val isSel = streamFormat == fmt
                      Button(
                        onClick = { streamFormat = fmt },
                        colors = ButtonDefaults.buttonColors(
                          containerColor = if (isSel) TodGold else Color(0xFF1F1F27),
                          contentColor = if (isSel) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                      ) {
                        Text(fmt, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  Button(
                    onClick = {
                      val updated = activeConfig.copy(
                        playlistName = nameInput.ifBlank { userInput.ifBlank { "سيرفر Xtream" } },
                        username = userInput.trim(),
                        password = passInput.trim(),
                        serverUrl = serverInput.trim(),
                        streamFormat = streamFormat,
                        isM3u = false
                      )
                      xtreamRepo.savePlaylistConfig(updated)
                      savedPlaylists = xtreamRepo.getAllPlaylists()
                      playlistConfig = updated
                      loadPlaylistData(updated)
                      viewMode = HubViewMode.CATEGORIES
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TodGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اتصال ومزامنة القنوات", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  }
                }
              }
            }
          }
        }

        HubViewMode.M3U_FORM -> {
          // M3U Playlist Form (TOD Dark & Gold Styled)
          val activeConfig = playlistConfig ?: XtreamPlaylistConfig(isM3u = true)
          var m3uName by remember { mutableStateOf(activeConfig.playlistName.ifBlank { "قائمة M3U" }) }
          var m3uUrlInput by remember { mutableStateOf(activeConfig.m3uUrl) }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(DarkBg)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C0C10))
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              IconButton(
                onClick = {
                  val updated = activeConfig.copy(
                    playlistName = m3uName.ifBlank { "قائمة M3U" },
                    m3uUrl = m3uUrlInput.trim(),
                    isM3u = true
                  )
                  xtreamRepo.savePlaylistConfig(updated)
                  savedPlaylists = xtreamRepo.getAllPlaylists()
                  playlistConfig = updated
                  loadPlaylistData(updated)
                  viewMode = HubViewMode.CATEGORIES
                }
              ) {
                Icon(Icons.Default.Check, contentDescription = "حفظ", tint = TodGold, modifier = Modifier.size(28.dp))
              }

              Text("إعدادات قائمة M3U", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)

              IconButton(onClick = {
                if (playlistConfig != null) viewMode = HubViewMode.CATEGORIES
                else viewMode = HubViewMode.ONBOARDING
              }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White, modifier = Modifier.size(24.dp))
              }
            }

            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Text("اسم القائمة", color = TodGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              OutlinedTextField(
                value = m3uName,
                onValueChange = { m3uName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = TodGold,
                  unfocusedBorderColor = Color(0xFF2B2B36),
                  focusedContainerColor = Color(0xFF141418),
                  unfocusedContainerColor = Color(0xFF141418),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )

              Text("رابط M3U أو M3U8", color = TodGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              OutlinedTextField(
                value = m3uUrlInput,
                onValueChange = { m3uUrlInput = it },
                placeholder = { Text("http://example.com/playlist.m3u", color = DarkTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = TodGold,
                  unfocusedBorderColor = Color(0xFF2B2B36),
                  focusedContainerColor = Color(0xFF141418),
                  unfocusedContainerColor = Color(0xFF141418),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )

              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = {
                  val updated = activeConfig.copy(
                    playlistName = m3uName.ifBlank { "قائمة M3U" },
                    m3uUrl = m3uUrlInput.trim(),
                    isM3u = true
                  )
                  xtreamRepo.savePlaylistConfig(updated)
                  savedPlaylists = xtreamRepo.getAllPlaylists()
                  playlistConfig = updated
                  loadPlaylistData(updated)
                  viewMode = HubViewMode.CATEGORIES
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TodGold, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اتصال وتحميل القنوات", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
      // MODAL 1: Playlists & Servers Manager
      // ========================================================
      if (showPlaylistsManagerModal) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { showPlaylistsManagerModal = false },
          contentAlignment = Alignment.Center
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth(0.9f)
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xFF141418))
              .border(1.dp, Color(0xFF282832), RoundedCornerShape(20.dp))
              .clickable(enabled = false) {}
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(onClick = { showPlaylistsManagerModal = false }) {
                Icon(Icons.Default.Clear, contentDescription = "إغلاق", tint = DarkTextSecondary)
              }

              Text(
                text = "إدارة السيرفرات وقوائم التشغيل",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }

            HorizontalDivider(color = Color(0xFF262630), thickness = 1.dp)

            // Saved Playlists List
            if (savedPlaylists.isNotEmpty()) {
              LazyColumn(
                modifier = Modifier
                  .fillMaxWidth()
                  .height((savedPlaylists.size * 70).coerceAtMost(280).dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(savedPlaylists, key = { if (it.isM3u) it.m3uUrl else "${it.serverUrl}_${it.username}" }) { pl ->
                  val isCurrent = if (pl.isM3u) playlistConfig?.m3uUrl == pl.m3uUrl
                  else (playlistConfig?.serverUrl == pl.serverUrl && playlistConfig?.username == pl.username)

                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isCurrent) Color(0xFF22222C) else Color(0xFF181820))
                      .border(
                        1.dp,
                        if (isCurrent) TodGold else Color.Transparent,
                        RoundedCornerShape(12.dp)
                      )
                      .clickable {
                        playlistConfig = pl
                        xtreamRepo.setActivePlaylist(pl)
                        loadPlaylistData(pl)
                        showPlaylistsManagerModal = false
                      }
                      .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    IconButton(
                      onClick = {
                        xtreamRepo.deletePlaylistConfig(pl)
                        savedPlaylists = xtreamRepo.getAllPlaylists()
                        if (savedPlaylists.isEmpty()) {
                          playlistConfig = null
                          showPlaylistsManagerModal = false
                          viewMode = HubViewMode.ONBOARDING
                        } else if (isCurrent) {
                          playlistConfig = savedPlaylists.first()
                          loadPlaylistData(savedPlaylists.first())
                        }
                      }
                    ) {
                      Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = pl.playlistName,
                        color = if (isCurrent) TodGold else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = if (pl.isM3u) "قائمة M3U" else pl.serverUrl,
                        color = DarkTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                      )
                    }
                  }
                }
              }
            }

            // Add buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  showPlaylistsManagerModal = false
                  playlistConfig = XtreamPlaylistConfig(isM3u = true)
                  viewMode = HubViewMode.M3U_FORM
                },
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TodButtonGrey)
              ) {
                Text("+ إضافة M3U", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  showPlaylistsManagerModal = false
                  playlistConfig = XtreamPlaylistConfig()
                  viewMode = HubViewMode.XTREAM_FORM
                },
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TodGold, contentColor = Color.Black)
              ) {
                Text("+ سيرفر Xtream", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // ========================================================
      // MODAL 2: Direct Stream Quick Player (M3U8 / TS / MP4)
      // ========================================================
      if (showDirectLinkModal) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { showDirectLinkModal = false },
          contentAlignment = Alignment.Center
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth(0.9f)
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xFF141418))
              .border(1.dp, TodGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
              .clickable(enabled = false) {}
              .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(onClick = { showDirectLinkModal = false }) {
                Icon(Icons.Default.Clear, contentDescription = "إغلاق", tint = DarkTextSecondary)
              }

              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("تشغيل رابط بث مباشر", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Bolt, contentDescription = null, tint = TodGold)
              }
            }

            HorizontalDivider(color = Color(0xFF262630), thickness = 1.dp)

            OutlinedTextField(
              value = directTitleInput,
              onValueChange = { directTitleInput = it },
              placeholder = { Text("اسم البث (اختياري)...", color = DarkTextSecondary, fontSize = 13.sp) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TodGold,
                unfocusedBorderColor = Color(0xFF2B2B36),
                focusedContainerColor = Color(0xFF1A1A22),
                unfocusedContainerColor = Color(0xFF1A1A22),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Button(
                onClick = {
                  val clip = clipboardManager.getText()?.text
                  if (!clip.isNullOrBlank()) {
                    directUrlInput = clip
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TodButtonGrey, contentColor = TodGold),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("لصق من الحافظة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Text("رابط البث (M3U8, TS, MP4)", color = TodGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
              value = directUrlInput,
              onValueChange = { directUrlInput = it },
              placeholder = { Text("https://domain.com/live/stream.m3u8", color = DarkTextSecondary, fontSize = 12.sp) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TodGold,
                unfocusedBorderColor = Color(0xFF2B2B36),
                focusedContainerColor = Color(0xFF1A1A22),
                unfocusedContainerColor = Color(0xFF1A1A22),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            Button(
              onClick = {
                if (directUrlInput.isNotBlank()) {
                  val stream = BroadcastStream(
                    id = "custom_${System.currentTimeMillis()}",
                    title = directTitleInput.ifBlank { "بث مباشر" },
                    subtitle = "رابط خارجي مباشر",
                    category = "Direct Stream",
                    streamUrl = directUrlInput.trim(),
                    isLive = true
                  )
                  xtreamRepo.addCustomUrlToHistory(directTitleInput.ifBlank { "بث مباشر" }, directUrlInput.trim())
                  showDirectLinkModal = false
                  onPlayStream(stream, listOf(stream))
                }
              },
              enabled = directUrlInput.isNotBlank(),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = TodGold,
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF2E2E38)
              )
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
              Spacer(modifier = Modifier.width(6.dp))
              Text("تشغيل البث الآن", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
          }
        }
      }
    }
  }
}
