package com.example.openswos.model

import androidx.compose.ui.graphics.Color

enum class PitchType(
    val displayName: String,
    val description: String,
    val grassLight: Color,
    val grassDark: Color,
    val bounceFactor: Float,
    val frictionFactor: Float
) {
    GRASS(
        displayName = "Classic Turf",
        description = "Standard manicured green turf with balanced roll and grip",
        grassLight = Color(0xFF1E8238),
        grassDark = Color(0xFF166B2C),
        bounceFactor = 0.65f,
        frictionFactor = 0.985f
    ),
    WET_MUD(
        displayName = "Muddy / Rain",
        description = "Heavy pitch with sodden ground, lower bounce and longer slides",
        grassLight = Color(0xFF4A5D35),
        grassDark = Color(0xFF384627),
        bounceFactor = 0.45f,
        frictionFactor = 0.965f
    ),
    FROZEN(
        displayName = "Frozen / Winter",
        description = "Icy hard turf with lightning fast ball travel and long slides",
        grassLight = Color(0xFF5A8E7D),
        grassDark = Color(0xFF457465),
        bounceFactor = 0.82f,
        frictionFactor = 0.992f
    ),
    ASTRO(
        displayName = "Astro Turf",
        description = "Modern synthetic surface with high uniform bounce",
        grassLight = Color(0xFF1B8A44),
        grassDark = Color(0xFF137035),
        bounceFactor = 0.75f,
        frictionFactor = 0.982f
    )
}

enum class FormationType(val label: String) {
    F_4_4_2("4-4-2"),
    F_4_3_3("4-3-3"),
    F_3_5_2("3-5-2"),
    F_5_3_2("5-3-2"),
    F_4_2_4("4-2-4")
}

enum class PlayerPosition {
    GK, LB, CB1, CB2, RB, LM, CM1, CM2, RM, CF1, CF2
}

data class PlayerStats(
    val speed: Float,     // 4.0 .. 9.5
    val shooting: Float,  // 4.0 .. 9.5
    val passing: Float,   // 4.0 .. 9.5
    val tackling: Float,  // 4.0 .. 9.5
    val control: Float,   // 4.0 .. 9.5
    val heading: Float    // 4.0 .. 9.5
) {
    val overall: Int
        get() = ((speed + shooting + passing + tackling + control + heading) / 6f * 10f).toInt()
}

data class Player(
    val id: String,
    val name: String,
    val number: Int,
    val position: PlayerPosition,
    val stats: PlayerStats,
    val skinTone: Color = Color(0xFFFCD0A1),
    val hairColor: Color = Color(0xFF4A2E1B)
)

data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val country: String,
    val isClub: Boolean = false,
    val jerseyPrimary: Color,
    val jerseyStripe: Color? = null,
    val shortsColor: Color,
    val socksColor: Color,
    val awayJersey: Color,
    val formation: FormationType = FormationType.F_4_4_2,
    val roster: List<Player>
) {
    val overallRating: Int
        get() = if (roster.isNotEmpty()) roster.map { it.stats.overall }.average().toInt() else 75
}

enum class Difficulty(val label: String, val reactionDelay: Float, val shotAccuracy: Float) {
    AMATEUR("Amateur", 0.45f, 0.65f),
    SEMI_PRO("Semi-Pro", 0.25f, 0.82f),
    WORLD_CLASS("World Class", 0.12f, 0.94f)
}

enum class MatchPeriod {
    PRE_MATCH,
    FIRST_HALF,
    HALF_TIME,
    SECOND_HALF,
    EXTRA_TIME_1,
    EXTRA_TIME_2,
    PENALTY_SHOOTOUT,
    FULL_TIME
}

data class MatchSettings(
    val homeTeam: Team,
    val awayTeam: Team,
    val matchMinutes: Int = 3,
    val pitchType: PitchType = PitchType.GRASS,
    val difficulty: Difficulty = Difficulty.SEMI_PRO,
    val isClassicControls: Boolean = false, // true = single button SWOS, false = dual button Pass/Shoot
    val windStrength: Float = 0f
)

data class MatchStats(
    var homeGoals: Int = 0,
    var awayGoals: Int = 0,
    var homeShots: Int = 0,
    var awayShots: Int = 0,
    var homeShotsOnTarget: Int = 0,
    var awayShotsOnTarget: Int = 0,
    var homeCorners: Int = 0,
    var awayCorners: Int = 0,
    var homeFouls: Int = 0,
    var awayFouls: Int = 0,
    var homePossessionSeconds: Float = 0f,
    var awayPossessionSeconds: Float = 0f
) {
    val homePossessionPercent: Int
        get() {
            val total = homePossessionSeconds + awayPossessionSeconds
            return if (total > 0f) ((homePossessionSeconds / total) * 100f).toInt() else 50
        }
    val awayPossessionPercent: Int
        get() = 100 - homePossessionPercent
}

enum class TeamSide {
    HOME, AWAY
}

sealed class MatchEvent(val message: String, val side: TeamSide? = null) {
    class Goal(val scorerName: String, side: TeamSide) : MatchEvent("GOAL! $scorerName", side)
    class Foul(val player: String, side: TeamSide) : MatchEvent("FOUL!", side)
    class YellowCard(val player: String, side: TeamSide) : MatchEvent("YELLOW CARD", side)
    class RedCard(val player: String, side: TeamSide) : MatchEvent("RED CARD!", side)
    class Offside(side: TeamSide) : MatchEvent("OFFSIDE", side)
    class Corner(side: TeamSide) : MatchEvent("CORNER KICK", side)
    class GoalKick(side: TeamSide) : MatchEvent("GOAL KICK", side)
    class ThrowIn(side: TeamSide) : MatchEvent("THROW-IN", side)
    class Penalty(side: TeamSide) : MatchEvent("PENALTY KICK!", side)
    object HalfTime : MatchEvent("HALF TIME")
    object FullTime : MatchEvent("FULL TIME")
}
