package com.example.myapplicationfocusflow.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationfocusflow.model.FocusSessionModel
import com.example.myapplicationfocusflow.model.SessionPhase
import com.example.myapplicationfocusflow.Repositories.FocusSessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusSessionViewModel(private val repository: FocusSessionRepository) : ViewModel() {

    private val _currentSession = MutableStateFlow<FocusSessionModel?>(null)
    val currentSession: StateFlow<FocusSessionModel?> = _currentSession.asStateFlow()

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _currentPhase = MutableStateFlow(SessionPhase.FOCUS)
    val currentPhase: StateFlow<SessionPhase> = _currentPhase.asStateFlow()

    private val _sessionCompleted = MutableStateFlow(false)
    val sessionCompleted: StateFlow<Boolean> = _sessionCompleted.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Track completed cycles
    private val _completedCycles = MutableStateFlow(0)
    val completedCycles: StateFlow<Int> = _completedCycles.asStateFlow()

    private var timerJob: Job? = null
    private var focusDuration = 0
    private var restDuration = 0
    private var isManuallyCompleted = false

    fun getAllSessions(): Flow<List<FocusSessionModel>> {
        return repository.getAllSessions()
    }

    fun startSession(focusId: Int, focusDurationMinutes: Int, restDurationMinutes: Int) {
        if (focusDurationMinutes <= 0 || restDurationMinutes <= 0) {
            _error.value = "Invalid duration values"
            return
        }

        focusDuration = focusDurationMinutes * 60
        restDuration = restDurationMinutes * 60

        viewModelScope.launch {
            try {
                _error.value = null
                val session = FocusSessionModel(
                    focusId = focusId,
                    currentPhase = SessionPhase.FOCUS
                )
                val savedSession = repository.insertSession(session)
                _currentSession.value = savedSession
                _timeLeft.value = focusDuration
                _currentPhase.value = SessionPhase.FOCUS
                _sessionCompleted.value = false
                _completedCycles.value = 0
                isManuallyCompleted = false
            } catch (e: Exception) {
                _error.value = "Failed to start session: ${e.message}"
            }
        }
    }

    fun toggleTimer() {
        val session = _currentSession.value
        if (session == null) {
            _error.value = "No active session"
            return
        }

        if (_isRunning.value) {
            pauseTimer()
        } else {
            resumeTimer()
        }
    }

    private fun resumeTimer() {
        if (_timeLeft.value <= 0) return

        _isRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            try {
                while (_isRunning.value && _timeLeft.value > 0 && !isManuallyCompleted) {
                    delay(1000L)
                    if (_isRunning.value && !isManuallyCompleted) {
                        _timeLeft.value = (_timeLeft.value - 1).coerceAtLeast(0)
                    }
                }

                // Only switch phase if not manually completed
                if (_timeLeft.value <= 0 && _isRunning.value && !isManuallyCompleted) {
                    switchPhase()
                }
            } catch (e: Exception) {
                _error.value = "Timer error: ${e.message}"
                _isRunning.value = false
            }
        }
    }

    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()

        viewModelScope.launch {
            try {
                _currentSession.value?.let { session ->
                    val updatedSession = session.copy(currentPhase = SessionPhase.PAUSED)
                    repository.updateSession(updatedSession)
                    _currentSession.value = updatedSession
                    _currentPhase.value = SessionPhase.PAUSED
                }
            } catch (e: Exception) {
                _error.value = "Failed to pause session: ${e.message}"
            }
        }
    }

    private fun switchPhase() {
        when (_currentPhase.value) {
            SessionPhase.FOCUS -> {
                // Switch to REST phase
                _currentPhase.value = SessionPhase.REST
                _timeLeft.value = restDuration

                // Update session in repository
                viewModelScope.launch {
                    _currentSession.value?.let { session ->
                        val updatedSession = session.copy(currentPhase = SessionPhase.REST)
                        repository.updateSession(updatedSession)
                        _currentSession.value = updatedSession
                    }
                }

                // Continue timer automatically
                resumeTimer()
            }
            SessionPhase.REST -> {
                // Complete one cycle and return to FOCUS
                _completedCycles.value = _completedCycles.value + 1
                _currentPhase.value = SessionPhase.FOCUS
                _timeLeft.value = focusDuration

                // Update session in repository
                viewModelScope.launch {
                    _currentSession.value?.let { session ->
                        val updatedSession = session.copy(currentPhase = SessionPhase.FOCUS)
                        repository.updateSession(updatedSession)
                        _currentSession.value = updatedSession
                    }
                }

                // Continue the loop automatically
                resumeTimer()
            }
            else -> {}
        }
    }

    // New function to manually complete the session
    fun completeSession() {
        isManuallyCompleted = true
        _isRunning.value = false
        timerJob?.cancel()
        _currentPhase.value = SessionPhase.COMPLETED
        _sessionCompleted.value = true

        viewModelScope.launch {
            _currentSession.value?.let { session ->
                val completedSession = session.copy(
                    endTime = System.currentTimeMillis(),
                    isCompleted = true,
                    currentPhase = SessionPhase.COMPLETED
                )
                repository.updateSession(completedSession)
                _currentSession.value = completedSession
            }
        }
    }

    fun resetTimer() {
        pauseTimer()
        _timeLeft.value = if (_currentPhase.value == SessionPhase.FOCUS) focusDuration else restDuration
    }

    fun skipPhase() {
        _timeLeft.value = 0
    }

    fun resetSession() {
        pauseTimer()
        _currentSession.value = null
        _timeLeft.value = 0
        _currentPhase.value = SessionPhase.FOCUS
        _sessionCompleted.value = false
        _completedCycles.value = 0
        isManuallyCompleted = false
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun clearError() {
        _error.value = null
    }
}