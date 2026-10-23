package com.example.openswos.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openswos.audio.RetroAudio
import com.example.openswos.data.TeamDatabase
import com.example.openswos.model.Difficulty
import com.example.openswos.model.MatchSettings
import com.example.openswos.model.PitchType
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
fun TeamSelectScreen(
    onStartMatch: (MatchSettings) -> Unit,
    onBack: () -> Unit,
    audio: RetroAudio,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Home team picker, 1 = Away team picker
    var homeTeam by remember { mutableStateOf(TeamDatabase.brazil) }
    var awayTeam by remember { mutableStateOf(TeamDatabase.england) }

    var matchMinutes by remember { mutableIntStateOf(3) }
    var pitchType by remember { mutableStateOf(PitchType.GRASS) }
    var difficulty by remember { mutableStateOf(Difficulty.SEMI_PRO) }
    var isClassicControls by remember { mutableStateOf(false) }

    val allTeams = TeamDatabase.allTeams

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(RetroDarkBg, Color(0xFF0F2617), RetroDarkBg)
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
                    modifier = Modifier.testTag("back_to_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SwosGold
                    )
                }

                Text(
                    text = "MATCH SETUP",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 15.sp,
                    color = SwosGold
                )

                IconButton(
                    onClick = {
                        audio.playMenuBeep()
                        val temp = homeTeam
                        homeTeam = awayTeam
                        awayTeam = temp
                    },
                    modifier = Modifier.testTag("swap_teams_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Teams",
                        tint = SwosCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Match Up Preview Cards (HOME vs AWAY)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TeamPreviewCard(
                    team = homeTeam,
                    isHome = true,
                    isSelected = selectedTab == 0,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            audio.playMenuBeep()
                            selectedTab = 0
                        }
                )

                TeamPreviewCard(
                    team = awayTeam,
                    isHome = false,
                    isSelected = selectedTab == 1,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            audio.playMenuBeep()
                            selectedTab = 1
                        }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Team Picker Tab
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0B1710),
                contentColor = SwosGold,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SwosGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        audio.playMenuBeep()
                        selectedTab = 0
                    },
                    text = {
                        Text(
                            "CHOOSE HOME (YOU)",
                            fontFamily = ArcadeFontFamily,
                            fontSize = 8.sp,
                            color = if (selectedTab == 0) SwosYellow else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        audio.playMenuBeep()
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            "CHOOSE AWAY (CPU)",
                            fontFamily = ArcadeFontFamily,
                            fontSize = 8.sp,
                            color = if (selectedTab == 1) SwosYellow else Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Teams Selector List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTeams.forEach { team ->
                    val isChosen = if (selectedTab == 0) team == homeTeam else team == awayTeam
                    TeamSelectionChip(
                        team = team,
                        isSelected = isChosen,
                        onClick = {
                            audio.playMenuBeep()
                            if (selectedTab == 0) {
                                homeTeam = team
                            } else {
                                awayTeam = team
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Match Settings Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RetroCardBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RetroBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "CONDITIONS & DIFFICULTY",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 10.sp,
                        color = SwosGold
                    )

                    // Pitch Selector
                    OptionRow(
                        label = "PITCH",
                        options = listOf(
                            PitchType.GRASS to "Grass",
                            PitchType.WET_MUD to "Mud",
                            PitchType.FROZEN to "Frozen",
                            PitchType.ASTRO to "Astro"
                        ),
                        selected = pitchType,
                        onSelect = {
                            audio.playMenuBeep()
                            pitchType = it
                        }
                    )

                    // Visual Pitch Condition Swatch & Overview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF09140D))
                            .border(1.dp, pitchType.lineColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Mini Pitch Swatch Box
                            Box(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 36.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(pitchType.grassDark)
                                    .border(1.dp, pitchType.lineColor, RoundedCornerShape(4.dp))
                            ) {
                                // Top half lighter stripe
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp)
                                        .background(pitchType.grassLight)
                                )
                                // Halfway line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .align(Alignment.Center)
                                        .background(pitchType.lineColor)
                                )
                                // Ball dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.Center)
                                        .background(pitchType.ballColor)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${pitchType.badgeEmoji} ${pitchType.displayName.uppercase()}",
                                        fontFamily = ArcadeFontFamily,
                                        fontSize = 9.sp,
                                        color = SwosGold
                                    )
                                    Text(
                                        text = "• ${pitchType.weatherName}",
                                        fontFamily = MonoFontFamily,
                                        fontSize = 9.sp,
                                        color = Color(0xFFA3C2AD)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pitchType.description,
                                    fontFamily = MonoFontFamily,
                                    fontSize = 8.sp,
                                    color = PitchLineWhite,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }

                    // Match Length Selector
                    OptionRow(
                        label = "TIME",
                        options = listOf(
                            3 to "3 Min",
                            5 to "5 Min",
                            7 to "7 Min"
                        ),
                        selected = matchMinutes,
                        onSelect = {
                            audio.playMenuBeep()
                            matchMinutes = it
                        }
                    )

                    // Difficulty Selector
                    OptionRow(
                        label = "CPU",
                        options = listOf(
                            Difficulty.AMATEUR to "Amateur",
                            Difficulty.SEMI_PRO to "Semi-Pro",
                            Difficulty.WORLD_CLASS to "World Class"
                        ),
                        selected = difficulty,
                        onSelect = {
                            audio.playMenuBeep()
                            difficulty = it
                        }
                    )

                    // Control scheme
                    OptionRow(
                        label = "PAD",
                        options = listOf(
                            false to "Dual Buttons",
                            true to "Classic 1-Btn"
                        ),
                        selected = isClassicControls,
                        onSelect = {
                            audio.playMenuBeep()
                            isClassicControls = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kick Off Button
            Button(
                onClick = {
                    audio.playWhistle(false)
                    onStartMatch(
                        MatchSettings(
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            matchMinutes = matchMinutes,
                            pitchType = pitchType,
                            difficulty = difficulty,
                            isClassicControls = isClassicControls
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("kick_off_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SwosGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KICK OFF MATCH",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TeamPreviewCard(
    team: Team,
    isHome: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF1B3824) else RetroCardBg),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) SwosGold else RetroBorder
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isHome) "HOME (YOU)" else "AWAY (CPU)",
                fontFamily = ArcadeFontFamily,
                fontSize = 8.sp,
                color = if (isHome) SwosYellow else SwosCyan
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Kit icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(team.jerseyPrimary)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (team.jerseyStripe != null) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(36.dp)
                            .background(team.jerseyStripe)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = team.name,
                fontFamily = ArcadeFontFamily,
                fontSize = 11.sp,
                color = PitchLineWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${team.formation.label} • OVR ${team.overallRating}",
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                color = Color(0xFFA3C2AD)
            )

            // Star player
            val star = team.roster.maxByOrNull { it.stats.overall }?.name ?: "Captain"
            Text(
                text = "Star: $star",
                fontFamily = MonoFontFamily,
                fontSize = 10.sp,
                color = SwosGold
            )
        }
    }
}

@Composable
fun TeamSelectionChip(
    team: Team,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) SwosGold else Color(0xFF132219))
            .border(1.dp, if (isSelected) Color.White else RetroBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(team.jerseyPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = team.shortName,
                fontFamily = ArcadeFontFamily,
                fontSize = 9.sp,
                color = if (isSelected) Color.Black else PitchLineWhite,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun <T> OptionRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = ArcadeFontFamily,
            fontSize = 9.sp,
            color = Color(0xFFA3C2AD),
            modifier = Modifier.width(55.dp)
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (value, title) ->
                val isChosen = value == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isChosen) SwosGold else Color(0xFF111E16))
                        .border(1.dp, if (isChosen) Color.White else RetroBorder, RoundedCornerShape(4.dp))
                        .clickable { onSelect(value) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = if (isChosen) Color.Black else PitchLineWhite,
                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
