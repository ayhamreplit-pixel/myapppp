package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStream
import com.example.model.StreamUrlParser
import com.example.player.XtreamRepository
import com.example.ui.theme.IosDarkBackground
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassThin
import com.example.ui.theme.IosLabelPrimary
import com.example.ui.theme.IosLabelSecondary
import com.example.ui.theme.IosLabelTertiary
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemIndigo
import com.example.ui.theme.IosSystemOrange
import com.example.ui.theme.IosSystemPurple
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosSystemTeal
import com.example.ui.theme.TodGradients

/**
 * Dedicated Full-Screen Apple iOS Modern Glass Quick Link Destination
 * Designed with authentic iOS glassmorphic blur layers, inset grouped cards,
 * interactive spring feedback, clipboard paste, history chips, and DRM / advanced headers.
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

  // Presets & format indicators
  var streamFormatType by remember { mutableStateOf("تلقائي") }
  val streamFormats = listOf("تلقائي", "HLS / M3U8", "MPEG-TS", "DASH / MPD", "MP4 Direct")

  val quickSamples = listOf(
    Pair("Mux BigBuck (HLS)", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
    Pair("Akamai Live 1080p", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"),
    Pair("Apple BipBop (HLS)", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"),
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

  fun launchStream() {
    if (urlInput.isBlank()) return
    val raw = urlInput.trim()
    val parsed = StreamUrlParser.parse(raw)

    val finalCleanUrl = parsed.cleanUrl.ifBlank { raw }
    val finalUa = userAgentInput.trim().ifBlank { parsed.userAgent }
    val finalOrigin = originInput.trim().ifBlank { parsed.origin }
    val finalReferer = refererInput.trim().ifBlank { parsed.referer }
    val finalCookie = cookieInput.trim().ifBlank { parsed.cookie }
    val finalDrmScheme = drmSchemeInput.trim().ifBlank { parsed.drmScheme }
    val finalDrmKey = drmKeyInput.trim().ifBlank { parsed.drmLicense }

    val stream = BroadcastStream(
      id = "custom_${System.currentTimeMillis()}",
      title = titleInput.ifBlank { "بث مباشر سريع" },
      subtitle = if (!finalDrmScheme.isNullOrBlank()) "بث محمي ($finalDrmScheme)" else "رابط خارجي مباشر",
      category = "Direct Stream",
      streamUrl = finalCleanUrl,
      format = parsed.format,
      isLive = true,
      origin = finalOrigin,
      referer = finalReferer,
      cookie = finalCookie,
      userAgent = finalUa,
      drmScheme = finalDrmScheme,
      drmKey = finalDrmKey,
      extraHeaders = parsed.extraHeaders
    )
    xtreamRepo.addCustomUrlToHistory(titleInput.ifBlank { "بث مباشر" }, finalCleanUrl)
    onPlayStream(stream, listOf(stream))
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(TodGradients.IosCanvasBg)
  ) {
    // Ambient iOS Specular Glow Circles in background
    Box(
      modifier = Modifier
        .size(280.dp)
        .align(Alignment.TopEnd)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0x330A84FF), Color.Transparent)
          )
        )
    )
    Box(
      modifier = Modifier
        .size(240.dp)
        .align(Alignment.BottomStart)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0x22BF5AF2), Color.Transparent)
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      // 1. iOS Top Navigation Bar (Translucent Glass with Large Title support)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xBB000000))
          .border(0.5.dp, IosGlassBorderSubtle, RoundedCornerShape(0.dp))
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Back Button in Apple iOS Style
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onBack() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "رجوع",
            tint = IosSystemBlue,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "الرئيسية",
            color = IosSystemBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Text(
          text = "تشغيل رابط سريع",
          color = IosLabelPrimary,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )

        // Clear input or Play action button
        IconButton(
          onClick = {
            titleInput = ""
            urlInput = ""
          },
          enabled = urlInput.isNotBlank() || titleInput.isNotBlank()
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "مسح",
            tint = if (urlInput.isNotBlank() || titleInput.isNotBlank()) IosLabelSecondary else Color.Transparent,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // 2. Scrollable Body with iOS Inset Grouped layout
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Hero Header Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
              Brush.verticalGradient(
                listOf(Color(0x350A84FF), Color(0x151C1C24))
              )
            )
            .border(1.dp, Brush.verticalGradient(listOf(Color(0x550A84FF), Color(0x10FFFFFF))), RoundedCornerShape(20.dp))
            .padding(20.dp)
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
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(IosSystemGreen)
                )
                Text(
                  text = "المشغل المباشر الفائق",
                  color = IosSystemBlue,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "تشغيل أي بث أو ملف فيديو",
                color = IosLabelPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "الصق الرابط مباشرة، يدعم M3U8, TS, MPD, MP4 وفك تشفير DRM والحماية التلقائية.",
                color = IosLabelSecondary,
                fontSize = 12.5.sp,
                lineHeight = 17.sp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
              modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                  Brush.linearGradient(listOf(IosSystemBlue, Color(0xFF0055D4)))
                )
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp)),
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

        // Section 1: Stream Details (Input Box)
        IosSectionHeader(title = "رابط البث والعنوان")
        IosListGroup {
          // Title Input Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = titleInput,
              onValueChange = { titleInput = it },
              placeholder = { Text("اسم البث أو القناة (اختياري)", color = IosLabelTertiary, fontSize = 14.sp) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IosSystemBlue,
                unfocusedBorderColor = Color(0x1FFFFFFF),
                focusedContainerColor = Color(0x18FFFFFF),
                unfocusedContainerColor = Color(0x0EFFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )
          }

          // URL Input Row
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Paste Button
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x22FFFFFF))
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
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = IosSystemBlue, modifier = Modifier.size(14.dp))
                Text("لصق من الحافظة", color = IosSystemBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              Text(
                text = "رابط البث المباشر (URL)",
                color = IosLabelSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = urlInput,
              onValueChange = { parseAndFill(it) },
              placeholder = { Text("https://domain.com/live/index.m3u8", color = IosLabelTertiary, fontSize = 13.sp) },
              modifier = Modifier.fillMaxWidth(),
              minLines = 2,
              maxLines = 4,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IosSystemBlue,
                unfocusedBorderColor = Color(0x1FFFFFFF),
                focusedContainerColor = Color(0x18FFFFFF),
                unfocusedContainerColor = Color(0x0EFFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )
          }
        }
        IosSectionFooter(text = "يمكنك إدراج روابط البث المباشر بصيغ M3U8, TS, MPD أو حتى روابط مع معاملات Headers مدمجة.")

        // Quick Presets Slider
        IosSectionHeader(title = "عينات سريعة للاختبار الفوري")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          quickSamples.forEach { (name, sampleUrl) ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x18FFFFFF))
                .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                .clickable {
                  titleInput = name
                  parseAndFill(sampleUrl)
                }
                .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = IosSystemTeal, modifier = Modifier.size(15.dp))
                Text(name, color = IosLabelPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }

        // Section 2: Format Selector
        IosSectionHeader(title = "صيغة تدفق الفيديو")
        IosSegmentedControl(
          items = listOf("تلقائي", "HLS", "TS", "DASH", "MP4"),
          selectedIndex = when (streamFormatType) {
            "HLS / M3U8" -> 1
            "MPEG-TS" -> 2
            "DASH / MPD" -> 3
            "MP4 Direct" -> 4
            else -> 0
          },
          onSelect = { idx ->
            streamFormatType = streamFormats[idx]
          }
        )

        // Section 3: Advanced Network & DRM Settings Toggle
        IosSectionHeader(title = "الحماية والمعاملات المتقدمة")
        IosListGroup {
          IosListRow(
            title = "معاملات الهيدرز وتخطي الحظر و DRM",
            subtitle = if (showAdvanced) "إخفاء لوحة الإعدادات المتقدمة" else "تعديل User-Agent, Origin, Referer, Cookies, ومفاتيح DRM",
            iconBadge = {
              IosIconBadge(
                icon = Icons.Default.Security,
                background = Brush.linearGradient(listOf(IosSystemOrange, Color(0xFFD66000)))
              )
            },
            trailing = {
              Icon(
                imageVector = if (showAdvanced) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = IosLabelSecondary,
                modifier = Modifier.size(20.dp)
              )
            },
            showChevron = false,
            onClick = { showAdvanced = !showAdvanced }
          )

          AnimatedVisibility(visible = showAdvanced) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // DRM Scheme & Key
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = drmSchemeInput,
                  onValueChange = { drmSchemeInput = it },
                  placeholder = { Text("DRM Scheme (clearkey/widevine)", color = IosLabelTertiary, fontSize = 11.sp) },
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IosSystemBlue,
                    unfocusedBorderColor = Color(0x1FFFFFFF),
                    focusedContainerColor = Color(0x14FFFFFF),
                    unfocusedContainerColor = Color(0x0AFFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )

                OutlinedTextField(
                  value = drmKeyInput,
                  onValueChange = { drmKeyInput = it },
                  placeholder = { Text("License / Keys (KeyId:Key)", color = IosLabelTertiary, fontSize = 11.sp) },
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IosSystemBlue,
                    unfocusedBorderColor = Color(0x1FFFFFFF),
                    focusedContainerColor = Color(0x14FFFFFF),
                    unfocusedContainerColor = Color(0x0AFFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )
              }

              // User-Agent
              OutlinedTextField(
                value = userAgentInput,
                onValueChange = { userAgentInput = it },
                placeholder = { Text("User-Agent (لتجاوز فحص وحظر السيرفرات)", color = IosLabelTertiary, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = IosSystemBlue,
                  unfocusedBorderColor = Color(0x1FFFFFFF),
                  focusedContainerColor = Color(0x14FFFFFF),
                  unfocusedContainerColor = Color(0x0AFFFFFF),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )

              // Referer & Origin
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = refererInput,
                  onValueChange = { refererInput = it },
                  placeholder = { Text("Referer Header", color = IosLabelTertiary, fontSize = 11.sp) },
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IosSystemBlue,
                    unfocusedBorderColor = Color(0x1FFFFFFF),
                    focusedContainerColor = Color(0x14FFFFFF),
                    unfocusedContainerColor = Color(0x0AFFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )

                OutlinedTextField(
                  value = originInput,
                  onValueChange = { originInput = it },
                  placeholder = { Text("Origin Header", color = IosLabelTertiary, fontSize = 11.sp) },
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IosSystemBlue,
                    unfocusedBorderColor = Color(0x1FFFFFFF),
                    focusedContainerColor = Color(0x14FFFFFF),
                    unfocusedContainerColor = Color(0x0AFFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  )
                )
              }

              // Cookie
              OutlinedTextField(
                value = cookieInput,
                onValueChange = { cookieInput = it },
                placeholder = { Text("Cookies (Session & Token)", color = IosLabelTertiary, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = IosSystemBlue,
                  unfocusedBorderColor = Color(0x1FFFFFFF),
                  focusedContainerColor = Color(0x14FFFFFF),
                  unfocusedContainerColor = Color(0x0AFFFFFF),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Big iOS Primary Action Button ("تشغيل البث الآن ▶")
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val buttonScale by animateFloatAsState(
          targetValue = if (isPressed) 0.97f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
          label = "iosBtnScale"
        )

        Button(
          onClick = { launchStream() },
          enabled = urlInput.isNotBlank(),
          modifier = Modifier
            .scale(buttonScale)
            .fillMaxWidth()
            .height(54.dp),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = IosSystemBlue,
            contentColor = Color.White,
            disabledContainerColor = Color(0x1AFFFFFF),
            disabledContentColor = Color(0x40FFFFFF)
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "تشغيل البث الآن",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}
