package com.example.myapplicationfocusflow.model

import kotlinx.serialization.Serializable

@Serializable
data class AmbientSoundModel(
    val ambientSound_ID: Int = 0,
    val name: String = "",
    val fileUrl: String = ""
)