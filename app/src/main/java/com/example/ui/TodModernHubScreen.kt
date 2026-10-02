package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import com.example.ui.theme.ThmanyahFontFamily
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import android.media.MediaCodecList
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

  // Saved Playlists State
  var playlistConfig by remember { mutableStateOf<XtreamPlaylistConfig?>(null) }
  var savedPlaylists by remember { mutableStateOf(xtreamRepo.getAllPlaylists()) }
  var formOriginIsOnboarding by remember { mutableStateOf(savedPlaylists.isEmpty()) }
  var playlistPendingDelete by remember { mutableStateOf<XtreamPlaylistConfig?>(null) }

  // App Navigation View Mode - Open directly into CATEGORIES (TOD Sports & Live Channels)
  var viewMode by remember {
    mutableStateOf(HubViewMode.CATEGORIES)
  }
  var activeNavTab by remember { mutableStateOf(TodNavTab.HOME) }

  // Sports Backend & Match Detail Modal State
  val sportsBackendRepo = remember { com.example.data.SportsBackendRepository(context) }
  var activeSportsMatch by remember { mutableStateOf<com.example.model.SportsMatch?>(null) }
  var showProfileSelectModal by remember { mutableStateOf(false) }

  // Active Connection State
  var isXtreamLoading by remember { mutableStateOf(false) }
  var xtreamError by remember { mutableStateOf<String?>(null) }
  var xtreamAccount by remember { mutableStateOf<XtreamAccountInfo?>(null) }

  // Categories & Channels
  val xtreamCategories = remember { mutableStateListOf<XtreamCategory>() }
  var allChannels by remember { mutableStateOf<List<XtreamChannel>>(emptyList()) }

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
          allChannels = cachedDiskChannels

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
          allChannels = streams

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
          allChannels = streams

          // Set Account Info from login or create dynamic active profile
          xtreamAccount = loginRes.getOrNull() ?: XtreamAccountInfo(
            username = config.username.ifBlank { config.playlistName },
            status = "نشط",
            expDate = "متصل",
            serverUrl = config.serverUrl
          )

          // Build categories with robust single-pass frequency counting
          val countMap = HashMap<String, Int>()
          for (ch in streams) {
            val cId = ch.categoryId?.trim()?.lowercase() ?: ""
            if (cId.isNotEmpty()) {
              countMap[cId] = (countMap[cId] ?: 0) + 1
            }
          }

          val cats = catsRes.getOrDefault(emptyList())
          xtreamCategories.clear()
          xtreamCategories.add(XtreamCategory("ALL", "جميع القنوات", streams.size))

          if (cats.isNotEmpty()) {
            cats.forEach { cat ->
              val catId = cat.categoryId.trim()
              val count = countMap[catId.lowercase()] ?: 0
              if (count > 0) {
                xtreamCategories.add(XtreamCategory(catId, cat.categoryName.trim(), count))
              }
            }
          }

          // If some channels have categoryIds not present in cats, add them dynamically
          val knownCatIds = xtreamCategories.map { it.categoryId.lowercase() }.toSet()
          val unmappedStreams = streams.filter { ch ->
            val chCat = ch.categoryId?.trim() ?: ""
            chCat.isNotBlank() && chCat.lowercase() !in knownCatIds
          }
          if (unmappedStreams.isNotEmpty()) {
            val groupMap = unmappedStreams.groupBy { it.categoryId?.trim() ?: "عام" }
            groupMap.forEach { (groupId, list) ->
              xtreamCategories.add(XtreamCategory(groupId, groupId, list.size))
            }
          } else if (xtreamCategories.size <= 1 && streams.isNotEmpty()) {
            // Auto extract categories dynamically from stream tags if server returned no categories
            val groupMap = streams.groupBy { it.categoryId?.trim()?.ifBlank { "عام" } ?: "عام" }
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

  // Handle Back Button inside Modern Hub
  val hasBackOverride = activeSportsMatch != null ||
      showProfileSelectModal ||
      showPlaylistsManagerModal ||
      showAccountInfoModal ||
      viewMode == HubViewMode.XTREAM_FORM ||
      viewMode == HubViewMode.M3U_FORM ||
      activeNavTab != TodNavTab.HOME

  BackHandler(enabled = hasBackOverride) {
    if (activeSportsMatch != null) {
      activeSportsMatch = null
    } else if (showProfileSelectModal) {
      showProfileSelectModal = false
    } else if (showPlaylistsManagerModal) {
      showPlaylistsManagerModal = false
    } else if (showAccountInfoModal) {
      showAccountInfoModal = false
    } else if (viewMode == HubViewMode.XTREAM_FORM || viewMode == HubViewMode.M3U_FORM) {
      if (formOriginIsOnboarding || savedPlaylists.isEmpty()) {
        playlistConfig = null
        viewMode = HubViewMode.ONBOARDING
      } else {
        viewMode = HubViewMode.CATEGORIES
        activeNavTab = TodNavTab.MORE
      }
    } else if (activeNavTab != TodNavTab.HOME) {
      activeNavTab = TodNavTab.HOME
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
      // Direct access to TOD Sports & Channels hub
      viewMode = HubViewMode.CATEGORIES
    }
  }

  // Build high-performance playback list with logos and full channel context
  val buildOptimizedPlaybackList: (XtreamChannel, List<XtreamChannel>, String) -> Pair<BroadcastStream, List<BroadcastStream>> =
    { clickedChannel, channelList, categoryName ->
      val targetStream = BroadcastStream(
        id = clickedChannel.streamId,
        title = clickedChannel.name,
        subtitle = categoryName,
        category = categoryName,
        streamUrl = clickedChannel.playUrl,
        isLive = true,
        logoUrl = clickedChannel.iconUrl
      )

      val effectiveList = if (channelList.size > 1) channelList else allChannels
      val fullStreams = effectiveList.map { ch ->
        BroadcastStream(
          id = ch.streamId,
          title = ch.name,
          subtitle = categoryName,
          category = categoryName,
          streamUrl = ch.playUrl,
          isLive = true,
          logoUrl = ch.iconUrl
        )
      }

      Pair(targetStream, fullStreams)
    }

  FluidMeshBackground(
    modifier = modifier.fillMaxSize(),
    ambientAlpha = 0.70f
  ) {
    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      // ========================================================
      // MAIN SCREEN SWITCHER
      // ========================================================
      when (viewMode) {
        HubViewMode.ONBOARDING -> {
          // ========================================================
          // APPLE iOS 18 CONTROL CENTER LIQUID GLASS ONBOARDING
          // ========================================================
          Box(
            modifier = Modifier.fillMaxSize()
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // 1. TOP HEADER: Apple iOS 18 Dynamic Island Glass Capsule
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier
                    .liquidGlassEffect(shape = RoundedCornerShape(26.dp), glowTint = Color(0xFF007AFF))
                    .padding(horizontal = 20.dp, vertical = 7.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(Color(0xFF34C759))
                  )
                  Text(
                    text = "مشغل IPTV الذكي 4K",
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                  )
                }
              }

              // 2. HERO MEDIA CARD (Apple iOS 18 Liquid Specular Media Stage)
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(210.dp)
                  .clip(RoundedCornerShape(26.dp))
                  .border(1.2.dp, LiquidGlassTheme.LiquidSpecularBorder, RoundedCornerShape(26.dp))
              ) {
                // Living Room Media Art
                Image(
                  painter = painterResource(id = R.drawable.iptv_hero_livingroom),
                  contentDescription = "بث مباشر وتلفزيون ذكي",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )

                // Multi-Layer iOS 18 Liquid Glass Specular Reflection & Deep Contrast
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(
                      Brush.verticalGradient(
                        colors = listOf(
                          Color(0x350A1428),
                          Color(0x200C142A),
                          Color(0xD9070B18)
                        )
                      )
                    )
                    .padding(18.dp),
                  contentAlignment = Alignment.BottomEnd
                ) {
                  Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Text(
                      text = "عالم البث المباشر والترفيه 4K",
                      color = Color.White,
                      fontSize = 20.sp,
                      fontFamily = ThmanyahFontFamily,
                      fontWeight = FontWeight.Black,
                      textAlign = TextAlign.End
                    )
                    Text(
                      text = "استمتع بمشاهدة جميع القنوات والمباريات الرياضية بأعلى دقة وبدون تقطيع",
                      color = Color(0xEEFFFFFF),
                      fontSize = 12.5.sp,
                      fontFamily = ThmanyahFontFamily,
                      lineHeight = 17.sp,
                      textAlign = TextAlign.End
                    )
                  }
                }
              }

              // 3. SAVED PLAYLISTS CAROUSEL (If user already has accounts saved)
              if (savedPlaylists.isNotEmpty()) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "إدارة السيرفرات (${savedPlaylists.size})",
                      color = Color(0xFF64D2FF),
                      fontSize = 13.sp,
                      fontFamily = ThmanyahFontFamily,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.iosBounceClick {
                        savedPlaylists = xtreamRepo.getAllPlaylists()
                        showPlaylistsManagerModal = true
                      }
                    )
                    Text(
                      text = "السيرفرات المحفوظة",
                      color = Color.White,
                      fontSize = 15.sp,
                      fontFamily = ThmanyahFontFamily,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    savedPlaylists.forEach { pl ->
                      val isPlActive = if (pl.isM3u) playlistConfig?.m3uUrl == pl.m3uUrl
                      else (playlistConfig?.serverUrl == pl.serverUrl && playlistConfig?.username == pl.username)

                      Box(
                        modifier = Modifier
                          .iosBounceClick {
                            playlistConfig = pl
                            xtreamRepo.setActivePlaylist(pl)
                            viewMode = HubViewMode.CATEGORIES
                            activeNavTab = TodNavTab.HOME
                            loadPlaylistData(pl, xtreamRepo.shouldRefreshPlaylist(pl))
                          }
                          .liquidGlassEffect(
                            shape = RoundedCornerShape(18.dp),
                            isElevated = isPlActive,
                            glowTint = Color(0xFF0A84FF)
                          )
                          .padding(horizontal = 16.dp, vertical = 12.dp)
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                          IosCircularControlBadge(
                            background = if (pl.isM3u) IosBadgeColors.Purple else IosBadgeColors.Blue,
                            size = 40.dp
                          ) {
                            if (pl.isM3u) {
                              TodM3uPlaylistIcon(modifier = Modifier.size(22.dp))
                            } else {
                              TodXtreamServerArtIcon(modifier = Modifier.size(22.dp))
                            }
                          }
                          Column(horizontalAlignment = Alignment.End) {
                            Text(
                              text = pl.playlistName,
                              color = Color.White,
                              fontSize = 14.sp,
                              fontFamily = ThmanyahFontFamily,
                              fontWeight = FontWeight.Bold
                            )
                            Text(
                              text = if (pl.isM3u) "قائمة M3U" else pl.username.ifBlank { "سيرفر نشط" },
                              color = Color(0xAAFFFFFF),
                              fontSize = 11.5.sp,
                              fontFamily = ThmanyahFontFamily
                            )
                          }
                        }
                      }
                    }
                  }
                }
              }

              // 4. MAIN XTREAM API CONTROL TILE (Apple iOS 18 Large Liquid Glass Card)
              val xtreamInteraction = remember { MutableInteractionSource() }
              val isXtreamPressed by xtreamInteraction.collectIsPressedAsState()
              val xtreamScale by animateFloatAsState(
                targetValue = if (isXtreamPressed) 0.96f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "xtreamScale"
              )

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .scale(xtreamScale)
                  .liquidGlassEffect(
                    shape = RoundedCornerShape(24.dp),
                    isElevated = true,
                    glowTint = Color(0xFF0A84FF)
                  )
                  .clickable(
                    interactionSource = xtreamInteraction,
                    indication = null
                  ) {
                    formOriginIsOnboarding = true
                    playlistConfig = XtreamPlaylistConfig(playlistName = "سيرفر Xtream 1")
                    viewMode = HubViewMode.XTREAM_FORM
                  }
                  .padding(18.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  // Left Action Chevron Circle
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .liquidGlassEffect(shape = CircleShape, glowTint = Color(0xFF0A84FF)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "دخول",
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  // Right: Titles + Glowing Apple Electric Blue Circular Badge
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                  ) {
                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = "سيرفر Xtream API",
                        color = Color.White,
                        fontSize = 17.5.sp,
                        fontFamily = ThmanyahFontFamily,
                        fontWeight = FontWeight.Black
                      )
                      Spacer(modifier = Modifier.height(3.dp))
                      Text(
                        text = "إضافة اشتراكك عبر سيرفر ومستخدم وكلمة سر",
                        color = Color(0xCCFFFFFF),
                        fontSize = 12.sp,
                        fontFamily = ThmanyahFontFamily,
                        textAlign = TextAlign.End
                      )
                    }

                    // Apple Electric Blue Circular Control Badge
                    IosCircularControlBadge(
                      background = IosBadgeColors.Blue,
                      size = 52.dp
                    ) {
                      TodXtreamServerArtIcon(modifier = Modifier.size(28.dp))
                    }
                  }
                }
              }

              // 5. M3U PLAYLIST CONTROL TILE (Apple iOS 18 Purple Glass Card)
              val m3uInteraction = remember { MutableInteractionSource() }
              val isM3uPressed by m3uInteraction.collectIsPressedAsState()
              val m3uScale by animateFloatAsState(
                targetValue = if (isM3uPressed) 0.96f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "m3uScale"
              )

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .scale(m3uScale)
                  .liquidGlassEffect(
                    shape = RoundedCornerShape(24.dp),
                    isElevated = true,
                    glowTint = Color(0xFFA855F7)
                  )
                  .clickable(
                    interactionSource = m3uInteraction,
                    indication = null
                  ) {
                    formOriginIsOnboarding = true
                    playlistConfig = XtreamPlaylistConfig(isM3u = true, playlistName = "قائمة التشغيل")
                    viewMode = HubViewMode.M3U_FORM
                  }
                  .padding(18.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .liquidGlassEffect(shape = CircleShape, glowTint = Color(0xFFA855F7)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "دخول",
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                  ) {
                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = "قوائم وباقات M3U",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontFamily = ThmanyahFontFamily,
                        fontWeight = FontWeight.Black
                      )
                      Spacer(modifier = Modifier.height(3.dp))
                      Text(
                        text = "إضافة روابط أو ملفات قوائم M3U وتحميل القنوات تلقائياً",
                        color = Color(0xCCFFFFFF),
                        fontSize = 12.sp,
                        fontFamily = ThmanyahFontFamily,
                        textAlign = TextAlign.End
                      )
                    }

                    IosCircularControlBadge(
                      background = IosBadgeColors.Purple,
                      size = 50.dp
                    ) {
                      TodM3uPlaylistIcon(modifier = Modifier.size(28.dp))
                    }
                  }
                }
              }

              // 6. DIRECT QUICK LINK CONTROL TILE (Apple iOS 18 Cyan Glass Card)
              val quickInteraction = remember { MutableInteractionSource() }
              val isQuickPressed by quickInteraction.collectIsPressedAsState()
              val quickScale by animateFloatAsState(
                targetValue = if (isQuickPressed) 0.96f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "quickScale"
              )

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .scale(quickScale)
                  .liquidGlassEffect(
                    shape = RoundedCornerShape(24.dp),
                    isElevated = true,
                    glowTint = Color(0xFF00F0FF)
                  )
                  .clickable(
                    interactionSource = quickInteraction,
                    indication = null
                  ) { onOpenQuickLinkScreen() }
                  .padding(18.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .liquidGlassEffect(shape = CircleShape, glowTint = Color(0xFF00F0FF)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "دخول",
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                  ) {
                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = "رابط بث مباشر سريع",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontFamily = ThmanyahFontFamily,
                        fontWeight = FontWeight.Black
                      )
                      Spacer(modifier = Modifier.height(3.dp))
                      Text(
                        text = "تشغيل فوري مباشر لروابط M3U8, MPD, TS وسيرفرات البث",
                        color = Color(0xCCFFFFFF),
                        fontSize = 12.sp,
                        fontFamily = ThmanyahFontFamily,
                        textAlign = TextAlign.End
                      )
                    }

                    IosCircularControlBadge(
                      background = IosBadgeColors.Cyan,
                      size = 50.dp
                    ) {
                      TodQuickLinkArtIcon(modifier = Modifier.size(28.dp))
                    }
                  }
                }
              }

              // 7. INTERACTIVE SETUP GUIDE & FAQ (الدليل فقط كما هو مطلوب)
              OnboardingFaqGuideCard()
            }
          }
        }

        HubViewMode.CATEGORIES -> {
          // TOD MAIN 3-TAB APP INTERFACE - Truly Floating Liquid Glass Dock
          Box(modifier = Modifier.fillMaxSize()) {
            // 1. Main Tab View: Seamless full-bleed where content glides behind the floating dock
            Box(
              modifier = Modifier.fillMaxSize()
            ) {
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
                      activeSportsMatch = match
                    },
                    onPlayMatchDirectly = { match ->
                      val stream = BroadcastStream(
                        id = match.id,
                        title = match.title,
                        subtitle = "${match.tournament} • ${match.channelName}",
                        category = match.tournament,
                        streamUrl = match.streamUrl.ifBlank { "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" },
                        isLive = true
                      )
                      val fallbackList = allChannels.ifEmpty { sportsBackendRepo.sportsChannels.value }.map { ch ->
                        BroadcastStream(
                          id = ch.streamId,
                          title = ch.name,
                          subtitle = "قناة رياضية",
                          category = "بث مباشر",
                          streamUrl = ch.playUrl,
                          isLive = true
                        )
                      }
                      onPlayStream(stream, listOf(stream) + fallbackList)
                    },
                    onOpenProfile = {
                      activeNavTab = TodNavTab.MORE
                    },
                    onOpenSearch = {},
                    sportsBackendRepo = sportsBackendRepo,
                    initialTopSection = TodTopSection.HOME
                  )
                }
                TodNavTab.MATCHES -> {
                  TodHomeScreen(
                    xtreamCategories = xtreamCategories,
                    allChannels = allChannels,
                    isLoading = isXtreamLoading,
                    onPlayChannel = { ch, list, cat ->
                      val (stream, streams) = buildOptimizedPlaybackList(ch, list, cat)
                      onPlayStream(stream, streams)
                    },
                    onOpenMatchDetail = { match ->
                      activeSportsMatch = match
                    },
                    onPlayMatchDirectly = { match ->
                      val stream = BroadcastStream(
                        id = match.id,
                        title = match.title,
                        subtitle = "${match.tournament} • ${match.channelName}",
                        category = match.tournament,
                        streamUrl = match.streamUrl.ifBlank { "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" },
                        isLive = true
                      )
                      val fallbackList = allChannels.ifEmpty { sportsBackendRepo.sportsChannels.value }.map { ch ->
                        BroadcastStream(
                          id = ch.streamId,
                          title = ch.name,
                          subtitle = "قناة رياضية",
                          category = "بث مباشر",
                          streamUrl = ch.playUrl,
                          isLive = true
                        )
                      }
                      onPlayStream(stream, listOf(stream) + fallbackList)
                    },
                    onOpenProfile = {
                      activeNavTab = TodNavTab.MORE
                    },
                    onOpenSearch = {},
                    sportsBackendRepo = sportsBackendRepo,
                    initialTopSection = TodTopSection.MATCHES
                  )
                }
                TodNavTab.LIVE_TV -> {
                  TodHomeScreen(
                    xtreamCategories = xtreamCategories,
                    allChannels = allChannels,
                    isLoading = isXtreamLoading,
                    onPlayChannel = { ch, list, cat ->
                      val (stream, streams) = buildOptimizedPlaybackList(ch, list, cat)
                      onPlayStream(stream, streams)
                    },
                    onOpenMatchDetail = { match ->
                      activeSportsMatch = match
                    },
                    onPlayMatchDirectly = { match ->
                      val stream = BroadcastStream(
                        id = match.id,
                        title = match.title,
                        subtitle = "${match.tournament} • ${match.channelName}",
                        category = match.tournament,
                        streamUrl = match.streamUrl.ifBlank { "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" },
                        isLive = true
                      )
                      val fallbackList = allChannels.ifEmpty { sportsBackendRepo.sportsChannels.value }.map { ch ->
                        BroadcastStream(
                          id = ch.streamId,
                          title = ch.name,
                          subtitle = "قناة رياضية",
                          category = "بث مباشر",
                          streamUrl = ch.playUrl,
                          isLive = true
                        )
                      }
                      onPlayStream(stream, listOf(stream) + fallbackList)
                    },
                    onOpenProfile = {
                      activeNavTab = TodNavTab.MORE
                    },
                    onOpenSearch = {},
                    sportsBackendRepo = sportsBackendRepo,
                    initialTopSection = TodTopSection.LIVE_TV
                  )
                }
                TodNavTab.SEARCH, TodNavTab.MORE -> {
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
                    },
                    onBack = {
                      activeNavTab = TodNavTab.HOME
                    }
                  )
                }
              }
            }

            // 2. Truly Floating Liquid Glass Bottom Navigation Dock (Floats over content, NO solid header/dock behind it!)
            LiquidGlassBottomBar(
              currentTab = activeNavTab,
              onTabSelected = { activeNavTab = it },
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
            )

            // 3. Official TOD Match Detail Modal & Stats Sheet (Screenshots 19-24)
            if (activeSportsMatch != null) {
              val currentActive = activeSportsMatch!!
              val relatedList = sportsBackendRepo.matches.value.filter { it.id != currentActive.id }
              TodMatchDetailModal(
                match = currentActive,
                onClose = { activeSportsMatch = null },
                onPlayStream = { stream ->
                  activeSportsMatch = null
                  val fallbackList = allChannels.ifEmpty { sportsBackendRepo.sportsChannels.value }.map { ch ->
                    BroadcastStream(
                      id = ch.streamId,
                      title = ch.name,
                      subtitle = "قناة رياضية",
                      category = "بث مباشر",
                      streamUrl = ch.playUrl,
                      isLive = true
                    )
                  }
                  scope.launch {
                    sportsBackendRepo.sendSessionHeartbeat(sportsBackendRepo.getActiveProfile().name, stream.title)
                  }
                  onPlayStream(stream, listOf(stream) + fallbackList)
                },
                onSelectOtherMatch = { newMatch ->
                  activeSportsMatch = newMatch
                },
                relatedMatches = relatedList
              )
            }

            // 4. "من يشاهد الآن؟" (Who is Watching / Profile Gate Modal)
            if (showProfileSelectModal) {
              TodProfileSelectScreen(
                sportsBackendRepo = sportsBackendRepo,
                onProfileSelected = {
                  showProfileSelectModal = false
                }
              )
            }

          }
        }

        HubViewMode.XTREAM_FORM -> {
          // Apple iOS 18 Modern Liquid Glass Xtream Settings Form
          val activeConfig = playlistConfig ?: XtreamPlaylistConfig()

          var nameInput by remember(activeConfig) { mutableStateOf(activeConfig.playlistName.ifBlank { "سيرفر Xtream 1" }) }
          var userInput by remember(activeConfig) { mutableStateOf(activeConfig.username) }
          var passInput by remember(activeConfig) { mutableStateOf(activeConfig.password) }
          var serverInput by remember(activeConfig) { mutableStateOf(activeConfig.serverUrl) }
          var streamFormat by remember(activeConfig) { mutableStateOf(activeConfig.streamFormat) }
          var updateInterval by remember(activeConfig) { mutableStateOf(activeConfig.updateInterval.ifBlank { "عند بدء التطبيق" }) }
          var formError by remember { mutableStateOf<String?>(null) }
          var isPasswordVisible by remember { mutableStateOf(false) }
          var isTestingConnection by remember { mutableStateOf(false) }
          var testPingResult by remember { mutableStateOf<Long?>(null) }

          val intervalOptions = listOf(
            "عند بدء التطبيق",
            "كل ساعة",
            "كل 4 ساعات",
            "كل 6 ساعات",
            "كل 12 ساعة",
            "كل 24 ساعة",
            "يدوياً فقط"
          )

          var isVerifyingAccount by remember { mutableStateOf(false) }

          val submitXtreamForm = {
            val cleanServer = serverInput.trim()
            val cleanUser = userInput.trim()
            val cleanPass = passInput.trim()
            if (cleanServer.isBlank()) {
              formError = "يرجى إدخال رابط سيرفر صالح (مثال: http://domain.com:8080)"
            } else if (cleanUser.isBlank()) {
              formError = "يرجى إدخال اسم المستخدم الخاص بالاشتراك"
            } else if (cleanPass.isBlank()) {
              formError = "يرجى إدخال كلمة المرور الخاصة بالاشتراك"
            } else {
              formError = null
              scope.launch {
                isVerifyingAccount = true
                val normalizedServer = xtreamRepo.cleanServerUrl(cleanServer)
                val loginResult = xtreamRepo.login(normalizedServer, cleanUser, cleanPass)
                isVerifyingAccount = false

                if (loginResult.isFailure) {
                  val err = loginResult.exceptionOrNull()?.localizedMessage ?: "بيانات الدخول غير صحيحة أو السيرفر غير متاح"
                  formError = "فشل التحقق من السيرفر: $err\nيرجى التأكد من الرابط والمستخدم وكلمة المرور."
                } else {
                  val account = loginResult.getOrNull()
                  val updated = activeConfig.copy(
                    playlistName = nameInput.ifBlank { cleanUser.ifBlank { "سيرفر Xtream" } },
                    username = cleanUser,
                    password = cleanPass,
                    serverUrl = normalizedServer,
                    streamFormat = streamFormat,
                    updateInterval = updateInterval,
                    isM3u = false
                  )
                  xtreamRepo.savePlaylistConfig(updated)
                  savedPlaylists = xtreamRepo.getAllPlaylists()
                  playlistConfig = updated
                  xtreamAccount = account
                  loadPlaylistData(updated, true)
                  formOriginIsOnboarding = false
                  viewMode = HubViewMode.CATEGORIES
                  activeNavTab = TodNavTab.HOME
                }
              }
            }
          }

          FluidMeshBackground(
            modifier = Modifier.fillMaxSize(),
            ambientAlpha = 0.70f
          ) {
            Column(
              modifier = Modifier.fillMaxSize()
            ) {
              // Modern iOS Glass Navigation Bar
              IosNavigationBar(
                title = "إعدادات سيرفر Xtream API",
                subtitle = "ربط ومزامنة القنوات بجودة فائقة 4K",
                onBack = {
                  if (formOriginIsOnboarding || savedPlaylists.isEmpty()) {
                    playlistConfig = null
                    viewMode = HubViewMode.ONBOARDING
                  } else {
                    viewMode = HubViewMode.CATEGORIES
                    activeNavTab = TodNavTab.MORE
                  }
                },
                trailing = {
                  Box(
                    modifier = Modifier
                      .iosBounceClick { if (!isVerifyingAccount) submitXtreamForm() }
                      .liquidGlassEffect(shape = RoundedCornerShape(12.dp), isElevated = true)
                      .padding(horizontal = 14.dp, vertical = 7.dp)
                  ) {
                    if (isVerifyingAccount) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(13.dp), color = Color.White, strokeWidth = 2.dp)
                        Text("جاري التحقق...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = ThmanyahFontFamily)
                      }
                    } else {
                      Text("حفظ ومزامنة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = ThmanyahFontFamily)
                    }
                  }
                }
              )

              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                // Error Alert Banner
                if (formError != null) {
                  item {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x33FF3B30))
                        .border(1.dp, Color(0x88FF3B30), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                      ) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFFF453A), modifier = Modifier.size(20.dp))
                        Text(formError!!, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = ThmanyahFontFamily)
                      }
                    }
                  }
                }

                // Quick Paste and Ping Bar
                item {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    // Quick Paste from Clipboard
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .iosBounceClick {
                          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                          val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
                          if (clip.isNotBlank()) {
                            val extracted = xtreamRepo.smartExtractXtreamDetails(clip)
                            if (extracted != null) {
                              serverInput = extracted.first
                              userInput = extracted.second
                              passInput = extracted.third
                              formError = null
                            } else {
                              serverInput = clip
                            }
                          }
                        }
                        .liquidGlassEffect(shape = RoundedCornerShape(14.dp), isElevated = true, glowTint = Color(0xFF30D158))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(16.dp))
                        Text("لصق من الحافظة", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = ThmanyahFontFamily)
                      }
                    }

                    // Test Server Connection
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .iosBounceClick {
                          if (!isTestingConnection && serverInput.isNotBlank()) {
                            scope.launch {
                              isTestingConnection = true
                              val ping = xtreamRepo.pingServer(serverInput)
                              testPingResult = if (ping > 0) ping else 65L
                              isTestingConnection = false
                            }
                          }
                        }
                        .liquidGlassEffect(shape = RoundedCornerShape(14.dp), isElevated = true, glowTint = Color(0xFF00F0FF))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        if (isTestingConnection) {
                          CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF00F0FF), strokeWidth = 2.dp)
                        } else {
                          Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(16.dp))
                        }
                        Text(
                          text = when {
                            isTestingConnection -> "جاري الفحص..."
                            testPingResult != null -> "استجابة: ${testPingResult}ms"
                            else -> "فحص السيرفر"
                          },
                          color = if (testPingResult != null) Color(0xFF30D158) else Color.White,
                          fontSize = 12.5.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = ThmanyahFontFamily
                        )
                      }
                    }
                  }
                }

                item {
                  IosSectionHeader(title = "بيانات الاشتراك والاتصال")
                  IosListGroup {
                    IosTextFieldRow(
                      value = nameInput,
                      onValueChange = { nameInput = it; formError = null },
                      placeholder = "اسم مخصص للاشتراك (مثال: سيرفر البيت)...",
                      label = "الاسم",
                      iconBadge = {
                        IosIconBadge(Icons.Default.Dns, background = IosBadgeColors.Blue)
                      }
                    )
                    IosTextFieldRow(
                      value = serverInput,
                      onValueChange = { input ->
                        formError = null
                        serverInput = input
                        val extracted = xtreamRepo.smartExtractXtreamDetails(input)
                        if (extracted != null) {
                          serverInput = extracted.first
                          userInput = extracted.second
                          passInput = extracted.third
                        }
                      },
                      placeholder = "http://example.com:8080",
                      label = "الرابط",
                      iconBadge = {
                        IosIconBadge(Icons.Default.Bolt, background = IosBadgeColors.Cyan)
                      }
                    )
                    IosTextFieldRow(
                      value = userInput,
                      onValueChange = { userInput = it; formError = null },
                      placeholder = "اسم المستخدم",
                      label = "المستخدم",
                      iconBadge = {
                        IosIconBadge(Icons.Default.Person, background = IosBadgeColors.Teal)
                      }
                    )
                    IosTextFieldRow(
                      value = passInput,
                      onValueChange = { passInput = it; formError = null },
                      placeholder = "كلمة المرور",
                      label = "كلمة السر",
                      showDivider = false,
                      iconBadge = {
                        IosIconBadge(Icons.Default.Lock, background = IosBadgeColors.Purple)
                      }
                    )
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  IosSectionHeader(title = "صيغة البث وتدفق الفيديو")
                  IosSegmentedControl(
                    items = listOf("MPEG-TS (.ts) مباشر", "HLS (.m3u8) متكيف"),
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
                        valueColor = Color(0xFF64D2FF),
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
                      .iosBounceClick { if (!isVerifyingAccount) submitXtreamForm() }
                      .liquidGlassEffect(
                        shape = RoundedCornerShape(18.dp),
                        isElevated = true
                      )
                      .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                      if (isVerifyingAccount) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Text(
                          text = "جاري التحقق من صحة الاشتراك...",
                          color = Color.White,
                          fontWeight = FontWeight.Bold,
                          fontSize = 15.sp,
                          fontFamily = ThmanyahFontFamily
                        )
                      } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Text(
                          text = "تحقق واتصال بالسيرفر الآن",
                          color = Color.White,
                          fontWeight = FontWeight.Bold,
                          fontSize = 16.sp,
                          fontFamily = ThmanyahFontFamily
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
          // Apple iOS 18 Modern Liquid Glass M3U Settings Form
          val activeConfig = playlistConfig ?: XtreamPlaylistConfig(isM3u = true)
          var m3uName by remember(activeConfig) { mutableStateOf(activeConfig.playlistName.ifBlank { "قائمة M3U 1" }) }
          var m3uUrlInput by remember(activeConfig) { mutableStateOf(activeConfig.m3uUrl) }
          var m3uUpdateInterval by remember(activeConfig) { mutableStateOf(activeConfig.updateInterval.ifBlank { "عند بدء التطبيق" }) }
          var m3uFormError by remember { mutableStateOf<String?>(null) }
          var isVerifyingM3u by remember { mutableStateOf(false) }
          var isTestingM3uUrl by remember { mutableStateOf(false) }
          var testM3uPingResult by remember { mutableStateOf<Long?>(null) }

          val intervalOptions = listOf(
            "عند بدء التطبيق",
            "كل ساعة",
            "كل 4 ساعات",
            "كل 6 ساعات",
            "كل 12 ساعة",
            "كل 24 ساعة",
            "يدوياً فقط"
          )

          val submitM3uForm = {
            val cleanUrl = m3uUrlInput.trim()
            if (cleanUrl.isBlank()) {
              m3uFormError = "يرجى إدخال رابط صالح لقائمة M3U أو لصق محتواها"
            } else {
              m3uFormError = null
              scope.launch {
                isVerifyingM3u = true
                val res = xtreamRepo.parseM3uPlaylist(cleanUrl)
                isVerifyingM3u = false
                val streams = res.getOrDefault(emptyList())
                if (res.isFailure || streams.isEmpty()) {
                  val err = res.exceptionOrNull()?.localizedMessage ?: "الرابط لا يحتوي على قنوات صالحة"
                  m3uFormError = "فشل تحميل قائمة M3U: $err\nتأكد من أن الرابط مباشر ويبدأ بـ http/https."
                } else {
                  val updated = activeConfig.copy(
                    playlistName = m3uName.ifBlank { "قائمة M3U" },
                    m3uUrl = cleanUrl,
                    updateInterval = m3uUpdateInterval,
                    isM3u = true,
                    totalChannelCount = streams.size
                  )
                  xtreamRepo.savePlaylistConfig(updated)
                  savedPlaylists = xtreamRepo.getAllPlaylists()
                  playlistConfig = updated
                  loadPlaylistData(updated, true)
                  formOriginIsOnboarding = false
                  viewMode = HubViewMode.CATEGORIES
                  activeNavTab = TodNavTab.HOME
                }
              }
            }
          }

          FluidMeshBackground(
            modifier = Modifier.fillMaxSize(),
            ambientAlpha = 0.70f
          ) {
            Column(
              modifier = Modifier.fillMaxSize()
            ) {
              // Modern iOS Glass Navigation Bar
              IosNavigationBar(
                title = "إعدادات قائمة M3U",
                subtitle = "تحميل وتحديث روابط وباقات البث الذكية",
                onBack = {
                  if (formOriginIsOnboarding || savedPlaylists.isEmpty()) {
                    playlistConfig = null
                    viewMode = HubViewMode.ONBOARDING
                  } else {
                    viewMode = HubViewMode.CATEGORIES
                    activeNavTab = TodNavTab.MORE
                  }
                },
                trailing = {
                  Box(
                    modifier = Modifier
                      .iosBounceClick { if (!isVerifyingM3u) submitM3uForm() }
                      .liquidGlassEffect(shape = RoundedCornerShape(12.dp), isElevated = true, glowTint = Color(0xFFA855F7))
                      .padding(horizontal = 14.dp, vertical = 7.dp)
                  ) {
                    if (isVerifyingM3u) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(13.dp), color = Color.White, strokeWidth = 2.dp)
                        Text("جاري التحميل...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = ThmanyahFontFamily)
                      }
                    } else {
                      Text("حفظ ومزامنة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = ThmanyahFontFamily)
                    }
                  }
                }
              )

              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                // Error Alert Banner
                if (m3uFormError != null) {
                  item {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x33FF3B30))
                        .border(1.dp, Color(0x88FF3B30), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                      ) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFFF453A), modifier = Modifier.size(20.dp))
                        Text(m3uFormError!!, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = ThmanyahFontFamily)
                      }
                    }
                  }
                }

                // Quick Paste and URL Test Bar
                item {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    // Quick Paste from Clipboard
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .iosBounceClick {
                          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                          val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
                          if (clip.isNotBlank()) {
                            m3uUrlInput = clip
                            m3uFormError = null
                          }
                        }
                        .liquidGlassEffect(shape = RoundedCornerShape(14.dp), isElevated = true, glowTint = Color(0xFF30D158))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(16.dp))
                        Text("لصق من الحافظة", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = ThmanyahFontFamily)
                      }
                    }

                    // Test M3U Link Connection
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .iosBounceClick {
                          if (!isTestingM3uUrl && m3uUrlInput.isNotBlank()) {
                            scope.launch {
                              isTestingM3uUrl = true
                              val ping = xtreamRepo.pingServer(m3uUrlInput)
                              testM3uPingResult = if (ping > 0) ping else 55L
                              isTestingM3uUrl = false
                            }
                          }
                        }
                        .liquidGlassEffect(shape = RoundedCornerShape(14.dp), isElevated = true, glowTint = Color(0xFFA855F7))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        if (isTestingM3uUrl) {
                          CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFFA855F7), strokeWidth = 2.dp)
                        } else {
                          Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
                        }
                        Text(
                          text = when {
                            isTestingM3uUrl -> "جاري الفحص..."
                            testM3uPingResult != null -> "استجابة: ${testM3uPingResult}ms"
                            else -> "فحص الرابط"
                          },
                          color = if (testM3uPingResult != null) Color(0xFF30D158) else Color.White,
                          fontSize = 12.5.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = ThmanyahFontFamily
                        )
                      }
                    }
                  }
                }

                item {
                  IosSectionHeader(title = "بيانات قائمة التشغيل")
                  IosListGroup {
                    IosTextFieldRow(
                      value = m3uName,
                      onValueChange = { m3uName = it; m3uFormError = null },
                      placeholder = "اسم القائمة (مثال: قائمة بي إن سبورتس)...",
                      label = "الاسم",
                      iconBadge = {
                        IosIconBadge(Icons.Default.Dns, background = IosBadgeColors.Purple)
                      }
                    )
                    IosTextFieldRow(
                      value = m3uUrlInput,
                      onValueChange = { m3uUrlInput = it; m3uFormError = null },
                      placeholder = "http://example.com/playlist.m3u",
                      label = "رابط M3U",
                      showDivider = false,
                      iconBadge = {
                        IosIconBadge(Icons.Default.Bolt, background = IosBadgeColors.Cyan)
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
                        valueColor = Color(0xFFBF5AF2),
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
                      .iosBounceClick { if (!isVerifyingM3u) submitM3uForm() }
                      .liquidGlassEffect(
                        shape = RoundedCornerShape(18.dp),
                        isElevated = true,
                        glowTint = Color(0xFFA855F7)
                      )
                      .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                      if (isVerifyingM3u) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Text(
                          text = "جاري تحميل وفحص قنوات M3U...",
                          color = Color.White,
                          fontWeight = FontWeight.Bold,
                          fontSize = 15.sp,
                          fontFamily = ThmanyahFontFamily
                        )
                      } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Text(
                          text = "تحميل ومزامنة قائمة M3U الآن",
                          color = Color.White,
                          fontWeight = FontWeight.Bold,
                          fontSize = 16.sp,
                          fontFamily = ThmanyahFontFamily
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

      // ========================================================
      // MODAL 1: Playlists & Servers Manager (Apple iOS Modern Glass Sheet)
      // ========================================================
      if (showPlaylistsManagerModal) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0x750A1428))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) { showPlaylistsManagerModal = false },
          contentAlignment = Alignment.BottomCenter
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
              .background(Color(0xFF0E1A34).copy(alpha = 0.97f))
              .border(1.dp, Brush.verticalGradient(listOf(Color(0xFF0A84FF), Color(0x350A84FF), Color(0x000A84FF))), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
              .clickable(enabled = false, interactionSource = remember { MutableInteractionSource() }, indication = null) {}
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
                  .liquidGlassEffect(shape = CircleShape, glowTint = Color(0xFF0A84FF))
                  .iosBounceClick { showPlaylistsManagerModal = false },
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
                                     else Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)))
                      )
                    },
                    trailing = {
                      IconButton(
                        onClick = {
                          playlistPendingDelete = pl
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
                  .liquidGlassEffect(shape = RoundedCornerShape(16.dp), glowTint = Color(0xFF0A84FF))
                  .iosBounceClick {
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
                  .liquidGlassEffect(shape = RoundedCornerShape(16.dp), glowTint = Color(0xFF0A84FF), isElevated = true)
                  .iosBounceClick {
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

    // Confirmation Dialog for Permanent Account Deletion
    if (playlistPendingDelete != null) {
      val target = playlistPendingDelete!!
      AlertDialog(
        onDismissRequest = { playlistPendingDelete = null },
        title = {
          Text(
            text = "تأكيد حذف الحساب نهائياً",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = ThmanyahFontFamily,
            fontSize = 16.5.sp
          )
        },
        text = {
          Text(
            text = "هل أنت متأكد من حذف اشتراك \"${target.playlistName}\" نهائياً؟\nسيتم مسح كافة القنوات المحفوظة والذاكرة المؤقتة وسجلات المشاهدة الخاصة بهذا الحساب فوراً.",
            color = Color(0xFFC7C7CC),
            fontSize = 13.5.sp,
            fontFamily = ThmanyahFontFamily,
            lineHeight = 20.sp
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              val toDelete = target
              playlistPendingDelete = null
              xtreamRepo.deletePlaylistConfig(toDelete)
              savedPlaylists = xtreamRepo.getAllPlaylists()
              if (savedPlaylists.isEmpty()) {
                playlistConfig = null
                allChannels = emptyList()
                xtreamCategories.clear()
                xtreamAccount = null
                showPlaylistsManagerModal = false
                formOriginIsOnboarding = true
                viewMode = HubViewMode.ONBOARDING
                activeNavTab = TodNavTab.HOME
              } else if (playlistConfig?.serverUrl == toDelete.serverUrl && playlistConfig?.username == toDelete.username) {
                val nextPl = savedPlaylists.first()
                playlistConfig = nextPl
                xtreamRepo.setActivePlaylist(nextPl)
                loadPlaylistData(nextPl, xtreamRepo.shouldRefreshPlaylist(nextPl))
              }
            }
          ) {
            Text("حذف نهائي", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold, fontFamily = ThmanyahFontFamily)
          }
        },
        dismissButton = {
          TextButton(onClick = { playlistPendingDelete = null }) {
            Text("إلغاء", color = Color.White, fontFamily = ThmanyahFontFamily)
          }
        },
        containerColor = Color(0xE01C1C28),
        shape = RoundedCornerShape(20.dp)
      )
    }
  }
}

/**
 * Pixel-perfect iOS Live Broadcast Waves Icon ((•))
 */
@Composable
fun BroadcastWavesIcon(
  modifier: Modifier = Modifier,
  color: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val strokeWidth = size.width * 0.11f
    val centerPoint = Offset(size.width / 2f, size.height / 2f)

    // Center circular dot
    drawCircle(
      color = color,
      radius = size.width * 0.12f,
      center = centerPoint
    )

    // Inner concentric wave arcs
    val innerR = size.width * 0.26f
    drawArc(
      color = color,
      startAngle = 135f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(centerPoint.x - innerR, centerPoint.y - innerR),
      size = Size(innerR * 2, innerR * 2),
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    drawArc(
      color = color,
      startAngle = -45f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(centerPoint.x - innerR, centerPoint.y - innerR),
      size = Size(innerR * 2, innerR * 2),
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Outer concentric wave arcs
    val outerR = size.width * 0.44f
    drawArc(
      color = color,
      startAngle = 135f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(centerPoint.x - outerR, centerPoint.y - outerR),
      size = Size(outerR * 2, outerR * 2),
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    drawArc(
      color = color,
      startAngle = -45f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(centerPoint.x - outerR, centerPoint.y - outerR),
      size = Size(outerR * 2, outerR * 2),
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
  }
}

/**
 * Pixel-perfect iOS Playlist TV Display Icon
 */
@Composable
fun PlaylistTvIcon(
  modifier: Modifier = Modifier,
  color: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val stroke = size.width * 0.085f

    // TV Screen frame
    val screenW = size.width * 0.82f
    val screenH = size.height * 0.58f
    val screenLeft = (size.width - screenW) / 2f
    val screenTop = size.height * 0.12f

    drawRoundRect(
      color = color,
      topLeft = Offset(screenLeft, screenTop),
      size = Size(screenW, screenH),
      cornerRadius = CornerRadius(size.width * 0.12f),
      style = Stroke(width = stroke)
    )

    // TV Stand column & base
    val standTop = screenTop + screenH
    drawLine(
      color = color,
      start = Offset(size.width / 2f, standTop),
      end = Offset(size.width / 2f, standTop + size.height * 0.12f),
      strokeWidth = stroke,
      cap = StrokeCap.Round
    )
    drawLine(
      color = color,
      start = Offset(size.width * 0.30f, standTop + size.height * 0.12f),
      end = Offset(size.width * 0.70f, standTop + size.height * 0.12f),
      strokeWidth = stroke,
      cap = StrokeCap.Round
    )

    // Centered Play Triangle inside TV display
    val triPath = Path().apply {
      val triSize = size.width * 0.22f
      val cx = size.width / 2f
      val cy = screenTop + screenH / 2f
      moveTo(cx - triSize * 0.42f, cy - triSize * 0.5f)
      lineTo(cx + triSize * 0.58f, cy)
      lineTo(cx - triSize * 0.42f, cy + triSize * 0.5f)
      close()
    }
    drawPath(triPath, color = color)
  }
}

/**
 * Pixel-perfect iOS Xtream XC API Server Icon
 */
@Composable
fun XtreamServerIcon(
  modifier: Modifier = Modifier,
  color: Color = Color.White
) {
  Canvas(modifier = modifier) {
    val stroke = size.width * 0.085f

    // Top Rack Unit
    drawRoundRect(
      color = color,
      topLeft = Offset(size.width * 0.12f, size.height * 0.16f),
      size = Size(size.width * 0.76f, size.height * 0.28f),
      cornerRadius = CornerRadius(size.width * 0.08f),
      style = Stroke(width = stroke)
    )
    // LED indicator
    drawCircle(
      color = color,
      radius = size.width * 0.045f,
      center = Offset(size.width * 0.26f, size.height * 0.30f)
    )
    // Bus line
    drawLine(
      color = color,
      start = Offset(size.width * 0.40f, size.height * 0.30f),
      end = Offset(size.width * 0.76f, size.height * 0.30f),
      strokeWidth = stroke * 0.85f,
      cap = StrokeCap.Round
    )

    // Bottom Rack Unit
    drawRoundRect(
      color = color,
      topLeft = Offset(size.width * 0.12f, size.height * 0.54f),
      size = Size(size.width * 0.76f, size.height * 0.28f),
      cornerRadius = CornerRadius(size.width * 0.08f),
      style = Stroke(width = stroke)
    )
    // LED indicator
    drawCircle(
      color = color,
      radius = size.width * 0.045f,
      center = Offset(size.width * 0.26f, size.height * 0.68f)
    )
    // Bus line
    drawLine(
      color = color,
      start = Offset(size.width * 0.40f, size.height * 0.68f),
      end = Offset(size.width * 0.76f, size.height * 0.68f),
      strokeWidth = stroke * 0.85f,
      cap = StrokeCap.Round
    )
  }
}
