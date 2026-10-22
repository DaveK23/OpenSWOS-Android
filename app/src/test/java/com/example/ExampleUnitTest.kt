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
}

