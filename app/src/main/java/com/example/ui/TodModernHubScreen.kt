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
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodLiveRed
import kotlinx.coroutines.launch

enum class HubTab {
  XTREAM,
  CUSTOM_URL
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodModernHubScreen(
  onPlayStream: (BroadcastStream, List<BroadcastStream>) -> Unit,
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

  val infiniteTransition = rememberInfiniteTransition(label = "beacon")
  val liveDotAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dotAlpha"
  )

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
      // 1. TOP BRANDING BAR
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkSurface)
          .border(1.dp, DarkSurfaceBorder)
          .padding(horizontal = 18.dp, vertical = 14.dp)
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
            TodWatermarkBadge(size = 38.dp)

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "TOD PLAYER",
                  color = Color.White,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(TodGold)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "PRO",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                  )
                }
              }
              Text(
                text = "مشغل IPTV وسيرفرات Xtream الفائقة",
                color = DarkTextSecondary,
                fontSize = 11.sp
              )
            }
          }

          // Status Beacon
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(if (xtreamAccount != null) Color(0x2210B981) else Color(0x22FF2A55))
              .border(1.dp, if (xtreamAccount != null) Color(0x5510B981) else Color(0x55FF2A55), RoundedCornerShape(20.dp))
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (xtreamAccount != null) Color(0xFF10B981) else TodLiveRed.copy(alpha = liveDotAlpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (xtreamAccount != null) "متصل بالسيرفر" else "جاهز للبث",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // 2. TWO CLEAN TABS (Xtream Codes vs Direct Stream)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(DarkSurfaceElevated)
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        TabItem(
          title = "سيرفر Xtream Codes",
          icon = Icons.Default.Dns,
          isSelected = selectedTab == HubTab.XTREAM,
          modifier = Modifier.weight(1f)
        ) {
          selectedTab = HubTab.XTREAM
        }

        TabItem(
          title = "رابط مباشر / M3U",
          icon = Icons.Default.Link,
          isSelected = selectedTab == HubTab.CUSTOM_URL,
          modifier = Modifier.weight(1f)
        ) {
          selectedTab = HubTab.CUSTOM_URL
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
                  format = StreamFormat.HLS,
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
                format = StreamFormat.HLS,
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
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (isSelected) TodGold else Color.Transparent)
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
        color = if (isSelected) Color.Black else DarkTextSecondary,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Dns, contentDescription = null, tint = TodGold, modifier = Modifier.size(20.dp))
            Text(
              text = "تسجيل الدخول لسيرفر Xtream IPTV",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "أدخل بيانات اشتراك IPTV الخاص بك وسيتم فحص السيرفر وجلب جميع باقات القنوات الرياضية والترفيهية فوراً وبدون أي تعليق.",
            color = DarkTextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
          )

          OutlinedTextField(
            value = server,
            onValueChange = onServerChange,
            label = { Text("عنوان السيرفر (Server URL)") },
            placeholder = { Text("http://example.com:8080") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TodGold,
              unfocusedBorderColor = DarkSurfaceBorder,
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
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TodGold,
              unfocusedBorderColor = DarkSurfaceBorder,
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
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TodGold,
              unfocusedBorderColor = DarkSurfaceBorder,
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
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33FF2A55))
                .padding(10.dp)
            ) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
              Text(text = err, color = Color(0xFFFF5252), fontSize = 12.sp)
            }
          }

          Button(
            onClick = onConnect,
            enabled = !isLoading && server.isNotBlank() && user.isNotBlank() && pass.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = TodGold,
              contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            if (isLoading) {
              CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("اتصال وتحميل القنوات المباشرة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, Color(0x33F5A623), RoundedCornerShape(16.dp))
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF10B981))
              )
              Text(
                text = "المستخدم: ${account.username}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = onRefresh, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TodGold, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(6.dp))
              IconButton(onClick = onDisconnect, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.Clear, contentDescription = "Disconnect", tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
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
              placeholder = { Text("بحث عن اسم القناة...", fontSize = 13.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(18.dp)) },
              singleLine = true,
              modifier = Modifier.weight(1f),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TodGold,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            )

            // Grid / List Toggle
            IconButton(
              onClick = onToggleGridView,
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
            ) {
              Icon(
                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                contentDescription = "Toggle Grid/List",
                tint = TodGold,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      // 2. RECENTLY PLAYED HORIZONTAL STRIP
      if (recentChannels.isNotEmpty() && searchQuery.isBlank() && !showOnlyFavorites) {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.History, contentDescription = null, tint = TodGold, modifier = Modifier.size(16.dp))
              Text("شوهد مؤخراً (تابع المشاهدة)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(recentChannels.take(8)) { rChan ->
                Row(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                    .clickable { onPlayChannel(rChan, channels) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(26.dp)
                      .clip(RoundedCornerShape(4.dp))
                      .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.LiveTv, contentDescription = null, tint = TodGold, modifier = Modifier.size(14.dp))
                  }
                  Text(
                    text = rChan.name,
                    color = Color.White,
                    fontSize = 12.sp,
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
          contentPadding = PaddingValues(vertical = 2.dp)
        ) {
          // Favorites filter chip
          item {
            val favCount = favoriteIds.size
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (showOnlyFavorites) Color(0xFFFF2A55) else DarkSurface)
                .border(1.dp, if (showOnlyFavorites) Color(0xFFFF2A55) else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleShowOnlyFavorites)
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = null,
                  tint = if (showOnlyFavorites) Color.White else Color(0xFFFF2A55),
                  modifier = Modifier.size(14.dp)
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
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) TodGold else DarkSurface)
                .border(1.dp, if (isSelected) TodGold else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                .clickable {
                  if (showOnlyFavorites) onToggleShowOnlyFavorites()
                  onSelectCategory(cat.categoryId)
                }
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = cat.categoryName,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
      .clip(RoundedCornerShape(12.dp))
      .background(DarkSurface)
      .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      if (!channel.iconUrl.isNullOrBlank()) {
        AsyncImage(
          model = channel.iconUrl,
          contentDescription = channel.name,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated)
        )
      } else {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.LiveTv, contentDescription = null, tint = TodGold, modifier = Modifier.size(18.dp))
        }
      }

      Column {
        Text(
          text = channel.name,
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "بث مباشر HD",
          color = DarkTextSecondary,
          fontSize = 11.sp
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (isFavorite) Color(0xFFFF2A55) else DarkTextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }

      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(TodGold)
          .padding(6.dp)
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(16.dp))
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
      .clip(RoundedCornerShape(12.dp))
      .background(DarkSurface)
      .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
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
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated)
        )
      } else {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.LiveTv, contentDescription = null, tint = TodGold, modifier = Modifier.size(18.dp))
        }
      }

      IconButton(onClick = onToggleFavorite, modifier = Modifier.size(30.dp)) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (isFavorite) Color(0xFFFF2A55) else DarkTextSecondary,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Text(
      text = channel.name,
      color = Color.White,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
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
          .clip(RoundedCornerShape(16.dp))
          .background(DarkSurface)
          .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Link, contentDescription = null, tint = TodGold, modifier = Modifier.size(20.dp))
          Text(
            text = "تشغيل رابط بث مباشر (M3U8 / DASH / TS)",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }

        OutlinedTextField(
          value = title,
          onValueChange = onTitleChange,
          label = { Text("اسم البث / القناة") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TodGold,
            unfocusedBorderColor = DarkSurfaceBorder,
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
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TodGold,
              unfocusedBorderColor = DarkSurfaceBorder,
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggleAdvanced)
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(16.dp))
            Text("هيدرات متقدمة (Referer / User-Agent لتجاوز الحماية)", color = DarkTextSecondary, fontSize = 12.sp)
          }
          Icon(
            imageVector = if (showAdvanced) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = DarkTextSecondary
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
            .height(48.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = TodGold,
            contentColor = Color.Black
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Text("تشغيل في المشغل الأفقي الفوري", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      }
    }

    // SAVED URL HISTORY
    if (history.isNotEmpty()) {
      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.History, contentDescription = null, tint = TodGold, modifier = Modifier.size(16.dp))
          Text("سجل الروابط السابقة المحفوظة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }

      items(history) { (hTitle, hUrl) ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { onSelectHistoryItem(hTitle, hUrl) }
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = hTitle,
              color = Color.White,
              fontSize = 13.sp,
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

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { onDeleteHistoryItem(hUrl) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkTextSecondary, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}
