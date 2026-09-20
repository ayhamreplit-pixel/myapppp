package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.BroadcastStream
import com.example.model.StreamFormat
import com.example.model.XtreamAccountInfo
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel
import com.example.player.XtreamRepository
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGoldGlow
import com.example.ui.theme.TodGreenLight
import com.example.ui.theme.TodLiveRed
import com.example.ui.theme.TodAmberYellow
import com.example.ui.theme.TodGradients
import com.example.ui.theme.CyberNeonBlue
import com.example.ui.theme.CyberElectricIndigo
import com.example.ui.theme.PremiumGoldGradStart
import com.example.ui.theme.PremiumGoldGradEnd
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SignalCellularAlt
import com.example.ui.theme.TodCyan

enum class HubTab {
  XTREAM,
  CUSTOM_URL,
  DUAL_STREAM
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodModernHubScreen(
  onPlayStream: (BroadcastStream, List<BroadcastStream>) -> Unit,
  onPlayDualStream: (BroadcastStream, BroadcastStream) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val scope = rememberCoroutineScope()
  val xtreamRepo = remember { XtreamRepository(context) }

  var selectedTab by remember { mutableStateOf(HubTab.XTREAM) }

  // Xtream state
  var xtreamServer by remember { mutableStateOf("") }
  var xtreamUser by remember { mutableStateOf("") }
  var xtreamPass by remember { mutableStateOf("") }
  var isPassVisible by remember { mutableStateOf(false) }
  var isXtreamLoading by remember { mutableStateOf(false) }
  var xtreamError by remember { mutableStateOf<String?>(null) }
  var xtreamAccount by remember { mutableStateOf<XtreamAccountInfo?>(null) }
  val xtreamCategories = remember { mutableStateListOf<XtreamCategory>() }
  var selectedCategory by remember { mutableStateOf<String?>("ALL") }
  val xtreamChannels = remember { mutableStateListOf<XtreamChannel>() }
  var channelSearchQuery by remember { mutableStateOf("") }

  // Features: Favorites & Recents & Layout
  var favoriteIds by remember { mutableStateOf(xtreamRepo.getFavorites()) }
  val recentChannels = remember { mutableStateListOf<XtreamChannel>().apply { addAll(xtreamRepo.getRecentChannels()) } }
  val customUrlHistory = remember { mutableStateListOf<Pair<String, String>>().apply { addAll(xtreamRepo.getCustomUrlHistory()) } }
  var isGridView by remember { mutableStateOf(false) }
  var showOnlyFavorites by remember { mutableStateOf(false) }

  // Direct URL state
  var directTitle by remember { mutableStateOf("بث مباشر مخصص") }
  var directUrl by remember { mutableStateOf("") }
  var directUserAgent by remember { mutableStateOf("") }
  var directReferer by remember { mutableStateOf("") }
  var directOrigin by remember { mutableStateOf("") }
  var showAdvancedDirect by remember { mutableStateOf(false) }

  // Dual Stream state (playing 2 channels/links simultaneously)
  var dualTitle1 by remember { mutableStateOf("القناة الأولى") }
  var dualUrl1 by remember { mutableStateOf("") }
  var dualTitle2 by remember { mutableStateOf("القناة الثانية") }
  var dualUrl2 by remember { mutableStateOf("") }

  val reloadChannels: (String?) -> Unit = { catId ->
    scope.launch {
      isXtreamLoading = true
      val res = xtreamRepo.fetchStreams(xtreamServer, xtreamUser, xtreamPass, catId)
      res.onSuccess {
        xtreamChannels.clear()
        xtreamChannels.addAll(it)
      }.onFailure {
        xtreamError = it.localizedMessage
      }
      isXtreamLoading = false
    }
  }

  // Load saved Xtream credentials on launch
  LaunchedEffect(Unit) {
    xtreamRepo.getSavedCredentials()?.let { (server, user, pass) ->
      xtreamServer = server
      xtreamUser = user
      xtreamPass = pass
      // Auto login in background if saved
      isXtreamLoading = true
      val res = xtreamRepo.login(server, user, pass)
      res.onSuccess { info ->
        xtreamAccount = info
        val catsRes = xtreamRepo.fetchCategories(server, user, pass)
        catsRes.onSuccess { cats ->
          xtreamCategories.clear()
          xtreamCategories.add(XtreamCategory("ALL", "الكل (All)"))
          xtreamCategories.addAll(cats)
        }
        val streamsRes = xtreamRepo.fetchStreams(server, user, pass, null)
        streamsRes.onSuccess { streams ->
          xtreamChannels.clear()
          xtreamChannels.addAll(streams)
        }
      }.onFailure {
        xtreamError = it.localizedMessage
      }
      isXtreamLoading = false
    }
  }

  Surface(
    modifier = modifier.fillMaxSize(),
    color = DarkBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
    ) {
      // 1. CLEAN ELEGANT TOD TOP BAR WITH GRADIENT
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(TodGradients.HeaderGlass)
          .border(
            width = 1.dp,
            color = DarkSurfaceBorder,
            shape = RoundedCornerShape(0.dp)
          )
          .padding(horizontal = 18.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, TodGradients.BorderGold, RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = null,
                tint = TodGold,
                modifier = Modifier.size(24.dp)
              )
            }

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "TOD",
                  color = TodGold,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "TV",
                  color = Color.White,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Text(
                text = "مشغل IPTV المباشر البسيط والأنيق",
                color = DarkTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
              )
            }
          }

          // Clean Status Pill with Smooth Gradient
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(if (xtreamAccount != null) Color(0x2210B981) else Color(0x22F5A623))
              .border(
                1.dp,
                if (xtreamAccount != null) Color(0x6610B981) else Color(0x66F5A623),
                RoundedCornerShape(20.dp)
              )
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (xtreamAccount != null) TodGreenLight else TodGold)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (xtreamAccount != null) "سيرفر متصل" else "جاهز للبث",
              color = if (xtreamAccount != null) TodGreenLight else TodGold,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // 2. THREE MODERN PILL TABS WITH GRADIENTS
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(DarkSurfaceElevated)
          .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        TabItem(
          title = "سيرفر Xtream",
          icon = Icons.Default.Dns,
          isSelected = selectedTab == HubTab.XTREAM,
          modifier = Modifier.weight(1f)
        ) {
          selectedTab = HubTab.XTREAM
        }

        TabItem(
          title = "رابط مخصص",
          icon = Icons.Default.Link,
          isSelected = selectedTab == HubTab.CUSTOM_URL,
          modifier = Modifier.weight(1f)
        ) {
          selectedTab = HubTab.CUSTOM_URL
        }

        TabItem(
          title = "بث مزدوج",
          icon = Icons.Default.ViewAgenda,
          isSelected = selectedTab == HubTab.DUAL_STREAM,
          modifier = Modifier.weight(1f)
        ) {
          selectedTab = HubTab.DUAL_STREAM
        }
      }

      // 3. TAB CONTENT
      when (selectedTab) {
        HubTab.XTREAM -> {
          XtreamContent(
            server = xtreamServer,
            onServerChange = { xtreamServer = it },
            user = xtreamUser,
            onUserChange = { xtreamUser = it },
            pass = xtreamPass,
            onPassChange = { xtreamPass = it },
            isPassVisible = isPassVisible,
            onTogglePassVisibility = { isPassVisible = !isPassVisible },
            isLoading = isXtreamLoading,
            error = xtreamError,
            account = xtreamAccount,
            categories = xtreamCategories,
            selectedCategory = selectedCategory,
            onSelectCategory = { catId ->
              selectedCategory = catId
              reloadChannels(catId)
            },
            channels = xtreamChannels,
            searchQuery = channelSearchQuery,
            onSearchQueryChange = { channelSearchQuery = it },
            favoriteIds = favoriteIds,
            onToggleFavorite = { streamId ->
              xtreamRepo.toggleFavorite(streamId)
              favoriteIds = xtreamRepo.getFavorites()
            },
            showOnlyFavorites = showOnlyFavorites,
            onToggleShowOnlyFavorites = { showOnlyFavorites = !showOnlyFavorites },
            isGridView = isGridView,
            onToggleGridView = { isGridView = !isGridView },
            recentChannels = recentChannels,
            onRefresh = { reloadChannels(selectedCategory) },
            onConnect = {
              scope.launch {
                focusManager.clearFocus()
                isXtreamLoading = true
                xtreamError = null
                val res = xtreamRepo.login(xtreamServer, xtreamUser, xtreamPass)
                res.onSuccess { info ->
                  xtreamAccount = info
                  xtreamRepo.saveCredentials(xtreamServer, xtreamUser, xtreamPass)
                  val catsRes = xtreamRepo.fetchCategories(xtreamServer, xtreamUser, xtreamPass)
                  catsRes.onSuccess { cats ->
                    xtreamCategories.clear()
                    xtreamCategories.add(XtreamCategory("ALL", "الكل (All)"))
                    xtreamCategories.addAll(cats)
                  }
                  val streamsRes = xtreamRepo.fetchStreams(xtreamServer, xtreamUser, xtreamPass, null)
                  streamsRes.onSuccess { streams ->
                    xtreamChannels.clear()
                    xtreamChannels.addAll(streams)
                  }
                }.onFailure {
                  xtreamError = it.localizedMessage ?: "فشل الاتصال بالسيرفر. تأكد من صحة الرابط والبيانات."
                }
                isXtreamLoading = false
              }
            },
            onDisconnect = {
              xtreamRepo.clearCredentials()
              xtreamAccount = null
              xtreamCategories.clear()
              xtreamChannels.clear()
            },
            onPlayChannel = { channel, allVisibleChannels ->
              xtreamRepo.addRecentChannel(channel)
              recentChannels.clear()
              recentChannels.addAll(xtreamRepo.getRecentChannels())

              val channelStreams = allVisibleChannels.map { ch ->
                BroadcastStream(
                  id = ch.streamId,
                  title = ch.name,
                  subtitle = "Xtream Live",
                  category = "Xtream IPTV",
                  tournamentOrLeague = "سيرفر IPTV مباشر",
                  streamUrl = ch.playUrl,
                  format = StreamFormat.AUTO,
                  isLive = true
                )
              }

              val targetStream = BroadcastStream(
                id = channel.streamId,
                title = channel.name,
                subtitle = "Xtream Live",
                category = "Xtream IPTV",
                tournamentOrLeague = "سيرفر IPTV مباشر",
                streamUrl = channel.playUrl,
                format = StreamFormat.AUTO,
                isLive = true
              )
              onPlayStream(targetStream, channelStreams)
            }
          )
        }

        HubTab.CUSTOM_URL -> {
          DirectUrlContent(
            title = directTitle,
            onTitleChange = { directTitle = it },
            url = directUrl,
            onUrlChange = { directUrl = it },
            userAgent = directUserAgent,
            onUserAgentChange = { directUserAgent = it },
            referer = directReferer,
            onRefererChange = { directReferer = it },
            origin = directOrigin,
            onOriginChange = { directOrigin = it },
            showAdvanced = showAdvancedDirect,
            onToggleAdvanced = { showAdvancedDirect = !showAdvancedDirect },
            history = customUrlHistory,
            onSelectHistoryItem = { hTitle, hUrl ->
              directTitle = hTitle
              directUrl = hUrl
            },
            onDeleteHistoryItem = { hUrl ->
              xtreamRepo.removeCustomUrlFromHistory(hUrl)
              customUrlHistory.clear()
              customUrlHistory.addAll(xtreamRepo.getCustomUrlHistory())
            },
            onPlayDirect = {
              focusManager.clearFocus()
              val rawUrl = directUrl.trim()
              if (rawUrl.isNotBlank()) {
                val normalizedUrl = when {
                  rawUrl.startsWith("http://", ignoreCase = true) ||
                  rawUrl.startsWith("https://", ignoreCase = true) ||
                  rawUrl.startsWith("rtsp://", ignoreCase = true) ||
                  rawUrl.startsWith("rtmp://", ignoreCase = true) -> rawUrl
                  rawUrl.contains("://") -> rawUrl
                  rawUrl.contains(".") -> "http://$rawUrl"
                  else -> rawUrl
                }

                val fmt = when {
                  normalizedUrl.contains(".mpd", ignoreCase = true) -> StreamFormat.DASH
                  normalizedUrl.contains(".m3u8", ignoreCase = true) -> StreamFormat.HLS
                  else -> StreamFormat.AUTO
                }

                val stream = BroadcastStream(
                  id = "direct_${System.currentTimeMillis()}",
                  title = directTitle.ifBlank { "بث مباشر مخصص" },
                  subtitle = "Direct Live Stream",
                  category = "Custom Stream",
                  tournamentOrLeague = "مباشر",
                  streamUrl = normalizedUrl,
                  format = fmt,
                  isLive = true,
                  userAgent = directUserAgent.takeIf { it.isNotBlank() },
                  referer = directReferer.takeIf { it.isNotBlank() },
                  origin = directOrigin.takeIf { it.isNotBlank() }
                )

                xtreamRepo.addCustomUrlToHistory(directTitle, normalizedUrl)
                customUrlHistory.clear()
                customUrlHistory.addAll(xtreamRepo.getCustomUrlHistory())

                onPlayStream(stream, listOf(stream))
              }
            }
          )
        }

        HubTab.DUAL_STREAM -> {
          DualStreamSetupContent(
            title1 = dualTitle1,
            onTitle1Change = { dualTitle1 = it },
            url1 = dualUrl1,
            onUrl1Change = { dualUrl1 = it },
            title2 = dualTitle2,
            onTitle2Change = { dualTitle2 = it },
            url2 = dualUrl2,
            onUrl2Change = { dualUrl2 = it },
            availableChannels = xtreamChannels,
            onSelectChannel1 = { ch ->
              dualTitle1 = ch.name
              dualUrl1 = ch.playUrl
            },
            onSelectChannel2 = { ch ->
              dualTitle2 = ch.name
              dualUrl2 = ch.playUrl
            },
            onPlayDual = {
              focusManager.clearFocus()
              val u1 = dualUrl1.trim()
              val u2 = dualUrl2.trim()
              if (u1.isNotBlank() && u2.isNotBlank()) {
                val stream1 = BroadcastStream(
                  id = "dual_1_${System.currentTimeMillis()}",
                  title = dualTitle1.ifBlank { "القناة 1" },
                  subtitle = "Dual Stream 1",
                  category = "Dual View",
                  tournamentOrLeague = "مباشر",
                  streamUrl = u1,
                  format = if (u1.contains(".m3u8", ignoreCase = true)) StreamFormat.HLS else StreamFormat.AUTO,
                  isLive = true
                )
                val stream2 = BroadcastStream(
                  id = "dual_2_${System.currentTimeMillis()}",
                  title = dualTitle2.ifBlank { "القناة 2" },
                  subtitle = "Dual Stream 2",
                  category = "Dual View",
                  tournamentOrLeague = "مباشر",
                  streamUrl = u2,
                  format = if (u2.contains(".m3u8", ignoreCase = true)) StreamFormat.HLS else StreamFormat.AUTO,
                  isLive = true
                )
                onPlayDualStream(stream1, stream2)
              }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun TabItem(
  title: String,
  icon: ImageVector,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val borderModifier = if (isSelected) {
    Modifier.border(1.dp, TodGoldGlow.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
  } else {
    Modifier.border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(
        if (isSelected) TodGradients.ActiveTab else TodGradients.InactiveTab
      )
      .then(borderModifier)
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) Color.Black else DarkTextSecondary,
        modifier = Modifier.size(17.dp)
      )
      Text(
        text = title,
        color = if (isSelected) Color.Black else Color.White,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
      )
    }
  }
}

@Composable
private fun XtreamContent(
  server: String,
  onServerChange: (String) -> Unit,
  user: String,
  onUserChange: (String) -> Unit,
  pass: String,
  onPassChange: (String) -> Unit,
  isPassVisible: Boolean,
  onTogglePassVisibility: () -> Unit,
  isLoading: Boolean,
  error: String?,
  account: XtreamAccountInfo?,
  categories: List<XtreamCategory>,
  selectedCategory: String?,
  onSelectCategory: (String?) -> Unit,
  channels: List<XtreamChannel>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  favoriteIds: Set<String>,
  onToggleFavorite: (String) -> Unit,
  showOnlyFavorites: Boolean,
  onToggleShowOnlyFavorites: () -> Unit,
  isGridView: Boolean,
  onToggleGridView: () -> Unit,
  recentChannels: List<XtreamChannel>,
  onRefresh: () -> Unit,
  onConnect: () -> Unit,
  onDisconnect: () -> Unit,
  onPlayChannel: (XtreamChannel, List<XtreamChannel>) -> Unit
) {
  if (account == null) {
    // LOGIN FORM
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141720))
            .border(1.dp, Color(0xFF262B3B), RoundedCornerShape(18.dp))
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x22FFC107)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Dns, contentDescription = null, tint = TodGold, modifier = Modifier.size(22.dp))
            }
            Column {
              Text(
                text = "تسجيل الدخول لسيرفر Xtream IPTV",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "جلب الباقات والقنوات بسرعه فائقة",
                color = DarkTextSecondary,
                fontSize = 11.sp
              )
            }
          }

          OutlinedTextField(
            value = server,
            onValueChange = onServerChange,
            label = { Text("عنوان السيرفر (Server URL)") },
            placeholder = { Text("http://example.com:8080") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF1B1F2B),
              unfocusedContainerColor = Color(0xFF1B1F2B),
              focusedBorderColor = TodGold,
              unfocusedBorderColor = Color(0xFF2D3345),
              focusedLabelColor = TodGold,
              unfocusedLabelColor = DarkTextSecondary,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          OutlinedTextField(
            value = user,
            onValueChange = onUserChange,
            label = { Text("اسم المستخدم (Username)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF1B1F2B),
              unfocusedContainerColor = Color(0xFF1B1F2B),
              focusedBorderColor = TodGold,
              unfocusedBorderColor = Color(0xFF2D3345),
              focusedLabelColor = TodGold,
              unfocusedLabelColor = DarkTextSecondary,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          OutlinedTextField(
            value = pass,
            onValueChange = onPassChange,
            label = { Text("كلمة المرور (Password)") },
            singleLine = true,
            visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF1B1F2B),
              unfocusedContainerColor = Color(0xFF1B1F2B),
              focusedBorderColor = TodGold,
              unfocusedBorderColor = Color(0xFF2D3345),
              focusedLabelColor = TodGold,
              unfocusedLabelColor = DarkTextSecondary,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            trailingIcon = {
              IconButton(onClick = onTogglePassVisibility) {
                Text(
                  text = if (isPassVisible) "إخفاء" else "إظهار",
                  color = TodGold,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          )

          error?.let { err ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x22FF2A55))
                .border(1.dp, Color(0x55FF2A55), RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
              Text(text = err, color = Color(0xFFFF6B6B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
          }

          Button(
            onClick = onConnect,
            enabled = !isLoading && server.isNotBlank() && user.isNotBlank() && pass.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = TodGold,
              contentColor = Color.Black,
              disabledContainerColor = DarkSurfaceHigh,
              disabledContentColor = DarkTextTertiary
            ),
            shape = RoundedCornerShape(14.dp)
          ) {
            if (isLoading) {
              CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("اتصال وتحميل القنوات المباشرة", fontWeight = FontWeight.Black, fontSize = 14.sp)
              }
            }
          }
        }
      }
    }
  } else {
    // CONNECTED CHANNELS VIEW
    val visibleChannels = remember(channels, searchQuery, showOnlyFavorites, favoriteIds) {
      channels.filter { ch ->
        val matchesSearch = searchQuery.isBlank() || ch.name.contains(searchQuery, ignoreCase = true)
        val matchesFav = !showOnlyFavorites || favoriteIds.contains(ch.streamId)
        matchesSearch && matchesFav
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. ACCOUNT INFO & TOOLS
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
              Brush.verticalGradient(
                listOf(DarkSurfaceElevated, DarkSurface)
              )
            )
            .border(
              1.dp,
              Brush.linearGradient(
                listOf(TodGold.copy(alpha = 0.4f), CyberNeonBlue.copy(alpha = 0.25f))
              ),
              RoundedCornerShape(18.dp)
            )
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
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
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF10B981))
              )
              Column {
                Text(
                  text = "المستخدم: ${account.username}",
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Black
                )
                Text(
                  text = "السيرفر: نشط ومتصل بجودة فائقة",
                  color = Color(0xFF34D399),
                  fontSize = 11.sp
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = onRefresh,
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(DarkSurfaceHigh)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TodGold, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(8.dp))
              IconButton(
                onClick = onDisconnect,
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0x22FF2A55))
              ) {
                Icon(Icons.Default.Clear, contentDescription = "Disconnect", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
              }
            }
          }

          // Search Bar & View Toggles
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = onSearchQueryChange,
              placeholder = { Text("بحث عن اسم القناة بالاسم أو الرقم...", fontSize = 13.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TodGold, modifier = Modifier.size(20.dp)) },
              singleLine = true,
              modifier = Modifier.weight(1f),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1B1F2B),
                unfocusedContainerColor = Color(0xFF1B1F2B),
                focusedBorderColor = TodGold,
                unfocusedBorderColor = Color(0xFF2D3345),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp)
            )

            // Grid / List Toggle
            IconButton(
              onClick = onToggleGridView,
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceHigh)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
            ) {
              Icon(
                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                contentDescription = "Toggle Grid/List",
                tint = TodGold,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }

      // 2. RECENTLY PLAYED HORIZONTAL STRIP
      if (recentChannels.isNotEmpty() && searchQuery.isBlank() && !showOnlyFavorites) {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.History, contentDescription = null, tint = TodGold, modifier = Modifier.size(17.dp))
              Text("شوهد مؤخراً (متابعة المشاهدة السريعة)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(recentChannels.take(10)) { rChan ->
                Row(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                      Brush.horizontalGradient(
                        listOf(DarkSurfaceElevated, DarkSurface)
                      )
                    )
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                    .clickable { onPlayChannel(rChan, channels) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(DarkSurfaceHigh),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TodGold, modifier = Modifier.size(16.dp))
                  }
                  Text(
                    text = rChan.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }

      // 3. CATEGORIES & FAVORITES CHIPS
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          // Favorites filter chip
          item {
            val favCount = favoriteIds.size
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (showOnlyFavorites) {
                    Brush.horizontalGradient(
                      listOf(Color(0xFFFF2A55), Color(0xFFFF5376))
                    )
                  } else {
                    Brush.linearGradient(
                      listOf(DarkSurfaceElevated, DarkSurface)
                    )
                  }
                )
                .border(
                  1.dp,
                  if (showOnlyFavorites) Color(0xFFFF708F) else DarkSurfaceBorder,
                  RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onToggleShowOnlyFavorites)
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = null,
                  tint = if (showOnlyFavorites) Color.White else Color(0xFFFF2A55),
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "المفضلة ($favCount)",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // Category Chips
          items(categories) { cat ->
            val isSelected = !showOnlyFavorites && (selectedCategory == cat.categoryId)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (isSelected) {
                    Brush.horizontalGradient(
                      listOf(PremiumGoldGradStart, PremiumGoldGradEnd)
                    )
                  } else {
                    Brush.linearGradient(
                      listOf(DarkSurfaceElevated, DarkSurface)
                    )
                  }
                )
                .border(
                  1.dp,
                  if (isSelected) Color(0x66FFFFFF) else DarkSurfaceBorder,
                  RoundedCornerShape(12.dp)
                )
                .clickable {
                  if (showOnlyFavorites) onToggleShowOnlyFavorites()
                  onSelectCategory(cat.categoryId)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
              Text(
                text = cat.categoryName,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
              )
            }
          }
        }
      }

      // 4. CHANNELS LIST OR GRID
      if (visibleChannels.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            if (isLoading) {
              CircularProgressIndicator(color = TodGold)
            } else {
              Text(
                text = if (showOnlyFavorites) "لم تقم بإضافة أي قنوات للمفضلة بعد. اضغط على رمز القلب ❤️ على أي قناة لإضافتها."
                       else "لا توجد قنوات مطابقة في هذا القسم",
                color = DarkTextSecondary,
                fontSize = 13.sp
              )
            }
          }
        }
      } else {
        if (!isGridView) {
          // Standard Clean List View
          items(visibleChannels) { channel ->
            val isFav = favoriteIds.contains(channel.streamId)
            ChannelListItem(
              channel = channel,
              isFavorite = isFav,
              onToggleFavorite = { onToggleFavorite(channel.streamId) },
              onClick = { onPlayChannel(channel, visibleChannels) }
            )
          }
        } else {
          // 2-Column Grid View
          val chunked = visibleChannels.chunked(2)
          items(chunked) { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              pair.forEach { channel ->
                val isFav = favoriteIds.contains(channel.streamId)
                Box(modifier = Modifier.weight(1f)) {
                  ChannelGridCard(
                    channel = channel,
                    isFavorite = isFav,
                    onToggleFavorite = { onToggleFavorite(channel.streamId) },
                    onClick = { onPlayChannel(channel, visibleChannels) }
                  )
                }
              }
              if (pair.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChannelListItem(
  channel: XtreamChannel,
  isFavorite: Boolean,
  onToggleFavorite: () -> Unit,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(
        Brush.horizontalGradient(
          listOf(DarkSurfaceElevated, DarkSurface)
        )
      )
      .border(
        1.dp,
        if (isFavorite) TodGold.copy(alpha = 0.4f) else DarkSurfaceBorder,
        RoundedCornerShape(14.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.weight(1f)
    ) {
      if (!channel.iconUrl.isNullOrBlank()) {
        AsyncImage(
          model = channel.iconUrl,
          contentDescription = channel.name,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceHigh)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
        )
      } else {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.linearGradient(
                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
              )
            )
            .border(1.dp, Color(0x33F5A623), RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.LiveTv, contentDescription = null, tint = TodGold, modifier = Modifier.size(20.dp))
        }
      }

      Column {
        Text(
          text = channel.name,
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0x2210B981))
              .padding(horizontal = 5.dp, vertical = 1.dp)
          ) {
            Text(
              text = "LIVE HD",
              color = Color(0xFF34D399),
              fontSize = 9.sp,
              fontWeight = FontWeight.Black
            )
          }
          Text(
            text = "فائق السرعة",
            color = DarkTextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      IconButton(onClick = onToggleFavorite, modifier = Modifier.size(38.dp)) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (isFavorite) Color(0xFFFF2A55) else DarkTextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }

      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(PremiumGoldGradStart, PremiumGoldGradEnd)
            )
          )
          .padding(7.dp),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(17.dp))
      }
    }
  }
}

