package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
 * High-resolution Team Crest Component (Supports real logo URL from server dashboard + flag emoji fallback)
 */
@Composable
fun TodTeamCrest(
  team: SportsTeam,
  size: Dp = 54.dp,
  borderGlowColor: Color = Color(0xFF64D2FF),
  modifier: Modifier = Modifier
) {
  if (!team.logoUrl.isNullOrBlank()) {
    Box(
      modifier = modifier.size(size),
      contentAlignment = Alignment.Center
    ) {
      AsyncImage(
        model = team.logoUrl,
        contentDescription = team.name,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
      )
    }
  } else {
    Box(
      modifier = modifier
        .size(size)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(borderGlowColor.copy(alpha = 0.35f), Color(0x18FFFFFF))
          )
        )
        .border(1.2.dp, borderGlowColor.copy(alpha = 0.6f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = team.flagEmoji,
        fontSize = (size.value * 0.46f).sp
      )
    }
  }
}

/**
 * 1. Hero Match Connected Banner (100% Identical to TOD Screenshot)
 * Seamlessly connects with header, displaying key players background,
 * Home/Away crests, score, minute pill, Arabic RTL details, 3 bottom glass action buttons, and carousel dots.
 */
@Composable
fun TodMatchCountdownHero(
  match: SportsMatch,
  onPlayMatch: (SportsMatch) -> Unit,
  onOpenDetails: (SportsMatch) -> Unit,
  modifier: Modifier = Modifier
) {
  val playInteraction = remember { MutableInteractionSource() }
  val isPlayPressed by playInteraction.collectIsPressedAsState()
  val playScale by animateFloatAsState(
    targetValue = if (isPlayPressed) 0.95f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "heroPlayScale"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .liquidGlassEffect(
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        isElevated = true,
        glowTint = Color(0xFF0A84FF),
        borderBrush = LiquidGlassTheme.LiquidSpecularBorder
      )
  ) {
    // Backdrop Cinematic Composite (Players + Stadium atmosphere)
    val heroBackdrop = if (!match.bannerUrl.isNullOrBlank()) match.bannerUrl else "android.resource://com.example/drawable/tod_hero_match_banner"
    AsyncImage(
      model = heroBackdrop,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxWidth()
        .height(490.dp)
        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
    )

    // Deep smooth glass overlay gradient fading down
    Box(
      modifier = Modifier
        .matchParentSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0x50000000),
              Color(0x20050E20),
              Color(0x80070B16),
              Color(0xE007090E),
              Color(0xFA07090E)
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 105.dp, bottom = 16.dp)
    ) {
      // Top Header: Right "رياضة ->"
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x25000000))
            .padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "رياضة",
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(60.dp))

      // Duel Row: Home Crest [Left]  Score & Minute [Center]  Away Crest [Right]
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Home Team Crest (Left)
        TodTeamCrest(
          team = match.homeTeam,
          size = 68.dp,
          borderGlowColor = Color(0xFF64D2FF)
        )

        // Center Score & Live Minute Pill
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (match.scoreHome != null && match.scoreAway != null) {
            Text(
              text = "${match.scoreHome}  -  ${match.scoreAway}",
              color = Color.White,
              fontSize = 34.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp
            )
          } else {
            Text(
              text = match.kickoffTime,
              color = Color.White,
              fontSize = 26.sp,
              fontWeight = FontWeight.Black
            )
          }

          // Minute Pill (e.g. '70)
          val displayMinute = match.liveMinute ?: if (match.isLive) "'70" else null
          if (displayMinute != null) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x80000000))
                .border(0.75.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
              Text(
                text = displayMinute,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        }

        // Away Team Crest (Right)
        TodTeamCrest(
          team = match.awayTeam,
          size = 68.dp,
          borderGlowColor = Color(0xFFBF5AF2)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Match Title (Bold White, RTL aligned)
      Text(
        text = match.title,
        color = Color.White,
        fontSize = 21.sp,
        fontWeight = FontWeight.Black,
        fontFamily = ThmanyahFontFamily,
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Match Subtitle Metadata: "٣٠ سبتمبر • ١٩:٤٥ • Stadio Tre Fontane • دوري أبطال أوروبا"
      Text(
        text = "${match.kickoffDate} • ${match.kickoffTime} • ${match.stadium.ifBlank { "الملعب الرئيسي" }} • ${match.tournament}",
        color = Color(0xFFB5BAC9),
        fontSize = 12.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Right,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Live Red Pill Badge (RTL aligned on right)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        if (match.isLive) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFFE50914))
              .padding(horizontal = 10.dp, vertical = 3.dp)
          ) {
            Text(
              text = "مباشر",
              color = Color.White,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Black,
              fontFamily = ThmanyahFontFamily
            )
          }
        } else if (!match.countdownText.isNullOrBlank()) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0x30FFFFFF))
              .border(0.75.dp, Color(0x40FFFFFF), RoundedCornerShape(6.dp))
              .padding(horizontal = 10.dp, vertical = 3.dp)
          ) {
            Text(
              text = match.countdownText,
              color = TodGold,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = ThmanyahFontFamily
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Bottom Action Bar (3 Buttons: [+] Add | ↺ Replay | ▶ تابع الآن Golden Liquid Glass)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // 1. [+] Add to watchlist / My TOD button (Settings Liquid Glass)
        Box(
          modifier = Modifier
            .iosBounceClick(scaleDown = 0.90f, onClick = { onOpenDetails(match) })
            .size(50.dp)
            .liquidGlassEffect(
              shape = RoundedCornerShape(16.dp),
              isElevated = true,
              glowTint = Color(0xFF0A84FF),
              borderBrush = LiquidGlassTheme.LiquidBlueBorder
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AddBox,
            contentDescription = "إضافة للمفضلة",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        // 2. ↺ Replay / Restart button (Settings Liquid Glass)
        Box(
          modifier = Modifier
            .iosBounceClick(scaleDown = 0.90f, onClick = { onPlayMatch(match) })
            .size(50.dp)
            .liquidGlassEffect(
              shape = RoundedCornerShape(16.dp),
              isElevated = true,
              glowTint = Color(0xFFBF5AF2),
              borderBrush = LiquidGlassTheme.LiquidPurpleBorder
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Replay,
            contentDescription = "إعادة البث",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        // 3. ▶ Golden Liquid Glass Wide "تابع الآن" Button (Settings Glowing Gold Sheen)
        Box(
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .iosBounceClick(scaleDown = 0.95f, onClick = { onPlayMatch(match) })
            .liquidGlassEffect(
              shape = RoundedCornerShape(16.dp),
              isElevated = true,
              glowTint = TodGold,
              glassColor = Color(0xFFFDB913),
              borderBrush = LiquidGlassTheme.LiquidGoldBorder
            ),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = Color.Black,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "تابع الآن",
              color = Color.Black,
              fontSize = 16.5.sp,
              fontFamily = ThmanyahFontFamily,
              fontWeight = FontWeight.Black
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Carousel Indicator Dots (• • ── • • •)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0x55FFFFFF)))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0x55FFFFFF)))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.width(22.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0x55FFFFFF)))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0x55FFFFFF)))
      }
    }
  }
}

