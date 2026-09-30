package com.example.ui

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.SportsBackendRepository
import com.example.model.TodUserProfile
import com.example.ui.theme.AppFontFamily
import com.example.ui.theme.TodGold

/**
 * TOD "من يشاهد الآن؟" (Who is Watching Profile Gateway)
 * Matches the official TOD & Netflix profile selection before entering the app.
 */
@Composable
fun TodProfileSelectScreen(
  sportsBackendRepo: SportsBackendRepository,
  onProfileSelected: (TodUserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  val profiles = remember { sportsBackendRepo.getUserProfiles() }
  var profileList by remember { mutableStateOf(profiles) }
  var activeProfile by remember { mutableStateOf(sportsBackendRepo.getActiveProfile()) }
  var showAddProfileDialog by remember { mutableStateOf(false) }
  var newProfileName by remember { mutableStateOf("") }
  var selectedEmoji by remember { mutableStateOf("⚽") }

  val emojiOptions = listOf("⚽", "👑", "🔥", "🏆", "🎯", "⚡", "🌟", "🎮")

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFF141728),
            Color(0xFF0C0E1A),
            Color(0xFF05060C)
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 24.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top TOD Official Branding
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp)
      ) {
        TodLogo(fontSize = 32)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
          text = "من يشاهد الآن؟",
          color = Color.White,
          fontSize = 26.sp,
          fontWeight = FontWeight.Black,
          fontFamily = AppFontFamily,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "اختر ملفك الشخصي لمتابعة البث والمباريات المباشرة",
          color = Color(0xFFA5A9BA),
          fontSize = 13.5.sp,
          fontFamily = AppFontFamily,
          textAlign = TextAlign.Center
        )
      }

      // Profiles Grid
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(profileList, key = { it.id }) { profile ->
          val isSelected = activeProfile.id == profile.id
          val cardInteraction = remember { MutableInteractionSource() }
          val isPressed by cardInteraction.collectIsPressedAsState()
          val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.94f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "profileScale"
          )

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .scale(scale)
              .clickable(
                interactionSource = cardInteraction,
                indication = null,
                onClick = {
                  activeProfile = profile
                  sportsBackendRepo.setActiveProfile(profile)
                  onProfileSelected(profile)
                }
              )
          ) {
            Box(
              modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                  Brush.linearGradient(
                    listOf(
                      Color(profile.avatarGradientHex),
                      Color(profile.avatarGradientHex).copy(alpha = 0.5f),
                      Color(0xFF0F1222)
                    )
                  )
                )
                .border(
                  width = if (isSelected) 2.5.dp else 1.dp,
                  color = if (isSelected) TodGold else Color(0x40FFFFFF),
                  shape = RoundedCornerShape(26.dp)
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = profile.avatarEmoji,
                fontSize = 42.sp
              )

              if (profile.isVip) {
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(TodGold),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                }
              }

              if (isSelected) {
                Box(
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A84FF)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = profile.name,
              color = if (isSelected) TodGold else Color.White,
              fontSize = 15.sp,
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
              fontFamily = AppFontFamily,
              textAlign = TextAlign.Center
            )
          }
        }

        // Add Profile Card
        item(key = "add_profile_card") {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { showAddProfileDialog = true }
          ) {
            Box(
              modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0x18FFFFFF))
                .border(1.2.dp, Color(0x35FFFFFF), RoundedCornerShape(26.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Add, contentDescription = "إضافة ملف", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "إضافة ملف +",
              color = Color(0xCCFFFFFF),
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Medium,
              fontFamily = AppFontFamily
            )
          }
        }
      }

      // Bottom Server Status & Quick Continue
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x15FFFFFF))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64D2FF), modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "متصل بالسيرفر السحابي https://ayham.alwaysdata.net",
            color = Color(0xFF8E93A6),
            fontSize = 11.sp,
            fontFamily = AppFontFamily
          )
        }
      }
    }

    // Add Profile Dialog
    if (showAddProfileDialog) {
      AlertDialog(
        onDismissRequest = { showAddProfileDialog = false },
        title = {
          Text("إضافة ملف شخصي جديد", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = AppFontFamily)
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("اختر رمزاً واسماً للملف الشخصي:", color = Color(0xFFCCCCCC), fontSize = 13.sp, fontFamily = AppFontFamily)

            // Emoji Picker Row
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              emojiOptions.forEach { emoji ->
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (selectedEmoji == emoji) TodGold else Color(0x20FFFFFF))
                    .clickable { selectedEmoji = emoji },
                  contentAlignment = Alignment.Center
                ) {
                  Text(emoji, fontSize = 20.sp)
                }
              }
            }

            OutlinedTextField(
              value = newProfileName,
              onValueChange = { newProfileName = it },
              placeholder = { Text("اسم المشاهد / الملف", color = Color.Gray) },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = TodGold,
                unfocusedBorderColor = Color(0x40FFFFFF)
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (newProfileName.isNotBlank()) {
                sportsBackendRepo.addProfile(newProfileName.trim(), selectedEmoji)
                profileList = sportsBackendRepo.getUserProfiles()
                showAddProfileDialog = false
                newProfileName = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TodGold, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("إضافة", fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddProfileDialog = false }) {
            Text("إلغاء", color = Color.White, fontFamily = AppFontFamily)
          }
        },
        containerColor = Color(0xFF1B1E32),
        shape = RoundedCornerShape(20.dp)
      )
    }
  }
}
