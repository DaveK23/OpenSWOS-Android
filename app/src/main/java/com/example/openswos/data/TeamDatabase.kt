package com.example.openswos.data

import androidx.compose.ui.graphics.Color
import com.example.openswos.model.FormationType
import com.example.openswos.model.Player
import com.example.openswos.model.PlayerPosition
import com.example.openswos.model.PlayerStats
import com.example.openswos.model.Team

object TeamDatabase {

    private fun createPlayer(
        id: String,
        name: String,
        number: Int,
        pos: PlayerPosition,
        spd: Float,
        sht: Float,
        pas: Float,
        tck: Float,
        ctl: Float,
        hed: Float,
        skin: Color = Color(0xFFF7D0B2),
        hair: Color = Color(0xFF2B1D14)
    ) = Player(
        id = id,
        name = name,
        number = number,
        position = pos,
        stats = PlayerStats(spd, sht, pas, tck, ctl, hed),
        skinTone = skin,
        hairColor = hair
    )

    // BRAZIL
    val brazil = Team(
        id = "bra",
        name = "Brazil",
        shortName = "BRA",
        country = "Brazil",
        isClub = false,
        jerseyPrimary = Color(0xFFFFEB3B), // Iconic Canary Yellow
        jerseyStripe = Color(0xFF009B3A),
        shortsColor = Color(0xFF002776),  // Royal Blue shorts
        socksColor = Color(0xFFFFFFFF),   // White socks
        awayJersey = Color(0xFF002776),
        formation = FormationType.F_4_3_3,
        roster = listOf(
            createPlayer("bra_1", "Taffarel", 1, PlayerPosition.GK, 6.2f, 3.0f, 6.5f, 5.0f, 6.0f, 6.0f, Color(0xFFECC29C), Color(0xFF2E1C0C)),
            createPlayer("bra_2", "Cafu", 2, PlayerPosition.RB, 8.8f, 6.8f, 7.8f, 8.2f, 8.4f, 7.0f, Color(0xFF7A4A28), Color(0xFF1A1A1A)),
            createPlayer("bra_3", "Aldair", 3, PlayerPosition.CB1, 6.8f, 5.0f, 7.2f, 8.8f, 7.5f, 8.5f, Color(0xFF5E391F), Color(0xFF101010)),
            createPlayer("bra_4", "Lucio", 4, PlayerPosition.CB2, 7.4f, 6.2f, 7.0f, 8.9f, 7.6f, 8.9f, Color(0xFFB88556), Color(0xFF1A1A1A)),
            createPlayer("bra_6", "R. Carlos", 6, PlayerPosition.LB, 9.4f, 9.5f, 7.9f, 8.2f, 8.6f, 6.8f, Color(0xFF8C5832), Color(0xFF101010)),
            createPlayer("bra_5", "Dunga", 5, PlayerPosition.LM, 6.9f, 7.0f, 8.6f, 8.7f, 7.8f, 7.4f, Color(0xFFECC29C), Color(0xFF4A3425)),
            createPlayer("bra_8", "Rivaldo", 8, PlayerPosition.CM1, 8.2f, 9.2f, 9.0f, 6.5f, 9.4f, 8.2f, Color(0xFF7D4E2C), Color(0xFF101010)),
            createPlayer("bra_10", "Ronaldinho", 10, PlayerPosition.CM2, 9.2f, 9.1f, 9.5f, 6.0f, 9.8f, 7.5f, Color(0xFF7D4E2C), Color(0xFF101010)),
            createPlayer("bra_7", "Bebeto", 7, PlayerPosition.RM, 8.4f, 8.8f, 8.2f, 5.5f, 8.8f, 7.8f, Color(0xFFD6A074), Color(0xFF20150E)),
            createPlayer("bra_9", "Ronaldo", 9, PlayerPosition.CF1, 9.6f, 9.6f, 8.4f, 5.0f, 9.7f, 8.5f, Color(0xFF9E6842), Color(0xFF151515)),
            createPlayer("bra_11", "Romario", 11, PlayerPosition.CF2, 8.9f, 9.7f, 8.2f, 4.8f, 9.6f, 8.0f, Color(0xFF825232), Color(0xFF151515))
        )
    )

