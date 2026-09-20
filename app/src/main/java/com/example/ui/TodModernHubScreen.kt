package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.BroadcastStream
import com.example.model.StreamFormat
import com.example.model.XtreamAccountInfo
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.model.XtreamPlaylistConfig
import com.example.player.XtreamRepository
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodAmberYellow
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

enum class HubViewMode {
  ONBOARDING,    // First launch setup screen
  CATEGORIES,    // Televizo Main View with Active Server Banner & Categories
  CHANNELS,      // Channel list inside selected Category
  XTREAM_FORM,   // Xtream Codes Playlist Settings Form
  M3U_FORM       // M3U Playlist Form
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodModernHubScreen(
  onPlayStream: (BroadcastStream, List<BroadcastStream>) -> Unit,
  onPlayDualStream: (BroadcastStream, BroadcastStream) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val focusManager = LocalFocusManager.current
  val scope = rememberCoroutineScope()
  val xtreamRepo = remember { XtreamRepository(context) }

  // App Navigation View Mode
  var viewMode by remember { mutableStateOf(HubViewMode.CATEGORIES) }

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

  // Selected Category
  var selectedCategory by remember { mutableStateOf<XtreamCategory?>(null) }

  // Search & Filter State
  var globalSearchQuery by remember { mutableStateOf("") }
  var channelSearchQuery by remember { mutableStateOf("") }
  var isSearching by remember { mutableStateOf(false) }
  var selectedTabFilter by remember { mutableStateOf("ALL") } // ALL, FAV, RECENT

  // Modals & Dialogs
  var showPlaylistsManagerModal by remember { mutableStateOf(false) }
  var showDirectLinkModal by remember { mutableStateOf(false) }
  var showAccountInfoModal by remember { mutableStateOf(false) }

  // Direct Stream Quick Player Inputs
  var directUrlInput by remember { mutableStateOf("") }
  var directTitleInput by remember { mutableStateOf("") }

  // Layout & Favorites
  var favoriteIds by remember { mutableStateOf(xtreamRepo.getFavorites()) }
  var recentChannels by remember { mutableStateOf(xtreamRepo.getRecentChannels()) }
  var customUrlHistory by remember { mutableStateOf(xtreamRepo.getCustomUrlHistory()) }
  var isGridView by remember { mutableStateOf(false) }
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
          xtreamError = "فشل تحميل قائمة M3U: ${it.localizedMessage}"
        }
      } else {
        val loginRes = xtreamRepo.login(config.serverUrl, config.username, config.password)
        loginRes.onSuccess { info ->
          xtreamAccount = info

          // Parallelize network requests for maximum loading speed
          val pingDeferred = async { xtreamRepo.pingServer(config.serverUrl) }
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
          val ping = pingDeferred.await()
          if (ping > 0) {
            serverPingMs = ping
          }

          if (streamsRes.isSuccess) {
            allChannels.clear()
            allChannels.addAll(streamsRes.getOrDefault(emptyList()))
          }

          if (catsRes.isSuccess) {
            val cats = catsRes.getOrDefault(emptyList())
            xtreamCategories.clear()
            xtreamCategories.add(XtreamCategory("ALL", "جميع القنوات", allChannels.size))

            cats.forEach { cat ->
              val count = allChannels.count { it.categoryId == cat.categoryId }
              xtreamCategories.add(XtreamCategory(cat.categoryId, cat.categoryName, if (count > 0) count else 0))
            }
          } else {
            xtreamCategories.clear()
            xtreamCategories.add(XtreamCategory("ALL", "جميع القنوات", allChannels.size))
          }
        }.onFailure {
          xtreamError = it.localizedMessage ?: "فشل الاتصال بالسيرفر. تحقق من صحة البيانات"
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

  Surface(
    modifier = modifier.fillMaxSize(),
    color = Color(0xFF0D0F17)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
    ) {
      // ========================================================
      // TOP HEADER BAR (Televizo Style with Quick Direct Link & Switcher)
      // ========================================================
      if (viewMode != HubViewMode.ONBOARDING) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF181B26))
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left Action Icons
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // 1. Playlists Manager
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "السيرفرات والقوائم",
                tint = Color.White,
                modifier = Modifier
                  .size(24.dp)
                  .clickable {
                    savedPlaylists = xtreamRepo.getAllPlaylists()
                    showPlaylistsManagerModal = true
                  }
              )

              // 2. Direct Link Quick Player (مشغل رابط مباشر)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(TodAmberYellow.copy(alpha = 0.2f))
                  .clickable { showDirectLinkModal = true }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "رابط مباشر",
                    tint = TodAmberYellow,
                    modifier = Modifier.size(18.dp)
                  )
                  Text("رابط مباشر", color = TodAmberYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              // 3. Settings / Account Info
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "الإعدادات",
                tint = Color.White,
                modifier = Modifier
                  .size(24.dp)
                  .clickable {
                    if (playlistConfig?.isM3u == true) {
                      viewMode = HubViewMode.M3U_FORM
                    } else {
                      viewMode = HubViewMode.XTREAM_FORM
                    }
                  }
              )

              // 4. Search Toggle
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "بحث",
                tint = Color.White,
                modifier = Modifier
                  .size(24.dp)
                  .clickable { isSearching = !isSearching }
              )
            }

            // Right Title Branding
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "TELEVIZO",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
              )
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(TodAmberYellow)
              )
            }
          }
        }
      }

      // ========================================================
      // MAIN SCREEN SWITCHER
      // ========================================================
      when (viewMode) {
        HubViewMode.ONBOARDING -> {
          // Dynamic Welcome Setup Screen
          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF0D0F17))
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(TodAmberYellow.copy(alpha = 0.15f))
                .border(2.dp, TodAmberYellow, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = null,
                tint = TodAmberYellow,
                modifier = Modifier.size(46.dp)
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
              text = "مرحباً بك في مشغل IPTV Pro",
              color = Color.White,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "قم بإضافة سيرفر Xtream جديد أو قائمة M3U أو شغل أي رابط مباشر فوراً",
              color = DarkTextSecondary,
              fontSize = 14.sp,
              modifier = Modifier.padding(horizontal = 16.dp)
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
                .height(52.dp),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TodAmberYellow, contentColor = Color.Black)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("إضافة قائمة Xtream Codes جديدة", fontSize = 15.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Button 2: Direct Link Quick Player
            Button(
              onClick = { showDirectLinkModal = true },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E), contentColor = Color.White)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = TodAmberYellow, modifier = Modifier.size(20.dp))
                Text("مشغل رابط مباشر سريع (M3U8 / TS)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                .height(52.dp),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2333), contentColor = Color.White)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("إضافة قائمة M3U جديدة", fontSize = 15.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        HubViewMode.CATEGORIES -> {
          // Active Server Banner (Televizo Style - Screenshot 1)
          playlistConfig?.let { currentConfig ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1E2B))
                .clickable {
                  savedPlaylists = xtreamRepo.getAllPlaylists()
                  showPlaylistsManagerModal = true
                }
                .padding(horizontal = 18.dp, vertical = 12.dp)
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
                  Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = DarkTextSecondary,
                    modifier = Modifier.size(22.dp)
                  )

                  IconButton(
                    onClick = { showAccountInfoModal = true },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Info,
                      contentDescription = "معلومات السيرفر",
                      tint = TodAmberYellow,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  // Quick Refresh Button
                  IconButton(
                    onClick = { loadPlaylistData(currentConfig) },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Refresh,
                      contentDescription = "تحديث القنوات",
                      tint = Color.White.copy(alpha = 0.8f),
                      modifier = Modifier.size(19.dp)
                    )
                  }

                  // Ping indicator
                  serverPingMs?.let { ping ->
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x3300E676))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(6.dp)
                          .clip(CircleShape)
                          .background(Color(0xFF00E676))
                      )
                      Text(
                        text = "${ping}ms",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentConfig.isM3u) Color(0xFF3F51B5) else Color(0xFF4CAF50))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = if (currentConfig.isM3u) "M3U" else "Xtream",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }

                    Text(
                      text = currentConfig.playlistName.ifBlank { "سيرفر IPTV" },
                      color = Color.White,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Text(
                    text = if (currentConfig.isM3u) currentConfig.m3uUrl else currentConfig.serverUrl,
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }

          // Search Input Bar
          AnimatedVisibility(visible = isSearching) {
            OutlinedTextField(
              value = globalSearchQuery,
              onValueChange = { globalSearchQuery = it },
              placeholder = { Text("بحث عام في الباقات والقنوات...", color = DarkTextSecondary, fontSize = 13.sp) },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161A26),
                unfocusedContainerColor = Color(0xFF161A26),
                focusedBorderColor = TodAmberYellow,
                unfocusedBorderColor = Color(0xFF2B3145),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              trailingIcon = {
                if (globalSearchQuery.isNotEmpty()) {
                  IconButton(onClick = { globalSearchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = DarkTextSecondary)
                  }
                }
              }
            )
          }

          // Error Banner
          xtreamError?.let { err ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF32121A))
                .padding(horizontal = 16.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = err,
                color = Color(0xFFFF6B6B),
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
              )
              Button(
                onClick = { playlistConfig?.let { loadPlaylistData(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = TodAmberYellow, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("إعادة المحاولة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          // Loading Indicator
          if (isXtreamLoading) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                CircularProgressIndicator(color = TodAmberYellow, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                Text("جاري اتصال السيرفر وجلب القنوات...", color = Color.White, fontSize = 13.sp)
              }
            }
          }

          // Filter Pills (الكل / المفضلة / الأحدث)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF121520))
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              "ALL" to "جميع الباقات",
              "FAV" to "المفضلة ❤️ (${favoriteIds.size})",
              "RECENT" to "السجل 🕒 (${recentChannels.size})"
            ).forEach { (key, label) ->
              val isSelected = selectedTabFilter == key
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(if (isSelected) TodAmberYellow else Color(0xFF1E2333))
                  .clickable { selectedTabFilter = key }
                  .padding(horizontal = 14.dp, vertical = 6.dp)
              ) {
                Text(
                  text = label,
                  color = if (isSelected) Color.Black else Color.White,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              }
            }
          }

          // Categories & Search Results Content
          val filteredCategories = remember(xtreamCategories, globalSearchQuery) {
            if (globalSearchQuery.isBlank()) xtreamCategories
            else xtreamCategories.filter { it.categoryName.contains(globalSearchQuery, ignoreCase = true) }
          }

          val matchingChannels = remember(allChannels, globalSearchQuery, selectedTabFilter, favoriteIds, recentChannels) {
            when {
              selectedTabFilter == "FAV" -> allChannels.filter { favoriteIds.contains(it.streamId) }
              selectedTabFilter == "RECENT" -> recentChannels
              globalSearchQuery.isNotBlank() -> allChannels.filter { it.name.contains(globalSearchQuery, ignoreCase = true) }.take(50)
              else -> emptyList()
            }
          }

          CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0F17))
            ) {
              // Direct channel results section if searching or on FAV/RECENT tabs
              if (matchingChannels.isNotEmpty()) {
                item {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(Color(0xFF181B28))
                      .padding(horizontal = 18.dp, vertical = 10.dp)
                  ) {
                    Text(
                      text = when (selectedTabFilter) {
                        "FAV" -> "القنوات المفضلة"
                        "RECENT" -> "آخر القنوات التي تم تشغيلها"
                        else -> "نتائج البحث المباشرة (${matchingChannels.size})"
                      },
                      color = TodAmberYellow,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                items(matchingChannels, key = { "match_${it.streamId}_${it.playUrl}" }) { ch ->
                  val isFav = favoriteIds.contains(ch.streamId)
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        xtreamRepo.addRecentChannel(ch)
                        recentChannels = xtreamRepo.getRecentChannels()

                        val streamList = matchingChannels.map { channel ->
                          BroadcastStream(
                            id = channel.streamId,
                            title = channel.name,
                            subtitle = "IPTV Stream",
                            category = "IPTV Live",
                            tournamentOrLeague = "Live Channel",
                            streamUrl = channel.playUrl,
                            format = StreamFormat.AUTO,
                            isLive = true
                          )
                        }
                        val currentStream = BroadcastStream(
                          id = ch.streamId,
                          title = ch.name,
                          subtitle = "IPTV Stream",
                          category = "IPTV Live",
                          tournamentOrLeague = "Live Channel",
                          streamUrl = ch.playUrl,
                          format = StreamFormat.AUTO,
                          isLive = true
                        )
                        onPlayStream(currentStream, streamList)
                      }
                      .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    IconButton(
                      onClick = {
                        xtreamRepo.toggleFavorite(ch.streamId)
                        favoriteIds = xtreamRepo.getFavorites()
                      }
                    ) {
                      Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFav) Color(0xFFFF2A55) else DarkTextSecondary,
                        modifier = Modifier.size(20.dp)
                      )
                    }

                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                      Text(
                        text = ch.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )

                      if (!ch.iconUrl.isNullOrBlank()) {
                        AsyncImage(
                          model = ch.iconUrl,
                          contentDescription = null,
                          modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                        )
                      } else {
                        Box(
                          modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF222838)),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(Icons.Default.LiveTv, contentDescription = null, tint = TodAmberYellow, modifier = Modifier.size(18.dp))
                        }
                      }
                    }
                  }
                  HorizontalDivider(color = Color(0xFF1B1E2B), thickness = 1.dp)
                }
              }

              // Standard Categories List (if on ALL tab)
              if (selectedTabFilter == "ALL") {
                items(filteredCategories, key = { cat -> cat.categoryId }) { cat ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        selectedCategory = cat
                        viewMode = HubViewMode.CHANNELS
                      }
                      .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                      )

                      Text(
                        text = "${cat.channelCount}",
                        color = Color(0xFF9CA3AF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                      )
                    }

                    Text(
                      text = cat.categoryName,
                      color = Color.White,
                      fontSize = 16.sp,
                      fontWeight = FontWeight.Normal,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  HorizontalDivider(color = Color(0xFF1B1E2B), thickness = 1.dp)
                }
              }
            }
          }
        }

        HubViewMode.CHANNELS -> {
          // Channels List inside Selected Category
          val catName = selectedCategory?.categoryName ?: "جميع القنوات"
          val catId = selectedCategory?.categoryId

          val visibleChannels = remember(allChannels, catId, channelSearchQuery, favoriteIds) {
            allChannels.filter { ch ->
              val matchesCat = catId == null || catId == "ALL" || ch.categoryId == catId
              val matchesSearch = channelSearchQuery.isBlank() || ch.name.contains(channelSearchQuery, ignoreCase = true)
              matchesCat && matchesSearch
            }
          }

          Column(modifier = Modifier.fillMaxSize()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181B26))
                .padding(horizontal = 16.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              IconButton(onClick = { viewMode = HubViewMode.CATEGORIES }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
              }

              Text(
                text = catName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )

              IconButton(onClick = { isGridView = !isGridView }) {
                Icon(
                  imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                  contentDescription = null,
                  tint = Color.White
                )
              }
            }

            OutlinedTextField(
              value = channelSearchQuery,
              onValueChange = { channelSearchQuery = it },
              placeholder = { Text("بحث عن قناة...", color = DarkTextSecondary, fontSize = 13.sp) },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161A26),
                unfocusedContainerColor = Color(0xFF161A26),
                focusedBorderColor = TodAmberYellow,
                unfocusedBorderColor = Color(0xFF2B3145),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              trailingIcon = {
                if (channelSearchQuery.isNotEmpty()) {
                  IconButton(onClick = { channelSearchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = DarkTextSecondary)
                  }
                }
              }
            )

            if (visibleChannels.isEmpty()) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(32.dp),
                contentAlignment = Alignment.Center
              ) {
                Text("لا توجد قنوات متاحة في هذه الباقة", color = DarkTextSecondary, fontSize = 14.sp)
              }
            } else if (isGridView) {
              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                items(visibleChannels, key = { it.streamId }) { ch ->
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(12.dp))
                      .background(Color(0xFF181B28))
                      .border(1.dp, Color(0xFF262C3E), RoundedCornerShape(12.dp))
                      .clickable {
                        xtreamRepo.addRecentChannel(ch)
                        recentChannels = xtreamRepo.getRecentChannels()

                        val channelStreams = visibleChannels.map { channel ->
                          BroadcastStream(
                            id = channel.streamId,
                            title = channel.name,
                            subtitle = catName,
                            category = "IPTV Live",
                            tournamentOrLeague = catName,
                            streamUrl = channel.playUrl,
                            format = StreamFormat.AUTO,
                            isLive = true
                          )
                        }
                        val currentStream = BroadcastStream(
                          id = ch.streamId,
                          title = ch.name,
                          subtitle = catName,
                          category = "IPTV Live",
                          tournamentOrLeague = catName,
                          streamUrl = ch.playUrl,
                          format = StreamFormat.AUTO,
                          isLive = true
                        )
                        onPlayStream(currentStream, channelStreams)
                      }
                      .padding(10.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      if (!ch.iconUrl.isNullOrBlank()) {
                        AsyncImage(
                          model = ch.iconUrl,
                          contentDescription = null,
                          modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                        )
                      } else {
                        Box(
                          modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF222838)),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(Icons.Default.LiveTv, contentDescription = null, tint = TodAmberYellow, modifier = Modifier.size(24.dp))
                        }
                      }

                      Text(
                        text = ch.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }
              }
            } else {
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                items(visibleChannels, key = { it.streamId }) { ch ->
                  val isFav = favoriteIds.contains(ch.streamId)
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(Color(0xFF181B28))
                      .border(1.dp, Color(0xFF262C3E), RoundedCornerShape(12.dp))
                      .clickable {
                        xtreamRepo.addRecentChannel(ch)
                        recentChannels = xtreamRepo.getRecentChannels()

                        val channelStreams = visibleChannels.map { channel ->
                          BroadcastStream(
                            id = channel.streamId,
                            title = channel.name,
                            subtitle = catName,
                            category = "IPTV Live",
                            tournamentOrLeague = catName,
                            streamUrl = channel.playUrl,
                            format = StreamFormat.AUTO,
                            isLive = true
                          )
                        }
                        val currentStream = BroadcastStream(
                          id = ch.streamId,
                          title = ch.name,
                          subtitle = catName,
                          category = "IPTV Live",
                          tournamentOrLeague = catName,
                          streamUrl = ch.playUrl,
                          format = StreamFormat.AUTO,
                          isLive = true
                        )
                        onPlayStream(currentStream, channelStreams)
                      }
                      .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      if (!ch.iconUrl.isNullOrBlank()) {
                        AsyncImage(
                          model = ch.iconUrl,
                          contentDescription = null,
                          modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                        )
                      } else {
                        Box(
                          modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF222838)),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(Icons.Default.LiveTv, contentDescription = null, tint = TodAmberYellow, modifier = Modifier.size(20.dp))
                        }
                      }

                      Column {
                        Text(
                          text = ch.name,
                          color = Color.White,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Bold,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                        Text(
                          text = catName,
                          color = DarkTextSecondary,
                          fontSize = 11.sp
                        )
                      }
                    }

                    IconButton(
                      onClick = {
                        xtreamRepo.toggleFavorite(ch.streamId)
                        favoriteIds = xtreamRepo.getFavorites()
                      }
                    ) {
                      Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFav) Color(0xFFFF2A55) else DarkTextSecondary,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        HubViewMode.XTREAM_FORM -> {
          // Xtream Codes Settings Screen
          val activeConfig = playlistConfig ?: XtreamPlaylistConfig()

          var nameInput by remember { mutableStateOf(activeConfig.playlistName.ifBlank { "سيرفر Xtream 1" }) }
          var userInput by remember { mutableStateOf(activeConfig.username) }
          var passInput by remember { mutableStateOf(activeConfig.password) }
          var serverInput by remember { mutableStateOf(activeConfig.serverUrl) }
          var useDefaultUa by remember { mutableStateOf(activeConfig.useDefaultUserAgent) }
          var customUaInput by remember { mutableStateOf(activeConfig.customUserAgent) }

          var isEnabled by remember { mutableStateOf(activeConfig.isEnabled) }
          var updateInterval by remember { mutableStateOf(activeConfig.updateInterval) }
          var enableChannels by remember { mutableStateOf(activeConfig.enableChannels) }
          var enableMovies by remember { mutableStateOf(activeConfig.enableMovies) }
          var enableSeries by remember { mutableStateOf(activeConfig.enableSeries) }
          var streamFormat by remember { mutableStateOf(activeConfig.streamFormat) }

          var showFormatDropdown by remember { mutableStateOf(false) }
          var showIntervalDropdown by remember { mutableStateOf(false) }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF0D0F17))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181B26))
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
                    useDefaultUserAgent = useDefaultUa,
                    customUserAgent = customUaInput,
                    isEnabled = isEnabled,
                    updateInterval = updateInterval,
                    enableChannels = enableChannels,
                    enableMovies = enableMovies,
                    enableSeries = enableSeries,
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
                Icon(Icons.Default.Check, contentDescription = "حفظ", tint = TodAmberYellow, modifier = Modifier.size(28.dp))
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
                  // 1. Playlist Name
                  Column {
                    Text("اسم السيرفر / القائمة", color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                      value = nameInput,
                      onValueChange = { nameInput = it },
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth(),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TodAmberYellow,
                        unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                      )
                    )
                  }

                  // 2. Username
                  Column {
                    Text("اسم المستخدم (Username)", color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                      value = userInput,
                      onValueChange = { userInput = it },
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth(),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TodAmberYellow,
                        unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                      )
                    )
                  }

                  // 3. Password
                  Column {
                    Text("كلمة السر (Password)", color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                      value = passInput,
                      onValueChange = { passInput = it },
                      singleLine = true,
                      visualTransformation = PasswordVisualTransformation(),
                      modifier = Modifier.fillMaxWidth(),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TodAmberYellow,
                        unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                      )
                    )
                  }

                  // 4. Server Address
                  Column {
                    Text("عنوان السيرفر (Server URL)", color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                      value = serverInput,
                      onValueChange = { serverInput = it },
                      singleLine = true,
                      placeholder = { Text("http://my-xtream-server.com:8080", color = DarkTextSecondary) },
                      modifier = Modifier.fillMaxWidth(),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TodAmberYellow,
                        unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                      )
                    )
                  }

                  Text(
                    text = "مثال: http://domain.com:8080",
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                  )

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                  ) {
                    Checkbox(
                      checked = useDefaultUa,
                      onCheckedChange = { useDefaultUa = it },
                      colors = CheckboxDefaults.colors(
                        checkedColor = TodAmberYellow,
                        uncheckedColor = DarkTextSecondary,
                        checkmarkColor = Color.Black
                      )
                    )
                    Text("استخدام User-Agent الافتراضي للتطبيق", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                  }

                  if (!useDefaultUa) {
                    Column {
                      Text("تخصيص User-Agent", color = TodAmberYellow, fontSize = 13.sp)
                      OutlinedTextField(
                        value = customUaInput,
                        onValueChange = { customUaInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                          focusedBorderColor = TodAmberYellow,
                          unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.6f),
                          focusedTextColor = Color.White,
                          unfocusedTextColor = Color.White
                        )
                      )
                    }
                  }
                }

                HorizontalDivider(color = Color(0xFF222738), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Switch(
                      checked = isEnabled,
                      onCheckedChange = { isEnabled = it },
                      colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = TodAmberYellow)
                    )
                    Text("قم بتشغيل السيرفر", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                  }

                  // Refresh Interval Option
                  Box {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIntervalDropdown = true },
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(updateInterval, color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                      Text("تحديث البيانات تلقائياً", color = Color.White, fontSize = 15.sp)
                    }

                    DropdownMenu(
                      expanded = showIntervalDropdown,
                      onDismissRequest = { showIntervalDropdown = false },
                      modifier = Modifier.background(Color(0xFF1E2333))
                    ) {
                      listOf("عند الفتح", "كل 12 ساعة", "كل يوم", "كل أسبوع").forEach { option ->
                        DropdownMenuItem(
                          text = { Text(option, color = Color.White) },
                          onClick = {
                            updateInterval = option
                            showIntervalDropdown = false
                          }
                        )
                      }
                    }
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Switch(
                      checked = enableChannels,
                      onCheckedChange = { enableChannels = it },
                      colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = TodAmberYellow)
                    )
                    Text("تمكين القنوات المباشرة", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Switch(
                      checked = enableMovies,
                      onCheckedChange = { enableMovies = it },
                      colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = TodAmberYellow)
                    )
                    Text("تمكين قسم الأفلام", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Switch(
                      checked = enableSeries,
                      onCheckedChange = { enableSeries = it },
                      colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = TodAmberYellow)
                    )
                    Text("تمكين قسم المسلسلات", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                  }

                  // Stream Format Option
                  Box {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFormatDropdown = true },
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(streamFormat, color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                      Text("صيغة البث المرغوبة", color = Color.White, fontSize = 15.sp)
                    }

                    DropdownMenu(
                      expanded = showFormatDropdown,
                      onDismissRequest = { showFormatDropdown = false },
                      modifier = Modifier.background(Color(0xFF1E2333))
                    ) {
                      listOf("MPEG-TS (.ts)", "HLS (.m3u8)", "تلقائي (Auto)").forEach { option ->
                        DropdownMenuItem(
                          text = { Text(option, color = Color.White) },
                          onClick = {
                            streamFormat = option
                            showFormatDropdown = false
                          }
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        HubViewMode.M3U_FORM -> {
          // M3U Playlist Settings Screen
          val activeConfig = playlistConfig ?: XtreamPlaylistConfig(isM3u = true)

          var m3uName by remember { mutableStateOf(activeConfig.playlistName.ifBlank { "قائمة M3U المباشرة" }) }
          var m3uUrlInput by remember { mutableStateOf(activeConfig.m3uUrl) }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF0D0F17))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181B26))
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              IconButton(
                onClick = {
                  val updated = activeConfig.copy(
                    playlistName = m3uName,
                    m3uUrl = m3uUrlInput,
                    isM3u = true
                  )
                  xtreamRepo.savePlaylistConfig(updated)
                  savedPlaylists = xtreamRepo.getAllPlaylists()
                  playlistConfig = updated
                  loadPlaylistData(updated)
                  viewMode = HubViewMode.CATEGORIES
                }
              ) {
                Icon(Icons.Default.Check, contentDescription = "حفظ", tint = TodAmberYellow, modifier = Modifier.size(28.dp))
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
                .padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Text("اسم القائمة", color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              OutlinedTextField(
                value = m3uName,
                onValueChange = { m3uName = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = TodAmberYellow,
                  unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.6f),
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
                      m3uUrlInput = clip
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3248), contentColor = TodAmberYellow),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("لصق من المحفظة", fontSize = 11.sp)
                  }
                }

                Text("رابط M3U أو M3U8", color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }

              OutlinedTextField(
                value = m3uUrlInput,
                onValueChange = { m3uUrlInput = it },
                placeholder = { Text("http://example.com/playlist.m3u", color = DarkTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = TodAmberYellow,
                  unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.6f),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )
            }
          }
        }
      }
    }
  }

  // ========================================================
  // MODAL 1: Playlists & Servers Manager (إدارة قوائم التشغيل والسيرفرات)
  // ========================================================
  if (showPlaylistsManagerModal) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.82f))
        .clickable { showPlaylistsManagerModal = false },
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth(0.9f)
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF181C2B))
          .border(1.dp, Color(0xFF2C3348), RoundedCornerShape(20.dp))
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
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }

        HorizontalDivider(color = Color(0xFF2A3045), thickness = 1.dp)

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
                  .background(if (isCurrent) Color(0xFF252B40) else Color(0xFF131622))
                  .clickable {
                    playlistConfig = pl
                    loadPlaylistData(pl)
                    showPlaylistsManagerModal = false
                  }
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  // Delete Button
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
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                  }

                  // Edit Button
                  IconButton(
                    onClick = {
                      playlistConfig = pl
                      showPlaylistsManagerModal = false
                      if (pl.isM3u) viewMode = HubViewMode.M3U_FORM
                      else viewMode = HubViewMode.XTREAM_FORM
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = TodAmberYellow, modifier = Modifier.size(18.dp))
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Column(horizontalAlignment = Alignment.End) {
                    Text(pl.playlistName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                      if (pl.isM3u) pl.m3uUrl else pl.serverUrl,
                      color = DarkTextSecondary,
                      fontSize = 11.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  RadioButton(
                    selected = isCurrent,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = TodAmberYellow)
                  )
                }
              }
            }
          }

          HorizontalDivider(color = Color(0xFF2A3045), thickness = 1.dp)
        }

        // Add Xtream Server
        Button(
          onClick = {
            showPlaylistsManagerModal = false
            playlistConfig = XtreamPlaylistConfig(playlistName = "سيرفر جديد ${savedPlaylists.size + 1}")
            viewMode = HubViewMode.XTREAM_FORM
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TodAmberYellow, contentColor = Color.Black)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("+ إضافة سيرفر Xtream Codes جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }

        // Add M3U Playlist
        Button(
          onClick = {
            showPlaylistsManagerModal = false
            playlistConfig = XtreamPlaylistConfig(isM3u = true, playlistName = "قائمة M3U جديدة")
            viewMode = HubViewMode.M3U_FORM
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E), contentColor = Color.White)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("+ إضافة قائمة M3U جديدة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }

        // Direct Stream Player Button
        Button(
          onClick = {
            showPlaylistsManagerModal = false
            showDirectLinkModal = true
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2333), contentColor = TodAmberYellow)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("⚡ مشغل رابط مباشر سريع", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // ========================================================
  // MODAL 2: Direct Stream Quick Player (مشغل رابط مباشر سريع)
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
          .fillMaxWidth(0.92f)
          .clip(RoundedCornerShape(22.dp))
          .background(Color(0xFF161A28))
          .border(1.dp, Color(0xFF2C3348), RoundedCornerShape(22.dp))
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

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = TodAmberYellow, modifier = Modifier.size(20.dp))
            Text(
              text = "مشغل رابط مباشر سريع",
              color = Color.White,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        HorizontalDivider(color = Color(0xFF2A3045), thickness = 1.dp)

        // URL Field with Paste Button
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                val clip = clipboardManager.getText()?.text
                if (!clip.isNullOrBlank()) {
                  directUrlInput = clip
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3248), contentColor = TodAmberYellow),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                Text("لصق الرابط", fontSize = 11.sp)
              }
            }

            Text("رابط البث المباشر (HLS / TS / MP4)", color = TodAmberYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedTextField(
            value = directUrlInput,
            onValueChange = { directUrlInput = it },
            placeholder = { Text("http://example.com/live/stream.m3u8", color = DarkTextSecondary, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TodAmberYellow,
              unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.5f),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )
        }

        // Optional Title Field
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("اسم البث (اختياري)", color = TodAmberYellow, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(
            value = directTitleInput,
            onValueChange = { directTitleInput = it },
            placeholder = { Text("مثال: مباراة اليوم المباشرة", color = DarkTextSecondary, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TodAmberYellow,
              unfocusedBorderColor = TodAmberYellow.copy(alpha = 0.5f),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )
        }

        // Play Action Button
        Button(
          onClick = {
            if (directUrlInput.isNotBlank()) {
              val title = directTitleInput.ifBlank { "بث مباشر مباشر" }
              xtreamRepo.addCustomUrlToHistory(title, directUrlInput)
              customUrlHistory = xtreamRepo.getCustomUrlHistory()

              val stream = BroadcastStream(
                id = "custom_${System.currentTimeMillis()}",
                title = title,
                subtitle = "Direct Link Stream",
                category = "Direct Stream",
                tournamentOrLeague = "Custom Stream",
                streamUrl = directUrlInput.trim(),
                format = StreamFormat.AUTO,
                isLive = true
              )
              showDirectLinkModal = false
              onPlayStream(stream, listOf(stream))
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TodAmberYellow, contentColor = Color.Black)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
            Text("▶ تشغيل البث الآن", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }
        }

        // History of Direct Links
        if (customUrlHistory.isNotEmpty()) {
          HorizontalDivider(color = Color(0xFF2A3045), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.History, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(16.dp))
            Text("الروابط السابقة المباشرة", color = DarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height((customUrlHistory.size * 52).coerceAtMost(160).dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(customUrlHistory, key = { it.second }) { (title, url) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF1E2335))
                  .clickable {
                    directUrlInput = url
                    directTitleInput = title
                  }
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                IconButton(
                  onClick = {
                    xtreamRepo.removeCustomUrlFromHistory(url)
                    customUrlHistory = xtreamRepo.getCustomUrlHistory()
                  },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  Text(url, color = DarkTextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
              }
            }
          }
        }
      }
    }
  }

  // ========================================================
  // MODAL 3: Server & Account Info (معلومات السيرفر والاشتراك)
  // ========================================================
  if (showAccountInfoModal) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.8f))
        .clickable { showAccountInfoModal = false },
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth(0.88f)
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF181C2B))
          .border(1.dp, Color(0xFF2C3348), RoundedCornerShape(20.dp))
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
          IconButton(onClick = { showAccountInfoModal = false }) {
            Icon(Icons.Default.Clear, contentDescription = "إغلاق", tint = DarkTextSecondary)
          }

          Text("تفاصيل الاشتراك والسيرفر", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = Color(0xFF2A3045), thickness = 1.dp)

        playlistConfig?.let { cfg ->
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(cfg.playlistName, color = TodAmberYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
              Text("اسم السيرفر", color = DarkTextSecondary, fontSize = 13.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (cfg.isM3u) "M3U Playlist" else cfg.username, color = Color.White, fontSize = 14.sp)
              Text("الحساب", color = DarkTextSecondary, fontSize = 13.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("${allChannels.size} قناة", color = Color.White, fontSize = 14.sp)
              Text("عدد القنوات", color = DarkTextSecondary, fontSize = 13.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(xtreamAccount?.expDate ?: "غير محدد", color = Color.White, fontSize = 14.sp)
              Text("تاريخ الانتهاء", color = DarkTextSecondary, fontSize = 13.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(cfg.streamFormat, color = TodAmberYellow, fontSize = 14.sp)
              Text("صيغة البث", color = DarkTextSecondary, fontSize = 13.sp)
            }
          }
        }

        Button(
          onClick = { showAccountInfoModal = false },
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TodAmberYellow, contentColor = Color.Black)
        ) {
          Text("تم", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
