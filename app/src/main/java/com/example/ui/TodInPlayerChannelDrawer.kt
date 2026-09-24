package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.BroadcastStream
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGradients

/**
 * Ultra-Modern iOS 18 Liquid Glass In-Player Channel Drawer
 * Features:
 * - Frosted acrylic glass sheet with specular borders
 * - Dynamic category filter capsules
 * - Instant live channel search
 * - Animated Equalizer bars for the actively playing stream
 * - iOS spring physics touch responses
 * - Crystal clear Thmanyah Arabic typography
 */
@Composable
fun TodInPlayerChannelDrawer(
  visible: Boolean,
  channels: List<BroadcastStream>,
  currentStreamId: String,
  onSelectChannel: (BroadcastStream) -> Unit,
  onClose: () -> Unit,
  onExitToHub: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("ALL") }

  // Extract distinct categories from streams
  val categories = remember(channels) {
    val extracted = channels.mapNotNull {
      it.category.ifEmpty { it.subtitle.ifEmpty { null } }
    }.distinct().filter { it.isNotBlank() }
    listOf("ALL") + extracted.take(8)
  }

  // Filter channels based on search and category
  val filteredChannels = remember(channels, searchQuery, selectedCategory) {
    channels.filter { stream ->
      val matchesSearch = searchQuery.isBlank() || stream.title.contains(searchQuery, ignoreCase = true)
      val matchesCategory = selectedCategory == "ALL" ||
        stream.category.equals(selectedCategory, ignoreCase = true) ||
        stream.subtitle.equals(selectedCategory, ignoreCase = true)
      matchesSearch && matchesCategory
    }
  }

  BackHandler(enabled = visible) {
    onClose()
  }

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(tween(250)) + slideInHorizontally(
      animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
      initialOffsetX = { if (isRtl) -it else it }
    ),
    exit = fadeOut(tween(200)) + slideOutHorizontally(
      animationSpec = tween(200),
      targetOffsetX = { if (isRtl) -it else it }
    ),
    modifier = modifier.fillMaxSize()
  ) {
    // Backdrop blur dimmer
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xB205070C))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClose
        )
    ) {
      // iOS 18 Frosted Glass Side Sheet Container
      Box(
        modifier = Modifier
          .align(if (isRtl) Alignment.CenterStart else Alignment.CenterEnd)
          .fillMaxHeight()
          .widthIn(min = 340.dp, max = 390.dp)
          .clip(
            if (isRtl) RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
            else RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
          )
          .background(
            Brush.verticalGradient(
              listOf(
                Color(0xF0121626),
                Color(0xFA0F1220),
                Color(0xFC080A12)
              )
            )
          )
          .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
              listOf(
                Color(0x66FFFFFF),
                Color(0x22FFFFFF),
                Color(0x08FFFFFF)
              )
            ),
            shape = if (isRtl) RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
                    else RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
          )
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {}
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // iOS 18 Sheet Top Indicator Pill
          Box(
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .width(36.dp)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(Color(0x40FFFFFF))
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Header Row: Title + Channel Count + Actions
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left Group: Close & Exit Icons
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val closeInteraction = remember { MutableInteractionSource() }
              val isClosePressed by closeInteraction.collectIsPressedAsState()
              val closeScale by animateFloatAsState(
                targetValue = if (isClosePressed) 0.88f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "drawerCloseScale"
              )

              Box(
                modifier = Modifier
                  .scale(closeScale)
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0x28FFFFFF))
                  .border(0.75.dp, Color(0x35FFFFFF), CircleShape)
                  .clickable(
                    interactionSource = closeInteraction,
                    indication = null,
                    onClick = onClose
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "إغلاق",
                  tint = Color.White,
                  modifier = Modifier.size(17.dp)
                )
              }

              val exitInteraction = remember { MutableInteractionSource() }
              val isExitPressed by exitInteraction.collectIsPressedAsState()
              val exitScale by animateFloatAsState(
                targetValue = if (isExitPressed) 0.88f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "drawerExitScale"
              )

              Box(
                modifier = Modifier
                  .scale(exitScale)
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0x1AFFFFFF))
                  .border(0.75.dp, Color(0x25FFFFFF), CircleShape)
                  .clickable(
                    interactionSource = exitInteraction,
                    indication = null,
                    onClick = onExitToHub
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                  contentDescription = "الخروج للرئيسية",
                  tint = Color(0xFF8E8E93),
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            // Right Group: Title & Live Beacon
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Column(horizontalAlignment = Alignment.End) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "قنوات البث المباشر",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Bold
                  )
                  // Pulsing beacon
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(TodGold)
                  )
                }

                Text(
                  text = "${filteredChannels.size} قناة متاحة",
                  color = Color(0xFF8E8E93),
                  fontSize = 11.sp,
                  fontFamily = ThmanyahFontFamily
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // iOS 18 Frosted Glass Search Bar
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0x22FFFFFF))
              .border(0.75.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
              .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(18.dp)
              )

              Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) {
                  Text(
                    text = "بحث فوري في القنوات...",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.5.sp,
                    fontFamily = ThmanyahFontFamily
                  )
                }

                BasicTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = FontWeight.Medium
                  ),
                  cursorBrush = SolidColor(TodGold),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
              }

              if (searchQuery.isNotEmpty()) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "مسح",
                  tint = Color(0xFF8E8E93),
                  modifier = Modifier
                    .size(16.dp)
                    .clickable { searchQuery = "" }
                )
              }
            }
          }

          // Category Quick-Filter Capsules
          if (categories.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                val displayLabel = if (cat == "ALL") "الكل" else cat
                val catInteraction = remember { MutableInteractionSource() }
                val isCatPressed by catInteraction.collectIsPressedAsState()
                val catScale by animateFloatAsState(
                  targetValue = if (isCatPressed) 0.92f else 1.0f,
                  animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                  label = "catFilterScale"
                )

                Box(
                  modifier = Modifier
                    .scale(catScale)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                      if (isSelected) Modifier.background(TodGradients.LiquidGold)
                      else Modifier.background(Color(0x18FFFFFF))
                    )
                    .border(
                      width = 0.75.dp,
                      color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color(0x22FFFFFF),
                      shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(
                      interactionSource = catInteraction,
                      indication = null
                    ) { selectedCategory = cat }
                    .padding(horizontal = 11.dp, vertical = 5.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = displayLabel,
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 11.5.sp,
                    fontFamily = ThmanyahFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Channel List in iOS 18 Inset Rows
          if (filteredChannels.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LiveTv,
                  contentDescription = null,
                  tint = Color(0xFF555566),
                  modifier = Modifier.size(36.dp)
                )
                Text(
                  text = if (channels.isEmpty()) "لا توجد قائمة قنوات متاحة" else "لم يتم العثور على قنوات تطابق البحث",
                  color = DarkTextSecondary,
                  fontSize = 12.5.sp,
                  fontFamily = ThmanyahFontFamily,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              contentPadding = PaddingValues(bottom = 16.dp)
            ) {
              itemsIndexed(filteredChannels, key = { _, ch -> ch.id }) { index, stream ->
                val isPlaying = stream.id == currentStreamId
                val rowInteraction = remember { MutableInteractionSource() }
                val isRowPressed by rowInteraction.collectIsPressedAsState()
                val rowScale by animateFloatAsState(
                  targetValue = if (isRowPressed) 0.97f else 1.0f,
                  animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                  label = "drawerRowScale"
                )

                val qualityLabel = when {
                  stream.title.contains("4K", ignoreCase = true) -> "4K"
                  stream.title.contains("FHD", ignoreCase = true) || stream.title.contains("1080", ignoreCase = true) -> "FHD"
                  else -> "HD"
                }

                Box(
                  modifier = Modifier
                    .scale(rowScale)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                      if (isPlaying) Color(0x30FEBC11)
                      else Color(0x18FFFFFF)
                    )
                    .border(
                      width = 1.dp,
                      color = if (isPlaying) TodGold else Color(0x22FFFFFF),
                      shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(
                      interactionSource = rowInteraction,
                      indication = null
                    ) {
                      onSelectChannel(stream)
                      onClose()
                    }
                    .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    // Right: Channel Logo + Name + Category
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      // Channel Index Badge
                      Text(
                        text = String.format("%02d", index + 1),
                        color = if (isPlaying) TodGold else Color(0xFF6E6E78),
                        fontSize = 11.sp,
                        fontFamily = ThmanyahFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(22.dp)
                      )

                      // Channel Logo Container
                      Box(
                        modifier = Modifier
                          .size(38.dp)
                          .clip(RoundedCornerShape(10.dp))
                          .background(
                            if (isPlaying) Color(0x40FEBC11)
                            else Color(0x28FFFFFF)
                          )
                          .border(
                            0.75.dp,
                            if (isPlaying) TodGold else Color(0x30FFFFFF),
                            RoundedCornerShape(10.dp)
                          ),
                        contentAlignment = Alignment.Center
                      ) {
                        if (!stream.logoUrl.isNullOrBlank()) {
                          AsyncImage(
                            model = stream.logoUrl,
                            contentDescription = null,
                            modifier = Modifier
                              .fillMaxSize()
                              .padding(3.dp),
                            contentScale = ContentScale.Fit
                          )
                        } else {
                          Icon(
                            imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = if (isPlaying) TodGold else Color.White,
                            modifier = Modifier.size(18.dp)
                          )
                        }
                      }

                      // Titles
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = stream.title,
                          color = if (isPlaying) TodGold else Color.White,
                          fontSize = 13.sp,
                          fontFamily = ThmanyahFontFamily,
                          fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          if (stream.subtitle.isNotEmpty()) {
                            Text(
                              text = stream.subtitle.take(16),
                              color = Color(0xFF8E8E93),
                              fontSize = 10.5.sp,
                              fontFamily = ThmanyahFontFamily,
                              maxLines = 1
                            )
                          }
                          // Quality Tag
                          Box(
                            modifier = Modifier
                              .clip(RoundedCornerShape(4.dp))
                              .background(Color(0x22FFFFFF))
                              .padding(horizontal = 4.dp, vertical = 1.dp)
                          ) {
                            Text(
                              text = qualityLabel,
                              color = if (isPlaying) TodGold else Color(0xFFC0C0C8),
                              fontSize = 9.sp,
                              fontFamily = ThmanyahFontFamily,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }
                      }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Left: Playing State Equalizer or Arrow
                    if (isPlaying) {
                      DrawerEqualizerIndicator()
                    } else {
                      Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل",
                        tint = Color(0x66FFFFFF),
                        modifier = Modifier.size(16.dp)
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
  }
}

/**
 * Animated iOS Equalizer indicator for playing stream
 */
@Composable
private fun DrawerEqualizerIndicator() {
  val transition = rememberInfiniteTransition(label = "eqAnim")
  val bar1 by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "b1"
  )
  val bar2 by transition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(550, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "b2"
  )
  val bar3 by transition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(350, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "b3"
  )

  Row(
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
    modifier = Modifier
      .height(18.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(TodGold.copy(alpha = 0.2f))
      .padding(horizontal = 6.dp, vertical = 3.dp)
  ) {
    Box(
      modifier = Modifier
        .width(2.5.dp)
        .height((12 * bar1).dp)
        .clip(RoundedCornerShape(1.dp))
        .background(TodGold)
    )
    Box(
      modifier = Modifier
        .width(2.5.dp)
        .height((12 * bar2).dp)
        .clip(RoundedCornerShape(1.dp))
        .background(TodGold)
    )
    Box(
      modifier = Modifier
        .width(2.5.dp)
        .height((12 * bar3).dp)
        .clip(RoundedCornerShape(1.dp))
        .background(TodGold)
    )
  }
}
