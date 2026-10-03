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
import androidx.compose.material3.CircularProgressIndicator
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

fun getCountryFlagUrl(teamName: String): String? {
  val norm = normalizeArabic(teamName)
  return when {
    norm.contains("المانيا") || norm.contains("germany") -> "https://flagcdn.com/w80/de.png"
    norm.contains("صربيا") || norm.contains("serbia") -> "https://flagcdn.com/w80/rs.png"
    norm.contains("اسبانيا") || norm.contains("spain") -> "https://flagcdn.com/w80/es.png"
    norm.contains("فرنسا") || norm.contains("france") -> "https://flagcdn.com/w80/fr.png"
    norm.contains("ايطاليا") || norm.contains("italy") -> "https://flagcdn.com/w80/it.png"
    norm.contains("انجلترا") || norm.contains("england") -> "https://flagcdn.com/w80/gb-eng.png"
    norm.contains("البرتغال") || norm.contains("portugal") -> "https://flagcdn.com/w80/pt.png"
    norm.contains("هولندا") || norm.contains("netherlands") -> "https://flagcdn.com/w80/nl.png"
    norm.contains("بلجيكا") || norm.contains("belgium") -> "https://flagcdn.com/w80/be.png"
    norm.contains("كرواتيا") || norm.contains("croatia") -> "https://flagcdn.com/w80/hr.png"
    norm.contains("الدنمارك") || norm.contains("denmark") -> "https://flagcdn.com/w80/dk.png"
    norm.contains("النرويج") || norm.contains("norway") -> "https://flagcdn.com/w80/no.png"
    norm.contains("السويد") || norm.contains("sweden") -> "https://flagcdn.com/w80/se.png"
    norm.contains("سويسرا") || norm.contains("switzerland") -> "https://flagcdn.com/w80/ch.png"
    norm.contains("ويلز") || norm.contains("wales") -> "https://flagcdn.com/w80/gb-wls.png"
    norm.contains("اسكتلندا") || norm.contains("scotland") -> "https://flagcdn.com/w80/gb-sct.png"
    norm.contains("بولندا") || norm.contains("poland") -> "https://flagcdn.com/w80/pl.png"
    norm.contains("اوكرانيا") || norm.contains("ukraine") -> "https://flagcdn.com/w80/ua.png"
    norm.contains("تركيا") || norm.contains("turkey") -> "https://flagcdn.com/w80/tr.png"
    norm.contains("اليونان") || norm.contains("greece") -> "https://flagcdn.com/w80/gr.png"
    norm.contains("التشيك") || norm.contains("czech") -> "https://flagcdn.com/w80/cz.png"
    norm.contains("النمسا") || norm.contains("austria") -> "https://flagcdn.com/w80/at.png"
    norm.contains("المجر") || norm.contains("hungary") -> "https://flagcdn.com/w80/hu.png"
    norm.contains("سلوفاكيا") || norm.contains("slovakia") -> "https://flagcdn.com/w80/sk.png"
    norm.contains("سلوفينيا") || norm.contains("slovenia") -> "https://flagcdn.com/w80/si.png"
    norm.contains("رومانيا") || norm.contains("romania") -> "https://flagcdn.com/w80/ro.png"
    norm.contains("جورجيا") || norm.contains("georgia") -> "https://flagcdn.com/w80/ge.png"
    norm.contains("البانيا") || norm.contains("albania") -> "https://flagcdn.com/w80/al.png"
    norm.contains("ايرلندا") || norm.contains("ireland") -> "https://flagcdn.com/w80/ie.png"
    norm.contains("البرازيل") || norm.contains("brazil") -> "https://flagcdn.com/w80/br.png"
    norm.contains("الارجنتين") || norm.contains("argentina") -> "https://flagcdn.com/w80/ar.png"
    norm.contains("اوروغواي") || norm.contains("uruguay") -> "https://flagcdn.com/w80/uy.png"
    norm.contains("كولومبيا") || norm.contains("colombia") -> "https://flagcdn.com/w80/co.png"
    norm.contains("تشيلي") || norm.contains("chile") -> "https://flagcdn.com/w80/cl.png"
    norm.contains("السعوديه") || norm.contains("saudi") -> "https://flagcdn.com/w80/sa.png"
    norm.contains("مصر") || norm.contains("egypt") -> "https://flagcdn.com/w80/eg.png"
    norm.contains("المغرب") || norm.contains("morocco") -> "https://flagcdn.com/w80/ma.png"
    norm.contains("الجزائر") || norm.contains("algeria") -> "https://flagcdn.com/w80/dz.png"
    norm.contains("تونس") || norm.contains("tunisia") -> "https://flagcdn.com/w80/tn.png"
    norm.contains("العراق") || norm.contains("iraq") -> "https://flagcdn.com/w80/iq.png"
    norm.contains("قطر") || norm.contains("qatar") -> "https://flagcdn.com/w80/qa.png"
    norm.contains("الامارات") || norm.contains("uae") -> "https://flagcdn.com/w80/ae.png"
    norm.contains("الكويت") || norm.contains("kuwait") -> "https://flagcdn.com/w80/kw.png"
    norm.contains("البحرين") || norm.contains("bahrain") -> "https://flagcdn.com/w80/bh.png"
    norm.contains("عمان") || norm.contains("oman") -> "https://flagcdn.com/w80/om.png"
    norm.contains("الاردن") || norm.contains("jordan") -> "https://flagcdn.com/w80/jo.png"
    norm.contains("سوريا") || norm.contains("syria") -> "https://flagcdn.com/w80/sy.png"
    norm.contains("لبنان") || norm.contains("lebanon") -> "https://flagcdn.com/w80/lb.png"
    norm.contains("فلسطين") || norm.contains("palestine") -> "https://flagcdn.com/w80/ps.png"
    norm.contains("اليمن") || norm.contains("yemen") -> "https://flagcdn.com/w80/ye.png"
    norm.contains("ليبيا") || norm.contains("libya") -> "https://flagcdn.com/w80/ly.png"
    norm.contains("السودان") || norm.contains("sudan") -> "https://flagcdn.com/w80/sd.png"
    norm.contains("موريتانيا") || norm.contains("mauritania") -> "https://flagcdn.com/w80/mr.png"
    norm.contains("اليابان") || norm.contains("japan") -> "https://flagcdn.com/w80/jp.png"
    norm.contains("كوريا") || norm.contains("korea") -> "https://flagcdn.com/w80/kr.png"
    norm.contains("استراليا") || norm.contains("australia") -> "https://flagcdn.com/w80/au.png"
    norm.contains("ايران") || norm.contains("iran") -> "https://flagcdn.com/w80/ir.png"
    norm.contains("السنغال") || norm.contains("senegal") -> "https://flagcdn.com/w80/sn.png"
    norm.contains("نيجيريا") || norm.contains("nigeria") -> "https://flagcdn.com/w80/ng.png"
    norm.contains("الكاميرون") || norm.contains("cameroon") -> "https://flagcdn.com/w80/cm.png"
    norm.contains("غانا") || norm.contains("ghana") -> "https://flagcdn.com/w80/gh.png"
    norm.contains("كوت ديفوار") || norm.contains("ivory") -> "https://flagcdn.com/w80/ci.png"
    norm.contains("مالي") || norm.contains("mali") -> "https://flagcdn.com/w80/ml.png"
    norm.contains("جنوب افريقيا") || norm.contains("south africa") -> "https://flagcdn.com/w80/za.png"
    norm.contains("كينيا") || norm.contains("kenya") -> "https://flagcdn.com/w80/ke.png"
    norm.contains("غينيا") || norm.contains("guinea") -> "https://flagcdn.com/w80/gn.png"
    norm.contains("الكونغو") || norm.contains("congo") -> "https://flagcdn.com/w80/cd.png"
    norm.contains("الغابون") || norm.contains("gabon") -> "https://flagcdn.com/w80/ga.png"
    norm.contains("امريكا") || norm.contains("usa") -> "https://flagcdn.com/w80/us.png"
    norm.contains("المكسيك") || norm.contains("mexico") -> "https://flagcdn.com/w80/mx.png"
    norm.contains("كندا") || norm.contains("canada") -> "https://flagcdn.com/w80/ca.png"
    norm.contains("اندورا") || norm.contains("andorra") -> "https://flagcdn.com/w80/ad.png"
    norm.contains("ليتوانيا") || norm.contains("lithuania") -> "https://flagcdn.com/w80/lt.png"
    else -> null
  }
}

