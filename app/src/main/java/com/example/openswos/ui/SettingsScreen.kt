package com.example.openswos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openswos.audio.RetroAudio
import com.example.ui.theme.ArcadeFontFamily
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchLineWhite
import com.example.ui.theme.RetroBorder
import com.example.ui.theme.RetroCardBg
import com.example.ui.theme.RetroDarkBg
import com.example.ui.theme.SwosCyan
import com.example.ui.theme.SwosGold
import com.example.ui.theme.SwosYellow

@Composable
fun SettingsScreen(
    audio: RetroAudio,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var soundEnabled by remember { mutableStateOf(audio.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var classicPadMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(RetroDarkBg, Color(0xFF0F2618), RetroDarkBg)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        audio.playMenuBeep()
                        onBack()
                    },
                    modifier = Modifier.testTag("settings_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SwosGold
                    )
                }

                Text(
                    text = "GAME SETTINGS",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 13.sp,
                    color = SwosGold
                )

                Box(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio & Feedback Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RetroCardBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RetroBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "AUDIO & HAPTICS",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 10.sp,
                        color = SwosGold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("16-Bit Audio Sound FX", fontFamily = MonoFontFamily, fontSize = 14.sp, color = PitchLineWhite)
                            Text("Whistles, kicks, crowd chants, net hits", fontFamily = MonoFontFamily, fontSize = 11.sp, color = Color(0xFFA3C2AD))
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                audio.soundEnabled = it
                                if (it) audio.playMenuBeep()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = SwosGold, checkedTrackColor = Color(0xFF1B5E20))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Vibration", fontFamily = MonoFontFamily, fontSize = 14.sp, color = PitchLineWhite)
                            Text("Tactile feedback on shots and tackles", fontFamily = MonoFontFamily, fontSize = 11.sp, color = Color(0xFFA3C2AD))
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SwosGold, checkedTrackColor = Color(0xFF1B5E20))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Control Scheme Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RetroCardBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RetroBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "CONTROL SCHEME",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 10.sp,
                        color = SwosGold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (classicPadMode) "Classic 1-Button SWOS" else "Modern Dual Buttons",
                                fontFamily = MonoFontFamily,
                                fontSize = 14.sp,
                                color = PitchLineWhite
                            )
                            Text(
                                text = if (classicPadMode) "Original 1996 Amiga action button (tap=pass, hold=shoot, release=curve)" else "Dedicated PASS and SHOOT / TACKLE buttons",
                                fontFamily = MonoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFA3C2AD)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = classicPadMode,
                            onCheckedChange = { classicPadMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SwosGold, checkedTrackColor = Color(0xFF1B5E20))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Heritage & About Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RetroCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, RetroBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ABOUT OPENSWOS",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 10.sp,
                        color = SwosCyan
                    )

                    Text(
                        text = "OpenSWOS is an open-source recreation of Sensible World of Soccer 96/97 originally created by Sensible Software.",
                        fontFamily = MonoFontFamily,
                        fontSize = 12.sp,
                        color = PitchLineWhite
                    )

                    Text(
                        text = "This native Android edition brings full 60FPS Compose Canvas match rendering, genuine Magnus aftertouch ball curling, 11v11 tactics, international knockout tournaments, and procedural retro sound synthesizer.",
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFA3C2AD)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ported with Kotlin & Jetpack Compose",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 8.sp,
                        color = SwosYellow
                    )
                }
            }
        }
    }
}
