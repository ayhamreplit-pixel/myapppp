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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
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
  size: Dp = 44.dp,
  borderGlowColor: Color = Color(0xFF64D2FF),
  modifier: Modifier = Modifier
) {
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
    if (team.logoUrl.isNotBlank()) {
      AsyncImage(
        model = team.logoUrl,
        contentDescription = team.name,
        modifier = Modifier
          .fillMaxSize()
          .padding(size * 0.12f),
        contentScale = ContentScale.Fit
      )
    } else {
      Text(
        text = team.flagEmoji,
        fontSize = (size.value * 0.46f).sp
      )
    }
  }
}

/**
 * 1. Hero Match Countdown Banner (Screenshots 11, 15)
 * Official TOD Hero match with team crests, live/countdown badges, and gold watch button.
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
    targetValue = if (isPlayPressed) 0.94f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "heroPlayScale"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(26.dp))
      .background(TodHeroGradient)
      .border(1.2.dp, TodSpecularBorder, RoundedCornerShape(26.dp))
      .clickable { onOpenDetails(match) }
  ) {
    // Backdrop poster if available
    if (!match.bannerUrl.isNullOrBlank()) {
      AsyncImage(
        model = match.bannerUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .matchParentSize()
          .clip(RoundedCornerShape(26.dp))
      )
      Box(
        modifier = Modifier
          .matchParentSize()
          .background(
            Brush.verticalGradient(
              listOf(Color(0xCC1F243A), Color(0xF0131626), Color(0xFA080A12))
            )
          )
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Status & Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (match.isLive) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFE50914))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("مباشر الآن", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = AppFontFamily)
            }
          }
        } else if (match.countdownText != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0x30FFFFFF))
              .border(0.75.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(match.countdownText, color = TodGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
          }
        } else {
          Spacer(modifier = Modifier.size(1.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (match.tournamentLogo.isNotBlank()) {
            AsyncImage(
              model = match.tournamentLogo,
              contentDescription = match.tournament,
              modifier = Modifier.size(18.dp),
              contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(6.dp))
          }
          Text(
            text = match.tournament,
            color = Color(0xFFD0D0E0),
            fontSize = 12.sp,
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Teams Crests / Flags Row (Large duel format matching TOD)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Home Team
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          TodTeamCrest(
            team = match.homeTeam,
            size = 60.dp,
            borderGlowColor = Color(0xFF64D2FF)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = match.homeTeam.name,
            color = Color.White,
            fontSize = 15.sp,
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Black
          )
        }

        // Center: Live Score or VS / Kickoff Time
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          if (match.scoreHome != null && match.scoreAway != null) {
            Text(
              text = "${match.scoreHome} - ${match.scoreAway}",
              color = Color.White,
              fontSize = 28.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp
            )
            if (match.liveMinute != null) {
              Text(match.liveMinute, color = TodGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          } else {
            Text(
              text = "ضد",
              color = TodGold,
              fontSize = 15.sp,
              fontFamily = AppFontFamily,
              fontWeight = FontWeight.Black
            )
            Text(
              text = match.kickoffTime,
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black
            )
          }
        }

        // Away Team
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          TodTeamCrest(
            team = match.awayTeam,
            size = 60.dp,
            borderGlowColor = Color(0xFFBF5AF2)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = match.awayTeam.name,
            color = Color.White,
            fontSize = 15.sp,
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Black
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Match Subtitle (Venue & Channel)
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0x18FFFFFF))
          .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "📺 ${match.channelName}",
          color = Color(0xFF64D2FF),
          fontSize = 11.5.sp,
          fontFamily = AppFontFamily,
          fontWeight = FontWeight.Bold
        )
        Text("•", color = Color(0x60FFFFFF), fontSize = 11.sp)
        Text(
          text = "🎙️ ${match.commentator}",
          color = Color(0xFFE0E0EC),
          fontSize = 11.5.sp,
          fontFamily = AppFontFamily
        )
        Text("•", color = Color(0x60FFFFFF), fontSize = 11.sp)
        Text(
          text = match.stadium.ifBlank { "الملعب الرئيسي" },
          color = Color(0xFFCCCCCC),
          fontSize = 11.sp,
          fontFamily = AppFontFamily
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Action Buttons (Play Live Button in Gold + My TOD Button)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // My TOD Button
        Box(
          modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0x22FFFFFF))
            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(13.dp))
            .clickable { onOpenDetails(match) }
            .padding(horizontal = 14.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("My TOD", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
          }
        }

        // Primary Play Gold Button
        Box(
          modifier = Modifier
            .weight(1f)
            .scale(playScale)
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
              Brush.horizontalGradient(
                listOf(TodGold, Color(0xFFFF9500), Color(0xFFFF7A00))
              )
            )
            .clickable(
              interactionSource = playInteraction,
              indication = null,
              onClick = { onPlayMatch(match) }
            ),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (match.isLive) "تابع الآن مباشر" else "مشاهدة اللقاء",
              color = Color.Black,
              fontSize = 14.5.sp,
              fontFamily = AppFontFamily,
              fontWeight = FontWeight.Black
            )
          }
        }
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
