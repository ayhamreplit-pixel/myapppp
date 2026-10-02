package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.example.ui.theme.DarkTextTertiary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.model.SportsCompetition
import com.example.model.SportsMatch
import com.example.model.SportsShow
import com.example.model.SportsTeam
import com.example.model.XtreamChannel
import com.example.ui.theme.AppFontFamily
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold

/**
 * Luxury Dark Gradients matching the original app design.
 */
val TodCardGradient = Brush.verticalGradient(
  listOf(
    Color(0xFF1B1E32),
    Color(0xFF111322),
    Color(0xFF090A14)
  )
)

val TodHeroGradient = Brush.verticalGradient(
  listOf(
    Color(0xFF1F243A),
    Color(0xFF131626),
    Color(0xFF080A12)
  )
)

val TodSpecularBorder = Brush.linearGradient(
  listOf(
    Color(0x4564D2FF),
    Color(0x35BF5AF2),
    Color(0x20FFFFFF)
  )
)

/**
 * High-resolution Team Crest Component (Pure transparent crests - NO circle underneath per user instruction)
 */
@Composable
fun TodTeamCrest(
  team: SportsTeam,
  size: Dp = 46.dp,
  borderGlowColor: Color = Color(0xFF64D2FF),
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    when {
      team.name.contains("ألمانيا") || team.code == "GER" -> {
        Image(
          painter = painterResource(id = R.drawable.ic_crest_germany),
          contentDescription = team.name,
          modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)),
          contentScale = ContentScale.Fit
        )
      }
      team.name.contains("صربيا") || team.code == "SRB" -> {
        Image(
          painter = painterResource(id = R.drawable.ic_crest_serbia),
          contentDescription = team.name,
          modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)),
          contentScale = ContentScale.Fit
        )
      }
      team.name.contains("غينيا") || team.code == "GUI" -> {
        Image(
          painter = painterResource(id = R.drawable.ic_crest_guinea),
          contentDescription = team.name,
          modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)),
          contentScale = ContentScale.Fit
        )
      }
      team.name.contains("كينيا") || team.code == "KEN" -> {
        Image(
          painter = painterResource(id = R.drawable.ic_crest_kenya),
          contentDescription = team.name,
          modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)),
          contentScale = ContentScale.Fit
        )
      }
      !team.logoUrl.isNullOrBlank() -> {
        AsyncImage(
          model = team.logoUrl,
          contentDescription = team.name,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Fit
        )
      }
      else -> {
        // Clean transparent crest display (Zero circles underneath)
        Text(
          text = team.flagEmoji,
          fontSize = (size.value * 0.72f).sp,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

/**
 * Official App Brand Logo for Poster Top Right (PNG / Emblemed TOD Logo)
 */
@Composable
fun TodPosterTopRightLogo(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0x40000000))
      .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Text(
      text = "TOD",
      color = Color(0xFFFFB800),
      fontSize = 20.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 1.sp,
      fontFamily = ThmanyahFontFamily
    )
    Image(
      painter = painterResource(id = R.drawable.tod_icon),
      contentDescription = "TOD",
      modifier = Modifier
        .size(24.dp)
        .clip(RoundedCornerShape(6.dp)),
      contentScale = ContentScale.Crop
    )
  }
}

/**
 * Countdown timer pill bar matching TOD Screenshots (04 ساعات : 45 دقائق : 18 ثوانى)
 */
@Composable
fun TodCountdownPillBar(
  hours: String = "04",
  minutes: String = "45",
  seconds: String = "18",
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Start
  ) {
    // 1. Hours Pill (Right in Arabic RTL)
    TodTimerPill(label = "ساعات", value = hours)

    Text(
      text = ":",
      color = Color(0xFFFFB800),
      fontSize = 15.sp,
      fontWeight = FontWeight.Black
    )

    // 2. Minutes Pill (Center)
    TodTimerPill(label = "دقائق", value = minutes)

    Text(
      text = ":",
      color = Color(0xFFFFB800),
      fontSize = 15.sp,
      fontWeight = FontWeight.Black
    )

    // 3. Seconds Pill (Left in Arabic RTL)
    TodTimerPill(label = "ثواني", value = seconds)
  }
}

@Composable
fun TodTimerPill(
  label: String,
  value: String
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0x851E2638))
      .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Text(
        text = label,
        color = Color(0xFFE2E8F0),
        fontSize = 12.5.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = value,
        color = Color(0xFFFFB800),
        fontSize = 14.5.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Black
      )
    }
  }
}

/**
 * 1. Hero Match Connected Banner (100% Identical to TOD Screenshot)
 * Seamless full-width poster without side-clips, live ticking countdown,
 * Home/Away crests (Home on Right), and smooth integrated bottom gradient.
 */
@Composable
fun PulsingLiveRedBadgeLarge() {
  val infiniteTransition = rememberInfiniteTransition(label = "pulseLive")
  val alphaPulse by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alphaPulse"
  )

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(
        Brush.horizontalGradient(
          listOf(Color(0xFFFF375F), Color(0xFFFF453A))
        )
      )
      .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(10.dp))
      .padding(horizontal = 14.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = alphaPulse))
      )
      Text(
        text = "مباشر الآن",
        color = Color.White,
        fontSize = 14.5.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Black
      )
    }
  }
}

/**
 * High-Fidelity TOD Hero Poster with Realtime Smart Kickoff Calculator & Dynamic Pulsing Live Badge
 */
