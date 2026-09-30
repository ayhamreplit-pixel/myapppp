package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastStream
import com.example.model.SportsMatch
import com.example.ui.theme.AppFontFamily
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodGold

/**
 * Luxury Dark Gradients matching the original app design.
 */
private val ModalBgGradient = Brush.verticalGradient(
  listOf(
    Color(0xFF141728),
    Color(0xFF0D0F1B),
    Color(0xFF06070E)
  )
)

private val ModalHeroGradient = Brush.verticalGradient(
  listOf(
    Color(0xFF1E2238),
    Color(0xFF131526),
    Color(0xFF090A14)
  )
)

private val ModalCardGradient = Brush.verticalGradient(
  listOf(
    Color(0xFF181B2E),
    Color(0xFF101220),
    Color(0xFF0A0B14)
  )
)

private val ModalSpecularBorder = Brush.linearGradient(
  listOf(
    Color(0x4564D2FF),
    Color(0x35BF5AF2),
    Color(0x20FFFFFF)
  )
)

/**
 * TOD Match Detail Modal & Full-screen Experience (Screenshots 19, 20, 21, 22, 23, 24)
 * Arranged with the luxury gradients matching the original design.
 */
@Composable
fun TodMatchDetailModal(
  match: SportsMatch,
  onClose: () -> Unit,
  onPlayStream: (BroadcastStream) -> Unit,
  onSelectOtherMatch: (SportsMatch) -> Unit = {},
  relatedMatches: List<SportsMatch> = emptyList(),
  modifier: Modifier = Modifier
) {
  var isFavorite by remember { mutableStateOf(match.isFavorite) }
  var selectedTab by remember { mutableStateOf(0) } // 0 = Info, 1 = Lineup, 2 = Stats, 3 = Standings, 4 = H2H

  val tabTitles = listOf("تفاصيل اللقاء", "التشكيلة والخطة", "إحصائيات اللقاء", "الترتيب المباشر", "وجهاً لوجه")

  Surface(
    modifier = modifier.fillMaxSize(),
    color = Color.Transparent
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(ModalBgGradient)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(bottom = 36.dp)
      ) {
        // 1. Top Header with Back / Close Button & Match Title
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onClose,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color(0x33FFFFFF))
              .border(1.dp, Color(0x35FFFFFF), CircleShape)
          ) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(20.dp))
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = match.tournament,
              color = Color(0xFF64D2FF),
              fontSize = 12.sp,
              fontFamily = AppFontFamily,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = match.title,
              color = Color.White,
              fontSize = 16.sp,
              fontFamily = AppFontFamily,
              fontWeight = FontWeight.Black
            )
          }

          IconButton(
            onClick = { isFavorite = !isFavorite },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color(0x33FFFFFF))
              .border(1.dp, Color(0x35FFFFFF), CircleShape)
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Default.Check else Icons.Default.Add,
              contentDescription = "My TOD",
              tint = if (isFavorite) TodGold else Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Match Hero Banner (Stadium/Player Backdrop + Team Badges in Luxury Gradients)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(ModalHeroGradient)
            .border(1.2.dp, ModalSpecularBorder, RoundedCornerShape(26.dp))
            .padding(20.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Live / Status Badge
            if (match.isLive) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFE50914))
                  .padding(horizontal = 12.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = match.liveMinute ?: "مباشر الآن",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = AppFontFamily
                  )
                }
              }
              Spacer(modifier = Modifier.height(14.dp))
            } else if (match.countdownText != null) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x30FFFFFF))
                  .border(0.75.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
                  .padding(horizontal = 12.dp, vertical = 4.dp)
              ) {
                Text(
                  text = match.countdownText,
                  color = TodGold,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = AppFontFamily
                )
              }
              Spacer(modifier = Modifier.height(14.dp))
            }

            // Teams Row (Flag / Logo vs Flag / Logo with Glowing Ambient Rings)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Home Team
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TodTeamCrest(
                  team = match.homeTeam,
                  size = 66.dp,
                  borderGlowColor = Color(0xFF64D2FF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = match.homeTeam.name,
                  color = Color.White,
                  fontSize = 16.sp,
                  fontFamily = AppFontFamily,
                  fontWeight = FontWeight.Black
                )
              }

              // Score or VS
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (match.scoreHome != null && match.scoreAway != null) {
                  Text(
                    text = "${match.scoreHome} - ${match.scoreAway}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                  )
                  if (match.liveMinute != null) {
                    Text(
                      text = match.liveMinute,
                      color = TodGold,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = AppFontFamily
                    )
                  }
                } else {
                  Text(
                    text = "ضد",
                    color = TodGold,
                    fontSize = 18.sp,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Black
                  )
                  Text(
                    text = match.kickoffTime,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                  )
                }
              }

              // Away Team
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TodTeamCrest(
                  team = match.awayTeam,
                  size = 66.dp,
                  borderGlowColor = Color(0xFFBF5AF2)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = match.awayTeam.name,
                  color = Color.White,
                  fontSize = 16.sp,
                  fontFamily = AppFontFamily,
                  fontWeight = FontWeight.Black
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Match Tournament & Venue Metadata Pill
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x20FFFFFF))
                .padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "📺 ${match.channelName}",
                color = Color(0xFF64D2FF),
                fontSize = 12.sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold
              )
              Text("•", color = Color(0x60FFFFFF), fontSize = 11.sp)
              Text(
                text = "🎙️ ${match.commentator}",
                color = Color(0xFFE0E0EC),
                fontSize = 12.sp,
                fontFamily = AppFontFamily
              )
              Text("•", color = Color(0x60FFFFFF), fontSize = 11.sp)
              Text(
                text = "🏟️ ${match.stadium.ifBlank { "الملعب الرئيسي" }}",
                color = Color(0xFFCCCCCC),
                fontSize = 11.5.sp,
                fontFamily = AppFontFamily
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Primary Play Live Stream Button (Official Yellow/Gold TOD Button)
            val playInteraction = remember { MutableInteractionSource() }
            val isPlayPressed by playInteraction.collectIsPressedAsState()
            val playScale by animateFloatAsState(
              targetValue = if (isPlayPressed) 0.94f else 1.0f,
              animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
              label = "matchPlayScale"
            )

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .scale(playScale)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                  Brush.horizontalGradient(
                    listOf(TodGold, Color(0xFFFF9500), Color(0xFFFF7A00))
                  )
                )
                .clickable(
                  interactionSource = playInteraction,
                  indication = null,
                  onClick = {
                    val stream = BroadcastStream(
                      id = match.id,
                      title = match.title,
                      subtitle = "${match.tournament} • ${match.channelName}",
                      category = match.tournament,
                      streamUrl = match.streamUrl.ifBlank { "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" },
                      isLive = true
                    )
                    onPlayStream(stream)
                  }
                ),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (match.isLive) "مشاهدة البث المباشر (FHD)" else "تشغيل البث المباشر للقناة",
                  color = Color.Black,
                  fontSize = 15.sp,
                  fontFamily = AppFontFamily,
                  fontWeight = FontWeight.Black
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary: Add to My TOD / Favorites
            Button(
              onClick = { isFavorite = !isFavorite },
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x22FFFFFF),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Check else Icons.Default.Add,
                  contentDescription = null,
                  tint = if (isFavorite) TodGold else Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isFavorite) "المباراة مضافة في مفضلتي" else "إضافة إلى مفضلتي My TOD +",
                  fontSize = 13.5.sp,
                  fontFamily = AppFontFamily,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Tab Selectors with Luxury Styling
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(tabTitles.size) { index ->
            val title = tabTitles[index]
            val isSelected = selectedTab == index
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                  if (isSelected) {
                    Brush.horizontalGradient(listOf(TodGold, Color(0xFFFF9500)))
                  } else {
                    Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x10FFFFFF)))
                  }
                )
                .border(
                  1.dp,
                  if (isSelected) TodGold else Color(0x25FFFFFF),
                  RoundedCornerShape(14.dp)
                )
                .clickable { selectedTab = index }
                .padding(horizontal = 14.dp, vertical = 9.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 12.5.sp,
                fontFamily = AppFontFamily,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                textAlign = TextAlign.Center
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Tab Content Panel in the exact same luxury dark gradient!
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ModalCardGradient)
            .border(1.dp, ModalSpecularBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
        ) {
          when (selectedTab) {
            0 -> {
              // Match Info (تفاصيل اللقاء)
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                Text("معلومات وتفاصيل النقل التلفزيوني", color = TodGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)

                InfoRow("البطولة والمنافسة", match.tournament)
                InfoRow("القناة الناقلة للبث", match.channelName)
                InfoRow("المعلق الرياضي", match.commentator)
                InfoRow("الملعب المستضيف", match.stadium.ifBlank { "الملعب الرئيسي" })
                InfoRow("تاريخ ووقت اللقاء", "${match.kickoffDate} - ${match.kickoffTime}")
                InfoRow("حالة الخادم السحابي", "متصل بسيرفر TOD الرياضي (https://ayham.alwaysdata.net)")
                InfoRow("بروتوكول التشفير", "AES-256 مشفر آمن")
                InfoRow("جودة البث الحي", "Full HD 1080p @ 50fps / صوت دولبي محيطي")
              }
            }

            1 -> {
              // Lineups (التشكيلة والخطة)
              val lineups = match.lineups
              if (lineups != null) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("${match.homeTeam.name} (${lineups.formationHome})", color = TodGold, fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = AppFontFamily)
                    Text("${match.awayTeam.name} (${lineups.formationAway})", color = Color(0xFF64D2FF), fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = AppFontFamily)
                  }
                  Text("المدرب: ${lineups.coachHome} • ${lineups.coachAway}", color = DarkTextSecondary, fontSize = 12.sp, fontFamily = AppFontFamily)

                  Spacer(modifier = Modifier.height(4.dp))
                  Text("التشكيلة الرسمية الأساسية:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, fontFamily = AppFontFamily)

                  lineups.startersHome.forEachIndexed { idx, p ->
                    val awayP = lineups.startersAway.getOrNull(idx) ?: ""
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (idx % 2 == 0) Color(0x10FFFFFF) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text("• $p", color = Color(0xFFEEEEEE), fontSize = 12.5.sp, fontFamily = AppFontFamily)
                      Text("• $awayP", color = Color(0xFFD0D0D8), fontSize = 12.5.sp, fontFamily = AppFontFamily)
                    }
                  }
                }
              } else {
                NoDataState("سيتم الإعلان عن التشكيلة الرسمية قبل ساعة من انطلاق اللقاء")
              }
            }

            2 -> {
              // Match Stats (إحصائيات اللقاء)
              val stats = match.stats
              if (stats != null) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(match.homeTeam.name, color = Color(0xFF64D2FF), fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = AppFontFamily)
                    Text("الإحصائيات المباشرة", color = TodGold, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppFontFamily)
                    Text(match.awayTeam.name, color = Color(0xFFBF5AF2), fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = AppFontFamily)
                  }

                  StatBarRow("الاستحواذ على الكرة %", "${stats.possessionHome}%", "${stats.possessionAway}%", stats.possessionHome / 100f)
                  StatBarRow("التسديدات على المرمى", "${stats.shotsOnTargetHome}", "${stats.shotsOnTargetAway}", calculateRatio(stats.shotsOnTargetHome, stats.shotsOnTargetAway))
                  StatBarRow("إجمالي التسديدات", "${stats.totalShotsHome}", "${stats.totalShotsAway}", calculateRatio(stats.totalShotsHome, stats.totalShotsAway))
                  StatBarRow("الضربات الركنية", "${stats.cornersHome}", "${stats.cornersAway}", calculateRatio(stats.cornersHome, stats.cornersAway))
                  StatBarRow("الأخطاء المرتكبة", "${stats.foulsHome}", "${stats.foulsAway}", calculateRatio(stats.foulsHome, stats.foulsAway))
                  StatBarRow("البطاقات الصفراء", "${stats.yellowCardsHome}", "${stats.yellowCardsAway}", calculateRatio(stats.yellowCardsHome, stats.yellowCardsAway))
                }
              } else {
                NoDataState("إحصائيات المباراة المباشرة تتوفر فور انطلاق صافرة البداية")
              }
            }

            3 -> {
              // Standings (الترتيب المباشر)
              if (match.standings.isNotEmpty()) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0x20FFFFFF))
                      .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("الفريق", color = Color(0xFFB0B0C0), fontSize = 11.5.sp, fontFamily = AppFontFamily, modifier = Modifier.weight(2.5f))
                    Text("لعب", color = Color(0xFFB0B0C0), fontSize = 11.5.sp, fontFamily = AppFontFamily, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("فرق", color = Color(0xFFB0B0C0), fontSize = 11.5.sp, fontFamily = AppFontFamily, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("نقاط", color = TodGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                  }
                  match.standings.forEach { row ->
                    val isCurrentTeam = row.teamName.contains(match.homeTeam.name, ignoreCase = true) ||
                      row.teamName.contains(match.awayTeam.name, ignoreCase = true)

                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCurrentTeam) Color(0x2564D2FF) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        "${row.position}. ${row.teamName}",
                        color = if (isCurrentTeam) Color(0xFF64D2FF) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isCurrentTeam) FontWeight.Black else FontWeight.SemiBold,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.weight(2.5f)
                      )
                      Text("${row.played}", color = Color(0xFFCCCCCC), fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                      Text("${row.goalDiff}", color = Color(0xFFCCCCCC), fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                      Text("${row.points}", color = TodGold, fontSize = 13.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    }
                  }
                }
              } else {
                NoDataState("لا يتوفر جدول ترتيب حالي لهذه المنافسة")
              }
            }

            4 -> {
              // Head to head (وجهاً لوجه)
              if (match.h2h.isNotEmpty()) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Text("نتائج المواجهات السابقة بين الفريقين", color = TodGold, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                  match.h2h.forEach { item ->
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x15FFFFFF))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(item.competition, color = DarkTextSecondary, fontSize = 11.sp, fontFamily = AppFontFamily)
                        Text(item.date, color = Color(0x99FFFFFF), fontSize = 10.sp, fontFamily = AppFontFamily)
                      }
                      Text(
                        text = item.score,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Black
                      )
                      Text(
                        text = "الفائز: ${item.winnerTeam ?: "تعادل"}",
                        color = Color(0xFF64D2FF),
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                  }
                }
              } else {
                NoDataState("لا تتوفر مواجهات مباشرة مسجلة سابقة بين الفريقين")
              }
            }
          }
        }

        // 5. Related matches carousel if available
        if (relatedMatches.isNotEmpty()) {
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "مباريات أخرى في نفس البطولة",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
          )

          LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(relatedMatches.take(6), key = { it.id }) { otherMatch ->
              Box(
                modifier = Modifier
                  .width(240.dp)
                  .clip(RoundedCornerShape(18.dp))
                  .background(ModalCardGradient)
                  .border(1.dp, ModalSpecularBorder, RoundedCornerShape(18.dp))
                  .clickable { onSelectOtherMatch(otherMatch) }
                  .padding(12.dp)
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(otherMatch.kickoffTime, color = TodGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text(otherMatch.kickoffDate, color = Color(0x80FFFFFF), fontSize = 10.sp, fontFamily = AppFontFamily)
                  }
                  Text(
                    text = "${otherMatch.homeTeam.name} vs ${otherMatch.awayTeam.name}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "📺 ${otherMatch.channelName}",
                    color = Color(0xFF64D2FF),
                    fontSize = 10.5.sp,
                    fontFamily = AppFontFamily
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

@Composable
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0x10FFFFFF))
      .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, color = Color(0xFFA0A5BA), fontSize = 12.sp, fontFamily = AppFontFamily)
    Text(value, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
  }
}

