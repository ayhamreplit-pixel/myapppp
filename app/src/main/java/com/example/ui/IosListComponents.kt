package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThmanyahFontFamily
import com.example.ui.theme.TodGold
import com.example.ui.theme.TodGradients

/**
 * Apple iOS Palette Presets for List Row Icon Badges
 */
object IosBadgeColors {
  val Blue = Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF0055D4)))
  val Green = Brush.linearGradient(listOf(Color(0xFF30D158), Color(0xFF1E8238)))
  val Orange = Brush.linearGradient(listOf(Color(0xFFFF9F0A), Color(0xFFD66000)))
  val Red = Brush.linearGradient(listOf(Color(0xFFFF453A), Color(0xFFC0150D)))
  val Purple = Brush.linearGradient(listOf(Color(0xFFBF5AF2), Color(0xFF7A24A6)))
  val Indigo = Brush.linearGradient(listOf(Color(0xFF5E5CE6), Color(0xFF3835B3)))
  val Teal = Brush.linearGradient(listOf(Color(0xFF64D2FF), Color(0xFF0E85B8)))
  val Pink = Brush.linearGradient(listOf(Color(0xFFFF375F), Color(0xFFB80B32)))
  val Slate = Brush.linearGradient(listOf(Color(0xFF8E8E93), Color(0xFF48484A)))
  val Gold = TodGradients.LiquidGold
}

/**
 * Apple iOS Spring Press & Bounce Feedback Modifier
 */
fun Modifier.iosBounceClick(
  scaleDown: Float = 0.94f,
  alphaDown: Float = 0.80f,
  onClick: (() -> Unit)? = null
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) scaleDown else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "iosBounceScale"
  )
  val alpha by animateFloatAsState(
    targetValue = if (isPressed) alphaDown else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "iosBounceAlpha"
  )

  this
    .scale(scale)
    .alpha(alpha)
    .then(
      if (onClick != null) {
        Modifier.clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick
        )
      } else Modifier
    )
}

/**
 * iOS Iconic Rounded Squircle Icon Badge
 */
@Composable
fun IosIconBadge(
  icon: ImageVector,
  background: Brush,
  modifier: Modifier = Modifier,
  tint: Color = Color.White,
  size: Dp = 32.dp,
  iconSize: Dp = 19.dp
) {
  Box(
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(8.dp))
      .background(background)
      .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(iconSize)
    )
  }
}

/**
 * Apple iOS Inset Grouped Card Container
 */
@Composable
fun IosListGroup(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0x28FFFFFF),
            Color(0x14FFFFFF),
            Color(0x0C121724)
          )
        )
      )
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0x55FFFFFF),
            Color(0x1CFFFFFF),
            Color(0x0AFFFFFF)
          )
        ),
        shape = RoundedCornerShape(20.dp)
      )
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      content = content
    )
  }
}

/**
 * Apple iOS Inset Grouped List Row
 */
@Composable
fun IosListRow(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  value: String? = null,
  valueColor: Color = Color(0xFF8E8E93),
  iconBadge: (@Composable () -> Unit)? = null,
  trailing: (@Composable () -> Unit)? = null,
  showChevron: Boolean = true,
  showDivider: Boolean = true,
  destructive: Boolean = false,
  onClick: (() -> Unit)? = null
) {
  val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val rowScale by animateFloatAsState(
    targetValue = if (isPressed && onClick != null) 0.985f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "iosRowScale"
  )

  Column(
    modifier = modifier
      .scale(rowScale)
      .fillMaxWidth()
      .then(
        if (onClick != null) {
          Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
          )
        } else Modifier
      )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(if (isPressed && onClick != null) Color(0x20FFFFFF) else Color.Transparent)
        .padding(horizontal = 16.dp, vertical = 13.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Right side in RTL (or Left in LTR): Icon + Titles
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.weight(1f, fill = false)
      ) {
        iconBadge?.invoke()

        Column {
          Text(
            text = title,
            color = if (destructive) Color(0xFFFF453A) else Color.White,
            fontSize = 15.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = subtitle,
              color = Color(0xFF8E8E93),
              fontSize = 12.sp,
              fontFamily = ThmanyahFontFamily,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              lineHeight = 16.sp
            )
          }
        }
      }

      // Left side in RTL (or Right in LTR): Value / Trailing / Chevron
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (!value.isNullOrBlank()) {
          Text(
            text = value,
            color = valueColor,
            fontSize = 13.5.sp,
            fontFamily = ThmanyahFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = if (isRtl) TextAlign.Start else TextAlign.End
          )
        }

        trailing?.invoke()

        if (showChevron && onClick != null && trailing == null) {
          Icon(
            imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF5C5C62),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    if (showDivider) {
      val dividerStart = if (iconBadge != null) 58.dp else 16.dp
      HorizontalDivider(
        modifier = Modifier.padding(start = dividerStart, end = 0.dp),
        thickness = 0.5.dp,
        color = Color(0x18FFFFFF)
      )
    }
  }
}

