package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val IbmPlexSansArabic = FontFamily(
  Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
  Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
  Font(R.font.ibm_plex_sans_arabic, FontWeight.SemiBold),
  Font(R.font.ibm_plex_sans_arabic, FontWeight.Bold)
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = IbmPlexSansArabic,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp
  ),
  displayMedium = TextStyle(
    fontFamily = IbmPlexSansArabic,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp
  ),
  titleLarge = TextStyle(
    fontFamily = IbmPlexSansArabic,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 28.sp
  ),
  titleMedium = TextStyle(
    fontFamily = IbmPlexSansArabic,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = IbmPlexSansArabic,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = IbmPlexSansArabic,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp
  ),
  labelLarge = TextStyle(
    fontFamily = IbmPlexSansArabic,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  labelMedium = TextStyle(
    fontFamily = IbmPlexSansArabic,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
  )
)
