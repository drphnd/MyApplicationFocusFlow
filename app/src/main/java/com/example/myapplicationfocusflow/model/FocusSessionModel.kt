package com.example.myapplicationfocusflow.model

import kotlinx.serialization.Serializable

@Serializable
data class FocusSessionModel(
    val sessionId: Int = 0,
    val focusId: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isCompleted: Boolean = false,
    val pausedDuration: Long = 0,
    val currentPhase: SessionPhase = SessionPhase.FOCUS
)

enum class SessionPhase {
    FOCUS, REST, COMPLETED, PAUSED
}