@Composable
fun TodMatchCountdownHero(
  match: SportsMatch,
  onPlayMatch: (SportsMatch) -> Unit,
  onOpenDetails: (SportsMatch) -> Unit,
  currentIndex: Int = 0,
  totalCount: Int = 1,
  onSelectIndex: (Int) -> Unit = {},
  modifier: Modifier = Modifier
) {
  // Smart Kickoff Calculation
  val nowMillis = System.currentTimeMillis()
  val targetKickoffMillis = remember(match) {
    try {
      val parts = match.kickoffTime.split(":")
      if (parts.size >= 2) {
        val hour = parts[0].trim().toIntOrNull() ?: 21
        val minute = parts[1].trim().toIntOrNull() ?: 45
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
        cal.set(java.util.Calendar.MINUTE, minute)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
      } else {
        nowMillis + 17118000L
      }
    } catch (e: Exception) {
      nowMillis + 17118000L
    }
  }

  var currentMillis by remember { mutableStateOf(System.currentTimeMillis()) }

  LaunchedEffect(match.id) {
    while (true) {
      delay(1000)
      currentMillis = System.currentTimeMillis()
    }
  }

  val remainingSeconds = remember(currentMillis, targetKickoffMillis) {
    ((targetKickoffMillis - currentMillis) / 1000).coerceAtLeast(0)
  }

  val hours = remember(remainingSeconds) {
    String.format(java.util.Locale.ENGLISH, "%02d", remainingSeconds / 3600)
  }
  val minutes = remember(remainingSeconds) {
    String.format(java.util.Locale.ENGLISH, "%02d", (remainingSeconds % 3600) / 60)
  }
  val seconds = remember(remainingSeconds) {
    String.format(java.util.Locale.ENGLISH, "%02d", remainingSeconds % 60)
  }

  val isNowLive = match.isLive || (targetKickoffMillis <= nowMillis && remainingSeconds <= 0)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(580.dp)
  ) {
    // 1. Full-bleed Poster Image (Hero Composite - Full width without rounded corner clips)
    val isGermanyPoster = match.id == "germany_serbia_nations" ||
      match.title.contains("ألمانيا") ||
      match.bannerUrl?.contains("87c142b3") == true ||
      match.bannerUrl?.contains("tod_germany_serbia_poster") == true

    val posterAlphaModifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
      }
      .drawWithContent {
        drawContent()
        drawRect(
          brush = Brush.verticalGradient(
            0.00f to Color.Black,
            0.50f to Color.Black,
            0.72f to Color.Black.copy(alpha = 0.75f),
            0.88f to Color.Black.copy(alpha = 0.30f),
            1.00f to Color.Transparent
          ),
          blendMode = BlendMode.DstIn
        )
      }

    if (isGermanyPoster) {
      Image(
        painter = painterResource(id = R.drawable.tod_germany_serbia_poster),
        contentDescription = match.title,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        modifier = posterAlphaModifier
      )
    } else {
      val heroBackdrop = if (!match.bannerUrl.isNullOrBlank()) match.bannerUrl else "android.resource://com.example/drawable/tod_hero_match_banner"
      AsyncImage(
        model = heroBackdrop,
        contentDescription = match.title,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        modifier = posterAlphaModifier
      )
    }

    val activeTheme = com.example.ui.theme.ThemeStateHolder.currentTheme
    val playButtonGradient = activeTheme.gradient
    val playTextColor = if (activeTheme == com.example.ui.theme.AppThemePreset.GOLD || activeTheme == com.example.ui.theme.AppThemePreset.CYAN) Color.Black else Color.White

    // 2. Top Subtle Glow Gradient (Seamless dark top shading for status bar integration)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
        .align(Alignment.TopCenter)
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xDD0A1024),
              Color(0x880A1024),
              Color(0x350A1024),
              Color.Transparent
            )
          )
        )
    )

    // 3. Ultra-Smooth Bottom Shadow Gradient (Protects text legibility over poster, fades back to transparent at bottom edge with ZERO line)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(360.dp)
        .align(Alignment.BottomCenter)
        .background(
          Brush.verticalGradient(
            listOf(
              Color.Transparent,
              Color(0x20000000),
              Color(0x70000000),
              Color(0xB0000000),
              Color(0x80000000),
              Color.Transparent
            )
          )
        )
    )

    // 4. Bottom Overlay Content (Logos, Title, Metadata, Red Live Badge, Action Buttons)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomStart)
        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
      horizontalAlignment = Alignment.Start
    ) {
      // 4.1 Team Flags / Crests in Softened Semi-Transparent Rectangles with LARGER Logos
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 10.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Home Team Flag / Crest in Softened Light Rectangle with Larger Logo
          Box(
            modifier = Modifier
              .size(width = 60.dp, height = 42.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0x22FFFFFF))
              .border(0.75.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
              .padding(3.dp),
            contentAlignment = Alignment.Center
          ) {
            TodTeamCrest(team = match.homeTeam, size = 42.dp)
          }

          // Custom Drawn White Dash Bar "-"
          Box(
            modifier = Modifier
              .width(16.dp)
              .height(3.dp)
              .clip(RoundedCornerShape(1.5.dp))
              .background(Color.White)
          )

          // Away Team Flag / Crest in Softened Light Rectangle with Larger Logo
          Box(
            modifier = Modifier
              .size(width = 60.dp, height = 42.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0x22FFFFFF))
              .border(0.75.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
              .padding(3.dp),
            contentAlignment = Alignment.Center
          ) {
            TodTeamCrest(team = match.awayTeam, size = 42.dp)
          }
        }
      }

      // 4.2 Match Title (RTL Right-aligned, sized moderately)
      Text(
        text = match.title.ifBlank { "${match.homeTeam.name} ضد ${match.awayTeam.name}" },
        color = Color.White,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = ThmanyahFontFamily,
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(4.dp))

      // 4.3 Match Subtitle Metadata (Date • Time • Stadium • Tournament)
      Text(
        text = "${match.kickoffDate} • ${match.kickoffTime} • ${match.stadium.ifBlank { "الملعب الرئيسي" }} • ${match.tournament}",
        color = Color(0xFFD1D5DB),
        fontSize = 13.5.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Right,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(6.dp))

      // 4.4 Red "مباشر" Badge (Slightly enlarged and clearly visible)
      if (match.isLive || isNowLive) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE50914))
            .padding(horizontal = 10.dp, vertical = 3.5.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "مباشر",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ThmanyahFontFamily
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
      }

      // 4.5 Action Buttons (Screenshot 1, 2, 5: Wide "My TOD [+]" or Screenshot 3, 4: "[+] | تشغيل ▶")
      if (match.isLive) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val isSaved = SavedMatchesHolder.isMatchSaved(match.id)

          // Plus / Saved Checkmark Box Button
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSaved) Color(0x3530D158) else Color(0x902C3444))
              .border(
                width = 0.75.dp,
                color = if (isSaved) Color(0xFF30D158) else Color(0x40FFFFFF),
                shape = RoundedCornerShape(12.dp)
              )
              .clickable {
                SavedMatchesHolder.toggleSaveMatch(match.id)
              },
            contentAlignment = Alignment.Center
          ) {
            if (isSaved) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "محفوظ",
                tint = Color(0xFF30D158),
                modifier = Modifier.size(24.dp)
              )
            } else {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .border(1.5.dp, Color.White, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "حفظ",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          // Large Theme-Adaptive "تشغيل ▶" Button (Matches selected settings theme)
          Box(
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(playButtonGradient)
              .clickable { onPlayMatch(match) },
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = playTextColor, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "تشغيل",
                color = playTextColor,
                fontSize = 17.sp,
                fontFamily = ThmanyahFontFamily,
                fontWeight = FontWeight.Black
              )
            }
          }
        }
      } else {
        // Wide "My TOD [+]" Button (Theme-Adaptive Accent)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(playButtonGradient)
            .clickable { onOpenDetails(match) },
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = "My TOD",
              color = playTextColor,
              fontSize = 16.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .size(20.dp)
                .border(1.5.dp, playTextColor, RoundedCornerShape(4.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = playTextColor, modifier = Modifier.size(14.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 5.6 Carousel Indicator Dots: • • • ───── • • •
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val count = totalCount.coerceAtLeast(1)
        if (count > 1) {
          for (idx in 0 until count) {
            val isCurrent = idx == currentIndex
            Box(
              modifier = Modifier
                .clickable { onSelectIndex(idx) }
                .padding(horizontal = 2.5.dp, vertical = 4.dp)
            ) {
              if (isCurrent) {
                Box(
                  modifier = Modifier
                    .width(24.dp)
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(4.5.dp)
                    .clip(CircleShape)
                    .background(Color(0x60FFFFFF))
                )
              }
            }
          }
        } else {
          Box(modifier = Modifier.size(4.5.dp).clip(CircleShape).background(Color(0x60FFFFFF)))
          Spacer(modifier = Modifier.width(3.dp))
          Box(modifier = Modifier.width(24.dp).height(3.5.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
        }
      }
    }
  }
}

/**
 * Dynamic Multi-Poster Hero Banner Carousel (Screenshots 11, 15)
 * Supports multiple posters with auto-advance, smooth horizontal pager,
 * dynamic indicator dots, and automatic cards generation.
 */
@Composable
fun TodHeroBannerCarousel(
  matches: List<SportsMatch>,
  onPlayMatch: (SportsMatch) -> Unit,
  onOpenDetails: (SportsMatch) -> Unit,
  modifier: Modifier = Modifier
) {
  if (matches.isEmpty()) return

  if (matches.size == 1) {
    TodMatchCountdownHero(
      match = matches[0],
      onPlayMatch = onPlayMatch,
      onOpenDetails = onOpenDetails,
      currentIndex = 0,
      totalCount = 1,
      modifier = modifier
    )
    return
  }

  val pagerState = rememberPagerState(pageCount = { matches.size })
  val coroutineScope = rememberCoroutineScope()

  // Gentle auto-scroll every 6 seconds
  LaunchedEffect(pagerState, matches.size) {
    while (true) {
      delay(6000)
      if (!pagerState.isScrollInProgress) {
        val nextPage = (pagerState.currentPage + 1) % matches.size
        pagerState.animateScrollToPage(nextPage)
      }
    }
  }

  Box(modifier = modifier.fillMaxWidth()) {
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth()
    ) { page ->
      TodMatchCountdownHero(
        match = matches[page],
        onPlayMatch = onPlayMatch,
        onOpenDetails = onOpenDetails,
        currentIndex = pagerState.currentPage,
        totalCount = matches.size,
        onSelectIndex = { targetIdx ->
          coroutineScope.launch {
            pagerState.animateScrollToPage(targetIdx)
          }
        },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

/**
 * Global Saved Matches In-Memory State Manager
 */
object SavedMatchesHolder {
  private val _savedMatchIds = androidx.compose.runtime.mutableStateListOf<String>()

  fun isMatchSaved(matchId: String): Boolean {
    return _savedMatchIds.contains(matchId)
  }

  fun toggleSaveMatch(matchId: String): Boolean {
    return if (_savedMatchIds.contains(matchId)) {
      _savedMatchIds.remove(matchId)
      false
    } else {
      _savedMatchIds.add(matchId)
      true
    }
  }
}

/**
 * GIF Skeleton Loading Placeholder Composable using loading_skeleton.gif
 */
@Composable
fun TodSkeletonPlaceholder(
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop
) {
  val context = LocalContext.current
  val imageLoader = remember(context) {
    ImageLoader.Builder(context)
      .components {
        if (android.os.Build.VERSION.SDK_INT >= 28) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
      }
      .build()
  }

  AsyncImage(
    model = R.drawable.loading_skeleton,
    contentDescription = "جاري التحميل...",
    imageLoader = imageLoader,
    modifier = modifier.fillMaxSize(),
    contentScale = contentScale
  )
}

/**
 * 2. Official Vector Channel Logo Renderer (100% Matches Screenshots 161610 & 161607)
 * Renders pure logos directly on transparent background: beIN Sports, Al Jazeera, beIN Series, Premier League, LFCTV
 */
@Composable
fun TodDrawnOfficialChannelLogo(channelName: String, modifier: Modifier = Modifier) {
  val nameLower = channelName.lowercase()
  when {
    nameLower.contains("premier") || nameLower.contains("بريمير") -> {
      // Premier League White Lion Emblem + Text (Screenshot 161607)
      Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.SportsSoccer,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Premier\nLeague",
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          fontFamily = ThmanyahFontFamily,
          lineHeight = 12.sp,
          textAlign = TextAlign.Center
        )
      }
    }
    nameLower.contains("lfc") || nameLower.contains("liverpool") -> {
      // LFCTV Pure White Typography (Screenshot 161607)
      Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "LFCTV",
          color = Color.White,
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          fontFamily = ThmanyahFontFamily,
          letterSpacing = 1.sp
        )
      }
    }
    nameLower.contains("جزيرة") || nameLower.contains("jazeera") -> {
      // Al Jazeera Iconic Gold Flame Calligraphy (Screenshots 161610 & 161607)
      Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "الجزيرة",
          color = Color(0xFFE5A823),
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          fontFamily = ThmanyahFontFamily
        )
        if (nameLower.contains("وثائق") || nameLower.contains("doc")) {
          Text(
            text = "الوثائقية\nDOCUMENTARY",
            color = Color(0xFFE5A823),
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ThmanyahFontFamily,
            lineHeight = 9.sp,
            textAlign = TextAlign.Center
          )
        } else if (nameLower.contains("مباشر") || nameLower.contains("live")) {
          Text(
            text = "مباشر",
            color = Color(0xFFE5A823),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ThmanyahFontFamily
          )
        }
      }
    }
    nameLower.contains("series") || nameLower.contains("مسلسل") -> {
      // beIN Series Blue Badge (Screenshots 161610 & 161607)
      val seriesNum = if (nameLower.contains("2")) "2" else "1"
      Box(
        modifier = modifier
          .width(108.dp)
          .height(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0091DF))
          .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "beIN",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = ThmanyahFontFamily
            )
            Spacer(modifier = Modifier.width(5.dp))
            Box(
              modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x30000000)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = seriesNum,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
          Text(
            text = "SERIES",
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
        }
      }
    }
    nameLower.contains("movie") || nameLower.contains("أفلام") || nameLower.contains("افلام") -> {
      // beIN Movies Ruby Badge (Screenshot 161610)
      val movieNum = if (nameLower.contains("2")) "2" else "1"
      Box(
        modifier = modifier
          .width(108.dp)
          .height(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFD61834))
          .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "beIN",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = ThmanyahFontFamily
            )
            Spacer(modifier = Modifier.width(5.dp))
            Box(
              modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x30000000)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = movieNum,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
          Text(
            text = "MOVIES",
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
        }
      }
    }
    nameLower.contains("kass") || nameLower.contains("كأس") -> {
      // Alkass Sports Maroon Emblem
      val isExtra = nameLower.contains("extra") || nameLower.contains("إكسترا")
      Box(
        modifier = modifier
          .width(108.dp)
          .height(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF6B0B24))
          .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "الكأس",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            fontFamily = ThmanyahFontFamily
          )
          Text(
            text = if (isExtra) "EXTRA HD" else "ALKASS 1",
            color = Color(0xFFFFD700),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        }
      }
    }
    nameLower.contains("ssc") -> {
      // SSC Saudi Sports Company Emblem
      Box(
        modifier = modifier
          .width(108.dp)
          .height(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF091A2E))
          .border(0.75.dp, Color(0x3500E676), RoundedCornerShape(12.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "SSC",
            color = Color(0xFF00E676),
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            fontFamily = ThmanyahFontFamily,
            letterSpacing = 1.sp
          )
          Text(
            text = "1 HD",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
    nameLower.contains("أبوظبي") || nameLower.contains("ad sport") -> {
      // Abu Dhabi Sports Emblem
      Box(
        modifier = modifier
          .width(108.dp)
          .height(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0284C7))
          .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "أبوظبي",
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Black,
            fontFamily = ThmanyahFontFamily
          )
          Text(
            text = "SPORTS 1",
            color = Color(0xFFE0F2FE),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        }
      }
    }
    nameLower.contains("ontime") || nameLower.contains("on time") -> {
      // ON Time Sports Emblem
      Box(
        modifier = modifier
          .width(108.dp)
          .height(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFB91C1C))
          .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "ON Time",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = ThmanyahFontFamily
          )
          Text(
            text = "SPORTS",
            color = Color.White,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        }
      }
    }
    else -> {
      // beIN Sports Iconic Purple Badge (Screenshots 161610 & 161607)
      val isNews = nameLower.contains("إخبار") || nameLower.contains("news")
      val channelNum = when {
        nameLower.contains("1") -> "1"
        nameLower.contains("2") -> "2"
        nameLower.contains("3") -> "3"
        nameLower.contains("4") -> "4"
        nameLower.contains("5") -> "5"
        nameLower.contains("6") -> "6"
        nameLower.contains("xtra") -> "XTRA"
        nameLower.contains("afc") -> "AFC"
        else -> null
      }

      Box(
        modifier = modifier
          .width(112.dp)
          .height(58.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF4B1669))
          .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
          .padding(horizontal = 7.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = "beIN",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              fontFamily = ThmanyahFontFamily
            )
            if (channelNum != null) {
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0x40000000))
                  .border(0.5.dp, Color(0x30FFFFFF), RoundedCornerShape(4.dp))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = channelNum,
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
          Text(
            text = "SPORTS",
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp
          )
          if (isNews) {
            Text(
              text = "الإخبارية",
              color = Color(0xFFFFB800),
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = ThmanyahFontFamily
            )
          }
        }
      }
    }
  }
}

