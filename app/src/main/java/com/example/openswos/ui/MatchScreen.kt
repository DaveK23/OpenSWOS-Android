package com.example.openswos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openswos.engine.MatchEngine
import com.example.openswos.engine.PITCH_H
import com.example.openswos.engine.PITCH_W
import com.example.openswos.model.MatchPeriod
import com.example.openswos.model.MatchSettings
import com.example.ui.theme.ArcadeFontFamily
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchLineWhite
import com.example.ui.theme.RetroBorder
import com.example.ui.theme.RetroCardBg
import com.example.ui.theme.RetroDarkBg
import com.example.ui.theme.SwosCyan
import com.example.ui.theme.SwosGold
import com.example.ui.theme.SwosRed
import com.example.ui.theme.SwosYellow
import kotlin.math.abs
import kotlin.math.min

@Composable
fun MatchScreen(
    engine: MatchEngine,
    onMatchEnd: () -> Unit,
    onOpenTactics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShowRadar by remember { mutableStateOf(true) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showEndStatsDialog by remember { mutableStateOf(false) }

    // Intercept back button to pause game instead of exiting immediately
    BackHandler {
        if (!showPauseDialog) {
            engine.isPaused = true
            showPauseDialog = true
        } else {
            showPauseDialog = false
            engine.isPaused = false
        }
    }

    // 60 FPS Game Loop using withFrameNanos
    var renderTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(engine) {
        var lastFrameTimeNanos = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    engine.tick(dt)
                    renderTick = frameTimeNanos
                    if (engine.period == MatchPeriod.FULL_TIME && !showEndStatsDialog) {
                        showEndStatsDialog = true
                    }
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A130C))
    ) {
        val screenW = maxWidth.value
        val screenH = maxHeight.value

        // Pitch scale: show ~450 units width on standard phone
        val zoom = (screenW / 440f).coerceIn(0.7f, 1.6f)

        // Game Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("match_canvas")
        ) {
            val _tick = renderTick // Reading renderTick triggers DrawScope redraw at 60 FPS
            val canvasW = size.width
            val canvasH = size.height

            // Camera transformation: center on ball/camera position
            val transX = canvasW / 2f - engine.cameraX * zoom
            val transY = canvasH / 2f - engine.cameraY * zoom

            translate(transX, transY) {
                scale(zoom, zoom, Offset.Zero) {
                    MatchRenderer.drawPitch(this, engine)
                    MatchRenderer.drawPlayers(this, engine)
                    MatchRenderer.drawBall(this, engine)
                    MatchRenderer.drawWeather(this, engine)
                }
            }

            // Radar Minimap (Top Right)
            if (isShowRadar) {
                val radarW = 75f * zoom
                val radarH = 135f * zoom
                val radarX = canvasW - radarW - 20f
                val radarY = 110f
                MatchRenderer.drawRadar(this, engine, radarW, radarH, Offset(radarX, radarY))
            }
        }

        // Top HUD Scoreboard & Controls
        MatchTopHud(
            engine = engine,
            isShowRadar = isShowRadar,
            onToggleRadar = { isShowRadar = !isShowRadar },
            onPauseClick = {
                engine.isPaused = true
                showPauseDialog = true
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )

        // Shot Power Charge Meter
        if (engine.actionCharge > 0.05f) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "POWER",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 10.sp,
                    color = SwosYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { engine.actionCharge },
                    modifier = Modifier
                        .width(160.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(4.dp)),
                    color = if (engine.actionCharge > 0.8f) SwosRed else SwosGold,
                    trackColor = Color(0x77000000)
                )
            }
        }

        // Active Aftertouch Indicator
        if (engine.isAftertouchActive) {
            Text(
                text = "SWOS AFTERTOUCH >>",
                fontFamily = ArcadeFontFamily,
                fontSize = 11.sp,
                color = SwosCyan,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 80.dp)
            )
        }

        // Celebratory Event Banner (GOAL!, OFFSIDE, PENALTY, etc.)
        AnimatedVisibility(
            visible = engine.currentEventBanner != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xE6E53935), Color(0xE6FFB300), Color(0xE6E53935))
                        )
                    )
                    .border(3.dp, Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 32.dp, vertical = 18.dp)
            ) {
                Text(
                    text = engine.currentEventBanner ?: "",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Virtual Controls (Bottom)
        VirtualControlsOverlay(
            engine = engine,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )

        // Pause Menu Dialog
        if (showPauseDialog) {
            PauseMenuDialog(
                engine = engine,
                onResume = {
                    showPauseDialog = false
                    engine.isPaused = false
                },
                onRestart = {
                    showPauseDialog = false
                    engine.isPaused = false
                    engine.setupKickoff(com.example.openswos.model.TeamSide.HOME)
                },
                onTactics = {
                    showPauseDialog = false
                    onOpenTactics()
                },
                onQuit = {
                    showPauseDialog = false
                    engine.isPaused = false
                    onMatchEnd()
                }
            )
        }

        // End of Match Stats Dialog
        if (showEndStatsDialog) {
            EndMatchStatsDialog(
                engine = engine,
                onPlayAgain = {
                    showEndStatsDialog = false
                    engine.period = MatchPeriod.FIRST_HALF
                    engine.matchTimeSeconds = 0f
                    engine.stats.homeGoals = 0
                    engine.stats.awayGoals = 0
                    engine.setupKickoff(com.example.openswos.model.TeamSide.HOME)
                },
                onMainMenu = onMatchEnd
            )
        }
    }
}

