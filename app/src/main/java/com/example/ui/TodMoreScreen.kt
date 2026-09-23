package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import com.example.ui.theme.AppFontPreset
import com.example.ui.theme.FontStateHolder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.TodGradients
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
  onRefreshPlaylist: (XtreamPlaylistConfig) -> Unit = {},
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
      .background(TodGradients.ObsidianCanvas)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Apple iOS Large Title Header ("المزيد")
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 18.dp, top = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color(0x18FFFFFF))
          .border(0.75.dp, Color(0x30FFFFFF), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Tune,
          contentDescription = null,
          tint = TodGold,
          modifier = Modifier.size(18.dp)
        )
      }

      Text(
        text = "المزيد",
        color = Color.White,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp
      )
    }

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
      val addInteraction = remember { MutableInteractionSource() }
      val isAddPressed by addInteraction.collectIsPressedAsState()
      val addScale by animateFloatAsState(
        targetValue = if (isAddPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "addProfileScale"
      )

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .scale(addScale)
          .clickable(
            interactionSource = addInteraction,
            indication = null
          ) { onOpenXtreamForm() }
      ) {
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF151522))
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

        val profileInteraction = remember { MutableInteractionSource() }
        val isProfilePressed by profileInteraction.collectIsPressedAsState()
        val profileScale by animateFloatAsState(
          targetValue = if (isProfilePressed) 0.92f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
          label = "profileScale_$index"
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .scale(profileScale)
            .clickable(
              interactionSource = profileInteraction,
              indication = null
            ) { onSelectPlaylist(config) }
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

    Spacer(modifier = Modifier.height(28.dp))

    // Switch states for iOS Preferences
    var hwDecodeEnabled by remember { mutableStateOf(true) }
    var spatialAudioEnabled by remember { mutableStateOf(true) }
    var adaptiveBitrateEnabled by remember { mutableStateOf(true) }

    // SECTION 1: الحساب والاشتراكات (Account & Subscriptions)
    IosSectionHeader(title = "الحساب والاشتراكات")
    IosListGroup {
      IosListRow(
        title = "إدارة الملفات والاشتراكات",
        subtitle = "التبديل بين السيرفرات وإدارة الحسابات المسجلة (${savedPlaylists.size})",
        iconBadge = {
          IosIconBadge(icon = Icons.Default.ManageAccounts, background = IosBadgeColors.Blue)
        },
        value = "${savedPlaylists.size} متاح",
        onClick = { onSwitchPlaylist() }
      )
      IosListRow(
        title = "إضافة اشتراك Xtream Codes",
        subtitle = "إضافة سيرفر جديد عبر الرابط واسم المستخدم وكلمة السر",
        iconBadge = {
          IosIconBadge(icon = Icons.Default.Bolt, background = IosBadgeColors.Gold, tint = Color.Black)
        },
        onClick = { onOpenXtreamForm() }
      )
      IosListRow(
        title = "إضافة قائمة قنوات M3U",
        subtitle = "تحميل ملف أو رابط M3U / M3U_PLUS المباشر",
        iconBadge = {
          IosIconBadge(icon = Icons.Default.Subscriptions, background = IosBadgeColors.Purple)
        },
        showDivider = false,
        onClick = { onOpenM3uForm() }
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // SECTION 2: تفاصيل السيرفر النشط (Active Server Details)
    if (activeConfig != null) {
      IosSectionHeader(title = "تفاصيل الاشتراك النشط")
      IosListGroup {
        IosListRow(
          title = "اسم الملف والاشتراك",
          value = activeConfig.playlistName,
          valueColor = TodGold,
          iconBadge = {
            IosIconBadge(icon = Icons.Default.Person, background = IosBadgeColors.Orange)
          },
          showChevron = false
        )
        IosListRow(
          title = "عنوان السيرفر",
          value = activeConfig.serverUrl.takeIf { it.isNotBlank() } ?: activeConfig.m3uUrl,
          iconBadge = {
            IosIconBadge(icon = Icons.Default.Dns, background = IosBadgeColors.Indigo)
          },
          showChevron = false
        )
        IosListRow(
          title = "حالة الاتصال وسرعة الاستجابة",
          value = if (serverPingMs != null && serverPingMs > 0) "${serverPingMs}ms • متصل" else "متصل",
          valueColor = if (serverPingMs != null && serverPingMs > 0) TodGreen else Color(0xFF8E8E93),
          iconBadge = {
            IosIconBadge(icon = Icons.Default.Wifi, background = IosBadgeColors.Green)
          },
          showChevron = false
        )
        if (accountInfo?.expDate != null) {
          IosListRow(
            title = "تاريخ الانتهاء والاشتراك",
            value = accountInfo.expDate,
            valueColor = TodGreen,
            iconBadge = {
              IosIconBadge(icon = Icons.Default.Event, background = IosBadgeColors.Pink)
            },
            showChevron = false
          )
        }
        if (activeConfig.totalChannelCount > 0) {
          IosListRow(
            title = "إجمالي القنوات المحملة",
            value = "${activeConfig.totalChannelCount} قناة",
            iconBadge = {
              IosIconBadge(icon = Icons.Default.Tv, background = IosBadgeColors.Teal)
            },
            showChevron = false
          )
        }
        IosListRow(
          title = "تحديث ومزامنة القنوات فوراً",
          subtitle = "إعادة جلب أحدث قائمة قنوات وفئات من السيرفر",
          value = "مزامنة الآن",
          valueColor = TodGold,
          iconBadge = {
            IosIconBadge(icon = Icons.Default.Refresh, background = IosBadgeColors.Gold, tint = Color.Black)
          },
          showDivider = false,
          onClick = { onRefreshPlaylist(activeConfig) }
        )
      }
      IosSectionFooter(text = "يتم تحديث القنوات تلقائياً وفقاً للجدول الزمني المختار: ${activeConfig.updateInterval.ifBlank { "عند بدء التطبيق" }}")
      Spacer(modifier = Modifier.height(24.dp))
    }

    // SECTION 3: التشغيل والبث المباشر (Playback & Live Streaming)
    IosSectionHeader(title = "التشغيل والروابط السريعة")
    IosListGroup {
      IosListRow(
        title = "تشغيل رابط مباشر (M3U8 / TS / HLS)",
        subtitle = "قم بلصق أي رابط ويب وتشغيله بلمسة واحدة",
        iconBadge = {
          IosIconBadge(icon = Icons.Default.Bolt, background = IosBadgeColors.Red)
        },
        onClick = { onOpenDirectLink() }
      )
    }
    IosSectionFooter(text = "يدعم المشغل جميع صيغ البث العالمي HLS, DASH, TS, RTMP بدقة تصل إلى 4K.")

    Spacer(modifier = Modifier.height(24.dp))

    // SECTION 4: الخط العربي والمظهر (Arabic Typography & Styling)
    IosSectionHeader(title = "الخط العربي ووضوح النصوص")
    IosListGroup {
      AppFontPreset.entries.forEachIndexed { index, fontPreset ->
        val isSelected = FontStateHolder.currentFont == fontPreset
        IosListRow(
          title = fontPreset.titleAr,
          subtitle = fontPreset.descriptionAr,
          value = if (isSelected) "مفعل" else "",
          valueColor = TodGold,
          iconBadge = {
            IosIconBadge(
              icon = if (isSelected) Icons.Default.Check else Icons.Default.TextFields,
              background = if (isSelected) IosBadgeColors.Gold else IosBadgeColors.Slate,
              tint = if (isSelected) Color.Black else Color.White
            )
          },
          showDivider = index < AppFontPreset.entries.size - 1,
          showChevron = false,
          onClick = {
            FontStateHolder.currentFont = fontPreset
          }
        )
      }
    }
    IosSectionFooter(text = "خط كايرو (Cairo) هو الخط الافتراضي الواضح والمريح للعين والمصمم خصيصاً للشاشات وواجهات البث التلفزيوني.")

    Spacer(modifier = Modifier.height(24.dp))

    // SECTION 5: إعدادات الأداء والعرض (Playback Performance & Preferences)
    IosSectionHeader(title = "تفضيلات العرض ومحرك الفيديو")
    IosListGroup {
      IosListRow(
        title = "التسريع العتادي الفائق (Hardware)",
        subtitle = "معالجة تدفقات 4K و FHD بسلاسة 60 إطار/ثانية",
        iconBadge = {
          IosIconBadge(icon = Icons.Default.Speed, background = IosBadgeColors.Green)
        },
        trailing = {
          IosSwitch(
            checked = hwDecodeEnabled,
            onCheckedChange = { hwDecodeEnabled = it }
          )
        },
        showChevron = false
      )
      IosListRow(
        title = "الصوت المحيطي (Spatial Audio)",
        subtitle = "تحسين أصوات التعليق الرياضي وصخب الجماهير",
        iconBadge = {
          IosIconBadge(icon = Icons.Default.VolumeUp, background = IosBadgeColors.Indigo)
        },
        trailing = {
          IosSwitch(
            checked = spatialAudioEnabled,
            onCheckedChange = { spatialAudioEnabled = it }
          )
        },
        showChevron = false
      )
      IosListRow(
        title = "التبديل التلقائي لمعدل البث (ABR)",
        subtitle = "مواءمة جودة العرض ديناميكياً مع سرعة الشبكة لمنع التقطيع",
        iconBadge = {
          IosIconBadge(icon = Icons.Default.Tune, background = IosBadgeColors.Teal)
        },
        trailing = {
          IosSwitch(
            checked = adaptiveBitrateEnabled,
            onCheckedChange = { adaptiveBitrateEnabled = it }
          )
        },
        showChevron = false
      )
      IosListRow(
        title = "إصدار التطبيق والواجهة",
        value = "v2.5.0 iOS Edition",
        iconBadge = {
          IosIconBadge(icon = Icons.Default.Info, background = IosBadgeColors.Slate)
        },
        showDivider = false,
        showChevron = false
      )
    }
    IosSectionFooter(text = "تصميم القوائم مستوحى من نظام Apple iOS الحديث لضمان تجربة انسيابية وسهلة الاستخدام.")

    Spacer(modifier = Modifier.height(48.dp))
  }
}
