package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.BroadcastStream
import com.example.model.XtreamChannel
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.TodButtonGrey
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGradients
import com.example.ui.theme.TodLiveRed
import kotlinx.coroutines.delay

/**
 * Official TOD Logo with "by beIN" subtext
 * Matches TOD official identity (Screenshots 7)
 */
@Composable
fun TodLogo(
  modifier: Modifier = Modifier,
  fontSize: Int = 26,
  showSubtext: Boolean = true
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "TOD",
      color = TodGold,
      fontSize = fontSize.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 2.sp,
      fontFamily = FontFamily.SansSerif
    )
    if (showSubtext) {
      Text(
        text = "by beIN",
        color = TodGold.copy(alpha = 0.85f),
        fontSize = (fontSize * 0.36f).sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
      )
    }
  }
}

/**
 * TOD Bottom Navigation Bar (Screenshots 3, 4, 5, 7)
 * 3 Tabs: الرئيسية (Home), بحث (Search), المزيد (More)
 */
enum class TodNavTab {
  HOME,
  SEARCH,
  MORE
}

@Composable
fun TodBottomNavBar(
  currentTab: TodNavTab,
  onTabSelected: (TodNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp),
    color = Color(0xFF09090C),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B1B20))
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceAround
    ) {
      // 1. More / Profile Tab (المزيد)
      val isMore = currentTab == TodNavTab.MORE
      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable { onTabSelected(TodNavTab.MORE) }
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isMore) TodGold else Color(0xFF26262D)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "المزيد",
            tint = if (isMore) Color.Black else Color(0xFFB0B0BC),
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = "المزيد",
          color = if (isMore) TodGold else DarkTextSecondary,
          fontSize = 11.sp,
          fontWeight = if (isMore) FontWeight.Bold else FontWeight.Normal
        )
      }

      // 2. Search Tab (بحث)
      val isSearch = currentTab == TodNavTab.SEARCH
      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable { onTabSelected(TodNavTab.SEARCH) }
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "بحث",
          tint = if (isSearch) TodGold else DarkTextSecondary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = "بحث",
          color = if (isSearch) TodGold else DarkTextSecondary,
          fontSize = 11.sp,
          fontWeight = if (isSearch) FontWeight.Bold else FontWeight.Normal
        )
      }

      // 3. Home Tab (الرئيسية)
      val isHome = currentTab == TodNavTab.HOME
      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable { onTabSelected(TodNavTab.HOME) }
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Home,
          contentDescription = "الرئيسية",
          tint = if (isHome) TodGold else DarkTextSecondary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = "الرئيسية",
          color = if (isHome) TodGold else DarkTextSecondary,
          fontSize = 11.sp,
          fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
        )
      }
    }
  }
}

/**
 * TOD Match Countdown Timer (Screenshot 2)
 * [10 ساعات] : [23 دقائق] : [42 ثواني] with bold yellow numbers
 */
@Composable
fun TodCountdownTimer(
  initialHours: Int = 10,
  initialMinutes: Int = 23,
  initialSeconds: Int = 42,
  modifier: Modifier = Modifier
) {
  var remainingSec by remember {
    mutableIntStateOf(initialHours * 3600 + initialMinutes * 60 + initialSeconds)
  }

  LaunchedEffect(Unit) {
    while (remainingSec > 0) {
      delay(1000)
      remainingSec--
    }
  }

  val hours = remainingSec / 3600
  val minutes = (remainingSec % 3600) / 60
  val seconds = remainingSec % 60

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // Seconds Box
    CountdownBox(value = "%02d".format(seconds), label = "ثواني")
    Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    // Minutes Box
    CountdownBox(value = "%02d".format(minutes), label = "دقائق")
    Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    // Hours Box
    CountdownBox(value = "%d".format(hours), label = "ساعات")
  }
}

