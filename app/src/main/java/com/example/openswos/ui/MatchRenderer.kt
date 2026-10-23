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

        // 1. Stadium Apron (surrounding runoff area beyond pitch boundaries)
        drawScope.drawRect(
            color = pitchType.apronColor,
            topLeft = Offset(-250f, -250f),
            size = Size(PITCH_W + 500f, PITCH_H + 500f)
        )

        // Perimeter Advertising Boards (Retro 90s Hoardings)
        drawAdvertisingBoards(drawScope, pitchType)

        // 2. Pitch Surface Texturing per PitchType
        when (pitchType) {
            com.example.openswos.model.PitchType.ASTRO -> {
                // Synthetic turf tiles (60x60 checkerboard weave pattern)
                val tileSize = 70f
                var y = 0f
                var row = 0
                while (y < PITCH_H) {
                    var x = 0f
                    var col = 0
                    while (x < PITCH_W) {
                        val tileColor = if ((row + col) % 2 == 0) c1 else c2
                        drawScope.drawRect(tileColor, topLeft = Offset(x, y), size = Size(tileSize, tileSize))
                        x += tileSize
                        col++
                    }
                    y += tileSize
                    row++
                }
                // Synthetic turf vertical weave stitch lines
                var gx = 0f
                while (gx < PITCH_W) {
                    drawScope.drawLine(Color(0x14000000), Offset(gx, 0f), Offset(gx, PITCH_H), strokeWidth = 1f)
                    gx += 35f
                }
                // Rubber infill specks
                val seedSpecks = listOf(
                    Offset(120f, 150f), Offset(340f, 210f), Offset(560f, 180f),
                    Offset(210f, 430f), Offset(450f, 490f), Offset(180f, 750f),
                    Offset(480f, 820f), Offset(270f, 1050f), Offset(510f, 1150f)
                )
                for (s in seedSpecks) {
                    drawScope.drawCircle(Color(0x22000000), radius = 1.8f, center = s)
                }
            }
            com.example.openswos.model.PitchType.WET_MUD -> {
                // Heavy sodden mud bands
                val stripeHeight = 90f
                var currentY = 0f
                var stripeIdx = 0
                while (currentY < PITCH_H) {
                    val color = if (stripeIdx % 2 == 0) c1 else c2
                    drawScope.drawRect(color, topLeft = Offset(0f, currentY), size = Size(PITCH_W, stripeHeight))
                    currentY += stripeHeight
                    stripeIdx++
                }

                // Churned worn mud in goalmouths (heavily kicked-away turf)
                // Top goalmouth worn mud
                drawScope.drawOval(
                    Color(0xFF382717),
                    topLeft = Offset(GOAL_X_LEFT - 35f, PITCH_MARGIN_Y - 10f),
                    size = Size(GOAL_WIDTH + 70f, SIX_YARD_H + 55f)
                )
                drawScope.drawOval(
                    Color(0xFF2A1C0E),
                    topLeft = Offset(GOAL_X_LEFT - 10f, PITCH_MARGIN_Y),
                    size = Size(GOAL_WIDTH + 20f, SIX_YARD_H + 25f)
                )

                // Bottom goalmouth worn mud
                drawScope.drawOval(
                    Color(0xFF382717),
                    topLeft = Offset(GOAL_X_LEFT - 35f, PITCH_H - PITCH_MARGIN_Y - SIX_YARD_H - 45f),
                    size = Size(GOAL_WIDTH + 70f, SIX_YARD_H + 55f)
                )
                drawScope.drawOval(
                    Color(0xFF2A1C0E),
                    topLeft = Offset(GOAL_X_LEFT - 10f, PITCH_H - PITCH_MARGIN_Y - SIX_YARD_H - 25f),
                    size = Size(GOAL_WIDTH + 20f, SIX_YARD_H + 25f)
                )

                // Midfield churn wear
                drawScope.drawOval(
                    Color(0x44382717),
                    topLeft = Offset(CENTER_X - 55f, CENTER_Y - 30f),
                    size = Size(110f, 60f)
                )

                // Reflective Standing Water Puddles
                val puddles = listOf(
                    Offset(170f, 320f) to Size(55f, 26f),
                    Offset(520f, 380f) to Size(48f, 22f),
                    Offset(220f, 620f) to Size(65f, 30f),
                    Offset(470f, 690f) to Size(58f, 25f),
                    Offset(160f, 950f) to Size(52f, 24f),
                    Offset(530f, 980f) to Size(60f, 28f),
                    Offset(340f, 480f) to Size(44f, 20f),
                    Offset(350f, 820f) to Size(46f, 22f),
                    Offset(115f, 650f) to Size(50f, 24f),
                    Offset(585f, 650f) to Size(52f, 25f),
                    Offset(250f, 180f) to Size(38f, 18f),
                    Offset(430f, 1120f) to Size(44f, 20f)
                )
                for ((pos, pSize) in puddles) {
                    drawScope.drawOval(Color(0x77263A4D), topLeft = pos, size = pSize)
                    drawScope.drawOval(Color(0x55152330), topLeft = Offset(pos.x + 4f, pos.y + 3f), size = Size(pSize.width - 8f, pSize.height - 6f))
                    drawScope.drawArc(
                        Color(0x44FFFFFF),
                        200f,
                        130f,
                        false,
                        topLeft = Offset(pos.x + 3f, pos.y + 2f),
                        size = Size(pSize.width - 6f, pSize.height - 4f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
            com.example.openswos.model.PitchType.FROZEN -> {
                // Snow-covered ground with pale icy frost stripes
                val stripeHeight = 90f
                var currentY = 0f
                var stripeIdx = 0
                while (currentY < PITCH_H) {
                    val color = if (stripeIdx % 2 == 0) c1 else c2
                    drawScope.drawRect(color, topLeft = Offset(0f, currentY), size = Size(PITCH_W, stripeHeight))
                    currentY += stripeHeight
                    stripeIdx++
                }

                // Translucent icy frozen patches with white specular glints
                val icePatches = listOf(
                    Offset(150f, 360f) to Size(85f, 42f),
                    Offset(480f, 420f) to Size(75f, 38f),
                    Offset(210f, 720f) to Size(92f, 46f),
                    Offset(490f, 850f) to Size(82f, 40f),
                    Offset(310f, 580f) to Size(86f, 42f),
                    Offset(180f, 980f) to Size(72f, 36f)
                )
                for ((pos, iSize) in icePatches) {
                    drawScope.drawOval(Color(0x66A8D5EA), topLeft = pos, size = iSize)
                    drawScope.drawOval(Color(0x44FFFFFF), topLeft = Offset(pos.x + 5f, pos.y + 4f), size = Size(iSize.width - 10f, iSize.height - 8f))
                    drawScope.drawCircle(Color(0x99FFFFFF), radius = 2.5f, center = Offset(pos.x + iSize.width * 0.4f, pos.y + iSize.height * 0.5f))
                }

                // Worn goalmouth icy scrapes
                drawScope.drawOval(Color(0x44B0D2E2), topLeft = Offset(GOAL_X_LEFT - 10f, PITCH_MARGIN_Y), size = Size(GOAL_WIDTH + 20f, SIX_YARD_H + 20f))
                drawScope.drawOval(Color(0x44B0D2E2), topLeft = Offset(GOAL_X_LEFT - 10f, PITCH_H - PITCH_MARGIN_Y - SIX_YARD_H - 20f), size = Size(GOAL_WIDTH + 20f, SIX_YARD_H + 20f))
            }
            com.example.openswos.model.PitchType.GRASS -> {
                // Classic rich English mower lawn stripes
                val stripeHeight = 90f
                var currentY = 0f
                var stripeIdx = 0
                while (currentY < PITCH_H) {
                    val color = if (stripeIdx % 2 == 0) c1 else c2
                    drawScope.drawRect(color, topLeft = Offset(0f, currentY), size = Size(PITCH_W, stripeHeight))
                    currentY += stripeHeight
                    stripeIdx++
                }
            }
        }

        // 3. Touchlines, Goal Lines, and Pitch Markings (using pitchType.lineColor)
        val lineColor = pitchType.lineColor
        val lineStroke = Stroke(width = if (pitchType == com.example.openswos.model.PitchType.FROZEN) 4f else 3.5f)

        // Outer Touchlines & Goal lines
        drawScope.drawRect(
            color = lineColor,
            topLeft = Offset(PITCH_MARGIN_X, PITCH_MARGIN_Y),
            size = Size(PITCH_W - 2 * PITCH_MARGIN_X, PITCH_H - 2 * PITCH_MARGIN_Y),
            style = lineStroke
        )

        // Halfway line & Center Circle & Center Spot
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

        // Penalty Areas (Top & Bottom)
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

        // Corner Arcs
        val cornerR = 20f
        drawScope.drawArc(lineColor, 0f, 90f, false, Offset(PITCH_MARGIN_X - cornerR, PITCH_MARGIN_Y - cornerR), Size(cornerR * 2, cornerR * 2), style = lineStroke)
        drawScope.drawArc(lineColor, 90f, 90f, false, Offset(PITCH_W - PITCH_MARGIN_X - cornerR, PITCH_MARGIN_Y - cornerR), Size(cornerR * 2, cornerR * 2), style = lineStroke)
        drawScope.drawArc(lineColor, 270f, 90f, false, Offset(PITCH_MARGIN_X - cornerR, PITCH_H - PITCH_MARGIN_Y - cornerR), Size(cornerR * 2, cornerR * 2), style = lineStroke)
        drawScope.drawArc(lineColor, 180f, 90f, false, Offset(PITCH_W - PITCH_MARGIN_X - cornerR, PITCH_H - PITCH_MARGIN_Y - cornerR), Size(cornerR * 2, cornerR * 2), style = lineStroke)

        // Goals & Net Mesh
        drawGoals(drawScope, engine)
    }

    private fun drawAdvertisingBoards(drawScope: DrawScope, pitchType: com.example.openswos.model.PitchType) {
        val boardColors = listOf(
            Color(0xFF0D47A1), // Royal Navy
            Color(0xFFB71C1C), // Crimson Red
            Color(0xFF00695C), // Deep Teal
            Color(0xFFE65100), // Retro Amber
            Color(0xFF4A148C), // Royal Purple
            Color(0xFF1B5E20)  // Forest Green
        )
        val boardWidth = 95f
        val boardHeight = 26f

        // Top Hoardings
        val topY = PITCH_MARGIN_Y - 50f
        var bx = 25f
        var colorIdx = 0
        while (bx < PITCH_W - 25f) {
            val bg = boardColors[colorIdx % boardColors.size]
            drawScope.drawRect(bg, topLeft = Offset(bx, topY), size = Size(boardWidth, boardHeight))
            drawScope.drawRect(Color(0x88FFFFFF), topLeft = Offset(bx, topY), size = Size(boardWidth, boardHeight), style = Stroke(width = 1f))
            // Simulated retro typography band
            drawScope.drawLine(Color.White, Offset(bx + 12f, topY + 13f), Offset(bx + boardWidth - 12f, topY + 13f), strokeWidth = 2.5f)
            bx += boardWidth + 10f
            colorIdx++
        }

        // Bottom Hoardings
        val botY = PITCH_H - PITCH_MARGIN_Y + 24f
        bx = 25f
        while (bx < PITCH_W - 25f) {
            val bg = boardColors[colorIdx % boardColors.size]
            drawScope.drawRect(bg, topLeft = Offset(bx, botY), size = Size(boardWidth, boardHeight))
            drawScope.drawRect(Color(0x88FFFFFF), topLeft = Offset(bx, botY), size = Size(boardWidth, boardHeight), style = Stroke(width = 1f))
            drawScope.drawLine(Color.White, Offset(bx + 12f, botY + 13f), Offset(bx + boardWidth - 12f, botY + 13f), strokeWidth = 2.5f)
            bx += boardWidth + 10f
            colorIdx++
        }

        // For Frozen: shoveled snow mounds along the hoardings
        if (pitchType == com.example.openswos.model.PitchType.FROZEN) {
            var sx = 10f
            while (sx < PITCH_W) {
                drawScope.drawOval(Color(0xFFFFFFFF), topLeft = Offset(sx, topY + 20f), size = Size(50f, 14f))
                drawScope.drawOval(Color(0xFFFFFFFF), topLeft = Offset(sx, botY - 8f), size = Size(50f, 14f))
                sx += 45f
            }
        }
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
        val pitchType = engine.settings.pitchType

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

        // Ball circle (vivid orange on Frozen snow, off-white on Mud, crisp white on Grass/Astro)
        drawScope.drawCircle(
            color = pitchType.ballColor,
            radius = ballRadius,
            center = Offset(bx, ballElevY)
        )
        // Ball outline
        drawScope.drawCircle(
            color = pitchType.ballTrimColor,
            radius = ballRadius,
            center = Offset(bx, ballElevY),
            style = Stroke(width = 1f)
        )
        // Pentagons / pattern
        drawScope.drawCircle(
            color = pitchType.ballTrimColor,
            radius = ballRadius * 0.35f,
            center = Offset(bx - 0.5f, ballElevY - 0.5f)
        )
    }

    fun drawRadar(drawScope: DrawScope, engine: MatchEngine, radarW: Float, radarH: Float, offset: Offset) {
        val pitchType = engine.settings.pitchType
        // Radar pitch background
        drawScope.drawRect(
            color = pitchType.radarBgColor,
            topLeft = offset,
            size = Size(radarW, radarH)
        )
        drawScope.drawRect(
            color = pitchType.radarLineColor,
            topLeft = offset,
            size = Size(radarW, radarH),
            style = Stroke(width = 1.5f)
        )
        // Radar Halfway Line
        drawScope.drawLine(
            color = pitchType.radarLineColor,
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

        // Draw Ball dot (matches pitch ball color, e.g. orange on Frozen)
        val ballRx = offset.x + engine.ball.x * scaleX
        val ballRy = offset.y + engine.ball.y * scaleY
        drawScope.drawCircle(pitchType.ballColor, radius = 2.8f, center = Offset(ballRx, ballRy))
        drawScope.drawCircle(pitchType.ballTrimColor, radius = 2.8f, center = Offset(ballRx, ballRy), style = Stroke(width = 0.8f))
    }

    /**
     * Atmospheric weather effects rendered over the pitch (falling rain for Wet/Mud, drifting snow for Frozen)
     */
    fun drawWeather(drawScope: DrawScope, engine: MatchEngine) {
        val pitchType = engine.settings.pitchType
        val clock = engine.matchTimeSeconds

        when (pitchType) {
            com.example.openswos.model.PitchType.WET_MUD -> {
                // Diagonal heavy rain streaks
                val rainCount = 85
                val animSpeed = 480f
                for (i in 0 until rainCount) {
                    val seedX = (i * 137.5f) % PITCH_W
                    val seedY = (i * 269.3f) % PITCH_H
                    val progressY = (seedY + clock * animSpeed) % PITCH_H
                    val progressX = (seedX + (clock * animSpeed * 0.35f)) % PITCH_W
                    drawScope.drawLine(
                        color = Color(0x66B0BEC5),
                        start = Offset(progressX, progressY),
                        end = Offset(progressX + 7f, progressY + 20f),
                        strokeWidth = 1.2f
                    )
                }

                // Water splash ripple circles around puddles / pitch
                val ripples = listOf(
                    Offset(190f, 330f), Offset(540f, 390f), Offset(240f, 630f),
                    Offset(490f, 700f), Offset(180f, 960f), Offset(360f, 490f)
                )
                for ((idx, rPos) in ripples.withIndex()) {
                    val phase = ((clock * 3.5f + idx * 0.6f) % 1f)
                    val radius = phase * 16f
                    val alpha = (1f - phase).coerceIn(0f, 1f) * 0.45f
                    drawScope.drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = radius,
                        center = rPos,
                        style = Stroke(width = 1.2f)
                    )
                }
            }
            com.example.openswos.model.PitchType.FROZEN -> {
                // Drifting white snowflakes with swaying movement
                val snowCount = 75
                val fallSpeed = 90f
                for (i in 0 until snowCount) {
                    val seedX = (i * 173.1f) % PITCH_W
                    val seedY = (i * 311.7f) % PITCH_H
                    val sway = (sin((clock * 2.2f + i).toDouble()).toFloat()) * 14f
                    val progressY = (seedY + clock * fallSpeed) % PITCH_H
                    val progressX = ((seedX + sway + PITCH_W) % PITCH_W)
                    val flakeSize = 1.6f + (i % 3) * 0.8f
                    drawScope.drawCircle(
                        color = Color(0xEEFFFFFF),
                        radius = flakeSize,
                        center = Offset(progressX, progressY)
                    )
                }
            }
            else -> {
                // Clear conditions for Grass & Astro
            }
        }
    }
}