/**
 * Pure Channel Logo (NO card, NO box, NO border - Pure logo floating directly on screen)
 */
@Composable
fun TodPureChannelLogo(
  channel: XtreamChannel,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .width(120.dp)
      .height(68.dp)
      .iosBounceClick(scaleDown = 0.92f, onClick = onClick)
      .padding(horizontal = 4.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    if (!channel.iconUrl.isNullOrBlank()) {
      SubcomposeAsyncImage(
        model = channel.iconUrl,
        contentDescription = channel.name,
        modifier = Modifier
          .fillMaxSize()
          .padding(2.dp),
        contentScale = ContentScale.Fit,
        error = {
          TodDrawnOfficialChannelLogo(channelName = channel.name)
        },
        loading = {
          TodDrawnOfficialChannelLogo(channelName = channel.name)
        }
      )
    } else {
      TodDrawnOfficialChannelLogo(channelName = channel.name)
    }
  }
}

/**
 * Channel Category Rail without cards - pure channel logos (Screenshots 161610 & 161607)
 */
@Composable
fun TodChannelsGroupRail(
  title: String,
  channels: List<XtreamChannel>,
  onPlayChannel: (XtreamChannel) -> Unit,
  modifier: Modifier = Modifier
) {
  if (channels.isEmpty()) return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
  ) {
    Text(
      text = title,
      color = Color.White,
      fontSize = 18.sp,
      fontFamily = ThmanyahFontFamily,
      fontWeight = FontWeight.Black,
      textAlign = TextAlign.Right,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp)
    )

    Spacer(modifier = Modifier.height(4.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(22.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      items(channels, key = { it.streamId }) { channel ->
        TodPureChannelLogo(
          channel = channel,
          onClick = { onPlayChannel(channel) }
        )
      }
    }
  }
}

