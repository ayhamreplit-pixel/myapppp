package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TodGold

/**
 * Android TV / TV Box Remote Control D-Pad Focus Modifier
 * Adds high-visibility glowing highlight, scale feedback, and D-Pad OK / Enter click support.
 */
fun Modifier.tvFocusable(
  shape: Shape = RoundedCornerShape(16.dp),
  focusBorderWidth: Dp = 2.5.dp,
  focusGlowColor: Color = TodGold,
  scaleOnFocus: Float = 1.04f,
  onEnterClick: (() -> Unit)? = null
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isFocused) scaleOnFocus else 1.0f,
    animationSpec = tween(150),
    label = "tvFocusScale"
  )

  this
    .scale(scale)
    .then(
      if (isFocused) {
        Modifier
          .shadow(12.dp, shape, spotColor = focusGlowColor, ambientColor = focusGlowColor)
          .border(
            width = focusBorderWidth,
            brush = Brush.linearGradient(
              listOf(focusGlowColor, Color.White, focusGlowColor)
            ),
            shape = shape
          )
      } else Modifier
    )
    .focusable(interactionSource = interactionSource)
    .then(
      if (onEnterClick != null) {
        Modifier.onKeyEvent { event ->
          if (event.type == KeyEventType.KeyUp) {
            when (event.key) {
              Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.ButtonA -> {
                onEnterClick()
                true
              }
              else -> false
            }
          } else false
        }
      } else Modifier
    )
}
