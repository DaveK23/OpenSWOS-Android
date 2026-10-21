package com.example.openswos.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openswos.engine.MatchEngine
import com.example.ui.theme.ArcadeFontFamily
import com.example.ui.theme.SwosGold
import com.example.ui.theme.SwosRed
import com.example.ui.theme.SwosYellow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualControlsOverlay(
    engine: MatchEngine,
    modifier: Modifier = Modifier,
    onTackleClick: () -> Unit = {}
) {
    val view = LocalView.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left: 8-Way Virtual Joystick
        VirtualJoystick(
            modifier = Modifier.testTag("virtual_joystick"),
            onMove = { x, y ->
                engine.joystickX = x
                engine.joystickY = y
            }
        )

        // Right: Arcade Buttons
        if (engine.settings.isClassicControls) {
            // Classic SWOS 1-Button
            ClassicSwosButton(
                modifier = Modifier.testTag("action_button_classic"),
                engine = engine,
                onHaptic = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            )
        } else {
            // Modern Dual-Button (PASS + SHOOT/TACKLE)
            DualActionButtons(
                modifier = Modifier.testTag("dual_action_buttons"),
                engine = engine,
                onTackle = onTackleClick,
                onHaptic = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            )
        }
    }
}

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    radiusDp: Float = 70f,
    onMove: (Float, Float) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size((radiusDp * 2).dp)
            .pointerInput(Unit) {
                val maxRadius = size.width / 2f
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        val dist = delta.getDistance()
                        val clampedDist = dist.coerceAtMost(maxRadius)
                        val angle = kotlin.math.atan2(delta.y, delta.x)
                        val normX = cos(angle) * (clampedDist / maxRadius)
                        val normY = sin(angle) * (clampedDist / maxRadius)
                        knobOffset = Offset(normX * maxRadius, normY * maxRadius)
                        onMove(normX, normY)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = change.position - center
                        val dist = delta.getDistance()
                        val clampedDist = dist.coerceAtMost(maxRadius)
                        val angle = kotlin.math.atan2(delta.y, delta.x)
                        val normX = cos(angle) * (clampedDist / maxRadius)
                        val normY = sin(angle) * (clampedDist / maxRadius)
                        knobOffset = Offset(normX * maxRadius, normY * maxRadius)
                        onMove(normX, normY)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((radiusDp * 2).dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width / 2f - 4f

            // Outer Base Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x7716251C), Color(0xAA0C140F)),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )
            drawCircle(
                color = Color(0x66FFD700),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Directional Tick Crosses
            val tickLen = 14f
            drawLine(Color(0x44FFFFFF), Offset(center.x - baseRadius + 4f, center.y), Offset(center.x - baseRadius + 4f + tickLen, center.y), strokeWidth = 2f)
            drawLine(Color(0x44FFFFFF), Offset(center.x + baseRadius - 4f - tickLen, center.y), Offset(center.x + baseRadius - 4f, center.y), strokeWidth = 2f)
            drawLine(Color(0x44FFFFFF), Offset(center.x, center.y - baseRadius + 4f), Offset(center.x, center.y - baseRadius + 4f + tickLen), strokeWidth = 2f)
            drawLine(Color(0x44FFFFFF), Offset(center.x, center.y + baseRadius - 4f - tickLen), Offset(center.x, center.y + baseRadius - 4f), strokeWidth = 2f)

            // Inner Thumb Knob
            val knobCenter = center + knobOffset
            val knobRadius = baseRadius * 0.44f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFC79100)),
                    center = knobCenter,
                    radius = knobRadius
                ),
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = Color(0xFF3E2723),
                radius = knobRadius,
                center = knobCenter,
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
fun DualActionButtons(
    engine: MatchEngine,
    modifier: Modifier = Modifier,
    onTackle: () -> Unit,
    onHaptic: () -> Unit
) {
    val hasBall = engine.ball.owner == engine.controlledPlayer

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // PASS / TACKLE Button
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (hasBall) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            if (hasBall) Color(0xFF1B5E20) else Color(0xFFE65100)
                        )
                    )
                )
                .pointerInput(hasBall) {
                    detectTapGestures(
                        onPress = {
                            onHaptic()
                            if (hasBall) {
                                engine.actionCharge = 0.1f
                                engine.onActionRelease()
                            } else {
                                engine.isTacklePressed = true
                                onTackle()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (hasBall) "PASS" else "SLIDE",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (hasBall) "TAP" else "TACKLE",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 7.sp,
                    color = SwosYellow
                )
            }
        }

        // SHOOT / CLEAR Button
        var isHeld by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (isHeld) SwosYellow else SwosRed,
                            Color(0xFFB71C1C)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onHaptic()
                            isHeld = true
                            engine.isActionHeld = true
                            tryAwaitRelease()
                            isHeld = false
                            engine.onActionRelease()
                            onHaptic()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SHOOT",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AFTERTOUCH",
                    fontFamily = ArcadeFontFamily,
                    fontSize = 6.sp,
                    color = SwosYellow
                )
            }
        }
    }
}

@Composable
fun ClassicSwosButton(
    engine: MatchEngine,
    modifier: Modifier = Modifier,
    onHaptic: () -> Unit
) {
    var isHeld by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        if (isHeld) SwosYellow else SwosGold,
                        Color(0xFF8C6600)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHaptic()
                        isHeld = true
                        engine.isActionHeld = true
                        tryAwaitRelease()
                        isHeld = false
                        engine.onActionRelease()
                        onHaptic()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ACTION",
                fontFamily = ArcadeFontFamily,
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "PASS / SHOOT",
                fontFamily = ArcadeFontFamily,
                fontSize = 7.sp,
                color = Color(0xFF2A1C0C)
            )
        }
    }
}
