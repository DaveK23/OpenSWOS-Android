package com.example.openswos.ui

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openswos.audio.RetroAudio
import com.example.openswos.data.TeamDatabase
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
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PenaltyShootoutScreen(
    audio: RetroAudio,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val view = LocalView.current

    val homeTeam = remember { TeamDatabase.brazil }
    val awayTeam = remember { TeamDatabase.italy }

    // Penalty score tracker (true = scored, false = missed)
    val homeKicks = remember { mutableStateListOf<Boolean>() }
    val awayKicks = remember { mutableStateListOf<Boolean>() }

    var isPlayerShooting by remember { mutableStateOf(true) }
    var currentRound by remember { mutableIntStateOf(1) }
    var isShotInProgress by remember { mutableStateOf(false) }

    // Aim position in normalized goal space (-1..1 horizontally, 0..1 vertically)
    var aimNormX by remember { mutableFloatStateOf(0f) }
    var aimNormY by remember { mutableFloatStateOf(0.4f) }
    var curveSlider by remember { mutableFloatStateOf(0f) } // -1 = curve left, 1 = curve right
    var shotPower by remember { mutableFloatStateOf(0.75f) }

    // Animation physics states
    var ballX by remember { mutableFloatStateOf(0f) }
    var ballY by remember { mutableFloatStateOf(0f) }
    var ballZ by remember { mutableFloatStateOf(0f) }
    var keeperX by remember { mutableFloatStateOf(0f) }
    var keeperDiveY by remember { mutableFloatStateOf(0f) }
    var netWobble by remember { mutableFloatStateOf(0f) }
    var resultBanner by remember { mutableStateOf<String?>(null) }
    var isShootoutOver by remember { mutableStateOf(false) }

    // Frame animation loop
    var animProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isShotInProgress) {
        if (isShotInProgress) {
            animProgress = 0f
            val targetX = aimNormX * 120f + curveSlider * 35f
            val targetY = aimNormY * 60f
            val keeperTargetX = (Random.nextFloat() * 2f - 1f) * 110f

            while (animProgress < 1.0f) {
                withFrameNanos {
                    animProgress = (animProgress + 0.035f).coerceAtMost(1.0f)
                    // Ball parabolic flight
                    ballX = targetX * animProgress
                    ballY = targetY * animProgress
                    ballZ = sin(animProgress * 3.14f) * 35f * shotPower

                    // Goalkeeper dive towards corner
                    keeperX = keeperTargetX * animProgress
                    keeperDiveY = if (abs(keeperTargetX) > 40f) 15f * animProgress else 0f
                }
            }

            // Check goal vs save
            val distToKeeper = abs(ballX - keeperX)
            val isSaved = distToKeeper < 32f && abs(ballY - keeperDiveY) < 30f
            val isMissed = abs(ballX) > 130f || ballY > 70f

            if (isSaved) {
                audio.playKick(0.7f)
                resultBanner = "SAVED BY KEEPER!"
                if (isPlayerShooting) homeKicks.add(false) else awayKicks.add(false)
            } else if (isMissed) {
                audio.playPostHit()
                resultBanner = "OFF TARGET!"
                if (isPlayerShooting) homeKicks.add(false) else awayKicks.add(false)
            } else {
                audio.playGoalCheer()
                netWobble = 1.0f
                resultBanner = "GOAL!"
                if (isPlayerShooting) homeKicks.add(true) else awayKicks.add(true)
            }

            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            // Check shootout over
            if (homeKicks.size >= 5 && awayKicks.size >= 5 && homeKicks.size == awayKicks.size) {
                val homeScore = homeKicks.count { it }
                val awayScore = awayKicks.count { it }
                if (homeScore != awayScore) {
                    isShootoutOver = true
                }
            }

            // Switch turn after delay
            kotlinx.coroutines.delay(1800)
            isShotInProgress = false
            resultBanner = null
            ballX = 0f
            ballY = 0f
            ballZ = 0f
            keeperX = 0f
            keeperDiveY = 0f
            netWobble = 0f
            isPlayerShooting = !isPlayerShooting
            if (isPlayerShooting) currentRound++
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF091F11), Color(0xFF144D26), Color(0xFF0A1F13))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
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
                    modifier = Modifier.testTag("penalties_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SwosGold
                    )
                }

                Text(
                    text = "PENALTY SHOOTOUT",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 13.sp,
                    color = SwosGold
                )

                Box(modifier = Modifier.size(40.dp))
            }

            // Scoreboard Dots Display
            ShootoutScoreboard(
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                homeKicks = homeKicks,
                awayKicks = awayKicks
            )

            // Penalty Goal Viewport Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E16)),
                border = androidx.compose.foundation.BorderStroke(2.dp, RetroBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(isShotInProgress) {
                                if (!isShotInProgress && isPlayerShooting) {
                                    detectDragGestures { change, _ ->
                                        change.consume()
                                        val cx = size.width / 2f
                                        val cy = size.height * 0.45f
                                        aimNormX = ((change.position.x - cx) / 120f).coerceIn(-1f, 1f)
                                        aimNormY = ((cy - change.position.y) / 60f).coerceIn(0f, 1f)
                                    }
                                }
                            }
                    ) {
                        val cw = size.width
                        val ch = size.height
                        val goalW = 260f
                        val goalH = 110f
                        val goalLeft = (cw - goalW) / 2f
                        val goalTop = ch * 0.22f

                        // Grass pitch base
                        drawRect(Color(0xFF1B5E20), Offset(0f, goalTop + goalH), Size(cw, ch - (goalTop + goalH)))
                        // Penalty spot
                        drawCircle(Color.White, radius = 4f, center = Offset(cw / 2f, ch * 0.85f))

                        // Goal frame & Net
                        drawRect(Color(0x33000000), Offset(goalLeft, goalTop), Size(goalW, goalH))
                        // Net mesh lines
                        for (i in 1..8) {
                            val ny = goalTop + (goalH * i / 9f)
                            drawLine(Color(0x55FFFFFF), Offset(goalLeft, ny), Offset(goalLeft + goalW, ny), strokeWidth = 1f)
                        }
                        for (i in 1..14) {
                            val nx = goalLeft + (goalW * i / 15f)
                            drawLine(Color(0x55FFFFFF), Offset(nx, goalTop), Offset(nx, goalTop + goalH), strokeWidth = 1f)
                        }
                        // Goal posts & Crossbar
                        drawLine(Color.White, Offset(goalLeft, goalTop), Offset(goalLeft + goalW, goalTop), strokeWidth = 5f)
                        drawLine(Color.White, Offset(goalLeft, goalTop), Offset(goalLeft, goalTop + goalH), strokeWidth = 5f)
                        drawLine(Color.White, Offset(goalLeft + goalW, goalTop), Offset(goalLeft + goalW, goalTop + goalH), strokeWidth = 5f)

                        // Goalkeeper
                        val gkCenterX = cw / 2f + keeperX
                        val gkCenterY = goalTop + goalH - 18f - keeperDiveY
                        // Goalkeeper body
                        drawOval(Color(0xFF00E676), Offset(gkCenterX - 10f, gkCenterY - 14f), Size(20f, 24f))
                        drawCircle(Color(0xFFF7D2B7), radius = 6f, center = Offset(gkCenterX, gkCenterY - 20f))

                        // Reticle Aim marker (when player shooting)
                        if (isPlayerShooting && !isShotInProgress) {
                            val rx = cw / 2f + aimNormX * 120f
                            val ry = goalTop + goalH - aimNormY * 60f
                            drawCircle(SwosYellow, radius = 9f, center = Offset(rx, ry), style = Stroke(width = 2f))
                            drawLine(SwosYellow, Offset(rx - 12f, ry), Offset(rx + 12f, ry), strokeWidth = 1.5f)
                            drawLine(SwosYellow, Offset(rx, ry - 12f), Offset(rx, ry + 12f), strokeWidth = 1.5f)
                        }

                        // Penalty Ball
                        val ballDrawX = if (isShotInProgress) cw / 2f + ballX else cw / 2f
                        val ballDrawY = if (isShotInProgress) (ch * 0.85f) - (ch * 0.55f * animProgress) - ballZ else ch * 0.85f
                        val ballR = if (isShotInProgress) 7f - animProgress * 2.5f else 7f

                        // Ball Shadow
                        drawOval(Color(0x55000000), Offset(ballDrawX - 6f, ch * 0.85f - (ch * 0.55f * animProgress)), Size(12f, 6f))
                        // Ball Body
                        drawCircle(Color.White, radius = ballR, center = Offset(ballDrawX, ballDrawY))
                        drawCircle(Color.Black, radius = ballR, center = Offset(ballDrawX, ballDrawY), style = Stroke(1f))
                    }

                    // Result Banner
                    if (resultBanner != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xDD000000))
                                .border(2.dp, SwosGold, RoundedCornerShape(8.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = resultBanner ?: "",
                                fontFamily = ArcadeFontFamily,
                                fontSize = 16.sp,
                                color = SwosYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Controls & Shoot Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RetroCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, RetroBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isPlayerShooting) "YOUR TURN TO SHOOT" else "OPPONENT SHOOTING",
                        fontFamily = ArcadeFontFamily,
                        fontSize = 11.sp,
                        color = if (isPlayerShooting) SwosYellow else SwosCyan
                    )

                    // Aftertouch Curve Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AFTERTOUCH CURVE", fontFamily = MonoFontFamily, fontSize = 11.sp, color = Color(0xFFA3C2AD))
                        Text(
                            text = when {
                                curveSlider < -0.2f -> "CURVE LEFT"
                                curveSlider > 0.2f -> "CURVE RIGHT"
                                else -> "STRAIGHT"
                            },
                            fontFamily = ArcadeFontFamily,
                            fontSize = 8.sp,
                            color = SwosCyan
                        )
                    }
                    Slider(
                        value = curveSlider,
                        onValueChange = { curveSlider = it },
                        valueRange = -1f..1f,
                        enabled = !isShotInProgress && isPlayerShooting,
                        colors = SliderDefaults.colors(thumbColor = SwosGold, activeTrackColor = SwosYellow)
                    )

                    // Shoot Button
                    Button(
                        onClick = {
                            if (!isShotInProgress) {
                                audio.playKick(shotPower)
                                isShotInProgress = true
                            }
                        },
                        enabled = !isShotInProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("penalty_kick_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SwosGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlayerShooting) "TAKE PENALTY" else "FACING SHOT",
                            fontFamily = ArcadeFontFamily,
                            fontSize = 11.sp,
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
fun ShootoutScoreboard(
    homeTeam: com.example.openswos.model.Team,
    awayTeam: com.example.openswos.model.Team,
    homeKicks: List<Boolean>,
    awayKicks: List<Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD0C1610)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, RetroBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Home team row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(homeTeam.name, fontFamily = ArcadeFontFamily, fontSize = 10.sp, color = SwosYellow)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0 until 5) {
                        val outcome = homeKicks.getOrNull(i)
                        KickResultDot(outcome)
                    }
                }
                Text("${homeKicks.count { it }}", fontFamily = ArcadeFontFamily, fontSize = 12.sp, color = Color.White)
            }

            // Away team row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(awayTeam.name, fontFamily = ArcadeFontFamily, fontSize = 10.sp, color = PitchLineWhite)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0 until 5) {
                        val outcome = awayKicks.getOrNull(i)
                        KickResultDot(outcome)
                    }
                }
                Text("${awayKicks.count { it }}", fontFamily = ArcadeFontFamily, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun KickResultDot(outcome: Boolean?) {
    val color = when (outcome) {
        true -> Color(0xFF00E676)  // Green goal
        false -> SwosRed           // Red miss
        null -> Color(0xFF263238)  // Pending gray
    }
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White, CircleShape)
    )
}
