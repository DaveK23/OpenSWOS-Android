package com.example.openswos.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openswos.audio.RetroAudio
import com.example.openswos.data.TeamDatabase
import com.example.openswos.model.MatchSettings
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
import kotlin.random.Random

data class TournamentMatch(
    val teamA: Team,
    val teamB: Team,
    var scoreA: Int? = null,
    var scoreB: Int? = null,
    var winner: Team? = null
)

class TournamentState(val userTeam: Team) {
    // 8 Teams for Quarter-Finals
    val qfMatches: List<TournamentMatch>
    var sfMatches: List<TournamentMatch>? = null
    var finalMatch: TournamentMatch? = null
    var champion: Team? = null
    var currentRoundName: String = "QUARTER FINALS"

    init {
        val pool = (TeamDatabase.allTeams.filter { !it.isClub } + listOf(TeamDatabase.brazil, TeamDatabase.england, TeamDatabase.argentina, TeamDatabase.germany, TeamDatabase.italy, TeamDatabase.france, TeamDatabase.netherlands, TeamDatabase.spain))
            .distinct()
            .take(8)
            .toMutableList()

        if (!pool.contains(userTeam)) {
            pool[0] = userTeam
        } else {
            pool.remove(userTeam)
            pool.add(0, userTeam)
        }

        qfMatches = listOf(
            TournamentMatch(pool[0], pool[1]),
            TournamentMatch(pool[2], pool[3]),
            TournamentMatch(pool[4], pool[5]),
            TournamentMatch(pool[6], pool[7])
        )
    }

    fun simulateOtherMatches() {
        if (sfMatches == null) {
            // Check if QF done
            qfMatches.forEachIndexed { idx, m ->
                if (idx != 0 && m.winner == null) {
                    val sA = Random.nextInt(0, 4)
                    val sB = if (sA == 0) Random.nextInt(1, 4) else Random.nextInt(0, 3)
                    m.scoreA = sA
                    m.scoreB = sB
                    m.winner = if (sA > sB) m.teamA else m.teamB
                }
            }
        }
    }
}

@Composable
fun TournamentScreen(
    userTeam: Team,
    audio: RetroAudio,
    onPlayTournamentMatch: (MatchSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var tournament by remember { mutableStateOf(TournamentState(userTeam)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(RetroDarkBg, Color(0xFF0D2517), RetroDarkBg)
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
            // Header
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
                    modifier = Modifier.testTag("tournament_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SwosGold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = SwosGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WORLD CUP KNOCKOUT",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 12.sp,
                        color = SwosGold
                    )
                }

                Box(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Team Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132B1B)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SwosGold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(userTeam.jerseyPrimary)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MANAGING ${userTeam.name.uppercase()}",
                                fontFamily = ArcadeFontFamily,
                                fontSize = 11.sp,
                                color = SwosYellow
                            )
                            Text(
                                text = "${userTeam.formation.label} • Star: ${userTeam.roster.firstOrNull()?.name}",
                                fontFamily = MonoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFA3C2AD)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SwosGold)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "OVR ${userTeam.overallRating}",
                            fontFamily = ArcadeFontFamily,
                            fontSize = 9.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bracket Viewer: Quarter Finals
            Text(
                text = "QUARTER FINALS",
                fontFamily = ArcadeFontFamily,
                fontSize = 11.sp,
                color = SwosCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tournament.qfMatches.forEachIndexed { idx, match ->
                    val isUserMatch = idx == 0
                    MatchBracketCard(
                        match = match,
                        isUserMatch = isUserMatch,
                        userTeam = userTeam,
                        onPlayClick = {
                            audio.playWhistle(false)
                            onPlayTournamentMatch(
                                MatchSettings(
                                    homeTeam = match.teamA,
                                    awayTeam = match.teamB,
                                    matchMinutes = 3
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Play Next Round Button
            val userQf = tournament.qfMatches[0]
            Button(
                onClick = {
                    audio.playWhistle(false)
                    onPlayTournamentMatch(
                        MatchSettings(
                            homeTeam = userQf.teamA,
                            awayTeam = userQf.teamB,
                            matchMinutes = 3
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("play_tournament_match_btn"),
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
                    text = "PLAY QUARTER FINAL",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 11.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MatchBracketCard(
    match: TournamentMatch,
    isUserMatch: Boolean,
    userTeam: Team,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUserMatch) Color(0xFF163823) else RetroCardBg
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isUserMatch) 1.5.dp else 1.dp,
            if (isUserMatch) SwosGold else RetroBorder
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Team A
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(match.teamA.jerseyPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = match.teamA.name,
                    fontFamily = ArcadeFontFamily,
                    fontSize = 10.sp,
                    color = if (match.teamA == userTeam) SwosYellow else PitchLineWhite
                )
            }

            // Score or VS
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF07120B))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (match.scoreA != null) "${match.scoreA} - ${match.scoreB}" else "VS",
                    fontFamily = MonoFontFamily,
                    fontSize = 12.sp,
                    color = SwosGold,
                    fontWeight = FontWeight.Bold
                )
            }

            // Team B
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = match.teamB.name,
                    fontFamily = ArcadeFontFamily,
                    fontSize = 10.sp,
                    color = if (match.teamB == userTeam) SwosYellow else PitchLineWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(match.teamB.jerseyPrimary)
                )
            }
        }
    }
}