/**
 * Tournament Visual Style definition for match cards (Screenshots 165527, 165536, 165601)
 * Colors vary strictly according to tournament brand identity and blend with user settings glow.
 */
/**
 * Authentic Tournament Card Watermark Types
 */
enum class TournamentWatermark {
  NONE,
  GEOMETRIC_CIRCLES, // Overlapping circular grid pattern (Screenshot 161624, 220513)
  UCL_STARS,         // Ambient UCL starball curves (Screenshot 162954, 161604)
  FRIENDLIES_LINES   // International Friendlies ambient field glow (Screenshot 161624)
}

/**
 * Tournament Visual Style definition for match cards (100% Matches Screenshots 1, 2, 3, 4)
 * Colors vary strictly according to tournament brand identity for ALL tournaments without exception.
 */
data class TournamentCardStyle(
  val tournamentKey: String,
  val displayName: String,
  val bgGradient: List<Color>,
  val accentColor: Color,
  val secondaryColor: Color,
  val logoUrl: String,
  val watermark: TournamentWatermark = TournamentWatermark.GEOMETRIC_CIRCLES
)

fun getTournamentCardStyle(tournamentName: String): TournamentCardStyle {
  val name = tournamentName.lowercase()
  return when {
    // 1. UEFA Champions League (Screenshots 1 & 3): Deep Royal Navy Blue & Starball
    name.contains("أبطال أوروبا") || name.contains("champions league") || name.contains("ucl") -> {
      TournamentCardStyle(
        tournamentKey = "ucl",
        displayName = "دوري أبطال أوروبا",
        bgGradient = listOf(
          Color(0xFF041138),
          Color(0xFF09205E),
          Color(0xFF030A20)
        ),
        accentColor = Color(0xFF00D4FF),
        secondaryColor = Color(0xFF0077B6),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        watermark = TournamentWatermark.UCL_STARS
      )
    }

    // 2. International Friendlies / مباريات ودية (Screenshot 4): Deep Rich Emerald Green
    name.contains("ودية") || name.contains("وديات") || name.contains("friendly") || name.contains("friendlies") -> {
      TournamentCardStyle(
        tournamentKey = "friendlies",
        displayName = "مباريات ودية دولية",
        bgGradient = listOf(
          Color(0xFF063618),
          Color(0xFF0B4E26),
          Color(0xFF042611)
        ),
        accentColor = Color(0xFF30D158),
        secondaryColor = Color(0xFF34C759),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/FIFA_logo_without_slogan.svg/512px-FIFA_logo_without_slogan.svg.png",
        watermark = TournamentWatermark.FRIENDLIES_LINES
      )
    }

    // 3. UEFA Nations League / Euro / World Cup (Screenshot 2): Deep Slate Navy with Circular Watermark
    name.contains("أمم") || name.contains("nations") || name.contains("euro") || name.contains("يورو") ||
        name.contains("كأس العالم") || name.contains("world cup") || name.contains("فيفا") || name.contains("دولي") -> {
      TournamentCardStyle(
        tournamentKey = "nations",
        displayName = "دوري الأمم الأوروبية",
        bgGradient = listOf(
          Color(0xFF101726),
          Color(0xFF17243B),
          Color(0xFF0A0F1A)
        ),
        accentColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF64D2FF),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }

    // 4. Premier League: Regal Deep Purple & Neon Mint
    name.contains("إنجليزي") || name.contains("premier") || name.contains("بريمير") -> {
      TournamentCardStyle(
        tournamentKey = "pl",
        displayName = "الدوري الإنجليزي الممتاز",
        bgGradient = listOf(
          Color(0xFF2C003D),
          Color(0xFF42005A),
          Color(0xFF1B0027)
        ),
        accentColor = Color(0xFF00FF85),
        secondaryColor = Color(0xFFE90052),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Premier_League_Logo.svg/512px-Premier_League_Logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }

    // 5. La Liga: Deep Spanish Burgundy & Crimson Red
    name.contains("إسباني") || name.contains("لا ليغا") || name.contains("laliga") || name.contains("اسباني") -> {
      TournamentCardStyle(
        tournamentKey = "laliga",
        displayName = "الدوري الإسباني",
        bgGradient = listOf(
          Color(0xFF2E050F),
          Color(0xFF440917),
          Color(0xFF1C0209)
        ),
        accentColor = Color(0xFFFF2E50),
        secondaryColor = Color(0xFFFF6B6B),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/LaLiga_logo_2023.svg/512px-LaLiga_logo_2023.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }

    // 6. Serie A: Deep Ocean Sapphire Blue
    name.contains("إيطالي") || name.contains("serie") || name.contains("كالتشيو") -> {
      TournamentCardStyle(
        tournamentKey = "seriea",
        displayName = "الدوري الإيطالي",
        bgGradient = listOf(
          Color(0xFF031A3A),
          Color(0xFF063063),
          Color(0xFF021228)
        ),
        accentColor = Color(0xFF008CFF),
        secondaryColor = Color(0xFF64D2FF),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Serie_A_logo_2019.svg/512px-Serie_A_logo_2019.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }

    // 7. Saudi Pro League: Deep Royal Malachite & Gold
    name.contains("روشن") || name.contains("سعودي") -> {
      TournamentCardStyle(
        tournamentKey = "spl",
        displayName = "دوري روشن السعودي",
        bgGradient = listOf(
          Color(0xFF04271D),
          Color(0xFF073C2D),
          Color(0xFF021812)
        ),
        accentColor = Color(0xFF00E676),
        secondaryColor = Color(0xFFF59E0B),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Saudi_Pro_League_logo.svg/512px-Saudi_Pro_League_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }

    // 8. AFC Champions League: Deep Charcoal Gold
    name.contains("آسيا") || name.contains("afc") -> {
      TournamentCardStyle(
        tournamentKey = "afc",
        displayName = "دوري أبطال آسيا",
        bgGradient = listOf(
          Color(0xFF1D180D),
          Color(0xFF2E2715),
          Color(0xFF120F08)
        ),
        accentColor = Color(0xFFFFB800),
        secondaryColor = Color(0xFFF59E0B),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/d/d4/AFC_Champions_League_Elite_logo.svg/512px-AFC_Champions_League_Elite_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }

    // 9. CAF / تصفيات أمم أفريقيا: Deep Savannah Earth
    name.contains("أفريقيا") || name.contains("caf") || name.contains("افريقيا") -> {
      TournamentCardStyle(
        tournamentKey = "caf",
        displayName = "تصفيات أمم أفريقيا",
        bgGradient = listOf(
          Color(0xFF1C2609),
          Color(0xFF2B3B0D),
          Color(0xFF101704)
        ),
        accentColor = Color(0xFFFFC72C),
        secondaryColor = Color(0xFF22C55E),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/0/07/Confederation_of_African_Football_logo.svg/512px-Confederation_of_African_Football_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }

    // 10. Padel, Tennis & Multi-Sports (Screenshot 4): Deep Carbon Slate
    name.contains("بادل") || name.contains("تنس") || name.contains("padel") || name.contains("tennis") || name.contains("رياضات") -> {
      TournamentCardStyle(
        tournamentKey = "padel",
        displayName = "بث مباشر - رياضات متنوعة",
        bgGradient = listOf(
          Color(0xFF14171E),
          Color(0xFF1E232E),
          Color(0xFF0F1116)
        ),
        accentColor = Color(0xFFE50914),
        secondaryColor = Color(0xFF8E8E93),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/ATP_Tour_logo.svg/512px-ATP_Tour_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }

    // 11. Universal / General Default
    else -> {
      TournamentCardStyle(
        tournamentKey = "general",
        displayName = tournamentName,
        bgGradient = listOf(
          Color(0xFF0C142A),
          Color(0xFF142244),
          Color(0xFF080D1D)
        ),
        accentColor = Color(0xFF64D2FF),
        secondaryColor = Color(0xFF0A84FF),
        logoUrl = "",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES
      )
    }
  }
}

/**
 * Team Crest or Country Flag Badge
 * Flags are wrapped in a rounded white-bordered badge (Screenshots 2 and 4),
 * while club logos are displayed cleanly as pure crests (Screenshots 1 and 3).
 */
@Composable
fun TodTeamBadgeOrFlag(
  team: SportsTeam,
  size: Dp = 28.dp,
  modifier: Modifier = Modifier
) {
  val knownCountries = listOf(
    "ألمانيا", "صربيا", "ليتوانيا", "أندورا", "البرتغال", "الدنمارك",
    "إسبانيا", "فرنسا", "إيطاليا", "إنجلترا", "البرازيل", "الأرجنتين",
    "السعودية", "مصر", "المغرب", "العراق", "الجزائر", "تونس", "قطر",
    "الإمارات", "غينيا", "كينيا", "كرواتيا", "هولندا", "بلجيكا",
    "النرويج", "ويلز", "الكاميرون", "الكونغو", "الغابون"
  )
  val isCountry = knownCountries.any { team.name.contains(it) } ||
      team.code in listOf("GER", "SRB", "LTU", "AND", "POR", "DEN", "ESP", "FRA", "ITA", "ENG", "BRA", "ARG", "KSA", "EGY", "MAR", "IRQ", "ALG", "TUN", "QAT", "UAE", "GUI", "KEN", "NOR", "WAL", "CMR", "CGO", "GAB")

  if (isCountry) {
    Box(
      modifier = modifier
        .size(width = 38.dp, height = 26.dp)
        .clip(RoundedCornerShape(5.dp))
        .background(Color(0x30FFFFFF))
        .border(0.85.dp, Color.White, RoundedCornerShape(5.dp)),
      contentAlignment = Alignment.Center
    ) {
      TodTeamCrest(team = team, size = 26.dp)
    }
  } else {
    TodTeamCrest(team = team, size = size, modifier = modifier)
  }
}

/**
 * 2. High-Precision Official TOD Match Card (100% Matches Screenshots 1, 2, 3, 4)
 * Architecture (Strict Right-To-Left RTL Layout):
 * - 3 horizontal stacked bands with subtle divider lines
 * - Band 1: Header (Right side: Kickoff Time/Date or Red "مباشر" Block, Left side: Tournament Logo)
 * - Band 2: Team 1 (Right side: Team Crest/Flag + Name, Left side: Score)
 * - Band 3: Team 2 (Right side: Team Crest/Flag + Name, Left side: Score)
 * - Dynamic tournament theme color gradients & authentic watermarks
 */
@Composable
fun TodTournamentMatchCard(
  match: SportsMatch,
  onClick: () -> Unit,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
  isCompactWidth: Boolean = true
) {
  val tournamentStyle = getTournamentCardStyle(match.tournament)

  val cardBackground = Brush.verticalGradient(
    listOf(
      tournamentStyle.bgGradient[0],
      tournamentStyle.bgGradient[1],
      tournamentStyle.bgGradient[2]
    )
  )

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .then(if (isCompactWidth) Modifier.width(280.dp).height(162.dp) else Modifier.fillMaxWidth().height(162.dp))
        .iosBounceClick(scaleDown = 0.97f, onClick = { if (match.isLive) onPlayClick() else onClick() })
        .clip(RoundedCornerShape(16.dp))
        .background(cardBackground)
        .border(0.75.dp, Color(0x35FFFFFF), RoundedCornerShape(16.dp))
    ) {
      // 1. Authentic Watermark Background Texture
      when (tournamentStyle.watermark) {
        TournamentWatermark.GEOMETRIC_CIRCLES -> {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 28.dp.toPx()
            val radius = 13.dp.toPx()
            val col = Color(0x0EFFFFFF)
            val col2 = Color(0x05FFFFFF)
            for (x in -radius.toInt()..(size.width.toInt() + radius.toInt()) step step.toInt()) {
              for (y in -radius.toInt()..(size.height.toInt() + radius.toInt()) step step.toInt()) {
                drawCircle(
                  color = col,
                  radius = radius,
                  center = Offset(x.toFloat(), y.toFloat()),
                  style = Stroke(width = 1.2.dp.toPx())
                )
                drawCircle(
                  color = col2,
                  radius = radius * 0.5f,
                  center = Offset(x.toFloat(), y.toFloat())
                )
              }
            }
          }
        }
        TournamentWatermark.UCL_STARS -> {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
              color = Color(0x1000D4FF),
              radius = size.width * 0.55f,
              center = Offset(size.width * 0.85f, size.height * 0.15f)
            )
            drawCircle(
              color = Color(0x08FFFFFF),
              radius = size.width * 0.35f,
              center = Offset(size.width * 0.15f, size.height * 0.85f)
            )
          }
        }
        TournamentWatermark.FRIENDLIES_LINES -> {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
              color = Color(0x1230D158),
              radius = size.width * 0.50f,
              center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
          }
        }
        else -> {}
      }

      // 2. Card Content: 3 Stacked Horizontal Bands (Strict RTL: Right -> Left)
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Band 1: Top Header (Right: Time/Date or "مباشر", Left: Tournament Logo)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0x18000000))
            .padding(horizontal = 14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Right side (START in RTL): Red "مباشر" Block or Kickoff Time/Date
            if (match.isLive) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    Brush.horizontalGradient(
                      listOf(Color(0xFFE50914), Color(0xFFCC0018))
                    )
                  )
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                )
                Text(
                  text = if (!match.liveMinute.isNullOrBlank()) "مباشر ${match.liveMinute}" else "مباشر",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = ThmanyahFontFamily
                )
              }
            } else {
              Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = match.kickoffTime,
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = ThmanyahFontFamily
                )
                Text(
                  text = match.kickoffDate,
                  color = Color(0xCCFFFFFF),
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Normal,
                  fontFamily = ThmanyahFontFamily
                )
              }
            }

            // Left side (END in RTL): Tournament Logo
            val logoUrl = if (match.tournamentLogo.isNotBlank()) match.tournamentLogo else tournamentStyle.logoUrl
            if (logoUrl.isNotBlank()) {
              AsyncImage(
                model = logoUrl,
                contentDescription = match.tournament,
                modifier = Modifier
                  .size(26.dp)
                  .padding(1.dp),
                contentScale = ContentScale.Fit
              )
            } else {
              Icon(
                Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }

        // Divider 1
        Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(Color(0x18FFFFFF)))

        // Band 2: Team 1 Row (Right: Crest + Team Name, Left: Score)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 14.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Right side (START in RTL): Team Crest/Flag + Team Name
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              TodTeamBadgeOrFlag(team = match.homeTeam)
              Text(
                text = match.homeTeam.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ThmanyahFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Left side (END in RTL): Score number (bold white if live or ended, otherwise blank)
            if (match.scoreHome != null && (match.isLive || match.isEnded)) {
              Text(
                text = "${match.scoreHome}",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = ThmanyahFontFamily
              )
            } else {
              Spacer(modifier = Modifier.width(4.dp))
            }
          }
        }

        // Divider 2
        Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(Color(0x18FFFFFF)))

        // Band 3: Team 2 Row (Right: Crest + Team Name, Left: Score)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 14.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Right side (START in RTL): Team Crest/Flag + Team Name
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              TodTeamBadgeOrFlag(team = match.awayTeam)
              Text(
                text = match.awayTeam.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ThmanyahFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Left side (END in RTL): Score number (bold white if live or ended, otherwise blank)
            if (match.scoreAway != null && (match.isLive || match.isEnded)) {
              Text(
                text = "${match.scoreAway}",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = ThmanyahFontFamily
              )
            } else {
              Spacer(modifier = Modifier.width(4.dp))
            }
          }
        }
      }
    }
  }
}

