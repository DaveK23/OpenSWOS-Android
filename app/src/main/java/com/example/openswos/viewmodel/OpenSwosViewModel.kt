package com.example.openswos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openswos.audio.RetroAudio
import com.example.openswos.data.TeamDatabase
import com.example.openswos.engine.MatchEngine
import com.example.openswos.model.MatchSettings
import com.example.openswos.model.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Screen {
    MAIN_MENU,
    TEAM_SELECT,
    MATCH,
    TOURNAMENT,
    TACTICS,
    PENALTIES,
    SETTINGS
}

class OpenSwosViewModel : ViewModel() {

    val audio = RetroAudio(viewModelScope)

    private val _currentScreen = MutableStateFlow(Screen.MAIN_MENU)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeMatchEngine = MutableStateFlow<MatchEngine?>(null)
    val activeMatchEngine: StateFlow<MatchEngine?> = _activeMatchEngine.asStateFlow()

    private val _managedTeam = MutableStateFlow(TeamDatabase.brazil)
    val managedTeam: StateFlow<Team> = _managedTeam.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun startMatch(settings: MatchSettings) {
        _activeMatchEngine.value = MatchEngine(settings, audio)
        _currentScreen.value = Screen.MATCH
    }

    fun endMatch() {
        _activeMatchEngine.value?.isPaused = true
        _activeMatchEngine.value = null
        _currentScreen.value = Screen.MAIN_MENU
    }

    fun setManagedTeam(team: Team) {
        _managedTeam.value = team
    }
}
