package com.example.openswos.model

import androidx.compose.ui.graphics.Color

enum class PitchType(
    val displayName: String,
    val description: String,
    val grassLight: Color,
    val grassDark: Color,
    val lineColor: Color = Color.White,
    val apronColor: Color = Color(0xFF145423),
    val ballColor: Color = Color(0xFFFBFBFB),
    val ballTrimColor: Color = Color(0xFF1B262C),
    val radarBgColor: Color = Color(0xCC145A27),
    val radarLineColor: Color = Color(0x88FFFFFF),
    val bounceFactor: Float,
    val frictionFactor: Float,
    val weatherName: String = "Clear",
    val badgeEmoji: String = "🌱"
) {
    GRASS(
        displayName = "Classic Turf",
        description = "Manicured English turf with rich lawn stripes and balanced grip",
        grassLight = Color(0xFF2E8B3A),
        grassDark = Color(0xFF23752D),
        lineColor = Color(0xFFFFFFFF),
        apronColor = Color(0xFF145423),
        ballColor = Color(0xFFFBFBFB),
        ballTrimColor = Color(0xFF1B262C),
        radarBgColor = Color(0xCC145A27),
        radarLineColor = Color(0x88FFFFFF),
        bounceFactor = 0.65f,
        frictionFactor = 0.985f,
        weatherName = "Fair / Clear",
        badgeEmoji = "🌱"
    ),
    WET_MUD(
        displayName = "Muddy / Rain",
        description = "Sodden muddy pitch with goalmouth churn, water puddles and pouring rain",
        grassLight = Color(0xFF5A442E),
        grassDark = Color(0xFF483623),
        lineColor = Color(0xFFD6CEB2),
        apronColor = Color(0xFF38291A),
        ballColor = Color(0xFFEBE5D8),
        ballTrimColor = Color(0xFF322416),
        radarBgColor = Color(0xCC463321),
        radarLineColor = Color(0x88D6CEB2),
        bounceFactor = 0.40f,
        frictionFactor = 0.955f,
        weatherName = "Heavy Rain",
        badgeEmoji = "🌧️"
    ),
    FROZEN(
        displayName = "Frozen / Winter",
        description = "Snow-covered icy pitch with vivid orange ball & lines, slick ice and snowfall",
        grassLight = Color(0xFFF0F6F9),
        grassDark = Color(0xFFD8E9F2),
        lineColor = Color(0xFFFF4500),
        apronColor = Color(0xFFB8CDD8),
        ballColor = Color(0xFFFF6D00),
        ballTrimColor = Color(0xFF0D0D0D),
        radarBgColor = Color(0xCCE6EFF5),
        radarLineColor = Color(0xCCFF4500),
        bounceFactor = 0.85f,
        frictionFactor = 0.994f,
        weatherName = "Blizzard",
        badgeEmoji = "❄️"
    ),
    ASTRO(
        displayName = "Astro Turf",
        description = "Electric synthetic carpet with woven grid tiles, rubber infill and high bounce",
        grassLight = Color(0xFF10BA54),
        grassDark = Color(0xFF099E44),
        lineColor = Color(0xFFFFFFFF),
        apronColor = Color(0xFF07481E),
        ballColor = Color(0xFFFFFFFF),
        ballTrimColor = Color(0xFF052B14),
        radarBgColor = Color(0xCC0C6B2F),
        radarLineColor = Color(0x88FFFFFF),
        bounceFactor = 0.78f,
        frictionFactor = 0.980f,
        weatherName = "Dry / Fast",
        badgeEmoji = "⚡"
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