    // ENGLAND
    val england = Team(
        id = "eng",
        name = "England",
        shortName = "ENG",
        country = "England",
        isClub = false,
        jerseyPrimary = Color(0xFFFFFFFF),
        jerseyStripe = Color(0xFFCE1124),
        shortsColor = Color(0xFF00247D),
        socksColor = Color(0xFFFFFFFF),
        awayJersey = Color(0xFFCE1124),
        formation = FormationType.F_4_4_2,
        roster = listOf(
            createPlayer("eng_1", "Seaman", 1, PlayerPosition.GK, 6.0f, 2.5f, 6.2f, 5.0f, 6.0f, 6.5f, Color(0xFFF7D2B7), Color(0xFF241C16)),
            createPlayer("eng_2", "G. Neville", 2, PlayerPosition.RB, 7.8f, 5.5f, 7.6f, 8.4f, 7.5f, 7.2f, Color(0xFFF7D2B7), Color(0xFF5A432D)),
            createPlayer("eng_5", "Adams", 5, PlayerPosition.CB1, 6.5f, 5.2f, 6.8f, 9.2f, 7.0f, 9.2f, Color(0xFFF7D2B7), Color(0xFF382A1D)),
            createPlayer("eng_6", "Campbell", 6, PlayerPosition.CB2, 7.8f, 4.8f, 6.6f, 9.3f, 7.2f, 9.4f, Color(0xFF593822), Color(0xFF111111)),
            createPlayer("eng_3", "Pearce", 3, PlayerPosition.LB, 7.6f, 8.5f, 7.4f, 8.9f, 7.2f, 7.8f, Color(0xFFF7D2B7), Color(0xFF302014)),
            createPlayer("eng_7", "Beckham", 7, PlayerPosition.RM, 8.0f, 9.0f, 9.6f, 7.2f, 8.9f, 6.9f, Color(0xFFFCE1C8), Color(0xFF8A6B3A)),
            createPlayer("eng_4", "Ince", 4, PlayerPosition.CM1, 7.7f, 7.0f, 8.0f, 8.9f, 7.8f, 8.0f, Color(0xFF5C3C24), Color(0xFF101010)),
            createPlayer("eng_8", "Gascoigne", 8, PlayerPosition.CM2, 8.2f, 8.8f, 9.2f, 7.0f, 9.4f, 7.2f, Color(0xFFF9D5BD), Color(0xFF402E1E)),
            createPlayer("eng_11", "McManaman", 11, PlayerPosition.LM, 8.6f, 7.8f, 8.5f, 6.2f, 9.0f, 6.8f, Color(0xFFF9D5BD), Color(0xFF503A22)),
            createPlayer("eng_9", "Shearer", 9, PlayerPosition.CF1, 8.4f, 9.6f, 7.8f, 6.0f, 8.7f, 9.6f, Color(0xFFF9D5BD), Color(0xFF322316)),
            createPlayer("eng_10", "Owen", 10, PlayerPosition.CF2, 9.6f, 9.0f, 7.5f, 4.5f, 8.9f, 6.5f, Color(0xFFF9D5BD), Color(0xFF433020))
        )
    )

