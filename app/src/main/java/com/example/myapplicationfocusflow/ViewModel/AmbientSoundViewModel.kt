package com.example.myapplicationfocusflow.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationfocusflow.model.AmbientSoundModel
import com.example.myapplicationfocusflow.Repositories.AmbientSoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AmbientSoundViewModel(private val repository: AmbientSoundRepository) : ViewModel() {

    private val _ambientSounds = MutableStateFlow<List<AmbientSoundModel>>(emptyList())
    val ambientSounds: StateFlow<List<AmbientSoundModel>> = _ambientSounds.asStateFlow()

    init {
        loadAmbientSounds()
    }

    private fun loadAmbientSounds() {
        viewModelScope.launch {
            repository.getAllAmbientSounds().collect { sounds ->
                _ambientSounds.value = sounds
            }
        }
    }
}