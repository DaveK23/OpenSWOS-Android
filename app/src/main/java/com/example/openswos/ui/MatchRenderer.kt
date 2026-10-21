package com.example.openswos.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.openswos.engine.CENTER_CIRCLE_R
import com.example.openswos.engine.CENTER_X
import com.example.openswos.engine.CENTER_Y
import com.example.openswos.engine.GOAL_BOT_Y
import com.example.openswos.engine.GOAL_DEPTH
import com.example.openswos.engine.GOAL_TOP_Y
import com.example.openswos.engine.GOAL_WIDTH
import com.example.openswos.engine.GOAL_X_LEFT
import com.example.openswos.engine.GOAL_X_RIGHT
import com.example.openswos.engine.MatchEngine
import com.example.openswos.engine.PENALTY_AREA_H
import com.example.openswos.engine.PENALTY_AREA_W
import com.example.openswos.engine.PENALTY_BOT_Y
import com.example.openswos.engine.PENALTY_TOP_Y
import com.example.openswos.engine.PENALTY_X_LEFT
import com.example.openswos.engine.PITCH_H
import com.example.openswos.engine.PITCH_MARGIN_X
import com.example.openswos.engine.PITCH_MARGIN_Y
import com.example.openswos.engine.PITCH_W
import com.example.openswos.engine.PlayerEntity
import com.example.openswos.engine.SIX_YARD_H
import com.example.openswos.engine.SIX_YARD_W
import com.example.openswos.engine.SIX_YARD_X_LEFT
import com.example.openswos.model.Team
import com.example.openswos.model.TeamSide
import com.example.ui.theme.SwosYellow
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance DrawScope renderer for the 60FPS retro SWOS pitch, players, ball, and goals.
 */
object MatchRenderer {

    fun drawPitch(drawScope: DrawScope, engine: MatchEngine) {
        val pitchType = engine.settings.pitchType
        val c1 = pitchType.grassLight
        val c2 = pitchType.grassDark

        // 1. Draw alternating grass stripes (Sensible Soccer mowed lawn)
        val stripeHeight = 90f
        var currentY = 0f
        var stripeIdx = 0
        while (currentY < PITCH_H) {
            val color = if (stripeIdx % 2 == 0) c1 else c2
            drawScope.drawRect(
                color = color,
                topLeft = Offset(0f, currentY),
                size = Size(PITCH_W, stripeHeight)
            )
            currentY += stripeHeight
            stripeIdx++
        }

        val lineColor = Color(0xEEFFFFFF)
        val lineStroke = Stroke(width = 3.5f)

        // 2. Outer Touchlines & Goal lines
        drawScope.drawRect(
            color = lineColor,
            topLeft = Offset(PITCH_MARGIN_X, PITCH_MARGIN_Y),
            size = Size(PITCH_W - 2 * PITCH_MARGIN_X, PITCH_H - 2 * PITCH_MARGIN_Y),
            style = lineStroke
        )

        // 3. Halfway line & Center Circle & Center Spot
        drawScope.drawLine(
            color = lineColor,
            start = Offset(PITCH_MARGIN_X, CENTER_Y),
            end = Offset(PITCH_W - PITCH_MARGIN_X, CENTER_Y),
            strokeWidth = 3.5f
        )
        drawScope.drawCircle(
            color = lineColor,
            radius = CENTER_CIRCLE_R,
            center = Offset(CENTER_X, CENTER_Y),
            style = lineStroke
        )
        drawScope.drawCircle(
            color = lineColor,
            radius = 4f,
            center = Offset(CENTER_X, CENTER_Y)
        )

        // 4. Penalty Areas (Top & Bottom)
        // Top 18-yard
        drawScope.drawRect(
            color = lineColor,
            topLeft = Offset(PENALTY_X_LEFT, PITCH_MARGIN_Y),
            size = Size(PENALTY_AREA_W, PENALTY_AREA_H),
            style = lineStroke
        )
        // Top 6-yard
        drawScope.drawRect(
            color = lineColor,
            topLeft = Offset(SIX_YARD_X_LEFT, PITCH_MARGIN_Y),
            size = Size(SIX_YARD_W, SIX_YARD_H),
            style = lineStroke
        )
        // Top Penalty spot
        drawScope.drawCircle(
            color = lineColor,
            radius = 3.5f,
            center = Offset(CENTER_X, PITCH_MARGIN_Y + 125f)
        )

        // Bottom 18-yard
        drawScope.drawRect(
            color = lineColor,
            topLeft = Offset(PENALTY_X_LEFT, PENALTY_BOT_Y),
            size = Size(PENALTY_AREA_W, PENALTY_AREA_H),
            style = lineStroke
        )
        // Bottom 6-yard
        drawScope.drawRect(
            color = lineColor,
            topLeft = Offset(SIX_YARD_X_LEFT, PITCH_H - PITCH_MARGIN_Y - SIX_YARD_H),
            size = Size(SIX_YARD_W, SIX_YARD_H),
            style = lineStroke
        )
        // Bottom Penalty spot
        drawScope.drawCircle(
            color = lineColor,
            radius = 3.5f,
            center = Offset(CENTER_X, PITCH_H - PITCH_MARGIN_Y - 125f)
        )

        // 5. Corner Arcs
        val cornerR = 20f
        drawScope.drawArc(lineColor, 0f, 90f, false, Offset(PITCH_MARGIN_X - cornerR, PITCH_MARGIN_Y - cornerR), Size(cornerR * 2, cornerR * 2), style = lineStroke)
        drawScope.drawArc(lineColor, 90f, 90f, false, Offset(PITCH_W - PITCH_MARGIN_X - cornerR, PITCH_MARGIN_Y - cornerR), Size(cornerR * 2, cornerR * 2), style = lineStroke)
        drawScope.drawArc(lineColor, 270f, 90f, false, Offset(PITCH_MARGIN_X - cornerR, PITCH_H - PITCH_MARGIN_Y - cornerR), Size(cornerR * 2, cornerR * 2), style = lineStroke)
        drawScope.drawArc(lineColor, 180f, 90f, false, Offset(PITCH_W - PITCH_MARGIN_X - cornerR, PITCH_H - PITCH_MARGIN_Y - cornerR), Size(cornerR * 2, cornerR * 2), style = lineStroke)

        // 6. Goals & Net Mesh
        drawGoals(drawScope, engine)
    }