/**
 * Apple iOS Toggle Switch
 */
@Composable
fun IosSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  activeTrackColor: Color = TodGold
) {
  val thumbOffset by animateDpAsState(
    targetValue = if (checked) 22.dp else 2.dp,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "iosSwitchThumb"
  )

  Box(
    modifier = modifier
      .width(51.dp)
      .height(31.dp)
      .clip(CircleShape)
      .background(if (checked) activeTrackColor else Color(0xFF39393D))
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 2.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Box(
      modifier = Modifier
        .offset(x = thumbOffset)
        .size(27.dp)
        .shadow(4.dp, CircleShape)
        .clip(CircleShape)
        .background(Color.White)
    )
  }
}

/**
 * Apple iOS Segmented Control
 */
@Composable
fun IosSegmentedControl(
  items: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(38.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(Color(0xFF1E1E26))
      .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
      .padding(3.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      items.forEachIndexed { index, title ->
        val isSelected = index == selectedIndex
        val itemScale by animateFloatAsState(
          targetValue = if (isSelected) 1.0f else 0.96f,
          label = "iosSegmentScale_$index"
        )

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .scale(itemScale)
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (isSelected) Color(0xFF2C2C36) else Color.Transparent
            )
            .then(
              if (isSelected) Modifier.border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
              else Modifier
            )
            .clickable { onSelect(index) },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFF8E8E93),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }
  }
}

/**
 * Apple iOS Section Header with subtle glowing category tag
 */
@Composable
fun IosSectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 18.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .width(3.dp)
          .height(13.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(Color(0xFFFDB913))
      )
      Text(
        text = title,
        color = Color(0xFF98989F),
        fontSize = 13.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp
      )
    }

    if (actionText != null && onActionClick != null) {
      Text(
        text = actionText,
        color = Color(0xFF0A84FF),
        fontSize = 12.5.sp,
        fontFamily = ThmanyahFontFamily,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable { onActionClick() }
      )
    }
  }
}

/**
 * Apple iOS Section Footer
 */
@Composable
fun IosSectionFooter(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    color = Color(0xFF636366),
    fontSize = 12.sp,
    lineHeight = 16.sp,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 18.dp, vertical = 6.dp)
  )
}

/**
 * Apple iOS Ultra-Modern Seamless Navigation Bar (Continuous Edge-to-Edge)
 */
@Composable
fun IosNavigationBar(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  onBack: (() -> Unit)? = null,
  trailing: (@Composable () -> Unit)? = null
) {
  val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xEE12131C),
            Color(0xAA0D0E15),
            Color.Transparent
          )
        )
      )
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Back Button with iOS Spring Tap
      if (onBack != null) {
        Box(
          modifier = Modifier
            .iosBounceClick(scaleDown = 0.88f, onClick = onBack)
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0x28FFFFFF)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "رجوع",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      } else {
        Spacer(modifier = Modifier.size(38.dp))
      }

      // Center: Title & Subtitle with Luxury iOS SF Pro Typography
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 8.dp)
      ) {
        Text(
          text = title,
          color = Color.White,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.2.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = subtitle,
            color = Color(0xFF8E8E93),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Right: Trailing Action Button
      if (trailing != null) {
        trailing()
      } else {
        Spacer(modifier = Modifier.size(38.dp))
      }
    }
  }
}

/**
 * Apple iOS Inset Grouped Text Field Row
 */
@Composable
fun IosTextFieldRow(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  label: String? = null,
  iconBadge: (@Composable () -> Unit)? = null,
  showDivider: Boolean = true
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      iconBadge?.invoke()

      if (!label.isNullOrBlank()) {
        Text(
          text = label,
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.width(60.dp)
        )
      }

      androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
          color = Color.White,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Normal
        ),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0A84FF)),
        modifier = Modifier
          .weight(1f)
          .padding(vertical = 6.dp),
        decorationBox = { innerTextField ->
          Box(contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
              Text(
                text = placeholder,
                color = Color(0xFF636366),
                fontSize = 14.5.sp
              )
            }
            innerTextField()
          }
        }
      )
    }

    if (showDivider) {
      HorizontalDivider(
        modifier = Modifier.padding(start = if (iconBadge != null) 56.dp else 16.dp),
        thickness = 0.5.dp,
        color = Color(0x1FFFFFFF)
      )
    }
  }
}

/**
 * Apple iOS Sheet Top Grabber Pill
 */
@Composable
fun IosGrabber(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .width(38.dp)
        .height(5.dp)
        .clip(RoundedCornerShape(2.5.dp))
        .background(Color(0x55FFFFFF))
    )
  }
}