fun getKnownClubLogoUrl(teamName: String): String? {
  val norm = normalizeArabic(teamName)
  return when {
    norm.contains("ريال مدريد") -> "https://upload.wikimedia.org/wikipedia/en/thumb/5/56/Real_Madrid_CF.svg/512px-Real_Madrid_CF.svg.png"
    norm.contains("برشلونه") -> "https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/512px-FC_Barcelona_%28crest%29.svg.png"
    norm.contains("اتلتيكو مدريد") -> "https://upload.wikimedia.org/wikipedia/en/thumb/f/f4/Atletico_Madrid_2017_logo.svg/512px-Atletico_Madrid_2017_logo.svg.png"
    norm.contains("مانشستر سيتي") -> "https://upload.wikimedia.org/wikipedia/en/thumb/e/eb/Manchester_City_FC_badge.svg/512px-Manchester_City_FC_badge.svg.png"
    norm.contains("مانشستر يونايتد") -> "https://upload.wikimedia.org/wikipedia/en/thumb/7/7a/Manchester_United_FC_crest.svg/512px-Manchester_United_FC_crest.svg.png"
    norm.contains("ليفربول") -> "https://upload.wikimedia.org/wikipedia/en/thumb/0/0c/Liverpool_FC.svg/512px-Liverpool_FC.svg.png"
    norm.contains("ارسنال") -> "https://upload.wikimedia.org/wikipedia/en/thumb/5/53/Arsenal_FC.svg/512px-Arsenal_FC.svg.png"
    norm.contains("تشيلسي") -> "https://upload.wikimedia.org/wikipedia/en/thumb/c/cc/Chelsea_FC.svg/512px-Chelsea_FC.svg.png"
    norm.contains("توتنهام") -> "https://upload.wikimedia.org/wikipedia/en/thumb/b/b4/Tottenham_Hotspur.svg/512px-Tottenham_Hotspur.svg.png"
    norm.contains("بايرن ميونخ") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/FC_Bayern_M%C3%BCnchen_logo_%282017%29.svg/512px-FC_Bayern_M%C3%BCnchen_logo_%282017%29.svg.png"
    norm.contains("بوروسيا دورتموند") || norm.contains("دورتموند") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Borussia_Dortmund_logo.svg/512px-Borussia_Dortmund_logo.svg.png"
    norm.contains("باير ليفركوزن") || norm.contains("ليفركوزن") -> "https://upload.wikimedia.org/wikipedia/en/thumb/5/59/Bayer_04_Leverkusen_logo.svg/512px-Bayer_04_Leverkusen_logo.svg.png"
    norm.contains("باريس سان جيرمان") -> "https://upload.wikimedia.org/wikipedia/en/thumb/a/a7/Paris_Saint-Germain_F.C..svg/512px-Paris_Saint-Germain_F.C..svg.png"
    norm.contains("يوفنتوس") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Juventus_FC_2017_icon_%28black%29.svg/512px-Juventus_FC_2017_icon_%28black%29.svg.png"
    norm.contains("انتر ميلان") || norm.contains("انتر") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/FC_Internazionale_Milano_2021.svg/512px-FC_Internazionale_Milano_2021.svg.png"
    norm.contains("اي سي ميلان") || norm.contains("ميلان") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Logo_of_AC_Milan.svg/512px-Logo_of_AC_Milan.svg.png"
    norm.contains("نابولي") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/SSC_Napoli_2024_%28deep_blue_navy%29.svg/512px-SSC_Napoli_2024_%28deep_blue_navy%29.svg.png"
    norm.contains("الهلال") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Al_Hilal_SFC_Logo_%282022%29.svg/512px-Al_Hilal_SFC_Logo_%282022%29.svg.png"
    norm.contains("النصر") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Al-Nassr_FC_logo.svg/512px-Al-Nassr_FC_logo.svg.png"
    norm.contains("الاتحاد") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Al-Ittihad_Club_logo.svg/512px-Al-Ittihad_Club_logo.svg.png"
    norm.contains("الاهلي السعودي") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/Al-Ahli_Saudi_FC_logo.svg/512px-Al-Ahli_Saudi_FC_logo.svg.png"
    norm.contains("الاهلي") || norm.contains("الاهلي المصري") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Al_Ahly_SC_logo.svg/512px-Al_Ahly_SC_logo.svg.png"
    norm.contains("الزمالك") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/ZamalekSC.png/512px-ZamalekSC.png"
    norm.contains("بيراميدز") -> "https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Pyramids_FC_logo.png/512px-Pyramids_FC_logo.png"
    norm.contains("الوداد") -> "https://upload.wikimedia.org/wikipedia/en/thumb/c/cf/Wydad_Athletic_Club_logo.svg/512px-Wydad_Athletic_Club_logo.svg.png"
    norm.contains("الرجاء") -> "https://upload.wikimedia.org/wikipedia/en/thumb/e/e0/Raja_Club_Athletic_logo.svg/512px-Raja_Club_Athletic_logo.svg.png"
    norm.contains("الزوراء") -> "https://upload.wikimedia.org/wikipedia/en/thumb/4/49/Al-Zawraa_SC_logo.png/512px-Al-Zawraa_SC_logo.png"
    norm.contains("القوه الجويه") -> "https://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Al-Quwa_Al-Jawiya_logo.png/512px-Al-Quwa_Al-Jawiya_logo.png"
    norm.contains("الشرطه") -> "https://upload.wikimedia.org/wikipedia/en/thumb/9/9c/Al-Shorta_SC_logo.png/512px-Al-Shorta_SC_logo.png"
    norm.contains("السد") -> "https://upload.wikimedia.org/wikipedia/en/thumb/9/91/Al_Sadd_SC_logo.svg/512px-Al_Sadd_SC_logo.svg.png"
    norm.contains("العين") -> "https://upload.wikimedia.org/wikipedia/en/thumb/7/77/Al_Ain_FC_logo.svg/512px-Al_Ain_FC_logo.svg.png"
    else -> null
  }
}