/**
 * 2. Live Football Matches Rail (Screenshots 165527, 165536, 165601)
 * "مباريات كرة القدم بث مباشر"
 */
@Composable
fun TodLiveSportsRail(
  matches: List<SportsMatch>,
  title: String = "مباريات كرة القدم بث مباشر",
  onPlayMatch: (SportsMatch) -> Unit,
  onOpenDetails: (SportsMatch) -> Unit,
  modifier: Modifier = Modifier
) {
  if (matches.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = title,
        color = Color.White,
        fontSize = 18.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Black
      )
      Text(
        text = "${matches.size} مباريات",
        color = Color(0x99FFFFFF),
        fontSize = 12.sp,
        fontFamily = ThmanyahFontFamily
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(matches, key = { it.id }) { match ->
        TodTournamentMatchCard(
          match = match,
          onClick = { onOpenDetails(match) },
          onPlayClick = { onPlayMatch(match) },
          isCompactWidth = true
        )
      }
    }
  }
}

/**
 * 3. Football Fixture Card Wrapper
 */
@Composable
fun TodMatchFixtureCard(
  match: SportsMatch,
  onClick: () -> Unit,
  onPlayClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  TodTournamentMatchCard(
    match = match,
    onClick = onClick,
    onPlayClick = { onPlayClick?.invoke() ?: onClick() },
    modifier = modifier,
    isCompactWidth = true
  )
}

