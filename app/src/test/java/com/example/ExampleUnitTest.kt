package com.example

import com.example.openswos.audio.RetroAudio
import com.example.openswos.data.TeamDatabase
import com.example.openswos.engine.MatchEngine
import com.example.openswos.model.Difficulty
import com.example.openswos.model.MatchPeriod
import com.example.openswos.model.MatchSettings
import com.example.openswos.model.PitchType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTeamDatabaseLoad() {
        val teams = TeamDatabase.allTeams
        assertTrue("Should have multiple national and club teams", teams.size >= 8)
        val brazil = TeamDatabase.brazil
        assertEquals("Brazil", brazil.name)
        assertEquals(11, brazil.roster.size)
        assertTrue("Brazil rating should be high", brazil.overallRating >= 70)
    }

    @Test
    fun testMatchEngineKickoffAndTick() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england,
            matchMinutes = 3,
            pitchType = PitchType.GRASS,
            difficulty = Difficulty.SEMI_PRO
        )

        val engine = MatchEngine(settings, audio)
        assertNotNull(engine.controlledPlayer)
        assertEquals(MatchPeriod.PRE_MATCH, engine.period)

        // Clear kickoff restart delay to start match immediately
        engine.restartType = null
        engine.restartDelay = 0f

        // Advance engine tick
        engine.tick(0.016f)
        assertEquals(MatchPeriod.FIRST_HALF, engine.period)

        // Verify ball position inside pitch bounds
        assertTrue("Ball X inside pitch", engine.ball.x in 0f..700f)
        assertTrue("Ball Y inside pitch", engine.ball.y in 0f..1300f)
    }

    @Test
    fun testAftertouchPhysics() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        engine.restartType = null
        engine.restartDelay = 0f

        // Apply aftertouch spin
        engine.ball.vx = 0f
        engine.ball.vy = -200f
        engine.ball.z = 10f
        engine.ball.spinX = 2.0f
        engine.ball.owner = null

        val initialVx = engine.ball.vx
        engine.tick(0.05f)

        // Aftertouch should curve ball horizontally
        assertTrue("Spin should impart horizontal velocity", engine.ball.vx > initialVx)
    }

    @Test
    fun testThrowInExecutionHome() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        engine.period = MatchPeriod.FIRST_HALF

        // Trigger throw-in for Home team on the left touchline
        engine.triggerRestart(
            MatchEngine.RestartType.THROW_IN,
            com.example.openswos.model.TeamSide.HOME,
            androidx.compose.ui.geometry.Offset(50f, 600f)
        )

        assertEquals(MatchEngine.RestartType.THROW_IN, engine.restartType)
        assertNotNull(engine.ball.owner)
        assertEquals(engine.controlledPlayer, engine.ball.owner)
        assertTrue("Ball should be elevated in player's hands for throw-in", engine.ball.z >= 10f)

        // When user taps/releases action button to throw
        engine.onActionRelease()

        // Restart should be completed
        assertEquals("Restart type should be cleared", null, engine.restartType)
        assertEquals("Ball should be released from owner", null, engine.ball.owner)
        assertTrue("Ball should be launched with velocity", engine.ball.vx != 0f || engine.ball.vy != 0f)
        assertTrue("Ball should have throw arc elevation", engine.ball.vz > 0f)
        assertTrue("Pitch rules should be on cooldown to avoid instant re-trigger", engine.outOfBoundsCooldown > 0f)
    }

    @Test
    fun testThrowInExecutionAwayCpu() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        engine.period = MatchPeriod.FIRST_HALF

        // Trigger throw-in for Away (CPU) team on the right touchline
        engine.triggerRestart(
            MatchEngine.RestartType.THROW_IN,
            com.example.openswos.model.TeamSide.AWAY,
            androidx.compose.ui.geometry.Offset(670f, 600f)
        )

        assertEquals(MatchEngine.RestartType.THROW_IN, engine.restartType)
        assertEquals(com.example.openswos.model.TeamSide.AWAY, engine.restartSide)

        // Advance ticks beyond restartDelay
        engine.restartDelay = 0.05f
        engine.tick(0.06f)

        // CPU should have automatically executed the throw-in without getting stuck
        assertEquals("CPU should have executed throw-in and cleared restartType", null, engine.restartType)
        assertTrue("Ball outOfBoundsCooldown should be active", engine.outOfBoundsCooldown > 0f)
    }

    @Test
    fun testNoInactivityTimeoutOnRestart() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        engine.period = MatchPeriod.FIRST_HALF

        // Trigger throw-in for Home team
        engine.triggerRestart(
            MatchEngine.RestartType.THROW_IN,
            com.example.openswos.model.TeamSide.HOME,
            androidx.compose.ui.geometry.Offset(50f, 600f)
        )

        assertEquals(MatchEngine.RestartType.THROW_IN, engine.restartType)

        // Advance 10 seconds of idle game time (well past the old 3.5s timeout)
        for (i in 1..200) {
            engine.tick(0.05f)
        }

        // Must still be waiting for human player input
        assertEquals("Should not auto-pass or timeout due to inactivity", MatchEngine.RestartType.THROW_IN, engine.restartType)
        assertNotNull("Ball owner should still be held by thrower", engine.ball.owner)

        // Player now chooses to throw
        engine.onActionRelease()
        assertEquals("Restart should execute upon player input", null, engine.restartType)
        assertEquals(null, engine.ball.owner)
    }

    @Test
    fun testControlledPlayerNeverAwayOnSetterOrPassOrRestarts() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        val initialControlled = engine.controlledPlayer
        assertNotNull(initialControlled)
        assertEquals(com.example.openswos.model.TeamSide.HOME, initialControlled!!.side)

        // 1. Verify custom setter strictly rejects setting an AWAY player
        val awayPlayer = engine.awayPlayers.first()
        engine.controlledPlayer = awayPlayer
        assertEquals(
            "controlledPlayer should reject setting an AWAY player",
            com.example.openswos.model.TeamSide.HOME,
            engine.controlledPlayer?.side
        )

        // 2. Verify kickoff for away side leaves controlledPlayer as a HOME player
        engine.setupKickoff(com.example.openswos.model.TeamSide.AWAY)
        assertNotNull(engine.controlledPlayer)
        assertEquals(com.example.openswos.model.TeamSide.HOME, engine.controlledPlayer?.side)

        // 3. Verify away restarts leave controlledPlayer as a HOME player
        engine.triggerRestart(
            MatchEngine.RestartType.CORNER,
            com.example.openswos.model.TeamSide.AWAY,
            androidx.compose.ui.geometry.Offset(600f, 100f)
        )
        assertNotNull(engine.controlledPlayer)
        assertEquals(com.example.openswos.model.TeamSide.HOME, engine.controlledPlayer?.side)

        engine.triggerRestart(
            MatchEngine.RestartType.GOAL_KICK,
            com.example.openswos.model.TeamSide.AWAY,
            androidx.compose.ui.geometry.Offset(350f, 1200f)
        )
        assertNotNull(engine.controlledPlayer)
        assertEquals(com.example.openswos.model.TeamSide.HOME, engine.controlledPlayer?.side)
    }

    @Test
    fun testControlledPlayerRemainsHomeDuringCpuPossessionInBothHalves() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        engine.period = MatchPeriod.FIRST_HALF
        engine.restartType = null
        engine.restartDelay = 0f

        // Give ball to an away player in the top half (CPU defending half)
        val cpuPlayerTop = engine.awayPlayers.first { !it.isGoalkeeper }
        cpuPlayerTop.x = 350f
        cpuPlayerTop.y = 300f // top half
        engine.ball.owner = cpuPlayerTop
        engine.ball.x = 350f
        engine.ball.y = 300f

        // Run multiple ticks where CPU AI acts, dribbles, passes, etc.
        for (i in 1..100) {
            engine.tick(0.016f)
            assertNotNull(engine.controlledPlayer)
            assertEquals(
                "Controlled player must NEVER be an away player when CPU has ball in top half (tick $i)",
                com.example.openswos.model.TeamSide.HOME,
                engine.controlledPlayer?.side
            )
        }

        // Give ball to an away player in the bottom half (CPU attacking half)
        val cpuPlayerBottom = engine.awayPlayers.first { !it.isGoalkeeper }
        cpuPlayerBottom.x = 350f
        cpuPlayerBottom.y = 900f // bottom half
        engine.ball.owner = cpuPlayerBottom
        engine.ball.x = 350f
        engine.ball.y = 900f

        for (i in 1..100) {
            engine.tick(0.016f)
            assertNotNull(engine.controlledPlayer)
            assertEquals(
                "Controlled player must NEVER be an away player when CPU has ball in bottom half (tick $i)",
                com.example.openswos.model.TeamSide.HOME,
                engine.controlledPlayer?.side
            )
        }
    }

    @Test
    fun testGoalKickExecutionHome() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        engine.period = MatchPeriod.FIRST_HALF

        // Trigger goal kick for Home team (bottom goal in first half)
        engine.triggerRestart(
            MatchEngine.RestartType.GOAL_KICK,
            com.example.openswos.model.TeamSide.HOME,
            androidx.compose.ui.geometry.Offset(350f, 1200f)
        )

        assertEquals(MatchEngine.RestartType.GOAL_KICK, engine.restartType)
        assertEquals(com.example.openswos.model.TeamSide.HOME, engine.restartSide)
        assertTrue("playerHasBall should be true on Home restart", engine.playerHasBall)
        assertNotNull(engine.controlledPlayer)
        assertEquals(com.example.openswos.model.TeamSide.HOME, engine.controlledPlayer?.side)
        assertTrue("Controlled player on goal kick should be the goalkeeper", engine.controlledPlayer!!.isGoalkeeper)

        // Aiming joystick should adjust goalkeeper angle and keep ball placed
        engine.joystickX = 0f
        engine.joystickY = -1f // Aiming upfield
        engine.tick(0.016f)
        assertEquals(-Math.PI.toFloat() / 2f, engine.controlledPlayer!!.facingAngle, 0.05f)

        // Taking the goal kick via action release
        engine.onActionRelease()

        assertEquals("Restart type should be cleared", null, engine.restartType)
        assertEquals("Ball owner cleared on kick launch", null, engine.ball.owner)
        assertTrue("Goal kick should launch with high vertical arc", engine.ball.vz > 0f)
        assertTrue("Controlled player handed over to outfield teammate", !engine.controlledPlayer!!.isGoalkeeper)
        assertEquals(com.example.openswos.model.TeamSide.HOME, engine.controlledPlayer?.side)
    }

    @Test
    fun testGoalKickExecutionAwayCpu() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        engine.period = MatchPeriod.FIRST_HALF

        // Trigger goal kick for Away (CPU) team (top goal in first half)
        engine.triggerRestart(
            MatchEngine.RestartType.GOAL_KICK,
            com.example.openswos.model.TeamSide.AWAY,
            androidx.compose.ui.geometry.Offset(350f, 100f)
        )

        assertEquals(MatchEngine.RestartType.GOAL_KICK, engine.restartType)
        assertEquals(com.example.openswos.model.TeamSide.AWAY, engine.restartSide)
        assertTrue("playerHasBall should be false on Away restart", !engine.playerHasBall)

        // Tick past restart delay
        engine.restartDelay = 0.05f
        engine.tick(0.06f)

        // CPU must auto-execute the goal kick
        assertEquals("CPU should have executed goal kick and cleared restartType", null, engine.restartType)
        assertTrue("Ball outOfBoundsCooldown active", engine.outOfBoundsCooldown > 0f)
        assertEquals(com.example.openswos.model.TeamSide.HOME, engine.controlledPlayer?.side)
    }

    @Test
    fun testRestartInputViaTackleOrShootDoesNotGetStuck() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)
        engine.period = MatchPeriod.FIRST_HALF

        // 1. Throw-in triggered
        engine.triggerRestart(
            MatchEngine.RestartType.THROW_IN,
            com.example.openswos.model.TeamSide.HOME,
            androidx.compose.ui.geometry.Offset(50f, 600f)
        )
        assertEquals(MatchEngine.RestartType.THROW_IN, engine.restartType)

        // Simulate user pressing tackle button while restart is active
        engine.isTacklePressed = true
        engine.tick(0.016f)

        // Must execute throw-in immediately instead of getting stuck or dropping the input
        assertEquals("Throw-in should execute on tackle input during restart", null, engine.restartType)
        assertTrue(!engine.isTacklePressed)

        // 2. Goal kick triggered
        engine.outOfBoundsCooldown = 0f
        engine.triggerRestart(
            MatchEngine.RestartType.GOAL_KICK,
            com.example.openswos.model.TeamSide.HOME,
            androidx.compose.ui.geometry.Offset(350f, 1200f)
        )
        assertEquals(MatchEngine.RestartType.GOAL_KICK, engine.restartType)

        // Simulate user holding shoot button
        engine.isShootHeld = true
        engine.tick(0.016f)

        // Must execute goal kick immediately
        assertEquals("Goal kick should execute on shoot held during restart", null, engine.restartType)
        assertTrue(!engine.isShootHeld)
    }

    @Test
    fun testEndlineRulesInFirstAndSecondHalves() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val audio = RetroAudio(scope)
        audio.soundEnabled = false

        val settings = MatchSettings(
            homeTeam = TeamDatabase.brazil,
            awayTeam = TeamDatabase.england
        )
        val engine = MatchEngine(settings, audio)

        // FIRST HALF: Home attacks top, defends bottom. Away attacks bottom, defends top.
        engine.period = MatchPeriod.FIRST_HALF
        engine.restartType = null
        engine.outOfBoundsCooldown = 0f

        // Ball goes over top endline wide of goal (ball.x = 100f, goal is 280f..420f)
        // Home touched it last (Home shot wide) -> Goal kick for Away
        engine.ball.x = 100f
        engine.ball.y = 30f // above top margin (PITCH_MARGIN_Y is 60f)
        engine.ball.lastKicker = engine.homePlayers.first()
        engine.tick(0.016f)
        assertEquals("Attacking shot wide over top endline should be Away goal kick", MatchEngine.RestartType.GOAL_KICK, engine.restartType)
        assertEquals(com.example.openswos.model.TeamSide.AWAY, engine.restartSide)

        // SECOND HALF: Home defends top, attacks bottom. Away defends bottom, attacks top.
        engine.period = MatchPeriod.SECOND_HALF
        engine.restartType = null
        engine.outOfBoundsCooldown = 0f

        // Ball goes over top endline wide of goal, Away touched it last (Away shot wide) -> Goal kick for Home
        engine.ball.x = 100f
        engine.ball.y = 30f
        engine.ball.lastKicker = engine.awayPlayers.first()
        engine.tick(0.016f)
        assertEquals("Attacking shot wide in 2nd half over top endline should be Home goal kick", MatchEngine.RestartType.GOAL_KICK, engine.restartType)
        assertEquals(com.example.openswos.model.TeamSide.HOME, engine.restartSide)
    }

    @Test
    fun testPitchTypeVisualAndPhysicsProperties() {
        // Verify all 4 pitch types exist and have distinct visual identities
        val pitchTypes = PitchType.values()
        assertEquals(4, pitchTypes.size)

        // 1. Frozen / Winter pitch should have high visibility orange ball and lines
        val frozen = PitchType.FROZEN
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFF4500), frozen.lineColor)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFF6D00), frozen.ballColor)
        assertTrue("Frozen bounce factor should be high", frozen.bounceFactor > 0.8f)

        // 2. Muddy pitch should have mud ball and low bounce
        val mud = PitchType.WET_MUD
        assertTrue("Mud bounce factor should be low", mud.bounceFactor <= 0.45f)
        assertEquals("Heavy Rain", mud.weatherName)

        // 3. Astro pitch should have high bounce and synthetic colors
        val astro = PitchType.ASTRO
        assertTrue("Astro bounce factor should be high", astro.bounceFactor >= 0.75f)

        // 4. Grass pitch
        val grass = PitchType.GRASS
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFFFFF), grass.lineColor)
    }
}