/**
 * High-resolution Team Crest Component with robust CDN flag & club logo fallbacks
 */
@Composable
fun TodTeamCrest(
  team: SportsTeam,
  size: Dp = 46.dp,
  borderGlowColor: Color = Color(0xFF64D2FF),
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val flagUrl = remember(team.name) { getCountryFlagUrl(team.name) }
  val clubUrl = remember(team.name) { getKnownClubLogoUrl(team.name) }
  val cleanedLogo = remember(team.logoUrl) {
    var u = team.logoUrl.trim()
    if (u.contains("img.ysscores.com/teams/")) {
      u = u.replace("https://img.ysscores.com/teams/", "https://imgs.ysscores.com/teams/128/")
           .replace("http://img.ysscores.com/teams/", "https://imgs.ysscores.com/teams/128/")
    } else if (u.contains("imgs.ysscores.com/teams/") && !u.contains("/128/") && !u.contains("/64/")) {
      u = u.replace("imgs.ysscores.com/teams/", "imgs.ysscores.com/teams/128/")
    }
    if (u.contains("null") || u.isBlank()) "" else u
  }

  // If country flag is known, prioritize the official flagcdn URL!
  // If known club, prioritize official Wikimedia vector!
  val primaryModel = remember(cleanedLogo, flagUrl, clubUrl) {
    when {
      !flagUrl.isNullOrBlank() -> flagUrl
      !clubUrl.isNullOrBlank() -> clubUrl
      cleanedLogo.isNotBlank() -> cleanedLogo
      else -> ""
    }
  }

  val imageRequest = remember(primaryModel) {
    if (primaryModel.isNotBlank()) {
      coil.request.ImageRequest.Builder(context)
        .data(primaryModel)
        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .addHeader("Referer", "https://www.ysscores.com/")
        .crossfade(true)
        .build()
    } else null
  }

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    if (imageRequest != null) {
      SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = team.name,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        loading = {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 1.dp, color = Color(0x60FFFFFF), modifier = Modifier.size(12.dp))
          }
        },
        error = {
          val fallbackTarget = when {
            primaryModel != cleanedLogo && cleanedLogo.isNotBlank() -> cleanedLogo
            primaryModel != flagUrl && !flagUrl.isNullOrBlank() -> flagUrl
            primaryModel != clubUrl && !clubUrl.isNullOrBlank() -> clubUrl
            else -> ""
          }
          if (fallbackTarget.isNotBlank()) {
            val fallbackRequest = remember(fallbackTarget) {
              coil.request.ImageRequest.Builder(context)
                .data(fallbackTarget)
                .addHeader("User-Agent", "Mozilla/5.0")
                .crossfade(true)
                .build()
            }
            SubcomposeAsyncImage(
              model = fallbackRequest,
              contentDescription = team.name,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit,
              error = {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0x25FFFFFF)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = team.flagEmoji.ifBlank { team.name.take(2) },
                    fontSize = (size.value * 0.50f).sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                  )
                }
              }
            )
          } else {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0x25FFFFFF)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = team.flagEmoji.ifBlank { team.name.take(2) },
                fontSize = (size.value * 0.50f).sp,
                textAlign = TextAlign.Center,
                color = Color.White
              )
            }
          }
        }
      )
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(CircleShape)
          .background(Color(0x25FFFFFF)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = team.flagEmoji.ifBlank { team.name.take(2) },
          fontSize = (size.value * 0.50f).sp,
          textAlign = TextAlign.Center,
          color = Color.White
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
  GEOMETRIC_CIRCLES, // Overlapping circular grid pattern (Screenshots 161624, 220513)
  UCL_STARS,         // Ambient UCL starball curves (Screenshot 162954, 161604)
  FRIENDLIES_LINES   // International Friendlies ambient field glow (Screenshot 161624)
}