/**
 * 4. Sports Channels Horizontal Rail (Screenshots 14, 16, 17)
 * "قنوات الرياضة والبث المباشر"
 */
@Composable
fun TodSportsChannelsRail(
  channels: List<XtreamChannel>,
  onPlayChannel: (XtreamChannel) -> Unit,
  modifier: Modifier = Modifier
) {
  if (channels.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "قنوات الرياضة والبث المباشر",
      color = Color.White,
      fontSize = 17.5.sp,
      fontFamily = AppFontFamily,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(channels, key = { it.streamId }) { channel ->
        Box(
          modifier = Modifier
            .width(140.dp)
            .height(134.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(TodCardGradient)
            .border(1.dp, TodSpecularBorder, RoundedCornerShape(18.dp))
            .clickable { onPlayChannel(channel) }
            .padding(10.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Channel Logo / Badge
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x20FFFFFF))
                .border(0.75.dp, Color(0x30FFFFFF), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              if (!channel.iconUrl.isNullOrBlank()) {
                AsyncImage(
                  model = channel.iconUrl,
                  contentDescription = channel.name,
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                  contentScale = ContentScale.Fit
                )
              } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TodGold, modifier = Modifier.size(24.dp))
              }
            }

            Text(
              text = channel.name,
              color = Color.White,
              fontSize = 12.sp,
              fontFamily = AppFontFamily,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            // Live Pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE50914))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text("مباشر HD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
            }
          }
        }
      }
    }
  }
}

