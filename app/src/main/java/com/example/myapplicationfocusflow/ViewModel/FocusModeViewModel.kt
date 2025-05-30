package com.example.myapplicationfocusflow.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationfocusflow.model.FocusModel
import com.example.myapplicationfocusflow.Repositories.FocusModeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusModeViewModel(private val repository: FocusModeRepository) : ViewModel() {

    private val _focusList = MutableStateFlow<List<FocusModel>>(emptyList())
    val focusList: StateFlow<List<FocusModel>> = _focusList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            loadFocusModels()
        }
    }


    private suspend fun loadFocusModels() {
        try {
            _isLoading.value = true
            _error.value = null
            repository.getAllFocusModels().collect { focusModels ->
                _focusList.value = focusModels.sortedByDescending { it.createdAt }
            }
        } catch (e: Exception) {
            _error.value = "Failed to load focus models: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun saveFocusModel(focusModel: FocusModel): Result<FocusModel> {
        return try {
            _error.value = null
            val saved = repository.insertFocusModel(focusModel)
            Result.success(saved)
        } catch (e: Exception) {
            _error.value = "Failed to save focus model: ${e.message}"
            Result.failure(e)
        }
    }

    fun updateFocusModel(focusModel: FocusModel) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.updateFocusModel(focusModel)
            } catch (e: Exception) {
                _error.value = "Failed to update focus model: ${e.message}"
            }
        }
    }

    suspend fun getFocusModelById(id: Int): FocusModel? {
        return repository.getFocusModelById(id)
    }

    fun deleteFocusModel(id: Int) {
        viewModelScope.launch {
            repository.deleteFocusModel(id)
        }
    }

    val usedCount: Int
        get() = _focusList.value.size

    val doneCount: Int
        get() = _focusList.value.count { it.isCompleted }

    fun clearError() {
        _error.value = null
    }
}