/**
 * Normalizes Arabic string for robust tournament matching
 */
fun normalizeArabic(text: String): String {
  var s = text.lowercase()
    .replace('أ', 'ا')
    .replace('إ', 'ا')
    .replace('آ', 'ا')
    .replace('ة', 'ه')
    .replace('ى', 'ي')
    .replace("ـ", "")
  val diacritics = Regex("[\u064B-\u065F\u0670]")
  s = diacritics.replace(s, "")
  return s.trim()
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
  val watermark: TournamentWatermark = TournamentWatermark.GEOMETRIC_CIRCLES,
  val priority: Int = 10
)

fun getTournamentCardStyle(
  tournamentName: String,
  homeTeam: String = "",
  awayTeam: String = ""
): TournamentCardStyle {
  val norm = normalizeArabic(tournamentName)
  val normCombined = normalizeArabic("$tournamentName $homeTeam $awayTeam")

  return when {
    // 1. دوري أبطال أوروبا (UEFA Champions League) - أزرق ملكي داكن مع توهج النجوم
    norm.contains("ابطال اوروبا") || norm.contains("champions league") || norm.contains("ucl") ||
    norm.contains("دوري الابطال") || norm.contains("شامبيونز") -> {
      TournamentCardStyle(
        tournamentKey = "ucl",
        displayName = "دوري أبطال أوروبا",
        bgGradient = listOf(
          Color(0xFF030D2E),
          Color(0xFF071B54),
          Color(0xFF020718)
        ),
        accentColor = Color(0xFF00D4FF),
        secondaryColor = Color(0xFF1D4ED8),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png",
        watermark = TournamentWatermark.UCL_STARS,
        priority = 1
      )
    }

    // 2. دوري الأمم الأوروبية (UEFA Nations League) - أزرق ياقوتي مع كحلي وسماوي
    norm.contains("امم اوروبا") || norm.contains("دوري الامم") || norm.contains("nations league") ||
    norm.contains("الامم الاوروبيه") || (norm.contains("امم") && !norm.contains("افريقيا") && !norm.contains("اسيا")) -> {
      TournamentCardStyle(
        tournamentKey = "nations",
        displayName = "دوري الأمم الأوروبية",
        bgGradient = listOf(
          Color(0xFF0A1832),
          Color(0xFF132A54),
          Color(0xFF050E1E)
        ),
        accentColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF90E0EF),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 2
      )
    }

    // 3. المباريات الودية (International & Club Friendlies) - أخضر عشبي داكن مميز للملعب
    norm.contains("ودي") || norm.contains("وديات") || norm.contains("وديه") ||
    norm.contains("friendly") || norm.contains("friendlies") -> {
      TournamentCardStyle(
        tournamentKey = "friendlies",
        displayName = "المباريات الودية",
        bgGradient = listOf(
          Color(0xFF022B16),
          Color(0xFF054524),
          Color(0xFF01180C)
        ),
        accentColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF34D399),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/FIFA_logo_without_slogan.svg/512px-FIFA_logo_without_slogan.svg.png",
        watermark = TournamentWatermark.FRIENDLIES_LINES,
        priority = 14
      )
    }

    // 4. تصفيات كأس العالم والبطولات الدولية (World Cup & Qualifiers) - خمري فيفا فاخر مع ذهبي
    norm.contains("كاس العالم") || norm.contains("world cup") || norm.contains("تصفيات") ||
    norm.contains("انتركونتيننتال") -> {
      TournamentCardStyle(
        tournamentKey = "worldcup",
        displayName = "تصفيات كأس العالم",
        bgGradient = listOf(
          Color(0xFF1E0713),
          Color(0xFF360C22),
          Color(0xFF11030B)
        ),
        accentColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFE11D48),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/FIFA_logo_without_slogan.svg/512px-FIFA_logo_without_slogan.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 2
      )
    }

    // 5. الدوري الإنجليزي الممتاز (Premier League) - بنفسجي ملكي عميق مع أخضر نيون
    norm.contains("انجليزي") || norm.contains("premier") || norm.contains("بريميرليج") ||
    norm.contains("بريمير") || norm.contains("epl") || norm.contains("كاراباو") || norm.contains("كاس الاتحاد الانجليزي") -> {
      TournamentCardStyle(
        tournamentKey = "pl",
        displayName = "الدوري الإنجليزي الممتاز",
        bgGradient = listOf(
          Color(0xFF220038),
          Color(0xFF3B005F),
          Color(0xFF12001F)
        ),
        accentColor = Color(0xFF00FF85),
        secondaryColor = Color(0xFFFF005A),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Premier_League_Logo.svg/512px-Premier_League_Logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 3
      )
    }

    // 6. الدوري الإسباني (LaLiga) - عنابي إسباني ناري مع أحمر قرمزي
    norm.contains("اسباني") || norm.contains("ليغا") || norm.contains("laliga") ||
    norm.contains("كاس الملك") || norm.contains("السوبر الاسباني") -> {
      TournamentCardStyle(
        tournamentKey = "laliga",
        displayName = "الدوري الإسباني (LaLiga)",
        bgGradient = listOf(
          Color(0xFF2E020A),
          Color(0xFF4A0512),
          Color(0xFF1A0105)
        ),
        accentColor = Color(0xFFFF2A4B),
        secondaryColor = Color(0xFFFF6B6B),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/LaLiga_logo_2023.svg/512px-LaLiga_logo_2023.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 3
      )
    }

    // 7. الدوري الإيطالي (Serie A) - أزرق لازوردي إيطالي عميق
    norm.contains("ايطالي") || norm.contains("serie") || norm.contains("كالتشيو") || norm.contains("كاس ايطاليا") -> {
      TournamentCardStyle(
        tournamentKey = "seriea",
        displayName = "الدوري الإيطالي (Serie A)",
        bgGradient = listOf(
          Color(0xFF021B38),
          Color(0xFF052C59),
          Color(0xFF011022)
        ),
        accentColor = Color(0xFF0091FF),
        secondaryColor = Color(0xFF64D2FF),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Serie_A_logo_2019.svg/512px-Serie_A_logo_2019.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 3
      )
    }

    // 8. الدوري الألماني (Bundesliga) - كربوني داكن مع أحمر بوندسليغا ناري
    norm.contains("الماني") || norm.contains("bundesliga") || norm.contains("بوندسليغا") || norm.contains("كاس المانيا") -> {
      TournamentCardStyle(
        tournamentKey = "bundesliga",
        displayName = "الدوري الألماني (Bundesliga)",
        bgGradient = listOf(
          Color(0xFF220505),
          Color(0xFF3B0B0B),
          Color(0xFF140202)
        ),
        accentColor = Color(0xFFE30613),
        secondaryColor = Color(0xFFFF5252),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/d/df/Bundesliga_logo_%282017%29.svg/512px-Bundesliga_logo_%282017%29.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 4
      )
    }

    // 9. الدوري الفرنسي (Ligue 1) - كحلي فرنسي داكن مع ليموني نيون
    norm.contains("فرنسي") || norm.contains("ligue 1") || norm.contains("ليغ 1") || norm.contains("كاس فرنسا") -> {
      TournamentCardStyle(
        tournamentKey = "ligue1",
        displayName = "الدوري الفرنسي (Ligue 1)",
        bgGradient = listOf(
          Color(0xFF09162E),
          Color(0xFF0F2347),
          Color(0xFF050E1E)
        ),
        accentColor = Color(0xFFCCFF00),
        secondaryColor = Color(0xFF38BDF8),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Ligue1_logo_2024.svg/512px-Ligue1_logo_2024.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 4
      )
    }

    // 10. دوري أبطال آسيا للنخبة (AFC Champions League Elite) - كحلي فاخر مع ذهبي مشع
    norm.contains("ابطال اسيا") || norm.contains("النخبه") || norm.contains("afc") ||
    (norm.contains("اسيا") && !norm.contains("تصفيات")) -> {
      TournamentCardStyle(
        tournamentKey = "afc",
        displayName = "دوري أبطال آسيا للنخبة",
        bgGradient = listOf(
          Color(0xFF1E1702),
          Color(0xFF362804),
          Color(0xFF120E01)
        ),
        accentColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFF59E0B),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/d/d4/AFC_Champions_League_Elite_logo.svg/512px-AFC_Champions_League_Elite_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 3
      )
    }

    // 11. دوري روشن السعودي (Saudi Pro League) - أخضر ملكي غامق مع ذهبي
    norm.contains("روشن") || norm.contains("سعودي") || norm.contains("saudi") ||
    norm.contains("كاس الملك سلمان") || norm.contains("كاس خادم الحرمين") -> {
      TournamentCardStyle(
        tournamentKey = "spl",
        displayName = "دوري روشن السعودي",
        bgGradient = listOf(
          Color(0xFF032612),
          Color(0xFF063B1C),
          Color(0xFF01160A)
        ),
        accentColor = Color(0xFF22C55E),
        secondaryColor = Color(0xFFEAB308),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Saudi_Pro_League_logo.svg/512px-Saudi_Pro_League_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 3
      )
    }

    // 12. دوري أبطال إفريقيا والبطولات الأفريقية (CAF Champions League) - برونزي إفريقي مع ذهبي
    norm.contains("ابطال افريقيا") || norm.contains("افريقيا") || norm.contains("caf") || norm.contains("الكونفيدراليه") || norm.contains("الكونفدراليه") -> {
      TournamentCardStyle(
        tournamentKey = "caf",
        displayName = "دوري أبطال إفريقيا",
        bgGradient = listOf(
          Color(0xFF221102),
          Color(0xFF3B1E05),
          Color(0xFF140A01)
        ),
        accentColor = Color(0xFFFFB300),
        secondaryColor = Color(0xFFD97706),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/8/87/CAF_Champions_League_logo.svg/512px-CAF_Champions_League_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 4
      )
    }

    // 13. الدوري المصري الممتاز (Egyptian Premier League)
    norm.contains("مصري") || norm.contains("كاس مصر") || norm.contains("الدوري المصري") -> {
      TournamentCardStyle(
        tournamentKey = "egypt",
        displayName = "الدوري المصري الممتاز",
        bgGradient = listOf(
          Color(0xFF26050B),
          Color(0xFF3F0B14),
          Color(0xFF160205)
        ),
        accentColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFF59E0B),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/47/Egyptian_Premier_League_logo.png/512px-Egyptian_Premier_League_logo.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 4
      )
    }

    // 14. دوري نجوم العراق (Iraqi Stars League)
    norm.contains("عراق") || norm.contains("نجوم العراق") -> {
      TournamentCardStyle(
        tournamentKey = "iraq",
        displayName = "دوري نجوم العراق",
        bgGradient = listOf(
          Color(0xFF032014),
          Color(0xFF063320),
          Color(0xFF01130B)
        ),
        accentColor = Color(0xFF10B981),
        secondaryColor = Color(0xFFFBBF24),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/4b/Iraq_Stars_League_logo.png/512px-Iraq_Stars_League_logo.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 4
      )
    }

    // 15. دوري نجوم قطر ودوري أدنوك الإماراتي (Gulf Leagues)
    norm.contains("قطر") || norm.contains("نجوم قطر") || norm.contains("امارات") || norm.contains("ادنوك") || norm.contains("كويتي") -> {
      val isQatar = norm.contains("قطر")
      TournamentCardStyle(
        tournamentKey = if (isQatar) "qatar" else "uae",
        displayName = if (isQatar) "دوري نجوم قطر" else "دوري أدنوك الإماراتي",
        bgGradient = listOf(
          Color(0xFF260513),
          Color(0xFF400A22),
          Color(0xFF18030B)
        ),
        accentColor = Color(0xFF8A1538),
        secondaryColor = Color(0xFFFFB800),
        logoUrl = if (isQatar) "https://upload.wikimedia.org/wikipedia/en/thumb/6/67/Qatar_Stars_League_logo.svg/512px-Qatar_Stars_League_logo.svg.png" else "https://upload.wikimedia.org/wikipedia/en/thumb/a/a2/UAE_Pro_League_logo.svg/512px-UAE_Pro_League_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 5
      )
    }

    // 16. رياضات متنوعة وبادل وتنس
    norm.contains("بادل") || norm.contains("تنس") || norm.contains("padel") || norm.contains("tennis") ||
    norm.contains("سله") || norm.contains("يد") -> {
      TournamentCardStyle(
        tournamentKey = "padel",
        displayName = "بث مباشر - رياضات متنوعة",
        bgGradient = listOf(
          Color(0xFF11141B),
          Color(0xFF1A1F29),
          Color(0xFF0A0C11)
        ),
        accentColor = Color(0xFFCCFF00),
        secondaryColor = Color(0xFF38BDF8),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/ATP_Tour_logo.svg/512px-ATP_Tour_logo.svg.png",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 6
      )
    }

    // 17. Fallback heuristics if tournamentName is generic:
    normCombined.contains("ليفربول") || normCombined.contains("ارسنال") || normCombined.contains("مانشستر") ||
    normCombined.contains("تشيلسي") || normCombined.contains("توتنهام") || normCombined.contains("استون فيلا") -> {
      TournamentCardStyle(
        tournamentKey = "pl",
        displayName = "الدوري الإنجليزي الممتاز",
        bgGradient = listOf(Color(0xFF220038), Color(0xFF3B005F), Color(0xFF12001F)),
        accentColor = Color(0xFF00FF85),
        secondaryColor = Color(0xFFFF005A),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Premier_League_Logo.svg/512px-Premier_League_Logo.svg.png",
        priority = 3
      )
    }

    normCombined.contains("ريال مدريد") || normCombined.contains("برشلونه") || normCombined.contains("اتلتيكو مدريد") -> {
      TournamentCardStyle(
        tournamentKey = "laliga",
        displayName = "الدوري الإسباني (LaLiga)",
        bgGradient = listOf(Color(0xFF2E020A), Color(0xFF4A0512), Color(0xFF1A0105)),
        accentColor = Color(0xFFFF2A4B),
        secondaryColor = Color(0xFFFF6B6B),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/LaLiga_logo_2023.svg/512px-LaLiga_logo_2023.svg.png",
        priority = 3
      )
    }

    normCombined.contains("يوفنتوس") || normCombined.contains("انتر ميلان") || normCombined.contains("اي سي ميلان") ||
    normCombined.contains("نابولي") || normCombined.contains("روما") -> {
      TournamentCardStyle(
        tournamentKey = "seriea",
        displayName = "الدوري الإيطالي (Serie A)",
        bgGradient = listOf(Color(0xFF021B38), Color(0xFF052C59), Color(0xFF011022)),
        accentColor = Color(0xFF0091FF),
        secondaryColor = Color(0xFF64D2FF),
        logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Serie_A_logo_2019.svg/512px-Serie_A_logo_2019.svg.png",
        priority = 3
      )
    }

    normCombined.contains("الهلال") || normCombined.contains("النصر") || normCombined.contains("الاتحاد") || normCombined.contains("الاهلي السعودي") -> {
      TournamentCardStyle(
        tournamentKey = "spl",
        displayName = "دوري روشن السعودي",
        bgGradient = listOf(Color(0xFF032612), Color(0xFF063B1C), Color(0xFF01160A)),
        accentColor = Color(0xFF22C55E),
        secondaryColor = Color(0xFFEAB308),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/6/69/Roshn_Saudi_League_logo.svg/512px-Roshn_Saudi_League_logo.svg.png",
        priority = 3
      )
    }

    normCombined.contains("الاهلي المصري") || normCombined.contains("الزمالك") || normCombined.contains("بيراميدز") -> {
      TournamentCardStyle(
        tournamentKey = "egypt",
        displayName = "الدوري المصري الممتاز",
        bgGradient = listOf(Color(0xFF26050B), Color(0xFF3F0B14), Color(0xFF160205)),
        accentColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFF59E0B),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/47/Egyptian_Premier_League_logo.png/512px-Egyptian_Premier_League_logo.png",
        priority = 4
      )
    }

    normCombined.contains("الزوراء") || normCombined.contains("القوه الجويه") || normCombined.contains("الشرطه") -> {
      TournamentCardStyle(
        tournamentKey = "iraq",
        displayName = "دوري نجوم العراق",
        bgGradient = listOf(Color(0xFF032014), Color(0xFF063320), Color(0xFF01130B)),
        accentColor = Color(0xFF10B981),
        secondaryColor = Color(0xFFFBBF24),
        logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/4/4b/Iraq_Stars_League_logo.png/512px-Iraq_Stars_League_logo.png",
        priority = 4
      )
    }

    // 18. Universal Modern Style
    else -> {
      TournamentCardStyle(
        tournamentKey = "general",
        displayName = tournamentName.ifBlank { "مباراة اليوم" },
        bgGradient = listOf(
          Color(0xFF0B1428),
          Color(0xFF132242),
          Color(0xFF070C1A)
        ),
        accentColor = Color(0xFF64D2FF),
        secondaryColor = Color(0xFF0A84FF),
        logoUrl = "",
        watermark = TournamentWatermark.GEOMETRIC_CIRCLES,
        priority = 10
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
    "النرويج", "ويلز", "الكاميرون", "الكونغو", "الغابون", "التشيك",
    "النمسا", "المجر", "سلوفاكيا", "سلوفينيا", "رومانيا", "جورجيا"
  )
  val isCountry = getCountryFlagUrl(team.name) != null ||
      knownCountries.any { team.name.contains(it) } ||
      team.code in listOf("GER", "SRB", "LTU", "AND", "POR", "DEN", "ESP", "FRA", "ITA", "ENG", "BRA", "ARG", "KSA", "EGY", "MAR", "IRQ", "ALG", "TUN", "QAT", "UAE", "GUI", "KEN", "NOR", "WAL", "CMR", "CGO", "GAB", "CZE", "AUT", "HUN", "SVK", "SVN", "ROU", "GEO")

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
 * Elegant Goal Minutes Display badge
 * Formats goal minutes into micro-pills with golden football icon and minute pills
 */
@Composable
fun TodGoalMinutesDisplay(
  goalDetails: String,
  accentColor: Color = Color(0xFFFFD54F),
  modifier: Modifier = Modifier
) {
  val trimmed = goalDetails.trim()
  if (trimmed.isBlank()) return

  val tokens = remember(trimmed) {
    trimmed.split(Regex("[•,]+"))
      .map { it.trim() }
      .filter { it.isNotBlank() }
  }

  if (tokens.isEmpty()) return

  Row(
    modifier = modifier.padding(top = 2.5.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(13.dp)
        .clip(CircleShape)
        .background(Color(0x40000000)),
      contentAlignment = Alignment.Center
    ) {
      Text(text = "⚽", fontSize = 8.5.sp)
    }

    tokens.take(3).forEach { token ->
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(5.dp))
          .background(Color(0x45000000))
          .border(0.6.dp, Color(0x70FFD700), RoundedCornerShape(5.dp))
          .padding(horizontal = 5.dp, vertical = 1.dp)
      ) {
        Text(
          text = token,
          color = Color(0xFFFFE082),
          fontSize = 9.5.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = ThmanyahFontFamily,
          maxLines = 1
        )
      }
    }
    if (tokens.size > 3) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(5.dp))
          .background(Color(0x35000000))
          .border(0.6.dp, Color(0x35FFD700), RoundedCornerShape(5.dp))
          .padding(horizontal = 4.dp, vertical = 1.dp)
      ) {
        Text(
          text = "+${tokens.size - 3}",
          color = Color(0xFFFFD54F),
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = ThmanyahFontFamily
        )
      }
    }
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
  val tournamentStyle = getTournamentCardStyle(match.tournament, match.homeTeam.name, match.awayTeam.name)

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
        .then(if (isCompactWidth) Modifier.width(282.dp).height(188.dp) else Modifier.fillMaxWidth().height(188.dp))
        .iosBounceClick(scaleDown = 0.97f, onClick = { onPlayClick() })
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

            // Left side (END in RTL): Tournament Logo with solid fallback
            val logoUrl = tournamentStyle.logoUrl.ifBlank { match.tournamentLogo }
            if (logoUrl.isNotBlank()) {
              SubcomposeAsyncImage(
                model = logoUrl,
                contentDescription = match.tournament,
                modifier = Modifier
                  .size(28.dp)
                  .padding(1.dp),
                contentScale = ContentScale.Fit,
                error = {
                  Icon(
                    Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = tournamentStyle.accentColor,
                    modifier = Modifier.size(22.dp)
                  )
                }
              )
            } else {
              Icon(
                Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = tournamentStyle.accentColor,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }

        // Divider 1
        Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(Color(0x18FFFFFF)))

        // Band 2: Team 1 Row (Right: Crest/Flag + Team Name + Goal minute, Left: Score)
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
            // Right side (START in RTL): Team Crest/Flag + Team Name + Goals
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f, fill = false)
            ) {
              TodTeamBadgeOrFlag(team = match.homeTeam)
              Column(verticalArrangement = Arrangement.Center) {
                Text(
                  text = match.homeTeam.name,
                  color = Color.White,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = ThmanyahFontFamily,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (!match.homeGoalDetails.isNullOrBlank() && (match.scoreHome ?: 0) > 0) {
                  TodGoalMinutesDisplay(
                    goalDetails = match.homeGoalDetails,
                    accentColor = tournamentStyle.accentColor
                  )
                }
              }
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

        // Band 3: Team 2 Row (Right: Crest/Flag + Team Name + Goal minute, Left: Score)
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
            // Right side (START in RTL): Team Crest/Flag + Team Name + Goals
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f, fill = false)
            ) {
              TodTeamBadgeOrFlag(team = match.awayTeam)
              Column(verticalArrangement = Arrangement.Center) {
                Text(
                  text = match.awayTeam.name,
                  color = Color.White,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = ThmanyahFontFamily,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (!match.awayGoalDetails.isNullOrBlank() && (match.scoreAway ?: 0) > 0) {
                  TodGoalMinutesDisplay(
                    goalDetails = match.awayGoalDetails,
                    accentColor = tournamentStyle.accentColor
                  )
                }
              }
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
 * Clean Alert Dialog when user tries to watch a match with no streaming servers yet
 */
@Composable
fun TodNoStreamAvailableDialog(
  match: SportsMatch,
  onDismiss: () -> Unit,
  onOpenDetails: (() -> Unit)? = null
) {
  androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFF14192A), Color(0xFF0C101A))
          )
        )
        .border(1.dp, Color(0x40FFB800), RoundedCornerShape(24.dp))
        .padding(22.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0x20FFB800)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Tv, contentDescription = null, tint = com.example.ui.theme.TodGold, modifier = Modifier.size(28.dp))
        }

        Text(
          text = "البث المباشر غير متوفر حالياً",
          color = Color.White,
          fontSize = 17.sp,
          fontWeight = FontWeight.Black,
          fontFamily = ThmanyahFontFamily,
          textAlign = TextAlign.Center
        )

        Text(
          text = "${match.homeTeam.name} ضد ${match.awayTeam.name}",
          color = com.example.ui.theme.TodGold,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = ThmanyahFontFamily,
          textAlign = TextAlign.Center
        )

        Text(
          text = "عزيزي المشاهد، ليس لدينا بث مباشر لهذه المباراة حالياً.\nلم يتم تفعيل أو ربط سيرفرات البث من لوحة التحكم، وسيتم إتاحتها فور توفرها ⚽",
          color = Color(0xCCFFFFFF),
          fontSize = 12.5.sp,
          fontFamily = ThmanyahFontFamily,
          textAlign = TextAlign.Center,
          lineHeight = 19.sp
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x18FFFFFF))
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "⏰ ${match.kickoffTime}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ThmanyahFontFamily
          )
          Text(
            text = "🏆 ${match.tournament.take(22)}",
            color = Color(0xBBFFFFFF),
            fontSize = 11.5.sp,
            fontFamily = ThmanyahFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (onOpenDetails != null) {
          androidx.compose.material3.Button(
            onClick = {
              onDismiss()
              onOpenDetails()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
              containerColor = Color(0x28FFFFFF),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("عرض تفاصيل وإحصائيات اللقاء 📋", fontWeight = FontWeight.Bold, fontFamily = ThmanyahFontFamily, fontSize = 12.5.sp)
          }
        }

        androidx.compose.material3.Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = com.example.ui.theme.TodGold,
            contentColor = Color.Black
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("حسناً، فهمت", fontWeight = FontWeight.Black, fontFamily = ThmanyahFontFamily)
        }
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
