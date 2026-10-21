package com.example.openswos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openswos.audio.RetroAudio
import com.example.openswos.data.TeamDatabase
import com.example.openswos.model.FormationType
import com.example.openswos.model.Player
import com.example.openswos.model.Team
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
fun TacticsScreen(
    currentTeam: Team,
    audio: RetroAudio,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTeam by remember { mutableStateOf(currentTeam) }
    var selectedFormation by remember { mutableStateOf(currentTeam.formation) }

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
                    modifier = Modifier.testTag("tactics_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SwosGold
                    )
                }

                Text(
                    text = "TACTICS & SQUAD",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 13.sp,
                    color = SwosGold
                )

                Box(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formation Switcher Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormationType.values().forEach { form ->
                    val isChosen = form == selectedFormation
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isChosen) SwosGold else Color(0xFF132B1C))
                            .border(1.dp, if (isChosen) Color.White else RetroBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                audio.playMenuBeep()
                                selectedFormation = form
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = form.label,
                            fontFamily = ArcadeFontFamily,
                            fontSize = 10.sp,
                            color = if (isChosen) Color.Black else PitchLineWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tactical Pitch Board
            TacticalPitchBoard(
                team = selectedTeam,
                formation = selectedFormation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Squad Roster Table Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedTeam.name.uppercase()} ROSTER",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 11.sp,
                    color = SwosYellow
                )
                Text(
                    text = "SPD SHT PAS TCK CTL OVR",
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFFA3C2AD),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Squad Roster List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                selectedTeam.roster.forEach { player ->
                    PlayerRowItem(player = player, team = selectedTeam)
                }
            }
        }
    }
}

@Composable
fun TacticalPitchBoard(
    team: Team,
    formation: FormationType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF124A22)),
        border = androidx.compose.foundation.BorderStroke(2.dp, SwosGold),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Pitch outer and halfway lines
                drawRect(Color(0x66FFFFFF), Offset(10f, 10f), Size(w - 20f, h - 20f), style = Stroke(2f))
                drawLine(Color(0x66FFFFFF), Offset(10f, h / 2f), Offset(w - 20f, h / 2f), strokeWidth = 1.5f)
                drawCircle(Color(0x66FFFFFF), radius = 35f, center = Offset(w / 2f, h / 2f), style = Stroke(1.5f))

                // Penalty areas
                drawRect(Color(0x44FFFFFF), Offset(w * 0.25f, 10f), Size(w * 0.5f, 45f), style = Stroke(1.5f))
                drawRect(Color(0x44FFFFFF), Offset(w * 0.25f, h - 55f), Size(w * 0.5f, 45f), style = Stroke(1.5f))
            }

            // Draw player badges based on formation
            val anchors = when (formation) {
                FormationType.F_4_4_2 -> listOf(
                    0.5f to 0.88f,
                    0.18f to 0.72f, 0.38f to 0.74f, 0.62f to 0.74f, 0.82f to 0.72f,
                    0.15f to 0.48f, 0.38f to 0.50f, 0.62f to 0.50f, 0.85f to 0.48f,
                    0.38f to 0.26f, 0.62f to 0.26f
                )
                FormationType.F_4_3_3 -> listOf(
                    0.5f to 0.88f,
                    0.18f to 0.72f, 0.38f to 0.74f, 0.62f to 0.74f, 0.82f to 0.72f,
                    0.25f to 0.52f, 0.50f to 0.55f, 0.75f to 0.52f,
                    0.20f to 0.26f, 0.50f to 0.22f, 0.80f to 0.26f
                )
                FormationType.F_3_5_2 -> listOf(
                    0.5f to 0.88f,
                    0.28f to 0.74f, 0.50f to 0.76f, 0.72f to 0.74f,
                    0.14f to 0.50f, 0.35f to 0.54f, 0.50f to 0.46f, 0.65f to 0.54f, 0.86f to 0.50f,
                    0.38f to 0.26f, 0.62f to 0.26f
                )
                FormationType.F_5_3_2 -> listOf(
                    0.5f to 0.88f,
                    0.15f to 0.72f, 0.32f to 0.74f, 0.50f to 0.76f, 0.68f to 0.74f, 0.85f to 0.72f,
                    0.28f to 0.50f, 0.50f to 0.48f, 0.72f to 0.50f,
                    0.38f to 0.26f, 0.62f to 0.26f
                )
                FormationType.F_4_2_4 -> listOf(
                    0.5f to 0.88f,
                    0.18f to 0.72f, 0.38f to 0.74f, 0.62f to 0.74f, 0.82f to 0.72f,
                    0.38f to 0.52f, 0.62f to 0.52f,
                    0.15f to 0.28f, 0.38f to 0.24f, 0.62f to 0.24f, 0.85f to 0.28f
                )
            }

            anchors.forEachIndexed { idx, (nx, ny) ->
                val player = team.roster.getOrNull(idx)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                start = (nx * 280).dp,
                                top = (ny * 220).dp
                            )
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (idx == 0) Color(0xFF00E676) else team.jerseyPrimary)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${player?.number ?: (idx + 1)}",
                            fontFamily = ArcadeFontFamily,
                            fontSize = 7.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerRowItem(player: Player, team: Team) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RetroCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetroBorder),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (player.position.name == "GK") Color(0xFF00E676) else team.jerseyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${player.number}",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 7.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = player.name,
                        fontFamily = ArcadeFontFamily,
                        fontSize = 9.sp,
                        color = PitchLineWhite
                    )
                    Text(
                        text = player.position.name,
                        fontFamily = MonoFontFamily,
                        fontSize = 10.sp,
                        color = SwosCyan
                    )
                }
            }

            // Stats values
            val stats = player.stats
            val sSpd = (stats.speed * 10).toInt()
            val sSht = (stats.shooting * 10).toInt()
            val sPas = (stats.passing * 10).toInt()
            val sTck = (stats.tackling * 10).toInt()
            val sCtl = (stats.control * 10).toInt()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$sSpd", fontFamily = MonoFontFamily, fontSize = 11.sp, color = Color(0xFFA3C2AD))
                Text("$sSht", fontFamily = MonoFontFamily, fontSize = 11.sp, color = Color(0xFFA3C2AD))
                Text("$sPas", fontFamily = MonoFontFamily, fontSize = 11.sp, color = Color(0xFFA3C2AD))
                Text("$sTck", fontFamily = MonoFontFamily, fontSize = 11.sp, color = Color(0xFFA3C2AD))
                Text("$sCtl", fontFamily = MonoFontFamily, fontSize = 11.sp, color = Color(0xFFA3C2AD))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(SwosGold)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${stats.overall}",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 8.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
