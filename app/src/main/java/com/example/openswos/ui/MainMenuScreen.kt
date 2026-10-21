package com.example.openswos.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openswos.audio.RetroAudio
import com.example.ui.theme.ArcadeFontFamily
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.PitchLineWhite
import com.example.ui.theme.RetroBorder
import com.example.ui.theme.RetroCardBg
import com.example.ui.theme.RetroDarkBg
import com.example.ui.theme.SwosCyan
import com.example.ui.theme.SwosGold
import com.example.ui.theme.SwosYellow

@Composable
fun MainMenuScreen(
    onPlayQuickMatch: () -> Unit,
    onPlayTournament: () -> Unit,
    onPlayPenalties: () -> Unit,
    onOpenTactics: () -> Unit,
    onOpenSettings: () -> Unit,
    audio: RetroAudio,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ball_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF072111),
                        Color(0xFF0E381C),
                        Color(0xFF081C10)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Title Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Retro Ball Logo Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(PitchGreen, Color(0xFF0F4D24))
                            )
                        )
                        .border(2.dp, SwosGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Soccer Ball",
                        tint = PitchLineWhite,
                        modifier = Modifier
                            .size(46.dp)
                            .rotate(rotation)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // OPEN SWOS arcade text
                Text(
                    text = "OPEN SWOS",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 28.sp,
                    color = SwosGold,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "SENSIBLE WORLD OF SOCCER RETRO ENGINE",
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFFA3C2AD),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Aftertouch Tag badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF052110))
                        .border(1.dp, SwosCyan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "GENUINE 60FPS AFTERTOUCH CURVE PHYSICS",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 7.sp,
                        color = SwosCyan
                    )
                }
            }

            // Menu Items List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuButton(
                    title = "EXHIBITION MATCH",
                    subtitle = "Quick friendly game • Custom pitch & teams",
                    icon = Icons.Default.PlayArrow,
                    accentColor = SwosGold,
                    testTag = "menu_btn_exhibition",
                    onClick = {
                        audio.playMenuBeep()
                        onPlayQuickMatch()
                    }
                )

                MenuButton(
                    title = "WORLD TOURNAMENT",
                    subtitle = "16-Nation knockout cup to lift the trophy",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = SwosYellow,
                    testTag = "menu_btn_tournament",
                    onClick = {
                        audio.playMenuBeep()
                        onPlayTournament()
                    }
                )

                MenuButton(
                    title = "PENALTY SHOOTOUT",
                    subtitle = "Test your aftertouch curves & saves",
                    icon = Icons.Default.SportsSoccer,
                    accentColor = SwosCyan,
                    testTag = "menu_btn_penalties",
                    onClick = {
                        audio.playMenuBeep()
                        onPlayPenalties()
                    }
                )

                MenuButton(
                    title = "TACTICS & SQUAD EDITOR",
                    subtitle = "Formations, custom lineups & squad stats",
                    icon = Icons.Default.Tune,
                    accentColor = Color(0xFF66BB6A),
                    testTag = "menu_btn_tactics",
                    onClick = {
                        audio.playMenuBeep()
                        onOpenTactics()
                    }
                )

                MenuButton(
                    title = "GAME SETTINGS",
                    subtitle = "Controls, sound, pitch & difficulty",
                    icon = Icons.Default.Settings,
                    accentColor = Color(0xFF90CAF9),
                    testTag = "menu_btn_settings",
                    onClick = {
                        audio.playMenuBeep()
                        onOpenSettings()
                    }
                )
            }

            // Footer credits
            Text(
                text = "OpenSWOS Amiga Tribute • Jetpack Compose Edition",
                fontFamily = MonoFontFamily,
                fontSize = 10.sp,
                color = Color(0xFF6B8B74),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = RetroCardBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, RetroBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F1E17))
                    .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = ArcadeFontFamily,
                    fontSize = 11.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFFA3C2AD)
                )
            }
        }
    }
}
