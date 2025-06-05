package com.example.myapplicationfocusflow.model

import kotlinx.serialization.Serializable

@Serializable
data class FocusModel(
    val focus_id: Int = 0,
    val title: String = "",
    val category: String = "",
    val goals: String = "",
    val focusDuration: Int = 0, // in minutes
    val restDuration: Int = 0,  // in minutes
    val isCompleted: Boolean = false,
    val completedSessions: Int = 0,
    val totalSessions: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)