    // ARGENTINA
    val argentina = Team(
        id = "arg",
        name = "Argentina",
        shortName = "ARG",
        country = "Argentina",
        isClub = false,
        jerseyPrimary = Color(0xFF75AADB), // Sky Blue & White
        jerseyStripe = Color(0xFFFFFFFF),
        shortsColor = Color(0xFF1A1A1A),
        socksColor = Color(0xFFFFFFFF),
        awayJersey = Color(0xFF1A237E),
        formation = FormationType.F_3_5_2,
        roster = listOf(
            createPlayer("arg_1", "Roa", 1, PlayerPosition.GK, 6.3f, 2.5f, 6.0f, 5.0f, 6.2f, 6.8f, Color(0xFFF0CCA8), Color(0xFF2A1C12)),
            createPlayer("arg_2", "Ayala", 2, PlayerPosition.LB, 7.4f, 5.0f, 7.2f, 9.1f, 7.5f, 9.3f, Color(0xFFECC29C), Color(0xFF20150E)),
            createPlayer("arg_3", "Chamot", 3, PlayerPosition.CB1, 7.0f, 4.8f, 7.0f, 8.5f, 7.2f, 8.2f, Color(0xFFF0CCA8), Color(0xFF332014)),
            createPlayer("arg_4", "Sensini", 4, PlayerPosition.CB2, 7.2f, 5.2f, 7.5f, 8.6f, 7.4f, 8.0f, Color(0xFFECC29C), Color(0xFF302015)),
            createPlayer("arg_8", "Zanetti", 8, PlayerPosition.RB, 8.9f, 7.0f, 8.4f, 8.8f, 8.6f, 7.4f, Color(0xFFECC29C), Color(0xFF1E140C)),
            createPlayer("arg_5", "Redondo", 5, PlayerPosition.CM1, 7.8f, 7.6f, 9.4f, 8.7f, 9.2f, 8.0f, Color(0xFFF6D4B8), Color(0xFF473120)),
            createPlayer("arg_11", "Veron", 11, PlayerPosition.CM2, 8.1f, 9.1f, 9.5f, 7.8f, 9.0f, 7.6f, Color(0xFFECC29C), Color(0xFF1C130D)),
            createPlayer("arg_10", "Maradona", 10, PlayerPosition.LM, 8.8f, 9.8f, 9.9f, 6.0f, 9.9f, 7.8f, Color(0xFFECC29C), Color(0xFF15100B)),
            createPlayer("arg_7", "Ortega", 7, PlayerPosition.RM, 8.8f, 8.2f, 8.6f, 5.5f, 9.5f, 6.2f, Color(0xFFECC29C), Color(0xFF15100B)),
            createPlayer("arg_9", "Batistuta", 9, PlayerPosition.CF1, 8.7f, 9.8f, 7.6f, 5.5f, 8.9f, 9.3f, Color(0xFFF6D4B8), Color(0xFF4C3322)),
            createPlayer("arg_19", "Crespo", 19, PlayerPosition.CF2, 8.5f, 9.2f, 7.5f, 5.0f, 8.6f, 9.2f, Color(0xFFECC29C), Color(0xFF261910))
        )
    )

    // GERMANY
    val germany = Team(
        id = "ger",
        name = "Germany",
        shortName = "GER",
        country = "Germany",
        isClub = false,
        jerseyPrimary = Color(0xFFFFFFFF),
        jerseyStripe = Color(0xFF000000),
        shortsColor = Color(0xFF000000),
        socksColor = Color(0xFFFFFFFF),
        awayJersey = Color(0xFF00897B),
        formation = FormationType.F_3_5_2,
        roster = listOf(
            createPlayer("ger_1", "Kahn", 1, PlayerPosition.GK, 6.5f, 3.0f, 6.8f, 5.5f, 6.8f, 7.0f, Color(0xFFFCE3CC), Color(0xFF876A3D)),
            createPlayer("ger_2", "Worns", 2, PlayerPosition.LB, 7.2f, 5.0f, 7.0f, 8.6f, 7.2f, 8.4f, Color(0xFFFCE3CC), Color(0xFF402E1C)),
            createPlayer("ger_6", "Sammer", 6, PlayerPosition.CB1, 8.0f, 7.8f, 8.8f, 9.4f, 8.6f, 8.8f, Color(0xFFFCE3CC), Color(0xFF8A3B24)),
            createPlayer("ger_5", "Kohler", 5, PlayerPosition.CB2, 7.0f, 4.5f, 6.9f, 9.2f, 7.0f, 9.0f, Color(0xFFFCE3CC), Color(0xFF2E2014)),
            createPlayer("ger_3", "Brehme", 3, PlayerPosition.RB, 7.8f, 8.8f, 8.6f, 8.3f, 8.2f, 7.2f, Color(0xFFFCE3CC), Color(0xFF7E6038)),
            createPlayer("ger_8", "Matthaus", 8, PlayerPosition.CM1, 8.4f, 9.3f, 9.2f, 9.0f, 8.9f, 8.2f, Color(0xFFFCE3CC), Color(0xFF4B3520)),
            createPlayer("ger_10", "Hassler", 10, PlayerPosition.CM2, 8.4f, 8.4f, 9.0f, 6.5f, 9.3f, 6.0f, Color(0xFFFCE3CC), Color(0xFF8A6C40)),
            createPlayer("ger_7", "Moller", 7, PlayerPosition.LM, 8.2f, 8.6f, 8.8f, 6.8f, 9.0f, 7.4f, Color(0xFFFCE3CC), Color(0xFF382819)),
            createPlayer("ger_16", "Ziege", 16, PlayerPosition.RM, 8.2f, 8.0f, 8.4f, 7.8f, 8.0f, 7.2f, Color(0xFFFCE3CC), Color(0xFF2B1D12)),
            createPlayer("ger_18", "Klinsmann", 18, PlayerPosition.CF1, 8.6f, 9.3f, 7.8f, 6.0f, 8.8f, 9.5f, Color(0xFFFCE3CC), Color(0xFF917342)),
            createPlayer("ger_20", "Bierhoff", 20, PlayerPosition.CF2, 7.8f, 8.9f, 7.2f, 5.5f, 8.2f, 9.9f, Color(0xFFFCE3CC), Color(0xFF3F2D1C))
        )
    )