/**
 * 5. Competitions & Tournaments Rail (Screenshots 10, 16, 18)
 * "المنافسات"
 */
@Composable
fun TodCompetitionsRail(
  competitions: List<SportsCompetition>,
  onSelectCompetition: (SportsCompetition) -> Unit,
  modifier: Modifier = Modifier
) {
  if (competitions.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "المنافسات والبطولات",
      color = Color.White,
      fontSize = 17.5.sp,
      fontFamily = AppFontFamily,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(competitions, key = { it.id }) { comp ->
        Box(
          modifier = Modifier
            .width(135.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
              Brush.verticalGradient(
                listOf(Color(comp.accentColorHex), Color(0xFF0F1222), Color(0xFF080912))
              )
            )
            .border(1.dp, TodSpecularBorder, RoundedCornerShape(18.dp))
            .clickable { onSelectCompetition(comp) }
            .padding(12.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            if (comp.logoUrl.isNotBlank()) {
              AsyncImage(
                model = comp.logoUrl,
                contentDescription = comp.name,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
              )
            } else {
              Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Column {
              Text(
                text = comp.name,
                color = Color.White,
                fontSize = 12.5.sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = comp.season,
                color = Color(0xCCFFFFFF),
                fontSize = 10.sp,
                fontFamily = AppFontFamily
              )
            }
          }
        }
      }
    }
  }
}

