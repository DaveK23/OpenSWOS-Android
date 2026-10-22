package com.example.openswos.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.example.openswos.audio.RetroAudio
import com.example.openswos.model.Difficulty
import com.example.openswos.model.FormationType
import com.example.openswos.model.MatchEvent
import com.example.openswos.model.MatchPeriod
import com.example.openswos.model.MatchSettings
import com.example.openswos.model.MatchStats
import com.example.openswos.model.PitchType
import com.example.openswos.model.Player
import com.example.openswos.model.PlayerPosition
import com.example.openswos.model.Team
import com.example.openswos.model.TeamSide
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Constants for Pitch geometry (SWOS scaled dimensions)
const val PITCH_W = 700f
const val PITCH_H = 1300f
const val PITCH_MARGIN_X = 50f
const val PITCH_MARGIN_Y = 60f

const val GOAL_WIDTH = 140f
const val GOAL_DEPTH = 36f
const val GOAL_X_LEFT = (PITCH_W - GOAL_WIDTH) / 2f
const val GOAL_X_RIGHT = GOAL_X_LEFT + GOAL_WIDTH
const val GOAL_TOP_Y = PITCH_MARGIN_Y
const val GOAL_BOT_Y = PITCH_H - PITCH_MARGIN_Y

const val PENALTY_AREA_W = 340f
const val PENALTY_AREA_H = 180f
const val PENALTY_TOP_Y = PITCH_MARGIN_Y + PENALTY_AREA_H
const val PENALTY_BOT_Y = PITCH_H - PITCH_MARGIN_Y - PENALTY_AREA_H
const val PENALTY_X_LEFT = (PITCH_W - PENALTY_AREA_W) / 2f
const val PENALTY_X_RIGHT = PENALTY_X_LEFT + PENALTY_AREA_W

const val SIX_YARD_W = 180f
const val SIX_YARD_H = 65f
const val SIX_YARD_X_LEFT = (PITCH_W - SIX_YARD_W) / 2f
const val SIX_YARD_X_RIGHT = SIX_YARD_X_LEFT + SIX_YARD_W

const val CENTER_X = PITCH_W / 2f
const val CENTER_Y = PITCH_H / 2f
const val CENTER_CIRCLE_R = 75f

class BallEntity {
    var x: Float = CENTER_X
    var y: Float = CENTER_Y
    var z: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var vz: Float = 0f
    var spinX: Float = 0f
    var spinY: Float = 0f

    var owner: PlayerEntity? = null
    var lastKicker: PlayerEntity? = null
    var isDeadBall: Boolean = false
    var aftertouchWindow: Float = 0f

    fun reset(px: Float = CENTER_X, py: Float = CENTER_Y) {
        x = px
        y = py
        z = 0f
        vx = 0f
        vy = 0f
        vz = 0f
        spinX = 0f
        spinY = 0f
        owner = null
        lastKicker = null
        isDeadBall = false
        aftertouchWindow = 0f
    }
}

class PlayerEntity(
    val player: Player,
    val side: TeamSide,
    val isGoalkeeper: Boolean = player.position == PlayerPosition.GK
) {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var facingAngle: Float = if (side == TeamSide.HOME) -PI.toFloat() / 2f else PI.toFloat() / 2f
    var animFrame: Float = 0f

    var isSliding: Boolean = false
    var slideTimer: Float = 0f
    var slideDirX: Float = 0f
    var slideDirY: Float = 0f

    var isDiving: Boolean = false
    var diveTimer: Float = 0f
    var diveDirX: Float = 0f

    var isKicking: Boolean = false
    var kickTimer: Float = 0f

    var stamina: Float = 100f
    var yellowCards: Int = 0
    var isSentOff: Boolean = false

    // Target formation anchor in normalized 0..1 pitch space
    var anchorNormX: Float = 0.5f
    var anchorNormY: Float = 0.5f

    val speedStat: Float get() = player.stats.speed
    val shotStat: Float get() = player.stats.shooting
    val passStat: Float get() = player.stats.passing
    val tackleStat: Float get() = player.stats.tackling
    val controlStat: Float get() = player.stats.control

    fun resetPosition(pitchW: Float, pitchH: Float, isFirstHalf: Boolean) {
        val effectiveSide = if (isFirstHalf) side else (if (side == TeamSide.HOME) TeamSide.AWAY else TeamSide.HOME)
        val py = if (effectiveSide == TeamSide.HOME) {
            PITCH_MARGIN_Y + anchorNormY * (pitchH - 2 * PITCH_MARGIN_Y)
        } else {
            PITCH_H - PITCH_MARGIN_Y - anchorNormY * (pitchH - 2 * PITCH_MARGIN_Y)
        }
        x = PITCH_MARGIN_X + anchorNormX * (pitchW - 2 * PITCH_MARGIN_X)
        y = py
        z = 0f
        vx = 0f
        vy = 0f
        facingAngle = if (effectiveSide == TeamSide.HOME) -PI.toFloat() / 2f else PI.toFloat() / 2f
        isSliding = false
        isDiving = false
        isKicking = false
    }
}

