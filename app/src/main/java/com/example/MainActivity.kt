package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.openswos.ui.MainMenuScreen
import com.example.openswos.ui.MatchScreen
import com.example.openswos.ui.PenaltyShootoutScreen
import com.example.openswos.ui.SettingsScreen
import com.example.openswos.ui.TacticsScreen
import com.example.openswos.ui.TeamSelectScreen
import com.example.openswos.ui.TournamentScreen
import com.example.openswos.viewmodel.OpenSwosViewModel
import com.example.openswos.viewmodel.Screen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RetroDarkBg

class MainActivity : ComponentActivity() {

    private val viewModel: OpenSwosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RetroDarkBg
                ) {
                    OpenSwosApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun OpenSwosApp(viewModel: OpenSwosViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeEngine by viewModel.activeMatchEngine.collectAsState()
    val managedTeam by viewModel.managedTeam.collectAsState()

    when (currentScreen) {
        Screen.MAIN_MENU -> {
            MainMenuScreen(
                onPlayQuickMatch = { viewModel.navigateTo(Screen.TEAM_SELECT) },
                onPlayTournament = { viewModel.navigateTo(Screen.TOURNAMENT) },
                onPlayPenalties = { viewModel.navigateTo(Screen.PENALTIES) },
                onOpenTactics = { viewModel.navigateTo(Screen.TACTICS) },
                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                audio = viewModel.audio
            )
        }

        Screen.TEAM_SELECT -> {
            TeamSelectScreen(
                onStartMatch = { settings ->
                    viewModel.startMatch(settings)
                },
                onBack = { viewModel.navigateTo(Screen.MAIN_MENU) },
                audio = viewModel.audio
            )
        }

        Screen.MATCH -> {
            val engine = activeEngine
            if (engine != null) {
                MatchScreen(
                    engine = engine,
                    onMatchEnd = { viewModel.endMatch() },
                    onOpenTactics = { viewModel.navigateTo(Screen.TACTICS) }
                )
            } else {
                viewModel.navigateTo(Screen.MAIN_MENU)
            }
        }

        Screen.TOURNAMENT -> {
            TournamentScreen(
                userTeam = managedTeam,
                audio = viewModel.audio,
                onPlayTournamentMatch = { settings ->
                    viewModel.startMatch(settings)
                },
                onBack = { viewModel.navigateTo(Screen.MAIN_MENU) }
            )
        }

        Screen.TACTICS -> {
            TacticsScreen(
                currentTeam = managedTeam,
                audio = viewModel.audio,
                onBack = {
                    if (activeEngine != null) {
                        viewModel.navigateTo(Screen.MATCH)
                    } else {
                        viewModel.navigateTo(Screen.MAIN_MENU)
                    }
                }
            )
        }

        Screen.PENALTIES -> {
            PenaltyShootoutScreen(
                audio = viewModel.audio,
                onBack = { viewModel.navigateTo(Screen.MAIN_MENU) }
            )
        }

        Screen.SETTINGS -> {
            SettingsScreen(
                audio = viewModel.audio,
                onBack = { viewModel.navigateTo(Screen.MAIN_MENU) }
            )
        }
    }
}