@Composable
private fun StatBarRow(label: String, homeVal: String, awayVal: String, ratio: Float) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(homeVal, color = Color(0xFF64D2FF), fontSize = 12.5.sp, fontWeight = FontWeight.Black)
      Text(label, color = Color(0xFFE0E0EC), fontSize = 12.sp, fontFamily = AppFontFamily)
      Text(awayVal, color = Color(0xFFBF5AF2), fontSize = 12.5.sp, fontWeight = FontWeight.Black)
    }

    // Gradient Double-Progress Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(Color(0x25FFFFFF))
    ) {
      Row(modifier = Modifier.fillMaxSize()) {
        Box(
          modifier = Modifier
            .weight(ratio.coerceIn(0.05f, 0.95f))
            .fillMaxHeight()
            .background(
              Brush.horizontalGradient(
                listOf(Color(0xFF0A84FF), Color(0xFF64D2FF))
              )
            )
        )
        Box(
          modifier = Modifier
            .weight((1f - ratio).coerceIn(0.05f, 0.95f))
            .fillMaxHeight()
            .background(
              Brush.horizontalGradient(
                listOf(Color(0xFFBF5AF2), Color(0xFFFF2D55))
              )
            )
        )
      }
    }
  }
}

private fun calculateRatio(v1: Int, v2: Int): Float {
  val sum = (v1 + v2).toFloat()
  return if (sum <= 0f) 0.5f else (v1 / sum).coerceIn(0.1f, 0.9f)
}

@Composable
private fun NoDataState(message: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 24.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = message,
      color = Color(0x88FFFFFF),
      fontSize = 12.5.sp,
      fontFamily = AppFontFamily,
      textAlign = TextAlign.Center
    )
  }
}