class MatchEngine(
    val settings: MatchSettings,
    val audio: RetroAudio
) {
    val ball = BallEntity()
    val homePlayers = mutableListOf<PlayerEntity>()
    val awayPlayers = mutableListOf<PlayerEntity>()
    val stats = MatchStats()

    var homeGoals by mutableIntStateOf(0)
    var awayGoals by mutableIntStateOf(0)
    var period by mutableStateOf(MatchPeriod.PRE_MATCH)
    var matchTimeSeconds: Float = 0f
    var matchClockDisplay by mutableStateOf("00:00")
    val halfDurationSeconds: Float = (settings.matchMinutes * 60f) / 2f
    var isPaused by mutableStateOf(false)

    var controlledPlayer: PlayerEntity? = null
    var currentEventBanner by mutableStateOf<String?>(null)
    var bannerTimer: Float = 0f

    var cameraX: Float = CENTER_X
    var cameraY: Float = CENTER_Y

    // Controls state
    var joystickX: Float = 0f
    var joystickY: Float = 0f
    var actionCharge by mutableFloatStateOf(0f)
    var isAftertouchActive by mutableStateOf(false)
    var playerHasBall by mutableStateOf(false)
    var isActionHeld: Boolean = false
    var isShootHeld: Boolean = false
    var isTacklePressed: Boolean = false

    // Restart state
    var restartType: RestartType? = RestartType.KICK_OFF
    var restartSide: TeamSide = TeamSide.HOME
    var restartPos = Offset(CENTER_X, CENTER_Y)
    var restartDelay: Float = 1.0f
    var restartHoldTimer: Float = 0f
    var outOfBoundsCooldown: Float = 0f

    // Net wobble animation effect
    var topNetWobble: Float = 0f
    var botNetWobble: Float = 0f

    enum class RestartType {
        KICK_OFF, THROW_IN, CORNER, GOAL_KICK, PENALTY, FREE_KICK
    }

    init {
        initSquads()
        setupKickoff(TeamSide.HOME)
    }

    private fun initSquads() {
        homePlayers.clear()
        awayPlayers.clear()

        setupFormationAnchors(settings.homeTeam.formation, homePlayers, settings.homeTeam, TeamSide.HOME)
        setupFormationAnchors(settings.awayTeam.formation, awayPlayers, settings.awayTeam, TeamSide.AWAY)

        val isFirstHalf = period <= MatchPeriod.HALF_TIME
        homePlayers.forEach { it.resetPosition(PITCH_W, PITCH_H, isFirstHalf) }
        awayPlayers.forEach { it.resetPosition(PITCH_W, PITCH_H, isFirstHalf) }
        controlledPlayer = homePlayers.firstOrNull { !it.isGoalkeeper }
    }

    private fun setupFormationAnchors(
        formation: FormationType,
        targetList: MutableList<PlayerEntity>,
        team: Team,
        side: TeamSide
    ) {
        val roster = team.roster.take(11)
        val anchors = when (formation) {
            FormationType.F_4_4_2 -> listOf(
                Offset(0.5f, 0.95f),  // GK
                Offset(0.18f, 0.80f), // LB
                Offset(0.38f, 0.83f), // CB1
                Offset(0.62f, 0.83f), // CB2
                Offset(0.82f, 0.80f), // RB
                Offset(0.15f, 0.55f), // LM
                Offset(0.38f, 0.58f), // CM1
                Offset(0.62f, 0.58f), // CM2
                Offset(0.85f, 0.55f), // RM
                Offset(0.38f, 0.35f), // CF1
                Offset(0.62f, 0.35f)  // CF2
            )
            FormationType.F_4_3_3 -> listOf(
                Offset(0.5f, 0.95f),  // GK
                Offset(0.18f, 0.80f), // LB
                Offset(0.38f, 0.83f), // CB1
                Offset(0.62f, 0.83f), // CB2
                Offset(0.82f, 0.80f), // RB
                Offset(0.25f, 0.60f), // CM1
                Offset(0.50f, 0.64f), // CM2
                Offset(0.75f, 0.60f), // CM3
                Offset(0.18f, 0.34f), // LW
                Offset(0.50f, 0.30f), // CF
                Offset(0.82f, 0.34f)  // RW
            )
            FormationType.F_3_5_2 -> listOf(
                Offset(0.5f, 0.95f),  // GK
                Offset(0.28f, 0.82f), // CB1
                Offset(0.50f, 0.84f), // CB2
                Offset(0.72f, 0.82f), // CB3
                Offset(0.12f, 0.60f), // LWB
                Offset(0.35f, 0.62f), // CM1
                Offset(0.50f, 0.56f), // CAM
                Offset(0.65f, 0.62f), // CM2
                Offset(0.88f, 0.60f), // RWB
                Offset(0.38f, 0.35f), // CF1
                Offset(0.62f, 0.35f)  // CF2
            )
            FormationType.F_5_3_2 -> listOf(
                Offset(0.5f, 0.95f),  // GK
                Offset(0.15f, 0.78f), // LB
                Offset(0.32f, 0.82f), // CB1
                Offset(0.50f, 0.84f), // CB2
                Offset(0.68f, 0.82f), // CB3
                Offset(0.85f, 0.78f), // RB
                Offset(0.28f, 0.58f), // CM1
                Offset(0.50f, 0.55f), // CM2
                Offset(0.72f, 0.58f), // CM3
                Offset(0.38f, 0.36f), // CF1
                Offset(0.62f, 0.36f)  // CF2
            )
            FormationType.F_4_2_4 -> listOf(
                Offset(0.5f, 0.95f),  // GK
                Offset(0.18f, 0.80f), // LB
                Offset(0.38f, 0.83f), // CB1
                Offset(0.62f, 0.83f), // CB2
                Offset(0.82f, 0.80f), // RB
                Offset(0.38f, 0.60f), // CM1
                Offset(0.62f, 0.60f), // CM2
                Offset(0.15f, 0.34f), // LW
                Offset(0.38f, 0.30f), // CF1
                Offset(0.62f, 0.30f), // CF2
                Offset(0.85f, 0.34f)  // RW
            )
        }

        roster.forEachIndexed { idx, player ->
            val anchor = anchors.getOrElse(idx) { Offset(0.5f, 0.5f) }
            val entity = PlayerEntity(player, side)
            entity.anchorNormX = anchor.x
            entity.anchorNormY = anchor.y
            targetList.add(entity)
        }
    }

    fun setupKickoff(side: TeamSide) {
        restartType = RestartType.KICK_OFF
        restartSide = side
        restartPos = Offset(CENTER_X, CENTER_Y)
        ball.reset(CENTER_X, CENTER_Y)
        restartDelay = 1.0f
        restartHoldTimer = 0f

        val isFirstHalf = period <= MatchPeriod.HALF_TIME
        homePlayers.forEach { it.resetPosition(PITCH_W, PITCH_H, isFirstHalf) }
        awayPlayers.forEach { it.resetPosition(PITCH_W, PITCH_H, isFirstHalf) }

        // Position kick-off taker
        val kickOffSquad = if (side == TeamSide.HOME) homePlayers else awayPlayers
        val taker = kickOffSquad.maxByOrNull { it.player.position.ordinal } ?: kickOffSquad[9]
        taker.x = CENTER_X
        taker.y = CENTER_Y + (if ((side == TeamSide.HOME && isFirstHalf) || (side == TeamSide.AWAY && !isFirstHalf)) 12f else -12f)
        taker.facingAngle = if (taker.y > CENTER_Y) -PI.toFloat() / 2f else PI.toFloat() / 2f
        ball.owner = taker
        ball.lastKicker = taker
        if (side == TeamSide.HOME) controlledPlayer = taker
        audio.playWhistle(false)
    }

    fun tick(dt: Float) {
        if (isPaused) return

        // Update timers
        if (bannerTimer > 0f) {
            bannerTimer -= dt
            if (bannerTimer <= 0f) {
                currentEventBanner = null
            }
        }
        if (topNetWobble > 0f) topNetWobble = max(0f, topNetWobble - dt * 2.5f)
        if (botNetWobble > 0f) botNetWobble = max(0f, botNetWobble - dt * 2.5f)

        // Pitch out-of-bounds boundary cooldown
        if (outOfBoundsCooldown > 0f) {
            outOfBoundsCooldown = max(0f, outOfBoundsCooldown - dt)
        }

        // Dead ball / Restart delay & handling
        if (restartType != null) {
            if (restartDelay > 0f) {
                restartDelay -= dt
            }
            if (restartDelay <= 0f) {
                // Restart delay elapsed - ready to resume play
                val squad = if (restartSide == TeamSide.HOME) homePlayers else awayPlayers
                val taker = ball.owner ?: squad.firstOrNull()

                if (restartSide == TeamSide.AWAY) {
                    // CPU auto-executes restart immediately
                    if (taker != null) {
                        executeRestartPass(taker, isThrowIn = (restartType == RestartType.THROW_IN))
                    } else {
                        restartType = null
                        outOfBoundsCooldown = 1.0f
                    }
                    updateCamera(dt)
                    return
                }
                // Human player restart: wait indefinitely for player to aim and execute
            }

            // Keep ball held/attached to taker during restart
            val currentTaker = ball.owner
            if (currentTaker != null) {
                if (restartType == RestartType.THROW_IN) {
                    ball.x = currentTaker.x + cos(currentTaker.facingAngle) * 8f
                    ball.y = currentTaker.y + sin(currentTaker.facingAngle) * 8f
                    ball.z = 12f
                } else {
                    ball.x = currentTaker.x + cos(currentTaker.facingAngle) * 6f
                    ball.y = currentTaker.y + sin(currentTaker.facingAngle) * 6f
                    ball.z = 0f
                }
            }

            // If human controlled taker during restart, allow aiming facing angle
            if (restartSide == TeamSide.HOME && controlledPlayer != null) {
                val player = controlledPlayer!!
                val mag = sqrt(joystickX * joystickX + joystickY * joystickY)
                if (mag > 0.2f) {
                    val joyAngle = atan2(joystickY, joystickX)
                    if (restartType == RestartType.THROW_IN) {
                        val isLeftTouchline = player.x < CENTER_X
                        if (isLeftTouchline) {
                            // Facing right into pitch (-75 deg to +75 deg)
                            player.facingAngle = joyAngle.coerceIn(-1.3f, 1.3f)
                        } else {
                            // Facing left into pitch
                            player.facingAngle = if (joyAngle in -1.3f..1.3f) {
                                if (joyAngle >= 0) (PI - 0.2).toFloat() else (-PI + 0.2).toFloat()
                            } else {
                                joyAngle
                            }
                        }
                    } else {
                        player.facingAngle = joyAngle
                    }
                }

                // If user pressed action/shoot during restart, trigger the pass immediately
                if (isActionHeld || isShootHeld) {
                    val isThrow = (restartType == RestartType.THROW_IN)
                    isActionHeld = false
                    isShootHeld = false
                    actionCharge = 0f
                    executeRestartPass(player, isThrowIn = isThrow)
                }
            }

            // Animate kick timer if active
            controlledPlayer?.let { p ->
                if (p.isKicking) {
                    p.kickTimer -= dt
                    if (p.kickTimer <= 0f) p.isKicking = false
                }
            }

            updateCamera(dt)
            return
        }

        // Match clock
        if (period == MatchPeriod.PRE_MATCH) {
            period = MatchPeriod.FIRST_HALF
            audio.playWhistle(false)
        }

        if (period == MatchPeriod.FIRST_HALF) {
            matchTimeSeconds += dt
            if (matchTimeSeconds >= halfDurationSeconds) {
                period = MatchPeriod.HALF_TIME
                currentEventBanner = "HALF TIME"
                bannerTimer = 3.0f
                audio.playWhistle(true)
                restartDelay = 3.0f
                return
            }
        } else if (period == MatchPeriod.HALF_TIME) {
            matchTimeSeconds = halfDurationSeconds
            period = MatchPeriod.SECOND_HALF
            setupKickoff(TeamSide.AWAY)
            return
        } else if (period == MatchPeriod.SECOND_HALF) {
            matchTimeSeconds += dt
            if (matchTimeSeconds >= halfDurationSeconds * 2) {
                period = MatchPeriod.FULL_TIME
                currentEventBanner = "FULL TIME"
                bannerTimer = 4.0f
                audio.playWhistle(true)
                return
            }
        }

        // Update reactive UI indicators
        playerHasBall = (ball.owner != null && ball.owner == controlledPlayer) || (restartType != null && restartSide == TeamSide.HOME)
        isAftertouchActive = ball.aftertouchWindow > 0f && (abs(ball.spinX) > 0.2f || abs(ball.spinY) > 0.2f)

        // Update clock display
        val totalHalf = halfDurationSeconds
        val displayMin = if (period <= MatchPeriod.HALF_TIME) {
            ((matchTimeSeconds / totalHalf) * 45f).toInt().coerceIn(0, 45)
        } else {
            (45f + ((matchTimeSeconds - totalHalf) / totalHalf) * 45f).toInt().coerceIn(45, 90)
        }
        val minStr = if (displayMin < 10) "0$displayMin" else "$displayMin"
        val formattedClock = "$minStr:00"
        if (matchClockDisplay != formattedClock) {
            matchClockDisplay = formattedClock
        }

        // Update possession stats
        if (ball.owner != null) {
            if (ball.owner?.side == TeamSide.HOME) {
                stats.homePossessionSeconds += dt
            } else {
                stats.awayPossessionSeconds += dt
            }
        }

        // 1. Process User Player Input
        processUserInput(dt)

        // 2. Process AI Players
        processAi(dt)

        // 3. Process Ball Physics & Collisions
        updateBallPhysics(dt)

        // 4. Check Goals & Out of Bounds
        checkPitchRules()

        // 5. Update Camera
        updateCamera(dt)
    }

    private fun processUserInput(dt: Float) {
        val player = controlledPlayer ?: return
        if (player.isSentOff) {
            pickNextControlledPlayer()
            return
        }

        // Handle Slide Tackle
        if (player.isSliding) {
            player.slideTimer -= dt
            player.x += player.slideDirX * dt * 260f
            player.y += player.slideDirY * dt * 260f
            // Check tackling ball
            checkTackleContact(player)
            if (player.slideTimer <= 0f) {
                player.isSliding = false
            }
            return
        }

        // Move with Joystick
        val mag = sqrt(joystickX * joystickX + joystickY * joystickY)
        if (mag > 0.15f) {
            val normX = joystickX / mag
            val normY = joystickY / mag
            player.facingAngle = atan2(normY, normX)
            val moveSpeed = (player.speedStat * 22f + 110f) * (player.stamina / 100f).coerceAtLeast(0.7f)
            player.x += normX * moveSpeed * dt
            player.y += normY * moveSpeed * dt
            player.animFrame = (player.animFrame + dt * 10f) % 4f

            // Clamp inside pitch
            player.x = player.x.coerceIn(PITCH_MARGIN_X - 10f, PITCH_W - PITCH_MARGIN_X + 10f)
            player.y = player.y.coerceIn(PITCH_MARGIN_Y - 10f, PITCH_H - PITCH_MARGIN_Y + 10f)

            // Dribble if owner
            if (ball.owner == player) {
                ball.x = player.x + cos(player.facingAngle) * 14f
                ball.y = player.y + sin(player.facingAngle) * 14f
                ball.z = 0f
            }
        } else {
            player.animFrame = 0f
        }

        // Apply Aftertouch if ball airborne and recently kicked
        if (ball.aftertouchWindow > 0f) {
            ball.aftertouchWindow -= dt
            if (mag > 0.2f) {
                ball.spinX += (joystickX * 5.0f * dt)
                ball.spinY += (joystickY * 5.0f * dt)
            }
        }

        // Action / Shoot Buttons
        if (isActionHeld) {
            actionCharge = min(1.0f, actionCharge + dt * 2.5f)
        }

        // If Tackle Pressed while defending
        if (isTacklePressed && !player.isSliding && ball.owner != player) {
            player.isSliding = true
            player.slideTimer = 0.45f
            player.slideDirX = cos(player.facingAngle)
            player.slideDirY = sin(player.facingAngle)
            audio.playSlideTackle()
            isTacklePressed = false
        }
    }

    fun onActionRelease() {
        val player = controlledPlayer ?: return
        val charge = actionCharge
        actionCharge = 0f
        isActionHeld = false

        if (restartType != null) {
            executeRestartPass(player, isThrowIn = (restartType == RestartType.THROW_IN))
            return
        }

        if (ball.owner == player) {
            if (charge < 0.22f) {
                // Short Tap: Ground Pass to best positioned teammate
                passToTeammate(player)
            } else {
                // Long Hold: Power Shot with aftertouch enabled!
                shootBall(player, charge)
            }
        } else {
            // Defending: attempt slide tackle
            if (!player.isSliding) {
                player.isSliding = true
                player.slideTimer = 0.45f
                player.slideDirX = cos(player.facingAngle)
                player.slideDirY = sin(player.facingAngle)
                audio.playSlideTackle()
            }
        }
    }

    fun executeRestartPass(taker: PlayerEntity, isThrowIn: Boolean) {
        val teammates = (if (taker.side == TeamSide.HOME) homePlayers else awayPlayers)
            .filter { it != taker && !it.isGoalkeeper && !it.isSentOff }

        val dirX = cos(taker.facingAngle)
        val dirY = sin(taker.facingAngle)

        // Find teammate best positioned in direction of throw/kick
        val target = teammates.maxByOrNull { tm ->
            val dx = tm.x - taker.x
            val dy = tm.y - taker.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val dot = (dx / dist) * dirX + (dy / dist) * dirY
            dot * 2.0f - (abs(dist - 240f) / 500f)
        } ?: teammates.firstOrNull()

        ball.owner = null
        ball.lastKicker = taker
        taker.isKicking = true
        taker.kickTimer = 0.25f

        if (isThrowIn) {
            val throwSpeed = (taker.passStat * 16f + 170f)
            if (target != null) {
                val dx = target.x - taker.x
                val dy = target.y - taker.y
                val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val speed = throwSpeed.coerceIn(130f, d * 2.2f)
                ball.vx = (dx / d) * speed
                ball.vy = (dy / d) * speed
                ball.vz = 5.5f
            } else {
                ball.vx = dirX * throwSpeed
                ball.vy = dirY * throwSpeed
                ball.vz = 5.5f
            }
            audio.playKick(0.45f)
        } else {
            // Corner, Goal Kick, Kickoff, Free Kick
            val isHighKick = (restartType == RestartType.CORNER || restartType == RestartType.GOAL_KICK)
            val kickSpeed = if (isHighKick) (taker.passStat * 24f + 250f) else (taker.passStat * 20f + 200f)
            if (target != null) {
                val dx = target.x - taker.x
                val dy = target.y - taker.y
                val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val speed = kickSpeed.coerceIn(150f, d * 2.5f)
                ball.vx = (dx / d) * speed
                ball.vy = (dy / d) * speed
                ball.vz = if (isHighKick) 6.5f else 1.5f
            } else {
                ball.vx = dirX * kickSpeed
                ball.vy = dirY * kickSpeed
                ball.vz = if (isHighKick) 6.5f else 1.5f
            }
            audio.playKick(0.7f)
        }

        if (taker.side == TeamSide.HOME && target != null) {
            controlledPlayer = target
        }

        restartType = null
        restartHoldTimer = 0f
        outOfBoundsCooldown = 1.0f
    }

    private fun passToTeammate(kicker: PlayerEntity) {
        val teammates = (if (kicker.side == TeamSide.HOME) homePlayers else awayPlayers)
            .filter { it != kicker && !it.isGoalkeeper }

        val dirX = cos(kicker.facingAngle)
        val dirY = sin(kicker.facingAngle)

        // Find teammate most aligned with stick direction
        val target = teammates.maxByOrNull { tm ->
            val dx = tm.x - kicker.x
            val dy = tm.y - kicker.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val dot = (dx / dist) * dirX + (dy / dist) * dirY
            dot - (dist / 1400f) * 0.3f
        } ?: teammates.firstOrNull()

        ball.owner = null
        ball.lastKicker = kicker
        kicker.isKicking = true
        kicker.kickTimer = 0.2f

        val passPower = (kicker.passStat * 28f + 240f)
        if (target != null) {
            val dx = target.x - kicker.x
            val dy = target.y - kicker.y
            val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            ball.vx = (dx / d) * passPower
            ball.vy = (dy / d) * passPower
            ball.vz = 1.5f
        } else {
            ball.vx = dirX * passPower
            ball.vy = dirY * passPower
            ball.vz = 1.0f
        }

        audio.playKick(0.6f)
        controlledPlayer = target
    }

    private fun shootBall(kicker: PlayerEntity, power: Float) {
        ball.owner = null
        ball.lastKicker = kicker
        kicker.isKicking = true
        kicker.kickTimer = 0.25f

        val isFirstHalf = period <= MatchPeriod.HALF_TIME
        val attackingTop = (kicker.side == TeamSide.HOME && isFirstHalf) || (kicker.side == TeamSide.AWAY && !isFirstHalf)
        val goalTargetY = if (attackingTop) GOAL_TOP_Y else GOAL_BOT_Y

        val dirX = cos(kicker.facingAngle)
        val dirY = sin(kicker.facingAngle)

        val shotSpeed = (kicker.shotStat * 52f + 320f) * (0.6f + power * 0.65f)
        ball.vx = dirX * shotSpeed
        ball.vy = dirY * shotSpeed
        ball.vz = (kicker.shotStat * 1.8f + 10f) * (power * 0.9f + 0.3f)

        // Enable aftertouch window so player can curl it!
        ball.aftertouchWindow = 0.7f
        ball.spinX = 0f
        ball.spinY = 0f

        audio.playKick(power)
        if (kicker.side == TeamSide.HOME) stats.homeShots++ else stats.awayShots++
    }

    private fun checkTackleContact(tackler: PlayerEntity) {
        val dx = ball.x - tackler.x
        val dy = ball.y - tackler.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 26f && ball.z < 12f) {
            ball.owner = null
            ball.lastKicker = tackler
            val tackleForce = (tackler.tackleStat * 30f + 180f)
            ball.vx = tackler.slideDirX * tackleForce
            ball.vy = tackler.slideDirY * tackleForce
            ball.vz = 3f
            audio.playKick(0.7f)
            if (tackler.side == TeamSide.HOME) controlledPlayer = tackler
        }
    }

    private fun processAi(dt: Float) {
        val isFirstHalf = period <= MatchPeriod.HALF_TIME
        val allOutfield = (homePlayers + awayPlayers).filter { !it.isGoalkeeper && it != controlledPlayer }

        allOutfield.forEach { entity ->
            val isAiTeam = entity.side == TeamSide.AWAY
            val effectiveAttackingTop = (entity.side == TeamSide.HOME && isFirstHalf) || (entity.side == TeamSide.AWAY && !isFirstHalf)

            // Calculate base formation target based on ball position
            val ballNormY = (ball.y - PITCH_MARGIN_Y) / (PITCH_H - 2 * PITCH_MARGIN_Y)
            val anchorX = PITCH_MARGIN_X + entity.anchorNormX * (PITCH_W - 2 * PITCH_MARGIN_X)

            // Formation dynamic shift
            val dynamicShiftY = (ballNormY - 0.5f) * 220f
            val anchorY = if (entity.side == TeamSide.HOME) {
                (PITCH_MARGIN_Y + entity.anchorNormY * (PITCH_H - 2 * PITCH_MARGIN_Y)) + dynamicShiftY
            } else {
                (PITCH_H - PITCH_MARGIN_Y - entity.anchorNormY * (PITCH_H - 2 * PITCH_MARGIN_Y)) - dynamicShiftY
            }

            val distToBall = sqrt((entity.x - ball.x) * (entity.x - ball.x) + (entity.y - ball.y) * (entity.y - ball.y))

            // AI Decision logic
            if (ball.owner == entity) {
                // Entity has ball
                val goalTargetX = CENTER_X + (Random.nextFloat() * 60f - 30f)
                val goalTargetY = if (effectiveAttackingTop) GOAL_TOP_Y else GOAL_BOT_Y
                val distToGoal = abs(entity.y - goalTargetY)

                if (distToGoal < 320f && abs(entity.x - CENTER_X) < 180f) {
                    // Shoot!
                    shootBall(entity, 0.85f)
                } else if (distToBall < 15f && Random.nextFloat() < 0.02f) {
                    // Pass to open teammate
                    passToTeammate(entity)
                } else {
                    // Dribble towards goal
                    val dx = goalTargetX - entity.x
                    val dy = goalTargetY - entity.y
                    val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                    val speed = (entity.speedStat * 18f + 95f) * settings.difficulty.shotAccuracy
                    entity.facingAngle = atan2(dy, dx)
                    entity.x += (dx / d) * speed * dt
                    entity.y += (dy / d) * speed * dt
                    ball.x = entity.x + cos(entity.facingAngle) * 14f
                    ball.y = entity.y + sin(entity.facingAngle) * 14f
                }
            } else {
                // Outfield without ball
                // If closest teammate to ball, chase ball!
                val teamList = if (entity.side == TeamSide.HOME) homePlayers else awayPlayers
                val closestToBall = teamList.filter { !it.isGoalkeeper }.minByOrNull {
                    (it.x - ball.x) * (it.x - ball.x) + (it.y - ball.y) * (it.y - ball.y)
                }

                if (entity == closestToBall && (isAiTeam || controlledPlayer == null)) {
                    // Chase ball
                    val dx = ball.x - entity.x
                    val dy = ball.y - entity.y
                    val d = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                    val speed = (entity.speedStat * 22f + 105f) * settings.difficulty.shotAccuracy
                    entity.facingAngle = atan2(dy, dx)
                    entity.x += (dx / d) * speed * dt
                    entity.y += (dy / d) * speed * dt

                    // Attempt tackle if close to opponent with ball
                    if (ball.owner != null && ball.owner?.side != entity.side && d < 32f && !entity.isSliding) {
                        if (Random.nextFloat() < 0.08f) {
                            entity.isSliding = true
                            entity.slideTimer = 0.45f
                            entity.slideDirX = dx / d
                            entity.slideDirY = dy / d
                            audio.playSlideTackle()
                        }
                    }
                } else {
                    // Return to tactical zone
                    val dx = anchorX - entity.x
                    val dy = anchorY - entity.y
                    val d = sqrt(dx * dx + dy * dy)
                    if (d > 20f) {
                        entity.facingAngle = atan2(dy, dx)
                        val returnSpeed = (entity.speedStat * 14f + 70f)
                        entity.x += (dx / d) * returnSpeed * dt
                        entity.y += (dy / d) * returnSpeed * dt
                    }
                }
            }
        }

        // Process Goalkeepers
        processGoalkeeper(homePlayers.firstOrNull { it.isGoalkeeper }, GOAL_BOT_Y, true, dt)
        processGoalkeeper(awayPlayers.firstOrNull { it.isGoalkeeper }, GOAL_TOP_Y, false, dt)
    }

    private fun processGoalkeeper(gk: PlayerEntity?, goalLineY: Float, isBottom: Boolean, dt: Float) {
        if (gk == null) return

        // Follow ball X on goal line
        val targetX = ball.x.coerceIn(GOAL_X_LEFT + 15f, GOAL_X_RIGHT - 15f)
        val dx = targetX - gk.x
        gk.x += dx * dt * 5.0f

        val distToGoal = abs(ball.y - goalLineY)
        val movingTowardsGoal = if (isBottom) ball.vy > 20f else ball.vy < -20f

        // Goalkeeper dive / parry / catch
        val distToBall = sqrt((gk.x - ball.x) * (gk.x - ball.x) + (gk.y - ball.y) * (gk.y - ball.y))
        if (distToGoal < 160f && movingTowardsGoal && distToBall < 55f && ball.z < 28f) {
            gk.isDiving = true
            gk.diveTimer = 0.5f

            // Parry or catch
            if (abs(ball.vx) + abs(ball.vy) < 220f) {
                // Catch ball
                ball.reset(gk.x, gk.y)
                ball.owner = gk
                audio.playKick(0.5f)
            } else {
                // Parry ball away
                ball.vx = (if (ball.x > gk.x) 180f else -180f) + Random.nextFloat() * 40f
                ball.vy = if (isBottom) -160f else 160f
                ball.vz = 8f
                audio.playKick(0.85f)
            }
        }
    }

    private fun updateBallPhysics(dt: Float) {
        if (ball.owner != null) {
            // Ball carried by owner
            return
        }

        // Apply Aftertouch / Spin curve
        if (ball.spinX != 0f || ball.spinY != 0f) {
            ball.vx += ball.spinX * dt * 80f
            ball.vy += ball.spinY * dt * 80f
            ball.spinX *= 0.94f
            ball.spinY *= 0.94f
        }

        // Apply wind
        ball.vx += settings.windStrength * dt * 10f

        // Gravity
        ball.vz -= 28f * dt

        // Move position
        ball.x += ball.vx * dt
        ball.y += ball.vy * dt
        ball.z += ball.vz * dt

        // Ground collision & bounce
        if (ball.z <= 0f) {
            ball.z = 0f
            if (abs(ball.vz) > 2.5f) {
                ball.vz = -ball.vz * settings.pitchType.bounceFactor
                if (abs(ball.vz) > 6f) audio.playKick(0.3f)
            } else {
                ball.vz = 0f
            }
            ball.vx *= settings.pitchType.frictionFactor
            ball.vy *= settings.pitchType.frictionFactor
        }

        // Loose ball interception by nearby players
        if (ball.z < 18f && ball.owner == null) {
            val allPlayers = homePlayers + awayPlayers
            for (p in allPlayers) {
                if (p.isSliding || p.isDiving) continue
                val dx = p.x - ball.x
                val dy = p.y - ball.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 18f) {
                    ball.owner = p
                    ball.lastKicker = p
                    ball.vx = 0f
                    ball.vy = 0f
                    ball.vz = 0f
                    if (p.side == TeamSide.HOME) {
                        controlledPlayer = p
                    }
                    break
                }
            }
        }
    }

    private fun checkPitchRules() {
        if (restartType != null || outOfBoundsCooldown > 0f) return

        // Goal Check Top (Away goal in first half)
        if (ball.y < GOAL_TOP_Y && ball.y > GOAL_TOP_Y - GOAL_DEPTH) {
            if (ball.x in GOAL_X_LEFT..GOAL_X_RIGHT && ball.z < 26f) {
                onGoalScored(TeamSide.HOME)
                topNetWobble = 1.0f
                return
            } else if (ball.x < GOAL_X_LEFT || ball.x > GOAL_X_RIGHT) {
                // Post hit
                if (abs(ball.x - GOAL_X_LEFT) < 6f || abs(ball.x - GOAL_X_RIGHT) < 6f) {
                    audio.playPostHit()
                    ball.vx = -ball.vx * 0.7f
                }
            }
        }

        // Goal Check Bottom (Home goal in first half)
        if (ball.y > GOAL_BOT_Y && ball.y < GOAL_BOT_Y + GOAL_DEPTH) {
            if (ball.x in GOAL_X_LEFT..GOAL_X_RIGHT && ball.z < 26f) {
                onGoalScored(TeamSide.AWAY)
                botNetWobble = 1.0f
                return
            } else if (ball.x < GOAL_X_LEFT || ball.x > GOAL_X_RIGHT) {
                // Post hit
                if (abs(ball.x - GOAL_X_LEFT) < 6f || abs(ball.x - GOAL_X_RIGHT) < 6f) {
                    audio.playPostHit()
                    ball.vx = -ball.vx * 0.7f
                }
            }
        }

        // Out of bounds - Sidelines (Throw In)
        val sidelineMargin = 8f
        if (ball.x < PITCH_MARGIN_X - sidelineMargin || ball.x > PITCH_W - PITCH_MARGIN_X + sidelineMargin) {
            val clampedX = if (ball.x < CENTER_X) PITCH_MARGIN_X else PITCH_W - PITCH_MARGIN_X
            val offendingSide = ball.lastKicker?.side ?: TeamSide.AWAY
            val awardedSide = if (offendingSide == TeamSide.HOME) TeamSide.AWAY else TeamSide.HOME
            triggerRestart(
                RestartType.THROW_IN,
                awardedSide,
                Offset(clampedX, ball.y.coerceIn(PITCH_MARGIN_Y + 50f, PITCH_H - PITCH_MARGIN_Y - 50f))
            )
            return
        }

        // Out of bounds - End lines (Corner or Goal Kick)
        val endlineMargin = 8f
        if (ball.y < PITCH_MARGIN_Y - endlineMargin) {
            val lastSide = ball.lastKicker?.side ?: TeamSide.HOME
            if (lastSide == TeamSide.AWAY) {
                // Goal Kick for Top Keeper
                triggerRestart(RestartType.GOAL_KICK, TeamSide.AWAY, Offset(CENTER_X, PITCH_MARGIN_Y + 40f))
            } else {
                // Corner Kick for Home
                stats.homeCorners++
                val cornerX = if (ball.x < CENTER_X) PITCH_MARGIN_X else PITCH_W - PITCH_MARGIN_X
                triggerRestart(RestartType.CORNER, TeamSide.HOME, Offset(cornerX, PITCH_MARGIN_Y))
            }
            return
        }

        if (ball.y > PITCH_H - PITCH_MARGIN_Y + endlineMargin) {
            val lastSide = ball.lastKicker?.side ?: TeamSide.AWAY
            if (lastSide == TeamSide.HOME) {
                // Goal Kick for Bottom Keeper
                triggerRestart(RestartType.GOAL_KICK, TeamSide.HOME, Offset(CENTER_X, PITCH_H - PITCH_MARGIN_Y - 40f))
            } else {
                // Corner Kick for Away
                stats.awayCorners++
                val cornerX = if (ball.x < CENTER_X) PITCH_MARGIN_X else PITCH_W - PITCH_MARGIN_X
                triggerRestart(RestartType.CORNER, TeamSide.AWAY, Offset(cornerX, PITCH_H - PITCH_MARGIN_Y))
            }
            return
        }
    }

    private fun onGoalScored(scoringSide: TeamSide) {
        val scorerName = ball.lastKicker?.player?.name ?: "Striker"
        if (scoringSide == TeamSide.HOME) {
            stats.homeGoals++
            homeGoals = stats.homeGoals
            currentEventBanner = "GOAL! $scorerName"
        } else {
            stats.awayGoals++
            awayGoals = stats.awayGoals
            currentEventBanner = "GOAL! $scorerName"
        }
        bannerTimer = 3.5f
        audio.playGoalCheer()

        val concedingSide = if (scoringSide == TeamSide.HOME) TeamSide.AWAY else TeamSide.HOME
        setupKickoff(concedingSide)
    }

    fun triggerRestart(type: RestartType, awardedSide: TeamSide, pos: Offset) {
        restartType = type
        restartSide = awardedSide
        restartPos = pos
        restartDelay = 0.8f
        restartHoldTimer = 0f
        audio.playWhistle(false)

        currentEventBanner = when (type) {
            RestartType.CORNER -> "CORNER KICK"
            RestartType.GOAL_KICK -> "GOAL KICK"
            RestartType.THROW_IN -> "THROW-IN"
            RestartType.PENALTY -> "PENALTY!"
            RestartType.FREE_KICK -> "FREE KICK"
            else -> null
        }
        bannerTimer = 1.8f

        // Move taker to restart position
        val squad = if (awardedSide == TeamSide.HOME) homePlayers else awayPlayers
        val taker = if (type == RestartType.GOAL_KICK) {
            squad.firstOrNull { it.isGoalkeeper } ?: squad[0]
        } else {
            squad.filter { !it.isGoalkeeper && !it.isSentOff }
                .minByOrNull { (it.x - pos.x) * (it.x - pos.x) + (it.y - pos.y) * (it.y - pos.y) }
                ?: squad[0]
        }

        if (type == RestartType.THROW_IN) {
            val isLeft = pos.x < CENTER_X
            val touchlineX = if (isLeft) PITCH_MARGIN_X else PITCH_W - PITCH_MARGIN_X
            taker.x = if (isLeft) touchlineX - 4f else touchlineX + 4f
            taker.y = pos.y
            taker.facingAngle = if (isLeft) 0f else PI.toFloat()

            ball.reset(touchlineX, pos.y)
            ball.owner = taker
            ball.lastKicker = taker
            ball.z = 12f
        } else if (type == RestartType.CORNER) {
            taker.x = pos.x
            taker.y = pos.y
            val targetGoalY = if (pos.y < CENTER_Y) PITCH_MARGIN_Y + 120f else PITCH_H - PITCH_MARGIN_Y - 120f
            taker.facingAngle = atan2(targetGoalY - pos.y, CENTER_X - pos.x)

            ball.reset(pos.x, pos.y)
            ball.owner = taker
            ball.lastKicker = taker
            ball.z = 0f
        } else if (type == RestartType.GOAL_KICK) {
            taker.x = pos.x
            taker.y = pos.y
            taker.facingAngle = if (pos.y < CENTER_Y) PI.toFloat() / 2f else -PI.toFloat() / 2f

            ball.reset(pos.x, pos.y)
            ball.owner = taker
            ball.lastKicker = taker
            ball.z = 0f
        } else {
            taker.x = pos.x
            taker.y = pos.y
            ball.reset(pos.x, pos.y)
            ball.owner = taker
            ball.lastKicker = taker
            ball.z = 0f
        }

        if (awardedSide == TeamSide.HOME) {
            controlledPlayer = taker
        }
    }

    private fun pickNextControlledPlayer() {
        val candidates = homePlayers.filter { !it.isGoalkeeper && !it.isSentOff }
        controlledPlayer = candidates.minByOrNull {
            (it.x - ball.x) * (it.x - ball.x) + (it.y - ball.y) * (it.y - ball.y)
        } ?: candidates.firstOrNull()
    }

    private fun updateCamera(dt: Float) {
        // Smooth dampening towards ball with slight lead towards action
        val targetCamX = ball.x.coerceIn(200f, PITCH_W - 200f)
        val targetCamY = ball.y.coerceIn(300f, PITCH_H - 300f)

        cameraX += (targetCamX - cameraX) * dt * 4.5f
        cameraY += (targetCamY - cameraY) * dt * 4.5f
    }
}
