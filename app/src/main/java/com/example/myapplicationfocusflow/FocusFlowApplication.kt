package com.example.myapplicationfocusflow


import android.app.Application
import com.example.myapplicationfocusflow.Repositories.FocusCategoryRepository
import com.example.myapplicationfocusflow.Repositories.FocusModeRepository
import com.example.myapplicationfocusflow.Repositories.FocusSessionRepository
import com.example.myapplicationfocusflow.model.DataStoreManager


class FocusFlowApplication : Application() {

    val dataStoreManager by lazy { DataStoreManager(this) }

    val focusModeRepository by lazy { FocusModeRepository(dataStoreManager) }
    val focusCategoryRepository by lazy { FocusCategoryRepository(dataStoreManager) }
    val focusSessionRepository by lazy { FocusSessionRepository(dataStoreManager) }
}