    // ITALY
    val italy = Team(
        id = "ita",
        name = "Italy",
        shortName = "ITA",
        country = "Italy",
        isClub = false,
        jerseyPrimary = Color(0xFF0038A8), // Azzurri Blue
        jerseyStripe = null,
        shortsColor = Color(0xFFFFFFFF),
        socksColor = Color(0xFF0038A8),
        awayJersey = Color(0xFFFFFFFF),
        formation = FormationType.F_4_4_2,
        roster = listOf(
            createPlayer("ita_1", "Pagliuca", 1, PlayerPosition.GK, 6.4f, 2.5f, 6.2f, 5.0f, 6.4f, 6.5f, Color(0xFFF0C8A4), Color(0xFF21150D)),
            createPlayer("ita_2", "Panucci", 2, PlayerPosition.RB, 7.8f, 6.0f, 7.5f, 8.6f, 7.8f, 8.0f, Color(0xFFECC29C), Color(0xFF26180E)),
            createPlayer("ita_5", "Costacurta", 5, PlayerPosition.CB1, 7.0f, 5.0f, 7.6f, 9.1f, 7.8f, 8.6f, Color(0xFFF0C8A4), Color(0xFF382518)),
            createPlayer("ita_6", "Baresi", 6, PlayerPosition.CB2, 7.6f, 6.0f, 8.5f, 9.8f, 8.8f, 8.9f, Color(0xFFECC29C), Color(0xFF28190F)),
            createPlayer("ita_3", "Maldini", 3, PlayerPosition.LB, 8.8f, 7.2f, 8.5f, 9.8f, 9.0f, 9.0f, Color(0xFFF0C8A4), Color(0xFF3A2819)),
            createPlayer("ita_14", "Di Biagio", 14, PlayerPosition.LM, 7.5f, 7.5f, 8.0f, 8.8f, 7.6f, 8.2f, Color(0xFFECC29C), Color(0xFF1E130B)),
            createPlayer("ita_4", "Albertini", 4, PlayerPosition.CM1, 7.4f, 8.4f, 9.3f, 8.4f, 8.9f, 7.5f, Color(0xFFF0C8A4), Color(0xFF2B1C11)),
            createPlayer("ita_11", "D. Baggio", 11, PlayerPosition.CM2, 7.8f, 8.2f, 8.4f, 8.6f, 8.3f, 8.6f, Color(0xFFECC29C), Color(0xFF26190F)),
            createPlayer("ita_7", "Donadoni", 7, PlayerPosition.RM, 8.2f, 7.8f, 8.9f, 7.0f, 9.1f, 7.0f, Color(0xFFF0C8A4), Color(0xFF24170E)),
            createPlayer("ita_10", "R. Baggio", 10, PlayerPosition.CF1, 8.8f, 9.6f, 9.6f, 5.5f, 9.9f, 7.6f, Color(0xFFECC29C), Color(0xFF24170E)),
            createPlayer("ita_9", "Vieri", 9, PlayerPosition.CF2, 8.5f, 9.7f, 7.4f, 5.8f, 8.6f, 9.7f, Color(0xFFECC29C), Color(0xFF22160E))
        )
    )