    private fun drawGoals(drawScope: DrawScope, engine: MatchEngine) {
        val netColor = Color(0x66FFFFFF)
        val postColor = Color(0xFFFFFFFF)

        // Top Goal Net
        val topWobble = sin(engine.topNetWobble * 15f) * engine.topNetWobble * 6f
        drawScope.drawRect(
            color = Color(0x33000000),
            topLeft = Offset(GOAL_X_LEFT, GOAL_TOP_Y - GOAL_DEPTH),
            size = Size(GOAL_WIDTH, GOAL_DEPTH)
        )
        // Net horizontal cords
        for (i in 1..4) {
            val ny = GOAL_TOP_Y - (GOAL_DEPTH * i / 5f) + topWobble
            drawScope.drawLine(netColor, Offset(GOAL_X_LEFT, ny), Offset(GOAL_X_RIGHT, ny), strokeWidth = 1.2f)
        }
        // Net vertical cords
        for (i in 1..10) {
            val nx = GOAL_X_LEFT + (GOAL_WIDTH * i / 11f)
            drawScope.drawLine(netColor, Offset(nx, GOAL_TOP_Y), Offset(nx, GOAL_TOP_Y - GOAL_DEPTH + topWobble), strokeWidth = 1.2f)
        }
        // Posts and Crossbar
        drawScope.drawLine(postColor, Offset(GOAL_X_LEFT, GOAL_TOP_Y), Offset(GOAL_X_RIGHT, GOAL_TOP_Y), strokeWidth = 4f)
        drawScope.drawCircle(postColor, radius = 3.5f, center = Offset(GOAL_X_LEFT, GOAL_TOP_Y))
        drawScope.drawCircle(postColor, radius = 3.5f, center = Offset(GOAL_X_RIGHT, GOAL_TOP_Y))

        // Bottom Goal Net
        val botWobble = sin(engine.botNetWobble * 15f) * engine.botNetWobble * 6f
        drawScope.drawRect(
            color = Color(0x33000000),
            topLeft = Offset(GOAL_X_LEFT, GOAL_BOT_Y),
            size = Size(GOAL_WIDTH, GOAL_DEPTH)
        )
        for (i in 1..4) {
            val ny = GOAL_BOT_Y + (GOAL_DEPTH * i / 5f) + botWobble
            drawScope.drawLine(netColor, Offset(GOAL_X_LEFT, ny), Offset(GOAL_X_RIGHT, ny), strokeWidth = 1.2f)
        }
        for (i in 1..10) {
            val nx = GOAL_X_LEFT + (GOAL_WIDTH * i / 11f)
            drawScope.drawLine(netColor, Offset(nx, GOAL_BOT_Y), Offset(nx, GOAL_BOT_Y + GOAL_DEPTH + botWobble), strokeWidth = 1.2f)
        }
        drawScope.drawLine(postColor, Offset(GOAL_X_LEFT, GOAL_BOT_Y), Offset(GOAL_X_RIGHT, GOAL_BOT_Y), strokeWidth = 4f)
        drawScope.drawCircle(postColor, radius = 3.5f, center = Offset(GOAL_X_LEFT, GOAL_BOT_Y))
        drawScope.drawCircle(postColor, radius = 3.5f, center = Offset(GOAL_X_RIGHT, GOAL_BOT_Y))
    }

