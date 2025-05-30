package com.example.myapplicationfocusflow.Repositories

import com.example.myapplicationfocusflow.model.AmbientSoundModel
import com.example.myapplicationfocusflow.model.DataStoreManager
import kotlinx.coroutines.flow.Flow

class AmbientSoundRepository(private val dataStoreManager: DataStoreManager) {

    fun getAllAmbientSounds(): Flow<List<AmbientSoundModel>> {
        return dataStoreManager.getAmbientSounds()
    }
}