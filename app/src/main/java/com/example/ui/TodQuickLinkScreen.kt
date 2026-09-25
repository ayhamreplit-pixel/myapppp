package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStream
import com.example.model.StreamFormat
import com.example.model.StreamUrlParser
import com.example.player.XtreamRepository
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosLabelTertiary
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemOrange
import com.example.ui.theme.IosSystemPurple
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosSystemTeal
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodAmberYellow
import com.example.ui.theme.TodGradients

/**
 * Modern iOS-Style Quick Link Screen (تشغيل رابط سريع).
 * Implements iOS 18 translucent glass navigation, inset grouped cards,
 * Cupertino text fields with paste shortcuts, playback history, format selection,
 * advanced DRM/headers parameters, and spring physics action buttons.
 */
@Composable
fun TodQuickLinkScreen(
  onBack: () -> Unit,
  onPlayStream: (BroadcastStream, List<BroadcastStream>) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val xtreamRepo = remember { XtreamRepository(context) }
  val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

  BackHandler { onBack() }

  var titleInput by remember { mutableStateOf("") }
  var urlInput by remember { mutableStateOf("") }
  var showAdvanced by remember { mutableStateOf(false) }

  // Advanced & DRM parameters
  var userAgentInput by remember { mutableStateOf("") }
  var originInput by remember { mutableStateOf("") }
  var refererInput by remember { mutableStateOf("") }
  var cookieInput by remember { mutableStateOf("") }
  var drmSchemeInput by remember { mutableStateOf("") }
  var drmKeyInput by remember { mutableStateOf("") }

  // Format selection
  var selectedFormatIndex by remember { mutableIntStateOf(0) }
  val streamFormats = listOf("تلقائي", "HLS / M3U8", "MPEG-TS", "DASH / MPD", "MP4 Direct")

  // Stream history list
  var historyList by remember { mutableStateOf(xtreamRepo.getCustomUrlHistory()) }

  // Quick testing stream presets
  val quickSamples = listOf(
    Pair("Mux BigBuck (HLS 1080p)", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
    Pair("Akamai Live 1080p", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
    Pair("Apple BipBop (HLS Multi)", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"),
    Pair("Sintel Clear (DASH)", "https://bitdash-a.akamaihd.net/content/sintel/sintel.mpd")
  )

  fun parseAndFill(input: String) {
    urlInput = input
    if (input.contains("|") || input.contains("#") || input.contains("drm", ignoreCase = true) || input.contains("user-agent", ignoreCase = true)) {
      val parsed = StreamUrlParser.parse(input)
      if (!parsed.userAgent.isNullOrBlank()) userAgentInput = parsed.userAgent
      if (!parsed.origin.isNullOrBlank()) originInput = parsed.origin
      if (!parsed.referer.isNullOrBlank()) refererInput = parsed.referer
      if (!parsed.cookie.isNullOrBlank()) cookieInput = parsed.cookie
      if (!parsed.drmScheme.isNullOrBlank()) drmSchemeInput = parsed.drmScheme
      if (!parsed.drmLicense.isNullOrBlank()) drmKeyInput = parsed.drmLicense
    }
  }

  fun launchStreamWithData(urlToPlay: String, titleToPlay: String) {
    if (urlToPlay.isBlank()) return
    val raw = urlToPlay.trim()
    val parsed = StreamUrlParser.parse(raw)

    val finalCleanUrl = parsed.cleanUrl.ifBlank { raw }
    val finalUa = userAgentInput.trim().ifBlank { parsed.userAgent }
    val finalOrigin = originInput.trim().ifBlank { parsed.origin }
    val finalReferer = refererInput.trim().ifBlank { parsed.referer }
    val finalCookie = cookieInput.trim().ifBlank { parsed.cookie }
    val finalDrmScheme = drmSchemeInput.trim().ifBlank { parsed.drmScheme }
    val finalDrmKey = drmKeyInput.trim().ifBlank { parsed.drmLicense }

    val resolvedFormat = when (selectedFormatIndex) {
      1 -> StreamFormat.HLS
      2 -> StreamFormat.HLS
      3 -> StreamFormat.DASH
      4 -> StreamFormat.PROGRESSIVE
      else -> parsed.format
    }

    val stream = BroadcastStream(
      id = "custom_${System.currentTimeMillis()}",
      title = titleToPlay.ifBlank { "بث مباشر سريع" },
      subtitle = if (!finalDrmScheme.isNullOrBlank()) "بث محمي ($finalDrmScheme)" else "رابط خارجي مباشر",
      category = "Direct Stream",
      streamUrl = finalCleanUrl,
      format = resolvedFormat,
      isLive = true,
      origin = finalOrigin,
      referer = finalReferer,
      cookie = finalCookie,
      userAgent = finalUa,
      drmScheme = finalDrmScheme,
      drmKey = finalDrmKey,
      extraHeaders = parsed.extraHeaders
    )

    xtreamRepo.addCustomUrlToHistory(titleToPlay.ifBlank { "بث مباشر" }, finalCleanUrl)
    historyList = xtreamRepo.getCustomUrlHistory()
    onPlayStream(stream, listOf(stream))
  }

    FluidMeshBackground(
      modifier = modifier.fillMaxSize(),
      ambientAlpha = 0.70f
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .navigationBarsPadding()
      ) {
        // ==========================================
        // 1. Apple iOS Seamless Navigation Bar (Continuous Edge-to-Edge Liquid Glass)
        // ==========================================
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // iOS Back Button with Spring Press
          Box(
            modifier = Modifier
              .iosBounceClick(scaleDown = 0.88f, onClick = onBack)
              .size(38.dp)
              .clip(CircleShape)
              .background(Color(0x28FFFFFF)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
              contentDescription = "رجوع",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }

          // Center Title & Subtitle Badge
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "تشغيل رابط سريع",
              color = Color.White,
              fontSize = 17.5.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(IosSystemGreen)
              )
              Text(
                text = "M3U8 • MPD • TS • MP4",
                color = Color(0xFF8E8E93),
                fontSize = 11.5.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = FontWeight.Medium
              )
            }
          }

          // Trailing Action: Clear Button or Paste Button
          if (urlInput.isNotBlank() || titleInput.isNotBlank()) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0x28FFFFFF))
                .border(0.75.dp, Color(0x35FFFFFF), CircleShape)
                .clickable {
                  urlInput = ""
                  titleInput = ""
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "مسح",
                tint = Color(0xFFFF453A),
                modifier = Modifier.size(18.dp)
              )
            }
          } else {
            // Paste Shortcut Button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x280A84FF))
                .border(0.75.dp, Color(0x550A84FF), RoundedCornerShape(12.dp))
                .clickable {
                  val clip = clipboardManager.getText()?.text
                  if (!clip.isNullOrBlank()) {
                    parseAndFill(clip)
                  }
                }
                .padding(horizontal = 10.dp, vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = null,
                  tint = Color(0xFF64D2FF),
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "لصق",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // ==========================================
      // 2. Scrollable Body (iOS Inset Grouped Architecture)
      // ==========================================
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Hero Card (Large Title Header in Apple TV / iOS style)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
              Brush.verticalGradient(
                listOf(Color(0x350A84FF), Color(0x1814141E))
              )
            )
            .border(
              1.dp,
              Brush.verticalGradient(listOf(Color(0x550A84FF), Color(0x15FFFFFF))),
              RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(IosSystemGreen)
                )
                Text(
                  text = "المشغل المباشر الفائق",
                  color = IosSystemBlue,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "تشغيل أي رابط أو بث مباشر",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "يدعم كافة بروتوكولات الفيديو الحديثة، وفك تشفير المفاتيح ومعاملات الرأس تلقائياً.",
                color = Color(0xFFA0A0AB),
                fontSize = 12.5.sp,
                lineHeight = 17.sp
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bolt Emblem Badge
            Box(
              modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                  Brush.linearGradient(listOf(IosSystemBlue, Color(0xFF0055D4)))
                )
                .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(16.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }
          }
        }

        // ==========================================
        // SECTION 1: Stream Details Grouped Card
        // ==========================================
        IosSectionHeader(title = "بيانات ورابط البث")
        IosListGroup {
          // Row 1: Optional Stream Title
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            IosIconBadge(
              icon = Icons.Default.Title,
              background = IosBadgeColors.Indigo,
              size = 32.dp,
              iconSize = 18.dp
            )

            Box(modifier = Modifier.weight(1f)) {
              if (titleInput.isEmpty()) {
                Text(
                  text = "اسم البث أو القناة (اختياري)",
                  color = Color(0xFF636366),
                  fontSize = 14.sp
                )
              }
              BasicTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                textStyle = TextStyle(
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(IosSystemBlue),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
            }

            if (titleInput.isNotEmpty()) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "مسح",
                tint = Color(0xFF8E8E93),
                modifier = Modifier
                  .size(18.dp)
                  .clickable { titleInput = "" }
              )
            }
          }

          HorizontalDivider(
            modifier = Modifier.padding(start = 58.dp),
            thickness = 0.5.dp,
            color = Color(0x1FFFFFFF)
          )

          // Row 2: Stream URL Input Field with inline Paste button & Clear
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                IosIconBadge(
                  icon = Icons.Default.Link,
                  background = IosBadgeColors.Blue,
                  size = 32.dp,
                  iconSize = 18.dp
                )
                Text(
                  text = "رابط البث (Stream URL)",
                  color = Color.White,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }

              // Paste Pill Button
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x280A84FF))
                  .border(0.5.dp, Color(0x440A84FF), RoundedCornerShape(8.dp))
                .clickable {
                  val clip = clipboardManager.getText()?.text
                  if (!clip.isNullOrBlank()) {
                    parseAndFill(clip)
                  }
                }
                .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = null,
                  tint = IosSystemBlue,
                  modifier = Modifier.size(13.dp)
                )
                Text(
                  text = "لصق من الحافظة",
                  color = IosSystemBlue,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Cupertino-styled URL Input Container
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x18FFFFFF))
                .border(
                  0.5.dp,
                  if (urlInput.isNotBlank()) IosSystemBlue.copy(alpha = 0.6f) else Color(0x22FFFFFF),
                  RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
              if (urlInput.isEmpty()) {
                Text(
                  text = "https://example.com/live/stream.m3u8",
                  color = Color(0xFF636366),
                  fontSize = 13.sp,
                  modifier = Modifier.fillMaxWidth()
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                BasicTextField(
                  value = urlInput,
                  onValueChange = { parseAndFill(it) },
                  textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                  ),
                  cursorBrush = SolidColor(IosSystemBlue),
                  minLines = 1,
                  maxLines = 3,
                  modifier = Modifier.weight(1f)
                )

                if (urlInput.isNotEmpty()) {
                  Spacer(modifier = Modifier.width(8.dp))
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "مسح الرابط",
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier
                      .size(18.dp)
                      .clickable { urlInput = "" }
                  )
                }
              }
            }
          }
        }
        IosSectionFooter(text = "يمكنك إدراج روابط بصيغ متعددة مع معاملات الرأس مدمجة مثل |User-Agent=...&Referer=...")

        // ==========================================
        // SECTION 2: Format Selector
        // ==========================================
        IosSectionHeader(title = "صيغة تدفق الفيديو")
        IosSegmentedControl(
          items = listOf("تلقائي", "HLS", "TS", "DASH", "MP4"),
          selectedIndex = selectedFormatIndex,
          onSelect = { selectedFormatIndex = it }
        )

        // ==========================================
        // SECTION 3: Quick Samples Carousel
        // ==========================================
        IosSectionHeader(title = "روابط تجريبية سريعة")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          quickSamples.forEach { (name, sampleUrl) ->
            val isCurrent = urlInput == sampleUrl
            Box(
              modifier = Modifier
                .shadow(if (isCurrent) 6.dp else 2.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF0A84FF).copy(alpha = 0.35f))
                .liquidGlassEffect(
                  shape = RoundedCornerShape(16.dp),
                  glowTint = Color(0xFF0A84FF),
                  isElevated = isCurrent
                )
                .clickable {
                  titleInput = name
                  parseAndFill(sampleUrl)
                }
                .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) IosSystemBlue else Color(0x22FFFFFF)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Column {
                  Text(
                    text = name,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = if (sampleUrl.contains(".mpd")) "DASH Format" else "HLS Master",
                    color = Color(0xFF8E8E93),
                    fontSize = 10.5.sp
                  )
                }
              }
            }
          }
        }

        // ==========================================
        // SECTION 4: Recent Stream History (If available)
        // ==========================================
        if (historyList.isNotEmpty()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IosSectionHeader(
              title = "آخر الروابط المشغلة",
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "مسح الكل",
              color = Color(0xFFFF453A),
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                  xtreamRepo.clearCustomUrlHistory()
                  historyList = emptyList()
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          IosListGroup {
            historyList.take(6).forEachIndexed { index, (itemTitle, itemUrl) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    titleInput = itemTitle
                    parseAndFill(itemUrl)
                  }
                  .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  IosIconBadge(
                    icon = Icons.Default.VideoLibrary,
                    background = IosBadgeColors.Teal,
                    size = 32.dp,
                    iconSize = 17.dp
                  )

                  Column {
                    Text(
                      text = itemTitle,
                      color = Color.White,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = itemUrl,
                      color = Color(0xFF8E8E93),
                      fontSize = 11.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Direct Play Icon
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(Color(0x220A84FF))
                      .clickable { launchStreamWithData(itemUrl, itemTitle) },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.PlayArrow,
                      contentDescription = "تشغيل",
                      tint = IosSystemBlue,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  // Delete from history
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(Color(0x18FFFFFF))
                      .clickable {
                        xtreamRepo.removeCustomUrlFromHistory(itemUrl)
                        historyList = xtreamRepo.getCustomUrlHistory()
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteOutline,
                      contentDescription = "حذف",
                      tint = Color(0xFF8E8E93),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }

              if (index < historyList.take(6).size - 1) {
                HorizontalDivider(
                  modifier = Modifier.padding(start = 58.dp),
                  thickness = 0.5.dp,
                  color = Color(0x1FFFFFFF)
                )
              }
            }
          }
        }

        // ==========================================
        // SECTION 5: Advanced Network & DRM Settings Group
        // ==========================================
        IosSectionHeader(title = "الحماية والمعاملات المتقدمة")
        IosListGroup {
          val arrowRotation by animateFloatAsState(
            targetValue = if (showAdvanced) 180f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "arrowRotation"
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showAdvanced = !showAdvanced }
              .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              IosIconBadge(
                icon = Icons.Default.Security,
                background = IosBadgeColors.Orange,
                size = 32.dp,
                iconSize = 18.dp
              )

              Column {
                Text(
                  text = "معاملات الرأس وتخطي الحظر و DRM",
                  color = Color.White,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = if (showAdvanced) "إخفاء لوحة الإعدادات المتقدمة" else "تعديل User-Agent, Origin, Referer ومفاتيح DRM",
                  color = Color(0xFF8E8E93),
                  fontSize = 11.5.sp
                )
              }
            }

            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = Color(0xFF8E8E93),
              modifier = Modifier
                .size(22.dp)
                .rotate(arrowRotation)
            )
          }

          AnimatedVisibility(
            visible = showAdvanced,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              HorizontalDivider(thickness = 0.5.dp, color = Color(0x1FFFFFFF))

              // DRM Scheme & Keys
              Text(
                text = "إعدادات DRM (Widevine / ClearKey)",
                color = IosSystemOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // DRM Scheme
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x18FFFFFF))
                    .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                  if (drmSchemeInput.isEmpty()) {
                    Text("DRM (clearkey/widevine)", color = Color(0xFF636366), fontSize = 11.sp)
                  }
                  BasicTextField(
                    value = drmSchemeInput,
                    onValueChange = { drmSchemeInput = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    cursorBrush = SolidColor(IosSystemBlue),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                  )
                }

                // DRM Key
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x18FFFFFF))
                    .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                  if (drmKeyInput.isEmpty()) {
                    Text("Key (KeyId:Key / License)", color = Color(0xFF636366), fontSize = 11.sp)
                  }
                  BasicTextField(
                    value = drmKeyInput,
                    onValueChange = { drmKeyInput = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    cursorBrush = SolidColor(IosSystemBlue),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }

              // User-Agent
              Text(
                text = "معامل User-Agent (تجاوز حظر السيرفرات)",
                color = Color(0xFF8E8E93),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
              )
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0x18FFFFFF))
                  .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                  .padding(horizontal = 12.dp, vertical = 10.dp)
              ) {
                if (userAgentInput.isEmpty()) {
                  Text("Mozilla/5.0 / AppleCoreMedia / VLC", color = Color(0xFF636366), fontSize = 12.sp)
                }
                BasicTextField(
                  value = userAgentInput,
                  onValueChange = { userAgentInput = it },
                  textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                  cursorBrush = SolidColor(IosSystemBlue),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
              }

              // Referer & Origin Headers
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Referer
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x18FFFFFF))
                    .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                  if (refererInput.isEmpty()) {
                    Text("Referer Header", color = Color(0xFF636366), fontSize = 11.sp)
                  }
                  BasicTextField(
                    value = refererInput,
                    onValueChange = { refererInput = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    cursorBrush = SolidColor(IosSystemBlue),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                  )
                }

                // Origin
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x18FFFFFF))
                    .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                  if (originInput.isEmpty()) {
                    Text("Origin Header", color = Color(0xFF636366), fontSize = 11.sp)
                  }
                  BasicTextField(
                    value = originInput,
                    onValueChange = { originInput = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    cursorBrush = SolidColor(IosSystemBlue),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }

              // Cookie
              Text(
                text = "ملفات تعريف الارتباط (Cookies / Session Tokens)",
                color = Color(0xFF8E8E93),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
              )
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0x18FFFFFF))
                  .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                  .padding(horizontal = 12.dp, vertical = 10.dp)
              ) {
                if (cookieInput.isEmpty()) {
                  Text("token=xyz; session=123", color = Color(0xFF636366), fontSize = 12.sp)
                }
                BasicTextField(
                  value = cookieInput,
                  onValueChange = { cookieInput = it },
                  textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                  cursorBrush = SolidColor(IosSystemBlue),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ==========================================
        // SECTION 6: Big iOS Fluid Launch Button
        // ==========================================
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val buttonScale by animateFloatAsState(
          targetValue = if (isPressed) 0.965f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
          label = "iosPlayBtnScale"
        )

        val isReady = urlInput.isNotBlank()

        Box(
          modifier = Modifier
            .scale(buttonScale)
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
              if (isReady) {
                Brush.horizontalGradient(
                  listOf(Color(0xFF0A84FF), Color(0xFF0066EE))
                )
              } else {
                Brush.horizontalGradient(
                  listOf(Color(0x22FFFFFF), Color(0x18FFFFFF))
                )
              }
            )
            .border(
              1.dp,
              if (isReady) Color(0x66FFFFFF) else Color(0x18FFFFFF),
              RoundedCornerShape(16.dp)
            )
            .clickable(
              interactionSource = interactionSource,
              indication = null,
              enabled = isReady
            ) {
              launchStreamWithData(urlInput, titleInput)
            },
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = if (isReady) Color.White else Color(0x40FFFFFF),
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "تشغيل البث الآن",
              color = if (isReady) Color.White else Color(0x50FFFFFF),
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(28.dp))
      }
    }
  }
}