/**
 * 2. Live Multi-Sports Horizontal Rail (Screenshots 4, 11, 14)
 * "البث المباشر - رياضات متعددة"
 */
@Composable
fun TodLiveSportsRail(
  matches: List<SportsMatch>,
  onPlayMatch: (SportsMatch) -> Unit,
  onOpenDetails: (SportsMatch) -> Unit,
  modifier: Modifier = Modifier
) {
  if (matches.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "البث المباشر - رياضات متعددة",
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
      items(matches, key = { it.id }) { match ->
        Box(
          modifier = Modifier
            .width(285.dp)
            .height(156.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(TodCardGradient)
            .border(1.dp, TodSpecularBorder, RoundedCornerShape(22.dp))
            .clickable { onOpenDetails(match) }
            .padding(14.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            // Header Row: Tournament + Red Pulsing "مباشر" Badge
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (match.tournamentLogo.isNotBlank()) {
                  AsyncImage(
                    model = match.tournamentLogo,
                    contentDescription = match.tournament,
                    modifier = Modifier.size(16.dp),
                    contentScale = ContentScale.Fit
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                  text = match.tournament,
                  color = Color(0xFFAAAAAA),
                  fontSize = 11.5.sp,
                  fontFamily = AppFontFamily,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFE50914))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(7.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("مباشر", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = AppFontFamily)
                }
              }
            }

            // Middle: Match Title + Score with team logos
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                TodTeamCrest(team = match.homeTeam, size = 30.dp)
                Spacer(modifier = Modifier.width(6.dp))
                TodTeamCrest(team = match.awayTeam, size = 30.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = match.title,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  if (match.liveMinute != null) {
                    Text(match.liveMinute, color = TodGold, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                  }
                }
              }

              if (match.scoreHome != null && match.scoreAway != null) {
                Text(
                  text = "${match.scoreHome} - ${match.scoreAway}",
                  color = Color.White,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }

            // Bottom: Quick Play Button + Channel
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tv, contentDescription = null, tint = Color(0xFF64D2FF), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = match.channelName,
                  color = Color(0xFF9EA3B5),
                  fontSize = 11.5.sp,
                  fontFamily = AppFontFamily
                )
              }

              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(TodGold)
                  .clickable { onPlayMatch(match) },
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
              }
            }
          }
        }
      }
    }
  }
}