    // FRANCE
    val france = Team(
        id = "fra",
        name = "France",
        shortName = "FRA",
        country = "France",
        isClub = false,
        jerseyPrimary = Color(0xFF002395), // French Blue
        jerseyStripe = Color(0xFFED2939),
        shortsColor = Color(0xFFFFFFFF),
        socksColor = Color(0xFFED2939),
        awayJersey = Color(0xFFFFFFFF),
        formation = FormationType.F_4_3_3,
        roster = listOf(
            createPlayer("fra_16", "Barthez", 16, PlayerPosition.GK, 6.8f, 3.0f, 7.0f, 5.0f, 6.8f, 6.5f, Color(0xFFF5CEAA), Color(0xFF1E130B)),
            createPlayer("fra_15", "Thuram", 15, PlayerPosition.RB, 8.9f, 6.2f, 7.8f, 9.4f, 8.2f, 8.8f, Color(0xFF4C2F19), Color(0xFF101010)),
            createPlayer("fra_8", "Desailly", 8, PlayerPosition.CB1, 8.2f, 6.0f, 7.6f, 9.6f, 8.0f, 9.3f, Color(0xFF422814), Color(0xFF0A0A0A)),
            createPlayer("fra_5", "Blanc", 5, PlayerPosition.CB2, 7.0f, 7.0f, 8.4f, 9.3f, 8.4f, 9.4f, Color(0xFFF5CEAA), Color(0xFF382517)),
            createPlayer("fra_3", "Lizarazu", 3, PlayerPosition.LB, 8.7f, 6.8f, 8.2f, 8.8f, 8.4f, 7.0f, Color(0xFFF5CEAA), Color(0xFF26190F)),
            createPlayer("fra_7", "Deschamps", 7, PlayerPosition.LM, 7.6f, 6.8f, 8.6f, 9.1f, 8.2f, 7.5f, Color(0xFFF5CEAA), Color(0xFF3B2819)),
            createPlayer("fra_17", "Petit", 17, PlayerPosition.CM1, 7.8f, 8.5f, 8.6f, 8.7f, 8.4f, 8.2f, Color(0xFFFCE1C8), Color(0xFF8F7142)),
            createPlayer("fra_10", "Zidane", 10, PlayerPosition.CM2, 8.5f, 9.2f, 9.9f, 7.2f, 9.9f, 9.0f, Color(0xFFECC29C), Color(0xFF26180E)),
            createPlayer("fra_6", "Djorkaeff", 6, PlayerPosition.RM, 8.4f, 8.8f, 8.9f, 6.4f, 9.1f, 7.8f, Color(0xFFF5CEAA), Color(0xFF24170E)),
            createPlayer("fra_12", "Henry", 12, PlayerPosition.CF1, 9.6f, 9.3f, 8.4f, 5.0f, 9.4f, 8.0f, Color(0xFF4A2D17), Color(0xFF101010)),
            createPlayer("fra_9", "Guivarc'h", 9, PlayerPosition.CF2, 7.8f, 8.2f, 7.4f, 5.2f, 7.8f, 8.5f, Color(0xFFF5CEAA), Color(0xFF352316))
        )
    )