@Composable
private fun ChannelGridCard(
  channel: XtreamChannel,
  isFavorite: Boolean,
  onToggleFavorite: () -> Unit,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(
        Brush.verticalGradient(
          listOf(DarkSurfaceElevated, DarkSurface)
        )
      )
      .border(
        1.dp,
        if (isFavorite) TodGold.copy(alpha = 0.5f) else DarkSurfaceBorder,
        RoundedCornerShape(14.dp)
      )
      .clickable(onClick = onClick)
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (!channel.iconUrl.isNullOrBlank()) {
        AsyncImage(
          model = channel.iconUrl,
          contentDescription = channel.name,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceHigh)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
        )
      } else {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
              Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
            )
            .border(1.dp, Color(0x33F5A623), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.LiveTv, contentDescription = null, tint = TodGold, modifier = Modifier.size(18.dp))
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(30.dp)) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) Color(0xFFFF2A55) else DarkTextSecondary,
            modifier = Modifier.size(17.dp)
          )
        }
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(listOf(PremiumGoldGradStart, PremiumGoldGradEnd))
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(14.dp))
        }
      }
    }

    Column {
      Text(
        text = channel.name,
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = "بث مباشر HD",
        color = Color(0xFF34D399),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
private fun DirectUrlContent(
  title: String,
  onTitleChange: (String) -> Unit,
  url: String,
  onUrlChange: (String) -> Unit,
  userAgent: String,
  onUserAgentChange: (String) -> Unit,
  referer: String,
  onRefererChange: (String) -> Unit,
  origin: String,
  onOriginChange: (String) -> Unit,
  showAdvanced: Boolean,
  onToggleAdvanced: () -> Unit,
  history: List<Pair<String, String>>,
  onSelectHistoryItem: (String, String) -> Unit,
  onDeleteHistoryItem: (String) -> Unit,
  onPlayDirect: () -> Unit
) {
  val context = LocalContext.current
  val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

  val trimmedUrl = url.trim()
  val hasValidScheme = trimmedUrl.startsWith("http://", ignoreCase = true) ||
      trimmedUrl.startsWith("https://", ignoreCase = true) ||
      trimmedUrl.startsWith("rtsp://", ignoreCase = true) ||
      trimmedUrl.startsWith("rtmp://", ignoreCase = true)
  val isLikelyHostUrl = !hasValidScheme && trimmedUrl.contains(".") && trimmedUrl.length >= 6
  val isValidUrl = hasValidScheme || isLikelyHostUrl
  val isMalformedUrl = trimmedUrl.isNotBlank() && !isValidUrl

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(
            Brush.verticalGradient(
              listOf(DarkSurfaceElevated, DarkSurface)
            )
          )
          .border(
            1.dp,
            Brush.linearGradient(
              listOf(TodGold.copy(alpha = 0.4f), CyberElectricIndigo.copy(alpha = 0.3f), Color(0x22FFFFFF))
            ),
            RoundedCornerShape(20.dp)
          )
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0x22F5A623)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Link, contentDescription = null, tint = TodGold, modifier = Modifier.size(22.dp))
          }
          Column {
            Text(
              text = "تشغيل رابط بث مباشر (M3U8 / DASH / TS)",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "دعم كامل للـ HLS والـ Tokens وتجاوز الحمايات",
              color = DarkTextSecondary,
              fontSize = 11.sp
            )
          }
        }

        OutlinedTextField(
          value = title,
          onValueChange = onTitleChange,
          label = { Text("اسم البث / القناة") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1B1F2B),
            unfocusedContainerColor = Color(0xFF1B1F2B),
            focusedBorderColor = TodGold,
            unfocusedBorderColor = Color(0xFF2D3345),
            focusedLabelColor = TodGold,
            unfocusedLabelColor = DarkTextSecondary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("رابط البث (Stream URL)") },
            placeholder = { Text("https://.../live.m3u8") },
            isError = isMalformedUrl,
            trailingIcon = {
              IconButton(
                onClick = {
                  val clipData = clipboard.primaryClip
                  if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString() ?: ""
                    if (text.isNotBlank()) onUrlChange(text)
                  }
                }
              ) {
                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = TodGold)
              }
            },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF1B1F2B),
              unfocusedContainerColor = Color(0xFF1B1F2B),
              focusedBorderColor = TodGold,
              unfocusedBorderColor = Color(0xFF2D3345),
              focusedLabelColor = TodGold,
              unfocusedLabelColor = DarkTextSecondary,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          if (isMalformedUrl) {
            Text(
              text = "يرجى كتابة رابط صالح يبدأ بـ http:// أو https://",
              color = Color(0xFFFF5252),
              fontSize = 11.sp
            )
          }
        }

        // Advanced Headers toggle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceHigh)
            .clickable(onClick = onToggleAdvanced)
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = TodGold, modifier = Modifier.size(18.dp))
            Text("هيدرات متقدمة (Referer / User-Agent لتجاوز الحماية)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          Icon(
            imageVector = if (showAdvanced) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = TodGold
          )
        }

        AnimatedVisibility(visible = showAdvanced) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = userAgent,
              onValueChange = onUserAgentChange,
              label = { Text("User-Agent (اختياري)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TodGold,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            OutlinedTextField(
              value = referer,
              onValueChange = onRefererChange,
              label = { Text("Referer (اختياري)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TodGold,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            OutlinedTextField(
              value = origin,
              onValueChange = onOriginChange,
              label = { Text("Origin (اختياري)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TodGold,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )
          }
        }

        Button(
          onClick = onPlayDirect,
          enabled = isValidUrl,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = TodGold,
            contentColor = Color.Black,
            disabledContainerColor = DarkSurfaceHigh,
            disabledContentColor = DarkTextTertiary
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
            Text("تشغيل في المشغل الأفقي الفوري", fontWeight = FontWeight.Black, fontSize = 14.sp)
          }
        }
      }
    }

    // SAVED URL HISTORY
    if (history.isNotEmpty()) {
      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.History, contentDescription = null, tint = TodGold, modifier = Modifier.size(18.dp))
          Text("سجل الروابط السابقة المحفوظة", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
      }

      items(history) { (hTitle, hUrl) ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
              Brush.horizontalGradient(
                listOf(DarkSurfaceElevated, DarkSurface)
              )
            )
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { onSelectHistoryItem(hTitle, hUrl) }
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = hTitle,
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = hUrl,
              color = DarkTextSecondary,
              fontSize = 11.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(Color(0x22F5A623))
                .padding(6.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TodGold, modifier = Modifier.size(16.dp))
            }
            IconButton(
              onClick = { onDeleteHistoryItem(hUrl) },
              modifier = Modifier.size(34.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(17.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DualStreamSetupContent(
  title1: String,
  onTitle1Change: (String) -> Unit,
  url1: String,
  onUrl1Change: (String) -> Unit,
  title2: String,
  onTitle2Change: (String) -> Unit,
  url2: String,
  onUrl2Change: (String) -> Unit,
  availableChannels: List<XtreamChannel>,
  onSelectChannel1: (XtreamChannel) -> Unit,
  onSelectChannel2: (XtreamChannel) -> Unit,
  onPlayDual: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(
            Brush.verticalGradient(
              listOf(DarkSurfaceElevated, DarkSurface)
            )
          )
          .border(
            1.dp,
            Brush.linearGradient(
              listOf(TodCyan.copy(alpha = 0.5f), CyberNeonBlue.copy(alpha = 0.3f), Color(0x22FFFFFF))
            ),
            RoundedCornerShape(20.dp)
          )
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Brush.linearGradient(listOf(TodCyan, CyberNeonBlue)))
              .padding(horizontal = 9.dp, vertical = 4.dp)
          ) {
            Text("DUAL ULTRA", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
          }
          Text(
            text = "تشغيل مباراتين أو قناتين في وقت واحد",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Text(
          text = "تابع مباراتين جنباً إلى جنب بسلاسة مطلقة وبدون أي تأخير، مع التحكم في صوت كل شاشة بشكل مستقل فوراً.",
          color = DarkTextSecondary,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      }
    }

    // Stream 1 Configuration Card
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(
            Brush.verticalGradient(
              listOf(DarkSurfaceElevated, DarkSurface)
            )
          )
          .border(1.dp, TodAmberYellow.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(TodAmberYellow),
            contentAlignment = Alignment.Center
          ) {
            Text("1", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Black)
          }
          Text("القناة الأولى (الشاشة اليسرى)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
          value = title1,
          onValueChange = onTitle1Change,
          label = { Text("اسم القناة 1") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1B1F2B),
            unfocusedContainerColor = Color(0xFF1B1F2B),
            focusedBorderColor = TodAmberYellow,
            unfocusedBorderColor = Color(0xFF2D3345),
            focusedLabelColor = TodAmberYellow,
            unfocusedLabelColor = DarkTextSecondary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        OutlinedTextField(
          value = url1,
          onValueChange = onUrl1Change,
          label = { Text("رابط البث 1 (HLS / m3u8)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1B1F2B),
            unfocusedContainerColor = Color(0xFF1B1F2B),
            focusedBorderColor = TodAmberYellow,
            unfocusedBorderColor = Color(0xFF2D3345),
            focusedLabelColor = TodAmberYellow,
            unfocusedLabelColor = DarkTextSecondary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        if (availableChannels.isNotEmpty()) {
          Text("أو اختر مباشرة من قنوات السيرفر:", color = DarkTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(availableChannels.take(15)) { ch ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(DarkSurfaceHigh)
                  .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                  .clickable { onSelectChannel1(ch) }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(ch.name, color = Color.White, fontSize = 11.sp, maxLines = 1)
              }
            }
          }
        }
      }
    }

    // Stream 2 Configuration Card
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(
            Brush.verticalGradient(
              listOf(DarkSurfaceElevated, DarkSurface)
            )
          )
          .border(1.dp, TodCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(TodCyan),
            contentAlignment = Alignment.Center
          ) {
            Text("2", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Black)
          }
          Text("القناة الثانية (الشاشة اليمنى)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
          value = title2,
          onValueChange = onTitle2Change,
          label = { Text("اسم القناة 2") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1B1F2B),
            unfocusedContainerColor = Color(0xFF1B1F2B),
            focusedBorderColor = TodCyan,
            unfocusedBorderColor = Color(0xFF2D3345),
            focusedLabelColor = TodCyan,
            unfocusedLabelColor = DarkTextSecondary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        OutlinedTextField(
          value = url2,
          onValueChange = onUrl2Change,
          label = { Text("رابط البث 2 (HLS / m3u8)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1B1F2B),
            unfocusedContainerColor = Color(0xFF1B1F2B),
            focusedBorderColor = TodCyan,
            unfocusedBorderColor = Color(0xFF2D3345),
            focusedLabelColor = TodCyan,
            unfocusedLabelColor = DarkTextSecondary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        if (availableChannels.isNotEmpty()) {
          Text("أو اختر مباشرة من قنوات السيرفر:", color = DarkTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(availableChannels.take(15)) { ch ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(DarkSurfaceHigh)
                  .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                  .clickable { onSelectChannel2(ch) }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(ch.name, color = Color.White, fontSize = 11.sp, maxLines = 1)
              }
            }
          }
        }
      }
    }

    // Launch Dual Stream Button
    item {
      Button(
        onClick = onPlayDual,
        enabled = url1.isNotBlank() && url2.isNotBlank(),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = TodAmberYellow,
          contentColor = Color.Black,
          disabledContainerColor = DarkSurfaceElevated,
          disabledContentColor = DarkTextSecondary
        )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
          Text(
            text = "تشغيل البث الثنائي الآن",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
          )
        }
      }
    }
  }
}