@Composable
fun MatchTopHud(
    engine: MatchEngine,
    isShowRadar: Boolean,
    onToggleRadar: () -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeTeam = engine.settings.homeTeam
    val awayTeam = engine.settings.awayTeam

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD0E1812)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, RetroBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Team Name & Kit Color
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(homeTeam.jerseyPrimary)
                        .border(1.dp, Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = homeTeam.shortName,
                    fontFamily = ArcadeFontFamily,
                    fontSize = 12.sp,
                    color = SwosYellow
                )
            }

            // Score Display
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF040A06))
                    .border(1.dp, RetroBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${engine.homeGoals} - ${engine.awayGoals}",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 15.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Away Team Name & Kit Color
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = awayTeam.shortName,
                    fontFamily = ArcadeFontFamily,
                    fontSize = 12.sp,
                    color = PitchLineWhite
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(awayTeam.jerseyPrimary)
                        .border(1.dp, Color.White, CircleShape)
                )
            }

            // Clock & Period
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = engine.matchClockDisplay,
                    fontFamily = MonoFontFamily,
                    fontSize = 14.sp,
                    color = SwosGold,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = when (engine.period) {
                            MatchPeriod.PRE_MATCH -> "READY"
                            MatchPeriod.FIRST_HALF -> "1ST"
                            MatchPeriod.HALF_TIME -> "HT"
                            MatchPeriod.SECOND_HALF -> "2ND"
                            MatchPeriod.FULL_TIME -> "FT"
                            else -> "ET"
                        },
                        fontFamily = ArcadeFontFamily,
                        fontSize = 8.sp,
                        color = Color(0xFFA3C2AD)
                    )
                    Text(
                        text = "•",
                        fontSize = 8.sp,
                        color = Color(0xFFA3C2AD)
                    )
                    Text(
                        text = "${engine.settings.pitchType.badgeEmoji} ${engine.settings.pitchType.displayName.uppercase()}",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 7.sp,
                        color = SwosYellow
                    )
                }
            }

            // Actions: Radar toggle and Pause
            Row {
                IconButton(
                    onClick = onToggleRadar,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("radar_toggle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Toggle Radar",
                        tint = if (isShowRadar) SwosYellow else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("pause_match_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Match",
                        tint = SwosGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PauseMenuDialog(
    engine: MatchEngine,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onTactics: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xBB000000))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = RetroCardBg),
            border = androidx.compose.foundation.BorderStroke(2.dp, SwosGold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "MATCH PAUSED",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 15.sp,
                    color = SwosGold
                )

                Text(
                    text = "${engine.settings.homeTeam.name} vs ${engine.settings.awayTeam.name}",
                    fontFamily = MonoFontFamily,
                    fontSize = 14.sp,
                    color = PitchLineWhite
                )

                Text(
                    text = "${engine.settings.pitchType.badgeEmoji} ${engine.settings.pitchType.displayName} • ${engine.settings.pitchType.weatherName}",
                    fontFamily = MonoFontFamily,
                    fontSize = 12.sp,
                    color = SwosGold
                )

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().testTag("resume_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESUME", fontFamily = ArcadeFontFamily, fontSize = 10.sp)
                }

                Button(
                    onClick = onTactics,
                    modifier = Modifier.fillMaxWidth().testTag("tactics_pause_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("TACTICS & SQUAD", fontFamily = ArcadeFontFamily, fontSize = 10.sp)
                }

                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().testTag("restart_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESTART", fontFamily = ArcadeFontFamily, fontSize = 10.sp)
                }

                Button(
                    onClick = onQuit,
                    modifier = Modifier.fillMaxWidth().testTag("quit_match_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SwosRed)
                ) {
                    Text("QUIT TO MENU", fontFamily = ArcadeFontFamily, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun EndMatchStatsDialog(
    engine: MatchEngine,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val stats = engine.stats
    val home = engine.settings.homeTeam
    val away = engine.settings.awayTeam

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = RetroCardBg),
            border = androidx.compose.foundation.BorderStroke(2.dp, SwosGold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FINAL SCORE",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 16.sp,
                    color = SwosGold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Scoreboard Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(home.shortName, fontFamily = ArcadeFontFamily, fontSize = 13.sp, color = SwosYellow)
                        Text("${stats.homeGoals}", fontFamily = ArcadeFontFamily, fontSize = 24.sp, color = Color.White)
                    }
                    Text("-", fontFamily = ArcadeFontFamily, fontSize = 20.sp, color = Color.Gray)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(away.shortName, fontFamily = ArcadeFontFamily, fontSize = 13.sp, color = PitchLineWhite)
                        Text("${stats.awayGoals}", fontFamily = ArcadeFontFamily, fontSize = 24.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text("MATCH STATISTICS", fontFamily = ArcadeFontFamily, fontSize = 10.sp, color = Color(0xFFA3C2AD))
                Spacer(modifier = Modifier.height(8.dp))

                // Stats rows
                StatRow("Possession", "${stats.homePossessionPercent}%", "${stats.awayPossessionPercent}%")
                StatRow("Total Shots", "${stats.homeShots}", "${stats.awayShots}")
                StatRow("Corners", "${stats.homeCorners}", "${stats.awayCorners}")
                StatRow("Fouls", "${stats.homeFouls}", "${stats.awayFouls}")

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier.weight(1f).testTag("play_again_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("REPLAY", fontFamily = ArcadeFontFamily, fontSize = 9.sp)
                    }
                    Button(
                        onClick = onMainMenu,
                        modifier = Modifier.weight(1f).testTag("main_menu_return_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SwosGold)
                    ) {
                        Text("MENU", fontFamily = ArcadeFontFamily, fontSize = 9.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(title: String, homeVal: String, awayVal: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(homeVal, fontFamily = MonoFontFamily, fontSize = 13.sp, color = SwosYellow, modifier = Modifier.width(50.dp), textAlign = TextAlign.Start)
        Text(title, fontFamily = MonoFontFamily, fontSize = 12.sp, color = Color(0xFFB0BEC5), textAlign = TextAlign.Center)
        Text(awayVal, fontFamily = MonoFontFamily, fontSize = 13.sp, color = PitchLineWhite, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
    }
}