    fun drawPlayers(drawScope: DrawScope, engine: MatchEngine) {
        val allPlayers = (engine.homePlayers + engine.awayPlayers).sortedBy { it.y }
        for (entity in allPlayers) {
            drawPlayer(drawScope, entity, entity == engine.controlledPlayer, engine.settings.homeTeam, engine.settings.awayTeam)
        }
    }

    private fun drawPlayer(
        drawScope: DrawScope,
        entity: PlayerEntity,
        isControlled: Boolean,
        homeTeam: Team,
        awayTeam: Team
    ) {
        val team = if (entity.side == TeamSide.HOME) homeTeam else awayTeam
        val px = entity.x
        val py = entity.y

        // 1. Drop shadow on grass
        drawScope.drawOval(
            color = Color(0x40000000),
            topLeft = Offset(px - 10f, py - 4f),
            size = Size(20f, 9f)
        )

        if (entity.isSliding) {
            // Sliding tackle pose: stretched body
            val dirX = entity.slideDirX
            val dirY = entity.slideDirY
            val tailX = px - dirX * 16f
            val tailY = py - dirY * 16f

            // Legs/Shorts
            drawScope.drawLine(team.shortsColor, Offset(tailX, tailY), Offset(px, py), strokeWidth = 7f)
            // Jersey
            drawScope.drawLine(team.jerseyPrimary, Offset(px - dirX * 6f, py - dirY * 6f), Offset(px + dirX * 8f, py + dirY * 8f), strokeWidth = 8f)
            // Head
            drawScope.drawCircle(entity.player.skinTone, radius = 4f, center = Offset(px + dirX * 12f, py + dirY * 12f))
            return
        }

        if (entity.isDiving && entity.isGoalkeeper) {
            // Goalkeeper diving pose
            drawScope.drawOval(team.jerseyPrimary, topLeft = Offset(px - 14f, py - 6f), size = Size(28f, 12f))
            drawScope.drawCircle(entity.player.skinTone, radius = 4.5f, center = Offset(px + 14f, py))
            drawScope.drawCircle(Color(0xFFFFF176), radius = 3f, center = Offset(px - 14f, py)) // Keeper gloves
            return
        }

        // Standard Top-Down / Isometric SWOS Player
        val dirAngle = entity.facingAngle
        val legCycleOffset = sin(entity.animFrame * 3.14f) * 4f

        // Animated boots/legs
        val legLeftX = px - 4f + legCycleOffset
        val legRightX = px + 4f - legCycleOffset
        drawScope.drawCircle(team.socksColor, radius = 2.5f, center = Offset(legLeftX, py + 4f))
        drawScope.drawCircle(team.socksColor, radius = 2.5f, center = Offset(legRightX, py + 4f))

        // Shorts
        drawScope.drawOval(
            color = team.shortsColor,
            topLeft = Offset(px - 6f, py - 1f),
            size = Size(12f, 7f)
        )

        // Jersey Torso (Team kit primary + optional stripe)
        val jerseyColor = if (entity.isGoalkeeper) Color(0xFF00E676) else team.jerseyPrimary
        drawScope.drawOval(
            color = jerseyColor,
            topLeft = Offset(px - 7f, py - 7f),
            size = Size(14f, 10f)
        )
        if (team.jerseyStripe != null && !entity.isGoalkeeper) {
            drawScope.drawLine(
                color = team.jerseyStripe,
                start = Offset(px, py - 7f),
                end = Offset(px, py + 2f),
                strokeWidth = 2f
            )
        }

        // Arms
        val armLeftX = px - 7f + cos(dirAngle + 1.2f) * 4f
        val armLeftY = py - 3f + sin(dirAngle + 1.2f) * 4f
        val armRightX = px + 7f + cos(dirAngle - 1.2f) * 4f
        val armRightY = py - 3f + sin(dirAngle - 1.2f) * 4f
        drawScope.drawCircle(entity.player.skinTone, radius = 2f, center = Offset(armLeftX, armLeftY))
        drawScope.drawCircle(entity.player.skinTone, radius = 2f, center = Offset(armRightX, armRightY))

        // Head (Skin tone + hair)
        val headY = py - 8f
        drawScope.drawCircle(entity.player.hairColor, radius = 4.8f, center = Offset(px, headY - 1f))
        drawScope.drawCircle(entity.player.skinTone, radius = 4.2f, center = Offset(px, headY))

        // Controlled Player Yellow Indicator (Iconic SWOS triangle)
        if (isControlled) {
            val triPath = Path().apply {
                val triY = py - 20f
                moveTo(px, triY + 5f)
                lineTo(px - 5f, triY)
                lineTo(px + 5f, triY)
                close()
            }
            drawScope.drawPath(triPath, color = SwosYellow)
            drawScope.drawPath(triPath, color = Color.Black, style = Stroke(width = 1f))
        }
    }

