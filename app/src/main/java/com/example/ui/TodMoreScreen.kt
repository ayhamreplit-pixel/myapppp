package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.XtreamAccountInfo
import com.example.model.XtreamPlaylistConfig
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.TodButtonGrey
import com.example.ui.theme.TodCyan
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGreen
import com.example.ui.theme.TodPink
import com.example.ui.theme.TodViolet

/**
 * Official TOD "المزيد" (More / Account & Dynamic Xtream Profiles Management)
 */
@Composable
fun TodMoreScreen(
  activeConfig: XtreamPlaylistConfig?,
  savedPlaylists: List<XtreamPlaylistConfig>,
  accountInfo: XtreamAccountInfo?,
  serverPingMs: Long?,
  onSelectPlaylist: (XtreamPlaylistConfig) -> Unit,
  onSwitchPlaylist: () -> Unit,
  onOpenXtreamForm: () -> Unit,
  onOpenM3uForm: () -> Unit,
  onOpenDirectLink: () -> Unit,
  onDeletePlaylist: (XtreamPlaylistConfig) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val avatarColors = listOf(
    TodGold,
    TodCyan,
    TodViolet,
    TodPink,
    TodGreen,
    Color(0xFFFF9800)
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. TOD Dynamic Profiles Section ("من يشاهد الآن؟" - Screenshot 6 + Multi-Xtream)
    Text(
      text = "من يشاهد الآن؟",
      color = Color.White,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "اختر اشتراك أو أضف سيرفر Xtream جديد للتبديل الفوري بين القنوات",
      color = DarkTextSecondary,
      fontSize = 12.sp,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Horizontal Row of Profiles with "+" Add Button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Add Profile Button (+)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onOpenXtreamForm() }
      ) {
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF15151C))
            .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "إضافة اشتراك جديد",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "إضافة اشتراك",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Dynamic Saved Xtream Profiles
      savedPlaylists.forEachIndexed { index, config ->
        val isActive = activeConfig?.serverUrl == config.serverUrl &&
            activeConfig?.username == config.username &&
            activeConfig?.playlistName == config.playlistName
        val color = avatarColors[index % avatarColors.size]

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable { onSelectPlaylist(config) }
        ) {
          Box(
            modifier = Modifier
              .size(76.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(color)
              .then(
                if (isActive) Modifier.border(3.dp, Color.White, RoundedCornerShape(14.dp))
                else Modifier
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = config.playlistName.take(1).uppercase(),
              color = Color.Black,
              fontWeight = FontWeight.Black,
              fontSize = 32.sp
            )
            if (isActive) {
              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(4.dp)
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(Color.Black),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TodGold, modifier = Modifier.size(14.dp))
              }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = config.playlistName,
            color = if (isActive) TodGold else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Manage Profiles Button
    Button(
      onClick = { onSwitchPlaylist() },
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp),
      colors = ButtonDefaults.buttonColors(containerColor = TodButtonGrey),
      shape = RoundedCornerShape(10.dp)
    ) {
      Text("إدارة الاشتراكات والملفات الشخصية", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(28.dp))

    // 2. Direct Stream Quick Play Button (تشغيل رابط مباشر فوراً)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF16161C))
        .border(1.dp, TodGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        .clickable { onOpenDirectLink() }
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowLeft,
          contentDescription = null,
          tint = TodGold,
          modifier = Modifier.size(24.dp)
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "تشغيل رابط مباشر فوراً (M3U8 / TS)",
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "قم بلصق أي رابط HLS, DASH, TS أو رابط ويب وتشغيله بلمسة واحدة",
              color = DarkTextSecondary,
              fontSize = 11.sp
            )
          }

          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(TodGold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = TodGold, modifier = Modifier.size(22.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 3. Active Xtream Account Details Card
    if (activeConfig != null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF14141A))
          .border(1.dp, Color(0xFF242432), RoundedCornerShape(14.dp))
          .padding(18.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Ping / Server Status Indicator
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (serverPingMs != null && serverPingMs > 0) TodGreen else Color(0xFFFF5252))
              )
              Text(
                text = if (serverPingMs != null && serverPingMs > 0) "${serverPingMs}ms • متصل" else "متصل",
                color = if (serverPingMs != null && serverPingMs > 0) TodGreen else DarkTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Text(
              text = "تفاصيل الاشتراك النشط",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Profile Name
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(activeConfig.playlistName, color = TodGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("اسم الملف:", color = DarkTextSecondary, fontSize = 13.sp)
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Server URL
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = activeConfig.serverUrl.takeIf { it.isNotBlank() } ?: activeConfig.m3uUrl,
              color = Color.White,
              fontSize = 12.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("السيرفر:", color = DarkTextSecondary, fontSize = 13.sp)
          }

          if (accountInfo?.expDate != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(accountInfo.expDate, color = TodGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text("تاريخ الانتهاء:", color = DarkTextSecondary, fontSize = 13.sp)
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // 4. Add Xtream / M3U Quick Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = onOpenM3uForm,
        modifier = Modifier
          .weight(1f)
          .height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B22)),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("إضافة M3U", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = onOpenXtreamForm,
        modifier = Modifier
          .weight(1f)
          .height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TodGold),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("إضافة Xtream Codes", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}