/**
 * 6. Sports Highlights and Replays Rail (Screenshots 15, 17, 18)
 * "إعادات وملخصات كرة القدم"
 */
@Composable
fun TodSportsShowsRail(
  shows: List<SportsShow>,
  onPlayShow: (SportsShow) -> Unit,
  modifier: Modifier = Modifier
) {
  if (shows.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "إعادات وملخصات كرة القدم",
      color = Color.White,
      fontSize = 17.5.sp,
      fontFamily = AppFontFamily,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(shows, key = { it.id }) { show ->
        Box(
          modifier = Modifier
            .width(200.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(TodCardGradient)
            .border(1.dp, TodSpecularBorder, RoundedCornerShape(18.dp))
            .clickable { onPlayShow(show) }
            .padding(12.dp)
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
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x30FFFFFF))
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(show.duration, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
              }

              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TodGold, modifier = Modifier.size(20.dp))
            }

            Column {
              Text(
                text = show.title,
                color = Color.White,
                fontSize = 13.5.sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Black
              )
              Text(
                text = show.subtitle,
                color = DarkTextSecondary,
                fontSize = 10.5.sp,
                fontFamily = AppFontFamily,
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
  TodTournamentMatchCard(
    match = match,
    onClick = { onOpenDetails(match) },
    onPlayClick = { onPlayMatch(match) },
    modifier = modifier,
    isCompactWidth = false
  )
}

/**
 * iOS-Fidelity Corporate Channel Card for Grid View
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
            SubcomposeAsyncImage(
              model = channel.iconUrl,
              contentDescription = null,
              modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
              contentScale = ContentScale.Fit,
              error = {
                TodDrawnOfficialChannelLogo(channelName = channel.name, modifier = Modifier.size(32.dp))
              },
              loading = {
                TodDrawnOfficialChannelLogo(channelName = channel.name, modifier = Modifier.size(32.dp))
              }
            )
          } else {
            TodDrawnOfficialChannelLogo(channelName = channel.name, modifier = Modifier.size(32.dp))
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
            SubcomposeAsyncImage(
              model = channel.iconUrl,
              contentDescription = null,
              modifier = Modifier.fillMaxSize().padding(3.dp),
              contentScale = ContentScale.Fit,
              error = {
                TodDrawnOfficialChannelLogo(channelName = channel.name, modifier = Modifier.size(28.dp))
              },
              loading = {
                TodDrawnOfficialChannelLogo(channelName = channel.name, modifier = Modifier.size(28.dp))
              }
            )
          } else {
            TodDrawnOfficialChannelLogo(channelName = channel.name, modifier = Modifier.size(28.dp))
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
