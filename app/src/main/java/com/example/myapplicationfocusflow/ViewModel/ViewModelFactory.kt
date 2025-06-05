package com.example.myapplicationfocusflow.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplicationfocusflow.FocusFlowApplication


class ViewModelFactory(private val application: FocusFlowApplication) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            FocusModeViewModel::class.java -> {
                FocusModeViewModel(application.focusModeRepository) as T
            }
            FocusCategoryViewModel::class.java -> {
                FocusCategoryViewModel(application.focusCategoryRepository) as T
            }
            FocusSessionViewModel::class.java -> {
                FocusSessionViewModel(application.focusSessionRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}