package com.example.myapplicationfocusflow.Repositories

import com.example.myapplicationfocusflow.model.DataStoreManager
import com.example.myapplicationfocusflow.model.FocusModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FocusModeRepository(private val dataStoreManager: DataStoreManager) {

    fun getAllFocusModels(): Flow<List<FocusModel>> {
        return dataStoreManager.getFocusModels()
    }

    suspend fun insertFocusModel(focusModel: FocusModel): FocusModel {
        val currentList = dataStoreManager.getFocusModels().first()
        val newId = dataStoreManager.getNextFocusId()
        val newFocusModel = focusModel.copy(focus_id = newId)

        dataStoreManager.saveFocusModels(currentList + newFocusModel)
        dataStoreManager.incrementFocusId()

        return newFocusModel
    }

    suspend fun updateFocusModel(focusModel: FocusModel) {
        val currentList = dataStoreManager.getFocusModels().first()
        val updatedList = currentList.map {
            if (it.focus_id == focusModel.focus_id) focusModel else it
        }
        dataStoreManager.saveFocusModels(updatedList)
    }

    suspend fun getFocusModelById(id: Int): FocusModel? {
        return dataStoreManager.getFocusModels().first().find { it.focus_id == id }
    }

    suspend fun deleteFocusModel(id: Int) {
        val currentList = dataStoreManager.getFocusModels().first()
        val updatedList = currentList.filter { it.focus_id != id }
        dataStoreManager.saveFocusModels(updatedList)
    }
}