    // NETHERLANDS
    val netherlands = Team(
        id = "ned",
        name = "Netherlands",
        shortName = "NED",
        country = "Netherlands",
        isClub = false,
        jerseyPrimary = Color(0xFFFF5F00), // Oranje
        jerseyStripe = null,
        shortsColor = Color(0xFFFFFFFF),
        socksColor = Color(0xFFFF5F00),
        awayJersey = Color(0xFF002776),
        formation = FormationType.F_4_3_3,
        roster = listOf(
            createPlayer("ned_1", "Van der Sar", 1, PlayerPosition.GK, 6.5f, 3.0f, 7.5f, 5.0f, 7.0f, 7.2f, Color(0xFFFCE1C8), Color(0xFF4C3825)),
            createPlayer("ned_2", "Reiziger", 2, PlayerPosition.RB, 8.5f, 5.5f, 7.5f, 8.4f, 7.9f, 7.5f, Color(0xFF6B4326), Color(0xFF101010)),
            createPlayer("ned_3", "Stam", 3, PlayerPosition.CB1, 8.0f, 5.5f, 7.4f, 9.7f, 7.8f, 9.6f, Color(0xFFFCE1C8), Color(0xFF2E2014)),
            createPlayer("ned_4", "F. de Boer", 4, PlayerPosition.CB2, 7.2f, 8.4f, 9.4f, 9.0f, 8.6f, 8.8f, Color(0xFFFCE1C8), Color(0xFF573E26)),
            createPlayer("ned_5", "Numan", 5, PlayerPosition.LB, 8.0f, 6.8f, 7.8f, 8.5f, 8.0f, 7.4f, Color(0xFFFCE1C8), Color(0xFF3F2B1A)),
            createPlayer("ned_16", "Davids", 16, PlayerPosition.LM, 8.8f, 8.2f, 8.5f, 9.4f, 9.0f, 7.2f, Color(0xFF5E391F), Color(0xFF111111)),
            createPlayer("ned_7", "R. de Boer", 7, PlayerPosition.CM1, 8.2f, 8.5f, 8.9f, 7.8f, 8.8f, 8.2f, Color(0xFFFCE1C8), Color(0xFF573E26)),
            createPlayer("ned_11", "Cocu", 11, PlayerPosition.CM2, 8.0f, 8.4f, 8.8f, 8.4f, 8.6f, 8.4f, Color(0xFFFCE1C8), Color(0xFF3B2A1B)),
            createPlayer("ned_14", "Overmars", 14, PlayerPosition.RM, 9.5f, 8.4f, 8.6f, 5.8f, 9.2f, 6.4f, Color(0xFFFCE1C8), Color(0xFF3B2A1B)),
            createPlayer("ned_8", "Bergkamp", 8, PlayerPosition.CF1, 8.6f, 9.5f, 9.6f, 6.0f, 9.8f, 8.4f, Color(0xFFFCE1C8), Color(0xFF6B4E30)),
            createPlayer("ned_9", "Kluivert", 9, PlayerPosition.CF2, 8.9f, 9.4f, 8.2f, 5.5f, 9.2f, 9.5f, Color(0xFF603C21), Color(0xFF121212))
        )
    )