@Composable
private fun CountdownBox(value: String, label: String) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF232328))
      .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = value,
      color = TodGold,
      fontSize = 15.sp,
      fontWeight = FontWeight.Black
    )
    Text(
      text = label,
      color = Color(0xFFDDDDDF),
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

/**
 * Match Detail & Multi-View Modal (Screenshots 1 & 2)
 * Stadium hero, tournament logo, teams, countdown, big yellow button, replay, multi-view.
 */
data class TodMatchDetail(
  val id: String,
  val title: String,
  val team1Name: String,
  val team1LogoUrl: String? = null,
  val team2Name: String,
  val team2LogoUrl: String? = null,
  val tournament: String,
  val tournamentLogoUrl: String? = null,
  val matchDateTime: String,
  val stadium: String = "",
  val isLive: Boolean = true,
  val streamUrl: String = "",
  val hoursRemaining: Int = 10,
  val minutesRemaining: Int = 23,
  val secondsRemaining: Int = 42
)

@Composable
fun TodMatchDetailSheet(
  match: TodMatchDetail,
  onClose: () -> Unit,
  onPlayNow: () -> Unit,
  onPlayCatchup: () -> Unit,
  onPlayMultiView: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isSavedToMyTod by remember { androidx.compose.runtime.mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      // 1. Hero Image / Stadium Banner with Close Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(230.dp)
      ) {
        // Gradient fallback or stadium backdrop
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color(0xFF0F2648), Color(0xFF1E0E32), Color(0xFF0A0A0C))
              )
            )
        )
        // Scrim
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color(0x55000000), Color.Transparent, Color(0xFF000000))
              )
            )
        )

        // Close 'X' Button on top-left (Screenshot 1 & 2)
        IconButton(
          onClick = onClose,
          modifier = Modifier
            .padding(top = 40.dp, start = 16.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .align(Alignment.TopStart)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "إغلاق",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        // Tournament badge on top-right (Screenshot 1)
        if (!match.tournamentLogoUrl.isNullOrBlank()) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(match.tournamentLogoUrl)
              .crossfade(false)
              .build(),
            contentDescription = null,
            modifier = Modifier
              .padding(top = 44.dp, end = 20.dp)
              .size(54.dp)
              .align(Alignment.TopEnd)
          )
        }
      }

      // 2. Teams and Match Header Info
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Teams Row (Aston Villa vs Tottenham)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          // Team 1
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1B1B22))
                .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              if (!match.team1LogoUrl.isNullOrBlank()) {
                AsyncImage(
                  model = match.team1LogoUrl,
                  contentDescription = match.team1Name,
                  modifier = Modifier.size(48.dp)
                )
              } else {
                Text("⚽", fontSize = 28.sp)
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = match.team1Name,
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
          }

          // VS separator
          Text(
            text = "—",
            color = TodGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
          )

          // Team 2
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1B1B22))
                .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              if (!match.team2LogoUrl.isNullOrBlank()) {
                AsyncImage(
                  model = match.team2LogoUrl,
                  contentDescription = match.team2Name,
                  modifier = Modifier.size(48.dp)
                )
              } else {
                Text("⚽", fontSize = 28.sp)
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = match.team2Name,
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Match Title & Info
        Text(
          text = match.title,
          color = Color.White,
          fontSize = 19.sp,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Countdown Timer if upcoming, or LIVE badge if active
        if (match.isLive) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(TodLiveRed)
              .padding(horizontal = 14.dp, vertical = 4.dp)
          ) {
            Text(
              text = "مباشر",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          TodCountdownTimer(
            initialHours = match.hoursRemaining,
            initialMinutes = match.minutesRemaining,
            initialSeconds = match.secondsRemaining
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Match metadata (date, time, stadium, tournament + HDR badge)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "${match.matchDateTime} • ${match.stadium} • ${match.tournament}",
            color = DarkTextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF202026))
              .border(1.dp, Color(0xFF33333E), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text("HDR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Primary Button: Big Wide Yellow Button (Screenshot 1 & 2)
        Button(
          onClick = {
            if (match.isLive) onPlayNow() else isSavedToMyTod = !isSavedToMyTod
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TodGold),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            if (match.isLive) {
              Text(
                text = "تابع الآن",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
              )
            } else {
              Text(
                text = if (isSavedToMyTod) "تمت الإضافة إلى My TOD ✓" else "+ My TOD",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Secondary Action Buttons (Screenshot 1)
        if (match.isLive) {
          // Replay Button (تابع من البداية ↺)
          Button(
            onClick = onPlayCatchup,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TodButtonGrey),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "تابع من البداية",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Multi-View Button (شاهد العرض المتعدد 㗊)
          Button(
            onClick = onPlayMultiView,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TodButtonGrey),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "شاهد العرض المتعدد",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // My TOD Add Action (Divider + "+ My TOD")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isSavedToMyTod = !isSavedToMyTod }
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isSavedToMyTod) "محفوظ في My TOD" else "My TOD",
            color = TodGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = TodGold,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Match Statistics Section (إحصائيات المباراة - Screenshot 2)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          horizontalAlignment = Alignment.End
        ) {
          Text(
            text = "إحصائيات المباراة",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF0F0F12))
              .border(1.dp, Color(0xFF1E1E24), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = TodGold,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "لا توجد بيانات متاحة حتى الآن",
                color = DarkTextSecondary,
                fontSize = 13.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

/**
 * TOD Profiles Screen: "من يشاهد الآن؟" (Screenshot 6)
 */
@Composable
fun TodProfileSelectionSheet(
  activeProfileName: String = "Main",
  onSelectProfile: (String) -> Unit,
  onManageProfiles: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "من يشاهد الآن؟",
      color = Color.White,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(48.dp))

    // Profiles Row
    Row(
      horizontalArrangement = Arrangement.spacedBy(32.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Add Profile Box
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            .clickable { /* Add profile */ },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "إضافة ملف شخصي",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "إضافة ملف شخصي",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // 2. Main Profile Box (Iconic TOD Golden Yellow Avatar)
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TodGold)
            .clickable { onSelectProfile("Main") },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Main",
            tint = Color.Black,
            modifier = Modifier.size(54.dp)
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = activeProfileName,
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Black
        )
      }
    }

    Spacer(modifier = Modifier.height(72.dp))

    // Manage Profiles Button
    Button(
      onClick = onManageProfiles,
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .height(50.dp),
      colors = ButtonDefaults.buttonColors(containerColor = TodButtonGrey),
      shape = RoundedCornerShape(10.dp)
    ) {
      Text(
        text = "إدارة الملفات الشخصية",
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
