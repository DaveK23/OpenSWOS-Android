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
}