    // SPAIN
    val spain = Team(
        id = "esp",
        name = "Spain",
        shortName = "ESP",
        country = "Spain",
        isClub = false,
        jerseyPrimary = Color(0xFFC60B1E), // La Roja
        jerseyStripe = Color(0xFFFFC400),
        shortsColor = Color(0xFF00247D),
        socksColor = Color(0xFF000000),
        awayJersey = Color(0xFFFFFFFF),
        formation = FormationType.F_4_4_2,
        roster = listOf(
            createPlayer("esp_1", "Zubizarreta", 1, PlayerPosition.GK, 6.0f, 2.0f, 6.4f, 5.0f, 6.0f, 6.2f, Color(0xFFECC29C), Color(0xFF2E1C0C)),
            createPlayer("esp_2", "Ferrer", 2, PlayerPosition.RB, 8.2f, 5.5f, 7.6f, 8.5f, 7.8f, 7.0f, Color(0xFFECC29C), Color(0xFF20150E)),
            createPlayer("esp_4", "Hierro", 4, PlayerPosition.CB1, 7.4f, 9.0f, 8.9f, 9.2f, 8.5f, 9.3f, Color(0xFFECC29C), Color(0xFF382316)),
            createPlayer("esp_5", "Nadal", 5, PlayerPosition.CB2, 7.2f, 5.8f, 7.5f, 8.9f, 7.6f, 8.8f, Color(0xFFECC29C), Color(0xFF302015)),
            createPlayer("esp_3", "Sergi", 3, PlayerPosition.LB, 8.5f, 6.2f, 8.0f, 8.4f, 8.2f, 7.2f, Color(0xFFECC29C), Color(0xFF28190F)),
            createPlayer("esp_6", "Guardiola", 6, PlayerPosition.LM, 7.2f, 7.2f, 9.6f, 8.6f, 9.2f, 7.5f, Color(0xFFECC29C), Color(0xFF2A1C12)),
            createPlayer("esp_8", "Luis Enrique", 8, PlayerPosition.CM1, 8.4f, 8.6f, 8.6f, 8.2f, 8.8f, 8.4f, Color(0xFFECC29C), Color(0xFF352215)),
            createPlayer("esp_21", "Valeron", 21, PlayerPosition.CM2, 7.8f, 8.0f, 9.4f, 6.5f, 9.5f, 7.0f, Color(0xFFECC29C), Color(0xFF20150E)),
            createPlayer("esp_11", "Alfonso", 11, PlayerPosition.RM, 8.4f, 8.4f, 8.0f, 5.8f, 8.6f, 8.0f, Color(0xFFECC29C), Color(0xFF24170E)),
            createPlayer("esp_7", "Raul", 7, PlayerPosition.CF1, 8.9f, 9.4f, 8.9f, 6.2f, 9.5f, 8.8f, Color(0xFFECC29C), Color(0xFF22160E)),
            createPlayer("esp_9", "Morientes", 9, PlayerPosition.CF2, 8.4f, 9.2f, 7.8f, 5.5f, 8.5f, 9.6f, Color(0xFFECC29C), Color(0xFF322115))
        )
    )

    // CLASSIC CLUB: MANCHESTER RED
    val manchesterRed = Team(
        id = "mun",
        name = "Manchester Red",
        shortName = "MAN",
        country = "England",
        isClub = true,
        jerseyPrimary = Color(0xFFDA291C),
        jerseyStripe = Color(0xFFFFFFFF),
        shortsColor = Color(0xFFFFFFFF),
        socksColor = Color(0xFF000000),
        awayJersey = Color(0xFF000000),
        formation = FormationType.F_4_4_2,
        roster = listOf(
            createPlayer("mun_1", "Schmeichel", 1, PlayerPosition.GK, 6.8f, 3.2f, 7.0f, 5.5f, 6.8f, 7.5f, Color(0xFFFCE1C8), Color(0xFF7B5E38)),
            createPlayer("mun_2", "G. Neville", 2, PlayerPosition.RB, 7.8f, 5.6f, 7.7f, 8.5f, 7.6f, 7.2f, Color(0xFFFCE1C8), Color(0xFF4C3724)),
            createPlayer("mun_6", "Stam", 6, PlayerPosition.CB1, 8.0f, 5.6f, 7.5f, 9.6f, 7.9f, 9.6f, Color(0xFFFCE1C8), Color(0xFF2A1C12)),
            createPlayer("mun_5", "Johnsen", 5, PlayerPosition.CB2, 7.4f, 5.0f, 7.2f, 8.8f, 7.5f, 8.6f, Color(0xFFFCE1C8), Color(0xFF5A432D)),
            createPlayer("mun_3", "Irwin", 3, PlayerPosition.LB, 8.0f, 8.2f, 8.4f, 8.6f, 8.2f, 7.4f, Color(0xFFFCE1C8), Color(0xFF382618)),
            createPlayer("mun_7", "Beckham", 7, PlayerPosition.RM, 8.2f, 9.2f, 9.7f, 7.4f, 9.0f, 7.0f, Color(0xFFFCE1C8), Color(0xFF8A6B3A)),
            createPlayer("mun_16", "Keane", 16, PlayerPosition.CM1, 8.2f, 8.2f, 8.8f, 9.6f, 8.6f, 8.4f, Color(0xFFFCE1C8), Color(0xFF332216)),
            createPlayer("mun_18", "Scholes", 18, PlayerPosition.CM2, 8.0f, 9.5f, 9.6f, 8.0f, 9.3f, 7.5f, Color(0xFFFCE1C8), Color(0xFF8C3E1F)),
            createPlayer("mun_11", "Giggs", 11, PlayerPosition.LM, 9.5f, 8.5f, 8.9f, 6.5f, 9.5f, 7.2f, Color(0xFFFCE1C8), Color(0xFF1F140D)),
            createPlayer("mun_9", "Cole", 9, PlayerPosition.CF1, 9.1f, 9.3f, 7.8f, 5.2f, 8.9f, 8.5f, Color(0xFF5E391F), Color(0xFF101010)),
            createPlayer("mun_19", "Yorke", 19, PlayerPosition.CF2, 8.8f, 9.3f, 8.2f, 5.4f, 9.2f, 8.8f, Color(0xFF57341B), Color(0xFF101010))
        )
    )