/**
 * 3. Upcoming Football Fixture Cards (Screenshots 12, 13, 15)
 * Official TOD Fixture Card with Team Crests, Tournament Badge, and Luxury Dark Gradients.
 */
@Composable
fun TodMatchFixtureCard(
  match: SportsMatch,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .width(285.dp)
      .height(185.dp)
      .clip(RoundedCornerShape(22.dp))
      .background(TodCardGradient)
      .border(1.dp, TodSpecularBorder, RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .padding(14.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Row: Tournament Title & Match Status / Kickoff
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (match.tournamentLogo.isNotBlank()) {
            AsyncImage(
              model = match.tournamentLogo,
              contentDescription = match.tournament,
              modifier = Modifier.size(16.dp),
              contentScale = ContentScale.Fit
            )
          } else {
            Icon(
              Icons.Default.SportsSoccer,
              contentDescription = null,
              tint = Color(0xFF64D2FF),
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = match.tournament,
            color = Color(0xFFCCCCCC),
            fontSize = 11.5.sp,
            fontFamily = AppFontFamily,
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
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(6.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text("مباشر ${match.liveMinute ?: ""}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
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
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = match.kickoffDate,
              color = Color(0x99FFFFFF),
              fontSize = 10.5.sp,
              fontFamily = AppFontFamily
            )
          }
        }
      }

      // Middle: Teams Duel Rows (Official TOD style with Team Crests)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Home Team Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TodTeamCrest(
              team = match.homeTeam,
              size = 36.dp,
              borderGlowColor = Color(0xFF64D2FF)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = match.homeTeam.name,
              color = Color.White,
              fontSize = 14.5.sp,
              fontFamily = AppFontFamily,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          if (match.scoreHome != null) {
            Text(
              text = "${match.scoreHome}",
              color = if (match.isLive) Color(0xFF64D2FF) else Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black
            )
          }
        }

        // Away Team Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TodTeamCrest(
              team = match.awayTeam,
              size = 36.dp,
              borderGlowColor = Color(0xFFBF5AF2)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = match.awayTeam.name,
              color = Color.White,
              fontSize = 14.5.sp,
              fontFamily = AppFontFamily,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          if (match.scoreAway != null) {
            Text(
              text = "${match.scoreAway}",
              color = if (match.isLive) Color(0xFF64D2FF) else Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black
            )
          }
        }
      }

      // Bottom Row: Channel & Commentator Information Pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0x15FFFFFF))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "📺 ${match.channelName}",
          color = Color(0xFF64D2FF),
          fontSize = 10.5.sp,
          fontFamily = AppFontFamily,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "🎙️ ${match.commentator}",
          color = Color(0xFFB0B0C0),
          fontSize = 10.sp,
          fontFamily = AppFontFamily
        )
      }
    }
  }
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
