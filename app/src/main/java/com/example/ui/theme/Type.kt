package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// خط ثمانية الرسمي المعتمد (IBM Plex Sans Arabic) - الخط الرسمي الموحد لجميع صفحات ونصوص التطبيق
val ThmanyahFontFamily = FontFamily(
  Font(R.font.ibm_plex_sans_arabic, FontWeight.Light),
  Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
  Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
  Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.SemiBold),
  Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.Bold),
  Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.ExtraBold),
  Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.Black)
)

val IbmPlexSansArabic = ThmanyahFontFamily

// الخط الموحد المطبق على التطبيق بالكامل
val AppFontFamily = ThmanyahFontFamily

fun getTypography(fontFamily: FontFamily = AppFontFamily) = Typography(
  displayLarge = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp
  ),
  displayMedium = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp
  ),
  displaySmall = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 32.sp
  ),
  headlineLarge = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp
  ),
  headlineSmall = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp
  ),
  titleLarge = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 28.sp
  ),
  titleMedium = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp
  ),
  titleSmall = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp
  ),
  bodySmall = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp
  ),
  labelLarge = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  labelMedium = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
  ),
  labelSmall = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp
  )
)

val Typography = getTypography(AppFontFamily)