    // CLASSIC CLUB: MADRID WHITE
    val madridWhite = Team(
        id = "rma",
        name = "Madrid White",
        shortName = "MAD",
        country = "Spain",
        isClub = true,
        jerseyPrimary = Color(0xFFFFFFFF),
        jerseyStripe = Color(0xFF4A148C), // Royal Purple trim
        shortsColor = Color(0xFFFFFFFF),
        socksColor = Color(0xFFFFFFFF),
        awayJersey = Color(0xFF1A1A1A),
        formation = FormationType.F_4_3_3,
        roster = listOf(
            createPlayer("rma_1", "Casillas", 1, PlayerPosition.GK, 7.0f, 2.5f, 6.8f, 5.0f, 6.6f, 6.6f, Color(0xFFECC29C), Color(0xFF24170E)),
            createPlayer("rma_2", "Salgado", 2, PlayerPosition.RB, 8.4f, 6.0f, 7.6f, 8.8f, 8.0f, 7.4f, Color(0xFFECC29C), Color(0xFF5B422B)),
            createPlayer("rma_4", "Hierro", 4, PlayerPosition.CB1, 7.5f, 9.2f, 9.0f, 9.3f, 8.6f, 9.4f, Color(0xFFECC29C), Color(0xFF382316)),
            createPlayer("rma_18", "Karanka", 18, PlayerPosition.CB2, 7.2f, 5.0f, 7.4f, 8.7f, 7.6f, 8.6f, Color(0xFFECC29C), Color(0xFF28190F)),
            createPlayer("rma_3", "R. Carlos", 3, PlayerPosition.LB, 9.5f, 9.6f, 8.2f, 8.4f, 8.8f, 7.0f, Color(0xFF8C5832), Color(0xFF101010)),
            createPlayer("rma_6", "Redondo", 6, PlayerPosition.LM, 8.0f, 7.8f, 9.5f, 8.9f, 9.4f, 8.2f, Color(0xFFF6D4B8), Color(0xFF473120)),
            createPlayer("rma_5", "Zidane", 5, PlayerPosition.CM1, 8.6f, 9.3f, 9.9f, 7.4f, 9.9f, 9.1f, Color(0xFFECC29C), Color(0xFF26180E)),
            createPlayer("rma_10", "Figo", 10, PlayerPosition.CM2, 8.9f, 9.0f, 9.5f, 6.8f, 9.7f, 7.8f, Color(0xFFECC29C), Color(0xFF26180E)),
            createPlayer("rma_14", "Guti", 14, PlayerPosition.RM, 8.2f, 8.4f, 9.3f, 6.8f, 9.2f, 7.5f, Color(0xFFFCE1C8), Color(0xFF8A6B3A)),
            createPlayer("rma_7", "Raul", 7, PlayerPosition.CF1, 8.9f, 9.5f, 9.0f, 6.2f, 9.6f, 8.8f, Color(0xFFECC29C), Color(0xFF22160E)),
            createPlayer("rma_11", "Ronaldo", 11, PlayerPosition.CF2, 9.6f, 9.7f, 8.5f, 5.0f, 9.8f, 8.6f, Color(0xFF9E6842), Color(0xFF151515))
        )
    )

    // ALL TEAMS
    val allTeams: List<Team> = listOf(
        brazil, england, argentina, germany, italy, france, netherlands, spain, manchesterRed, madridWhite
    )

    fun getTeam(id: String): Team = allTeams.find { it.id == id } ?: brazil
}