    fun drawBall(drawScope: DrawScope, engine: MatchEngine) {
        val ball = engine.ball
        val bx = ball.x
        val by = ball.y
        val bz = ball.z

        // 1. Ball ground shadow (scales with z height)
        val shadowScale = (1f - (bz / 80f)).coerceIn(0.4f, 1f)
        drawScope.drawOval(
            color = Color(0x55000000),
            topLeft = Offset(bx - 5f * shadowScale, by - 2.5f * shadowScale),
            size = Size(10f * shadowScale, 5f * shadowScale)
        )

        // 2. Ball body (rises with z elevation)
        val ballElevY = by - bz * 0.9f
        val ballRadius = 5.2f + (bz * 0.05f).coerceAtMost(2.5f)

        // White ball circle
        drawScope.drawCircle(
            color = Color(0xFFFBFBFB),
            radius = ballRadius,
            center = Offset(bx, ballElevY)
        )
        // Ball outline
        drawScope.drawCircle(
            color = Color(0xFF1B262C),
            radius = ballRadius,
            center = Offset(bx, ballElevY),
            style = Stroke(width = 1f)
        )
        // Pentagons / pattern
        drawScope.drawCircle(
            color = Color(0xFF1B262C),
            radius = ballRadius * 0.35f,
            center = Offset(bx - 0.5f, ballElevY - 0.5f)
        )
    }

    fun drawRadar(drawScope: DrawScope, engine: MatchEngine, radarW: Float, radarH: Float, offset: Offset) {
        // Radar pitch background
        drawScope.drawRect(
            color = Color(0xCC0E3B1B),
            topLeft = offset,
            size = Size(radarW, radarH)
        )
        drawScope.drawRect(
            color = Color(0x88FFFFFF),
            topLeft = offset,
            size = Size(radarW, radarH),
            style = Stroke(width = 1.5f)
        )
        // Radar Halfway Line
        drawScope.drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(offset.x, offset.y + radarH / 2f),
            end = Offset(offset.x + radarW, offset.y + radarH / 2f),
            strokeWidth = 1f
        )

        val scaleX = radarW / PITCH_W
        val scaleY = radarH / PITCH_H

        // Draw Home Players dots
        for (p in engine.homePlayers) {
            val rx = offset.x + p.x * scaleX
            val ry = offset.y + p.y * scaleY
            val color = if (p == engine.controlledPlayer) SwosYellow else engine.settings.homeTeam.jerseyPrimary
            drawScope.drawCircle(color, radius = 2.4f, center = Offset(rx, ry))
        }

        // Draw Away Players dots
        for (p in engine.awayPlayers) {
            val rx = offset.x + p.x * scaleX
            val ry = offset.y + p.y * scaleY
            drawScope.drawCircle(engine.settings.awayTeam.jerseyPrimary, radius = 2.4f, center = Offset(rx, ry))
        }

        // Draw Ball dot (flashing white)
        val ballRx = offset.x + engine.ball.x * scaleX
        val ballRy = offset.y + engine.ball.y * scaleY
        drawScope.drawCircle(Color.White, radius = 2.8f, center = Offset(ballRx, ballRy))
    }
}
