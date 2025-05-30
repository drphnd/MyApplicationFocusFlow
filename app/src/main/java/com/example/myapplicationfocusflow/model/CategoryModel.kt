package com.example.myapplicationfocusflow.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryModel(
    val category_ID: Int = 0,
    val name: String = ""
)
